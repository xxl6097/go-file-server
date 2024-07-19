package main

import (
	"bufio"
	"fmt"
	"log"
	"os"
	"path/filepath"
	"strings"
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

func ReadExt() []string {
	// 打开文件
	file, err := os.Open("./.ext")
	if err != nil {
		fmt.Println("Error opening file:", err)
		return nil
	}
	defer file.Close() // 确保文件在函数结束时关闭

	exts := make([]string, 0)
	// 创建一个bufio.Scanner来读取文件
	scanner := bufio.NewScanner(file)

	for scanner.Scan() {
		line := scanner.Text() // 获取当前行的内容
		// 按空格分割每行的内容
		elements := strings.Fields(line)
		// 打印每行的数组
		fmt.Println(line, elements)
		exts = append(exts, elements...)
	}

	if err := scanner.Err(); err != nil {
		fmt.Println("Error reading file:", err)
	}
	fmt.Println("===>", exts)
	return exts
}
