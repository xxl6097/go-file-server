package server

import (
	"github.com/xxl6097/go-serverfile/internal/access"
	"github.com/xxl6097/go-serverfile/internal/args"
	"github.com/xxl6097/go-serverfile/pkg/file"
	"gopkg.in/yaml.v2"
	"io/ioutil"
	"log"
	"os"
	"path/filepath"
)

func (s *FileServer) defaultAccessConf() access.Access {
	return access.Access{
		Upload: s.Upload,
		Delete: s.Delete,
	}
}

func (s *FileServer) readAccessConf(realPath string) (ac access.Access) {
	relativePath, err := filepath.Rel(s.Root, realPath)
	if err != nil || relativePath == "." || relativePath == "" { // actually relativePath is always "." if root == realPath
		ac = s.defaultAccessConf()
		realPath = s.Root
	} else {
		parentPath := filepath.Dir(realPath)
		ac = s.readAccessConf(parentPath)
	}
	if file.IsFile(realPath) {
		realPath = filepath.Dir(realPath)
	}
	cfgFile := filepath.Join(realPath, args.YAMLCONF)
	data, err := ioutil.ReadFile(cfgFile)
	if err != nil {
		if os.IsNotExist(err) {
			return
		}
		log.Printf("Err read .ghs.yml: %v", err)
	}
	err = yaml.Unmarshal(data, &ac)
	if err != nil {
		log.Printf("Err format .ghs.yml: %v", err)
	}
	return
}
