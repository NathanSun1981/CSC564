package main

import (
	"chat-room/routes"
	"github.com/gin-gonic/gin"
	"io/ioutil"
	"net/http"
	"strconv"
)

func index() gin.HandlerFunc {
	return func(c *gin.Context) {
		c.HTML(http.StatusOK, "index.html", gin.H{
			"title": "Home Page",
		},)
	}
}

func resetMsgNo() {
	res, err := http.Get("http://127.0.0.1:8000/get?key=msgno")
	if err != nil {
		// handle error
	}
	if res !=nil {
		defer res.Body.Close()

		msgno, _ := ioutil.ReadAll(res.Body)

		//println("print start:" + string(msgno))
		//println("print end")
		if string(msgno) == "\n" {
			res, err := http.Get("http://127.0.0.1:8000/set?key=msgno&value=" + strconv.Itoa(0))
			if err != nil {
				// handle error
			}
			defer res.Body.Close()

		}
	}

}

func main() {

	resetMsgNo()

	s := gin.Default()
	s.GET("/", index())
	routes.WebSocket(s)
	s.LoadHTMLGlob("./templates/*")
	s.Static("/static", "./static")
	s.Run()

}
