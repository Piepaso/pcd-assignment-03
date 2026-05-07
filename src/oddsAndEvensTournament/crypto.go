package main

import "math/rand"

func encrypt(value int, key int) int {
	return value + key
}

func decrypt(value int, key int) int {
	return value - key
}

func genKey() int {
	return rand.Intn(2)
}
