package com.londontubeai.navigator.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.londontubeai.navigator.ui.theme.StatusGood
import com.londontubeai.navigator.ui.theme.TubePrimary

@Composable
fun CarriageVisualizer(
    totalCarriages: Int,
    recommendedCarriage: Int,
    lineColor: Color = TubePrimary,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Direction indicator
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = "Front",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = "Rear",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Carriage boxes
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            for (i in 1..totalCarriages) {
                val isRecommended = i == recommendedCarriage
                val bgColor by animateColorAsState(
                    targetValue = if (isRecommended) StatusGood else lineColor.copy(alpha = 0.12f),
                    animationSpec = tween(500),
                    label = "carriageBg$i",
                )
                val borderColor by animateColorAsState(
                    targetValue = if (isRecommended) StatusGood else lineColor.copy(alpha = 0.3f),
                    animationSpec = tween(500),
                    label = "carriageBorder$i",
                )
                val scale by animateFloatAsState(
                    targetValue = if (isRecommended) 1.08f else 1f,
                    animationSpec = tween(400),
                    label = "carriageScale$i",
                )

                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    if (isRecommended) {
                        Icon(
                            imageVector = Icons.Filled.ArrowDropDown,
                            contentDescription = "Recommended",
                            tint = StatusGood,
                            modifier = Modifier.height(18.dp),
                        )
                    } else {
                        Spacer(modifier = Modifier.height(18.dp))
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp)
                            .scale(scale)
                            .clip(RoundedCornerShape(6.dp))
                            .background(bgColor)
                            .border(
                                width = if (isRecommended) 2.dp else 1.dp,
                                color = borderColor,
                                shape = RoundedCornerShape(6.dp),
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "$i",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (isRecommended) FontWeight.Bold else FontWeight.Normal,
                            color = if (isRecommended) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center,
                            fontSize = 12.sp,
                        )
                    }

                    if (isRecommended) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Board here",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = StatusGood,
                            fontSize = 9.sp,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
        }
    }
}
