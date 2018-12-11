package main

import (
	"fmt"
	"log"
	"net"
	"net/http"
	"os"
	"strconv"
	"sync"
)

type stCached struct {
	hs   *httpServer
	opts *options
	log  *log.Logger
	cm   *cacheManager
	raft *raftNodeInfo
}

type stCachedContext struct {
	st *stCached
}

var mutex sync.RWMutex

// Start a proxy server listen on fromport
// this proxy will then forward all request from fromport to toport
//
// Notice: a service must has been started on toport
func proxyStart(fromport, toport int, proxyFlag *bool) {

	proxyaddr := fmt.Sprintf(":%d", fromport)

	mutex.Lock()
	defer mutex.Unlock()

	proxylistener, err := net.Listen("tcp", proxyaddr)
	if err != nil {
		fmt.Printf("Unable to listen on: %s, error: %s\n", proxyaddr, err.Error())
	}

	defer println("port 8000 closed")
	defer proxylistener.Close()


	for {
		if  *proxyFlag == true {
			proxyconn, err := proxylistener.Accept()
			if err != nil {
				fmt.Printf("Unable to accept a request, error: %s\n", err.Error())
				continue
			}

			// Read a header firstly in case you could have opportunity to check request
			// whether to decline or proceed the request
			buffer := make([]byte, 1024)
			n, err := proxyconn.Read(buffer)
			if err != nil {
				fmt.Printf("Unable to read from input, error: %s\n", err.Error())
				continue
			}

			targetaddr := fmt.Sprintf("localhost:%d", toport)
			targetconn, err := net.Dial("tcp", targetaddr)
			if err != nil {
				fmt.Printf("Unable to connect to: %s, error: %s\n", targetaddr, err.Error())
				proxyconn.Close()
				continue
			}

			n, err = targetconn.Write(buffer[:n])
			if err != nil {
				fmt.Printf("Unable to write to output, error: %s\n", err.Error())
				proxyconn.Close()
				targetconn.Close()
				continue
			}

			go proxyRequest(proxyconn, targetconn)
			go proxyRequest(targetconn, proxyconn)
		}else{
			break
		}
	}
}

// Forward all requests from r to w
func proxyRequest(r net.Conn, w net.Conn) {
	defer r.Close()
	defer w.Close()

	var buffer = make([]byte, 4096000)
	for {
		n, err := r.Read(buffer)
		if err != nil {
			fmt.Printf("Unable to read from input, error: %s\n", err.Error())
			break
		}

		n, err = w.Write(buffer[:n])
		if err != nil {
			fmt.Printf("Unable to write to output, error: %s\n", err.Error())
			break
		}
	}
}

func main() {
	st := &stCached{
		opts: NewOptions(),
		log:  log.New(os.Stderr, "stCached: ", log.Ldate|log.Ltime),
		cm:   NewCacheManager(),
	}
	ctx := &stCachedContext{st}

	var l net.Listener
	var err error
	l, err = net.Listen("tcp", st.opts.httpAddress)
	if err != nil {
		st.log.Fatal(fmt.Sprintf("listen %s failed: %s", st.opts.httpAddress, err))
	}
	st.log.Printf("http server listen:%s", l.Addr())

	logger := log.New(os.Stderr, "httpserver: ", log.Ldate|log.Ltime)
	httpServer := NewHttpServer(ctx, logger)
	st.hs = httpServer
	go func() {
		http.Serve(l, httpServer.mux)
	}()

	raft, err := newRaftNode(st.opts, ctx)
	if err != nil {
		st.log.Fatal(fmt.Sprintf("new raft node failed:%v", err))
	}
	st.raft = raft

	if st.opts.joinAddress != "" {
		err = joinRaftCluster(st.opts)
		if err != nil {
			st.log.Fatal(fmt.Sprintf("join raft cluster failed:%v", err))
		}
	}

	proxyStartFlag := true
	// monitor leadership
	for {
		select {
		case leader := <-st.raft.leaderNotifyCh:
			if leader {
				proxyStartFlag = true
				st.log.Println("become leader, enable write api")
				st.hs.setWriteFlag(true)
				st.log.Println("transfer port 8000 to " + strconv.Itoa(l.Addr().(*net.TCPAddr).Port))
				go proxyStart(8000, l.Addr().(*net.TCPAddr).Port, &proxyStartFlag)
			} else {
				st.log.Println("become follower, close write api")
				st.hs.setWriteFlag(false)
				st.log.Println("shut down port 8000")
				proxyStartFlag = false


			}
		}
	}
}
