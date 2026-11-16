package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.AudioFile
import androidx.compose.material.icons.rounded.ContentCut
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.IosShare
import androidx.compose.material.icons.rounded.Movie
import androidx.compose.material.icons.rounded.MovieCreation
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.PipelineKind
import com.example.model.PipelineTaskItem
import com.example.model.StudioRoutes
import com.example.ui.components.PulsingDot
import com.example.ui.theme.ObsidianOnSurface
import com.example.ui.theme.ObsidianOnSurfaceVariant
import com.example.ui.theme.ObsidianPrimary
import com.example.ui.theme.ObsidianSecondary
import com.example.ui.theme.ObsidianSurfaceContainer
import com.example.ui.theme.ObsidianSurfaceContainerHigh
import com.example.ui.theme.ObsidianSurfaceContainerHighest
import com.example.ui.theme.ObsidianSurfaceContainerLow
import com.example.ui.theme.ObsidianTertiary
import com.example.ui.theme.StudioTextStyles
import com.example.viewmodel.HubUiState
import java.util.Locale

@Composable
fun HubScreen(
    uiState: HubUiState,
    onNavigate: (String) -> Unit,
    onPickFromGallery: () -> Unit,
    onPickAudioFile: () -> Unit,
    onPickMediaFile: () -> Unit,
    onClearCompletedQueue: () -> Unit,
    onToggleQueuePlay: (String) -> Unit,
    onShareQueueItem: (PipelineTaskItem) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 720.dp),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 1. REAL STORAGE & HARDWARE TELEMETRY
            item {
                EngineStatusTelemetryCard(uiState = uiState)
            }

            // 2. CORE FEATURE MODULES
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "核心工作流",
                            style = StudioTextStyles.LabelSm,
                            color = ObsidianOnSurfaceVariant
                        )
                        Text(
                            text = "LOCAL ENGINE",
                            style = StudioTextStyles.LabelSm,
                            color = ObsidianPrimary
                        )
                    }

                    EngineCoreCard(
                        title = "视频提取音频",
                        coreTag = "EXTRACT",
                        description = "支持提取 M4A / WAV / MP4 音轨并支持多轨探测",
                        ctaLabel = "进入管道",
                        accentColor = ObsidianPrimary,
                        icon = Icons.Rounded.GraphicEq,
                        testTag = "hub_card_extract",
                        onClick = { onNavigate(StudioRoutes.EXTRACT) },
                        graphic = {
                            WaveformSparklineGraphic(accentColor = ObsidianPrimary)
                        }
                    )

                    EngineCoreCard(
                        title = "视频剪辑与处理",
                        coreTag = "VIDEO",
                        description = "精确关键帧时间轴裁剪、无重编码直通导出",
                        ctaLabel = "进入剪辑台",
                        accentColor = ObsidianSecondary,
                        icon = Icons.Rounded.MovieCreation,
                        testTag = "hub_card_video",
                        onClick = { onNavigate(StudioRoutes.VIDEO) },
                        graphic = {
                            FrameStripGraphic(accentColor = ObsidianSecondary)
                        }
                    )

                    EngineCoreCard(
                        title = "音频深度编辑",
                        coreTag = "AUDIO",
                        description = "真实波形裁剪、淡入淡出、增益调节与标签预览",
                        ctaLabel = "打开工程室",
                        accentColor = ObsidianTertiary,
                        icon = Icons.Rounded.Tune,
                        testTag = "hub_card_audio",
                        onClick = { onNavigate(StudioRoutes.AUDIO) },
                        graphic = {
                            EqSlidersGraphic(accentColor = ObsidianTertiary)
                        }
                    )
                }
            }

            // 3. QUICK INGESTION SUITE (100% Real System Pickers)
            item {
                QuickIngestionSuite(
                    onPickFromGallery = onPickFromGallery,
                    onPickAudioFile = onPickAudioFile,
                    onPickMediaFile = onPickMediaFile
                )
            }

            // 4. REAL EXPORTED FILES QUEUE
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "导出记录 (${uiState.pipelineQueue.size})",
                        style = StudioTextStyles.LabelSm,
                        color = ObsidianOnSurfaceVariant
                    )
                    if (uiState.pipelineQueue.isNotEmpty()) {
                        Text(
                            text = "清空全部",
                            style = StudioTextStyles.LabelSm,
                            color = ObsidianOnSurfaceVariant,
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .clickable(onClick = onClearCompletedQueue)
                                .padding(horizontal = 6.dp, vertical = 4.dp)
                                .testTag("clear_queue_button")
                        )
                    }
                }
            }

            if (uiState.pipelineQueue.isEmpty()) {
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = ObsidianSurfaceContainerLow,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.GraphicEq,
                                contentDescription = null,
                                tint = ObsidianPrimary.copy(alpha = 0.6f),
                                modifier = Modifier.size(28.dp)
                            )
                            Text(
                                text = "暂无导出记录",
                                style = StudioTextStyles.BodyMd,
                                color = ObsidianOnSurface
                            )
                            Text(
                                text = "从上方选择视频或音频素材进行处理，导出的文件会保存在这里并同步到系统媒体库",
                                style = StudioTextStyles.BodySm,
                                color = ObsidianOnSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(uiState.pipelineQueue, key = { it.id }) { item ->
                    PipelineQueueRow(
                        item = item,
                        onTogglePlay = { onToggleQueuePlay(item.id) },
                        onShare = { onShareQueueItem(item) }
                    )
                }
            }

            item { Spacer(Modifier.height(12.dp)) }
        }
    }
}

