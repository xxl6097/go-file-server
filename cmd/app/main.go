package main

import "github.com/xxl6097/go-server-file/internal"

//go:generate goversioninfo -icon=resource/icon.ico -manifest=resource/goversioninfo.exe.manifest
func main() {
	internal.Serve()
}
