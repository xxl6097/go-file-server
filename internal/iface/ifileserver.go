package iface

import "github.com/xxl6097/go-server-file/internal/model"

type IFileServer interface {
	LoadConfig(*model.Configure)
}
