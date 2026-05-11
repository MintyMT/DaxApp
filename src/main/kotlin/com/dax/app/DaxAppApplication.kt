package com.dax.app

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
class DaxAppApplication

fun main(args: Array<String>) {
	runApplication<DaxAppApplication>(*args)
}
