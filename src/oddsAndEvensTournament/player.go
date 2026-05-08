package main

import "fmt"

type PlayerTicket struct {
	id                 int
	myChannel          chan int
	sendMeOtherChannel chan chan int
}

func player(id int, strategy Strategy, tournamentChannel chan<- PlayerTicket) {
	myChannel := make(chan int)
	sendMeOtherChannel := make(chan chan int)
	internalSyncChannel := make(chan struct{})

	tournamentChannel <- PlayerTicket{id: id, myChannel: myChannel, sendMeOtherChannel: sendMeOtherChannel}
	winnerChannel := <-sendMeOtherChannel

	for {
		myKey := genKey()
		otherChannel := <-sendMeOtherChannel

		reminder := strategy.chooseReminder()
		select {
		case otherChannel <- reminder:
			fmt.Printf("[Player%d] I chose reminder %d\n", id, reminder)
		case otherChoice := <-myChannel:
			reminder = (otherChoice + 1) % 2
			fmt.Printf("[Player%d] I got %d\n", id, reminder)
		}

		myNumber := strategy.throwNumber(reminder)

		go func() {
			otherChannel <- encrypt(myNumber, myKey)
			internalSyncChannel <- struct{}{}
		}()

		otherNumberEncrypted := <-myChannel

		go func() {
			<-internalSyncChannel
			otherChannel <- myKey
		}()

		otherKey := <-myChannel
		otherNumber := decrypt(otherNumberEncrypted, otherKey)

		if !isValid(otherNumber) {
			fmt.Printf("[Player%d] Error in decription\n", id)
			winnerChannel <- id
		} else {
			if (myNumber+otherNumber)%2 == reminder {
				fmt.Printf("[Player%d] Sum is %d, I won!\n", id, myNumber+otherNumber)
				winnerChannel <- id
			} else {
				winnerChannel <- -1
				fmt.Printf("[Player%d] Sum is %d, I lost, better luck next time.\n", id, myNumber+otherNumber)
				return
			}
		}
	}
}
