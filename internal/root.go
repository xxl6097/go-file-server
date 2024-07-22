package internal

import (
	"github.com/xxl6097/go-server-file/internal/args"
	"github.com/xxl6097/go-server-file/internal/server"
	"log"
)

func Serve() {
	if err := args.ParseFlags(); err != nil {
		log.Fatal(err)
	}
	cfg := args.LoadAgrs()

	svr := server.NewFileServer(cfg)
	svr.Serve()
}
