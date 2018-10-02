package main

import (
	"fmt"
	"time"
)

func consumer(cname string, id int, ch chan int) {
	for{
		i := <-ch;
		fmt.Println(cname, id, " successfully consume one product ", i);
	}
}

func producer(pname string, id int, ch chan int) {
	for i:=0; i< 10; i++{
		ch <- i
		fmt.Println(pname, id, "successfully produce one product ", i);
	}
}

func main() {
	data := make(chan int, 4)
	for i:=0; i< 4; i++ {
		go producer("Producer", i, data)
		go consumer("customer1", i, data)
	}
	time.Sleep(1 * time.Second)
	close(data);
}
