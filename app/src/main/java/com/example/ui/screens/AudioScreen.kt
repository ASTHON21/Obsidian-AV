package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddPhotoAlternate
import androidx.compose.material.icons.rounded.Album
import androidx.compose.material.icons.rounded.AudioFile
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ContentCut
import androidx.compose.material.icons.rounded.DragIndicator
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Equalizer
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material.icons.rounded.IosShare
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.Sell
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material.icons.rounded.TrendingDown
import androidx.compose.material.icons.rounded.TrendingUp
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material.icons.rounded.ZoomIn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.media.ExportAudioFormat
import com.example.model.AudioEditorUiState
import com.example.model.AudioExportStatus
import com.example.ui.components.PulsingDot
import com.example.ui.theme.ObsidianOnPrimary
import com.example.ui.theme.ObsidianOnSecondary
import com.example.ui.theme.ObsidianOnSurface
import com.example.ui.theme.ObsidianOnSurfaceVariant
import com.example.ui.theme.ObsidianOutline
import com.example.ui.theme.ObsidianPrimary
import com.example.ui.theme.ObsidianPrimaryContainer
import com.example.ui.theme.ObsidianSecondary
import com.example.ui.theme.ObsidianSurfaceContainer
import com.example.ui.theme.ObsidianSurfaceContainerHigh
import com.example.ui.theme.ObsidianSurfaceContainerHighest
import com.example.ui.theme.ObsidianSurfaceContainerLow
import com.example.ui.theme.ObsidianSurfaceContainerLowest
import com.example.ui.theme.ObsidianTertiary
import com.example.ui.theme.StudioTextStyles
import com.example.util.formatTimecodeCentis
import com.example.util.formatTimecodeMillis
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun AudioScreen(
    uiState: AudioEditorUiState,
    onPickAudioFile: () -> Unit,
    onTogglePlay: () -> Unit,
    onToggleLoop: () -> Unit,
    onJumpToIn: () -> Unit,
    onJumpToOut: () -> Unit,
    onToggleZoom: () -> Unit,
    onUpdateInPoint: (Float) -> Unit,
    onUpdateOutPoint: (Float) -> Unit,
    onUpdatePlayhead: (Float) -> Unit,
    onSplitInAtPlayhead: () -> Unit,
    onResetSelection: () -> Unit,
    onUpdateFadeIn: (Float) -> Unit,
    onUpdateFadeOut: (Float) -> Unit,
    onUpdateGain: (Float) -> Unit,
    onPickAlbumArt: () -> Unit,
    onUpdateId3: (title: String?, artist: String?, album: String?, genre: String?, year: String?, track: String?) -> Unit,
    onSelectOutputFormat: (ExportAudioFormat) -> Unit,
    onSelectOutputBitrate: (Int) -> Unit,
    onUpdateCustomOutputFileName: (String) -> Unit,
    onExportMaster: () -> Unit,
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
            // 1. TOP INSTRUMENT MONITOR STRIP (OR CLEAN PICKER)
            item {
                if (uiState.hasMedia) {
                    AudioMonitorStripCard(
                        uiState = uiState,
                        onPickAudioFile = onPickAudioFile,
                        onTogglePlay = onTogglePlay,
                        onToggleLoop = onToggleLoop,
                        onJumpToIn = onJumpToIn,
                        onJumpToOut = onJumpToOut,
                        onToggleZoom = onToggleZoom,
                        onUpdateInPoint = onUpdateInPoint,
                        onUpdateOutPoint = onUpdateOutPoint,
                        onUpdatePlayhead = onUpdatePlayhead
                    )
                } else {
                    EmptyAudioPickerCard(onPickAudioFile = onPickAudioFile)
                }
            }

            // 2. QUICK EDIT & DSP TOOLS ROW
            item {
                QuickEditDspToolsCard(
                    uiState = uiState,
                    onSplitInAtPlayhead = onSplitInAtPlayhead,
                    onResetSelection = onResetSelection,
                    onUpdateFadeIn = onUpdateFadeIn,
                    onUpdateFadeOut = onUpdateFadeOut,
                    onUpdateGain = onUpdateGain
                )
            }

            // 3. METADATA SECTION
            item {
                Id3MetadataEditorCard(
                    uiState = uiState,
                    onPickAlbumArt = onPickAlbumArt,
                    onUpdateId3 = onUpdateId3
                )
            }

            // 4. OUTPUT ENCODING SETTINGS
            item {
                AudioOutputSettingsCard(
                    uiState = uiState,
                    onSelectOutputFormat = onSelectOutputFormat,
                    onSelectOutputBitrate = onSelectOutputBitrate,
                    onUpdateCustomOutputFileName = onUpdateCustomOutputFileName
                )
            }

            // 5. ACTION BUTTON BAR
            item {
                AudioExportActionDock(
                    uiState = uiState,
                    onExportMaster = onExportMaster
                )
            }

            item { Spacer(Modifier.height(12.dp)) }
        }
    }
}

