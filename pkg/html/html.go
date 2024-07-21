package html

import (
	"bytes"
	"encoding/json"
	"github.com/gorilla/mux"
	"github.com/xxl6097/go-server-file/pkg/file"
	"io/ioutil"
	"log"
	"net/http"
	"net/url"
	"path/filepath"
	"strings"
)

func CombineURL(r *http.Request, path string) *url.URL {
	return &url.URL{
		Scheme: r.URL.Scheme,
		Host:   r.Host,
		Path:   path,
	}
}

// GetRealPath 获取真实路径 Return real path with Seperator(/)
func GetRealPath(root, prefix string, r *http.Request) string {
	path := mux.Vars(r)["path"]
	if !strings.HasPrefix(path, "/") {
		path = "/" + path
	}
	path = filepath.Clean(path) // prevent .. for safe issues
	relativePath, err := filepath.Rel(prefix, path)
	if err != nil {
		relativePath = path
	}
	realPath := filepath.Join(root, relativePath)
	return filepath.ToSlash(realPath)
}

func GenPlistLink(plistProxy, defaultPlistProxy, httpPlistLink string) (plistUrl string, err error) {
	// Maybe need a proxy, a little slowly now.
	pp := plistProxy
	if pp == "" {
		pp = defaultPlistProxy
	}
	resp, err := http.Get(httpPlistLink)
	if err != nil {
		return
	}
	defer resp.Body.Close()

	data, _ := ioutil.ReadAll(resp.Body)
	retData, err := http.Post(pp, "text/xml", bytes.NewBuffer(data))
	if err != nil {
		return
	}
	defer retData.Body.Close()

	jsonData, _ := ioutil.ReadAll(retData.Body)
	var ret map[string]string
	if err = json.Unmarshal(jsonData, &ret); err != nil {
		return
	}
	plistUrl = pp + "/" + ret["key"]
	return
}

func HFileSave(root, prefix string, w http.ResponseWriter, r *http.Request) {
	path := mux.Vars(r)["path"]
	relPath := GetRealPath(root, prefix, r)

	if file.IsNotExist(relPath) {
		http.Error(w, "file not exist", 500)
		return
	}

	log.Println(relPath, path)
	fileContent, err := ioutil.ReadAll(r.Body)
	if err != nil {
		http.Error(w, err.Error(), http.StatusInternalServerError)
		return
	}
	err = ioutil.WriteFile(relPath, fileContent, 0644)
	if err != nil {
		http.Error(w, err.Error(), http.StatusInternalServerError)
		return
	}
	w.WriteHeader(http.StatusOK)
}
