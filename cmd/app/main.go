package main

import (
	"github.com/xxl6097/go-file-server/internal"
)

//go:generate goversioninfo -icon=resource/icon.ico -manifest=resource/goversioninfo.exe.manifest
func main() {
	test()
	internal.Serve()
}

func test() {
	// 打开 .tar.gz 文件
}
