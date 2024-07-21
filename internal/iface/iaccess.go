package iface

import "net/http"

type IAccess interface {
	CanAccess(fileName string) bool
	CanDelete(r *http.Request) bool
	CanUploadByToken(token string) bool
	CanUpload(r *http.Request) bool
	IsDelete() bool
}
