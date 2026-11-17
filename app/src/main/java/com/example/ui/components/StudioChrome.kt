package com.example.ui.components

import android.os.Build
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.MovieCreation
import androidx.compose.material.icons.rounded.Output
import androidx.compose.material.icons.rounded.SpaceDashboard
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.StudioRoutes
import com.example.ui.theme.ObsidianOnSurface
import com.example.ui.theme.ObsidianOnSurfaceVariant
import com.example.ui.theme.ObsidianOutlineVariant
import com.example.ui.theme.ObsidianPrimary
import com.example.ui.theme.ObsidianSecondary
import com.example.ui.theme.ObsidianSurface
import com.example.ui.theme.ObsidianSurfaceContainer
import com.example.ui.theme.ObsidianSurfaceContainerHigh
import com.example.ui.theme.ObsidianSurfaceContainerLowest
import com.example.ui.theme.StudioTextStyles

data class StudioNavTab(
    val route: String,
    val label: String,
    val subtitleEn: String,
    val icon: ImageVector
)

val StudioNavTabs = listOf(
    StudioNavTab(StudioRoutes.HUB, "工作台", "Hub", Icons.Rounded.SpaceDashboard),
    StudioNavTab(StudioRoutes.EXTRACT, "转音频", "Extract", Icons.Rounded.Output),
    StudioNavTab(StudioRoutes.AUDIO, "音频编辑", "Audio", Icons.Rounded.GraphicEq),
    StudioNavTab(StudioRoutes.VIDEO, "视频剪辑", "Video", Icons.Rounded.MovieCreation)
)

@Composable
fun PulsingDot(
    color: Color = ObsidianPrimary,
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "pulse")
    val alpha by transition.animateFloat(
        initialValue = 1f,
        targetValue = 0.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(900),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )
    Box(
        modifier = modifier
            .size(6.dp)
            .alpha(alpha)
            .clip(CircleShape)
            .background(color)
    )
}

@Composable
fun StudioTopBar(
    currentRoute: String,
    canNavigateBack: Boolean,
    onBackClick: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activeTab = StudioNavTabs.firstOrNull { it.route == currentRoute } ?: StudioNavTabs.first()

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = ObsidianSurface.copy(alpha = 0.94f),
        shadowElevation = 6.dp
    ) {
        Column(modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(horizontal = if (canNavigateBack) 8.dp else 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    if (canNavigateBack) {
                        IconButton(
                            onClick = onBackClick,
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("top_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                contentDescription = stringResource(R.string.cd_back),
                                tint = ObsidianOnSurface,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Column(verticalArrangement = Arrangement.Center) {
                            Text(
                                text = activeTab.label,
                                style = StudioTextStyles.HeadlineSm.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = (-0.2).sp
                                ),
                                color = ObsidianOnSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                PulsingDot(color = ObsidianPrimary)
                                Text(
                                    text = "${activeTab.subtitleEn} · 本地处理",
                                    style = StudioTextStyles.LabelSm,
                                    color = ObsidianOnSurfaceVariant
                                )
                            }
                        }
                    } else {
                        Image(
                            painter = painterResource(R.drawable.ic_launcher_art),
                            contentDescription = stringResource(R.string.cd_logo),
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(6.dp))
                        )
                        Column(verticalArrangement = Arrangement.Center) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = stringResource(R.string.brand_title),
                                    style = StudioTextStyles.HeadlineSm.copy(
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = (-0.3).sp
                                    ),
                                    color = ObsidianOnSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(ObsidianSurfaceContainerLowest)
                                        .border(
                                            width = 1.dp,
                                            color = ObsidianPrimary.copy(alpha = 0.22f),
                                            shape = RoundedCornerShape(4.dp)
                                        )
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "LOSSLESS",
                                        style = StudioTextStyles.LabelSm.copy(
                                            fontSize = 9.sp,
                                            letterSpacing = 0.5.sp
                                        ),
                                        color = ObsidianPrimary,
                                        maxLines = 1
                                    )
                                }
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                PulsingDot(color = ObsidianPrimary)
                                Text(
                                    text = "工作台 · ${activeTab.subtitleEn}",
                                    style = StudioTextStyles.LabelSm,
                                    color = ObsidianOnSurfaceVariant
                                )
                            }
                        }
                    }
                }

                IconButton(
                    onClick = onSettingsClick,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("top_settings_button")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Tune,
                        contentDescription = stringResource(R.string.cd_settings),
                        tint = ObsidianOnSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            HorizontalDivider(color = ObsidianOutlineVariant.copy(alpha = 0.3f), thickness = 0.5.dp)
        }
    }
}

@Composable
fun EngineSettingsDialog(
    hardwareAccelName: String,
    exportsCount: Int,
    exportsMb: Float,
    onClearCache: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ObsidianSurfaceContainer,
        titleContentColor = ObsidianOnSurface,
        textContentColor = ObsidianOnSurfaceVariant,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Outlined.Tune, contentDescription = null, tint = ObsidianPrimary)
                Text("处理引擎信息", style = StudioTextStyles.HeadlineSm)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SettingSpecRow("设备型号", "${Build.MANUFACTURER} ${Build.MODEL}")
                SettingSpecRow("系统版本", "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})")
                SettingSpecRow("编码引擎", hardwareAccelName)
                SettingSpecRow("已导出文件", "$exportsCount 个 (${String.format(java.util.Locale.US, "%.1f", exportsMb)} MB)")
                SettingSpecRow("音频出口", "Music/ObsidianAV")
                SettingSpecRow("视频出口", "Movies/ObsidianAV")
            }
        },
        dismissButton = {
            TextButton(onClick = onClearCache) {
                Text("清理缓存", color = ObsidianSecondary, style = StudioTextStyles.LabelMd)
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("确认", color = ObsidianPrimary, style = StudioTextStyles.LabelMd)
            }
        }
    )
}

@Composable
private fun SettingSpecRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(ObsidianSurfaceContainerHigh)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = StudioTextStyles.BodySm, color = ObsidianOnSurfaceVariant)
        Text(value, style = StudioTextStyles.LabelSm, color = ObsidianPrimary)
    }
}
