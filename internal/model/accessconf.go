package model

type AccessConf struct {
	Upload       bool          `yaml:"upload" json:"upload"`
	Delete       bool          `yaml:"delete" json:"delete"`
	Users        []UserControl `yaml:"users" json:"users"`
	AccessTables []AccessTable `yaml:"accessTables"`
}
