package main

import (
	"math/rand"
)

type Strategy struct {
	chooseReminder func() int
	throwNumber    func(int) int
}

func randomStrategy() Strategy {
	return Strategy{
		chooseReminder: func() int {
			return rand.Intn(2)
		},
		throwNumber: func(reminder int) int {
			return rand.Intn(6)
		},
	}
}

func evenStrategy() Strategy {
	return Strategy{
		chooseReminder: func() int {
			return 0
		},
		throwNumber: func(reminder int) int {
			return reminder
		},
	}
}
