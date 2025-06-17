package com.uhufor.inspector.ui.compose

import androidx.compose.animation.core.AnimationState
import androidx.compose.animation.core.DecayAnimationSpec
import androidx.compose.animation.core.animateDecay
import androidx.compose.animation.core.exponentialDecay
import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.foundation.gestures.ScrollScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlin.math.absoluteValue

@Composable
fun rememberLowDecayFling(
    friction: Float = 0.30f,
): FlingBehavior {
    val decay: DecayAnimationSpec<Float> = remember(friction) {
        exponentialDecay(frictionMultiplier = friction)
    }

    return remember(decay) {
        object : FlingBehavior {

            override suspend fun ScrollScope.performFling(initialVelocity: Float): Float {
                val anim = AnimationState(initialValue = 0f, initialVelocity = initialVelocity)

                var lastValue = 0f
                var remainingVelocity = initialVelocity
                anim.animateDecay(decay) {
                    val delta = value - lastValue
                    lastValue = value

                    val consumed = scrollBy(delta)
                    val unconsumed = delta - consumed

                    remainingVelocity = velocity

                    if (unconsumed.absoluteValue > 0.5f) {
                        cancelAnimation()
                    }
                }
                return remainingVelocity
            }
        }
    }
}
