package main

import (
	"fmt"
	"math/rand"
	"time"
)

func tossACoin() CoinSide {
	time.Sleep(1000 * time.Millisecond) // Flip time
	return CoinSide(rand.Intn(2) == 1)
}

func referee(id int, channel <-chan Match) {
	var p2Assigned CoinSide
	var winner, loser PlayerTicket
	replyChan := make(chan CoinSide)

	for {
		match := <-channel
		match.player1.askChoiceChan <- replyChan
		p1Choice := <-replyChan

		if p1Choice == Tail {
			p2Assigned = Head
		} else {
			p2Assigned = Tail
		}

		toss := tossACoin()

		if toss == p1Choice {
			winner, loser = match.player1, match.player2
		} else {
			winner, loser = match.player2, match.player1
		}

		fmt.Printf("[Referee %d] P%d plays %s, P%d plays %s. Flip: %s -> P%d wins!\n",
			id, match.player1.id, p1Choice, match.player2.id, p2Assigned, toss, winner.id)

		loser.resultChan <- false
		winner.resultChan <- true

		match.ResChannel <- winner
	}
}
