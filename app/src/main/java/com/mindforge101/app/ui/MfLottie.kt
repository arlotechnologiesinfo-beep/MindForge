package com.mindforge101.app.ui

import androidx.annotation.RawRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition

/** Bundled (offline) Lottie animation. Under Reduce motion it shows one still frame instead of playing. */
@Composable
fun MfLottie(@RawRes res: Int, modifier: Modifier = Modifier, loop: Boolean = true, stillProgress: Float = 0.4f) {
    val composition = rememberLottieComposition(LottieCompositionSpec.RawRes(res))
    if (LocalReduceMotion.current) {
        LottieAnimation(composition.value, progress = { stillProgress }, modifier = modifier)
    } else {
        val progress = animateLottieCompositionAsState(
            composition.value, iterations = if (loop) LottieConstants.IterateForever else 1,
        )
        LottieAnimation(composition.value, progress = { progress.value }, modifier = modifier)
    }
}