@Composable
private fun EmptyAudioPickerCard(onPickAudioFile: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = ObsidianSurfaceContainerLowest,
        shape = RoundedCornerShape(12.dp),
        shadowElevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(ObsidianSurfaceContainerHigh),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.AudioFile,
                    contentDescription = null,
                    tint = ObsidianTertiary,
                    modifier = Modifier.size(26.dp)
                )
            }
            Text("未载入音频文件", style = StudioTextStyles.HeadlineSm, color = ObsidianOnSurface)
            Text(
                text = "选择音频文件后将自动解析真实波形、时间线与音频标签信息",
                style = StudioTextStyles.BodySm,
                color = ObsidianOnSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Row(
                modifier = Modifier
                    .height(42.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(ObsidianPrimary)
                    .clickable(onClick = onPickAudioFile)
                    .padding(horizontal = 20.dp)
                    .testTag("audio_pick_file_button"),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Rounded.FolderOpen, contentDescription = null, tint = ObsidianOnPrimary, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "选择音频文件",
                    style = StudioTextStyles.LabelMd.copy(fontWeight = FontWeight.Bold),
                    color = ObsidianOnPrimary
                )
            }
        }
    }
}

@Composable
private fun AudioMonitorStripCard(
    uiState: AudioEditorUiState,
    onPickAudioFile: () -> Unit,
    onTogglePlay: () -> Unit,
    onToggleLoop: () -> Unit,
    onJumpToIn: () -> Unit,
    onJumpToOut: () -> Unit,
    onToggleZoom: () -> Unit,
    onUpdateInPoint: (Float) -> Unit,
    onUpdateOutPoint: (Float) -> Unit,
    onUpdatePlayhead: (Float) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = ObsidianSurfaceContainerLowest,
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
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    PulsingDot(color = ObsidianPrimary)
                    Text(
                        text = uiState.sourceFileName,
                        style = StudioTextStyles.LabelSm,
                        color = ObsidianPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(ObsidianSurfaceContainerHigh)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = uiState.formatSpecLabel,
                            style = StudioTextStyles.LabelSm.copy(fontSize = 9.sp),
                            color = ObsidianSecondary,
                            maxLines = 1
                        )
                    }
                    Text(
                        text = "更换",
                        style = StudioTextStyles.LabelSm,
                        color = ObsidianPrimary,
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .clickable(onClick = onPickAudioFile)
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(ObsidianSurfaceContainerLow)
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text("CURRENT PLAYHEAD", style = StudioTextStyles.LabelSm, color = ObsidianOnSurfaceVariant)
                    Text(
                        text = formatTimecodeMillis(uiState.currentPlayheadMs),
                        style = StudioTextStyles.MetricDisplay,
                        color = ObsidianPrimary,
                        modifier = Modifier.testTag("audio_playhead_timecode")
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text("SELECTION", style = StudioTextStyles.LabelSm.copy(fontSize = 9.sp), color = ObsidianOnSurfaceVariant)
                        Text(
                            text = formatTimecodeMillis(uiState.selectionDurationMs),
                            style = StudioTextStyles.LabelMd,
                            color = ObsidianSecondary
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("TOTAL", style = StudioTextStyles.LabelSm.copy(fontSize = 9.sp), color = ObsidianOnSurfaceVariant)
                        Text(
                            text = formatTimecodeMillis(uiState.totalDurationMs),
                            style = StudioTextStyles.LabelMd,
                            color = ObsidianOnSurface
                        )
                    }
                }
            }

            InteractiveSpectralWaveform(
                uiState = uiState,
                onUpdateInPoint = onUpdateInPoint,
                onUpdateOutPoint = onUpdateOutPoint,
                onUpdatePlayhead = onUpdatePlayhead
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val marks = listOf(0f, 0.25f, 0.5f, 0.75f, 1f).map { f ->
                    formatTimecodeCentis((f * uiState.totalDurationMs).toLong()).substringBefore('.')
                }
                marks.forEach { mark ->
                    Text(mark, style = StudioTextStyles.LabelSm, color = ObsidianOnSurfaceVariant)
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    TransportSmallButton(
                        icon = Icons.Rounded.Repeat,
                        contentDescription = "循环选区",
                        tint = if (uiState.isLooping) ObsidianPrimary else ObsidianOnSurfaceVariant,
                        onClick = onToggleLoop
                    )
                    TransportSmallButton(
                        icon = Icons.Rounded.SkipPrevious,
                        contentDescription = "跳至入点",
                        tint = ObsidianOnSurface,
                        onClick = onJumpToIn
                    )
                }

                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(ObsidianPrimary)
                        .clickable(onClick = onTogglePlay)
                        .testTag("audio_play_pause_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (uiState.isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                        contentDescription = stringResource(R.string.cd_play_pause),
                        tint = ObsidianOnPrimary,
                        modifier = Modifier.size(26.dp)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    TransportSmallButton(
                        icon = Icons.Rounded.SkipNext,
                        contentDescription = "跳至出点",
                        tint = ObsidianOnSurface,
                        onClick = onJumpToOut
                    )
                    TransportSmallButton(
                        icon = Icons.Rounded.ZoomIn,
                        contentDescription = "缩放波形",
                        tint = if (uiState.isZoomed) ObsidianSecondary else ObsidianOnSurface,
                        onClick = onToggleZoom
                    )
                }
            }
        }
    }
}

