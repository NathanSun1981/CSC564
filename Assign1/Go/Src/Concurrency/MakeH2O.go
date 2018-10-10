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

var H2Onum int
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

		if H2Onum == 100{
			fmt.Printf("smokernum arrive 100")
			break
		}

		<- maker
		fmt.Printf("Start to make water \n")
		<- g.Hydrogen
		<- g.Hydrogen
		<- g.Oxygen
		H2Onum++
		//time.Sleep(time.Microsecond * 1)
		waitgroup.Done()
	}

}

func main() {
	t1 := time.Now()
	waitgroup = new(sync.WaitGroup)
	H2Onum = 0
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

	elapsed := time.Since(t1)

	fmt.Println("All threads elapsed: ", elapsed)
}