package main

import "math/rand"

func main() {
	n_players := 8

	tournamentChan := make(chan PlayerTicket)
	done := make(chan bool)

	go tournament("Head or Tails Tournament", n_players, tournamentChan, done)

	for i := 0; i < n_players; i++ {
		var strat Strategy
		if rand.Intn(2) == 0 {
			strat = randomStrategy()
		} else {
			strat = evenStrategy()
		}
		go player(i, strat, tournamentChan)
	}

	<-done
}
