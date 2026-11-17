package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Album
import androidx.compose.material.icons.rounded.Audiotrack
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DataUsage
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Equalizer
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.Memory
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.Sell
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.VideoLibrary
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.media.ExportAudioFormat
import com.example.model.BitrateMode
import com.example.model.BitrateTier
import com.example.model.ExtractMethod
import com.example.model.ExtractStatus
import com.example.model.ExtractUiState
import com.example.ui.components.PulsingDot
import com.example.ui.theme.ObsidianOnPrimary
import com.example.ui.theme.ObsidianOnPrimaryContainer
import com.example.ui.theme.ObsidianOnSecondaryFixed
import com.example.ui.theme.ObsidianOnSurface
import com.example.ui.theme.ObsidianOnSurfaceVariant
import com.example.ui.theme.ObsidianOutline
import com.example.ui.theme.ObsidianPrimary
import com.example.ui.theme.ObsidianPrimaryContainer
import com.example.ui.theme.ObsidianSecondary
import com.example.ui.theme.ObsidianSecondaryFixed
import com.example.ui.theme.ObsidianSurfaceContainer
import com.example.ui.theme.ObsidianSurfaceContainerHigh
import com.example.ui.theme.ObsidianSurfaceContainerHighest
import com.example.ui.theme.ObsidianSurfaceContainerLow
import com.example.ui.theme.ObsidianSurfaceContainerLowest
import com.example.ui.theme.ObsidianTertiary
import com.example.ui.theme.ObsidianTertiaryContainer
import com.example.ui.theme.StudioTextStyles
import com.example.util.formatTimecodeCentis
import java.util.Locale
import kotlin.math.roundToInt

private val BitrateTiers = listOf(
    BitrateTier(128, "省空间"),
    BitrateTier(192, "均衡标准"),
    BitrateTier(256, "高品质"),
    BitrateTier(320, "极致保真")
)

@Composable
fun ExtractScreen(
    uiState: ExtractUiState,
    onPickFromGallery: () -> Unit,
    onPickFromFiles: () -> Unit,
    onTogglePreviewPlay: () -> Unit,
    onSeek: (Float) -> Unit,
    onSelectFormat: (ExportAudioFormat) -> Unit,
    onSelectBitrateMode: (BitrateMode) -> Unit,
    onSelectBitrateKbps: (Int) -> Unit,
    onSelectTrack: (Int) -> Unit,
    onToggleAdvanced: () -> Unit,
    onSelectMethod: (ExtractMethod) -> Unit,
    onAdjustFadeIn: (Float) -> Unit,
    onAdjustFadeOut: (Float) -> Unit,
    onUpdateCustomFileName: (String) -> Unit,
    onUpdateCustomArtist: (String) -> Unit,
    onUpdateCustomTitle: (String) -> Unit,
    onRunExtraction: () -> Unit,
    onOpenInAudioEditor: () -> Unit,
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
            // 1. Source Video Card or Empty Picker Card
            item {
                if (uiState.hasMedia) {
                    SourceVideoScrubCard(
                        uiState = uiState,
                        onPickFromGallery = onPickFromGallery,
                        onTogglePreviewPlay = onTogglePreviewPlay,
                        onSeek = onSeek
                    )
                } else {
                    EmptyExtractSourceCard(
                        onPickFromGallery = onPickFromGallery,
                        onPickFromFiles = onPickFromFiles
                    )
                }
            }

            // 2. Target Audio Format Selector
            item {
                FormatSelectorSection(
                    selected = uiState.selectedFormat,
                    onSelectFormat = onSelectFormat
                )
            }

            // 3. Bitrate & Quality Console
            item {
                BitrateQualityConsole(
                    uiState = uiState,
                    onSelectBitrateMode = onSelectBitrateMode,
                    onSelectBitrateKbps = onSelectBitrateKbps
                )
            }

            // 4. Track Channel Extraction Multi-selector
            item {
                TrackStreamSelectorCard(
                    uiState = uiState,
                    onSelectTrack = onSelectTrack
                )
            }

            // 5. Custom Output Filename & Artist/Author/Title Card
            item {
                ExtractNamingAndMetadataCard(
                    uiState = uiState,
                    onUpdateCustomFileName = onUpdateCustomFileName,
                    onUpdateCustomArtist = onUpdateCustomArtist,
                    onUpdateCustomTitle = onUpdateCustomTitle
                )
            }

            // 6. Advanced Extraction Settings (Collapsible)
            item {
                AdvancedExtractionSettingsCard(
                    uiState = uiState,
                    onToggleAdvanced = onToggleAdvanced,
                    onSelectMethod = onSelectMethod,
                    onAdjustFadeIn = onAdjustFadeIn,
                    onAdjustFadeOut = onAdjustFadeOut
                )
            }

            // 7. Primary Action CTA Button Dock
            item {
                ExtractPrimaryCtaDock(
                    uiState = uiState,
                    onRunExtraction = onRunExtraction,
                    onOpenInAudioEditor = onOpenInAudioEditor
                )
            }

            item { Spacer(Modifier.height(12.dp)) }
        }
    }
}

