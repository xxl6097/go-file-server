package model

import "os"

type Auth struct {
	Type   string `yaml:"type"` // openid|http|github
	OpenID string `yaml:"openid"`
	HTTP   string `yaml:"http"`
	ID     string `yaml:"id"`     // for oauth2
	Secret string `yaml:"secret"` // for oauth2
}

type Configure struct {
	Conf             *os.File `yaml:"-"`
	Addr             string   `yaml:"addr"`
	Keyword          string   `yaml:"keyword"`
	Port             int      `yaml:"port"`
	Root             string   `yaml:"root"`
	Prefix           string   `yaml:"prefix"`
	Showdir          bool     `yaml:"showdir"`
	HTTPAuth         string   `yaml:"httpauth"`
	Cert             string   `yaml:"cert"`
	Key              string   `yaml:"key"`
	Theme            string   `yaml:"theme"`
	XHeaders         bool     `yaml:"xheaders"`
	Upload           bool     `yaml:"upload"`
	Delete           bool     `yaml:"delete"`
	NoLogin          bool     `yaml:"nologin"`
	PlistProxy       string   `yaml:"plistproxy"`
	Title            string   `yaml:"title"`
	Debug            bool     `yaml:"debug"`
	GoogleTrackerID  string   `yaml:"google-tracker-id"`
	Auth             Auth     `yaml:"auth"`
	DeepPathMaxDepth int      `yaml:"deep-path-max-depth"`
	NoIndex          bool     `yaml:"no-index"`
}
