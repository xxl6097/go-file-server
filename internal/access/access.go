package access

import (
	"github.com/xxl6097/go-serverfile/internal/auth/openid"
	"github.com/xxl6097/go-serverfile/internal/model"
	"net/http"
	"regexp"
)

var reCache = make(map[string]*regexp.Regexp)

type Access struct {
	Upload       bool                `yaml:"upload" json:"upload"`
	Delete       bool                `yaml:"delete" json:"delete"`
	Users        []model.UserControl `yaml:"users" json:"users"`
	AccessTables []model.AccessTable `yaml:"accessTables"`
}

func (c *Access) GetAccess() *Access {
	return c
}

func (c *Access) IsDelete() bool {
	return c.Delete
}

func (c *Access) CanAccess(fileName string) bool {
	for _, table := range c.AccessTables {
		pattern, ok := reCache[table.Regex]
		if !ok {
			pattern, _ = regexp.Compile(table.Regex)
			reCache[table.Regex] = pattern
		}
		// skip wrong format regex
		if pattern == nil {
			continue
		}
		if pattern.MatchString(fileName) {
			return table.Allow
		}
	}
	return true
}

func (c *Access) CanDelete(r *http.Request) bool {
	session, err := openid.Store.Get(r, openid.DefaultSessionName)
	if err != nil {
		return c.Delete
	}
	val := session.Values["user"]
	if val == nil {
		return c.Delete
	}
	userInfo := val.(*model.UserInfo)
	for _, rule := range c.Users {
		if rule.Email == userInfo.Email {
			return rule.Delete
		}
	}
	return c.Delete
}

func (c *Access) CanUploadByToken(token string) bool {
	for _, rule := range c.Users {
		if rule.Token == token {
			return rule.Upload
		}
	}
	return c.Upload
}

func (c *Access) CanUpload(r *http.Request) bool {
	token := r.FormValue("token")
	if token != "" {
		return c.CanUploadByToken(token)
	}
	session, err := openid.Store.Get(r, openid.DefaultSessionName)
	if err != nil {
		return c.Upload
	}
	val := session.Values["user"]
	if val == nil {
		return c.Upload
	}
	userInfo := val.(*model.UserInfo)

	for _, rule := range c.Users {
		if rule.Email == userInfo.Email {
			return rule.Upload
		}
	}
	return c.Upload
}
