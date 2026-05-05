package main

type CoinSide bool

const (
	Head CoinSide = true
	Tail CoinSide = false
)

func (c CoinSide) String() string {
	if c == Head {
		return "Head"
	}
	return "Tail"
}

func main() {
	n_players := 32
	n_referees := n_players / 2

	registerChannel := make(chan PlayerTicket)
	matches := make(chan Match) // Canale unificato per il worker pool
	done := make(chan bool)

	for i := 0; i < n_referees; i++ {
		go referee(i, matches)
	}

	go tournament("Head or Tails Tournament", n_players, registerChannel, matches, done)

	for i := 0; i < n_players; i++ {
		var strat Strategy
		if i%2 == 0 {
			strat = randomStrategy
		} else {
			strat = headStrategy
		}
		go player(i, strat, registerChannel)
	}

	<-done
}
