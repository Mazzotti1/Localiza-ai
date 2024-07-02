package com.ecoheat.ui.screen

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ecoheat.R
import com.ecoheat.ui.ViewModel.AboutScreenViewModel
import com.ecoheat.ui.ViewModel.SettingsScreenViewModel
import com.ecoheat.ui.factory.SettingsScreenViewModelFactory
import com.ecoheat.ui.screen.ui.theme.EcoHeatTheme
import com.ecoheat.ui.util.ConfirmationDialog
import com.ecoheat.ui.util.LoadingIndicator
import com.ecoheat.ui.util.NavigationBar


class SettingsAcitivty : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val context = applicationContext
            val viewModel: SettingsScreenViewModel = viewModel(factory = SettingsScreenViewModelFactory(context))
            val aboutViewModel: AboutScreenViewModel = viewModel()

            val navController = rememberNavController()

            viewModel.loadLanguageState(this)
            viewModel.loadThemeState(this)

            val themeMode = viewModel.themeMode.value
            SettingsScreen(viewModel, navController, themeMode, context)

            NavHost(navController = navController, startDestination = "settings") {
                composable("settings") {
                    SettingsScreen(viewModel = viewModel, navController = navController, themeMode, context)
                }
                composable("about") {
                    AboutScreen(aboutViewModel, navController, themeMode, context)
                }
            }
        }
    }
}
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun SettingsScreen(
    viewModel: SettingsScreenViewModel,
    navController: NavController,
    themeMode: Boolean,
    context: Context
) {
    val language = viewModel.language.value
    val themeMode = viewModel.themeMode.value
    EcoHeatTheme(darkTheme = themeMode) {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Scaffold(
                topBar = {
                    TopBar(viewModel,navController)
                },
                bottomBar = {
                    NavigationBar(navController, context)
                }
            ) {
                SettingsContent(
                    viewModel = viewModel,
                    navController = navController,
                    themeMode = themeMode,
                    language = language,
                    context = context

                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(viewModel: SettingsScreenViewModel, navController : NavController) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
    CenterAlignedTopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.primary,
        ),
        title = {
            Text(
                text = stringResource(id = R.string.settings_title),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        navigationIcon = {
            IconButton(onClick = { viewModel.onBackPressed(navController) }) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = "Back"
                )
            }
        },
        scrollBehavior = scrollBehavior,
    )
}

@Composable
fun SettingsContent(
    viewModel: SettingsScreenViewModel,
    navController: NavController,
    themeMode: Boolean,
    language: String,
    context: Context
) {
    val focusManager = LocalFocusManager.current
    viewModel.getTokenProps(context)
    val hasToken = viewModel.hasToken.value

    Column (
        modifier = Modifier
            .fillMaxSize()
            .clickable { focusManager.clearFocus() },
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(56.dp))
        Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
        ){
            val logoDrawable = if (themeMode) R.drawable.logo_white else R.drawable.logo_black
            Image(
                    painter = painterResource(id = logoDrawable),
                    contentDescription = "Logo",
                    modifier = Modifier.size(150.dp)
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            TextButton(
                onClick = { navController.navigate("about") },
                modifier = Modifier
                    .padding(8.dp)
                    .width(IntrinsicSize.Max)
            ) {
                Text(text = stringResource(id = R.string.about_title),
                    fontSize = 16.sp,
                    style = MaterialTheme.typography.labelSmall)
            }
        }
        if(hasToken) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                TextButton(
                    onClick = { navController.navigate("main") },
                    modifier = Modifier
                        .padding(8.dp)
                        .width(IntrinsicSize.Max)
                ) {
                    Text(text = stringResource(id = R.string.change_password),
                        fontSize = 16.sp,
                        style = MaterialTheme.typography.labelSmall)
                }
            }
        }
        if(hasToken){
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                TextButton(
                    onClick = {
                        viewModel.isDialogLogoutOpen.value = true
                    },
                    modifier = Modifier
                        .padding(8.dp)
                        .width(IntrinsicSize.Max)
                ) {
                    Text(text = stringResource(id = R.string.logout),
                        fontSize = 16.sp,
                        color = Color.Red,
                        style = MaterialTheme.typography.labelSmall)
                }
            }
        }
        if(hasToken) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                TextButton(
                    onClick = { viewModel.isDialogDeleteOpen.value = true },
                    modifier = Modifier
                        .padding(8.dp)
                        .width(IntrinsicSize.Max)
                ) {
                    Text(
                        text = stringResource(id = R.string.delete_account),
                        fontSize = 16.sp,
                        color = Color.Red,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
        if(hasToken) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                TextButton(
                    onClick = { viewModel.isDialogDesactivateOpen.value = true},
                    modifier = Modifier
                        .padding(8.dp)
                        .width(IntrinsicSize.Max)
                ) {
                    Text(
                        text = stringResource(id = R.string.delete_desactivate),
                        fontSize = 16.sp,
                        color = Color.Red,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Button(
                onClick = { viewModel.toggleThemeMode(context) },
                modifier = Modifier.padding(16.dp)
            ) {
                val icon = if (themeMode) Icons.Filled.WbSunny else Icons.Filled.Nightlight
                Icon(
                    icon,
                    contentDescription = "Modo ${if (themeMode) "Claro" else "Escuro"}",
                    modifier = Modifier.size(28.dp)
                )
            }
            Button(
                onClick = { viewModel.toggleLanguage(context) },
                modifier = Modifier.padding(16.dp)
            ) {
                val icon = when (language) {
                    "en" -> R.drawable.brazil_flag
                    else -> R.drawable.usa_flag
                }

                Icon(
                    painter = painterResource(id = icon),
                    contentDescription = "Linguagem ${if (language == "pt") "Português" else "Inglês"}",
                    modifier = Modifier.size(28.dp),
                    tint = Color.Unspecified
                )
            }
            if (viewModel.isDialogLogoutOpen.value) {
                ConfirmationDialog(
                    onDismissRequest = { viewModel.isDialogLogoutOpen.value = false },
                    onConfirmation = { viewModel.logout(context) },
                    icon = Icons.Default.Warning,
                    title = stringResource(id = R.string.logout_title),
                    message = stringResource(id = R.string.logout_cofirm),
                    confirmButtonText = stringResource(id = R.string.logout_yes),
                    dismissButtonText = stringResource(id = R.string.logout_cancel)
                )
            }
            if (viewModel.isDialogDeleteOpen.value) {
                ConfirmationDialog(
                    onDismissRequest = { viewModel.isDialogDeleteOpen.value = false },
                    onConfirmation = { viewModel.deleteAccount(context) },
                    icon = Icons.Default.Warning,
                    title = stringResource(id = R.string.delete_title),
                    message = stringResource(id = R.string.delete_cofirm),
                    confirmButtonText = stringResource(id = R.string.delete_yes),
                    dismissButtonText = stringResource(id = R.string.delete_cancel)
                )
            }
            if (viewModel.isDialogDesactivateOpen.value) {
                ConfirmationDialog(
                    onDismissRequest = { viewModel.isDialogDesactivateOpen.value = false },
                    onConfirmation = { viewModel.desactivateAccount(context) },
                    icon = Icons.Default.Warning,
                    title = stringResource(id = R.string.desactive_title),
                    message = stringResource(id = R.string.desactive_cofirm),
                    confirmButtonText = stringResource(id = R.string.desactive_yes),
                    dismissButtonText = stringResource(id = R.string.desactive_cancel)
                )
            }
            if (viewModel.isLoading) {
                LoadingIndicator()
            }
        }
    }
}