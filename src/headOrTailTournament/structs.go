package main

type PlayerTicket struct {
	id            int
	askChoiceChan chan chan CoinSide
	resultChan    chan bool
}

type Match struct {
	player1    PlayerTicket
	player2    PlayerTicket
	ResChannel chan<- PlayerTicket
}

func createPlayerTicket(id int) PlayerTicket {
	return PlayerTicket{
		id:            id,
		askChoiceChan: make(chan chan CoinSide),
		resultChan:    make(chan bool),
	}
}

func createMatch(p1, p2 PlayerTicket, resChan chan<- PlayerTicket) Match {
	return Match{
		player1:    p1,
		player2:    p2,
		ResChannel: resChan,
	}
}
