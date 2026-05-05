package main

import "fmt"

func tournament(title string, playersNum int, registerChan <-chan PlayerTicket, matches chan<- Match, done chan<- bool) {

	fmt.Printf("=== Registration is now open for the tournament %s ===\n", title)

	var activePlayers []PlayerTicket
	for i := 0; i < playersNum; i++ {
		activePlayers = append(activePlayers, <-registerChan)
	}

	fmt.Printf("=== Registration closed	===\n")
	fmt.Printf("=== Welcome to the %s tournament ===\n", title)

	round := 1

	for len(activePlayers) > 1 {
		fmt.Printf("\n--- Round %d start, (%d players) ---\n", round, len(activePlayers))
		resultsChannel := make(chan PlayerTicket)

		for i := 0; i < len(activePlayers); i += 2 {
			p1 := activePlayers[i]
			p2 := activePlayers[i+1]

			go func(player1, player2 PlayerTicket) {
				matches <- Match{
					P1:         player1,
					P2:         player2,
					ResChannel: resultsChannel,
				}
			}(p1, p2)
		}

		var nextRoundPlayers []PlayerTicket
		for i := 0; i < len(activePlayers)/2; i++ {
			roundWinner := <-resultsChannel
			nextRoundPlayers = append(nextRoundPlayers, roundWinner)
		}

		activePlayers = nextRoundPlayers
		round++
	}

	fmt.Printf("\n🏆 The %s winner is... Player %d! 🏆\n", title, activePlayers[0].ID)
	done <- true
}
