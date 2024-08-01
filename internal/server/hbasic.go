package server

import (
	"encoding/json"
	"fmt"
	"github.com/codeskyblue/go-accesslog"
	"github.com/goji/httpauth"
	"github.com/gorilla/handlers"
	"github.com/gorilla/mux"
	"github.com/xxl6097/go-file-server/internal/assets"
	"github.com/xxl6097/go-file-server/internal/auth/oauth2"
	"github.com/xxl6097/go-file-server/internal/auth/openid"
	"github.com/xxl6097/go-file-server/internal/logger"
	"github.com/xxl6097/go-file-server/internal/version"
	"github.com/xxl6097/go-file-server/pkg/file"
	"github.com/xxl6097/go-file-server/pkg/ip"
	"github.com/xxl6097/go-file-server/pkg/middle"
	"log"
	"net"
	"net/http"
	"path/filepath"
	"strconv"
	"strings"
)

func (this *FileServer) makeConfig() {
	this.Prefix = this.config.Prefix
	this.Theme = this.config.Theme
	this.Title = this.config.Title
	this.GoogleTrackerID = this.config.GoogleTrackerID
	this.Upload = this.config.Upload
	this.Delete = this.config.Delete
	this.Debug = this.config.Debug
	this.AuthType = this.config.Auth.Type
	this.NoLogin = this.config.NoLogin
	this.ShowDir = this.config.Showdir
	this.DeepPathMaxDepth = this.config.DeepPathMaxDepth
	log.Println(this)
}

func (f *FileServer) hbasicAuth() http.Handler {
	var hdlr http.Handler = f //f必须要有ServeHTTP函数

	hdlr = accesslog.NewLoggingHandler(hdlr, logger.HttpLogger{})
	// HTTP Basic Authentication
	userpass := strings.SplitN(f.config.Auth.HTTP, ":", 2)
	switch f.config.Auth.Type {
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
							f.hUp(w, r)
						default:
							//realPath := fmt.Sprintf(".%s", path)
							relativePath, err := filepath.Rel(f.Prefix, path)
							if err != nil {
								relativePath = path
							}
							realPath := filepath.Join(f.Root, relativePath)
							isDirOrFileExist := file.IsDirOrFileExist(realPath)
							isDir := file.IsDir(realPath)
							log.Println("路径", isDirOrFileExist, isDir, realPath)
							if !isDirOrFileExist || isDir {
								http.Error(w, http.StatusText(http.StatusUnauthorized), http.StatusUnauthorized)
							} else {
								f.ServeHTTP(w, r)
							}
						}
					} else {
						http.Error(w, http.StatusText(http.StatusUnauthorized), http.StatusUnauthorized)
						log.Println("认证失败", r.URL.Path)
					}
				}),
			})(hdlr)
		}
	case "openid":
		openid.HandleOpenID(f.config.Auth.OpenID, false) // FIXME(ssx): set secure default to false
		// case "github":
		// 	handleOAuth2ID(Gcfg.Auth.Type, Gcfg.Auth.ID, Gcfg.Auth.Secret) // FIXME(ssx): set secure default to false
	case "oauth2-proxy":
		oauth2.HandleOauth2()
	}

	// CORS

	hdlr = middle.Cors(hdlr)
	if f.config.XHeaders {
		hdlr = handlers.ProxyHeaders(hdlr)
	}

	return hdlr
}

func (f *FileServer) hSubRouter() *mux.Router {
	hdlr := f.hbasicAuth()
	mainRouter := mux.NewRouter()
	subRouter := mainRouter
	if f.config.Prefix != "" {
		subRouter = mainRouter.PathPrefix(f.config.Prefix).Subrouter()
		mainRouter.Handle(f.config.Prefix, hdlr)
		mainRouter.HandleFunc("/", func(w http.ResponseWriter, r *http.Request) {
			http.Redirect(w, r, f.config.Prefix, http.StatusTemporaryRedirect)
		})
	}

	subRouter.PathPrefix("/-/assets/").Handler(http.StripPrefix(f.config.Prefix+"/-/", http.FileServer(assets.Assets)))
	subRouter.HandleFunc("/-/sysinfo", func(w http.ResponseWriter, r *http.Request) {
		data, _ := json.Marshal(version.ToVersion())
		w.Header().Set("Content-Type", "application/json")
		w.Header().Set("Content-Length", fmt.Sprintf("%d", len(data)))
		w.Write(data)
	})
	subRouter.PathPrefix("/").Handler(hdlr)

	if f.config.Addr == "" {
		f.config.Addr = fmt.Sprintf(":%d", f.config.Port)
	}
	if !strings.Contains(f.config.Addr, ":") {
		f.config.Addr = ":" + f.config.Addr
	}
	_, port, _ := net.SplitHostPort(f.config.Addr)
	_port, err1 := strconv.Atoi(port)
	if err1 == nil {
		f.config.Port = _port
	}
	log.Printf("listening on %s, local address http://%s:%s\n", strconv.Quote(f.config.Addr), ip.GetHostIp(), port)
	return mainRouter
}
