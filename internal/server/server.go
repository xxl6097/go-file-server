package server

import (
	"fmt"
	"github.com/gorilla/mux"
	"github.com/xxl6097/go-server-file/internal/assets"
	"github.com/xxl6097/go-server-file/internal/iface"
	"github.com/xxl6097/go-server-file/internal/model"
	"github.com/xxl6097/go-server-file/internal/version"
	"html/template"
	"log"
	"net/http"
	"net/url"
	"path/filepath"
	"strconv"
	"strings"
	"sync"
	"time"
)

type FileServer struct {
	Root             string
	Prefix           string
	Upload           bool
	Delete           bool
	Title            string
	Theme            string
	PlistProxy       string
	GoogleTrackerID  string
	AuthType         string
	DeepPathMaxDepth int
	NoIndex          bool
	NoLogin          bool

	config  *model.Configure
	indexes []model.IndexFileItem
	router  *mux.Router
	bufPool sync.Pool // use sync.Pool caching buf to reduce gc ratio

	_tmpls  map[string]*template.Template
	funcMap template.FuncMap

	dirInfoSize model.Directory
}

func NewFileServer(cfg *model.Configure) iface.IFileServer {
	if cfg == nil {
		log.Fatal("cfg is nil")
	}
	root := cfg.Root
	root = filepath.ToSlash(filepath.Clean(root))
	if !strings.HasSuffix(root, "/") {
		root = root + "/"
	}
	log.Printf("root path: %s\n", root)
	this := FileServer{
		Root:   root,
		config: cfg,
		router: mux.NewRouter(),
		Theme:  "black",
		bufPool: sync.Pool{
			New: func() interface{} { return make([]byte, 32*1024) },
		},
		NoIndex:     cfg.NoIndex,
		_tmpls:      make(map[string]*template.Template),
		dirInfoSize: model.Directory{Size: make(map[string]int64), Mutex: &sync.RWMutex{}},
	}
	this.makeFuncMap()
	if !cfg.NoIndex {
		go this.index()
	}
	this.makeHandleFunc()
	this.makeConfig()
	return &this
}

func (f *FileServer) makeFuncMap() {
	f.funcMap = template.FuncMap{
		"title": strings.Title,
		"urlhash": func(path string) string {
			httpFile, err := assets.Assets.Open(path)
			if err != nil {
				return path + "#no-such-file"
			}
			info, err := httpFile.Stat()
			if err != nil {
				return path + "#stat-error"
			}
			return fmt.Sprintf("%s?t=%d", path, info.ModTime().Unix())
		},
	}
}

func (s *FileServer) ServeHTTP(w http.ResponseWriter, r *http.Request) {
	s.router.ServeHTTP(w, r)
}

func (f *FileServer) Serve() {
	if f.config.PlistProxy != "" {
		u, err := url.Parse(f.config.PlistProxy)
		if err != nil {
			log.Fatal(err)
		}
		u.Scheme = "https"
		f.PlistProxy = u.String()
	}
	if f.PlistProxy != "" {
		log.Printf("plistproxy: %s", strconv.Quote(f.PlistProxy))
	}
	version.Version()
	mainRouter := f.hSubRouter()
	srv := &http.Server{
		Handler: mainRouter,
		Addr:    f.config.Addr,
	}
	var err error
	if f.config.Key != "" && f.config.Cert != "" {
		err = srv.ListenAndServeTLS(f.config.Cert, f.config.Key)
	} else {
		err = srv.ListenAndServe()
	}
	log.Fatal(err)
}

func (f *FileServer) makeHandleFunc() {
	f.router.HandleFunc("/-/ipa/plist/{path:.*}", f.hPlist)
	f.router.HandleFunc("/-/ipa/link/{path:.*}", f.hIpaLink)
	f.router.HandleFunc("/up", f.hUp).Methods("GET")
	f.router.HandleFunc("/{path:.*}", f.hIndex).Methods("GET", "HEAD")
	f.router.HandleFunc("/{path:.*}", f.hFile).Methods(http.MethodPut)
	f.router.HandleFunc("/{path:.*}", f.hUploadOrMkdir).Methods("POST")
	f.router.HandleFunc("/{path:.*}", f.hDelete).Methods("DELETE")
}

func (f *FileServer) index() {
	time.Sleep(1 * time.Second)
	for {
		startTime := time.Now()
		log.Println("Started making search index")
		f.makeIndex()
		log.Printf("Completed search index in %v", time.Since(startTime))
		time.Sleep(time.Minute * 10)
	}
}

func (f *FileServer) findIndex(text string) []model.IndexFileItem {
	ret := make([]model.IndexFileItem, 0)
	for _, item := range f.indexes {
		ok := true
		// search algorithm, space for AND
		for _, keyword := range strings.Fields(text) {
			needContains := true
			if strings.HasPrefix(keyword, "-") {
				needContains = false
				keyword = keyword[1:]
			}
			if keyword == "" {
				continue
			}
			ok = (needContains == strings.Contains(strings.ToLower(item.Path), strings.ToLower(keyword)))
			if !ok {
				break
			}
		}
		if ok {
			ret = append(ret, item)
		}
	}
	return ret
}
