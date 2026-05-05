package main

import (
	"fmt"
	"math/rand"
	"time"
)

func tossACoin() CoinSide {
	time.Sleep(2000 * time.Millisecond) // Flip time
	return CoinSide(rand.Intn(2) == 1)
}

func referee(id int, matches <-chan Match) {
	for match := range matches {
		replyChan := make(chan CoinSide)
		match.P1.AskChoiceChan <- replyChan
		p1Choice := <-replyChan

		p2Assigned := Tail
		if p1Choice == Tail {
			p2Assigned = Head
		}

		toss := tossACoin()

		fmt.Printf("[Referee %d] P%d plays %s, P%d plays %s. Flip: %s -> ", id, match.P1.ID, p1Choice, match.P2.ID, p2Assigned, toss)

		var winner, loser PlayerTicket
		if toss == p1Choice {
			winner, loser = match.P1, match.P2
		} else {
			winner, loser = match.P2, match.P1
		}

		fmt.Printf("P%d wins!\n", winner.ID)
		loser.ResultChan <- false
		winner.ResultChan <- true

		match.ResChannel <- winner
	}
}
