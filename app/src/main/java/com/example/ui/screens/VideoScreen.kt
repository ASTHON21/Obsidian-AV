package com.example.ui.screens

import android.widget.VideoView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AspectRatio
import androidx.compose.material.icons.rounded.Audiotrack
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ContentCut
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material.icons.rounded.Forward5
import androidx.compose.material.icons.rounded.Grid4x4
import androidx.compose.material.icons.rounded.Memory
import androidx.compose.material.icons.rounded.Movie
import androidx.compose.material.icons.rounded.MovieCreation
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Replay5
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material.icons.rounded.Straighten
import androidx.compose.material.icons.rounded.VerifiedUser
import androidx.compose.material.icons.rounded.VideoLibrary
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
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
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.R
import com.example.model.ExportState
import com.example.model.VideoEditorUiState
import com.example.ui.components.PulsingDot
import com.example.ui.theme.ObsidianError
import com.example.ui.theme.ObsidianOnPrimary
import com.example.ui.theme.ObsidianOnSurface
import com.example.ui.theme.ObsidianOnSurfaceVariant
import com.example.ui.theme.ObsidianOutlineVariant
import com.example.ui.theme.ObsidianPrimary
import com.example.ui.theme.ObsidianSecondary
import com.example.ui.theme.ObsidianSurfaceContainerHigh
import com.example.ui.theme.ObsidianSurfaceContainerHighest
import com.example.ui.theme.ObsidianSurfaceContainerLow
import com.example.ui.theme.ObsidianSurfaceContainerLowest
import com.example.ui.theme.ObsidianTertiary
import com.example.ui.theme.StudioTextStyles
import com.example.util.formatSecondsLabel
import com.example.util.formatTimecodeMillis
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun VideoScreen(
    uiState: VideoEditorUiState,
    onPickFromGallery: () -> Unit,
    onPickFromFiles: () -> Unit,
    onTogglePlay: () -> Unit,
    onPlaybackTick: (Long) -> Unit,
    onPlaybackCompleted: () -> Unit,
    onStepFrames: (Int) -> Unit,
    onSeekSeconds: (Int) -> Unit,
    onUpdateInPoint: (Float) -> Unit,
    onUpdateOutPoint: (Float) -> Unit,
    onUpdatePlayhead: (Float) -> Unit,
    onSetInAtPlayhead: () -> Unit,
    onToggleGridOverlay: () -> Unit,
    onToggleFullscreen: (Boolean) -> Unit,
    onToggleIncludeAudio: () -> Unit,
    onExportCutVideo: () -> Unit,
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
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Precision Breadcrumb & File Meta Strip
            item {
                VideoBreadcrumbStrip(
                    uiState = uiState,
                    onPickFromGallery = onPickFromGallery
                )
            }

            // 1. Video Preview Monitor (16:9 Frameless Studio Canvas)
            item {
                if (uiState.hasMedia) {
                    VideoPreviewMonitor(
                        uiState = uiState,
                        onTogglePlay = onTogglePlay,
                        onPlaybackTick = onPlaybackTick,
                        onPlaybackCompleted = onPlaybackCompleted,
                        onStepFrames = onStepFrames,
                        onSeekSeconds = onSeekSeconds,
                        onToggleGridOverlay = onToggleGridOverlay,
                        onOpenFullscreen = { onToggleFullscreen(true) }
                    )
                } else {
                    EmptyVideoMonitorCard(
                        onPickFromGallery = onPickFromGallery,
                        onPickFromFiles = onPickFromFiles
                    )
                }
            }

            // 2. Precision Video Filmstrip & Caliper Timeline
            item {
                VideoFilmstripTimelineCard(
                    uiState = uiState,
                    onUpdateInPoint = onUpdateInPoint,
                    onUpdateOutPoint = onUpdateOutPoint,
                    onUpdatePlayhead = onUpdatePlayhead,
                    onSetInAtPlayhead = onSetInAtPlayhead
                )
            }

            // 3. Hardware Pipeline & Trim Output Settings
            item {
                VideoPipelineSettingsCard(
                    uiState = uiState,
                    onToggleIncludeAudio = onToggleIncludeAudio
                )
            }

            // 4. Primary Action Bar
            item {
                VideoExportActionBar(
                    uiState = uiState,
                    onExportCutVideo = onExportCutVideo
                )
            }

            item { Spacer(Modifier.height(12.dp)) }
        }
    }

    if (uiState.isFullscreenPreview && uiState.hasMedia) {
        FullscreenMonitorDialog(
            uiState = uiState,
            onTogglePlay = onTogglePlay,
            onDismiss = { onToggleFullscreen(false) }
        )
    }
}

