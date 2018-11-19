package main

import (
	"chat-room/routes"
	"github.com/gin-gonic/gin"
	"net/http"
)

func index() gin.HandlerFunc {
	return func(c *gin.Context) {
		c.HTML(http.StatusOK, "index.html", gin.H{
			"title": "Home Page",
		},)
	}
}

func main() {
	s := gin.Default()
	s.GET("/", index())
	routes.WebSocket(s)
	s.LoadHTMLGlob("./templates/*")
	s.Static("/static", "./static")
	s.Run()

}
