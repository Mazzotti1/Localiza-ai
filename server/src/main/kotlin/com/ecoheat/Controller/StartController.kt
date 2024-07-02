package com.ecoheat.Controller

import com.ecoheat.Exception.RegistroIncorretoException
import com.ecoheat.Service.Impl.StartServiceImpl
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.MessageSource
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.*

@RestController
@RequestMapping("/start")
class StartController(private val messageSource: MessageSource) {
    @Autowired
    private val startService: StartServiceImpl? = null
    val locale = Locale("pt")

    @GetMapping
    fun getStartMessage (requestedMessage: String?): Any? {
        try {
            val message = startService!!.getStartMessage(requestedMessage)
            return message
        } catch (ex: RegistroIncorretoException) {
            val errorMessage = messageSource.getMessage("generic.service.error", null, locale)
            return ResponseEntity(errorMessage, HttpStatus.BAD_REQUEST)
        }

    }
}