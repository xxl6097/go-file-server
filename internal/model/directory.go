package model

import "sync"

type Directory struct {
	Size  map[string]int64
	Mutex *sync.RWMutex
}
