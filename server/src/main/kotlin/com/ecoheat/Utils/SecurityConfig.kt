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
    private val dotenv = dotenv()
    val routeA: String? = dotenv["ROUTE_A"]
    val routeB : String? = dotenv["ROUTE_B"]
    val routeC : String? = dotenv["ROUTE_C"]
    val routeD : String? = dotenv["ROUTE_D"]
    val routeE : String? = dotenv["ROUTE_E"]
    val routeF : String? = dotenv["ROUTE_F"]
    val routeG : String? = dotenv["ROUTE_F"]
    val routeH : String? = dotenv["ROUTE_H"]
    val routeJ : String? = dotenv["ROUTE_J"]
    val routeK : String? = dotenv["ROUTE_K"]
    val routeL : String? = dotenv["ROUTE_L"]
    val routeM : String? = dotenv["ROUTE_M"]
    val routeN : String? = dotenv["ROUTE_N"]
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
                routeA?.let { authorize(it, hasRole("ADMIN")) }
                routeB?.let { authorize(it, permitAll) }
                routeC?.let { authorize(it, permitAll) }
                routeD?.let { authorize(it, hasRole("USER")) }
                routeE?.let { authorize(it, hasRole("USER")) }
                routeF?.let { authorize(it, hasRole("USER")) }
                routeG?.let { authorize(it, hasRole("USER")) }
                routeH?.let { authorize(it, hasRole("USER")) }
                routeJ?.let { authorize(it, hasRole("USER")) }
                routeK?.let { authorize(it, hasRole("USER")) }
                routeL?.let { authorize(it, hasRole("USER")) }
                routeM?.let { authorize(it, hasRole("USER")) }
                routeN?.let { authorize(it, hasRole("USER")) }
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