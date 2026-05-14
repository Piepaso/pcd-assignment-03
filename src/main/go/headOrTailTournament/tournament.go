package main

import (
	"fmt"
	"math/rand"
)

func tournament(title string, playersNum int, tournamentChan <-chan PlayerTicket, refereeChannels []chan Match, done chan<- bool) {

	fmt.Printf("=== Registration is now open for the tournament %s ===\n", title)

	var players []PlayerTicket
	for i := 0; i < playersNum; i++ {
		players = append(players, <-tournamentChan)
	}

	fmt.Printf("=== Registration closed	===\n")

	rand.Shuffle(len(players), func(i, j int) {
		players[i], players[j] = players[j], players[i]
	})

	round := 1

	fmt.Printf("=== Welcome to the %s tournament ===\n", title)

	for len(players) > 1 {
		fmt.Printf("\n--- Round %d start, (%d players) ---\n", round, len(players))
		resultsChannel := make(chan PlayerTicket)

		for i := 0; i < len(players); i += 2 {
			player1 := players[i]
			player2 := players[i+1]

			go func() {
				refereeChannels[i/2] <- createMatch(player1, player2, resultsChannel)
			}()
		}

		var nextRoundPlayers []PlayerTicket
		for i := 0; i < len(players)/2; i++ {
			roundWinner := <-resultsChannel
			nextRoundPlayers = append(nextRoundPlayers, roundWinner)
		}

		players = nextRoundPlayers
		round++
	}

	fmt.Printf("\n🏆 The %s winner is... Player %d! 🏆\n", title, players[0].id)
	done <- true
}
