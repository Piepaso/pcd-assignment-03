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
