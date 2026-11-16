package com.example.viewmodel

import android.app.Application
import android.media.MediaPlayer
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.media.ExportAudioFormat
import com.example.media.MediaEngine
import com.example.model.AudioEditorUiState
import com.example.model.AudioExportStatus
import com.example.model.BitrateMode
import com.example.model.ExportState
import com.example.model.ExtractMethod
import com.example.model.ExtractStatus
import com.example.model.ExtractUiState
import com.example.model.PipelineKind
import com.example.model.PipelineTaskItem
import com.example.model.VideoEditorUiState
import com.example.util.formatTimecodeMillis
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Locale

data class HubUiState(
    val availableGb: Float = 0f,
    val totalGb: Float = 1f,
    val usedGb: Float = 0f,
    val studioExportsMb: Float = 0f,
    val cacheMb: Float = 0f,
    val hardwareAccelName: String = "MediaCodec HW",
    val showEngineSettingsSheet: Boolean = false,
    val pipelineQueue: List<PipelineTaskItem> = emptyList()
)

class StudioViewModel(application: Application) : AndroidViewModel(application) {

    private val appContext get() = getApplication<Application>()

    private val _hubState = MutableStateFlow(HubUiState())
    val hubState: StateFlow<HubUiState> = _hubState.asStateFlow()

    private val _extractState = MutableStateFlow(ExtractUiState())
    val extractState: StateFlow<ExtractUiState> = _extractState.asStateFlow()

    private val _audioState = MutableStateFlow(AudioEditorUiState())
    val audioState: StateFlow<AudioEditorUiState> = _audioState.asStateFlow()

    private val _videoState = MutableStateFlow(VideoEditorUiState())
    val videoState: StateFlow<VideoEditorUiState> = _videoState.asStateFlow()

    private val _toastMessages = MutableSharedFlow<String>(extraBufferCapacity = 8)
    val toastMessages: SharedFlow<String> = _toastMessages.asSharedFlow()

    private var extractPlayer: MediaPlayer? = null
    private var extractTickerJob: Job? = null

    private var audioPlayer: MediaPlayer? = null
    private var audioTickerJob: Job? = null

    private var queuePlayer: MediaPlayer? = null
    private var videoFrameJob: Job? = null

    init {
        refreshStorageAndQueue()
    }

    fun refreshStorageAndQueue() {
        viewModelScope.launch {
            val telemetry = withContext(Dispatchers.IO) {
                MediaEngine.getStorageTelemetry(appContext)
            }
            val hwName = withContext(Dispatchers.IO) {
                MediaEngine.detectHardwareAcceleratorName()
            }
            val existingFiles = withContext(Dispatchers.IO) {
                val exportsDir = MediaEngine.getExportsDir(appContext)
                exportsDir.listFiles()
                    ?.filter { it.isFile && it.length() > 0L }
                    ?.sortedByDescending { it.lastModified() }
                    ?.map { file -> fileToQueueItem(file) }
                    ?: emptyList()
            }
            _hubState.update {
                it.copy(
                    availableGb = telemetry.availableGb,
                    totalGb = telemetry.totalGb.coerceAtLeast(0.1f),
                    usedGb = telemetry.usedGb,
                    studioExportsMb = telemetry.studioExportsMb,
                    cacheMb = telemetry.cacheMb,
                    hardwareAccelName = hwName,
                    pipelineQueue = existingFiles
                )
            }
            _videoState.update { it.copy(hardwareAccelLabel = hwName) }
        }
    }

