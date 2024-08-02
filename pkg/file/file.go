package file

import (
	"bufio"
	"bytes"
	"embed"
	"errors"
	"fmt"
	"io/ioutil"
	"log"
	"os"
	"path/filepath"
	"strconv"
	"strings"
	"time"
)

func DeepPath(basedir, name string, maxDepth int) string {
	// loop max 5, incase of for loop not finished
	for depth := 0; depth <= maxDepth; depth += 1 {
		finfos, err := ioutil.ReadDir(filepath.Join(basedir, name))
		if err != nil || len(finfos) != 1 {
			break
		}
		if finfos[0].IsDir() {
			name = filepath.ToSlash(filepath.Join(name, finfos[0].Name()))
		} else {
			break
		}
	}
	return name
}

func formatSize(file os.FileInfo) string {
	if file.IsDir() {
		return "-"
	}
	size := file.Size()
	switch {
	case size > 1024*1024:
		return fmt.Sprintf("%.1f MB", float64(size)/1024/1024)
	case size > 1024:
		return fmt.Sprintf("%.1f KB", float64(size)/1024)
	default:
		return strconv.Itoa(int(size)) + " B"
	}
	return ""
}

func FileExists(path string) bool {
	info, err := os.Stat(path)
	if err != nil {
		return false
	}
	return !info.IsDir()
}

// Convert path to normal paths
func CleanPath(path string) string {
	return filepath.ToSlash(filepath.Clean(path))
}

func IsFile(path string) bool {
	info, err := os.Stat(path)
	return err == nil && info.Mode().IsRegular()
}

func IsDir(path string) bool {
	info, err := os.Stat(path)
	return err == nil && info.Mode().IsDir()
}

func IsDirOrFileExist(path string) bool {
	if _, err := os.Stat(path); !os.IsNotExist(err) {
		// 文件夹/文件存在
		return true
	}
	return false
}

func CopyFile(source *bytes.Reader, destPath string) error {
	srcBytes, err := ioutil.ReadAll(source)
	if err != nil {
		return err
	}

	destFile, err := os.Create(destPath)
	if err != nil {
		return err
	}
	defer destFile.Close()

	_, err = destFile.Write(srcBytes)
	return err
}

func ReadAssetsFile(filePath string, embeddedFiles embed.FS) ([]byte, error) {
	f, err := embeddedFiles.Open(filePath)
	if err != nil {
		return nil, err
	}
	defer f.Close()

	return ioutil.ReadAll(f)
}

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

func GetFileNameWithTime() string {
	return GetTimeFormat("20060102150405")
}

func GetTimeFormat(format string) string {
	const timezone = "Asia/Shanghai"
	location, err := time.LoadLocation(timezone)
	if err != nil {
		location = time.FixedZone("CST", 8*3600) //替换上海时区方式
	}
	date := time.Now()
	date.In(location)
	return date.Format(format)
}

func ReadExt() []string {
	filepath := "./.ext"
	if _, err := os.Stat(filepath); os.IsNotExist(err) {
		return nil
	}
	// 打开文件
	file, err := os.Open(filepath)
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

func CheckFilename(name string) error {
	if strings.ContainsAny(name, "\\/:*<>|") {
		return errors.New("Name should not contains \\/:*<>|")
	}
	return nil
}

// GetRootDirectory returns the root directory of the given path
func GetRootDirectory(path string) string {
	absPath, err := filepath.Abs(path)
	if err != nil {
		fmt.Println("Error getting absolute path:", err)
		return ""
	}

	root := filepath.VolumeName(absPath)
	if root == "" {
		// For UNIX-like systems, return "/"
		root = "/"
	}
	return root
}

// SplitPath splits the given path into its components
func SplitPath(path string) (string, string, []string) {
	// Get the directory and base file name
	dir := filepath.Dir(path)
	base := filepath.Base(path)

	// Split the path into its components
	parts := strings.Split(dir, string(filepath.Separator))

	return dir, base, parts
}

func GetPathFirst(path string) string {
	_, _, parts := SplitPath(path)
	if parts != nil && len(parts) > 0 {
		part := parts[0]
		if part == "." {
			return ""
		}
		return part
	}
	return ""
}

func GetFileName(path string) string {
	filenameWithExt := filepath.Base(path)
	filename := strings.TrimSuffix(filenameWithExt, filepath.Ext(filenameWithExt))
	return filename
}
