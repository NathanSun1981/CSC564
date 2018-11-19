package main

import (
	"fmt"
	"math/rand"
	"sync"
	"time"
)

var mutex sync.Mutex
var commandchannel chan *string
var vectorGeneralmap map[int][N]*General

const (
	N  = 7
	M  = 2
	CO = 2
)

type General struct {
	Node    int
	Command string
}

func generateRandomNumber(start int, end int, count int) []int {
	if end < start || (end-start) < count {
		return nil
	}

	nums := make([]int, 0)

	r := rand.New(rand.NewSource(time.Now().UnixNano()))
	for len(nums) < count {
		num := r.Intn((end - start)) + start

		exist := false
		for _, v := range nums {
			if v == num {
				exist = true
				break
			}
		}

		if !exist {
			nums = append(nums, num)
		}
	}

	return nums
}

func isBetraiter(num int, array []int) bool {
	for _, v := range array {
		if v == num {
			return true
		}
	}
	return false
}

func generals(general *General, signal chan *General, send chan *int) {
	var vector [N]*General
	var lastcommand *General

	for {
		select {
		case message := <-signal:
			fmt.Printf("G%d receive G%d command  %s\n", general.Node, message.Node, message.Command)
			vector[message.Node] = message
			if message.Node == 0 {
				lastcommand = message
			}
			//fmt.Printf("G%d set the last received command %s\n", general.Node, lastcommand.Command)
			//for i := 0; i < N; i++ {
				//fmt.Println(vector[i])
			//}
			vectorGeneralmap[general.Node] = vector
		case <-send:
			//fmt.Printf("G%d get the last received command %s\n", general.Node, lastcommand.Command)
			commandchannel <- &lastcommand.Command
		default:
		}
	}

}

func generateCommand(Node int, betraiterIndex []int, commands []string, command string) *General {

	g := new(General)

	if Node == 0 {
		//first command
		g.Node = 0
		if isBetraiter(Node, betraiterIndex) {
			g.Command = commands[generateRandomNumber(0, CO, 1)[0]]
		} else {
			g.Command = commands[0]
		}
		//fmt.Printf("command %d = %s\n", Node, g.Command)
		return g

	} else {
		g.Node = Node
		if isBetraiter(Node, betraiterIndex) {
			/*
			 if command == commands[0]{
			 	g.Command = commands[1]
			 }else
			 {
			 	g.Command = commands[0]
			 }
			*/
			g.Command = commands[generateRandomNumber(0, len(commands), 1)[0]]
			fmt.Printf("G%d is betraiter, so the command is random from command list = %s\n", Node, g.Command)
		} else {
			//return general.ReceivedCommand
			g.Command = command
		}
		return g
	}

}

func isAgree(array [N]string) bool {

	for i := 0; i < N; i++ {
		if (i > i && array[i] != array[i-1]) || array[i] == "X" {
			return false
		}
	}
	return true
}

func elect(arrayTemp [N]string, commands []string) string {
	var maxindex int
	var index [CO]int
	for i := 0; i < N; i++ {
		fmt.Println(arrayTemp[i])
		for j := 0; j < CO; j++ {
			if arrayTemp[i] == commands[j] {
				index[j]++
			}
		}
	}
	for i := 0; i < CO; i++ {
		maxindex = 0
		if i > 0 {
			for j := 0; j < i; j++ {
				if index[i] > index[j] {
					maxindex = i
				} else {
					maxindex = j
				}
			}
		}
	}
	return commands[maxindex]
}

func finalDecisonVector(vectorGeneralmap map[int] [N]*General, commands []string, step int) [N]string{

	var finalDecison [N]string
	var startindex int

	if step == 0{
		startindex =0
	}else {
		startindex =1
	}


	for key, value := range vectorGeneralmap {
		fmt.Println("Key:", key)
		var numCommands [CO]int

		for i := startindex; i < N; i++ {
			fmt.Println(value[i])
			for j := 0; j < CO; j++ {
				if (value[i] != nil) && (value[i].Command == commands[j]) {
					numCommands[j] += 1
				}
			}
		}
		for j := 0; j < CO; j++ {
			fmt.Printf("For General %d, The number of command %s is %d\n", key, commands[j], numCommands[j])
			finalDecison[key] = commands[0]
			if j > 0 {
				if numCommands[j] > numCommands[j-1] {
					finalDecison[key] = commands[j]
				} else if numCommands[j] == numCommands[j-1] {
					finalDecison[key] = "X"
				}

			}
		}
		fmt.Printf("For General %d, the final decision is %s\n", key, finalDecison[key])
	}
	return finalDecison
}