    private fun fileToQueueItem(file: File): PipelineTaskItem {
        val ext = file.extension.lowercase(Locale.US)
        val kind = when {
            ext == "mp4" && !file.name.contains("_audio", ignoreCase = true) -> PipelineKind.VIDEO_CUT
            file.name.contains("_master", ignoreCase = true) -> PipelineKind.AUDIO_MASTER
            else -> PipelineKind.AUDIO_EXTRACT
        }
        val mime = when (ext) {
            "wav" -> "audio/wav"
            "m4a" -> "audio/mp4"
            "mp4" -> if (kind == PipelineKind.VIDEO_CUT) "video/mp4" else "audio/mp4"
            else -> "application/octet-stream"
        }
        val badge = ext.uppercase(Locale.US).ifBlank { "MEDIA" }
        val spec = when (kind) {
            PipelineKind.VIDEO_CUT -> "KEYFRAME TRIM"
            PipelineKind.AUDIO_MASTER -> "DSP MASTER"
            PipelineKind.AUDIO_EXTRACT -> "AUDIO STREAM"
        }
        return PipelineTaskItem(
            id = file.absolutePath,
            fileName = file.name,
            badge = badge,
            spec = spec,
            sizeLabel = MediaEngine.formatSize(file.length()),
            kind = kind,
            file = file,
            mimeType = mime
        )
    }

    fun notifyMessage(msg: String) {
        _toastMessages.tryEmit(msg)
    }

    fun toggleEngineSettingsSheet(show: Boolean) {
        _hubState.update { it.copy(showEngineSettingsSheet = show) }
    }

    fun clearCache() {
        viewModelScope.launch(Dispatchers.IO) {
            val remaining = MediaEngine.clearCacheDir(appContext)
            val telemetry = MediaEngine.getStorageTelemetry(appContext)
            _hubState.update {
                it.copy(
                    cacheMb = remaining,
                    availableGb = telemetry.availableGb
                )
            }
            notifyMessage("已清理临时缓存")
        }
    }

    fun clearCompletedQueue() {
        viewModelScope.launch(Dispatchers.IO) {
            stopQueuePlayer()
            MediaEngine.getExportsDir(appContext).listFiles()?.forEach { it.delete() }
            refreshStorageAndQueue()
            notifyMessage("已清空导出记录与工作区文件")
        }
    }

    fun toggleQueueItemPlayback(id: String) {
        val target = _hubState.value.pipelineQueue.firstOrNull { it.id == id } ?: return
        val shouldPlay = !target.isPlaying
        stopQueuePlayer()

        if (shouldPlay) {
            try {
                val uri = FileProvider.getUriForFile(
                    appContext,
                    "${appContext.packageName}.fileprovider",
                    target.file
                )
                queuePlayer = MediaPlayer().apply {
                    setDataSource(appContext, uri)
                    prepare()
                    setOnCompletionListener {
                        _hubState.update { st ->
                            st.copy(pipelineQueue = st.pipelineQueue.map { it.copy(isPlaying = false) })
                        }
                        stopQueuePlayer()
                    }
                    start()
                }
            } catch (e: Exception) {
                notifyMessage("无法播放：${e.localizedMessage ?: "格式不支持"}")
                return
            }
        }

        _hubState.update { st ->
            st.copy(
                pipelineQueue = st.pipelineQueue.map { item ->
                    if (item.id == id) item.copy(isPlaying = shouldPlay)
                    else item.copy(isPlaying = false)
                }
            )
        }
    }

    private fun stopQueuePlayer() {
        try {
            queuePlayer?.stop()
        } catch (_: Exception) {
        }
        try {
            queuePlayer?.release()
        } catch (_: Exception) {
        }
        queuePlayer = null
    }

    fun getShareUriForFile(file: File): Uri {
        return FileProvider.getUriForFile(
            appContext,
            "${appContext.packageName}.fileprovider",
            file
        )
    }

    // --- Media Loading ---

    fun loadPickedMedia(uri: Uri, preferredRoute: String? = null) {
        viewModelScope.launch {
            val result = MediaEngine.inspectMedia(appContext, uri)
            if (!result.isVideo || preferredRoute == "audio") {
                loadAudioInspection(result)
                notifyMessage("已载入音频：${result.displayName}")
            } else {
                loadVideoIntoExtractAndEditor(result)
                notifyMessage("已载入视频：${result.displayName}")
            }
        }
    }

