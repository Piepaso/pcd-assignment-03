package main

import "math/rand"

func main() {
	n_players := 32
	n_referees := n_players / 2

	tournamentChan := make(chan PlayerTicket)
	refereeChannels := make([]chan Match, n_referees)
	done := make(chan bool)

	for i := 0; i < n_referees; i++ {
		refereeChannels[i] = make(chan Match)
		go referee(i, refereeChannels[i])
	}

	go tournament("Head or Tails Tournament", n_players, tournamentChan, refereeChannels, done)

	for i := 0; i < n_players; i++ {
		var strat Strategy
		if rand.Intn(2) == 0 {
			strat = randomStrategy
		} else {
			strat = headStrategy
		}
		go player(i, strat, tournamentChan)
	}

	<-done
}
