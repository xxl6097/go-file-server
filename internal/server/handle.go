package server

import (
	"encoding/base64"
	"encoding/json"
	"fmt"
	"github.com/gorilla/mux"
	"github.com/xxl6097/go-serverfile/internal/args"
	"github.com/xxl6097/go-serverfile/internal/assets"
	"github.com/xxl6097/go-serverfile/internal/model"
	file2 "github.com/xxl6097/go-serverfile/pkg/file"
	"github.com/xxl6097/go-serverfile/pkg/html"
	"github.com/xxl6097/go-serverfile/pkg/ipa"
	"github.com/xxl6097/go-serverfile/pkg/zip"
	"io"
	"io/ioutil"
	"log"
	"net/http"
	"net/url"
	"os"
	"path/filepath"
	"strconv"
	"strings"
)

func (f *FileServer) makeIndex() error {
	var indexes = make([]model.IndexFileItem, 0)
	var err = filepath.Walk(f.Root, func(path string, info os.FileInfo, err error) error {
		if err != nil {
			log.Printf("WARN: Visit path: %s error: %v", strconv.Quote(path), err)
			return filepath.SkipDir
			// return err
		}
		if info.IsDir() {
			return nil
		}

		path, _ = filepath.Rel(f.Root, path)
		path = filepath.ToSlash(path)
		indexes = append(indexes, model.IndexFileItem{path, info})
		return nil
	})
	f.indexes = indexes
	return err
}

func (f *FileServer) hPlist(w http.ResponseWriter, r *http.Request) {
	path := mux.Vars(r)["path"]
	// rename *.plist to *.ipa
	if filepath.Ext(path) == ".plist" {
		path = path[0:len(path)-6] + ".ipa"
	}

	relPath := f.getRealPath(r)
	plinfo, err := ipa.ParseIPA(relPath)
	if err != nil {
		http.Error(w, err.Error(), 500)
		return
	}

	scheme := "http"
	if r.TLS != nil {
		scheme = "https"
	}
	baseURL := &url.URL{
		Scheme: scheme,
		Host:   r.Host,
	}
	data, err := ipa.GenerateDownloadPlist(baseURL, path, plinfo)
	if err != nil {
		http.Error(w, err.Error(), 500)
		return
	}
	w.Header().Set("Content-Type", "text/xml")
	w.Write(data)
}

func (f *FileServer) hIpaLink(w http.ResponseWriter, r *http.Request) {
	path := mux.Vars(r)["path"]
	var plistUrl string

	if r.URL.Scheme == "https" {
		plistUrl = html.CombineURL(r, "/-/ipa/plist/"+path).String()
	} else if f.PlistProxy != "" {
		httpPlistLink := "http://" + r.Host + "/-/ipa/plist/" + path
		url, err := html.GenPlistLink(f.PlistProxy, args.DefaultPlistProxy, httpPlistLink)
		if err != nil {
			http.Error(w, err.Error(), 500)
			return
		}
		plistUrl = url
	} else {
		http.Error(w, "500: Server should be https:// or provide valid plistproxy", 500)
		return
	}

	w.Header().Set("Content-Type", "text/html")
	log.Println("PlistURL:", plistUrl)
	f.renderHTML(w, "assets/ipa-install.html", map[string]string{
		"Name":      filepath.Base(path),
		"PlistLink": plistUrl,
	})
}

func (f *FileServer) hUp(w http.ResponseWriter, r *http.Request) {
	// 设置响应头
	w.Header().Set("Content-Type", "text/plain")
	userpass := strings.SplitN(f.config.Auth.HTTP, ":", 2)
	var basicauth string
	if userpass != nil && len(userpass) == 2 {
		basicauth = fmt.Sprintf("%s:%s", userpass[0], userpass[1])
		basicauth = base64.StdEncoding.EncodeToString([]byte(basicauth))
		basicauth = fmt.Sprintf(" -H \\\"Authorization: Basic %s\\\"", basicauth)
	}
	// 编写要回复的数据
	responseText := assets.UploadShell
	responseText = strings.ReplaceAll(responseText, "127.0.0.1:8000", r.Host)
	responseText = strings.ReplaceAll(responseText, "-H", basicauth)
	fmt.Println(responseText)
	// 将数据写入响应
	_, err := w.Write([]byte(responseText))
	if err != nil {
		fmt.Println("无法写入响应:", err)
	}
}

