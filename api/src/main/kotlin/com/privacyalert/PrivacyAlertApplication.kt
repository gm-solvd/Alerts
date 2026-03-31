package com.privacyalert

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class PrivacyAlertApplication

fun main(args: Array<String>) {
    runApplication<PrivacyAlertApplication>(*args)
}