@Composable
private fun InteractiveSpectralWaveform(
    uiState: AudioEditorUiState,
    onUpdateInPoint: (Float) -> Unit,
    onUpdateOutPoint: (Float) -> Unit,
    onUpdatePlayhead: (Float) -> Unit
) {
    val density = LocalDensity.current
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(144.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(ObsidianSurfaceContainerLowest)
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    onUpdatePlayhead((offset.x / size.width.toFloat()).coerceIn(0f, 1f))
                }
            }
    ) {
        val widthPx = with(density) { maxWidth.toPx() }
        val inX = (uiState.inFraction * widthPx).roundToInt()
        val outX = (uiState.outFraction * widthPx).roundToInt()
        val playX = (uiState.playheadFraction * widthPx).roundToInt()

        val bars = if (uiState.waveformBars.isNotEmpty()) {
            uiState.waveformBars
        } else {
            List(72) { 0.12f }
        }

        Canvas(modifier = Modifier.fillMaxSize()) {
            val midY = size.height / 2f
            val totalBars = bars.size
            val step = size.width / totalBars
            val barWidth = (step * 0.6f).coerceAtLeast(2f)

            val gridYs = listOf(0.12f, 0.31f, 0.5f, 0.69f, 0.88f)
            gridYs.forEachIndexed { idx, frac ->
                drawLine(
                    color = if (idx == 2) ObsidianPrimary.copy(alpha = 0.25f) else ObsidianOutline.copy(alpha = 0.16f),
                    start = Offset(0f, size.height * frac),
                    end = Offset(size.width, size.height * frac),
                    strokeWidth = 1f
                )
            }

            val selStart = uiState.inFraction * size.width
            val selEnd = uiState.outFraction * size.width
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        ObsidianPrimary.copy(alpha = 0.16f),
                        ObsidianPrimary.copy(alpha = 0.05f),
                        ObsidianPrimary.copy(alpha = 0.16f)
                    )
                ),
                topLeft = Offset(selStart, 0f),
                size = Size((selEnd - selStart).coerceAtLeast(2f), size.height)
            )

            bars.forEachIndexed { i, amp ->
                val frac = (i + 0.5f) / totalBars.toFloat()
                val gainBoost = (1f + uiState.gainDb / 24f).coerceIn(0.4f, 1.35f)
                val halfH = (size.height * 0.43f) * amp * gainBoost
                val inSelection = frac in uiState.inFraction..uiState.outFraction
                val color = if (inSelection) {
                    if (frac <= uiState.playheadFraction) ObsidianPrimary else ObsidianPrimaryContainer.copy(alpha = 0.78f)
                } else {
                    ObsidianSurfaceContainerHighest
                }
                drawRect(
                    color = color,
                    topLeft = Offset(i * step, midY - halfH.coerceAtMost(size.height * 0.46f)),
                    size = Size(barWidth, (halfH * 2f).coerceAtMost(size.height * 0.92f))
                )
            }
        }

        if (uiState.isLoadingWaveform) {
            Row(
                modifier = Modifier
                    .align(Alignment.Center)
                    .clip(RoundedCornerShape(6.dp))
                    .background(ObsidianSurfaceContainerHigh.copy(alpha = 0.9f))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CircularProgressIndicator(modifier = Modifier.size(14.dp), color = ObsidianPrimary, strokeWidth = 2.dp)
                Text("正在分析音频波形...", style = StudioTextStyles.LabelSm, color = ObsidianOnSurface)
            }
        }

        // IN-POINT Marker Handle
        Box(
            modifier = Modifier
                .offset { IntOffset((inX - with(density) { 28.dp.toPx() }).roundToInt(), 0) }
                .width(56.dp)
                .fillMaxHeight()
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        onUpdateInPoint(uiState.inFraction + dragAmount.x / widthPx)
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .fillMaxHeight()
                    .background(ObsidianPrimary)
            )
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .clip(RoundedCornerShape(3.dp))
                    .background(ObsidianPrimary)
                    .padding(horizontal = 4.dp, vertical = 1.dp)
            ) {
                Text(
                    text = "IN ${formatTimecodeCentis(uiState.inPointMs)}",
                    style = StudioTextStyles.LabelSm.copy(fontSize = 8.sp, fontWeight = FontWeight.Bold),
                    color = ObsidianOnPrimary,
                    maxLines = 1
                )
            }
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .size(width = 16.dp, height = 16.dp)
                    .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                    .background(ObsidianPrimary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.DragIndicator,
                    contentDescription = "调整入点",
                    tint = ObsidianOnPrimary,
                    modifier = Modifier.size(11.dp)
                )
            }
        }

        // OUT-POINT Marker Handle
        Box(
            modifier = Modifier
                .offset { IntOffset((outX - with(density) { 30.dp.toPx() }).roundToInt(), 0) }
                .width(60.dp)
                .fillMaxHeight()
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        onUpdateOutPoint(uiState.outFraction + dragAmount.x / widthPx)
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .fillMaxHeight()
                    .background(ObsidianSecondary)
            )
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .clip(RoundedCornerShape(3.dp))
                    .background(ObsidianSecondary)
                    .padding(horizontal = 4.dp, vertical = 1.dp)
            ) {
                Text(
                    text = "OUT ${formatTimecodeCentis(uiState.outPointMs)}",
                    style = StudioTextStyles.LabelSm.copy(fontSize = 8.sp, fontWeight = FontWeight.Bold),
                    color = ObsidianOnSecondary,
                    maxLines = 1
                )
            }
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .size(width = 16.dp, height = 16.dp)
                    .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                    .background(ObsidianSecondary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.DragIndicator,
                    contentDescription = "调整出点",
                    tint = ObsidianOnSecondary,
                    modifier = Modifier.size(11.dp)
                )
            }
        }

        // Current Scrubbing Playhead Indicator
        Box(
            modifier = Modifier
                .offset { IntOffset((playX - with(density) { 12.dp.toPx() }).roundToInt(), 0) }
                .width(24.dp)
                .fillMaxHeight()
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        onUpdatePlayhead(uiState.playheadFraction + dragAmount.x / widthPx)
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .fillMaxHeight()
                    .background(ObsidianOnSurface)
            )
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 2.dp)
                    .size(10.dp)
                    .rotate(45f)
                    .clip(RoundedCornerShape(2.dp))
                    .background(ObsidianOnSurface)
            )
        }
    }
}