func main() {

	var commands = []string{"ATTACK", "RETREAT"}
	commandchannel = make(chan *string)
	vectorGeneralmap = make(map[int][N]*General)
	var signals [N]chan *General
	var sends [N]chan *int
	if (N - 1) < 3*M {
		fmt.Printf("Too many betraiters, can not reach consistency\n")
		return
	}
	// N nodes
	for i := 0; i < N; i++ {
		general := new(General)
		general.Node = i
		signal := make(chan *General)
		signals[i] = signal
		send := make(chan *int)
		sends[i] = send
		go generals(general, signal, send)
	}

	//randemly generate the betraiter's index
	//betraiterIndex := generateRandomNumber(0, N-1, M)
	betraiterIndex := []int{0, 6}
	fmt.Println(betraiterIndex)

	for m := 0; m < M; m++ {
		if m == 0 {
			fmt.Println("---------the first level M = 0 ----------")
			//at the beginning, G0 send command to all L(i - (N-1))
			for j := 1; j < N; j++ {
				command := ""
				signals[j] <- generateCommand(0, betraiterIndex, commands, command)
			}
		} else {
			fmt.Printf("----------the %d level M = %d ---------\n", m, m)
			for k := 1; k < N; k++ {
				//each general send their own message to the others
				fmt.Printf("** the %d General ask for the other's reveived command\n", k)
				for p := 1; p < N; p++ {
					if p != k {
						sends[p] <- &p
						command := <-commandchannel
						fmt.Printf("** the %d General answer %s\n", p, *command)
						signals[k] <- generateCommand(p, betraiterIndex, commands, *command)

					}
				}

			}
			//election，if all agree，the consistency arrived


			/*
			var finalDecison [N]string

			for key, value := range vectorGeneralmap {
				fmt.Println("Key:", key)
				var numCommands [CO]int

				for i := 0; i < N; i++ {
					fmt.Println(value[i])
					for j := 0; j < CO; j++ {
						if (value[i] != nil) && (value[i].Command == commands[j]) {
							numCommands[j] += 1
						}
					}
				}
				for j := 0; j < CO; j++ {
					fmt.Printf("For General %d, The number of command %s is %d\n", key, commands[j], numCommands[j])
					finalDecison[key] = commands[0]
					if j > 0 {
						if numCommands[j] > numCommands[j-1] {
							finalDecison[key] = commands[j]
						} else if numCommands[j] == numCommands[j-1] {
							finalDecison[key] = "X"
						}

					}
				}
				fmt.Printf("For General %d, the final decision is %s\n", key, finalDecison[key])
			}
			*/

			if isAgree(finalDecisonVector(vectorGeneralmap, commands, 0)) {
				fmt.Println("make agreement!")
				return
			} else {
				fmt.Println("disagree in step2!")
				//can not agree
				for i := 1; i < N; i++ {
					var arrayTemp [N]string
					var commandTemp string
					for j := 1; j < N; j++ {
						if i != j {
							fmt.Printf("G%d check the G%d send command %s\n ", i, j, vectorGeneralmap[j][i].Command)
							//fmt.Println("ele,ct result", elect(vectorGeneralmap[j]))
							arrayTemp[j] = vectorGeneralmap[j][i].Command
						}
					}
					//替换向量中的对应值
					commandTemp = elect(arrayTemp, commands)
					fmt.Printf("For G%d elected command is %s\n", i, commandTemp)
					for j := 1; j < N; j++ {
						if i != j {
							vectorGeneralmap[j][i].Command = commandTemp
						}
					}
				}

				if isAgree(finalDecisonVector(vectorGeneralmap, commands, 1)) {
					fmt.Println("finally make agreement!")
					return
				}



			}

		}

	}
}
