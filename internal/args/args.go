package args

import (
	"bytes"
	"fmt"
	"github.com/alecthomas/kingpin"
	"github.com/xxl6097/go-server-file/internal/assets"
	"github.com/xxl6097/go-server-file/internal/model"
	"github.com/xxl6097/go-server-file/internal/version"
	"github.com/xxl6097/go-server-file/pkg/file"
	"gopkg.in/yaml.v2"
	"io/ioutil"
	"log"
	"os"
	"path/filepath"
	"regexp"
	"strings"
)

const YAMLCONF = ".ghs.yml"

var (
	DefaultPlistProxy = "https://plistproxy.herokuapp.com/plist"
	defaultOpenID     = "https://login.netease.com/openid"
	cfg               = model.Configure{}
	isCopyReadMe      bool
)

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

func ParseFlags() error {
	// initial default conf
	cfg.Root = "./"
	cfg.Port = 8000
	cfg.Addr = ""
	cfg.Theme = "black"
	cfg.PlistProxy = DefaultPlistProxy
	cfg.Auth.OpenID = defaultOpenID
	cfg.GoogleTrackerID = "UA-81205425-2"
	cfg.Title = "Go HTTP File Server"
	cfg.DeepPathMaxDepth = 5
	cfg.NoIndex = false

	kingpin.HelpFlag.Short('h')
	//kingpin.Version(versionMessage())
	kingpin.Flag("conf", "config file path, yaml format").FileVar(&cfg.Conf)
	kingpin.Flag("root", "root directory, default ./").Short('r').StringVar(&cfg.Root)
	kingpin.Flag("prefix", "url prefix, eg /foo").StringVar(&cfg.Prefix)
	kingpin.Flag("keyword", "不能说的秘密, eg 愚蠢").StringVar(&cfg.Keyword)
	kingpin.Flag("port", "listen port, default 8000").IntVar(&cfg.Port)
	kingpin.Flag("addr", "listen address, eg 127.0.0.1:8000").Short('a').StringVar(&cfg.Addr)
	kingpin.Flag("cert", "tls cert.pem path").StringVar(&cfg.Cert)
	kingpin.Flag("key", "tls key.pem path").StringVar(&cfg.Key)
	kingpin.Flag("auth-type", "Auth type <http|openid>").StringVar(&cfg.Auth.Type)
	kingpin.Flag("auth-http", "HTTP basic auth (ex: user:pass)").StringVar(&cfg.Auth.HTTP)
	kingpin.Flag("auth-openid", "OpenID auth identity url").StringVar(&cfg.Auth.OpenID)
	kingpin.Flag("theme", "web theme, one of <black|green>").StringVar(&cfg.Theme)
	kingpin.Flag("upload", "enable upload support").BoolVar(&cfg.Upload)
	kingpin.Flag("delete", "enable delete support").BoolVar(&cfg.Delete)
	kingpin.Flag("nologin", "nologin").BoolVar(&cfg.NoLogin)
	kingpin.Flag("xheaders", "used when behide nginx").BoolVar(&cfg.XHeaders)
	kingpin.Flag("debug", "enable debug mode").BoolVar(&cfg.Debug)
	kingpin.Flag("plistproxy", "plist proxy when server is not https").Short('p').StringVar(&cfg.PlistProxy)
	kingpin.Flag("title", "server title").StringVar(&cfg.Title)
	kingpin.Flag("google-tracker-id", "set to empty to disable it").StringVar(&cfg.GoogleTrackerID)
	kingpin.Flag("deep-path-max-depth", "set to -1 to not combine dirs").IntVar(&cfg.DeepPathMaxDepth)
	kingpin.Flag("no-index", "disable indexing").BoolVar(&cfg.NoIndex)

	kingpin.Parse() // first parse conf

	if cfg.Conf != nil {
		defer func() {
			kingpin.Parse() // command line priority high than conf
		}()
		ymlData, err := ioutil.ReadAll(cfg.Conf)
		if err != nil {
			return err
		}
		return yaml.Unmarshal(ymlData, &cfg)
	}
	return nil
}

func LoadAgrs() *model.Configure {
	if cfg.Debug {
		data, _ := yaml.Marshal(cfg)
		fmt.Printf("--- config ---\n%s\n", string(data))
	}
	log.SetFlags(log.Lshortfile | log.LstdFlags)

	// make sure prefix matches: ^/.*[^/]$
	cfg.Prefix = fixPrefix(cfg.Prefix)
	if cfg.Prefix != "" {
		log.Printf("url prefix: %s", cfg.Prefix)
	}

	cfg.Title = fmt.Sprintf("%s %s", cfg.Title, version.BuildVersion)
	return &cfg
}
func copys() {
	readmepath := filepath.Join(cfg.Root, ".ext")
	if _, err := os.Stat(readmepath); os.IsNotExist(err) {
		file.CopyFile(bytes.NewReader([]byte(assets.ExtArrayContent)), readmepath)
	}

	ghspath := filepath.Join(cfg.Root, ".ghs.yml")
	if _, err := os.Stat(ghspath); os.IsNotExist(err) {
		file.CopyFile(bytes.NewReader([]byte(assets.GhsContent)), ghspath)
	}
}

func CopyConfigFile(newstring string) {
	if isCopyReadMe {
		return
	}
	readmepath := filepath.Join(cfg.Root, "README.md")
	if _, err := os.Stat(readmepath); os.IsNotExist(err) {
		isCopyReadMe = true
		configData, err1 := file.ReadAssetsFile("assets/README.md", assets.AssetsFS)
		if err1 == nil {
			content := string(configData)
			content = strings.ReplaceAll(content, "127.0.0.1:8000", newstring)
			content = strings.ReplaceAll(content, "admin:admin", cfg.Auth.HTTP)
			file.CopyFile(bytes.NewReader([]byte(content)), readmepath)
		}
	}
	copys()
	file.ReadExt()
}
