package main

import "github.com/xxl6097/go-file-server/internal"

//go:generate goversioninfo -icon=resource/icon.ico -manifest=resource/goversioninfo.exe.manifest
func main() {
	internal.Serve()
}

// 跟进极空间目录
//1. 界面上点击 Show Dir按钮；
//2. 输入 /；
//3. /data下就是你想要的