    private suspend fun loadVideoIntoExtractAndEditor(info: com.example.media.MediaInspectionResult) {
        stopExtractPlayer()
        val firstThumb = MediaEngine.extractFrameAtTime(appContext, info.uri, info.durationMs / 4L)
        val firstTrack = info.audioTracks.firstOrNull()
        val srLabel = if (firstTrack != null) "${firstTrack.sampleRate / 1000f}kHz ${firstTrack.channelCount}ch" else "无音轨"
        val codecShort = firstTrack?.mime?.substringAfter('/')?.uppercase(Locale.US) ?: "NONE"
        val brLabel = if (firstTrack != null && firstTrack.bitrateBps > 0) {
            "${firstTrack.bitrateBps / 1000} kbps"
        } else if (info.totalBitrateBps > 0) {
            "${info.totalBitrateBps / 1000} kbps"
        } else {
            "VBR"
        }

        _extractState.update {
            ExtractUiState(
                pickedUri = info.uri,
                fileName = info.displayName,
                durationMs = info.durationMs,
                currentPositionMs = 0L,
                fileSizeLabel = info.sizeLabel,
                resolutionLabel = if (info.width > 0) "${info.width}×${info.height}" else "VIDEO",
                sourceCodecBadge = codecShort,
                sourceRateBadge = srLabel,
                sourceBitrateLabel = brLabel,
                thumbnailBitmap = firstThumb,
                detectedTracks = info.audioTracks,
                selectedTrackIndex = firstTrack?.trackIndex
            )
        }

        _videoState.update {
            it.copy(
                pickedVideoUri = info.uri,
                fileName = info.displayName,
                resolutionBadge = if (info.width > 0) "${info.width}×${info.height}" else "VIDEO",
                codecBadge = info.videoCodecLabel,
                sourceFps = info.frameRateFps,
                totalDurationMs = info.durationMs,
                inFraction = 0.0f,
                outFraction = 1.0f,
                playheadFraction = 0.0f,
                isPlaying = false,
                previewFrameBitmap = firstThumb,
                filmstripBitmaps = emptyList(),
                isLoadingThumbnails = true
            )
        }

        val strip = MediaEngine.extractFilmstripThumbnails(appContext, info.uri, info.durationMs, count = 8)
        _videoState.update {
            it.copy(
                filmstripBitmaps = strip,
                isLoadingThumbnails = false
            )
        }
    }

    private suspend fun loadAudioInspection(info: com.example.media.MediaInspectionResult) {
        stopAudioPlayer()
        val track = info.audioTracks.firstOrNull()
        val spec = if (track != null) {
            val srKhz = String.format(Locale.US, "%.1f kHz", track.sampleRate / 1000f)
            val ch = if (track.channelCount == 1) "Mono" else "Stereo"
            "${track.mime.substringAfter('/').uppercase(Locale.US)} · $srKhz · $ch"
        } else {
            info.sizeLabel
        }

        _audioState.update {
            AudioEditorUiState(
                pickedUri = info.uri,
                sourceFileName = info.displayName,
                formatSpecLabel = spec,
                totalDurationMs = info.durationMs,
                inFraction = 0.0f,
                outFraction = 1.0f,
                playheadFraction = 0.0f,
                waveformBars = emptyList(),
                isLoadingWaveform = true,
                embeddedAlbumArt = info.embeddedArtwork,
                tagTitle = info.title,
                tagArtist = info.artist,
                tagAlbum = info.album,
                tagGenre = info.genre,
                tagYear = info.year,
                tagTrack = info.trackNumber,
                statusNote = "已就绪：${info.displayName} (${info.sizeLabel})"
            )
        }

        val bars = MediaEngine.extractAudioWaveform(appContext, info.uri, info.durationMs)
        _audioState.update {
            it.copy(
                waveformBars = bars,
                isLoadingWaveform = false
            )
        }
    }

    // --- Extract Actions ---

