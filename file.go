package main

import (
	"log"
	"os"
	"path/filepath"
)

// IsNotExist 判断文件不存在，返回true
func IsNotExist(path string) bool {
	if _, err := os.Stat(path); os.IsNotExist(err) {
		return true
	}
	return false
}

func BackupFile(path string) {
	if IsNotExist(path) {
		return
	}
	dir, _ := filepath.Split(path)
	fileName := filepath.Base(path)
	ext := filepath.Ext(fileName)
	fileName = fileName[:len(fileName)-len(filepath.Ext(fileName))]
	// 重命名文件
	newFile := dir + fileName + "_" + GetFileNameWithTime() + ext
	err := os.Rename(path, newFile)
	if err != nil {
		log.Println("重命名文件时发生错误:", err)
		//文件存在，删除文件
		err := os.Remove(path)
		if err != nil {
			log.Println("删除文件时发生错误:", err)
		} else {
			log.Println("文件存在，已删除:", path)
		}
	}
}
