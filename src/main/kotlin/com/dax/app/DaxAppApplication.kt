package com.dax.app

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class DaxAppApplication

fun main(args: Array<String>) {
	runApplication<DaxAppApplication>(*args)
}
