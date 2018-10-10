package main
import (
	"fmt"
	"sync"
	"time"
)

var input string
func userReadeThread(m *sync.RWMutex, id int) {
	for {
		m.RLock()
		fmt.Println("User", id, " is reading");
		m.RUnlock()
	}

}

func userWriteThread(m *sync.RWMutex, id int, wg *sync.WaitGroup, times *int) {

	for{
		m.Lock()
		fmt.Println("User", id, " starts to write");
		fmt.Printf("Please input your information: ")
		fmt.Scanln(&input)
		fmt.Println("User", id, " starts to write to database")
		*times++;
		if *times == 10{
			wg.Done()
		}
		m.Unlock()
	}

}

func main() {
	t1 := time.Now()
	var wg sync.WaitGroup
	var m sync.RWMutex
	times := 0

	wg.Add(1)

	for i := 0; i < 10; i++ {
		go userReadeThread(&m, i)
	}

	for i := 0; i < 3; i++{
		go userWriteThread(&m, i, &wg, &times)
	}

	wg.Wait()

	elapsed := time.Since(t1)
	fmt.Println("All threads elapsed: ", elapsed)

}