@Composable
private fun EmptyExtractSourceCard(
    onPickFromGallery: () -> Unit,
    onPickFromFiles: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = ObsidianSurfaceContainerLow,
        shape = RoundedCornerShape(12.dp),
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
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
                    imageVector = Icons.Rounded.VideoLibrary,
                    contentDescription = null,
                    tint = ObsidianPrimary,
                    modifier = Modifier.size(26.dp)
                )
            }
            Text(
                text = "未载入视频素材",
                style = StudioTextStyles.HeadlineSm,
                color = ObsidianOnSurface
            )
            Text(
                text = "从相册或本地文件选择视频，将自动探测音轨并支持提取导出",
                style = StudioTextStyles.BodySm,
                color = ObsidianOnSurfaceVariant
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(ObsidianPrimary)
                        .clickable(onClick = onPickFromGallery)
                        .testTag("extract_pick_gallery_button"),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Rounded.PhotoLibrary, contentDescription = null, tint = ObsidianOnPrimary, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("相册视频", style = StudioTextStyles.LabelMd.copy(fontWeight = FontWeight.Bold), color = ObsidianOnPrimary)
                }
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(ObsidianSurfaceContainerHigh)
                        .clickable(onClick = onPickFromFiles)
                        .testTag("extract_pick_files_button"),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Rounded.FolderOpen, contentDescription = null, tint = ObsidianSecondary, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("本地文件", style = StudioTextStyles.LabelMd.copy(fontWeight = FontWeight.SemiBold), color = ObsidianOnSurface)
                }
            }
        }
    }
}

