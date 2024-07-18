package main

import (
	"bytes"
	"encoding/json"
	"fmt"
	"github.com/alecthomas/kingpin"
	"github.com/codeskyblue/go-accesslog"
	"github.com/go-yaml/yaml"
	"github.com/goji/httpauth"
	"github.com/gorilla/handlers"
	"github.com/gorilla/mux"
	"github.com/xxl6097/go-server-file/version"
	"io/ioutil"
	"log"
	"net"
	"net/http"
	"net/url"
	"os"
	"path/filepath"
	"regexp"
	"runtime"
	"strconv"
	"strings"
	"text/template"
)

type Configure struct {
	Conf            *os.File `yaml:"-"`
	Addr            string   `yaml:"addr"`
	Keyword         string   `yaml:"keyword"`
	Port            int      `yaml:"port"`
	Root            string   `yaml:"root"`
	Prefix          string   `yaml:"prefix"`
	HTTPAuth        string   `yaml:"httpauth"`
	Cert            string   `yaml:"cert"`
	Key             string   `yaml:"key"`
	Theme           string   `yaml:"theme"`
	XHeaders        bool     `yaml:"xheaders"`
	Upload          bool     `yaml:"upload"`
	Delete          bool     `yaml:"delete"`
	PlistProxy      string   `yaml:"plistproxy"`
	Title           string   `yaml:"title"`
	Debug           bool     `yaml:"debug"`
	GoogleTrackerID string   `yaml:"google-tracker-id"`
	Auth            struct {
		Type   string `yaml:"type"` // openid|http|github
		OpenID string `yaml:"openid"`
		HTTP   string `yaml:"http"`
		ID     string `yaml:"id"`     // for oauth2
		Secret string `yaml:"secret"` // for oauth2
	} `yaml:"auth"`
	DeepPathMaxDepth int  `yaml:"deep-path-max-depth"`
	NoIndex          bool `yaml:"no-index"`
}

type httpLogger struct{}

func (l httpLogger) Log(record accesslog.LogRecord) {
	log.Printf("%s - %s %d %s", record.Ip, record.Method, record.Status, record.Uri)
}

var (
	DefaultPlistProxy = "https://plistproxy.herokuapp.com/plist"
	defaultOpenID     = "https://login.netease.com/openid"
	Gcfg              = Configure{}
	logger            = httpLogger{}

	VERSION      = "unknown"
	BUILDTIME    = "unknown time"
	GITCOMMIT    = "unknown git commit"
	SITE         = "https://github.com/codeskyblue/gohttpserver"
	isCopyReadMe bool
)

func versionMessage() string {
	t := template.Must(template.New("version").Parse(`GoHTTPServer
  Version:        {{.Version}}
  Go version:     {{.GoVersion}}
  OS/Arch:        {{.OSArch}}
  Git commit:     {{.GitCommit}}
  Built:          {{.Built}}
  Site:           {{.Site}}`))
	buf := bytes.NewBuffer(nil)
	t.Execute(buf, map[string]interface{}{
		"Version":   VERSION,
		"GoVersion": runtime.Version(),
		"OSArch":    runtime.GOOS + "/" + runtime.GOARCH,
		"GitCommit": GITCOMMIT,
		"Built":     BUILDTIME,
		"Site":      SITE,
	})
	return buf.String()
}