@Composable
private fun VideoBreadcrumbStrip(
    uiState: VideoEditorUiState,
    onPickFromGallery: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.Movie,
                contentDescription = null,
                tint = ObsidianPrimary,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = if (uiState.hasMedia) uiState.fileName else "未选择视频素材",
                style = StudioTextStyles.LabelSm,
                color = ObsidianOnSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false)
            )
            if (uiState.hasMedia && uiState.resolutionBadge.isNotBlank()) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(ObsidianSurfaceContainerHighest)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = uiState.resolutionBadge,
                        style = StudioTextStyles.LabelSm.copy(fontSize = 9.sp),
                        color = ObsidianSecondary
                    )
                }
            }
        }

        if (uiState.hasMedia) {
            Text(
                text = "更换视频",
                style = StudioTextStyles.LabelSm,
                color = ObsidianPrimary,
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .clickable(onClick = onPickFromGallery)
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            )
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                PulsingDot(color = ObsidianPrimary)
                Text(
                    text = "MediaMuxer 就绪",
                    style = StudioTextStyles.LabelSm,
                    color = ObsidianPrimary
                )
            }
        }
    }
}

@Composable
private fun EmptyVideoMonitorCard(
    onPickFromGallery: () -> Unit,
    onPickFromFiles: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f),
        color = ObsidianSurfaceContainerLowest,
        shape = RoundedCornerShape(10.dp),
        shadowElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
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
                    tint = ObsidianSecondary,
                    modifier = Modifier.size(26.dp)
                )
            }
            Spacer(Modifier.height(10.dp))
            Text("载入视频开始无损剪辑", style = StudioTextStyles.HeadlineSm, color = ObsidianOnSurface)
            Text(
                text = "支持关键帧直通裁剪，无需二次重编码",
                style = StudioTextStyles.BodySm,
                color = ObsidianOnSurfaceVariant
            )
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier
                        .height(38.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(ObsidianPrimary)
                        .clickable(onClick = onPickFromGallery)
                        .padding(horizontal = 14.dp)
                        .testTag("video_pick_gallery_button"),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Rounded.PhotoLibrary, contentDescription = null, tint = ObsidianOnPrimary, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("相册选择", style = StudioTextStyles.LabelMd.copy(fontWeight = FontWeight.Bold), color = ObsidianOnPrimary)
                }
                Row(
                    modifier = Modifier
                        .height(38.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(ObsidianSurfaceContainerHigh)
                        .clickable(onClick = onPickFromFiles)
                        .padding(horizontal = 14.dp)
                        .testTag("video_pick_files_button"),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Rounded.FolderOpen, contentDescription = null, tint = ObsidianSecondary, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("本地文件", style = StudioTextStyles.LabelMd, color = ObsidianOnSurface)
                }
            }
        }
    }
}