    fun toggleExtractPreviewPlay() {
        val st = _extractState.value
        val uri = st.pickedUri ?: return
        if (st.isPlayingPreview) {
            stopExtractPlayer()
            _extractState.update { it.copy(isPlayingPreview = false) }
        } else {
            try {
                stopExtractPlayer()
                extractPlayer = MediaPlayer().apply {
                    setDataSource(appContext, uri)
                    prepare()
                    if (st.currentPositionMs > 0L) seekTo(st.currentPositionMs.toInt())
                    setOnCompletionListener {
                        stopExtractPlayer()
                        _extractState.update { it.copy(isPlayingPreview = false, currentPositionMs = 0L) }
                    }
                    start()
                }
                _extractState.update { it.copy(isPlayingPreview = true) }
                extractTickerJob = viewModelScope.launch {
                    while (isActive) {
                        delay(80L)
                        val pos = extractPlayer?.currentPosition?.toLong() ?: break
                        _extractState.update { it.copy(currentPositionMs = pos) }
                    }
                }
            } catch (e: Exception) {
                notifyMessage("预览播放失败：${e.localizedMessage ?: "未知错误"}")
            }
        }
    }

    private fun stopExtractPlayer() {
        extractTickerJob?.cancel()
        extractTickerJob = null
        try {
            extractPlayer?.stop()
        } catch (_: Exception) {
        }
        try {
            extractPlayer?.release()
        } catch (_: Exception) {
        }
        extractPlayer = null
    }

    fun seekExtractPosition(fraction: Float) {
        val st = _extractState.value
        if (!st.hasMedia) return
        val targetMs = (fraction.coerceIn(0f, 1f) * st.durationMs).toLong()
        _extractState.update { it.copy(currentPositionMs = targetMs) }
        try {
            extractPlayer?.seekTo(targetMs.toInt())
        } catch (_: Exception) {
        }
    }

    fun selectExtractFormat(format: ExportAudioFormat) {
        _extractState.update { it.copy(selectedFormat = format) }
    }

    fun selectExtractBitrateMode(mode: BitrateMode) {
        _extractState.update { it.copy(bitrateMode = mode) }
    }

    fun selectExtractBitrateKbps(kbps: Int) {
        _extractState.update { it.copy(selectedBitrateKbps = kbps) }
    }

    fun selectExtractTrack(trackIndex: Int) {
        _extractState.update { it.copy(selectedTrackIndex = trackIndex) }
    }

    fun toggleExtractAdvanced() {
        _extractState.update { it.copy(advancedExpanded = !it.advancedExpanded) }
    }

    fun selectExtractMethod(method: ExtractMethod) {
        _extractState.update { it.copy(extractMethod = method) }
    }

    fun adjustExtractFadeIn(delta: Float) {
        _extractState.update { it.copy(fadeInSeconds = (it.fadeInSeconds + delta).coerceIn(0f, 5f)) }
    }

    fun adjustExtractFadeOut(delta: Float) {
        _extractState.update { it.copy(fadeOutSeconds = (it.fadeOutSeconds + delta).coerceIn(0f, 5f)) }
    }

