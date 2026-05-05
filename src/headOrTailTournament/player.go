package main

import (
	"math/rand"
	"time"
)

type Strategy func() CoinSide

func headStrategy() CoinSide {
	time.Sleep(2000 * time.Millisecond) // Think time
	return Head
}

func randomStrategy() CoinSide {
	time.Sleep(2000 * time.Millisecond) // Think time
	return CoinSide(rand.Intn(2) == 1)
}

func player(id int, strategy Strategy, registerChan chan<- PlayerTicket) {
	ticket := createPlayerTicket(id)

	time.Sleep(time.Duration(rand.Intn(1000)) * time.Millisecond) // Registration time
	registerChan <- ticket

	for {
		select {
		case replyChan := <-ticket.AskChoiceChan:
			replyChan <- strategy()

		case won := <-ticket.ResultChan:
			if !won {
				return
			}
		}
	}
}
