package main

import (
	"sync"
	"time"
	"fmt"
)

func sleep() {
	time.Sleep(1 * time.Millisecond)
}

func reader(m *sync.RWMutex, id int, wg *sync.WaitGroup) {
	defer wg.Done()
	fmt.Println("reader", id, " is ready to read");
	sleep()
	m.RLock()
	fmt.Println("reading", id, " is reading");
	m.RUnlock()
}
func writer(m *sync.RWMutex, id int, wg *sync.WaitGroup) {
	defer wg.Done()
	fmt.Println("writer", id, " is ready to write");
	sleep()
	m.Lock()
	fmt.Println("writer", id, " is writing");
	m.Unlock()

}
func main() {
	t1 := time.Now()

	var m sync.RWMutex
	var wg sync.WaitGroup
	for i := 0; i < 1000; i++ {
		wg.Add(1)
		go reader(&m, i, &wg)
	}
	for i := 0; i < 500; i++ {
		wg.Add(1)
		go writer(&m, i, &wg)
	}

	wg.Wait()

	elapsed := time.Since(t1)
	fmt.Println("All threads elapsed: ", elapsed)

}
