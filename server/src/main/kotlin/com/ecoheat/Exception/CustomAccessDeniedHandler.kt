package com.ecoheat.Exception

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.context.MessageSource
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.web.access.AccessDeniedHandler
import org.springframework.stereotype.Component
import java.util.*

@Component
class CustomAccessDeniedHandler(private val messageSource: MessageSource?) : AccessDeniedHandler {
    val locale = Locale("pt")
    override fun handle(
        request: HttpServletRequest?,
        response: HttpServletResponse?,
        accessDeniedException: AccessDeniedException?
    ) {
        response!!.status = HttpServletResponse.SC_FORBIDDEN
        response!!.contentType = "application/json"
        response!!.characterEncoding = "UTF-8"
        response!!.writer.write(messageSource!!.getMessage("unautorized.role", null, locale))
    }
}
