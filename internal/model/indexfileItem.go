package model

import "os"

type IndexFileItem struct {
	Path string
	Info os.FileInfo
}
