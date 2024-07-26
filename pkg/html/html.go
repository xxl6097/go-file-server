package html

import (
	"bytes"
	"encoding/json"
	"fmt"
	"github.com/gorilla/mux"
	"github.com/xxl6097/go-serverfile/pkg/file"
	"io/ioutil"
	"log"
	"mime"
	"net/http"
	"net/textproto"
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

//// GetFilenameFromContentDisposition 从请求中获取Content-Disposition头部中的filename字段
//func GetFilenameFromContentDisposition(r *http.Request) (string, error) {
//	// 获取Content-Disposition头信息
//	contentDisposition := r.Header.Get("Content-Disposition")
//	if contentDisposition == "" {
//		return "", nil // 如果没有找到Content-Disposition头信息，则返回nil
//	}
//
//	// 分割Content-Disposition头部的各个部分
//	dispositionParts := strings.Split(contentDisposition, ";")
//
//	// 遍历各个部分，寻找filename
//	for _, part := range dispositionParts {
//		part = strings.TrimSpace(part) // 去除空白字符
//		if strings.HasPrefix(part, "filename=") {
//			// 提取filename的值
//			quoteIndex := strings.IndexByte(part, '"')
//			if quoteIndex == -1 {
//				// 如果没有找到引号，尝试获取filename=之后的值
//				return strings.TrimPrefix(part, "filename="), nil
//			}
//			// 如果找到了引号，获取引号之间的值
//			return part[quoteIndex+1 : strings.LastIndex(part, '"')], nil
//		}
//	}
//
//	// 未找到filename字段
//	return "", nil
//}

// textproto.MIMEHeader
func GetFileNameFromMIMEHeader(header textproto.MIMEHeader) (string, error) {
	contentDisposition := header.Get("Content-Disposition")
	if contentDisposition == "" {
		return "", fmt.Errorf("Content-Disposition header not found")
	}

	_, params, err := mime.ParseMediaType(contentDisposition)
	if err != nil {
		return "", fmt.Errorf("failed to parse Content-Disposition header: %v", err)
	}

	filename, ok := params["filename"]
	if !ok {
		return "", fmt.Errorf("filename not found in Content-Disposition header")
	}

	return filename, nil
}

func GetFileNameFromHeader(header http.Header) (string, error) {
	contentDisposition := header.Get("Content-Disposition")
	if contentDisposition == "" {
		return "", fmt.Errorf("Content-Disposition header not found")
	}

	_, params, err := mime.ParseMediaType(contentDisposition)
	if err != nil {
		return "", fmt.Errorf("failed to parse Content-Disposition header: %v", err)
	}

	filename, ok := params["filename"]
	if !ok {
		return "", fmt.Errorf("filename not found in Content-Disposition header")
	}

	return filename, nil
}
