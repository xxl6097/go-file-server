package main

import (
	"archive/zip"
	"bytes"
	"embed"
	"golang.org/x/text/encoding/simplifiedchinese"
	"io"
	"io/ioutil"
	"os"
	"path/filepath"
	"time"
)

const timezone = "Asia/Shanghai"

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

func UnzipFile(filename, dest string) error {
	zr, err := zip.OpenReader(filename)
	if err != nil {
		return err
	}
	defer zr.Close()

	if dest == "" {
		dest = filepath.Dir(filename)
	}

	for _, f := range zr.File {
		rc, err := f.Open()
		if err != nil {
			return err
		}
		defer rc.Close()

		// ignore .ghs.yml
		filename := sanitizedName(f.Name)
		if filepath.Base(filename) == ".ghs.yml" {
			continue
		}
		fpath := filepath.Join(dest, filename)

		// filename maybe GBK or UTF-8
		// Ref: https://studygolang.com/articles/3114
		if f.Flags&(1<<11) == 0 { // GBK
			tr := simplifiedchinese.GB18030.NewDecoder()
			fpathUtf8, err := tr.String(fpath)
			if err == nil {
				fpath = fpathUtf8
			}
		}

		if f.FileInfo().IsDir() {
			os.MkdirAll(fpath, os.ModePerm)
			continue
		}

		os.MkdirAll(filepath.Dir(fpath), os.ModePerm)
		outFile, err := os.OpenFile(fpath, os.O_WRONLY|os.O_CREATE|os.O_TRUNC, f.Mode())
		if err != nil {
			return err
		}
		_, err = io.Copy(outFile, rc)
		outFile.Close()

		if err != nil {
			return err
		}
	}
	return nil
}

func GetFileNameWithTime() string {
	//loc, _ := time.LoadLocation("Asia/Shanghai")
	//return time.Now().In(loc).Format("20060102150405")
	return GetTimeFormat("20060102150405")
}

func GetTimeFormat(format string) string {
	location, err := time.LoadLocation(timezone)
	if err != nil {
		location = time.FixedZone("CST", 8*3600) //替换上海时区方式
	}
	date := time.Now()
	date.In(location)
	return date.Format(format)

}
