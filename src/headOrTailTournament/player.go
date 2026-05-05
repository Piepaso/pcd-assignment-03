package main

import (
	"math/rand"
	"time"
)

type Strategy func() CoinSide

func headStrategy() CoinSide {
	time.Sleep(1000 * time.Millisecond) // Think time
	return Head
}

func randomStrategy() CoinSide {
	time.Sleep(1000 * time.Millisecond) // Think time
	return CoinSide(rand.Intn(2) == 1)
}

func player(id int, strategy Strategy, tournamentChan chan<- PlayerTicket) {
	time.Sleep(time.Duration(rand.Intn(1000)) * time.Millisecond)

	ticket := createPlayerTicket(id)
	tournamentChan <- ticket

	for {
		select {
		case replyChan := <-ticket.askChoiceChan:
			replyChan <- strategy()

		case won := <-ticket.resultChan:
			if !won {
				return
			}
		}
	}
}
