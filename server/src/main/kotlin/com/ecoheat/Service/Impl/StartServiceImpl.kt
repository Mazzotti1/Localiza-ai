package com.ecoheat.Service.Impl

import com.ecoheat.Exception.RegistroIncorretoException
import com.ecoheat.Service.IStartService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.MessageSource
import org.springframework.stereotype.Service
import java.util.*

@Service
class StartServiceImpl @Autowired constructor(private val messageSource: MessageSource): IStartService {
    val locale = Locale("pt")
    override fun getStartMessage(requestedMessage: String?): String? {
        try {
        val locale = Locale("pt")
        val message = messageSource.getMessage("startup.message", null, locale)
        return message
        } catch (ex: RegistroIncorretoException) {
            val errorMessage = messageSource.getMessage("unautorized.role", null, locale)
            return errorMessage
        }
    }
}

