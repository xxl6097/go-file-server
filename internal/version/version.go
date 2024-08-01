package version

import (
	"fmt"
	"time"
)

var (
	AppName      string // 应用名称
	AppVersion   string // 应用版本
	BuildVersion string // 编译版本
	BuildTime    string // 编译时间
	GitRevision  string // Git版本
	GitBranch    string // Git分支
	GoVersion    string // Golang信息
)

// Version 版本信息
func Version() {
	if AppName != "" {
		fmt.Printf("App Name:\t%s\n", AppName)
		fmt.Printf("App Version:\t%s\n", AppVersion)
		fmt.Printf("Build version:\t%s\n", BuildVersion)
		fmt.Printf("Build time:\t%s\n", BuildTime)
		fmt.Printf("Git revision:\t%s\n", GitRevision)
		fmt.Printf("Git branch:\t%s\n", GitBranch)
		fmt.Printf("Golang Version: %s\n", GoVersion)
	} else {
		AppName = "go-file-server"
		AppVersion = "v0.0.0"
		BuildVersion = "v0.0.0"
		BuildTime = time.Now().Format("2006-01-02 15:04:05")
	}
}

func ToVersion() map[string]interface{} {
	return map[string]interface{}{
		"AppName":      AppName,
		"AppVersion":   AppVersion,
		"BuildVersion": BuildVersion,
		"BuildTime":    BuildTime,
		"GitRevision":  GitRevision,
		"GitBranch":    GitBranch,
		"GoVersion":    GoVersion,
	}
}
