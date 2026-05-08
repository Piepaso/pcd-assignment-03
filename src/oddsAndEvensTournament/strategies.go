package main

import (
	"math/rand"
	"time"
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

func slowStrategy()  Strategy {
	return Strategy{
		chooseReminder: func() int {
			time.Sleep(1000 * time.Millisecond)
			return 0
		},
		throwNumber: func(reminder int) int {
			time.Sleep(1000 * time.Millisecond)	
			return reminder
		},
	}
} 