func (f *FileServer) hPutMethod(w http.ResponseWriter, r *http.Request) {
	path := mux.Vars(r)["path"]
	realPath := f.getRealPath(r)
	putType := r.FormValue("putType")

	log.Println("PUT", path, realPath)
	switch putType {
	case "savefile":
		html.HFileSave(f.Root, f.Prefix, w, r)
	case "showdir":
		token := r.Header.Get("Token")
		userpass := strings.SplitN(f.config.Auth.HTTP, ":", 2)
		if len(userpass) == 2 {
			if !strings.EqualFold(userpass[1], token) {
				http.Error(w, "token error", http.StatusUnauthorized)
				return
			}
		}

		content, err := ioutil.ReadAll(r.Body)
		if err != nil {
			http.Error(w, err.Error(), http.StatusInternalServerError)
			return
		}
		realdir := string(content)
		ok := strings.EqualFold(realdir, "ok")
		if file2.IsDirOrFileExist(realdir) || ok {
			if ok {
				realdir = f.OldRoot
			}
			log.Println("chg dir", realdir)
			files, err1 := f.scanDir(realdir)
			if err1 == nil {
				if !strings.EqualFold(f.Root, f.OldRoot) {
					args.DeleteConfigFile(f.Root)
				}
				f.chgDirs[realdir] = realdir
				f.Root = realdir
				args.CopyExtFile(f.Root)
				w.Header().Set("Content-Type", "application/json")
				w.Write(files)
				return
			}
		} else {
			http.Error(w, realdir+" is not directory", http.StatusInternalServerError)
		}

		log.Println(putType, realdir)
	case "token":
		content, err := ioutil.ReadAll(r.Body)
		if err != nil {
			http.Error(w, err.Error(), http.StatusInternalServerError)
			return
		}
		token := string(content)
		log.Println("token", token)
		userpass := strings.SplitN(f.config.Auth.HTTP, ":", 2)
		if len(userpass) == 2 {
			if strings.EqualFold(userpass[1], token) {
				w.Write([]byte("Success"))
				return
			}
		}
		http.Error(w, "Chg forbidden", http.StatusUnauthorized)

	default:
		http.Error(w, "Chg forbidden", http.StatusForbidden)
	}
}

