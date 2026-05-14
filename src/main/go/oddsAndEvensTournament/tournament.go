package main

import (
	"fmt"
	"math/rand"
)

func tournament(title string, playersNum int, tournamentChan <-chan PlayerTicket, done chan<- bool) {

	var players = make(map[int]PlayerTicket)
	var inGamePlayers = []int{}
	winnerChannel := make(chan int)

	fmt.Printf("=== Registration is now open for the tournament %s ===\n", title)

	for i := 0; i < playersNum; i++ {
		newPlayer := <-tournamentChan
		players[newPlayer.id] = newPlayer
		inGamePlayers = append(inGamePlayers, newPlayer.id)
		newPlayer.sendMeOtherChannel <- winnerChannel
	}

	fmt.Printf("=== Registration closed	===\n")

	rand.Shuffle(len(inGamePlayers), func(i, j int) {
		inGamePlayers[i], inGamePlayers[j] = inGamePlayers[j], inGamePlayers[i]
	})

	round := 1

	fmt.Printf("=== Welcome to the %s tournament ===\n", title)

	for len(inGamePlayers) > 1 {
		fmt.Printf("\n--- Round %d start, (%d players) ---\n", round, len(inGamePlayers))
		for i := 0; i < len(inGamePlayers); i += 2 {
			player1 := players[inGamePlayers[i]]
			player2 := players[inGamePlayers[i+1]]

			go func() {
				player1.sendMeOtherChannel <- player2.myChannel
				player2.sendMeOtherChannel <- player1.myChannel
			}()
		}

		var nextRoundPlayers = []int{}
		var countEliminated = 0
		for i := 0; i < len(inGamePlayers); i++ {
			roundWinner := <-winnerChannel
			if roundWinner >= 0 {
				nextRoundPlayers = append(nextRoundPlayers, roundWinner)
			} else {
				countEliminated++
			}
		}
		if countEliminated != len(inGamePlayers)/2 {
			fmt.Printf("Expected %d players to be eliminated, but got %d", len(inGamePlayers)/2, countEliminated)
			done <- true
			return
		}

		inGamePlayers = nextRoundPlayers
		round++
	}

	fmt.Printf("\n🏆 The %s winner is... Player %d! 🏆\n", title, inGamePlayers[0])
	done <- true
}
