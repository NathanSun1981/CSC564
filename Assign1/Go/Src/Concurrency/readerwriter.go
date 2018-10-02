package main

import (
	"sync"
	"time"
	"fmt"
)

func sleep() {
	time.Sleep(1 * time.Millisecond)
}

func reader(m *sync.RWMutex, id int) {
	fmt.Println("reader", id, " is ready to read");
	sleep()
	m.RLock()
	fmt.Println("reading", id, " is reading");
	m.RUnlock()
}
func writer(m *sync.RWMutex, id int) {
	fmt.Println("writer", id, " is ready to write");
	sleep()
	m.Lock()
	fmt.Println("writer", id, " is writing");
	m.Unlock()

}
func main() {
	var m sync.RWMutex
	for i := 0; i < 10; i++ {
		go reader(&m, i)
	}
	for i := 0; i < 3; i++ {
		go writer(&m, i)
	}

	time.Sleep(1 * time.Second)
}
