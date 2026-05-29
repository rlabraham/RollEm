package com.rltech.rollem.ui.game

import android.media.MediaPlayer
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import com.rltech.rollem.R
import kotlinx.coroutines.delay

@Composable
fun DiceRoller(
    modifier: Modifier = Modifier,
    onRollFinalized: (Int) -> Unit
) {
    var displayedValue by remember { mutableIntStateOf(1) }
    var isRolling by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val isPreview = LocalInspectionMode.current

    val mediaPlayer = remember(context, isPreview) {
        if (isPreview) null else MediaPlayer.create(context, R.raw.dice_roll)
    }

    DisposableEffect(mediaPlayer) {
        onDispose {
            mediaPlayer?.release()
        }
    }

    val rotation = remember { Animatable(0f) }
    val scale = remember { Animatable(1f) }

    LaunchedEffect(isRolling) {
        if (!isRolling) return@LaunchedEffect

        mediaPlayer?.seekTo(0)
        mediaPlayer?.start()

        // Gradual slowdown: short delays first, longer delays near the end.
        val frameDelaysMs = listOf(18L, 22L, 28L, 36L, 50L, 68L, 92L, 100L)
        val spinStep = 110f

        frameDelaysMs.forEachIndexed { index, frameDelay ->
            displayedValue = (1..6).random()

            rotation.animateTo(
                targetValue = rotation.value + spinStep,
                animationSpec = tween(
                    durationMillis = frameDelay.toInt(),
                    easing = FastOutSlowInEasing
                )
            )

            val targetScale = if (index % 2 == 0) 0.94f else 1.04f
            scale.animateTo(
                targetValue = targetScale,
                animationSpec = tween(durationMillis = (frameDelay * 0.8f).toInt())
            )

            delay(frameDelay / 3)
        }

        val finalValue = (1..6).random()
        displayedValue = finalValue

        onRollFinalized(finalValue)

        rotation.animateTo(
            targetValue = 0f,
            animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing)
        )
        scale.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing)
        )

        isRolling = false
    }

    val diceDrawable = when (displayedValue) {
        1 -> R.drawable.dice_1
        2 -> R.drawable.dice_2
        3 -> R.drawable.dice_3
        4 -> R.drawable.dice_4
        5 -> R.drawable.dice_5
        else -> R.drawable.dice_6
    }

    Column(
        modifier = modifier.clickable(enabled = !isRolling) {
            if (!isRolling) isRolling = true
        },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = diceDrawable),
            contentDescription = displayedValue.toString(),
            modifier = Modifier.graphicsLayer {
                rotationZ = rotation.value
                scaleX = scale.value
                scaleY = scale.value
            }
        )
    }
}