func parseFlags() error {
	// initial default conf
	Gcfg.Root = "./"
	Gcfg.Port = 8000
	Gcfg.Addr = ""
	Gcfg.Theme = "black"
	Gcfg.PlistProxy = DefaultPlistProxy
	Gcfg.Auth.OpenID = defaultOpenID
	Gcfg.GoogleTrackerID = "UA-81205425-2"
	Gcfg.Title = "Go HTTP File Server"
	Gcfg.DeepPathMaxDepth = 5
	Gcfg.NoIndex = false

	kingpin.HelpFlag.Short('h')
	kingpin.Version(versionMessage())
	kingpin.Flag("conf", "config file path, yaml format").FileVar(&Gcfg.Conf)
	kingpin.Flag("root", "root directory, default ./").Short('r').StringVar(&Gcfg.Root)
	kingpin.Flag("prefix", "url prefix, eg /foo").StringVar(&Gcfg.Prefix)
	kingpin.Flag("keyword", "不能说的秘密, eg 愚蠢").StringVar(&Gcfg.Keyword)
	kingpin.Flag("port", "listen port, default 8000").IntVar(&Gcfg.Port)
	kingpin.Flag("addr", "listen address, eg 127.0.0.1:8000").Short('a').StringVar(&Gcfg.Addr)
	kingpin.Flag("cert", "tls cert.pem path").StringVar(&Gcfg.Cert)
	kingpin.Flag("key", "tls key.pem path").StringVar(&Gcfg.Key)
	kingpin.Flag("auth-type", "Auth type <http|openid>").StringVar(&Gcfg.Auth.Type)
	kingpin.Flag("auth-http", "HTTP basic auth (ex: user:pass)").StringVar(&Gcfg.Auth.HTTP)
	kingpin.Flag("auth-openid", "OpenID auth identity url").StringVar(&Gcfg.Auth.OpenID)
	kingpin.Flag("theme", "web theme, one of <black|green>").StringVar(&Gcfg.Theme)
	kingpin.Flag("upload", "enable upload support").BoolVar(&Gcfg.Upload)
	kingpin.Flag("delete", "enable delete support").BoolVar(&Gcfg.Delete)
	kingpin.Flag("xheaders", "used when behide nginx").BoolVar(&Gcfg.XHeaders)
	kingpin.Flag("debug", "enable debug mode").BoolVar(&Gcfg.Debug)
	kingpin.Flag("plistproxy", "plist proxy when server is not https").Short('p').StringVar(&Gcfg.PlistProxy)
	kingpin.Flag("title", "server title").StringVar(&Gcfg.Title)
	kingpin.Flag("google-tracker-id", "set to empty to disable it").StringVar(&Gcfg.GoogleTrackerID)
	kingpin.Flag("deep-path-max-depth", "set to -1 to not combine dirs").IntVar(&Gcfg.DeepPathMaxDepth)
	kingpin.Flag("no-index", "disable indexing").BoolVar(&Gcfg.NoIndex)

	kingpin.Parse() // first parse conf

	if Gcfg.Conf != nil {
		defer func() {
			kingpin.Parse() // command line priority high than conf
		}()
		ymlData, err := ioutil.ReadAll(Gcfg.Conf)
		if err != nil {
			return err
		}
		return yaml.Unmarshal(ymlData, &Gcfg)
	}
	return nil
}

func fixPrefix(prefix string) string {
	prefix = regexp.MustCompile(`/*$`).ReplaceAllString(prefix, "")
	if !strings.HasPrefix(prefix, "/") {
		prefix = "/" + prefix
	}
	if prefix == "/" {
		prefix = ""
	}
	return prefix
}

func cors(next http.Handler) http.Handler {
	// access control and CORS middleware
	return http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		w.Header().Set("Access-Control-Allow-Origin", "*")
		w.Header().Set("Access-Control-Allow-Methods", "*")
		w.Header().Set("Access-Control-Allow-Headers", "*")
		if r.Method == "OPTIONS" {
			return
		}
		next.ServeHTTP(w, r)
	})
}

