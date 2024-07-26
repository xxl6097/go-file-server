package main

import (
	"fmt"
	"path/filepath"
	"strings"
)

//go:generate goversioninfo -icon=resource/icon.ico -manifest=resource/goversioninfo.exe.manifest
func main() {
	test()
	//internal.Serve()
}

// getRootAndFirstLevelDir returns the root directory and the first-level directory of the given path
func getRootAndFirstLevelDir(path string) (string, string) {
	absPath, err := filepath.Abs(path)
	if err != nil {
		fmt.Println("Error getting absolute path:", err)
		return "", ""
	}

	// Split the path into the root and the rest of the path
	root := filepath.VolumeName(absPath)
	if root == "" {
		// For UNIX-like systems, root is "/"
		root = "/"
	}

	// Remove the root from the absolute path to get the rest of the path
	restPath := strings.TrimPrefix(absPath, root)
	if strings.HasPrefix(restPath, string(filepath.Separator)) {
		restPath = strings.TrimPrefix(restPath, string(filepath.Separator))
	}

	// Get the first-level directory
	firstLevelDir := ""
	if restPath != "" {
		firstLevelDir = strings.Split(restPath, string(filepath.Separator))[0]
	}

	return root, firstLevelDir
}
func test() {
	fmt.Println(string(filepath.Separator))
}