@Composable
private fun SourceVideoScrubCard(
    uiState: ExtractUiState,
    onPickFromGallery: () -> Unit,
    onTogglePreviewPlay: () -> Unit,
    onSeek: (Float) -> Unit
) {
    val progress = (uiState.currentPositionMs.toFloat() / uiState.durationMs.coerceAtLeast(1L).toFloat())
        .coerceIn(0f, 1f)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = ObsidianSurfaceContainerLow,
        shape = RoundedCornerShape(12.dp),
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(ObsidianSurfaceContainerLowest),
                    contentAlignment = Alignment.Center
                ) {
                    if (uiState.thumbnailBitmap != null) {
                        Image(
                            bitmap = uiState.thumbnailBitmap.asImageBitmap(),
                            contentDescription = uiState.fileName,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Rounded.VideoLibrary,
                            contentDescription = null,
                            tint = ObsidianOnSurfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.size(36.dp)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        ObsidianSurfaceContainerLowest.copy(alpha = 0.85f)
                                    )
                                )
                            )
                    )
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(4.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(ObsidianSurfaceContainerLowest.copy(alpha = 0.9f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = uiState.resolutionLabel,
                            style = StudioTextStyles.LabelSm,
                            color = ObsidianSecondary
                        )
                    }
                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(ObsidianPrimary.copy(alpha = 0.92f))
                            .clickable(onClick = onTogglePreviewPlay)
                            .testTag("extract_preview_play_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (uiState.isPlayingPreview) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                            contentDescription = stringResource(R.string.cd_play_pause),
                            tint = ObsidianOnPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .height(96.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = uiState.fileName,
                                style = StudioTextStyles.HeadlineSm.copy(fontWeight = FontWeight.SemiBold),
                                color = ObsidianOnSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "更换",
                                style = StudioTextStyles.LabelSm,
                                color = ObsidianPrimary,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .clickable(onClick = onPickFromGallery)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(top = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Timer,
                                contentDescription = null,
                                tint = ObsidianSecondary,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = "${formatTimecodeCentis(uiState.durationMs)} | ${uiState.fileSizeLabel}",
                                style = StudioTextStyles.LabelSm,
                                color = ObsidianOnSurfaceVariant
                            )
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(ObsidianSurfaceContainerHigh)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = uiState.sourceCodecBadge,
                                    style = StudioTextStyles.LabelSm,
                                    color = ObsidianPrimary
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(ObsidianSurfaceContainerHigh)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = uiState.sourceRateBadge,
                                    style = StudioTextStyles.LabelSm,
                                    color = ObsidianOnSurfaceVariant
                                )
                            }
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
                                text = "码率: ${uiState.sourceBitrateLabel}",
                                style = StudioTextStyles.LabelSm,
                                color = ObsidianOutline
                            )
                        }
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = formatTimecodeCentis(uiState.currentPositionMs),
                        style = StudioTextStyles.LabelSm.copy(fontWeight = FontWeight.Medium),
                        color = ObsidianSecondary
                    )
                    Text(
                        text = formatTimecodeCentis(uiState.durationMs),
                        style = StudioTextStyles.LabelSm,
                        color = ObsidianOutline
                    )
                }
                TactileCyanScrubber(
                    progress = progress,
                    onSeek = onSeek
                )
            }
        }
    }
}

@Composable
private fun TactileCyanScrubber(
    progress: Float,
    onSeek: (Float) -> Unit
) {
    val density = LocalDensity.current
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(24.dp)
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    onSeek((offset.x / size.width.toFloat()).coerceIn(0f, 1f))
                }
            }
            .pointerInput(Unit) {
                detectDragGestures { change, _ ->
                    change.consume()
                    onSeek((change.position.x / size.width.toFloat()).coerceIn(0f, 1f))
                }
            },
        contentAlignment = Alignment.CenterStart
    ) {
        val widthPx = with(density) { maxWidth.toPx() }
        val thumbWidthPx = with(density) { 14.dp.toPx() }
        val thumbOffsetX = ((widthPx - thumbWidthPx) * progress).roundToInt()

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(CircleShape)
                .background(ObsidianSurfaceContainerHighest)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress.coerceAtLeast(0.01f))
                    .height(6.dp)
                    .clip(CircleShape)
                    .background(ObsidianSecondary)
            )
        }

        Box(
            modifier = Modifier
                .offset { IntOffset(thumbOffsetX, 0) }
                .width(14.dp)
                .height(18.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(ObsidianSecondaryFixed),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .height(8.dp)
                    .clip(CircleShape)
                    .background(ObsidianOnSecondaryFixed.copy(alpha = 0.5f))
            )
        }
    }
}