@Composable
private fun EngineStatusTelemetryCard(uiState: HubUiState) {
    val totalSafe = uiState.totalGb.coerceAtLeast(1f)
    val usedFraction = (uiState.usedGb / totalSafe).coerceIn(0.02f, 0.96f)
    val availFraction = (1f - usedFraction).coerceAtLeast(0.02f)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = ObsidianSurfaceContainerLow,
        shape = RoundedCornerShape(12.dp),
        shadowElevation = 4.dp
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    PulsingDot(color = ObsidianPrimary)
                    Text(
                        text = uiState.hardwareAccelName,
                        style = StudioTextStyles.LabelSm,
                        color = ObsidianOnSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(ObsidianPrimary.copy(alpha = 0.12f))
                        .padding(horizontal = 6.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "ON-DEVICE",
                        style = StudioTextStyles.LabelSm,
                        color = ObsidianPrimary
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = "设备可用存储",
                        style = StudioTextStyles.LabelSm,
                        color = ObsidianOnSurfaceVariant
                    )
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = String.format(Locale.US, "%.1f", uiState.availableGb),
                            style = StudioTextStyles.MetricDisplay,
                            color = ObsidianPrimary
                        )
                        Text(
                            text = String.format(Locale.US, "/ %.1f GB AVAIL", uiState.totalGb),
                            style = StudioTextStyles.LabelSm,
                            color = ObsidianOnSurfaceVariant,
                            modifier = Modifier.padding(bottom = 2.dp)
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(CircleShape)
                        .background(ObsidianSurfaceContainerHighest)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(usedFraction)
                            .background(ObsidianPrimary)
                    )
                    Spacer(modifier = Modifier.weight(availFraction))
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(ObsidianPrimary)
                        )
                        Text(
                            text = String.format(Locale.US, "工作区导出 %.1f MB", uiState.studioExportsMb),
                            style = StudioTextStyles.LabelSm,
                            color = ObsidianOnSurfaceVariant
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(ObsidianSecondary)
                        )
                        Text(
                            text = String.format(Locale.US, "临时缓存 %.1f MB", uiState.cacheMb),
                            style = StudioTextStyles.LabelSm,
                            color = ObsidianOnSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EngineCoreCard(
    title: String,
    coreTag: String,
    description: String,
    ctaLabel: String,
    accentColor: Color,
    icon: ImageVector,
    testTag: String,
    onClick: () -> Unit,
    graphic: @Composable () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .testTag(testTag),
        color = ObsidianSurfaceContainer,
        shape = RoundedCornerShape(12.dp),
        shadowElevation = 4.dp
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Transparent,
                                accentColor.copy(alpha = 0.08f)
                            )
                        )
                    )
            )
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = title,
                                style = StudioTextStyles.HeadlineSm.copy(fontWeight = FontWeight.SemiBold),
                                color = ObsidianOnSurface
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(accentColor.copy(alpha = 0.15f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = coreTag,
                                    style = StudioTextStyles.LabelSm,
                                    color = accentColor
                                )
                            }
                        }
                        Text(
                            text = description,
                            style = StudioTextStyles.BodySm,
                            color = ObsidianOnSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(ObsidianSurfaceContainerHigh),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = title,
                            tint = accentColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        graphic()
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Text(
                            text = ctaLabel,
                            style = StudioTextStyles.LabelSm,
                            color = accentColor
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WaveformSparklineGraphic(accentColor: Color) {
    val heights = listOf(8, 16, 24, 12, 20, 24, 16, 8, 12, 4, 16, 8, 20)
    Row(
        modifier = Modifier.height(24.dp),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        heights.forEachIndexed { idx, h ->
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(h.dp)
                    .clip(RoundedCornerShape(topStart = 2.dp, topEnd = 2.dp))
                    .background(
                        if (idx < 8) accentColor.copy(alpha = 0.78f)
                        else ObsidianSurfaceContainerHighest
                    )
            )
        }
    }
}

@Composable
private fun FrameStripGraphic(accentColor: Color) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val alphas = listOf(0f, 0.30f, 0.50f, 0f)
        alphas.forEachIndexed { idx, a ->
            Box(
                modifier = Modifier
                    .width(28.dp)
                    .height(16.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(
                        if (a > 0f) accentColor.copy(alpha = a)
                        else ObsidianSurfaceContainerHighest
                    ),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(if (idx < 3) accentColor else ObsidianOnSurfaceVariant)
                )
            }
        }
    }
}