func copyReadMe(newstring string) {
	if isCopyReadMe {
		return
	}
	readmepath := filepath.Join(Gcfg.Root, "README.md")
	if _, err := os.Stat(readmepath); os.IsNotExist(err) {
		isCopyReadMe = true
		configData, err1 := ReadAssetsFile("assets/README.md", assetsFS)
		if err1 == nil {
			content := string(configData)
			content = strings.ReplaceAll(content, "127.0.0.1:8000", newstring)
			content = strings.ReplaceAll(content, "admin:admin", Gcfg.Auth.HTTP)
			CopyFile(bytes.NewReader([]byte(content)), readmepath)
		}
	}
}
func main() {
	if err := parseFlags(); err != nil {
		log.Fatal(err)
	}
	if Gcfg.Debug {
		data, _ := yaml.Marshal(Gcfg)
		fmt.Printf("--- config ---\n%s\n", string(data))
	}
	log.SetFlags(log.Lshortfile | log.LstdFlags)

	// make sure prefix matches: ^/.*[^/]$
	Gcfg.Prefix = fixPrefix(Gcfg.Prefix)
	if Gcfg.Prefix != "" {
		log.Printf("url prefix: %s", Gcfg.Prefix)
	}

	Gcfg.Title = fmt.Sprintf("%s v%s", Gcfg.Title, version.BuildVersion)
	server := NewHTTPStaticServer(Gcfg.Root, Gcfg.NoIndex)
	server.Prefix = Gcfg.Prefix
	server.Theme = Gcfg.Theme
	server.Title = Gcfg.Title
	server.GoogleTrackerID = Gcfg.GoogleTrackerID
	server.Upload = Gcfg.Upload
	server.Delete = Gcfg.Delete
	server.AuthType = Gcfg.Auth.Type
	server.DeepPathMaxDepth = Gcfg.DeepPathMaxDepth

	if Gcfg.PlistProxy != "" {
		u, err := url.Parse(Gcfg.PlistProxy)
		if err != nil {
			log.Fatal(err)
		}
		u.Scheme = "https"
		server.PlistProxy = u.String()
	}
	if server.PlistProxy != "" {
		log.Printf("plistproxy: %s", strconv.Quote(server.PlistProxy))
	}

	if os.Getenv("FRP_DOWN") == "true" {
		go FrpcDown(Gcfg.Root)
	}

	var hdlr http.Handler = server

	hdlr = accesslog.NewLoggingHandler(hdlr, logger)

	// HTTP Basic Authentication
	userpass := strings.SplitN(Gcfg.Auth.HTTP, ":", 2)
	switch Gcfg.Auth.Type {
	case "http":
		if len(userpass) == 2 {
			user, password := userpass[0], userpass[1]
			//hdlr = httpauth.SimpleBasicAuth(user, password)(hdlr)
			hdlr = httpauth.BasicAuth(httpauth.AuthOptions{
				Realm:    "Restricted",
				User:     user,
				Password: password,
				UnauthorizedHandler: http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
					if strings.EqualFold(r.Method, http.MethodGet) {
						path := r.URL.Path
						switch path {
						case "/up":
							server.up(w, r)
						default:
							realPath := fmt.Sprintf(".%s", path)
							if !IsDirOrFileExist(realPath) || IsDir(realPath) {
								http.Error(w, http.StatusText(http.StatusUnauthorized), http.StatusUnauthorized)
							} else {
								server.ServeHTTP(w, r)
							}
						}
					} else {
						http.Error(w, http.StatusText(http.StatusUnauthorized), http.StatusUnauthorized)
					}
				}),
			})(hdlr)
		}
	case "openid":
		HandleOpenID(Gcfg.Auth.OpenID, false) // FIXME(ssx): set secure default to false
		// case "github":
		// 	handleOAuth2ID(Gcfg.Auth.Type, Gcfg.Auth.ID, Gcfg.Auth.Secret) // FIXME(ssx): set secure default to false
	case "oauth2-proxy":
		HandleOauth2()
	}

	// CORS
	hdlr = cors(hdlr)

	if Gcfg.XHeaders {
		hdlr = handlers.ProxyHeaders(hdlr)
	}

	mainRouter := mux.NewRouter()
	router := mainRouter
	if Gcfg.Prefix != "" {
		router = mainRouter.PathPrefix(Gcfg.Prefix).Subrouter()
		mainRouter.Handle(Gcfg.Prefix, hdlr)
		mainRouter.HandleFunc("/", func(w http.ResponseWriter, r *http.Request) {
			http.Redirect(w, r, Gcfg.Prefix, http.StatusTemporaryRedirect)
		})
	}

	router.PathPrefix("/-/assets/").Handler(http.StripPrefix(Gcfg.Prefix+"/-/", http.FileServer(Assets)))
	router.HandleFunc("/-/sysinfo", func(w http.ResponseWriter, r *http.Request) {
		data, _ := json.Marshal(map[string]interface{}{
			"version": VERSION,
		})
		w.Header().Set("Content-Type", "application/json")
		w.Header().Set("Content-Length", fmt.Sprintf("%d", len(data)))
		w.Write(data)
	})
	router.PathPrefix("/").Handler(hdlr)

	if Gcfg.Addr == "" {
		Gcfg.Addr = fmt.Sprintf(":%d", Gcfg.Port)
	}
	if !strings.Contains(Gcfg.Addr, ":") {
		Gcfg.Addr = ":" + Gcfg.Addr
	}
	_, port, _ := net.SplitHostPort(Gcfg.Addr)
	_port, err1 := strconv.Atoi(port)
	if err1 == nil {
		Gcfg.Port = _port
	}
	log.Printf("listening on %s, local address http://%s:%s\n", strconv.Quote(Gcfg.Addr), GetLocalIP(), port)

	srv := &http.Server{
		Handler: mainRouter,
		Addr:    Gcfg.Addr,
	}

	var err error
	if Gcfg.Key != "" && Gcfg.Cert != "" {
		err = srv.ListenAndServeTLS(Gcfg.Cert, Gcfg.Key)
	} else {
		err = srv.ListenAndServe()
	}
	log.Fatal(err)
}
