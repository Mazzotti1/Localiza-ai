package com.localizaai.ui.util

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ControlPoint
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.FlagCircle
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SouthAmerica
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.localizaai.Model.Autocomplete
import com.localizaai.Model.AutocompleteResult
import com.localizaai.Model.Suggestion
import com.localizaai.Model.SuggestionResponse
import com.localizaai.R
import com.localizaai.ui.ViewModel.MenuScreenViewModel


@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun SearchBarMain(
        viewModel: MenuScreenViewModel,
        onSearch: (String) -> Unit,
        isFocused: Boolean,
        onFocusChanged: (Boolean) -> Unit
) {
    var text by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    LaunchedEffect(isFocused) {
        if (isFocused) {
            focusRequester.requestFocus()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {
                focusManager.clearFocus()
            }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            TextField(
                value = text,
                onValueChange = { newText ->
                    viewModel.showSearchListItens.value = true
                    text = newText
                    onSearch(newText)
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = "Search Icon",
                        modifier = Modifier.size(20.dp)
                    )
                },
                modifier = Modifier
                    .weight(1f)
                    .height(45.dp)
                    .focusRequester(focusRequester)
                    .onFocusChanged { focusState ->
                        onFocusChanged(focusState.isFocused)
                        viewModel.showSearchListItens.value = false
                    }
                    .border(
                        width = 1.dp,
                        color = if (isFocused) MaterialTheme.colorScheme.primary else Color.Gray,
                        shape = RoundedCornerShape(8.dp)
                    ),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Search
                ),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        onSearch(text)
                        focusManager.clearFocus()
                    }
                ),
                colors = TextFieldDefaults.textFieldColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    cursorColor = MaterialTheme.colorScheme.inversePrimary,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor = MaterialTheme.colorScheme.onPrimary,
                    unfocusedTextColor = MaterialTheme.colorScheme.onPrimary,
                ),
                textStyle = TextStyle(fontSize = 12.sp),
                trailingIcon = {
                    if (text.isNotEmpty()) {
                        IconButton(onClick = {
                            text = ""
                            focusManager.clearFocus()
                            viewModel.showSearchListItens.value = false
                        }) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "Clear",
                                tint = MaterialTheme.colorScheme.inversePrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                },
                shape = RoundedCornerShape(8.dp),
            )
        }
    }

}

@Composable
fun SearchResultList(context : Context, autocomplete: SuggestionResponse?, autocompletePlaces: Autocomplete?, viewModel: MenuScreenViewModel) {
    val itemsGeneral = autocomplete?.suggestions ?: emptyList()
    val itemsPlaces = autocompletePlaces?.results?: emptyList()
    val displayedNames = mutableSetOf<String>()

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 200.dp)
            .zIndex(10f)
    ) {
        LazyColumn(
            modifier = Modifier
                .widthIn(max = 355.dp)
                .background(Color.White)
                .zIndex(10f)
        ) {
            items(itemsGeneral) { itemGeneral ->
                SearchResultItem(context, itemGeneral, viewModel)
            }
            items(itemsPlaces) { itemPlace ->
                if (displayedNames.add(itemPlace.text.primary!!)) {
                    SearchResultItemPlace(context, itemPlace, viewModel)
                }
            }
        }
    }
}

@SuppressLint("NewApi")
@Composable
fun SearchResultItem(context : Context, item: Suggestion, viewModel: MenuScreenViewModel) {

    val itemName = item.name
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.inversePrimary
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 1.dp)
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {
                viewModel.getMapBoxSelectedData(context, itemName)
            }
            .zIndex(10f)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Filled.SouthAmerica,
                contentDescription = "Sinalization Icon",
                tint = Color.White,
                modifier = Modifier
                    .padding(start = 10.dp)
                    .align(alignment = Alignment.CenterVertically)
            )
            Text(
                text = item.name ?: "",
                modifier = Modifier
                    .padding(16.dp),
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

@SuppressLint("NewApi")
@Composable
fun SearchResultItemPlace(context : Context, item: AutocompleteResult, viewModel: MenuScreenViewModel) {

    val lat = item.place?.geocodes?.main?.latitude?.toString() ?: ""
    val long = item.place?.geocodes?.main?.longitude?.toString() ?: ""

    val itemName = item.place?.name
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.inversePrimary
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 1.dp)
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {
                if (itemName != null) {
                    viewModel.onSelectSearchListItem(context, lat, long, itemName)
                }
            }
            .zIndex(10f)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ){
            Icon(
                imageVector = Icons.Filled.FlagCircle,
                contentDescription = "Sinalization Icon",
                tint = Color.Green,
                modifier = Modifier
                    .padding(start = 10.dp)
            )
            Text(
                text = item.text.primary ?: "" ,
                modifier = Modifier
                    .padding(16.dp),
                style = MaterialTheme.typography.labelLarge
            )
        }

    }
}

fun performSearch(query: String, viewModel:MenuScreenViewModel) {
    viewModel.getMapBoxAutocompletes(query)
    viewModel.onSearch(query)
}