@Composable
private fun TransportSmallButton(
    icon: ImageVector,
    contentDescription: String,
    tint: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(38.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(ObsidianSurfaceContainerHigh)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
private fun QuickEditDspToolsCard(
    uiState: AudioEditorUiState,
    onSplitInAtPlayhead: () -> Unit,
    onResetSelection: () -> Unit,
    onUpdateFadeIn: (Float) -> Unit,
    onUpdateFadeOut: (Float) -> Unit,
    onUpdateGain: (Float) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = ObsidianSurfaceContainer,
        shape = RoundedCornerShape(12.dp),
        shadowElevation = 2.dp
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
                Text("声学修剪与包络", style = StudioTextStyles.HeadlineSm, color = ObsidianOnSurface)
                Text("PCM DSP", style = StudioTextStyles.LabelSm, color = ObsidianPrimary)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QuickToolChip("入点设为光标", Icons.Rounded.ContentCut, ObsidianSecondary, Modifier.weight(1f), onSplitInAtPlayhead)
                QuickToolChip("重置选区与增益", Icons.Rounded.Refresh, ObsidianPrimary, Modifier.weight(1f), onResetSelection)
            }

            // Fade In / Fade Out / Gain sliders
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(ObsidianSurfaceContainerLow)
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                DspSliderRow(
                    label = "淡入 (Fade In)",
                    icon = Icons.Rounded.TrendingUp,
                    valueText = String.format(Locale.US, "%.1f s", uiState.fadeInSec),
                    value = uiState.fadeInSec,
                    range = 0f..5f,
                    color = ObsidianPrimary,
                    onValueChange = onUpdateFadeIn
                )
                DspSliderRow(
                    label = "淡出 (Fade Out)",
                    icon = Icons.Rounded.TrendingDown,
                    valueText = String.format(Locale.US, "%.1f s", uiState.fadeOutSec),
                    value = uiState.fadeOutSec,
                    range = 0f..5f,
                    color = ObsidianSecondary,
                    onValueChange = onUpdateFadeOut
                )
                DspSliderRow(
                    label = "增益 (Gain)",
                    icon = Icons.Rounded.Equalizer,
                    valueText = String.format(Locale.US, "%+.1f dB", uiState.gainDb),
                    value = uiState.gainDb,
                    range = -12f..12f,
                    color = ObsidianTertiary,
                    onValueChange = onUpdateGain
                )
            }
        }
    }
}