@Composable
private fun VideoPreviewMonitor(
    uiState: VideoEditorUiState,
    onTogglePlay: () -> Unit,
    onPlaybackTick: (Long) -> Unit,
    onPlaybackCompleted: () -> Unit,
    onStepFrames: (Int) -> Unit,
    onSeekSeconds: (Int) -> Unit,
    onToggleGridOverlay: () -> Unit,
    onOpenFullscreen: () -> Unit
) {
    var videoViewRef by remember { mutableStateOf<VideoView?>(null) }

    LaunchedEffect(uiState.isPlaying, uiState.pickedVideoUri) {
        val vv = videoViewRef
        if (vv != null && uiState.pickedVideoUri != null) {
            if (uiState.isPlaying) {
                vv.seekTo(uiState.currentTimeMs.toInt())
                vv.start()
                while (isActive && uiState.isPlaying) {
                    delay(80L)
                    if (vv.isPlaying) {
                        val pos = vv.currentPosition.toLong()
                        if (pos >= uiState.outPointMs && uiState.outPointMs > uiState.inPointMs) {
                            vv.seekTo(uiState.inPointMs.toInt())
                            onPlaybackTick(uiState.inPointMs)
                        } else {
                            onPlaybackTick(pos)
                        }
                    }
                }
            } else {
                if (vv.isPlaying) vv.pause()
            }
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f),
        color = ObsidianSurfaceContainerLowest,
        shape = RoundedCornerShape(10.dp),
        shadowElevation = 10.dp
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            val uri = uiState.pickedVideoUri
            if (uri != null && uiState.isPlaying) {
                AndroidView(
                    factory = { ctx ->
                        VideoView(ctx).apply {
                            setVideoURI(uri)
                            setOnPreparedListener { mp ->
                                mp.isLooping = false
                                seekTo(uiState.currentTimeMs.toInt())
                                if (uiState.isPlaying) start()
                            }
                            setOnCompletionListener {
                                onPlaybackCompleted()
                            }
                        }.also { videoViewRef = it }
                    },
                    update = { vv ->
                        videoViewRef = vv
                    },
                    modifier = Modifier.fillMaxSize()
                )
            } else if (uiState.previewFrameBitmap != null) {
                Image(
                    bitmap = uiState.previewFrameBitmap.asImageBitmap(),
                    contentDescription = uiState.fileName,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // 3x3 Framing Grid Overlay
            AnimatedVisibility(
                visible = uiState.showGridOverlay,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Spacer(Modifier.weight(1f))
                        HorizontalDivider(color = ObsidianPrimary.copy(alpha = 0.22f), thickness = 1.dp)
                        Spacer(Modifier.weight(1f))
                        HorizontalDivider(color = ObsidianPrimary.copy(alpha = 0.22f), thickness = 1.dp)
                        Spacer(Modifier.weight(1f))
                    }
                    Row(modifier = Modifier.fillMaxSize()) {
                        Spacer(Modifier.weight(1f))
                        VerticalDivider(color = ObsidianPrimary.copy(alpha = 0.22f), thickness = 1.dp)
                        Spacer(Modifier.weight(1f))
                        VerticalDivider(color = ObsidianPrimary.copy(alpha = 0.22f), thickness = 1.dp)
                        Spacer(Modifier.weight(1f))
                    }
                }
            }

            // Top Overlay Micro Badges
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                ObsidianSurfaceContainerLowest.copy(alpha = 0.85f),
                                Color.Transparent
                            )
                        )
                    )
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(ObsidianSurfaceContainerLowest.copy(alpha = 0.9f))
                            .padding(horizontal = 6.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        PulsingDot(color = if (uiState.isPlaying) ObsidianError else ObsidianPrimary)
                        Text(
                            text = formatTimecodeMillis(uiState.currentTimeMs),
                            style = StudioTextStyles.LabelSm,
                            color = ObsidianPrimary,
                            modifier = Modifier.testTag("video_rec_timecode")
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(ObsidianSurfaceContainerLowest.copy(alpha = 0.8f))
                            .padding(horizontal = 6.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "FRAME #${uiState.currentFrameNumber}",
                            style = StudioTextStyles.LabelSm,
                            color = ObsidianOnSurfaceVariant,
                            modifier = Modifier.testTag("video_frame_counter")
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(ObsidianSurfaceContainerHigh.copy(alpha = 0.85f))
                            .clickable(onClick = onToggleGridOverlay)
                            .testTag("video_grid_toggle"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Grid4x4,
                            contentDescription = stringResource(R.string.cd_grid_toggle),
                            tint = if (uiState.showGridOverlay) ObsidianPrimary else ObsidianOnSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(ObsidianSurfaceContainerHigh.copy(alpha = 0.85f))
                            .clickable(onClick = onOpenFullscreen)
                            .testTag("video_fullscreen_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.AspectRatio,
                            contentDescription = stringResource(R.string.cd_fullscreen),
                            tint = ObsidianOnSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Floating Micro Transport Controls Toolbar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                ObsidianSurfaceContainerLowest.copy(alpha = 0.78f),
                                ObsidianSurfaceContainerLowest
                            )
                        )
                    )
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier
                            .height(32.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(ObsidianSurfaceContainerHigh.copy(alpha = 0.9f))
                            .clickable { onStepFrames(-1) }
                            .padding(horizontal = 8.dp)
                            .testTag("video_prev_frame_button"),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.SkipPrevious,
                            contentDescription = stringResource(R.string.cd_prev_frame),
                            tint = ObsidianOnSurface,
                            modifier = Modifier.size(16.dp)
                        )
                        Text("-1F", style = StudioTextStyles.LabelSm.copy(fontSize = 9.sp), color = ObsidianOnSurfaceVariant)
                    }
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(ObsidianSurfaceContainerHigh.copy(alpha = 0.9f))
                            .clickable { onSeekSeconds(-5) }
                            .testTag("video_rewind_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Replay5,
                            contentDescription = stringResource(R.string.cd_rewind_5s),
                            tint = ObsidianOnSurface,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(ObsidianPrimary)
                        .clickable(onClick = onTogglePlay)
                        .testTag("video_play_pause_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (uiState.isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                        contentDescription = stringResource(R.string.cd_play_pause),
                        tint = ObsidianOnPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(ObsidianSurfaceContainerHigh.copy(alpha = 0.9f))
                            .clickable { onSeekSeconds(5) }
                            .testTag("video_forward_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Forward5,
                            contentDescription = stringResource(R.string.cd_forward_5s),
                            tint = ObsidianOnSurface,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Row(
                        modifier = Modifier
                            .height(32.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(ObsidianSurfaceContainerHigh.copy(alpha = 0.9f))
                            .clickable { onStepFrames(1) }
                            .padding(horizontal = 8.dp)
                            .testTag("video_next_frame_button"),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text("+1F", style = StudioTextStyles.LabelSm.copy(fontSize = 9.sp), color = ObsidianOnSurfaceVariant)
                        Icon(
                            imageVector = Icons.Rounded.SkipNext,
                            contentDescription = stringResource(R.string.cd_next_frame),
                            tint = ObsidianOnSurface,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun VideoFilmstripTimelineCard(
    uiState: VideoEditorUiState,
    onUpdateInPoint: (Float) -> Unit,
    onUpdateOutPoint: (Float) -> Unit,
    onUpdatePlayhead: (Float) -> Unit,
    onSetInAtPlayhead: () -> Unit
) {
    val density = LocalDensity.current

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = ObsidianSurfaceContainerLow,
        shape = RoundedCornerShape(10.dp),
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
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Straighten,
                        contentDescription = null,
                        tint = ObsidianPrimary,
                        modifier = Modifier.size(14.dp)
                    )
                    Text("入点", style = StudioTextStyles.LabelSm, color = ObsidianOnSurfaceVariant)
                    Text(
                        text = formatTimecodeMillis(uiState.inPointMs),
                        style = StudioTextStyles.LabelSm.copy(fontWeight = FontWeight.SemiBold),
                        color = ObsidianOnSurface
                    )
                    Text("/", style = StudioTextStyles.LabelSm, color = ObsidianOutlineVariant)
                    Text("出点", style = StudioTextStyles.LabelSm, color = ObsidianOnSurfaceVariant)
                    Text(
                        text = formatTimecodeMillis(uiState.outPointMs),
                        style = StudioTextStyles.LabelSm.copy(fontWeight = FontWeight.SemiBold),
                        color = ObsidianOnSurface
                    )
                }

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(ObsidianSurfaceContainerLowest)
                        .padding(horizontal = 8.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "选区时长",
                        style = StudioTextStyles.LabelSm.copy(fontSize = 9.sp),
                        color = ObsidianOnSurfaceVariant
                    )
                    Text(
                        text = formatTimecodeMillis(uiState.selectionDurationMs),
                        style = StudioTextStyles.LabelMd.copy(fontWeight = FontWeight.Bold),
                        color = ObsidianPrimary
                    )
                }
            }

            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(ObsidianSurfaceContainerLowest)
                    .pointerInput(uiState.hasMedia) {
                        if (uiState.hasMedia) {
                            detectTapGestures { offset ->
                                onUpdatePlayhead((offset.x / size.width.toFloat()).coerceIn(0f, 1f))
                            }
                        }
                    }
            ) {
                val widthPx = with(density) { maxWidth.toPx() }
                val inX = (uiState.inFraction * widthPx).roundToInt()
                val outX = (uiState.outFraction * widthPx).roundToInt()
                val playX = (uiState.playheadFraction * widthPx).roundToInt()

                if (uiState.filmstripBitmaps.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 2.dp, vertical = 4.dp)
                            .alpha(0.82f),
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        uiState.filmstripBitmaps.forEachIndexed { index, bmp ->
                            Image(
                                bitmap = bmp.asImageBitmap(),
                                contentDescription = "视频帧 ${index + 1}",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(4.dp))
                            )
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        if (uiState.isLoadingThumbnails) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(14.dp), color = ObsidianPrimary, strokeWidth = 2.dp)
                                Text("正在提取视频关键帧...", style = StudioTextStyles.LabelSm, color = ObsidianOnSurfaceVariant)
                            }
                        } else {
                            Text("载入视频后显示真实帧缩略图轨", style = StudioTextStyles.LabelSm, color = ObsidianOnSurfaceVariant.copy(alpha = 0.5f))
                        }
                    }
                }

                if (uiState.hasMedia) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(with(density) { inX.toDp() })
                            .align(Alignment.CenterStart)
                            .background(ObsidianSurfaceContainerLowest.copy(alpha = 0.82f))
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(with(density) { (widthPx - outX).coerceAtLeast(0f).toDp() })
                            .align(Alignment.CenterEnd)
                            .background(ObsidianSurfaceContainerLowest.copy(alpha = 0.82f))
                    )

                    Box(
                        modifier = Modifier
                            .offset { IntOffset(inX, 0) }
                            .width(with(density) { (outX - inX).coerceAtLeast(10).toDp() })
                            .fillMaxHeight()
                            .background(ObsidianPrimary.copy(alpha = 0.12f))
                            .border(1.dp, ObsidianPrimary.copy(alpha = 0.35f))
                    )

                    // Left Caliper Handle
                    Box(
                        modifier = Modifier
                            .offset { IntOffset((inX - with(density) { 8.dp.toPx() }).roundToInt(), 0) }
                            .width(24.dp)
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
                                .width(12.dp)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(topStart = 4.dp, bottomStart = 4.dp))
                                .background(ObsidianPrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(2.dp)
                                    .height(14.dp)
                                    .clip(CircleShape)
                                    .background(ObsidianOnPrimary)
                            )
                        }
                    }

                    // Right Caliper Handle
                    Box(
                        modifier = Modifier
                            .offset { IntOffset((outX - with(density) { 16.dp.toPx() }).roundToInt(), 0) }
                            .width(24.dp)
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
                                .width(12.dp)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(topEnd = 4.dp, bottomEnd = 4.dp))
                                .background(ObsidianPrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(2.dp)
                                    .height(14.dp)
                                    .clip(CircleShape)
                                    .background(ObsidianOnPrimary)
                            )
                        }
                    }

                    // Playhead Indicator
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
                                .background(ObsidianSecondary)
                        )
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .size(9.dp)
                                .rotate(45f)
                                .clip(RoundedCornerShape(2.dp))
                                .background(ObsidianSecondary)
                        )
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(ObsidianPrimary.copy(alpha = 0.14f))
                        .padding(horizontal = 6.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(Icons.Rounded.Bolt, contentDescription = null, tint = ObsidianPrimary, modifier = Modifier.size(12.dp))
                    Text(
                        text = "无损关键帧直通剪辑 (MediaMuxer)",
                        style = StudioTextStyles.LabelSm.copy(fontWeight = FontWeight.SemiBold),
                        color = ObsidianPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .clickable(enabled = uiState.hasMedia, onClick = onSetInAtPlayhead)
                        .padding(horizontal = 4.dp, vertical = 3.dp)
                        .testTag("video_split_button"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.ContentCut,
                        contentDescription = "设为入点",
                        tint = if (uiState.hasMedia) ObsidianSecondary else ObsidianOnSurfaceVariant,
                        modifier = Modifier.size(15.dp)
                    )
                    Text(
                        text = "光标设为入点",
                        style = StudioTextStyles.LabelSm,
                        color = if (uiState.hasMedia) ObsidianSecondary else ObsidianOnSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun VideoPipelineSettingsCard(
    uiState: VideoEditorUiState,
    onToggleIncludeAudio: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = ObsidianSurfaceContainerLow,
        shape = RoundedCornerShape(10.dp),
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
                    Icon(Icons.Rounded.Memory, contentDescription = null, tint = ObsidianPrimary, modifier = Modifier.size(18.dp))
                    Text("封装与流参数", style = StudioTextStyles.HeadlineSm, color = ObsidianOnSurface)
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(ObsidianSurfaceContainerLowest)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text("MP4 PASSTHROUGH", style = StudioTextStyles.LabelSm, color = ObsidianSecondary)
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(ObsidianSurfaceContainerLowest)
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text("源视频编码 / 帧率", style = StudioTextStyles.LabelSm, color = ObsidianOnSurfaceVariant)
                    Text(
                        text = if (uiState.hasMedia) "${uiState.codecBadge} · ${uiState.sourceFps} FPS" else "等待载入",
                        style = StudioTextStyles.LabelMd.copy(fontWeight = FontWeight.SemiBold),
                        color = ObsidianPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(ObsidianSurfaceContainerLowest)
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text("硬件引擎", style = StudioTextStyles.LabelSm, color = ObsidianOnSurfaceVariant)
                    Text(
                        text = uiState.hardwareAccelLabel,
                        style = StudioTextStyles.LabelMd.copy(fontWeight = FontWeight.SemiBold),
                        color = ObsidianTertiary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
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
                    Icon(Icons.Rounded.Audiotrack, contentDescription = null, tint = ObsidianSecondary, modifier = Modifier.size(18.dp))
                    Column {
                        Text("保留原视频音轨", style = StudioTextStyles.BodyMd.copy(fontWeight = FontWeight.Medium), color = ObsidianOnSurface)
                        Text("关闭可导出纯静音画面轨道", style = StudioTextStyles.LabelSm, color = ObsidianOnSurfaceVariant)
                    }
                }
                Switch(
                    checked = uiState.includeAudioTrack,
                    onCheckedChange = { onToggleIncludeAudio() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = ObsidianOnPrimary,
                        checkedTrackColor = ObsidianPrimary,
                        uncheckedThumbColor = ObsidianOnSurfaceVariant,
                        uncheckedTrackColor = ObsidianSurfaceContainerHighest
                    )
                )
            }
        }
    }
}

@Composable
private fun VideoExportActionBar(
    uiState: VideoEditorUiState,
    onExportCutVideo: () -> Unit
) {
    val durBadge = if (uiState.hasMedia) {
        "${uiState.resolutionBadge.ifBlank { "CLIP" }} · ${formatSecondsLabel(uiState.selectionDurationMs)}"
    } else {
        "--"
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (uiState.exportState == ExportState.EXPORTING) {
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
                .clip(RoundedCornerShape(12.dp))
                .clickable(enabled = uiState.hasMedia, onClick = onExportCutVideo)
                .testTag("video_export_button"),
            color = if (uiState.hasMedia) ObsidianPrimary else ObsidianSurfaceContainerHighest,
            shape = RoundedCornerShape(12.dp),
            shadowElevation = if (uiState.hasMedia) 8.dp else 0.dp
        ) {
            val contentTint = if (uiState.hasMedia) ObsidianOnPrimary else ObsidianOnSurfaceVariant
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                when (uiState.exportState) {
                    ExportState.EXPORTING -> {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = contentTint,
                            strokeWidth = 2.dp
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "正在无损裁剪 (${(uiState.exportProgress * 100).roundToInt()}%)...",
                            style = StudioTextStyles.HeadlineSm.copy(fontWeight = FontWeight.Bold),
                            color = contentTint
                        )
                    }
                    ExportState.DONE -> {
                        Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = contentTint, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "已导出至 Movies/ObsidianAV",
                            style = StudioTextStyles.HeadlineSm.copy(fontWeight = FontWeight.Bold),
                            color = contentTint
                        )
                    }
                    ExportState.IDLE -> {
                        Icon(Icons.Rounded.MovieCreation, contentDescription = null, tint = contentTint, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = if (uiState.hasMedia) "导出无损剪辑视频" else "请先选择视频素材",
                            style = StudioTextStyles.HeadlineSm.copy(fontWeight = FontWeight.Bold),
                            color = contentTint
                        )
                        if (uiState.hasMedia) {
                            Spacer(Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(ObsidianOnPrimary.copy(alpha = 0.18f))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = durBadge,
                                    style = StudioTextStyles.LabelSm.copy(fontWeight = FontWeight.SemiBold),
                                    color = ObsidianOnPrimary
                                )
                            }
                        }
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Rounded.VerifiedUser,
                contentDescription = null,
                tint = ObsidianPrimary,
                modifier = Modifier.size(13.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = "保持关键帧时间戳完整 · 导出至系统 Movies/ObsidianAV",
                style = StudioTextStyles.LabelSm,
                color = ObsidianOnSurfaceVariant
            )
        }
    }
}

