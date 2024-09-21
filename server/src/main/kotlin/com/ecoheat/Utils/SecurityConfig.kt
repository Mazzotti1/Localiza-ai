package com.ecoheat.Utils

import com.ecoheat.Exception.CustomAccessDeniedHandler
import com.ecoheat.Service.Impl.UserServiceImpl
import io.github.cdimascio.dotenv.Dotenv
import io.github.cdimascio.dotenv.dotenv
import org.springframework.context.MessageSource
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.invoke
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.access.AccessDeniedHandler
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import java.util.*

@Configuration
@EnableWebSecurity
class SecurityConfig{
    val locale = Locale("pt")
    val dotenv = dotenv()
    val routeA = dotenv["ROUTE_A"]!!
    val routeB = dotenv["ROUTE_B"]!!
    val routeC = dotenv["ROUTE_C"]!!
    val routeD = dotenv["ROUTE_D"]!!
    val routeE = dotenv["ROUTE_E"]!!
    val routeF = dotenv["ROUTE_F"]!!
    val routeG = dotenv["ROUTE_F"]!!
    val routeH = dotenv["ROUTE_H"]!!
    val routeJ = dotenv["ROUTE_J"]!!
    val routeK = dotenv["ROUTE_K"]!!
    val routeL = dotenv["ROUTE_L"]!!
    val routeM = dotenv["ROUTE_M"]!!
    val routeN = dotenv["ROUTE_N"]!!
    val routeO = dotenv["ROUTE_O"]!!
    val routeP = dotenv["ROUTE_P"]!!
    val routeQ = dotenv["ROUTE_Q"]!!
    val routeR = dotenv["ROUTE_Q"]!!
    val routeS = dotenv["ROUTE_S"]!!
    val routeT = dotenv["ROUTE_S"]!!
    @Bean
    fun encoder(): PasswordEncoder? {
        return BCryptPasswordEncoder()
    }

    @Bean
    fun accessDeniedHandler(messageSource: MessageSource): AccessDeniedHandler {
        return CustomAccessDeniedHandler(messageSource)
    }

    @Bean
    fun filterChain(http: HttpSecurity, messageSource: MessageSource, userService: UserServiceImpl): SecurityFilterChain {
        http {
            authorizeRequests {
                authorize(routeA, hasRole("ADMIN"))
                authorize(routeB, permitAll)
                authorize(routeC, permitAll)
                authorize(routeD, hasRole("USER"))
                authorize(routeE, hasRole("USER"))
                authorize(routeF, hasRole("USER"))
                authorize(routeG, hasRole("USER"))
                authorize(routeH, hasRole("USER"))
                authorize(routeJ, hasRole("USER"))
                authorize(routeK, hasRole("USER"))
                authorize(routeL, hasRole("USER"))
                authorize(routeM, hasRole("USER"))
                authorize(routeN, hasRole("USER"))
                authorize(routeO, hasRole("USER"))
                authorize(routeP, hasRole("USER"))
                authorize(routeQ, hasRole("USER"))
                authorize(routeR, hasRole("USER"))
                authorize(routeS, hasRole("USER"))
                authorize(routeT, hasRole("USER"))
            }
            cors {  }
            headers { frameOptions { disable() } }
            csrf { disable() }
            sessionManagement {SessionCreationPolicy.STATELESS}
            authorizeRequests {  }
            addFilterBefore<UsernamePasswordAuthenticationFilter>(JwtAuthenticationFilter(JwtToken(messageSource), messageSource,userService))
            formLogin {disable()}
            httpBasic {}
            exceptionHandling {
                accessDeniedHandler = accessDeniedHandler(messageSource)
            }
        }

        return http.build()
    }
}