package iface

import (
	"github.com/xxl6097/go-server-file/internal/access"
	"net/http"
)

type IAccess interface {
	CanAccess(fileName string) bool
	CanDelete(r *http.Request) bool
	CanUploadByToken(token string) bool
	CanUpload(r *http.Request) bool
	IsDelete() bool
	GetAccess() *access.Access
}
