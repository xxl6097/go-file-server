package server

import (
	"encoding/json"
	"github.com/gorilla/mux"
	"github.com/xxl6097/go-server-file/internal/model"
	"github.com/xxl6097/go-server-file/pkg/apk"
	"github.com/xxl6097/go-server-file/pkg/file"
	"github.com/xxl6097/go-server-file/pkg/zip"
	"log"
	"net/http"
	"os"
	"path/filepath"
)

func (f *FileServer) hZip(w http.ResponseWriter, r *http.Request) {
	zip.CompressToZip(w, f.Root, f.getRealPath(r))
}

func (f *FileServer) hInfo(w http.ResponseWriter, r *http.Request) {
	path := mux.Vars(r)["path"]
	relPath := f.getRealPath(r)

	fi, err := os.Stat(relPath)
	if err != nil {
		http.Error(w, err.Error(), 500)
		return
	}
	fji := &model.FileJsonInfo{
		Name:    fi.Name(),
		Size:    fi.Size(),
		Path:    path,
		ModTime: fi.ModTime().UnixNano() / 1e6,
	}
	ext := filepath.Ext(path)
	switch ext {
	case ".md":
		fji.Type = "markdown"
	case ".apk":
		fji.Type = "apk"
		fji.Extra = apk.ParseApkInfo(relPath)
	case "":
		fji.Type = "dir"
	default:
		fji.Type = "text"
	}
	data, _ := json.Marshal(fji)
	w.Header().Set("Content-Type", "application/json")
	w.Write(data)
}

func (f *FileServer) hRaw(w http.ResponseWriter, r *http.Request) {
	path := mux.Vars(r)["path"]
	relPath := f.getRealPath(r)

	if file.IsNotExist(relPath) {
		http.Error(w, "file not exist", 500)
		return
	}

	log.Println(relPath, path)
	fileContent, err := os.ReadFile(relPath)
	if err != nil {
		http.Error(w, err.Error(), http.StatusNotFound)
		return
	}
	w.Write(fileContent)
}
