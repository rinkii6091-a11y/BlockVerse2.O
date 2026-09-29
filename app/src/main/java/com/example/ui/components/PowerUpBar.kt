package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.UserProfileEntity
import com.example.model.PowerUpType
import com.example.theme.ThemeConfig

@Composable
fun PowerUpBar(
    userProfile: UserProfileEntity,
    activePowerUp: PowerUpType?,
    theme: ThemeConfig,
    onPowerUpSelected: (PowerUpType) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        PowerUpItem(
            type = PowerUpType.HAMMER,
            name = "Hammer",
            icon = Icons.Default.Build,
            count = userProfile.hammerCount,
            isActive = activePowerUp == PowerUpType.HAMMER,
            theme = theme,
            onClick = { onPowerUpSelected(PowerUpType.HAMMER) },
            testTag = "powerup_hammer"
        )

        PowerUpItem(
            type = PowerUpType.SHUFFLE,
            name = "Shuffle",
            icon = Icons.Default.Refresh,
            count = userProfile.shuffleCount,
            isActive = false,
            theme = theme,
            onClick = { onPowerUpSelected(PowerUpType.SHUFFLE) },
            testTag = "powerup_shuffle"
        )

        PowerUpItem(
            type = PowerUpType.UNDO,
            name = "Undo",
            icon = Icons.Default.Undo,
            count = userProfile.undoCount,
            isActive = false,
            theme = theme,
            onClick = { onPowerUpSelected(PowerUpType.UNDO) },
            testTag = "powerup_undo"
        )

        PowerUpItem(
            type = PowerUpType.BOMB,
            name = "Bomb",
            icon = Icons.Default.LocalFireDepartment,
            count = userProfile.bombCount,
            isActive = activePowerUp == PowerUpType.BOMB,
            theme = theme,
            onClick = { onPowerUpSelected(PowerUpType.BOMB) },
            testTag = "powerup_bomb"
        )

        PowerUpItem(
            type = PowerUpType.COLOR_BLAST,
            name = "Blast",
            icon = Icons.Default.Palette,
            count = userProfile.colorBlastCount,
            isActive = activePowerUp == PowerUpType.COLOR_BLAST,
            theme = theme,
            onClick = { onPowerUpSelected(PowerUpType.COLOR_BLAST) },
            testTag = "powerup_color_blast"
        )
    }
}

@Composable
fun PowerUpItem(
    type: PowerUpType,
    name: String,
    icon: ImageVector,
    count: Int,
    isActive: Boolean,
    theme: ThemeConfig,
    onClick: () -> Unit,
    testTag: String
) {
    val activeBorderColor = if (isActive) theme.secondaryAccent else Color(0x33FFFFFF)
    val activeBg = if (isActive) Color(0x55FF007A) else Color(0x331E293B)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Box(
            modifier = Modifier
                .minimumInteractiveComponentSize()
                .size(48.dp)
                .clip(CircleShape)
                .background(activeBg)
                .border(1.5.dp, activeBorderColor, CircleShape)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClick
                )
                .testTag(testTag),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = name,
                tint = if (count > 0) Color.White else Color(0x66FFFFFF),
                modifier = Modifier.size(22.dp)
            )

            // Stock Count badge
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(if (count > 0) theme.accentColor else Color(0xFF64748B)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = count.toString(),
                    color = Color.Black,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }

        Text(
            text = name,
            color = if (isActive) theme.secondaryAccent else Color(0xAAFFFFFF),
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
