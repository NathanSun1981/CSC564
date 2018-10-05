package main

import (
	"fmt"
	"sync"
	"time"
)

const (
	Hydrogen = 0
	Oxygen = 1
)

var GasMap = []string{
	Hydrogen: "Hydrogen",
	Oxygen: "Oxygen",
}

type Gas struct {
	Hydrogen chan int
	Oxygen chan int
	moreOx chan int
	moreHy chan int
}

var waitgroup *sync.WaitGroup

func ProvideOxygen(g *Gas,) {
	for {
		g.Oxygen <- 1
		fmt.Printf("provide Oxygen\n")
		<- g.moreOx

	}
}
func ProvideHydrogen(g *Gas) {
	for {
		g.Hydrogen <- 1
		fmt.Printf("provide Hydrogen\n")
		<- g.moreHy
	}
}

func Monitor(g *Gas, maker chan int) {

	for {
		if len(g.Hydrogen) < 2 {
			g.moreHy <- 1
		}
		if len(g.Oxygen) < 1 {
			g.moreOx <- 1
		}
		if (len(g.Hydrogen) == 2  && len(g.Oxygen) == 1) {
			fmt.Printf("Hydrogen: %d Oxygen: %d \n", len(g.Hydrogen), len(g.Oxygen))
			fmt.Printf("Notify to make water \n")
			maker <- 1
			waitgroup.Add(1)
			waitgroup.Wait()
			g.moreOx <- 1
			g.moreHy <- 1
		}
	}
}

func MakeH2O(g *Gas, maker chan int) {

	for {
		<- maker
		fmt.Printf("Start to make water \n")
		<- g.Hydrogen
		<- g.Hydrogen
		<- g.Oxygen

		time.Sleep(time.Millisecond * 10)
		waitgroup.Done()
	}

}

func main() {
	waitgroup = new(sync.WaitGroup)

	Gas := new(Gas)
	Gas.Hydrogen = make(chan int, 2)
	Gas.Oxygen = make(chan int, 1)
	Gas.moreHy = make(chan int, 1)
	Gas.moreOx = make(chan int, 1)

	maker := make(chan int, 1)

	go ProvideHydrogen (Gas)
	go ProvideOxygen (Gas)
	go Monitor (Gas , maker)

	MakeH2O(Gas, maker)


}