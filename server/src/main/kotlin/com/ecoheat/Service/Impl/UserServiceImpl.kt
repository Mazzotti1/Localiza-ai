package com.ecoheat.Service.Impl

import com.ecoheat.Exception.RegistroIncorretoException
import com.ecoheat.Model.Forms.UserForm
import com.ecoheat.Model.Roles
import com.ecoheat.Model.Users
import com.ecoheat.Model.Users.Companion.DEFAULT_ROLE_ID
import com.ecoheat.Repository.UsersRepository
import com.ecoheat.Service.IUsersService
import com.ecoheat.Utils.JwtToken
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.MessageSource
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.util.*

@Service
class UserServiceImpl @Autowired constructor(
    private val usersRepository: UsersRepository,
    private val jwtToken: JwtToken,
    private val messageSource: MessageSource
) : IUsersService {

    val locale = Locale("pt")
    @Autowired
    private lateinit var passwordEncoder: PasswordEncoder
    override fun getUserById(id: String): Pair<Users?, String?> {
        try {
            val user = usersRepository.findById(id.toLong()).orElse(null)
            return Pair(user, null)
        } catch (ex: RegistroIncorretoException) {
            val errorMessage = messageSource.getMessage("unautorized.role", null, locale)
            return Pair(null, errorMessage)
        }
    }

    @Throws(RegistroIncorretoException::class)
    override fun create(form: UserForm?): Users {

        if (usersRepository.existsByName(form?.name!!.lowercase(Locale.getDefault()))) {
            throw RegistroIncorretoException(messageSource.getMessage("user.same.name", null, locale))
        }
        val defaultActive = true
        val defaultRole = Roles(id = DEFAULT_ROLE_ID, name = "USER")
        val users = Users(id = 0, name = form.name!!, password = form.password!!, role = defaultRole, isActive = defaultActive)
        users.name = form.name.toString()
        val rawPassword: String? = form.password

        if (rawPassword != null && rawPassword.length >= 6) {
            users.password = passwordEncoder.encode(rawPassword).toString()
            return usersRepository.save(users)
        } else {
            throw RegistroIncorretoException(messageSource.getMessage("user.same.password", null, locale))
        }
    }

    @Throws(RegistroIncorretoException::class)
    override fun login(name: String?, password: String?): Users? {
        val users: Users = usersRepository.findByName(name) ?: throw RegistroIncorretoException(messageSource.getMessage("user.notfound", null, locale))

        if (!isActiveUser(users.id)) {
            throw RegistroIncorretoException(messageSource.getMessage("user.inactive", null, locale))
        }

        if (!passwordEncoder.matches(password, users.password) ) {
            throw RegistroIncorretoException(messageSource.getMessage("user.incorrect.password", null, locale))
        }
        val token: String? = jwtToken.generateToken(
            users.id,
            users.name,
            users.role.toString(),
        )
        users.token = token
        usersRepository.save(users)
        return users
    }
    fun isActiveUser(userId: Long): Boolean {
        val user = usersRepository.findById(userId)
        return user.isPresent && user.get().isActive
    }
    override fun deleteUserById(id: String): Boolean {
        val id = usersRepository.findById(id.toLong())
        if (id.isPresent) {
            usersRepository.delete(id.get())
            return true
        }
        return false
    }

    override fun desactivateUserById(id: String): Boolean {
        val userId = id.toLong()
        val userOptional = usersRepository.findById(userId)
        if (userOptional.isPresent) {
            val user = userOptional.get()
            user.isActive = false
            usersRepository.save(user)
            return true
        }
        return false
    }

}