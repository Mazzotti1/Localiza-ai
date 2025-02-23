package com.hatlas.ui.screen

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.hatlas.R
import com.hatlas.ui.ViewModel.RegisterScreenViewModel
import com.hatlas.ui.factory.RegisterScreenViewModelFactory
import com.hatlas.ui.screen.ui.theme.localizaaiTheme
import com.hatlas.ui.util.LoadingIndicator

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
                    label = {Text(text =stringResource(id = R.string.name_placeholder), fontSize = 16.sp, color = MaterialTheme.colorScheme.onPrimary)},
                    modifier = Modifier
                        .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                        .border(BorderStroke(2.dp, MaterialTheme.colorScheme.onPrimary), RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                        ),
                    textStyle = TextStyle(
                        color = MaterialTheme.colorScheme.onPrimary
                    )
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
                    label = {Text(text =stringResource(id = R.string.password_placeholder), fontSize = 16.sp, color = MaterialTheme.colorScheme.onPrimary)},
                    modifier = Modifier
                        .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                        .border(BorderStroke(2.dp, MaterialTheme.colorScheme.onPrimary), RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                    visualTransformation = PasswordVisualTransformation(),
                    textStyle = TextStyle(
                        color = MaterialTheme.colorScheme.onPrimary
                    )
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
                    label = {Text(text=stringResource(id = R.string.rePassword_placeholder), fontSize = 16.sp, color = MaterialTheme.colorScheme.onPrimary)},
                    modifier = Modifier
                        .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                        .border(BorderStroke(2.dp, MaterialTheme.colorScheme.onPrimary), RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                    visualTransformation = PasswordVisualTransformation(),
                    textStyle = TextStyle(
                        color = MaterialTheme.colorScheme.onPrimary
                    )

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