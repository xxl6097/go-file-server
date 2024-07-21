package internal

import (
	"bytes"
	"fmt"
	"github.com/xxl6097/go-server-file/internal/assets"
	"github.com/xxl6097/go-server-file/internal/model"
	"github.com/xxl6097/go-server-file/pkg/file"
	"html/template"
	"net/http"
	"os"
	"path/filepath"
	"strings"
)

const YAMLCONF = ".ghs.yml"

var (
	DefaultPlistProxy = "https://plistproxy.herokuapp.com/plist"
	defaultOpenID     = "https://login.netease.com/openid"
	Gcfg              = model.Configure{}
	copyFiles         = []string{".ghs.yml", ".ext", ".extent"}
	VERSION           = "unknown"
	BUILDTIME         = "unknown time"
	GITCOMMIT         = "unknown git commit"
	SITE              = "https://github.com/codeskyblue/gohttpserver"
	isCopyReadMe      bool
)

// TODO: I need to read more abouthtml/template
var (
	funcMap template.FuncMap
	_tmpls  = make(map[string]*template.Template)
)

func init() {
	funcMap = template.FuncMap{
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

func RenderHTML(w http.ResponseWriter, name string, v interface{}) {
	if t, ok := _tmpls[name]; ok {
		t.Execute(w, v)
		return
	}
	t := template.Must(template.New(name).Funcs(funcMap).Delims("[[", "]]").Parse(assets.AssetsContent(name)))
	_tmpls[name] = t
	t.Execute(w, v)
}

func copys() {
	readmepath := filepath.Join(Gcfg.Root, ".ext")
	if _, err := os.Stat(readmepath); os.IsNotExist(err) {
		file.CopyFile(bytes.NewReader([]byte(assets.ExtArrayContent)), readmepath)
	}

	ghspath := filepath.Join(Gcfg.Root, ".ghs.yml")
	if _, err := os.Stat(ghspath); os.IsNotExist(err) {
		file.CopyFile(bytes.NewReader([]byte(assets.GhsContent)), ghspath)
	}
}

func CopyConfigFile(newstring string) {
	if isCopyReadMe {
		return
	}
	readmepath := filepath.Join(Gcfg.Root, "README.md")
	if _, err := os.Stat(readmepath); os.IsNotExist(err) {
		isCopyReadMe = true
		configData, err1 := file.ReadAssetsFile("assets/README.md", assets.AssetsFS)
		if err1 == nil {
			content := string(configData)
			content = strings.ReplaceAll(content, "127.0.0.1:8000", newstring)
			content = strings.ReplaceAll(content, "admin:admin", Gcfg.Auth.HTTP)
			file.CopyFile(bytes.NewReader([]byte(content)), readmepath)
		}
	}
	copys()
	file.ReadExt()
}

func Execute() {
}
