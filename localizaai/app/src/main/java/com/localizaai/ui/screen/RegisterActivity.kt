package com.localizaai.ui.screen

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.localizaai.R
import com.localizaai.ui.ViewModel.RegisterScreenViewModel
import com.localizaai.ui.factory.RegisterScreenViewModelFactory
import com.localizaai.ui.screen.ui.theme.localizaaiTheme
import com.localizaai.ui.util.LoadingIndicator

class RegisterActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val context = applicationContext;
            val viewModel: RegisterScreenViewModel = viewModel(factory = RegisterScreenViewModelFactory(context))
            val navController = rememberNavController()
            viewModel.clearFields()
            viewModel.loadThemeState(this)
            viewModel.loadLanguageState(this)
            val themeMode = viewModel.themeMode.value
            RegisterScreen(viewModel,navController, themeMode, context)

        }
    }
}

@Composable
fun RegisterScreen(
    viewModel: RegisterScreenViewModel,
    navController: NavController,
    themeMode: Boolean,
    context: Context
) {
    localizaaiTheme(darkTheme = themeMode) {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            RegisterContent(
                viewModel = viewModel,
                navController = navController,
                context = context,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("ClickableViewAccessibility")
@Composable
fun RegisterContent(
    viewModel: RegisterScreenViewModel,
    navController: NavController,
    context: Context
) {

    DisposableEffect(Unit) {
        viewModel.clearFields()
        viewModel.loadLanguageState(context)
        onDispose { }
    }

    val focusManager = LocalFocusManager.current

    Column (
        modifier = Modifier.fillMaxSize()
            .clickable { focusManager.clearFocus() },
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Column(
            horizontalAlignment = Alignment.Start
        ){
            Text(
                text = stringResource(id = R.string.name_title),
                fontSize = 20.sp,
                modifier = Modifier.padding(bottom = 2.dp, start = 10.dp),
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                TextField(
                    value = viewModel.name,
                    onValueChange = { viewModel.onChangeName(it) },
                    label = {Text(text =stringResource(id = R.string.name_placeholder), fontSize = 16.sp)},
                    modifier = Modifier
                        .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                        .border(BorderStroke(2.dp, MaterialTheme.colorScheme.primary), RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                        ),
                )
            }
        }
        Column(
            horizontalAlignment = Alignment.Start
        ){
            Text(
                text = stringResource(id = R.string.password_title),
                fontSize = 20.sp,
                modifier = Modifier.padding(bottom = 2.dp, top = 20.dp, start = 10.dp)
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                TextField(
                    value = viewModel.password,
                    onValueChange = { viewModel.onChangePassword(it) },
                    label = {Text(text =stringResource(id = R.string.password_placeholder), fontSize = 16.sp)},
                    modifier = Modifier
                        .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                        .border(BorderStroke(2.dp, MaterialTheme.colorScheme.primary), RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                    visualTransformation = PasswordVisualTransformation()
                )
            }
        }
        Column(
            horizontalAlignment = Alignment.Start,
            modifier = Modifier.padding(bottom = 15.dp)
        ){
            Text(
                text = stringResource(id = R.string.repassword_title),
                fontSize = 20.sp,
                modifier = Modifier.padding(bottom = 2.dp, top = 20.dp, start = 10.dp)
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                TextField(
                    value = viewModel.rePassword,
                    onValueChange = { viewModel.onChangeRePassword(it) },
                    label = {Text(text=stringResource(id = R.string.rePassword_placeholder), fontSize = 16.sp)},
                    modifier = Modifier
                        .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                        .border(BorderStroke(2.dp, MaterialTheme.colorScheme.primary), RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                    visualTransformation = PasswordVisualTransformation()

                )
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (viewModel.isLoading) {
                LoadingIndicator()
            } else {
                Button(
                    onClick = {
                        viewModel.isLoading = true
                        viewModel.register(context, navController)
                    },
                    colors = ButtonDefaults.buttonColors(colorResource(id = R.color.DarkModeHighlight)),
                    modifier = Modifier.padding(16.dp)
                        .width(200.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.btn_register),
                        fontSize = 24.sp,
                        color = colorResource(id = R.color.off_white)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(30.dp))
        Text(
            text = stringResource(id = R.string.has_account),
            fontSize = 16.sp,
            modifier = Modifier.padding(bottom = 10.dp)
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Button(
                onClick = { navController.navigate("login") },
                colors = ButtonDefaults.buttonColors(colorResource(id = R.color.DarkModeHighlight)),
                modifier = Modifier.width(200.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.btn_login),
                    fontSize = 24.sp,
                    color = Color.White
                )
            }
        }
    }
}