func (f *FileServer) hDelete(w http.ResponseWriter, req *http.Request) {
	path := mux.Vars(req)["path"]
	realPath := f.getRealPath(req)
	// path = filepath.Clean(path) // for safe reason, prevent path contain ..
	auth := f.readAccessConf(realPath)
	if !auth.CanDelete(req) {
		http.Error(w, "Delete forbidden", http.StatusForbidden)
		log.Printf("Can Not Delete by AccessConf realPath: %s ,path:%s\n", realPath, path)
		return
	}
	if !file2.IsDirOrFileExist(realPath) {
		http.Error(w, "Delete forbidden", http.StatusForbidden)
		log.Printf("Not Exist realPath: %s ,path:%s\n", realPath, path)
		return
	}
	// TODO: path safe check
	err := os.RemoveAll(realPath)
	if err != nil {
		pathErr, ok := err.(*os.PathError)
		if ok {
			log.Printf("delete Err realPath: %s ,path:%s,err:%v,pathErr:%v\n", realPath, path, err, pathErr)
			http.Error(w, pathErr.Op+" "+path+": "+pathErr.Err.Error(), 500)
		} else {
			log.Printf("delete Err realPath: %s ,path:%s,err:%v\n", realPath, path, err)
			http.Error(w, err.Error(), 500)
		}
		return
	}
	log.Printf("delete OK realPath: %s ,path:%s\n", realPath, path)
	w.Write([]byte("Success"))
}
func (f *FileServer) hUpload(w http.ResponseWriter, r *http.Request) {
	dirpath := f.getRealPath(r)
	// check auth
	auth := f.readAccessConf(dirpath)
	if !auth.CanUpload(r) {
		http.Error(w, "Upload forbidden", http.StatusForbidden)
		return
	}

	if _, err1 := os.Stat(dirpath); os.IsNotExist(err1) {
		if err2 := os.MkdirAll(dirpath, os.ModePerm); err2 != nil {
			log.Println("Create directory:", err2)
			http.Error(w, "Directory create "+err2.Error(), http.StatusInternalServerError)
			return
		}
	}

	//ParseMultipartForm将请求的主体作为multipart/form-data解析。请求的整个主体都会被解析，得到的文件记录最多 maxMemery字节保存在内存，其余部分保存在硬盘的temp文件里。如果必要，ParseMultipartForm会自行调用 ParseForm。重复调用本方法是无意义的
	//设置内存大小
	err := r.ParseMultipartForm(32 << 20)
	if err != nil {
		//http.Error(w, err.Error(), http.StatusInternalServerError)
		w.Header().Set("Content-Type", "application/json;charset=utf-8")
		json.NewEncoder(w).Encode(map[string]interface{}{
			"success":     true,
			"destination": dirpath,
		})
		return
	}

	m := r.MultipartForm
	fileHeaders := m.File["file"]
	if fileHeaders == nil || len(fileHeaders) == 0 {
		http.Error(w, "please insert file filed into MultipartForm", http.StatusForbidden)
		return
	}

	//if _, err1 := os.Stat(dirpath); os.IsNotExist(err1) {
	//	if err2 := os.MkdirAll(dirpath, os.ModePerm); err2 != nil {
	//		log.Println("Create directory:", err2)
	//		http.Error(w, "Directory create "+err2.Error(), http.StatusInternalServerError)
	//		return
	//	}
	//}
	defer func() {
		r.MultipartForm.RemoveAll() // Seen from go source code, req.MultipartForm not nil after call FormFile(..)
	}()
	filesurl := []string{}
	for _, header := range fileHeaders {
		fileName := header.Filename
		fileSize := header.Size
		file, err2 := header.Open()
		if err2 != nil || file == nil {
			http.Error(w, err2.Error(), http.StatusForbidden)
			return
		}
		defer func() {
			file.Close()
		}()
		log.Println(fileName, fileSize)
		if err1 := file2.CheckFilename(fileName); err1 != nil {
			http.Error(w, err1.Error(), http.StatusForbidden)
			return
		}

		path, err3 := html.GetFileNameFromMIMEHeader(header.Header)
		log.Println(path, err3)
		var tmpPath string
		if err3 == nil && path != "" {
			dir, name := filepath.Split(path)
			tmpPath = filepath.Join(dirpath, dir)
			if name != "" {
				fileName = path
			}
			if !file2.IsDirOrFileExist(tmpPath) {
				if err21 := os.MkdirAll(tmpPath, os.ModePerm); err21 != nil {
					log.Println("Create directory:", err21)
					continue
				}
			}
		}
		dstPath := filepath.Join(dirpath, fileName)
		//如果文件存在，则旧文件备份，以日期命名
		file2.BackupFile(dstPath)

		dst, err1 := os.Create(dstPath)
		if err1 != nil {
			log.Println("Create file:", err1)
			http.Error(w, "File create "+err1.Error(), http.StatusInternalServerError)
			return
		}

		buf := f.bufPool.Get().([]byte)
		defer f.bufPool.Put(buf)
		_, copyErr := io.CopyBuffer(dst, file, buf)
		dst.Close()
		if copyErr != nil {
			log.Println("Handle upload file:", err)
			http.Error(w, copyErr.Error(), http.StatusInternalServerError)
			return
		}
		//filepath := dstPath[len(f.Root):]
		filePath := dstPath
		if strings.HasPrefix(dstPath, f.Root) {
			filePath = dstPath[len(f.Root):]
		}
		fileurl := fmt.Sprintf("http://%s/%s", r.Host, filePath)
		filesurl = append(filesurl, fileurl)
	}

	log.Println(filesurl)
	w.Header().Set("Content-Type", "application/json;charset=utf-8")
	json.NewEncoder(w).Encode(map[string]interface{}{
		"success":     true,
		"destination": filesurl,
	})
}

