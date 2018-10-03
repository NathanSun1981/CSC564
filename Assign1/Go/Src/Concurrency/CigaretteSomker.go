package main

import (
	"fmt"
	"math/rand"
	"sync"
	"time"
)

const (
	paper = 0
	tobacco = 1
	match = 2
)

var smokeMap = []string{
	paper: "paper",
	tobacco: "tobacco",
	match: "match",
}

type Pusher struct {
	paper chan int
	tobacco chan int
	match chan int
}


func disposer(p *Pusher, smokers [3]chan int) {
	for {
		//time.Sleep(time.Millisecond * 500)
		i := rand.Intn(3)
		switch i {
		case paper:
			fmt.Printf("Pusher %d starts choosing tobacco and match\n", i)
			p.tobacco <- 1
			p.match <- 1
		case tobacco:
			fmt.Printf("Pusher %d starts choosing paper and match\n", i)
			p.paper <- 1
			p.match <- 1
		case match:
			fmt.Printf("Pusher %d starts choosing tobacco and paper\n", i)
			p.tobacco <- 1
			p.paper <- 1
		}
		for _, smoker := range smokers {
			smoker <- i
		}
		wg.Add(1)
		wg.Wait()
	}
}

func smoker(p *Pusher, smokes int, signal chan int) {
	var chosen = -1
	for {
		chosen = <-signal // blocks

		if smokes != chosen {
			continue
		}

		fmt.Printf("paper: %d tobacco: %d match: %d\n", len(p.paper), len(p.tobacco), len(p.match))
		select {
		case <-p.paper:
		case <-p.tobacco:
		case <-p.match:
		}
		fmt.Printf("paper: %d tobacco: %d match: %d\n", len(p.paper), len(p.tobacco), len(p.match))
		//time.Sleep(10 * time.Millisecond)
		select {
		case <-p.paper:
		case <-p.tobacco:
		case <-p.match:
		}
		fmt.Printf("paper: %d tobacco: %d match: %d\n", len(p.paper), len(p.tobacco), len(p.match))
		fmt.Printf("smokes a cigarette\n")
		time.Sleep(time.Millisecond * 10)
		wg.Done()
		//time.Sleep(time.Millisecond * 100)
	}
}
var wg *sync.WaitGroup

func main() {
	wg = new(sync.WaitGroup)
	pusher := new(Pusher)
	pusher.match = make(chan int, 1)
	pusher.paper = make(chan int, 1)
	pusher.tobacco = make(chan int, 1)
	var signals [3]chan int
	// three smokers
	for i := 0; i < 3; i++ {
		signal := make(chan int, 1)
		signals[i] = signal
		go smoker(pusher, i, signal)
	}

	disposer(pusher, signals)

}