@Composable
private fun DspSliderRow(
    label: String,
    icon: ImageVector,
    valueText: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    color: Color,
    onValueChange: (Float) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(14.dp))
                Text(label, style = StudioTextStyles.LabelSm, color = ObsidianOnSurfaceVariant)
            }
            Text(valueText, style = StudioTextStyles.LabelMd.copy(fontWeight = FontWeight.Bold), color = color)
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = range,
            colors = SliderDefaults.colors(
                thumbColor = color,
                activeTrackColor = color,
                inactiveTrackColor = ObsidianSurfaceContainerHighest
            )
        )
    }
}

@Composable
private fun QuickToolChip(
    label: String,
    icon: ImageVector,
    tint: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Row(
        modifier = modifier
            .height(40.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(ObsidianSurfaceContainerHigh)
            .clickable(onClick = onClick),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = label, tint = tint, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(6.dp))
        Text(label, style = StudioTextStyles.LabelSm, color = ObsidianOnSurface, maxLines = 1)
    }
}

@Composable
private fun Id3MetadataEditorCard(
    uiState: AudioEditorUiState,
    onPickAlbumArt: () -> Unit,
    onUpdateId3: (title: String?, artist: String?, album: String?, genre: String?, year: String?, track: String?) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = ObsidianSurfaceContainer,
        shape = RoundedCornerShape(12.dp),
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Rounded.Sell, contentDescription = null, tint = ObsidianPrimary, modifier = Modifier.size(20.dp))
                    Text("音频元数据", style = StudioTextStyles.HeadlineSm, color = ObsidianOnSurface)
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(ObsidianSurfaceContainerHigh)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text("METADATA", style = StudioTextStyles.LabelSm, color = ObsidianPrimary)
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(104.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(ObsidianSurfaceContainerHigh)
                        .clickable(onClick = onPickAlbumArt)
                        .testTag("pick_album_art_button"),
                    contentAlignment = Alignment.Center
                ) {
                    when {
                        uiState.customAlbumArtUri != null -> {
                            AsyncImage(
                                model = uiState.customAlbumArtUri,
                                contentDescription = stringResource(R.string.cd_album_art),
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        uiState.embeddedAlbumArt != null -> {
                            Image(
                                bitmap = uiState.embeddedAlbumArt.asImageBitmap(),
                                contentDescription = stringResource(R.string.cd_album_art),
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        else -> {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Rounded.AddPhotoAlternate,
                                    contentDescription = "选择封面",
                                    tint = ObsidianPrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Text("选择封面", style = StudioTextStyles.LabelSm.copy(fontSize = 9.sp), color = ObsidianOnSurfaceVariant)
                            }
                        }
                    }
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Id3FieldBox(
                        label = "标题 (TITLE)",
                        value = uiState.tagTitle,
                        placeholder = "输入曲目标题",
                        icon = Icons.Rounded.Edit,
                        onValueChange = { onUpdateId3(it, null, null, null, null, null) }
                    )
                    Id3FieldBox(
                        label = "艺术家 (ARTIST)",
                        value = uiState.tagArtist,
                        placeholder = "输入艺术家",
                        icon = Icons.Rounded.Mic,
                        onValueChange = { onUpdateId3(null, it, null, null, null, null) }
                    )
                }
            }

            Id3FieldBox(
                label = "专辑 (ALBUM)",
                value = uiState.tagAlbum,
                placeholder = "输入专辑名称",
                icon = Icons.Rounded.Album,
                onValueChange = { onUpdateId3(null, null, it, null, null, null) }
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Id3FieldBox(
                    label = "流派 (GENRE)",
                    value = uiState.tagGenre,
                    placeholder = "--",
                    icon = null,
                    onValueChange = { onUpdateId3(null, null, null, it, null, null) },
                    modifier = Modifier.weight(1.2f)
                )
                Id3FieldBox(
                    label = "年代 (YEAR)",
                    value = uiState.tagYear,
                    placeholder = "2026",
                    icon = null,
                    centered = true,
                    onValueChange = { onUpdateId3(null, null, null, null, it, null) },
                    modifier = Modifier.weight(0.8f)
                )
                Id3FieldBox(
                    label = "音轨 (TRACK)",
                    value = uiState.tagTrack,
                    placeholder = "1",
                    icon = null,
                    centered = true,
                    onValueChange = { onUpdateId3(null, null, null, null, null, it) },
                    modifier = Modifier.weight(0.8f)
                )
            }
        }
    }
}

