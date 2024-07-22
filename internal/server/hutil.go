package server

import (
	"github.com/xxl6097/go-server-file/internal/assets"
	"github.com/xxl6097/go-server-file/pkg/html"
	"html/template"
	"net/http"
	"path/filepath"
)

func (f *FileServer) historyDirSize(dir string) int64 {
	f.dirInfoSize.Mutex.RLock()
	size, ok := f.dirInfoSize.Size[dir]
	f.dirInfoSize.Mutex.RUnlock()

	if ok {
		return size
	}

	for _, fitem := range f.indexes {
		if filepath.HasPrefix(fitem.Path, dir) {
			size += fitem.Info.Size()
		}
	}

	f.dirInfoSize.Mutex.Lock()
	f.dirInfoSize.Size[dir] = size
	f.dirInfoSize.Mutex.Unlock()

	return size
}

func (f *FileServer) getRealPath(r *http.Request) string {
	return html.GetRealPath(f.Root, f.Prefix, r)
}

func (f *FileServer) renderHTML(w http.ResponseWriter, name string, v interface{}) {
	if t, ok := f._tmpls[name]; ok {
		t.Execute(w, v)
		return
	}
	t := template.Must(template.New(name).Funcs(f.funcMap).Delims("[[", "]]").Parse(assets.AssetsContent(name)))
	f._tmpls[name] = t
	t.Execute(w, v)
}
