package id.nearyou.app

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class NearyouAppApplication

fun main(args: Array<String>) {
	runApplication<NearyouAppApplication>(*args)
}
