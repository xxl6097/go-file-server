package server

import (
	"encoding/json"
	"github.com/gorilla/mux"
	"github.com/xxl6097/go-serverfile/internal/model"
	"github.com/xxl6097/go-serverfile/pkg/apk"
	"github.com/xxl6097/go-serverfile/pkg/file"
	"github.com/xxl6097/go-serverfile/pkg/zip"
	"io/ioutil"
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

func (f *FileServer) scanDir(realPath string) ([]byte, error) {
	infos, err := ioutil.ReadDir(realPath)
	if err != nil {
		return nil, err
	}
	fileInfoMap := make(map[string]os.FileInfo, 0)
	for _, info := range infos {
		fileInfoMap[info.Name()] = info
	}

	maxDepth := f.DeepPathMaxDepth

	lrs := make([]model.HTTPFileInfo, 0)
	for path, info := range fileInfoMap {
		lr := model.HTTPFileInfo{
			Name:    info.Name(),
			Path:    path,
			ModTime: info.ModTime().UnixNano() / 1e6,
		}
		if info.IsDir() {
			name := file.DeepPath(realPath, info.Name(), maxDepth)
			lr.Name = name
			lr.Path = filepath.Join(filepath.Dir(path), name)
			lr.Type = "dir"
			lr.Size = f.historyDirSize(lr.Path)
		} else {
			lr.Type = "file"
			lr.Size = info.Size() // formatSize(info)
		}
		lrs = append(lrs, lr)
	}

	data, _ := json.Marshal(map[string]interface{}{
		"files": lrs,
	})
	return data, nil
}

func (f *FileServer) hJSONList(w http.ResponseWriter, r *http.Request) {
	requestPath := mux.Vars(r)["path"]
	realPath := f.getRealPath(r)
	search := r.FormValue("search")
	auth := f.readAccessConf(realPath)
	accessConf := auth.GetAccess()
	accessConf.Upload = auth.CanUpload(r)
	accessConf.Delete = auth.CanDelete(r)
	maxDepth := f.DeepPathMaxDepth

	// path string -> info os.FileInfo
	fileInfoMap := make(map[string]os.FileInfo, 0)

	if search != "" {
		//if strings.HasPrefix(search, f.config.Keyword) {
		//	paths := strings.Split(search, "-")
		//	if paths != nil && len(paths) >= 3 {
		//		if strings.EqualFold(paths[1], f.config.Auth.HTTP) {
		//			ok := file.IsDirOrFileExist(paths[2])
		//			log.Println("dir path:", ok, paths[2])
		//			if ok {
		//				realPath = paths[2]
		//				f.Root = realPath
		//				infos, err := ioutil.ReadDir(realPath)
		//				if err != nil {
		//					http.Error(w, err.Error(), 500)
		//					return
		//				}
		//				for _, info := range infos {
		//					fileInfoMap[filepath.Join(requestPath, info.Name())] = info
		//				}
		//			}
		//		} else {
		//			f.Root = f.config.Root
		//		}
		//	} else {
		//		f.Root = f.config.Root
		//	}
		//} else {
		//
		//}
		results := f.findIndex(search)
		if len(results) > 50 { // max 50
			results = results[:50]
		}
		for _, item := range results {
			if filepath.HasPrefix(item.Path, requestPath) {
				fileInfoMap[item.Path] = item.Info
			}
		}
	} else {
		infos, err := ioutil.ReadDir(realPath)
		if err != nil {
			http.Error(w, err.Error(), 500)
			return
		}
		for _, info := range infos {
			filePath := filepath.Join(requestPath, info.Name())
			fileInfoMap[filePath] = info
		}
	}

	// turn file list -> json
	lrs := make([]model.HTTPFileInfo, 0)
	for path, info := range fileInfoMap {
		if !auth.CanAccess(info.Name()) {
			continue
		}
		lr := model.HTTPFileInfo{
			Name:    info.Name(),
			Path:    path,
			ModTime: info.ModTime().UnixNano() / 1e6,
		}
		if search != "" {
			name, err := filepath.Rel(requestPath, path)
			if err != nil {
				log.Println(requestPath, path, err)
			}
			lr.Name = filepath.ToSlash(name) // fix for windows
		}
		if info.IsDir() {
			name := file.DeepPath(realPath, info.Name(), maxDepth)
			lr.Name = name
			lr.Path = filepath.Join(filepath.Dir(path), name)
			lr.Type = "dir"
			lr.Size = f.historyDirSize(lr.Path)
		} else {
			lr.Type = "file"
			lr.Size = info.Size() // formatSize(info)
		}
		lrs = append(lrs, lr)
	}

	data, _ := json.Marshal(map[string]interface{}{
		"files": lrs,
		"auth":  auth,
	})
	w.Header().Set("Content-Type", "application/json")
	w.Write(data)
}
