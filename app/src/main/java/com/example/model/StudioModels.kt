package com.example.model

import android.graphics.Bitmap
import android.net.Uri
import com.example.media.DetectedAudioTrack
import com.example.media.ExportAudioFormat
import java.io.File

object StudioRoutes {
    const val HUB = "hub"
    const val EXTRACT = "extract"
    const val AUDIO = "audio"
    const val VIDEO = "video"
}

enum class PipelineKind { AUDIO_EXTRACT, AUDIO_MASTER, VIDEO_CUT }

data class PipelineTaskItem(
    val id: String,
    val fileName: String,
    val badge: String,
    val spec: String,
    val sizeLabel: String,
    val kind: PipelineKind,
    val file: File,
    val mimeType: String,
    val isPlaying: Boolean = false
)

enum class BitrateMode { CBR, VBR }

data class BitrateTier(
    val kbps: Int,
    val label: String
)

enum class ExtractMethod { ACCURATE_RESAMPLE, PASSTHROUGH_COPY }

enum class ExtractStatus { IDLE, PROCESSING, COMPLETED }

data class ExtractUiState(
    val pickedUri: Uri? = null,
    val fileName: String = "",
    val durationMs: Long = 0L,
    val currentPositionMs: Long = 0L,
    val fileSizeLabel: String = "",
    val resolutionLabel: String = "",
    val sourceCodecBadge: String = "",
    val sourceRateBadge: String = "",
    val sourceBitrateLabel: String = "",
    val thumbnailBitmap: Bitmap? = null,
    val isPlayingPreview: Boolean = false,
    val selectedFormat: ExportAudioFormat = ExportAudioFormat.M4A,
    val bitrateMode: BitrateMode = BitrateMode.CBR,
    val selectedBitrateKbps: Int = 320,
    val detectedTracks: List<DetectedAudioTrack> = emptyList(),
    val selectedTrackIndex: Int? = null,
    val advancedExpanded: Boolean = false,
    val extractMethod: ExtractMethod = ExtractMethod.PASSTHROUGH_COPY,
    val fadeInSeconds: Float = 0.0f,
    val fadeOutSeconds: Float = 0.0f,
    val extractStatus: ExtractStatus = ExtractStatus.IDLE,
    val extractProgress: Float = 0f,
    val customFileName: String = "",
    val customArtist: String = "",
    val customTitle: String = "",
    val lastExportedFile: File? = null,
    val lastExportedArtist: String = "",
    val lastExportedTitle: String = ""
) {
    val hasMedia: Boolean get() = pickedUri != null

    val estimatedSizeMb: String
        get() {
            if (!hasMedia || durationMs <= 0L) return "0.0"
            val sec = durationMs / 1000.0
            val mb = if (selectedFormat.isLossless) {
                // 48kHz 16-bit stereo PCM ~ 192 KB/s
                (sec * 192_000.0) / 1_048_576.0
            } else {
                (sec * selectedBitrateKbps * 1000.0 / 8.0) / 1_048_576.0
            }
            return String.format(java.util.Locale.US, "%.1f", mb.coerceAtLeast(0.1))
        }
}

enum class AudioExportStatus { IDLE, PROCESSING, COMPLETED }

data class AudioEditorUiState(
    val pickedUri: Uri? = null,
    val sourceFileName: String = "",
    val formatSpecLabel: String = "",
    val totalDurationMs: Long = 0L,
    val inFraction: Float = 0.0f,
    val outFraction: Float = 1.0f,
    val playheadFraction: Float = 0.0f,
    val waveformBars: List<Float> = emptyList(),
    val isLoadingWaveform: Boolean = false,
    val isPlaying: Boolean = false,
    val isLooping: Boolean = true,
    val isZoomed: Boolean = false,
    val fadeInSec: Float = 0.0f,
    val fadeOutSec: Float = 0.0f,
    val gainDb: Float = 0.0f,
    val customAlbumArtUri: Uri? = null,
    val embeddedAlbumArt: Bitmap? = null,
    val tagTitle: String = "",
    val tagArtist: String = "",
    val tagAlbum: String = "",
    val tagGenre: String = "",
    val tagYear: String = "",
    val tagTrack: String = "",
    val customOutputFileName: String = "",
    val outputFormat: ExportAudioFormat = ExportAudioFormat.M4A,
    val outputBitrateKbps: Int = 320,
    val exportStatus: AudioExportStatus = AudioExportStatus.IDLE,
    val exportProgress: Float = 0f,
    val statusNote: String = "请载入本地音频或从视频提取音轨"
) {
    val hasMedia: Boolean get() = pickedUri != null

    val currentPlayheadMs: Long
        get() = (playheadFraction * totalDurationMs).toLong()

    val selectionDurationMs: Long
        get() = ((outFraction - inFraction).coerceAtLeast(0f) * totalDurationMs).toLong()

    val inPointMs: Long
        get() = (inFraction * totalDurationMs).toLong()

    val outPointMs: Long
        get() = (outFraction * totalDurationMs).toLong()
}

enum class ExportState { IDLE, EXPORTING, DONE }

data class VideoEditorUiState(
    val pickedVideoUri: Uri? = null,
    val fileName: String = "",
    val resolutionBadge: String = "",
    val codecBadge: String = "",
    val sourceFps: Int = 30,
    val totalDurationMs: Long = 0L,
    val inFraction: Float = 0.0f,
    val outFraction: Float = 1.0f,
    val playheadFraction: Float = 0.0f,
    val isPlaying: Boolean = false,
    val showGridOverlay: Boolean = true,
    val isFullscreenPreview: Boolean = false,
    val includeAudioTrack: Boolean = true,
    val previewFrameBitmap: Bitmap? = null,
    val filmstripBitmaps: List<Bitmap> = emptyList(),
    val isLoadingThumbnails: Boolean = false,
    val hardwareAccelLabel: String = "MediaCodec HW",
    val exportState: ExportState = ExportState.IDLE,
    val exportProgress: Float = 0f
) {
    val hasMedia: Boolean get() = pickedVideoUri != null

    val currentTimeMs: Long
        get() = (playheadFraction * totalDurationMs).toLong()

    val currentFrameNumber: Int
        get() = ((currentTimeMs / 1000.0) * sourceFps).toInt()

    val inPointMs: Long
        get() = (inFraction * totalDurationMs).toLong()

    val outPointMs: Long
        get() = (outFraction * totalDurationMs).toLong()

    val selectionDurationMs: Long
        get() = (outPointMs - inPointMs).coerceAtLeast(0L)

    val estimatedSizeMb: Float
        get() {
            if (!hasMedia || totalDurationMs <= 0L) return 0f
            val ratio = (outFraction - inFraction).coerceIn(0.01f, 1f)
            // Approximate proportional trimmed size based on 8 Mbps baseline if unknown
            return (selectionDurationMs / 1000f * 1.1f * ratio.coerceAtLeast(0.2f)).coerceAtLeast(0.2f)
        }
}
