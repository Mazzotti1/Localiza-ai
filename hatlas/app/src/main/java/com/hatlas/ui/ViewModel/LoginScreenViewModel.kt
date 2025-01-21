package com.hatlas.ui.ViewModel

import android.content.Context
import android.content.res.Configuration
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.hatlas.Model.Login
import com.hatlas.R
import com.hatlas.data.repository.LoginRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.util.Locale

class LoginScreenViewModel(private val context: Context) : ViewModel() {
    private val repository = LoginRepository(context)
    val loginStatus = MutableLiveData<String?>()

    val themeMode = mutableStateOf(true)
    var name by mutableStateOf("")
    var password by mutableStateOf("")
    var isLoading by mutableStateOf(false)
    private var token = ""
    val language = mutableStateOf("pt")

    fun loadThemeState(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            val sharedPreferences =
                context.getSharedPreferences("preferences", Context.MODE_PRIVATE)
            themeMode.value = sharedPreferences.getBoolean("mode", true)
        }
    }

    fun onChangeName(newName: String){
        name = newName
    }

    fun onChangePassword(newPassword: String){
        password = newPassword
    }

    fun loadLanguageState(context: Context) {
        val sharedPreferences = context.getSharedPreferences("preferences", Context.MODE_PRIVATE)
        val savedLanguage = sharedPreferences.getString("language", "pt") ?: "pt"
        language.value = savedLanguage

        val locale = Locale(savedLanguage)
        Locale.setDefault(locale)
        val configuration = Configuration()
        configuration.setLocale(locale)
        context.resources.updateConfiguration(configuration, context.resources.displayMetrics)
    }

    fun login(context: Context, navController: NavController) {
        val resources = context.resources

        if (name.isBlank() || password.isBlank()) {
            Toast.makeText(
                context,
                resources.getString(R.string.fill_all_fields),
                Toast.LENGTH_SHORT
            ).show()
            isLoading = false
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val request = Login(name, password)
                val status = repository.loginUser(request)
                viewModelScope.launch(Dispatchers.Main) {
                    if (status.token != null) {
                        clearFields()
                        loginStatus.value = status.message
                        Toast.makeText(
                            context,
                            status.message,
                            Toast.LENGTH_SHORT
                        ).show()
                        isLoading = false

                        val sharedPreferences =
                            context.getSharedPreferences("preferences", Context.MODE_PRIVATE)
                        token = status.token
                        sharedPreferences.edit().putString("jwtToken", status.token).apply()

                        navController.navigate("menu") {
                            popUpTo(navController.graph.startDestinationId)
                        }
                    } else {
                        val errorBody = when (language.value) {
                            "en" -> "Credentials not found"
                            else -> "Credenciais não encontradas"
                        }
                        Toast.makeText(
                            context,
                            errorBody ?: "Erro desconhecido",
                            Toast.LENGTH_SHORT
                        ).show()
                        isLoading = false
                    }
                }
            } catch (e: HttpException) {
                val errorBody = e.response()?.errorBody()?.string()
                println("Erro HTTP: $errorBody")
                isLoading = false
                viewModelScope.launch(Dispatchers.Main) {
                    Toast.makeText(
                        context,
                        errorBody ?: "Erro de comunicação com o servidor",
                        Toast.LENGTH_SHORT
                    ).show()
                    isLoading = false
                }
            } catch (e: Throwable) {
                println("Erro servidor: ${e.message}")
                isLoading = false
                viewModelScope.launch(Dispatchers.Main) {
                    Toast.makeText(
                        context,
                        "Erro: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                    isLoading = false
                }
            }
        }
    }


    fun clearFields(){
        name = ""
        password = ""
        isLoading = false
    }

}


