import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun PlaceModalLoading() {
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
            ShimmerEffect()
            Spacer(modifier = Modifier.height(8.dp))
            ShimmerEffect()
            Spacer(modifier = Modifier.height(8.dp))
            ShimmerEffect()
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                ShowPopularityStars(0)
            }
            Spacer(modifier = Modifier.height(16.dp))
            ShimmerEffect()
            Spacer(modifier = Modifier.height(8.dp))
            ShimmerEffect()
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(
                modifier = Modifier
                    .padding(horizontal = 32.dp)
                    .fillMaxWidth(),
                thickness = 1.dp,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(16.dp))
            ShimmerEffect()
            Spacer(modifier = Modifier.height(8.dp))
            ShimmerEffect()
            Spacer(modifier = Modifier.height(16.dp))
            ShimmerEffect()
            Spacer(modifier = Modifier.height(8.dp))
            ShimmerEffect()
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(
                modifier = Modifier
                    .padding(horizontal = 32.dp)
                    .fillMaxWidth(),
                thickness = 1.dp,
                color = Color.Black
            )
        }
    }
}
@Composable
fun ShimmerEffect() {
    val transition = rememberInfiniteTransition()
    val color by transition.animateColor(
        initialValue = Color.Gray.copy(alpha = 0.2f),
        targetValue = Color.Gray.copy(alpha = 0.6f),
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = ""
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(24.dp)
            .background(color = color, shape = RoundedCornerShape(4.dp))
    )
}


