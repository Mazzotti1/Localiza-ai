package com.localizaai.ui.ViewModel

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.auth0.jwt.JWT
import com.auth0.jwt.exceptions.JWTDecodeException
import com.auth0.jwt.interfaces.DecodedJWT
import com.localizaai.Model.Delete
import com.localizaai.Model.Login
import com.localizaai.Model.Register
import com.localizaai.R
import com.localizaai.data.repository.DeleteRepository
import com.localizaai.data.repository.DesactivateRepository
import com.localizaai.data.repository.EventsRepository
import com.localizaai.data.repository.LoginRepository
import com.localizaai.data.repository.PlacesRepository
import com.localizaai.data.repository.TrafficRepository
import com.localizaai.data.repository.WeatherRepository
import com.localizaai.ui.screen.MainActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class SettingsScreenViewModel(private val context: Context) : ViewModel() {
    val sharedPreferences = context.getSharedPreferences("preferences", Context.MODE_PRIVATE)
    val themeMode = mutableStateOf(sharedPreferences.getBoolean("mode", true))
    var username by mutableStateOf("")
    var roleName by mutableStateOf("")
    val language = mutableStateOf(sharedPreferences.getString("language", "en") ?: "en")
    private val languages = mutableListOf("en", "pt")
    var hasToken = mutableStateOf(false)

    var isDialogLogoutOpen = mutableStateOf(false)
    var isDialogDeleteOpen = mutableStateOf(false)
    var isDialogDesactivateOpen = mutableStateOf(false)

    var isLoading by mutableStateOf(false)
    private val deleteRepository = DeleteRepository(context)
    private val desactivateRepository = DesactivateRepository(context)

    val deleteStatus = MutableLiveData<String?>()
    private var token = ""

    fun loadThemeState(context: Context) {
            val sharedPreferences =
                context.getSharedPreferences("preferences", Context.MODE_PRIVATE)
            themeMode.value = sharedPreferences.getBoolean("mode", true)
    }

    fun toggleThemeMode(context: Context) {
        themeMode.value = !themeMode.value
        saveThemeState(context)
    }

    private fun saveThemeState(context: Context) {
        val sharedPreferences = context.getSharedPreferences("preferences", Context.MODE_PRIVATE)
        sharedPreferences.edit().putBoolean("mode", themeMode.value).apply()
        val intent = Intent(context, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    fun getTokenProps(context: Context) {
            val sharedPreferences =
                context.getSharedPreferences("preferences", Context.MODE_PRIVATE)
            val token = sharedPreferences.getString("jwtToken", "")

            if (!token.isNullOrBlank()) {
                try {
                    val jwt = JWT.decode(token)

                    username = jwt.getClaim("name").asString()
                    val roleClaim = jwt.getClaim("role").asString()

                    val roleNameMatchResult = Regex("name=(\\w+)").find(roleClaim)
                    roleName = roleNameMatchResult?.groupValues?.get(1).toString()
                    hasToken = mutableStateOf(true)
                } catch (e: JWTDecodeException) {
                    println("Erro ao decodificar o token: ${e.message}")
                }
            } else {
                println("Ainda não há token")
            }
    }

    fun loadLanguageState(context: Context) {
        val sharedPreferences = context.getSharedPreferences("preferences", Context.MODE_PRIVATE)
        val savedLanguage = sharedPreferences.getString("language", "en") ?: "en"
        language.value = savedLanguage

        val locale = Locale(savedLanguage)
        Locale.setDefault(locale)
        val configuration = Configuration()
        configuration.setLocale(locale)
        context.resources.updateConfiguration(configuration, context.resources.displayMetrics)
    }

    fun toggleLanguage(context: Context) {
        val currentIndex = languages.indexOf(language.value)
        val nextIndex = (currentIndex + 1) % languages.size
        language.value = languages[nextIndex]

        saveLanguageState(context)

        val intent = Intent(context, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    private fun saveLanguageState(context: Context){
        val sharedPreferences = context.getSharedPreferences("preferences", Context.MODE_PRIVATE)
        sharedPreferences.edit().putString("language", language.value).apply()
    }

    fun onBackPressed( navController: NavController){
        navController.popBackStack()
    }


    fun logout(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            val sharedPreferences =
                context.getSharedPreferences("preferences", Context.MODE_PRIVATE)
            val token = sharedPreferences.getString("jwtToken", "")

            if (!token.isNullOrBlank()) {
                try {
                    sharedPreferences.edit().remove("jwtToken").apply()
                    val intent = Intent(context, MainActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(intent)

                } catch (e: JWTDecodeException) {
                    println("Erro ao decodificar o token: ${e.message}")
                }
            } else {
                println("Ainda não há token")
            }
        }
    }

    @SuppressLint("SuspiciousIndentation")
    fun deleteAccount(context: Context){
        val sharedPreferences = context.getSharedPreferences("preferences", Context.MODE_PRIVATE)
        token = sharedPreferences.getString("jwtToken", "").toString()
        val decodedJWT: DecodedJWT = JWT.decode(token)
        val userId = decodedJWT.subject
        isLoading = true
        viewModelScope.launch(Dispatchers.IO) {
            val status = deleteRepository.deleteUser(userId)
                viewModelScope.launch(Dispatchers.Main) {
                    deleteStatus.value = status
                    Toast.makeText(
                        context,
                        status,
                        Toast.LENGTH_SHORT
                    ).show()
                    withContext(Dispatchers.Main) {
                        isLoading = false
                    }
                    if (status == "Conta deletada com sucesso") {
                        viewModelScope.launch(Dispatchers.IO) {
                            sharedPreferences.edit().remove("jwtToken").apply()
                            val intent = Intent(context, MainActivity::class.java)
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                            context.startActivity(intent)
                        }
                    }
            }
        }
    }

    @SuppressLint("SuspiciousIndentation")
    fun desactivateAccount(context: Context){
        val sharedPreferences = context.getSharedPreferences("preferences", Context.MODE_PRIVATE)
        token = sharedPreferences.getString("jwtToken", "").toString()
        val decodedJWT: DecodedJWT = JWT.decode(token)
        val userId = decodedJWT.subject
        isLoading = true
        viewModelScope.launch(Dispatchers.IO) {
            val status = desactivateRepository.desactivateUser(userId)
                viewModelScope.launch(Dispatchers.Main) {
                    deleteStatus.value = status
                    Toast.makeText(
                        context,
                        status,
                        Toast.LENGTH_SHORT
                    ).show()
                    withContext(Dispatchers.Main) {
                        isLoading = false
                    }
                    if (status == "Conta desativada com sucesso") {
                        viewModelScope.launch(Dispatchers.IO) {
                            sharedPreferences.edit().remove("jwtToken").apply()
                            val intent = Intent(context, MainActivity::class.java)
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                            context.startActivity(intent)
                        }
                    }
            }
        }
    }
}