    fun runExtraction() {
        val st = _extractState.value
        val uri = st.pickedUri ?: run {
            notifyMessage("请先选择视频文件")
            return
        }
        if (st.extractStatus == ExtractStatus.PROCESSING) return

        viewModelScope.launch {
            _extractState.update { it.copy(extractStatus = ExtractStatus.PROCESSING, extractProgress = 0f) }
            try {
                val baseName = st.fileName.substringBeforeLast('.').ifBlank { "extracted_audio" }
                val outFile = File(
                    MediaEngine.getExportsDir(appContext),
                    "${baseName}_${System.currentTimeMillis()}.${st.selectedFormat.extension}"
                )
                val forceDsp = st.extractMethod == ExtractMethod.ACCURATE_RESAMPLE ||
                    st.fadeInSeconds > 0f || st.fadeOutSeconds > 0f
                val fadeIn = if (forceDsp) st.fadeInSeconds else 0f
                val fadeOut = if (forceDsp) st.fadeOutSeconds else 0f

                MediaEngine.exportAudioStream(
                    context = appContext,
                    sourceUri = uri,
                    outputFile = outFile,
                    format = st.selectedFormat,
                    trackIndex = st.selectedTrackIndex,
                    bitrateBps = st.selectedBitrateKbps * 1000,
                    gainDb = 0f,
                    fadeInSec = fadeIn,
                    fadeOutSec = fadeOut,
                    onProgress = { p ->
                        _extractState.update { it.copy(extractProgress = p) }
                    }
                )
                MediaEngine.saveToPublicMediaStore(
                    context = appContext,
                    sourceFile = outFile,
                    isVideo = false,
                    mimeType = st.selectedFormat.mimeType
                )
                refreshStorageAndQueue()
                _extractState.update {
                    it.copy(
                        extractStatus = ExtractStatus.COMPLETED,
                        extractProgress = 1f,
                        lastExportedFile = outFile
                    )
                }
                notifyMessage("提取完成：${outFile.name} 已保存到 Music/ObsidianAV")
                delay(2200L)
                _extractState.update { it.copy(extractStatus = ExtractStatus.IDLE) }
            } catch (e: Exception) {
                _extractState.update { it.copy(extractStatus = ExtractStatus.IDLE, extractProgress = 0f) }
                notifyMessage("提取失败：${e.localizedMessage ?: "无法处理该音轨"}")
            }
        }
    }

    fun openLastExtractedInAudioEditor() {
        val file = _extractState.value.lastExportedFile ?: return
        loadPickedMedia(Uri.fromFile(file), preferredRoute = "audio")
    }

    // --- Audio Editor Actions ---

