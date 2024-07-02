package com.ecoheat.Controller

import com.ecoheat.Exception.RegistroIncorretoException
import com.ecoheat.Model.Forms.DeleteForm
import com.ecoheat.Model.Forms.GetUserForm
import com.ecoheat.Model.Forms.UserForm
import com.ecoheat.Model.Users
import com.ecoheat.Service.Impl.UserServiceImpl
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.MessageSource
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*
import javax.naming.AuthenticationException

@RestController
@RequestMapping("/users")
class UsersController (private val messageSource: MessageSource){
    @Autowired
    private val service: UserServiceImpl? = null

    val locale = Locale("pt")
    @PostMapping("/register")
    fun create(@RequestBody form: UserForm?): ResponseEntity<out Any> {
        try {
            val user = service!!.create(form!!)
            return ResponseEntity(user, HttpStatus.CREATED)
        } catch (ex: RegistroIncorretoException) {
            val errorMessage = ex.message ?: messageSource.getMessage("user.create.error", null, locale)
            return ResponseEntity(errorMessage, HttpStatus.BAD_REQUEST)
        }
    }

    @PostMapping("/login")
    @Throws(AuthenticationException::class)
    fun login(@RequestBody form: UserForm): ResponseEntity<out Any> {
        try {
            val users: Users? = service?.login(form.name, form.password)
            return ResponseEntity.ok().body(users?.token)
        } catch (ex: RegistroIncorretoException) {
            val errorMessage = ex.message ?: messageSource.getMessage("user.create.error", null, locale)
            return ResponseEntity(errorMessage, HttpStatus.BAD_REQUEST)
        }
    }

    @DeleteMapping("/delete/{userId}")
    fun deleteAccount(@PathVariable userId: String): ResponseEntity<String> {
        try {
            val deleted = service!!.deleteUserById(userId)
            return if (deleted) {
                val successMessage = messageSource.getMessage("user.success.delete", null, locale)
                ResponseEntity.ok(successMessage)
            } else {
                ResponseEntity.notFound().build()
            }
        }catch(ex: Exception){
            val errorMessage = messageSource.getMessage("user.error.delete", null, locale)
            return ResponseEntity(errorMessage, HttpStatus.BAD_REQUEST)
        }
    }

    @DeleteMapping("/desactivate/{userId}")
    fun desactivateAccount(@PathVariable userId: String): ResponseEntity<String> {
        try {
            val desactivated = service!!.desactivateUserById(userId)
            return if (desactivated) {
                val successMessage = messageSource.getMessage("user.success.desactivate", null, locale)
                ResponseEntity.ok(successMessage)
            } else {
                ResponseEntity.notFound().build()
            }
        }catch(ex: Exception){
            val errorMessage = messageSource.getMessage("user.error.desactivate", null, locale)
            return ResponseEntity(errorMessage, HttpStatus.BAD_REQUEST)
        }
    }

    @GetMapping("/getUserById")
    fun getUserById(@RequestBody form: GetUserForm): ResponseEntity<out Any> {
        try {
            val user = service!!.getUserById(form.userId)
            return ResponseEntity.ok().body(user)
        }catch(ex: RegistroIncorretoException){
            val errorMessage = messageSource.getMessage("user.error.delete", null, locale)
            return ResponseEntity(errorMessage, HttpStatus.BAD_REQUEST)
        }
    }
}