@Composable
private fun FormatSelectorSection(
    selected: ExportAudioFormat,
    onSelectFormat: (ExportAudioFormat) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Album,
                    contentDescription = null,
                    tint = ObsidianPrimary,
                    modifier = Modifier.size(18.dp)
                )
                Text("输出格式", style = StudioTextStyles.HeadlineSm, color = ObsidianOnSurface)
            }
            Text("Native MediaCodec / PCM", style = StudioTextStyles.LabelSm, color = ObsidianOnSurfaceVariant)
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            ExportAudioFormat.entries.forEach { format ->
                val isSelected = format == selected
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onSelectFormat(format) }
                        .then(
                            if (isSelected) Modifier.border(
                                1.dp,
                                ObsidianPrimary.copy(alpha = 0.35f),
                                RoundedCornerShape(8.dp)
                            ) else Modifier
                        )
                        .testTag("format_card_${format.label}"),
                    color = if (isSelected) ObsidianSurfaceContainerHigh else ObsidianSurfaceContainerLow,
                    shape = RoundedCornerShape(8.dp),
                    shadowElevation = if (isSelected) 6.dp else 0.dp
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = format.label,
                                style = StudioTextStyles.HeadlineSm.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold
                                ),
                                color = if (isSelected) ObsidianPrimary else ObsidianOnSurface
                            )
                            if (isSelected) {
                                PulsingDot(color = ObsidianPrimary)
                            } else if (format.isLossless) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(ObsidianTertiaryContainer.copy(alpha = 0.3f))
                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                ) {
                                    Text(
                                        text = "无损",
                                        style = StudioTextStyles.LabelSm.copy(fontSize = 9.sp),
                                        color = ObsidianTertiary
                                    )
                                }
                            }
                        }
                        Text(
                            text = format.sub,
                            style = StudioTextStyles.LabelSm,
                            color = if (isSelected) ObsidianOnSurfaceVariant else ObsidianOutline,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BitrateQualityConsole(
    uiState: ExtractUiState,
    onSelectBitrateMode: (BitrateMode) -> Unit,
    onSelectBitrateKbps: (Int) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = ObsidianSurfaceContainerLow,
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
                    Icon(
                        imageVector = Icons.Rounded.Equalizer,
                        contentDescription = null,
                        tint = ObsidianSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                    Text("编码质量与码率", style = StudioTextStyles.HeadlineSm, color = ObsidianOnSurface)
                }

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(ObsidianSurfaceContainerLowest)
                        .padding(2.dp)
                ) {
                    BitrateModeChip(
                        label = "CBR 恒定",
                        selected = uiState.bitrateMode == BitrateMode.CBR,
                        onClick = { onSelectBitrateMode(BitrateMode.CBR) }
                    )
                    BitrateModeChip(
                        label = "VBR 动态",
                        selected = uiState.bitrateMode == BitrateMode.VBR,
                        onClick = { onSelectBitrateMode(BitrateMode.VBR) }
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                BitrateTiers.forEach { tier ->
                    val isSelected = tier.kbps == uiState.selectedBitrateKbps
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isSelected) ObsidianPrimaryContainer else ObsidianSurfaceContainer
                            )
                            .clickable { onSelectBitrateKbps(tier.kbps) }
                            .padding(vertical = 8.dp, horizontal = 4.dp)
                            .testTag("bitrate_pill_${tier.kbps}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = "${tier.kbps}",
                                style = StudioTextStyles.LabelMd.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold
                                ),
                                color = if (isSelected) ObsidianOnPrimaryContainer else ObsidianOnSurface
                            )
                            Text(
                                text = tier.label,
                                style = StudioTextStyles.LabelSm.copy(fontSize = 9.sp),
                                color = if (isSelected) ObsidianOnPrimaryContainer else ObsidianOutline
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(ObsidianSurfaceContainerLowest)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.DataUsage,
                        contentDescription = null,
                        tint = ObsidianSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = if (uiState.hasMedia) "预估导出文件大小" else "载入视频后自动估算体积",
                        style = StudioTextStyles.BodySm,
                        color = ObsidianOnSurfaceVariant
                    )
                }
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = if (uiState.hasMedia) uiState.estimatedSizeMb else "--",
                        style = StudioTextStyles.MetricDisplay.copy(fontWeight = FontWeight.Bold),
                        color = ObsidianPrimary
                    )
                    Text(
                        text = "MB",
                        style = StudioTextStyles.LabelSm,
                        color = ObsidianOnSurfaceVariant,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun BitrateModeChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(if (selected) ObsidianSurfaceContainerHigh else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Text(
            text = label,
            style = StudioTextStyles.LabelSm.copy(
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium
            ),
            color = if (selected) ObsidianPrimary else ObsidianOutline
        )
    }
}

