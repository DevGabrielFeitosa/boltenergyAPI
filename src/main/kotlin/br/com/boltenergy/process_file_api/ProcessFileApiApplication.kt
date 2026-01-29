package br.com.boltenergy.process_file_api

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
class ProcessFileApiApplication

fun main(args: Array<String>) {
	runApplication<ProcessFileApiApplication>(*args)
}