@Composable
private fun FullscreenMonitorDialog(
    uiState: VideoEditorUiState,
    onTogglePlay: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(ObsidianSurfaceContainerLowest)
                .padding(16.dp)
        ) {
            val uri = uiState.pickedVideoUri
            if (uri != null && uiState.isPlaying) {
                AndroidView(
                    factory = { ctx ->
                        VideoView(ctx).apply {
                            setVideoURI(uri)
                            setOnPreparedListener {
                                seekTo(uiState.currentTimeMs.toInt())
                                start()
                            }
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            } else if (uiState.previewFrameBitmap != null) {
                Image(
                    bitmap = uiState.previewFrameBitmap.asImageBitmap(),
                    contentDescription = uiState.fileName,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize()
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${formatTimecodeMillis(uiState.currentTimeMs)}  FRAME #${uiState.currentFrameNumber}",
                    style = StudioTextStyles.LabelMd,
                    color = ObsidianPrimary
                )
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(ObsidianSurfaceContainerHigh)
                        .clickable(onClick = onDismiss),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Rounded.Close, contentDescription = "关闭全屏", tint = ObsidianOnSurface)
                }
            }
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp)
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(ObsidianPrimary)
                    .clickable(onClick = onTogglePlay),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (uiState.isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                    contentDescription = stringResource(R.string.cd_play_pause),
                    tint = ObsidianOnPrimary,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}
