package com.localizaai.ui.util

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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
                placeholder = {
                    Text(text = stringResource(id = R.string.search), fontSize = 12.sp)
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = "Search Icon"
                    )
                },
                modifier = Modifier
                    .weight(1f)
                    .height(55.dp)
                    .border(0.8.dp, Color.Black, RoundedCornerShape(16.dp))
                    .focusRequester(focusRequester)
                    .onFocusChanged { focusState ->
                        onFocusChanged(focusState.isFocused)
                        viewModel.showSearchListItens.value = false
                    },
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
                    cursorColor = Color.Black,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
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
                                tint = Color.Black
                            )
                        }
                    }
                },
                shape = RoundedCornerShape(16.dp)
            )
        }
    }
}

@Composable
fun SearchResultList(context : Context, autocomplete: SuggestionResponse?, autocompletePlaces: Autocomplete?, viewModel: MenuScreenViewModel) {
    val itemsGeneral = autocomplete?.suggestions ?: emptyList()
    val itemsPlaces = autocompletePlaces?.results?: emptyList()
    val displayedNames = mutableSetOf<String>()

    LazyColumn(
        modifier = Modifier
            .widthIn(max = 400.dp)
            .heightIn(max = 200.dp)
            .background(Color.White)
    ) {
        items(itemsGeneral) {itemGeneral ->
            SearchResultItem(context, itemGeneral, viewModel)
        }
        items(itemsPlaces) { itemPlace ->
            if (displayedNames.add(itemPlace.text.primary!!)) {
                SearchResultItemPlace(context, itemPlace, viewModel)
            }
        }
    }
}

@Composable
fun SearchResultItem(context : Context, item: Suggestion, viewModel: MenuScreenViewModel) {

    val itemName = item.name
    Card(
        shape = RoundedCornerShape(0.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {
                viewModel.getMapBoxSelectedData(context, itemName)
            }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Filled.SouthAmerica,
                contentDescription = "Sinalization Icon",
                tint = Color.Blue,
                modifier = Modifier
                    .padding(start = 10.dp)
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

@Composable
fun SearchResultItemPlace(context : Context, item: AutocompleteResult, viewModel: MenuScreenViewModel) {

    val lat = item.place?.geocodes?.main?.latitude?.toString() ?: ""
    val long = item.place?.geocodes?.main?.longitude?.toString() ?: ""

    val itemName = item.place?.name
    Card(
        shape = RoundedCornerShape(0.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {
                if (itemName != null) {
                    viewModel.onSelectSearchListItem(context, lat, long, itemName)
                }
            }
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

