package server

import (
	"github.com/gorilla/mux"
	"github.com/xxl6097/go-server-file/internal/iface"
	"github.com/xxl6097/go-server-file/internal/model"
	"github.com/xxl6097/go-server-file/pkg/html"
	"log"
	"net/http"
	"path/filepath"
	"strings"
	"sync"
	"time"
)

var dirInfoSize = model.Directory{Size: make(map[string]int64), Mutex: &sync.RWMutex{}}

// FileServer var dirInfoSize = Directory{size: make(map[string]int64), mutex: &sync.RWMutex{}}
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
}

func NewFileServer(root string, noIndex bool) iface.IFileServer {
	root = filepath.ToSlash(filepath.Clean(root))
	if !strings.HasSuffix(root, "/") {
		root = root + "/"
	}
	log.Printf("root path: %s\n", root)
	this := FileServer{
		Root:   root,
		router: mux.NewRouter(),
		Theme:  "black",
		bufPool: sync.Pool{
			New: func() interface{} { return make([]byte, 32*1024) },
		},
		NoIndex: noIndex,
	}
	if !noIndex {
		go this.index()
	}
	return &this
}

func (f *FileServer) LoadConfig(configure *model.Configure) {
	if configure == nil {
		log.Fatal("config is nil")
	}
	f.config = configure
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
		//time.Sleep(time.Second * 1)
		time.Sleep(time.Minute * 10)
	}
}

func (f *FileServer) getRealPath(r *http.Request) string {
	return html.GetRealPath(f.Root, f.Prefix, r)
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

func (f *FileServer) historyDirSize(dir string) int64 {
	dirInfoSize.Mutex.RLock()
	size, ok := dirInfoSize.Size[dir]
	dirInfoSize.Mutex.RUnlock()

	if ok {
		return size
	}

	for _, fitem := range f.indexes {
		if filepath.HasPrefix(fitem.Path, dir) {
			size += fitem.Info.Size()
		}
	}

	dirInfoSize.Mutex.Lock()
	dirInfoSize.Size[dir] = size
	dirInfoSize.Mutex.Unlock()

	return size
}