@Composable
private fun EqSlidersGraphic(accentColor: Color) {
    val offsets = listOf(14.dp, 28.dp, 8.dp)
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        offsets.forEach { offset ->
            Box(
                modifier = Modifier
                    .width(48.dp)
                    .height(20.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(ObsidianSurfaceContainerHighest),
                contentAlignment = Alignment.CenterStart
            ) {
                Box(
                    modifier = Modifier
                        .padding(start = offset)
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(accentColor)
                )
            }
        }
    }
}

@Composable
private fun QuickIngestionSuite(
    onPickFromGallery: () -> Unit,
    onPickAudioFile: () -> Unit,
    onPickMediaFile: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "快速载入素材",
                style = StudioTextStyles.LabelSm,
                color = ObsidianOnSurfaceVariant
            )
            Text(
                text = "ZERO-PERMISSION PICKER",
                style = StudioTextStyles.LabelSm,
                color = ObsidianSecondary
            )
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = ObsidianSurfaceContainerLow,
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    IngestActionTile(
                        title = "相册视频",
                        subtitle = "MP4 / MOV",
                        icon = Icons.Rounded.PhotoLibrary,
                        accentColor = ObsidianPrimary,
                        testTag = "ingest_gallery_button",
                        onClick = onPickFromGallery,
                        modifier = Modifier.weight(1f)
                    )
                    IngestActionTile(
                        title = "音频文件",
                        subtitle = "M4A / MP3 / WAV",
                        icon = Icons.Rounded.AudioFile,
                        accentColor = ObsidianTertiary,
                        testTag = "ingest_audio_button",
                        onClick = onPickAudioFile,
                        modifier = Modifier.weight(1f)
                    )
                    IngestActionTile(
                        title = "浏览文件",
                        subtitle = "全格式音视频",
                        icon = Icons.Rounded.FolderOpen,
                        accentColor = ObsidianSecondary,
                        testTag = "ingest_files_button",
                        onClick = onPickMediaFile,
                        modifier = Modifier.weight(1f)
                    )
                }

                Text(
                    text = "使用系统安全文件选择器载入媒体，所有剪辑与转码均在本地完成",
                    style = StudioTextStyles.LabelSm,
                    color = ObsidianOnSurfaceVariant.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun IngestActionTile(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    testTag: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(ObsidianSurfaceContainer)
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 4.dp)
            .testTag(testTag)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = accentColor,
                modifier = Modifier.size(22.dp)
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = title,
                style = StudioTextStyles.LabelMd,
                color = ObsidianOnSurface,
                maxLines = 1
            )
            Text(
                text = subtitle,
                style = StudioTextStyles.LabelSm.copy(fontSize = 9.sp),
                color = ObsidianOnSurfaceVariant,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun PipelineQueueRow(
    item: PipelineTaskItem,
    onTogglePlay: () -> Unit,
    onShare: () -> Unit
) {
    val accent = when (item.kind) {
        PipelineKind.AUDIO_EXTRACT -> ObsidianPrimary
        PipelineKind.AUDIO_MASTER -> ObsidianSecondary
        PipelineKind.VIDEO_CUT -> ObsidianTertiary
    }
    val icon = when (item.kind) {
        PipelineKind.AUDIO_EXTRACT -> Icons.Rounded.MusicNote
        PipelineKind.AUDIO_MASTER -> Icons.Rounded.Movie
        PipelineKind.VIDEO_CUT -> Icons.Rounded.ContentCut
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = ObsidianSurfaceContainer,
        shape = RoundedCornerShape(12.dp),
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(ObsidianSurfaceContainerHighest),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = item.fileName,
                    tint = accent,
                    modifier = Modifier.size(20.dp)
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = item.fileName,
                    style = StudioTextStyles.BodyMd.copy(fontWeight = FontWeight.Medium),
                    color = ObsidianOnSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(3.dp))
                            .background(accent.copy(alpha = 0.14f))
                            .padding(horizontal = 5.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = item.badge,
                            style = StudioTextStyles.LabelSm.copy(fontWeight = FontWeight.SemiBold),
                            color = accent
                        )
                    }
                    Text(
                        text = item.spec,
                        style = StudioTextStyles.LabelSm,
                        color = ObsidianOnSurfaceVariant,
                        maxLines = 1
                    )
                    Text(
                        text = "• ${item.sizeLabel}",
                        style = StudioTextStyles.LabelSm,
                        color = ObsidianOnSurfaceVariant,
                        maxLines = 1
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Box(
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .size(34.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(ObsidianSurfaceContainerHigh)
                        .clickable(onClick = onTogglePlay),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (item.isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                        contentDescription = stringResource(R.string.cd_play_pause),
                        tint = if (item.isPlaying) ObsidianPrimary else ObsidianOnSurface,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Box(
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .size(34.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(ObsidianSurfaceContainerHigh)
                        .clickable(onClick = onShare),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.IosShare,
                        contentDescription = stringResource(R.string.cd_share),
                        tint = ObsidianOnSurface,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
