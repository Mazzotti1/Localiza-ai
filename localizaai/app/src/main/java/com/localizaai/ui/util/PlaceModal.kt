import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.localizaai.Model.PlaceInfo
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun PlaceModal(onDismiss: () -> Unit, placeInfo: PlaceInfo) {
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .widthIn(max = 300.dp)
                .heightIn(max = 400.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(color = MaterialTheme.colorScheme.background)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                placeInfo.place?.let { place ->
                    Text(
                        text = place.name.ifEmpty { "Nome desconhecido" },
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Nota: ${if (place.rating != 0.0) place.rating else "Desconhecido"}",
                        style = MaterialTheme.typography.labelMedium,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "Popularidade: ",
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ){
                        ShowPopularityStars(formatPopularity(place.popularity))
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (place.hours_popular.isNotEmpty()) {
                        Text(text = "Horários populares:", style = MaterialTheme.typography.titleMedium)
                        place.hours_popular.forEach { hours ->
                            val dayOfWeek = when (hours.day) {
                                1 -> "Segunda-feira"
                                2 -> "Terça-feira"
                                3 -> "Quarta-feira"
                                4 -> "Quinta-feira"
                                5 -> "Sexta-feira"
                                6 -> "Sábado"
                                7 -> "Domingo"
                                else -> "Nenhum"
                            }
                            Text(text = "$dayOfWeek: ${formatTime(hours.open)} - ${formatTime(hours.close)}")
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    if (place.tips.isNotEmpty()) {
                        Text(text = "Comentários sobre o local",
                             style = MaterialTheme.typography.titleMedium,
                             textAlign = TextAlign.Center)
                        Spacer(modifier = Modifier.height(16.dp))
                        place.tips.forEach { tip ->
                            Text(text = tip.text,
                                 style = MaterialTheme.typography.labelMedium,
                                 textAlign = TextAlign.Center)
                            Spacer(modifier = Modifier.height(16.dp))
                            HorizontalDivider(
                                modifier = Modifier
                                    .padding(horizontal = 32.dp)
                                    .fillMaxWidth(),
                                thickness = 1.dp,
                                color = Color.Black
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                    Text(
                        text = "Telefone: ${place.tel.ifEmpty { "Desconhecido" }}",
                        style = MaterialTheme.typography.labelMedium,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Site: ${place.website.ifEmpty { "Desconhecido" }}",
                        style = MaterialTheme.typography.labelMedium,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold

                    )
                    Spacer(modifier = Modifier.height(8.dp))
                } ?: run {
                    Text(
                        text = "Informações do local não estão disponíveis",
                        style = MaterialTheme.typography.labelMedium,
                        textAlign = TextAlign.Center,

                    )
                }

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Black,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Fechar",
                    )
                }

            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
fun formatTime(time: String?): String {
    return if (time != null) {
        val formatter = DateTimeFormatter.ofPattern("HHmm")
        val parsedTime = LocalTime.parse(time, formatter)
        parsedTime.format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT))
    } else {
        "Desconhecido"
    }
}
fun formatPopularity(popularity: Double?): Int {
     if (popularity != null) {
        val stars = popularity * 5
         return stars.toInt()
    } else {
         return 0
    }
}
@Composable
fun ShowPopularityStars(stars: Int?) {
    val totalStars = 5
    val yellowStar: Painter = painterResource(id = com.localizaai.R.drawable.star_yellow)
    val greyStar: Painter = painterResource(id =com.localizaai.R.drawable.star_grey)

    for (i in 1..totalStars) {
        val starIcon = if (i <= (stars ?: 0)) yellowStar else greyStar
        Image(painter = starIcon, contentDescription = null, modifier = Modifier.size(24.dp))
    }
}

