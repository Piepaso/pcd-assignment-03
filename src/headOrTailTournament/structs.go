package main

type PlayerTicket struct {
	ID            int
	AskChoiceChan chan chan CoinSide
	ResultChan    chan bool
}

func createPlayerTicket(id int) PlayerTicket {
	return PlayerTicket{
		ID:            id,
		AskChoiceChan: make(chan chan CoinSide),
		ResultChan:    make(chan bool),
	}
}

type Match struct {
	P1         PlayerTicket
	P2         PlayerTicket
	ResChannel chan PlayerTicket
}
