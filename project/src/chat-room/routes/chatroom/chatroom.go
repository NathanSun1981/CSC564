package chatroom

import (
	"container/list"
	"io/ioutil"
	"net/http"
	"strconv"
	"strings"
	"time"
)

// message number of storage
const archiveSize = 10

const (
	//msg
	EVENT_TYPE_MSG = iota
	EVENT_TYPE_JOIN
	EVENT_TYPE_LEAVE

)

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


// user subscription
type Subscription struct {

	// event log
	Archive []Event

	// channel for message
	// user receive message from this channel
	NewMsg <-chan Event

}

var (
	// channel for receive subscription event
	// broadcast the history to user when he join
	subscribe = make(chan (chan<- Subscription), 10)

	//channel for unsubscribe
	//release the history from channel
	//and delete user from the user list
	unsubscribe = make(chan (<-chan Event), 10)

	//channel for the message broadcasting
	publish = make(chan Event, 10)
)

// unsubscribe
func (s Subscription) Cancel() {
	unsubscribe <- s.NewMsg // delete user from user list
}

func newEvent(typ int , user, msg string) Event {
	return Event{typ, user, time.Now().Unix(), msg}
}

// enter function for user subscript chatroom
// return the object of user subscription, user read the message (immediate message and historical message)
func Subscribe() Subscription {
	resp := make(chan Subscription)
	subscribe <- resp
	return <-resp
}

// send message to chatroom
func Join(user string) {
	publish <- newEvent(EVENT_TYPE_JOIN, user, ": **join the room**")
}

func Say(user, message string) {
	publish <- newEvent(EVENT_TYPE_MSG, user, ":"+message)
}

func Leave(user string) {
	publish <- newEvent(EVENT_TYPE_LEAVE, user, ": **leave the room**")
}

// handel the event
func chatroom() {

	// list of archived message
	archive := list.New()

	// subscriber list
	subscribers := list.New()

	for {
		select {

		// when a new user join, get the object of user and add to the archived event list.
		case ch := <- subscribe:

			var events []Event

			//add message to archived events
			for e := archive.Front(); e != nil; e = e.Next() {
				events = append(events, e.Value.(Event))
			}

			subscriber := make(chan Event, 10)

			subscribers.PushBack(subscriber)

			//here I will use KV storage to archive subscribers, that if the machine fail, the subscribers never lost.

			ch <- Subscription{events, subscriber}

		//new message arrive
		case event := <-publish:

			//receive message and push to all users
			for ch := subscribers.Front(); ch != nil; ch = ch.Next() {
				//println(event.Text)
				ch.Value.(chan Event) <- event
			}

			//after pushing, archive message with archiveSize


			if archive.Len() >= archiveSize {
			archive.Remove(archive.Front())
			}
			archive.PushBack(event)
			res, err := http.Get("http://127.0.0.1:8000/get?key=msgno")
			if err == nil {
				// handle error
				println("retrieved msgno from host")
				defer res.Body.Close()

				msgno, _ := ioutil.ReadAll(res.Body)
				n, err := strconv.Atoi(strings.TrimRight(string(msgno), "\n"))
				if err == nil {
					n += 1
					res, err := http.Get("http://127.0.0.1:8000/set?key=msgno&value=" + strconv.Itoa(n))
					println("set msgno to host")
					if err != nil {
						// handle error
					}
					defer res.Body.Close()

					//here I will use KV storage to archive message, that if the machine fail, the message never lost.
					response, err := http.Get("http://127.0.0.1:8000/set?key=msg" + strconv.Itoa(n) + "&value=" + strings.Replace(strconv.Itoa(event.Type) + " " + event.User + " " + event.Text + " " + strconv.FormatInt(event.Timestamp, 10), " ", "|", -1))
					println("set msg to host")
					if err != nil {
						// handle error
					}
					defer response.Body.Close()
				}
			}





		//unsubscribe
		case unsub := <-unsubscribe:

			//find all subscriber
			for ch := subscribers.Front(); ch != nil; ch = ch.Next() {
				if ch.Value.(chan Event) == unsub {
					subscribers.Remove(ch)
					break
				}
			}
		}
	}
}

// create goroutine loop chatroom
func init() {
	go chatroom()
}