    fun toggleAudioPlay() {
        val st = _audioState.value
        val uri = st.pickedUri ?: return
        if (st.isPlaying) {
            stopAudioPlayer()
            _audioState.update { it.copy(isPlaying = false) }
        } else {
            try {
                stopAudioPlayer()
                val startMs = st.currentPlayheadMs.coerceIn(st.inPointMs, st.outPointMs)
                audioPlayer = MediaPlayer().apply {
                    setDataSource(appContext, uri)
                    prepare()
                    seekTo(startMs.toInt())
                    setOnCompletionListener {
                        stopAudioPlayer()
                        _audioState.update { it.copy(isPlaying = false, playheadFraction = it.inFraction) }
                    }
                    start()
                }
                _audioState.update { it.copy(isPlaying = true) }
                audioTickerJob = viewModelScope.launch {
                    while (isActive) {
                        delay(50L)
                        val player = audioPlayer ?: break
                        val curMs = player.currentPosition.toLong()
                        val latest = _audioState.value
                        if (curMs >= latest.outPointMs) {
                            if (latest.isLooping) {
                                player.seekTo(latest.inPointMs.toInt())
                                _audioState.update { it.copy(playheadFraction = it.inFraction) }
                            } else {
                                stopAudioPlayer()
                                _audioState.update { it.copy(isPlaying = false, playheadFraction = it.outFraction) }
                                break
                            }
                        } else if (latest.totalDurationMs > 0L) {
                            _audioState.update {
                                it.copy(playheadFraction = (curMs.toFloat() / latest.totalDurationMs.toFloat()).coerceIn(0f, 1f))
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                notifyMessage("音频播放失败：${e.localizedMessage ?: "未知错误"}")
            }
        }
    }

    private fun stopAudioPlayer() {
        audioTickerJob?.cancel()
        audioTickerJob = null
        try {
            audioPlayer?.stop()
        } catch (_: Exception) {
        }
        try {
            audioPlayer?.release()
        } catch (_: Exception) {
        }
        audioPlayer = null
    }

    fun toggleAudioLoop() {
        _audioState.update { it.copy(isLooping = !it.isLooping) }
    }

    fun jumpAudioToInPoint() {
        val st = _audioState.value
        _audioState.update { it.copy(playheadFraction = it.inFraction) }
        try {
            audioPlayer?.seekTo(st.inPointMs.toInt())
        } catch (_: Exception) {
        }
    }

    fun jumpAudioToOutPoint() {
        val st = _audioState.value
        _audioState.update { it.copy(playheadFraction = it.outFraction) }
        try {
            audioPlayer?.seekTo(st.outPointMs.toInt())
        } catch (_: Exception) {
        }
    }

    fun toggleAudioZoom() {
        _audioState.update { it.copy(isZoomed = !it.isZoomed) }
    }

    fun updateAudioInPoint(fraction: Float) {
        _audioState.update { st ->
            val safeIn = fraction.coerceIn(0f, st.outFraction - 0.03f)
            st.copy(
                inFraction = safeIn,
                playheadFraction = st.playheadFraction.coerceIn(safeIn, st.outFraction)
            )
        }
    }

    fun updateAudioOutPoint(fraction: Float) {
        _audioState.update { st ->
            val safeOut = fraction.coerceIn(st.inFraction + 0.03f, 1f)
            st.copy(
                outFraction = safeOut,
                playheadFraction = st.playheadFraction.coerceIn(st.inFraction, safeOut)
            )
        }
    }

    fun updateAudioPlayhead(fraction: Float) {
        val clamped = fraction.coerceIn(0f, 1f)
        _audioState.update { it.copy(playheadFraction = clamped) }
        val targetMs = (clamped * _audioState.value.totalDurationMs).toInt()
        try {
            audioPlayer?.seekTo(targetMs)
        } catch (_: Exception) {
        }
    }

    fun splitAudioInAtPlayhead() {
        val st = _audioState.value
        if (!st.hasMedia) return
        val newIn = st.playheadFraction.coerceAtMost(st.outFraction - 0.03f)
        _audioState.update {
            it.copy(
                inFraction = newIn,
                statusNote = "已将入点设为 ${formatTimecodeMillis(st.currentPlayheadMs)}"
            )
        }
    }

    fun resetAudioSelection() {
        val st = _audioState.value
        if (!st.hasMedia) return
        _audioState.update {
            it.copy(
                inFraction = 0f,
                outFraction = 1f,
                playheadFraction = 0f,
                fadeInSec = 0f,
                fadeOutSec = 0f,
                gainDb = 0f,
                statusNote = "已重置选区与声学增益"
            )
        }
    }

    fun updateAudioFadeIn(seconds: Float) {
        _audioState.update { it.copy(fadeInSec = seconds.coerceIn(0f, 5f)) }
    }

    fun updateAudioFadeOut(seconds: Float) {
        _audioState.update { it.copy(fadeOutSec = seconds.coerceIn(0f, 5f)) }
    }

    fun updateAudioGain(db: Float) {
        _audioState.update { it.copy(gainDb = db.coerceIn(-12f, 12f)) }
    }

    fun updateAlbumArt(uri: Uri) {
        _audioState.update { it.copy(customAlbumArtUri = uri) }
        notifyMessage("已选择封面图片")
    }

    fun updateId3Fields(
        title: String? = null,
        artist: String? = null,
        album: String? = null,
        genre: String? = null,
        year: String? = null,
        track: String? = null
    ) {
        _audioState.update {
            it.copy(
                tagTitle = title ?: it.tagTitle,
                tagArtist = artist ?: it.tagArtist,
                tagAlbum = album ?: it.tagAlbum,
                tagGenre = genre ?: it.tagGenre,
                tagYear = year ?: it.tagYear,
                tagTrack = track ?: it.tagTrack
            )
        }
    }

    fun selectAudioOutputFormat(format: ExportAudioFormat) {
        _audioState.update { it.copy(outputFormat = format) }
    }

    fun selectAudioOutputBitrate(kbps: Int) {
        _audioState.update { it.copy(outputBitrateKbps = kbps) }
    }

    fun exportAudioMaster() {
        val st = _audioState.value
        val uri = st.pickedUri ?: run {
            notifyMessage("请先载入音频文件")
            return
        }
        if (st.exportStatus == AudioExportStatus.PROCESSING) return

        viewModelScope.launch {
            _audioState.update { it.copy(exportStatus = AudioExportStatus.PROCESSING, exportProgress = 0f) }
            try {
                val safeTitle = st.tagTitle.ifBlank { st.sourceFileName.substringBeforeLast('.') }
                    .replace(Regex("[^a-zA-Z0-9_\\-\\u4e00-\\u9fa5]"), "_")
                val outFile = File(
                    MediaEngine.getExportsDir(appContext),
                    "${safeTitle}_master_${System.currentTimeMillis()}.${st.outputFormat.extension}"
                )
                MediaEngine.exportAudioStream(
                    context = appContext,
                    sourceUri = uri,
                    outputFile = outFile,
                    format = st.outputFormat,
                    startMs = st.inPointMs,
                    endMs = st.outPointMs,
                    bitrateBps = st.outputBitrateKbps * 1000,
                    gainDb = st.gainDb,
                    fadeInSec = st.fadeInSec,
                    fadeOutSec = st.fadeOutSec,
                    onProgress = { p ->
                        _audioState.update { it.copy(exportProgress = p) }
                    }
                )
                MediaEngine.saveToPublicMediaStore(
                    context = appContext,
                    sourceFile = outFile,
                    isVideo = false,
                    mimeType = st.outputFormat.mimeType
                )
                refreshStorageAndQueue()
                _audioState.update {
                    it.copy(
                        exportStatus = AudioExportStatus.COMPLETED,
                        exportProgress = 1f,
                        statusNote = "已导出母带：${outFile.name}"
                    )
                }
                notifyMessage("已导出音频母带：${outFile.name}")
                delay(2200L)
                _audioState.update { it.copy(exportStatus = AudioExportStatus.IDLE) }
            } catch (e: Exception) {
                _audioState.update { it.copy(exportStatus = AudioExportStatus.IDLE) }
                notifyMessage("导出失败：${e.localizedMessage ?: "无法编码该音频"}")
            }
        }
    }

    // --- Video Editor Actions ---

    fun toggleVideoPlay() {
        val st = _videoState.value
        if (!st.hasMedia) return
        _videoState.update { it.copy(isPlaying = !it.isPlaying) }
    }

    fun onVideoPlaybackPositionTick(positionMs: Long) {
        val st = _videoState.value
        if (st.totalDurationMs <= 0L) return
        val frac = (positionMs.toFloat() / st.totalDurationMs.toFloat()).coerceIn(0f, 1f)
        _videoState.update { it.copy(playheadFraction = frac) }
    }

    fun onVideoPlaybackCompleted() {
        _videoState.update { it.copy(isPlaying = false) }
    }

    fun stepVideoFrames(frames: Int) {
        val st = _videoState.value
        if (!st.hasMedia || st.totalDurationMs <= 0L) return
        val frameMs = (1000.0 / st.sourceFps.coerceAtLeast(15)).toFloat()
        val delta = (frames * frameMs) / st.totalDurationMs.toFloat()
        val nextFrac = (st.playheadFraction + delta).coerceIn(0f, 1f)
        _videoState.update { it.copy(isPlaying = false, playheadFraction = nextFrac) }
        refreshVideoPreviewFrame((nextFrac * st.totalDurationMs).toLong())
    }

    fun seekVideoSeconds(seconds: Int) {
        val st = _videoState.value
        if (!st.hasMedia || st.totalDurationMs <= 0L) return
        val delta = (seconds * 1000f) / st.totalDurationMs.toFloat()
        val nextFrac = (st.playheadFraction + delta).coerceIn(0f, 1f)
        _videoState.update { it.copy(playheadFraction = nextFrac) }
        refreshVideoPreviewFrame((nextFrac * st.totalDurationMs).toLong())
    }

    fun updateVideoInPoint(fraction: Float) {
        _videoState.update { st ->
            val safeIn = fraction.coerceIn(0f, st.outFraction - 0.03f)
            st.copy(
                inFraction = safeIn,
                playheadFraction = st.playheadFraction.coerceIn(safeIn, st.outFraction)
            )
        }
    }

    fun updateVideoOutPoint(fraction: Float) {
        _videoState.update { st ->
            val safeOut = fraction.coerceIn(st.inFraction + 0.03f, 1f)
            st.copy(
                outFraction = safeOut,
                playheadFraction = st.playheadFraction.coerceIn(st.inFraction, safeOut)
            )
        }
    }

    fun updateVideoPlayhead(fraction: Float) {
        val st = _videoState.value
        if (!st.hasMedia) return
        val clamped = fraction.coerceIn(0f, 1f)
        _videoState.update { it.copy(playheadFraction = clamped) }
        refreshVideoPreviewFrame((clamped * st.totalDurationMs).toLong())
    }

    private fun refreshVideoPreviewFrame(timeMs: Long) {
        val uri = _videoState.value.pickedVideoUri ?: return
        videoFrameJob?.cancel()
        videoFrameJob = viewModelScope.launch {
            val bmp = MediaEngine.extractFrameAtTime(appContext, uri, timeMs)
            if (bmp != null) {
                _videoState.update { it.copy(previewFrameBitmap = bmp) }
            }
        }
    }

    fun setVideoInAtPlayhead() {
        val st = _videoState.value
        if (!st.hasMedia) return
        val nextIn = st.playheadFraction.coerceAtMost(st.outFraction - 0.03f)
        _videoState.update { it.copy(inFraction = nextIn) }
        notifyMessage("已将剪辑入点设为 ${formatTimecodeMillis(st.currentTimeMs)}")
    }

    fun toggleVideoGridOverlay() {
        _videoState.update { it.copy(showGridOverlay = !it.showGridOverlay) }
    }

    fun toggleVideoFullscreen(show: Boolean) {
        _videoState.update { it.copy(isFullscreenPreview = show) }
    }

    fun toggleVideoIncludeAudio() {
        _videoState.update { it.copy(includeAudioTrack = !it.includeAudioTrack) }
    }

    fun exportCutVideo() {
        val st = _videoState.value
        val uri = st.pickedVideoUri ?: run {
            notifyMessage("请先选择视频素材")
            return
        }
        if (st.exportState == ExportState.EXPORTING) return

        viewModelScope.launch {
            _videoState.update { it.copy(exportState = ExportState.EXPORTING, exportProgress = 0f) }
            try {
                val baseName = st.fileName.substringBeforeLast('.').ifBlank { "trimmed_video" }
                val outFile = File(
                    MediaEngine.getExportsDir(appContext),
                    "${baseName}_cut_${System.currentTimeMillis()}.mp4"
                )
                MediaEngine.exportTrimmedVideo(
                    context = appContext,
                    sourceUri = uri,
                    outputFile = outFile,
                    startMs = st.inPointMs,
                    endMs = st.outPointMs,
                    includeAudio = st.includeAudioTrack,
                    onProgress = { p ->
                        _videoState.update { it.copy(exportProgress = p) }
                    }
                )
                MediaEngine.saveToPublicMediaStore(
                    context = appContext,
                    sourceFile = outFile,
                    isVideo = true,
                    mimeType = "video/mp4"
                )
                refreshStorageAndQueue()
                _videoState.update { it.copy(exportState = ExportState.DONE, exportProgress = 1f) }
                notifyMessage("导出成功：${outFile.name} 已保存到 Movies/ObsidianAV")
                delay(2200L)
                _videoState.update { it.copy(exportState = ExportState.IDLE) }
            } catch (e: Exception) {
                _videoState.update { it.copy(exportState = ExportState.IDLE) }
                notifyMessage("剪辑导出失败：${e.localizedMessage ?: "容器格式不支持无损直通"}")
            }
        }
    }

    override fun onCleared() {
        stopExtractPlayer()
        stopAudioPlayer()
        stopQueuePlayer()
        super.onCleared()
    }
}
