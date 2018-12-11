package routes

import (
	"chat-room/routes/chatroom"
	"github.com/gin-gonic/gin"
	"github.com/gorilla/websocket"
	"io/ioutil"
	"net/http"
	"strconv"
	"strings"
	"time"
)

const (
	//msg
	EVENT_TYPE_MSG = iota
	EVENT_TYPE_JOIN
	EVENT_TYPE_LEAVE

)

var upgrader = websocket.Upgrader{
	ReadBufferSize:  1024,
	WriteBufferSize: 1024,
}


//chat room event
type Event struct {
	// event type
	Type      	int
	// user name
	User      	string
	// time stamps
	Timestamp 	int64
	// event content
	Text		string
}

func newEvent(typ int , user string, timestamp int64, msg string) Event {
	return Event{typ, user, timestamp, msg}
}

func WebSocket(server *gin.Engine)  {

	r := server.Group("/websocket")

	r.GET("/room", func(c *gin.Context) {
		user := c.Query("user")
		c.HTML(http.StatusOK,"websocket.html", struct {
			User string
		}{user})
	})


	r.GET("/room/socket", func(c *gin.Context) {
		user := c.Query("user")

		conn, err := upgrader.Upgrade(c.Writer, c.Request, nil)
		if err != nil || user == "" {
			panic(err)
		}

		chatroom.Join(user)
		defer chatroom.Leave(user)

		// Join the room.
		subscription := chatroom.Subscribe()
		defer subscription.Cancel()

		// Send down the archive.


		res, err := http.Get("http://127.0.0.1:8000/get?key=msgno")
		if err == nil {

			println("retrieved msgno from host")
			defer res.Body.Close()

			msgno, _ := ioutil.ReadAll(res.Body)

			n, err := strconv.Atoi(strings.TrimRight(string(msgno), "\n"))

			if err == nil {
				for i := 1; i <= n; i++ {
					//here I will use KV storage to archive message, that if the machine fail, the message never lost.
					println("retrieve msg from host")
					response, err := http.Get("http://127.0.0.1:8000/get?key=msg" + strconv.Itoa(i))
					if err == nil {
						body, _ := ioutil.ReadAll(response.Body)

						temp := strings.Split(string(body), "|")
						if temp[0] == "\n" {
							continue
						}
						timestamp := strings.TrimRight(temp[len(temp)-1],"\n")

						iTime, err := strconv.ParseInt(timestamp, 10, 64)
						if err != nil {
							panic(err)
						}
						tm := time.Unix(iTime, 0)

						//get the message from temp
						msg :=""
						for j:= 2; j< len(temp)-1; j++{
							msg += temp[j] + " "
						}

						typ := EVENT_TYPE_MSG

						switch temp[0] {
						case "0":
							typ = EVENT_TYPE_MSG
						case "1":
							typ = EVENT_TYPE_JOIN
						case "2":
							typ = EVENT_TYPE_LEAVE
						}

						event := newEvent(typ, temp[1], tm.UnixNano(), msg + " @ " + tm.Format("Mon Jan _2 15:04:05 2006") +"\n")

						if conn.WriteJSON(&event) != nil {
							// They disconnected
							return
						}


					}
					defer response.Body.Close()
				}
			}

		}else
		{
			for _, event := range subscription.Archive {
				if conn.WriteJSON(&event) != nil {
					// They disconnected
					return
				}
			}
		}


		// In order to select between websocket messages and subscription events, we
		// need to stuff websocket events into a channel.
		newMessages := make(chan string)
		go func() {
			var res = struct {
				Msg string `json:"msg"`
			}{}
			for {
				err := conn.ReadJSON(&res)
				if err != nil {
					close(newMessages)
					return
				}
				newMessages <- res.Msg
			}
		}()

		// Now listen for new events from either the websocket or the chatroom.
		for {
			select {
			case event := <-subscription.NewMsg:
				if conn.WriteJSON(&event) != nil {
					// They disconnected.
					return
				}
			case msg, ok := <-newMessages:
				// If the channel is closed, they disconnected.
				if !ok {
					return
				}
				// Otherwise, say something.
				chatroom.Say(user, msg)
			}
		}
	})
}