@Composable
private fun TrackStreamSelectorCard(
    uiState: ExtractUiState,
    onSelectTrack: (Int) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = ObsidianSurfaceContainerLow,
        shape = RoundedCornerShape(12.dp),
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
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
                    Icon(
                        imageVector = Icons.Rounded.Audiotrack,
                        contentDescription = null,
                        tint = ObsidianPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = if (uiState.hasMedia) "检测到 ${uiState.detectedTracks.size} 条音轨" else "音轨流探测",
                        style = StudioTextStyles.HeadlineSm,
                        color = ObsidianOnSurface
                    )
                }
                Text("MediaExtractor", style = StudioTextStyles.LabelSm, color = ObsidianSecondary)
            }

            if (!uiState.hasMedia) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(ObsidianSurfaceContainerLowest)
                        .padding(12.dp)
                ) {
                    Text(
                        text = "载入视频后将自动列出容器内的音频轨道编码、采样率与声道信息",
                        style = StudioTextStyles.BodySm,
                        color = ObsidianOutline
                    )
                }
            } else if (uiState.detectedTracks.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(ObsidianSurfaceContainerLowest)
                        .padding(12.dp)
                ) {
                    Text(
                        text = "当前视频文件未检测到音轨",
                        style = StudioTextStyles.BodySm,
                        color = ObsidianOutline
                    )
                }
            } else {
                uiState.detectedTracks.forEachIndexed { idx, track ->
                    val selected = track.trackIndex == uiState.selectedTrackIndex
                    val chLabel = if (track.channelCount == 1) "Mono" else "${track.channelCount}ch"
                    val lang = track.language?.uppercase(Locale.US)?.let { " · $it" }.orEmpty()
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (selected) ObsidianSurfaceContainer else ObsidianSurfaceContainerLowest
                            )
                            .clickable { onSelectTrack(track.trackIndex) }
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(
                                        if (selected) ObsidianPrimary else ObsidianSurfaceContainerHighest
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (selected) {
                                    Icon(
                                        imageVector = Icons.Rounded.Check,
                                        contentDescription = null,
                                        tint = ObsidianOnPrimary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    text = "Track ${idx + 1}: ${track.mime.substringAfter('/').uppercase(Locale.US)}$lang",
                                    style = StudioTextStyles.BodyMd.copy(
                                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                                    ),
                                    color = if (selected) ObsidianOnSurface else ObsidianOnSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "$chLabel · ${track.sampleRate} Hz",
                                    style = StudioTextStyles.LabelSm,
                                    color = ObsidianOutline
                                )
                            }
                        }
                        Text(
                            text = if (idx == 0) "主声轨" else "音轨 ${idx + 1}",
                            style = StudioTextStyles.LabelSm,
                            color = if (selected) ObsidianPrimary else ObsidianOutline,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AdvancedExtractionSettingsCard(
    uiState: ExtractUiState,
    onToggleAdvanced: () -> Unit,
    onSelectMethod: (ExtractMethod) -> Unit,
    onAdjustFadeIn: (Float) -> Unit,
    onAdjustFadeOut: (Float) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = ObsidianSurfaceContainerLow,
        shape = RoundedCornerShape(12.dp),
        shadowElevation = 2.dp
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onToggleAdvanced)
                    .padding(12.dp)
                    .testTag("extract_advanced_toggle"),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Tune,
                        contentDescription = null,
                        tint = ObsidianSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                    Text("高级提取参数", style = StudioTextStyles.HeadlineSm, color = ObsidianOnSurface)
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = if (uiState.extractMethod == ExtractMethod.PASSTHROUGH_COPY) "直通复用" else "PCM 重编码",
                        style = StudioTextStyles.LabelSm,
                        color = ObsidianOutline
                    )
                    Icon(
                        imageVector = Icons.Rounded.ExpandMore,
                        contentDescription = null,
                        tint = ObsidianOnSurfaceVariant,
                        modifier = Modifier
                            .size(18.dp)
                            .rotate(if (uiState.advancedExpanded) 180f else 0f)
                    )
                }
            }

            AnimatedVisibility(visible = uiState.advancedExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 12.dp, end = 12.dp, bottom = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("提取模式", style = StudioTextStyles.BodyMd, color = ObsidianOnSurface)
                            Text("MediaMuxer / Codec", style = StudioTextStyles.LabelSm, color = ObsidianSecondary)
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            MethodOptionButton(
                                label = "直通无损抽轨",
                                selected = uiState.extractMethod == ExtractMethod.PASSTHROUGH_COPY,
                                onClick = { onSelectMethod(ExtractMethod.PASSTHROUGH_COPY) },
                                modifier = Modifier.weight(1f)
                            )
                            MethodOptionButton(
                                label = "PCM 重编码",
                                selected = uiState.extractMethod == ExtractMethod.ACCURATE_RESAMPLE,
                                onClick = { onSelectMethod(ExtractMethod.ACCURATE_RESAMPLE) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        FadeStepperCard(
                            label = "淡入时间 (Fade In)",
                            seconds = uiState.fadeInSeconds,
                            onDecrement = { onAdjustFadeIn(-0.5f) },
                            onIncrement = { onAdjustFadeIn(0.5f) },
                            modifier = Modifier.weight(1f)
                        )
                        FadeStepperCard(
                            label = "淡出时间 (Fade Out)",
                            seconds = uiState.fadeOutSeconds,
                            onDecrement = { onAdjustFadeOut(-0.5f) },
                            onIncrement = { onAdjustFadeOut(0.5f) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MethodOptionButton(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (selected) ObsidianSurfaceContainerHigh else ObsidianSurfaceContainer)
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = StudioTextStyles.LabelMd.copy(
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
            ),
            color = if (selected) ObsidianPrimary else ObsidianOnSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun FadeStepperCard(
    label: String,
    seconds: Float,
    onDecrement: () -> Unit,
    onIncrement: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(ObsidianSurfaceContainer)
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(label, style = StudioTextStyles.BodySm, color = ObsidianOnSurfaceVariant)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(ObsidianSurfaceContainerHigh)
                    .clickable(onClick = onDecrement),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.Remove, contentDescription = "减少", tint = ObsidianOnSurface, modifier = Modifier.size(14.dp))
            }
            Text(
                text = String.format(Locale.US, "%.1fs", seconds),
                style = StudioTextStyles.LabelMd.copy(fontWeight = FontWeight.Bold),
                color = ObsidianOnSurface
            )
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(ObsidianSurfaceContainerHigh)
                    .clickable(onClick = onIncrement),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.Add, contentDescription = "增加", tint = ObsidianPrimary, modifier = Modifier.size(14.dp))
            }
        }
    }
}

