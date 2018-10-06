package main
import (
	"fmt"
	"sync"
)

var input string
func userReadeThread(m *sync.RWMutex, id int) {
	for {
		m.RLock()
		fmt.Println("User", id, " is reading");
		m.RUnlock()
	}

}

func userWriteThread(m *sync.RWMutex, id int) {

	for{
		m.Lock()
		fmt.Println("User", id, " starts to write");
		fmt.Printf("Please input your information: ")
		fmt.Scanln(&input)
		fmt.Println("User", id, " starts to write to database")
		//writerarrive = false;
		m.Unlock()
	}

}

func main() {
	var m sync.RWMutex
	waiter := make(chan int)

	for i := 0; i < 10; i++ {
		go userReadeThread(&m, i)
	}

	for i := 0; i < 3; i++{
		go userWriteThread(&m, i)
	}

	<- waiter

}
