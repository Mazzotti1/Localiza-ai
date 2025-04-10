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
import com.hatlas.Model.Register
import com.hatlas.R
import com.hatlas.data.repository.RegisterRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class RegisterScreenViewModel(private val context: Context) : ViewModel() {
    private val repository = RegisterRepository(context)
    val registerStatus = MutableLiveData<String>()

    val themeMode = mutableStateOf(true)
    var name by mutableStateOf("")
    var password by mutableStateOf("")
    var rePassword by mutableStateOf("")
    var isLoading by mutableStateOf((false))
    val language = mutableStateOf("pt")
    fun loadThemeState(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            val sharedPreferences = context.getSharedPreferences("preferences", Context.MODE_PRIVATE)
            themeMode.value = sharedPreferences.getBoolean("mode", true)
        }
    }

    fun onChangeName(newName: String){
        name = newName
    }

    fun onChangePassword(newPassword: String){
        password = newPassword
    }

    fun onChangeRePassword(newRePassword: String){
        rePassword = newRePassword
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

    fun register(context: Context, navController: NavController) {
        val resources = context.resources
        val regex = Regex("^[a-zA-Z0-9]+$")
        if (name.isBlank() || password.isBlank() || rePassword.isBlank()) {
            Toast.makeText(
                context,
                resources.getString(R.string.fill_all_fields),
                Toast.LENGTH_SHORT
            ).show()
            isLoading = false
            return
        }

        if (password != rePassword) {
            Toast.makeText(
                context,
                resources.getString(R.string.passwords_match),
                Toast.LENGTH_SHORT
            ).show()
            isLoading = false
            return
        }

        if (!regex.matches(name)) {
            Toast.makeText(
                context,
                resources.getString(R.string.invalid_nickname),
                Toast.LENGTH_SHORT
            ).show()
            isLoading = false
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            val request = Register(name, password)
            val status = repository.registerUser(request)
            if (status.isNotEmpty()) {
                viewModelScope.launch(Dispatchers.Main) {
                    clearFields()
                    registerStatus.value = status
                    Toast.makeText(
                        context,
                        status,
                        Toast.LENGTH_SHORT
                    ).show()
                    withContext(Dispatchers.Main) {
                        isLoading = false
                    }
                    if (status == "Conta criada com sucesso!") {
                        navController.navigate("login") {
                            popUpTo(navController.graph.startDestinationId)
                        }
                    }

                }
            }
        }
    }

    fun clearFields(){
        name = ""
        password = ""
        rePassword = ""
    }
}
