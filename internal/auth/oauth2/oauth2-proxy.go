package oauth2

import (
	"encoding/json"
	"github.com/xxl6097/go-file-server/internal/model"
	"net/http"
	"net/url"
)

func HandleOauth2() {
	http.HandleFunc("/-/user", func(w http.ResponseWriter, r *http.Request) {
		fullNameMap, _ := url.ParseQuery(r.Header.Get("X-Auth-Request-Fullname"))
		var fullName string
		for k := range fullNameMap {
			fullName = k
			break
		}
		user := &model.UserInfo{
			Email:    r.Header.Get("X-Auth-Request-Email"),
			Name:     fullName,
			NickName: r.Header.Get("X-Auth-Request-User"),
		}

		w.Header().Set("Content-Type", "application/json; charset=utf-8")
		data, _ := json.Marshal(user)
		w.Write(data)
	})
}