@Composable
private fun Id3FieldBox(
    label: String,
    value: String,
    placeholder: String,
    icon: ImageVector?,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    centered: Boolean = false
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Text(label, style = StudioTextStyles.LabelSm, color = ObsidianOnSurfaceVariant, maxLines = 1)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(ObsidianSurfaceContainerLow)
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Box(modifier = Modifier.weight(1f)) {
                if (value.isEmpty()) {
                    Text(
                        text = placeholder,
                        style = StudioTextStyles.BodyMd.copy(
                            color = ObsidianOutline.copy(alpha = 0.6f),
                            textAlign = if (centered) TextAlign.Center else TextAlign.Start
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    singleLine = true,
                    cursorBrush = SolidColor(ObsidianPrimary),
                    textStyle = StudioTextStyles.BodyMd.copy(
                        color = ObsidianOnSurface,
                        fontWeight = FontWeight.Medium,
                        textAlign = if (centered) TextAlign.Center else TextAlign.Start
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = ObsidianOnSurfaceVariant.copy(alpha = 0.45f),
                    modifier = Modifier
                        .padding(start = 6.dp)
                        .size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun AudioOutputSettingsCard(
    uiState: AudioEditorUiState,
    onSelectOutputFormat: (ExportAudioFormat) -> Unit,
    onSelectOutputBitrate: (Int) -> Unit,
    onUpdateCustomOutputFileName: (String) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = ObsidianSurfaceContainer,
        shape = RoundedCornerShape(12.dp),
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Rounded.Tune, contentDescription = null, tint = ObsidianSecondary, modifier = Modifier.size(20.dp))
                    Text("音频导出格式与命名", style = StudioTextStyles.HeadlineSm, color = ObsidianOnSurface)
                }
                Text(".${uiState.outputFormat.extension}", style = StudioTextStyles.LabelSm, color = ObsidianPrimary)
            }

            Id3FieldBox(
                label = "输出文件名 (FILENAME)",
                value = uiState.customOutputFileName,
                placeholder = "留空则按原音频名生成",
                icon = Icons.Rounded.Edit,
                onValueChange = onUpdateCustomOutputFileName,
                modifier = Modifier.testTag("audio_custom_filename_input")
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(ObsidianSurfaceContainerLow)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                ExportAudioFormat.entries.forEach { fmt ->
                    val selected = fmt == uiState.outputFormat
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (selected) ObsidianSurfaceContainerHighest else Color.Transparent)
                            .clickable { onSelectOutputFormat(fmt) }
                            .padding(vertical = 6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = fmt.label,
                            style = StudioTextStyles.LabelMd.copy(fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal),
                            color = if (selected) ObsidianPrimary else ObsidianOnSurfaceVariant
                        )
                        Text(
                            text = fmt.sub,
                            style = StudioTextStyles.LabelSm.copy(fontSize = 9.sp),
                            color = if (selected) ObsidianPrimary.copy(alpha = 0.8f) else ObsidianOnSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                }
            }

            if (!uiState.outputFormat.isLossless) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(128, 192, 256, 320).forEach { kbps ->
                        val selected = kbps == uiState.outputBitrateKbps
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (selected) ObsidianSurfaceContainerHighest else ObsidianSurfaceContainerLow)
                                .clickable { onSelectOutputBitrate(kbps) }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${kbps}k",
                                style = StudioTextStyles.LabelMd,
                                color = if (selected) ObsidianPrimary else ObsidianOnSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AudioExportActionDock(
    uiState: AudioEditorUiState,
    onExportMaster: () -> Unit
) {
    val label = when (uiState.exportStatus) {
        AudioExportStatus.PROCESSING -> "正在渲染导出 (${(uiState.exportProgress * 100).roundToInt()}%)..."
        AudioExportStatus.COMPLETED -> "导出完成！已保存到 Music/ObsidianAV"
        AudioExportStatus.IDLE -> if (uiState.hasMedia) "导出处理后音频 (${uiState.outputFormat.label})" else "请先载入音频文件"
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (uiState.exportStatus == AudioExportStatus.PROCESSING) {
            LinearProgressIndicator(
                progress = { uiState.exportProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(CircleShape),
                color = ObsidianPrimary,
                trackColor = ObsidianSurfaceContainerHighest
            )
        }

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clip(RoundedCornerShape(10.dp))
                .clickable(enabled = uiState.hasMedia, onClick = onExportMaster)
                .testTag("audio_export_master_button"),
            color = if (uiState.hasMedia) ObsidianPrimary else ObsidianSurfaceContainerHighest,
            shape = RoundedCornerShape(10.dp),
            shadowElevation = if (uiState.hasMedia) 6.dp else 0.dp
        ) {
            val contentTint = if (uiState.hasMedia) ObsidianOnPrimary else ObsidianOnSurfaceVariant
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                when (uiState.exportStatus) {
                    AudioExportStatus.PROCESSING -> CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = contentTint,
                        strokeWidth = 2.dp
                    )
                    AudioExportStatus.COMPLETED -> Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = contentTint, modifier = Modifier.size(20.dp))
                    AudioExportStatus.IDLE -> Icon(Icons.Rounded.IosShare, contentDescription = null, tint = contentTint, modifier = Modifier.size(20.dp))
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    text = label,
                    style = StudioTextStyles.HeadlineSm.copy(fontWeight = FontWeight.SemiBold),
                    color = contentTint
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Rounded.Verified,
                contentDescription = null,
                tint = ObsidianPrimary,
                modifier = Modifier.size(14.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = uiState.statusNote,
                style = StudioTextStyles.LabelSm,
                color = ObsidianOnSurfaceVariant.copy(alpha = 0.85f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
