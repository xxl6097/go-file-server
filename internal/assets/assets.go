package assets

import (
	"embed"
	"io/ioutil"
	"net/http"
)

//go:embed assets
var AssetsFS embed.FS

// Assets contains project assets.
var Assets = http.FS(AssetsFS)

//go:embed assets/.ext
var ExtArrayContent string

//go:embed assets/.ghs.yml
var GhsContent string

func AssetsContent(name string) string {
	fd, err := Assets.Open(name)
	if err != nil {
		panic(err)
	}
	data, err := ioutil.ReadAll(fd)
	if err != nil {
		panic(err)
	}
	return string(data)
}