@Composable
private fun ExtractNamingAndMetadataCard(
    uiState: ExtractUiState,
    onUpdateCustomFileName: (String) -> Unit,
    onUpdateCustomArtist: (String) -> Unit,
    onUpdateCustomTitle: (String) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = ObsidianSurfaceContainerLow,
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Sell,
                        contentDescription = null,
                        tint = ObsidianPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Text("输出命名与作者信息", style = StudioTextStyles.HeadlineSm, color = ObsidianOnSurface)
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(ObsidianSurfaceContainerHigh)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = ".${uiState.selectedFormat.extension}",
                        style = StudioTextStyles.LabelSm.copy(fontWeight = FontWeight.Bold),
                        color = ObsidianPrimary
                    )
                }
            }

            ExtractInputRow(
                label = "自定义文件名",
                value = uiState.customFileName,
                placeholder = "留空则按视频原名自动生成",
                icon = Icons.Rounded.Edit,
                suffixText = ".${uiState.selectedFormat.extension}",
                testTag = "extract_custom_filename_input",
                onValueChange = onUpdateCustomFileName
            )

            ExtractInputRow(
                label = "歌手 / 作家",
                value = uiState.customArtist,
                placeholder = "填写歌手、主播或作者名（写入媒体库标签）",
                icon = Icons.Rounded.Person,
                suffixText = null,
                testTag = "extract_custom_artist_input",
                onValueChange = onUpdateCustomArtist
            )

            ExtractInputRow(
                label = "曲目标题",
                value = uiState.customTitle,
                placeholder = "填写曲目或章节标题（留空沿用文件名）",
                icon = Icons.Rounded.MusicNote,
                suffixText = null,
                testTag = "extract_custom_title_input",
                onValueChange = onUpdateCustomTitle
            )
        }
    }
}

