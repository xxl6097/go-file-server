package logger

import (
	"github.com/codeskyblue/go-accesslog"
	"log"
)

type HttpLogger struct{}

func (l HttpLogger) Log(record accesslog.LogRecord) {
	log.Printf("%s - %s %d %s", record.Ip, record.Method, record.Status, record.Uri)
}