func (f *FileServer) hUploadOrMkdir(w http.ResponseWriter, req *http.Request) {
	dirpath := f.getRealPath(req)

	// check auth
	auth := f.readAccessConf(dirpath)
	if !auth.CanUpload(req) {
		http.Error(w, "Upload forbidden", http.StatusForbidden)
		return
	}

	file, header, err := req.FormFile("file")

	if _, err1 := os.Stat(dirpath); os.IsNotExist(err1) {
		if err2 := os.MkdirAll(dirpath, os.ModePerm); err2 != nil {
			log.Println("Create directory:", err2)
			http.Error(w, "Directory create "+err2.Error(), http.StatusInternalServerError)
			return
		}
	}

	if file == nil { // only mkdir
		w.Header().Set("Content-Type", "application/json;charset=utf-8")
		json.NewEncoder(w).Encode(map[string]interface{}{
			"success":     true,
			"destination": dirpath,
		})
		return
	}

	if err != nil {
		log.Println("Parse form file:", err)
		http.Error(w, err.Error(), http.StatusInternalServerError)
		return
	}
	defer func() {
		file.Close()
		req.MultipartForm.RemoveAll() // Seen from go source code, req.MultipartForm not nil after call FormFile(..)
	}()

	filename := req.FormValue("filename")
	if filename == "" {
		filename = header.Filename
	}
	if err := file2.CheckFilename(filename); err != nil {
		http.Error(w, err.Error(), http.StatusForbidden)
		return
	}

	dstPath := filepath.Join(dirpath, filename)

	// Large file (>32MB) will store in tmp directory
	// The quickest operation is call os.Move instead of os.Copy
	// Note: it seems not working well, os.Rename might be failed

	//如果文件存在，则旧文件备份，以日期命名
	file2.BackupFile(dstPath)

	var copyErr error
	// if osFile, ok := file.(*os.File); ok && fileExists(osFile.Name()) {
	// 	tmpUploadPath := osFile.Name()
	// 	osFile.Close() // Windows can not rename opened file
	// 	log.Printf("Move %s -> %s", tmpUploadPath, dstPath)
	// 	copyErr = os.Rename(tmpUploadPath, dstPath)
	// } else {
	dst, err := os.Create(dstPath)
	if err != nil {
		log.Println("Create file:", err)
		http.Error(w, "File create "+err.Error(), http.StatusInternalServerError)
		return
	}

	// Note: very large size file might cause poor performance
	// _, copyErr = io.Copy(dst, file)
	buf := f.bufPool.Get().([]byte)
	defer f.bufPool.Put(buf)
	_, copyErr = io.CopyBuffer(dst, file, buf)
	dst.Close()
	// }
	if copyErr != nil {
		log.Println("Handle upload file:", err)
		http.Error(w, err.Error(), http.StatusInternalServerError)
		return
	}

	w.Header().Set("Content-Type", "application/json;charset=utf-8")

	if req.FormValue("unzip") == "true" {
		err = zip.UnzipFile(dstPath, dirpath)
		os.Remove(dstPath)
		message := "success"
		if err != nil {
			message = err.Error()
		}
		json.NewEncoder(w).Encode(map[string]interface{}{
			"success":     err == nil,
			"description": message,
		})
		return
	}

	json.NewEncoder(w).Encode(map[string]interface{}{
		"success":     true,
		"destination": dstPath,
	})
}

func (f *FileServer) hIndex(w http.ResponseWriter, r *http.Request) {
	path := mux.Vars(r)["path"]
	realPath := f.getRealPath(r)
	ext := r.FormValue("ext")

	if ext == "ext" {
		f.hRaw(w, r)
		return
	}
	if r.FormValue("json") == "true" {
		f.hJSONList(w, r)
		return
	}

	if r.FormValue("raw") == "content" {
		f.hRaw(w, r)
		return
	}

	if r.FormValue("op") == "info" {
		f.hInfo(w, r)
		return
	}

	if r.FormValue("op") == "archive" {
		f.hZip(w, r)
		return
	}

	if r.FormValue("unzip") == "true" {
		log.Println(realPath)
		dirpath, _ := filepath.Split(realPath)
		err := zip.UnzipFile(realPath, dirpath)
		log.Println(realPath, err)
		//os.Remove(dstPath)
		//message := "success"
		//if err != nil {
		//	message = err.Error()
		//}
		//json.NewEncoder(w).Encode(map[string]interface{}{
		//	"success":     err == nil,
		//	"description": message,
		//})
		return
	}

	log.Println("GET", path, realPath)
	if r.FormValue("raw") == "false" || file2.IsDir(realPath) {
		if r.Method == "HEAD" {
			return
		}
		args.CopyConfigFile(f.Root, r.Host)
		f.renderHTML(w, "assets/index.html", f)
	} else {
		if filepath.Base(path) == args.YAMLCONF {
			auth := f.readAccessConf(realPath)
			if !auth.IsDelete() {
				http.Error(w, "Security warning, not allowed to read", http.StatusForbidden)
				return
			}
		}
		if r.FormValue("download") == "true" {
			w.Header().Set("Content-Disposition", "attachment; filename="+strconv.Quote(filepath.Base(path)))
		}
		http.ServeFile(w, r, realPath)
	}
}