@Composable
private fun ExtractInputRow(
    label: String,
    value: String,
    placeholder: String,
    icon: ImageVector,
    suffixText: String?,
    testTag: String,
    onValueChange: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = label,
            style = StudioTextStyles.LabelSm,
            color = ObsidianOnSurfaceVariant
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(ObsidianSurfaceContainerLowest)
                .padding(horizontal = 10.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = ObsidianSecondary.copy(alpha = 0.8f),
                modifier = Modifier.size(16.dp)
            )
            Box(modifier = Modifier.weight(1f)) {
                if (value.isEmpty()) {
                    Text(
                        text = placeholder,
                        style = StudioTextStyles.BodyMd,
                        color = ObsidianOutline.copy(alpha = 0.65f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    singleLine = true,
                    cursorBrush = SolidColor(ObsidianPrimary),
                    textStyle = StudioTextStyles.BodyMd.copy(
                        color = ObsidianOnSurface,
                        fontWeight = FontWeight.Medium
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag(testTag)
                )
            }
            if (value.isNotEmpty()) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = "清空$label",
                    tint = ObsidianOnSurfaceVariant,
                    modifier = Modifier
                        .size(18.dp)
                        .clip(CircleShape)
                        .clickable { onValueChange("") }
                        .padding(2.dp)
                )
            }
            if (suffixText != null) {
                Text(
                    text = suffixText,
                    style = StudioTextStyles.LabelSm.copy(fontWeight = FontWeight.SemiBold),
                    color = ObsidianPrimary
                )
            }
        }
    }
}

@Composable
private fun ExtractPrimaryCtaDock(
    uiState: ExtractUiState,
    onRunExtraction: () -> Unit,
    onOpenInAudioEditor: () -> Unit
) {
    val ctaLabel = when (uiState.extractStatus) {
        ExtractStatus.PROCESSING -> "正在提取音轨 (${(uiState.extractProgress * 100).roundToInt()}%)..."
        ExtractStatus.COMPLETED -> "提取完成！已保存到 Music/ObsidianAV"
        ExtractStatus.IDLE -> {
            if (!uiState.hasMedia) {
                "请先载入视频文件"
            } else if (uiState.selectedFormat.isLossless) {
                "提取为 ${uiState.selectedFormat.label} (无损 PCM)"
            } else {
                "提取为 ${uiState.selectedFormat.label} (${uiState.selectedBitrateKbps}k)"
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (uiState.extractStatus == ExtractStatus.PROCESSING) {
            LinearProgressIndicator(
                progress = { uiState.extractProgress },
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
                .clip(RoundedCornerShape(12.dp))
                .clickable(enabled = uiState.hasMedia, onClick = onRunExtraction)
                .testTag("extract_cta_button"),
            color = if (uiState.hasMedia) ObsidianPrimary else ObsidianSurfaceContainerHighest,
            shape = RoundedCornerShape(12.dp),
            shadowElevation = if (uiState.hasMedia) 8.dp else 0.dp
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val contentTint = if (uiState.hasMedia) ObsidianOnPrimary else ObsidianOnSurfaceVariant
                when (uiState.extractStatus) {
                    ExtractStatus.PROCESSING -> {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = contentTint,
                            strokeWidth = 2.dp
                        )
                    }
                    ExtractStatus.COMPLETED -> {
                        Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = contentTint, modifier = Modifier.size(20.dp))
                    }
                    ExtractStatus.IDLE -> {
                        Icon(Icons.Rounded.Bolt, contentDescription = null, tint = contentTint, modifier = Modifier.size(20.dp))
                    }
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    text = ctaLabel,
                    style = StudioTextStyles.HeadlineSm.copy(fontWeight = FontWeight.Bold),
                    color = contentTint
                )
            }
        }

        if (uiState.lastExportedFile != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(ObsidianSurfaceContainerHigh)
                    .clickable(onClick = onOpenInAudioEditor)
                    .padding(vertical = 10.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Rounded.GraphicEq, contentDescription = null, tint = ObsidianSecondary, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text(
                    text = "在「音频编辑」中打开已提取的音轨",
                    style = StudioTextStyles.LabelMd,
                    color = ObsidianSecondary
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Rounded.Memory,
                contentDescription = null,
                tint = ObsidianPrimary,
                modifier = Modifier.size(13.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = "导出文件自动保存至系统 Music/ObsidianAV 目录",
                style = StudioTextStyles.LabelSm,
                color = ObsidianOutline
            )
        }
    }
}
