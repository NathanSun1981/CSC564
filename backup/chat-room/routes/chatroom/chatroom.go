package chatroom

import (
	"container/list"
	"time"
)

// message number of storage
const archiveSize = 10

const (
	//msg
	EVENT_TYPE_MSG = iota

	EVENT_TYPE_JOIN
	EVENT_TYPE_TYPING
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
	return Event{typ, user, time.Now().UnixNano(), msg}
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
	publish <- newEvent(EVENT_TYPE_MSG, user, ": "+message)
}

func Leave(user string) {
	publish <- newEvent(EVENT_TYPE_LEAVE, user, ": **leave the room**")
}

func Typing(user string)  {
	publish <- newEvent(EVENT_TYPE_TYPING,user,": **typing**")
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
				ch.Value.(chan Event) <- event
			}

			//after pushing, archive message with archiveSize

			if archive.Len() >= archiveSize {
				archive.Remove(archive.Front())
			}
			archive.PushBack(event)

			//here I will use KV storage to archive message, that if the machine fail, the message never lost.


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

