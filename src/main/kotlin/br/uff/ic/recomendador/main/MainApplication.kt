package br.uff.ic.recomendador.main

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication(scanBasePackages = ["br.uff.ic.recomendador"])
class MainApplication

fun main(args: Array<String>) {
	runApplication<MainApplication>(*args)
}
