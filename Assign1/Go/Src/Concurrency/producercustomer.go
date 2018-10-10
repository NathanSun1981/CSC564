package main

import (
	"fmt"
	"log"
	"os"
	"runtime/pprof"
	"sync"
	"time"
)

func consumer(cname string, id int, ch chan int, wg *sync.WaitGroup) {
	defer wg.Done()
	for i:=0; i< 100; i++{
		i := <-ch;
		fmt.Println(cname, id, " successfully consume one product ", i);
	}
}

func producer(pname string, id int, ch chan int, wg *sync.WaitGroup) {
	defer wg.Done()
	for i:=0; i< 100; i++{
		ch <- i
		fmt.Println(pname, id, "successfully produce one product ", i);
	}
}

func main() {

	f, err := os.Create("cpu-profile.prof")
	if err != nil {
		log.Fatal(err)
	}
	pprof.StartCPUProfile(f)

	fm, err := os.Create("mem-profile.prof")
	if err != nil {
		log.Fatal(err)
	}
	pprof.WriteHeapProfile(fm)



	t1 := time.Now()
	var wg sync.WaitGroup
	data := make(chan int, 4)
	for i:=0; i< 100; i++ {
		wg.Add(1)
		go producer("Producer", i, data, &wg)
		wg.Add(1)
		go consumer("customer", i, data, &wg)
	}

	wg.Wait()

	elapsed := time.Since(t1)
	fmt.Println("All threads elapsed: ", elapsed)

	pprof.StopCPUProfile()
	fm.Close()
	close(data);
}
