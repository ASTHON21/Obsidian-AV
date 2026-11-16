package com.example.media

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaCodecList
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMetadataRetriever
import android.media.MediaMuxer
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.provider.MediaStore
import android.provider.OpenableColumns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.Locale
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.pow

data class DetectedAudioTrack(
    val trackIndex: Int,
    val mime: String,
    val sampleRate: Int,
    val channelCount: Int,
    val bitrateBps: Int,
    val language: String?
)

data class MediaInspectionResult(
    val uri: Uri,
    val displayName: String,
    val sizeBytes: Long,
    val sizeLabel: String,
    val durationMs: Long,
    val isVideo: Boolean,
    val width: Int,
    val height: Int,
    val videoCodecLabel: String,
    val frameRateFps: Int,
    val totalBitrateBps: Int,
    val audioTracks: List<DetectedAudioTrack>,
    val title: String,
    val artist: String,
    val album: String,
    val genre: String,
    val year: String,
    val trackNumber: String,
    val embeddedArtwork: Bitmap?
)

data class StorageTelemetry(
    val availableGb: Float,
    val totalGb: Float,
    val usedGb: Float,
    val studioExportsMb: Float,
    val cacheMb: Float
)

enum class ExportAudioFormat(val extension: String, val mimeType: String, val label: String, val sub: String, val isLossless: Boolean) {
    M4A("m4a", "audio/mp4", "M4A", "AAC 高效封装", false),
    WAV("wav", "audio/wav", "WAV", "无损 PCM 波形", true),
    MP4_AUDIO("mp4", "audio/mp4", "MP4", "通用音轨容器", false)
}

object MediaEngine {

    fun getExportsDir(context: Context): File {
        val dir = File(context.filesDir, "exports")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    fun getStorageTelemetry(context: Context): StorageTelemetry {
        val stat = StatFs(Environment.getDataDirectory().path)
        val totalBytes = stat.totalBytes.toDouble()
        val availBytes = stat.availableBytes.toDouble()
        val usedBytes = (totalBytes - availBytes).coerceAtLeast(0.0)

        val exportsSize = dirSizeBytes(getExportsDir(context))
        val cacheSize = dirSizeBytes(context.cacheDir)

        val gb = 1_073_741_824.0
        val mb = 1_048_576.0
        return StorageTelemetry(
            availableGb = (availBytes / gb).toFloat(),
            totalGb = (totalBytes / gb).toFloat(),
            usedGb = (usedBytes / gb).toFloat(),
            studioExportsMb = (exportsSize / mb).toFloat(),
            cacheMb = (cacheSize / mb).toFloat()
        )
    }

    private fun dirSizeBytes(dir: File): Long {
        if (!dir.exists()) return 0L
        return dir.walkTopDown().filter { it.isFile }.sumOf { it.length() }
    }

    fun clearCacheDir(context: Context): Float {
        context.cacheDir.listFiles()?.forEach { it.deleteRecursively() }
        return (dirSizeBytes(context.cacheDir) / 1_048_576.0).toFloat()
    }

    fun detectHardwareAcceleratorName(): String {
        return try {
            val list = MediaCodecList(MediaCodecList.REGULAR_CODECS)
            val hwEncoder = list.codecInfos.firstOrNull { info ->
                info.isEncoder &&
                    !info.name.contains("google", ignoreCase = true) &&
                    info.supportedTypes.any { it.equals(MediaFormat.MIMETYPE_VIDEO_AVC, true) || it.equals(MediaFormat.MIMETYPE_VIDEO_HEVC, true) }
            }
            if (hwEncoder != null) {
                "MediaCodec HW (${Build.HARDWARE.uppercase(Locale.US)})"
            } else {
                "MediaCodec (${Build.HARDWARE.uppercase(Locale.US)})"
            }
        } catch (_: Exception) {
            "MediaCodec HW"
        }
    }

    suspend fun inspectMedia(context: Context, uri: Uri): MediaInspectionResult = withContext(Dispatchers.IO) {
        var displayName = "Media_${System.currentTimeMillis()}"
        var sizeBytes = 0L
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIdx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            val sizeIdx = cursor.getColumnIndex(OpenableColumns.SIZE)
            if (cursor.moveToFirst()) {
                if (nameIdx >= 0) displayName = cursor.getString(nameIdx) ?: displayName
                if (sizeIdx >= 0 && !cursor.isNull(sizeIdx)) sizeBytes = cursor.getLong(sizeIdx)
            }
        }

        val mime = context.contentResolver.getType(uri) ?: ""
        var durationMs = 0L
        var width = 0
        var height = 0
        var totalBitrate = 0
        var title = ""
        var artist = ""
        var album = ""
        var genre = ""
        var year = ""
        var trackNum = ""
        var embeddedArt: Bitmap? = null
        var hasVideoMeta = mime.startsWith("video/")

        val retriever = MediaMetadataRetriever()
        try {
            retriever.setDataSource(context, uri)
            durationMs = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0L
            width = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)?.toIntOrNull() ?: 0
            height = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)?.toIntOrNull() ?: 0
            totalBitrate = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE)?.toIntOrNull() ?: 0
            if (retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_HAS_VIDEO) == "yes" || width > 0) {
                hasVideoMeta = true
            }
            title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE).orEmpty()
            artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST).orEmpty()
            album = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM).orEmpty()
            genre = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE).orEmpty()
            year = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_YEAR).orEmpty()
            trackNum = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_CD_TRACK_NUMBER).orEmpty()
            retriever.embeddedPicture?.let { bytes ->
                embeddedArt = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            }
        } catch (_: Exception) {
        } finally {
            try {
                retriever.release()
            } catch (_: Exception) {
            }
        }

        val audioTracks = mutableListOf<DetectedAudioTrack>()
        var videoCodec = if (hasVideoMeta) "H.264 / AVC" else "AUDIO"
        var detectedFps = 30

        val extractor = MediaExtractor()
        try {
            extractor.setDataSource(context, uri, null)
            for (i in 0 until extractor.trackCount) {
                val fmt = extractor.getTrackFormat(i)
                val trackMime = fmt.getString(MediaFormat.KEY_MIME) ?: continue
                if (trackMime.startsWith("video/")) {
                    hasVideoMeta = true
                    videoCodec = when {
                        trackMime.contains("hevc", true) || trackMime.contains("h265", true) -> "H.265 / HEVC"
                        trackMime.contains("av01", true) -> "AV1"
                        trackMime.contains("vp9", true) -> "VP9"
                        else -> "H.264 / AVC"
                    }
                    if (fmt.containsKey(MediaFormat.KEY_FRAME_RATE)) {
                        detectedFps = fmt.getInteger(MediaFormat.KEY_FRAME_RATE).coerceIn(15, 120)
                    }
                    if (width == 0 && fmt.containsKey(MediaFormat.KEY_WIDTH)) {
                        width = fmt.getInteger(MediaFormat.KEY_WIDTH)
                    }
                    if (height == 0 && fmt.containsKey(MediaFormat.KEY_HEIGHT)) {
                        height = fmt.getInteger(MediaFormat.KEY_HEIGHT)
                    }
                } else if (trackMime.startsWith("audio/")) {
                    val sr = if (fmt.containsKey(MediaFormat.KEY_SAMPLE_RATE)) fmt.getInteger(MediaFormat.KEY_SAMPLE_RATE) else 48000
                    val ch = if (fmt.containsKey(MediaFormat.KEY_CHANNEL_COUNT)) fmt.getInteger(MediaFormat.KEY_CHANNEL_COUNT) else 2
                    val br = if (fmt.containsKey(MediaFormat.KEY_BIT_RATE)) fmt.getInteger(MediaFormat.KEY_BIT_RATE) else 0
                    val lang = if (fmt.containsKey(MediaFormat.KEY_LANGUAGE)) fmt.getString(MediaFormat.KEY_LANGUAGE) else null
                    audioTracks.add(
                        DetectedAudioTrack(
                            trackIndex = i,
                            mime = trackMime,
                            sampleRate = sr,
                            channelCount = ch,
                            bitrateBps = br,
                            language = lang
                        )
                    )
                }
            }
        } catch (_: Exception) {
        } finally {
            try {
                extractor.release()
            } catch (_: Exception) {
            }
        }

        if (title.isBlank()) {
            title = displayName.substringBeforeLast('.')
        }

        MediaInspectionResult(
            uri = uri,
            displayName = displayName,
            sizeBytes = sizeBytes,
            sizeLabel = formatSize(sizeBytes),
            durationMs = durationMs.coerceAtLeast(1000L),
            isVideo = hasVideoMeta,
            width = width,
            height = height,
            videoCodecLabel = videoCodec,
            frameRateFps = detectedFps,
            totalBitrateBps = totalBitrate,
            audioTracks = audioTracks,
            title = title,
            artist = artist,
            album = album,
            genre = genre,
            year = year,
            trackNumber = trackNum,
            embeddedArtwork = embeddedArt
        )
    }

    suspend fun extractFilmstripThumbnails(
        context: Context,
        uri: Uri,
        durationMs: Long,
        count: Int = 8
    ): List<Bitmap> = withContext(Dispatchers.IO) {
        val frames = mutableListOf<Bitmap>()
        val retriever = MediaMetadataRetriever()
        try {
            retriever.setDataSource(context, uri)
            val safeDurUs = (durationMs.coerceAtLeast(1000L)) * 1000L
            for (i in 0 until count) {
                val timeUs = (safeDurUs * (i + 0.5) / count).toLong()
                val raw = retriever.getFrameAtTime(timeUs, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
                if (raw != null) {
                    val scaled = Bitmap.createScaledBitmap(raw, 160, 90, true)
                    if (scaled !== raw) raw.recycle()
                    frames.add(scaled)
                }
            }
        } catch (_: Exception) {
        } finally {
            try {
                retriever.release()
            } catch (_: Exception) {
            }
        }
        frames
    }

    suspend fun extractFrameAtTime(context: Context, uri: Uri, timeMs: Long): Bitmap? = withContext(Dispatchers.IO) {
        val retriever = MediaMetadataRetriever()
        try {
            retriever.setDataSource(context, uri)
            retriever.getFrameAtTime(timeMs * 1000L, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
        } catch (_: Exception) {
            null
        } finally {
            try {
                retriever.release()
            } catch (_: Exception) {
            }
        }
    }

    suspend fun extractAudioWaveform(
        context: Context,
        uri: Uri,
        durationMs: Long,
        bars: Int = 88
    ): List<Float> = withContext(Dispatchers.IO) {
        val amplitudes = MutableList(bars) { 0.15f }
        val extractor = MediaExtractor()
        var codec: MediaCodec? = null
        try {
            extractor.setDataSource(context, uri, null)
            var audioIdx = -1
            var audioFormat: MediaFormat? = null
            for (i in 0 until extractor.trackCount) {
                val fmt = extractor.getTrackFormat(i)
                val mime = fmt.getString(MediaFormat.KEY_MIME) ?: ""
                if (mime.startsWith("audio/")) {
                    audioIdx = i
                    audioFormat = fmt
                    break
                }
            }
            if (audioIdx < 0 || audioFormat == null) return@withContext amplitudes
            val mime = audioFormat.getString(MediaFormat.KEY_MIME) ?: return@withContext amplitudes
            extractor.selectTrack(audioIdx)

            codec = MediaCodec.createDecoderByType(mime)
            codec.configure(audioFormat, null, null, 0)
            codec.start()

            val info = MediaCodec.BufferInfo()
            val safeDurationUs = (durationMs.coerceAtLeast(1000L)) * 1000L
            val stepUs = (safeDurationUs / bars).coerceAtLeast(10_000L)

            for (barIdx in 0 until bars) {
                val targetUs = barIdx * stepUs
                extractor.seekTo(targetUs, MediaExtractor.SEEK_TO_CLOSEST_SYNC)
                codec.flush()

                var maxPeak = 0
                var iterations = 0
                var sawOutput = false
                while (!sawOutput && iterations < 8) {
                    iterations++
                    val inIdx = codec.dequeueInputBuffer(5_000)
                    if (inIdx >= 0) {
                        val inBuf = codec.getInputBuffer(inIdx)
                        if (inBuf != null) {
                            val sampleSize = extractor.readSampleData(inBuf, 0)
                            if (sampleSize < 0) {
                                codec.queueInputBuffer(inIdx, 0, 0, 0L, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
                            } else {
                                codec.queueInputBuffer(inIdx, 0, sampleSize, extractor.sampleTime, 0)
                                extractor.advance()
                            }
                        }
                    }
                    val outIdx = codec.dequeueOutputBuffer(info, 5_000)
                    if (outIdx >= 0) {
                        val outBuf = codec.getOutputBuffer(outIdx)
                        if (outBuf != null && info.size > 0) {
                            outBuf.position(info.offset)
                            outBuf.limit(info.offset + info.size)
                            val shorts = outBuf.order(ByteOrder.LITTLE_ENDIAN).asShortBuffer()
                            val limit = min(shorts.remaining(), 512)
                            for (s in 0 until limit) {
                                val v = abs(shorts.get(s).toInt())
                                if (v > maxPeak) maxPeak = v
                            }
                            sawOutput = true
                        }
                        codec.releaseOutputBuffer(outIdx, false)
                    }
                }
                if (maxPeak > 0) {
                    amplitudes[barIdx] = (maxPeak / 32767f).coerceIn(0.08f, 0.96f)
                }
            }
        } catch (_: Exception) {
            // Leave gentle baseline if stream cannot be probed
        } finally {
            try {
                codec?.stop()
                codec?.release()
            } catch (_: Exception) {
            }
            try {
                extractor.release()
            } catch (_: Exception) {
            }
        }
        amplitudes
    }

    /**
     * Lossless keyframe trim of video (+ optional audio) using MediaExtractor + MediaMuxer.
     */
    suspend fun exportTrimmedVideo(
        context: Context,
        sourceUri: Uri,
        outputFile: File,
        startMs: Long,
        endMs: Long,
        includeAudio: Boolean = true,
        onProgress: (Float) -> Unit = {}
    ): File = withContext(Dispatchers.IO) {
        val extractor = MediaExtractor()
        var muxer: MediaMuxer? = null
        try {
            extractor.setDataSource(context, sourceUri, null)
            muxer = MediaMuxer(outputFile.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)

            val trackIndexMap = mutableMapOf<Int, Int>()
            var bufferSize = 256 * 1024
            for (i in 0 until extractor.trackCount) {
                val fmt = extractor.getTrackFormat(i)
                val mime = fmt.getString(MediaFormat.KEY_MIME) ?: continue
                val isVideo = mime.startsWith("video/")
                val isAudio = mime.startsWith("audio/") && includeAudio
                if (isVideo || isAudio) {
                    extractor.selectTrack(i)
                    val dstIdx = muxer.addTrack(fmt)
                    trackIndexMap[i] = dstIdx
                    if (fmt.containsKey(MediaFormat.KEY_MAX_INPUT_SIZE)) {
                        val maxInput = fmt.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE)
                        if (maxInput > bufferSize) bufferSize = maxInput
                    }
                }
            }

            if (trackIndexMap.isEmpty()) {
                throw IllegalStateException("未在源文件中检测到可用的音视频轨道")
            }

            // Preserve portrait/landscape rotation metadata for exported MP4
            val retriever = MediaMetadataRetriever()
            try {
                retriever.setDataSource(context, sourceUri)
                val rot = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION)?.toIntOrNull()
                if (rot != null && (rot == 0 || rot == 90 || rot == 180 || rot == 270)) {
                    muxer.setOrientationHint(rot)
                }
            } catch (_: Exception) {
            } finally {
                try {
                    retriever.release()
                } catch (_: Exception) {
                }
            }

            val startUs = (startMs * 1000L).coerceAtLeast(0L)
            val endUs = (endMs * 1000L).coerceAtLeast(startUs + 100_000L)
            val spanUs = (endUs - startUs).coerceAtLeast(1L)

            extractor.seekTo(startUs, MediaExtractor.SEEK_TO_PREVIOUS_SYNC)
            muxer.start()

            val buffer = ByteBuffer.allocate(bufferSize)
            val bufferInfo = MediaCodec.BufferInfo()
            var basePtsUs = -1L
            val lastTrackPts = mutableMapOf<Int, Long>()

            while (true) {
                val sampleTrack = extractor.sampleTrackIndex
                if (sampleTrack < 0) break
                val ptsUs = extractor.sampleTime
                if (ptsUs > endUs) break

                val dstTrack = trackIndexMap[sampleTrack]
                if (dstTrack == null) {
                    extractor.advance()
                    continue
                }

                val sampleSize = extractor.readSampleData(buffer, 0)
                if (sampleSize < 0) break

                if (basePtsUs < 0L) basePtsUs = ptsUs
                val rawRelPts = (ptsUs - basePtsUs).coerceAtLeast(0L)
                val prevPts = lastTrackPts[dstTrack]
                val safeRelPts = if (prevPts != null && rawRelPts <= prevPts) prevPts + 1L else rawRelPts
                lastTrackPts[dstTrack] = safeRelPts

                bufferInfo.offset = 0
                bufferInfo.size = sampleSize
                bufferInfo.presentationTimeUs = safeRelPts
                bufferInfo.flags = extractor.sampleFlags

                muxer.writeSampleData(dstTrack, buffer, bufferInfo)
                onProgress(((ptsUs - startUs).toFloat() / spanUs.toFloat()).coerceIn(0f, 1f))
                extractor.advance()
            }
            onProgress(1f)
            outputFile
        } finally {
            try {
                muxer?.stop()
                muxer?.release()
            } catch (_: Exception) {
            }
            try {
                extractor.release()
            } catch (_: Exception) {
            }
        }
    }

    /**
     * Real audio extraction or edited audio export:
     * - If M4A/MP4_AUDIO with no DSP gain/fade changes and source track is already AAC, does zero-reencode lossless demux/mux!
     * - Otherwise decodes PCM via MediaCodec, applies Gain (dB) + Fade In + Fade Out, and writes genuine WAV or AAC M4A.
     */
    suspend fun exportAudioStream(
        context: Context,
        sourceUri: Uri,
        outputFile: File,
        format: ExportAudioFormat,
        trackIndex: Int? = null,
        startMs: Long = 0L,
        endMs: Long = Long.MAX_VALUE,
        bitrateBps: Int = 320_000,
        gainDb: Float = 0f,
        fadeInSec: Float = 0f,
        fadeOutSec: Float = 0f,
        onProgress: (Float) -> Unit = {}
    ): File = withContext(Dispatchers.IO) {
        val extractor = MediaExtractor()
        extractor.setDataSource(context, sourceUri, null)

        var chosenTrack = trackIndex ?: -1
        var chosenFormat: MediaFormat? = null
        if (chosenTrack in 0 until extractor.trackCount) {
            val fmt = extractor.getTrackFormat(chosenTrack)
            if (fmt.getString(MediaFormat.KEY_MIME)?.startsWith("audio/") == true) {
                chosenFormat = fmt
            }
        }
        if (chosenFormat == null) {
            for (i in 0 until extractor.trackCount) {
                val fmt = extractor.getTrackFormat(i)
                val mime = fmt.getString(MediaFormat.KEY_MIME) ?: continue
                if (mime.startsWith("audio/")) {
                    chosenTrack = i
                    chosenFormat = fmt
                    break
                }
            }
        }

        val sourceFmt = chosenFormat ?: run {
            extractor.release()
            throw IllegalStateException("该媒体文件不包含可提取的音轨")
        }
        val sourceMime = sourceFmt.getString(MediaFormat.KEY_MIME) ?: "audio/mp4a-latm"
        val canDirectMux = format != ExportAudioFormat.WAV &&
            sourceMime.equals(MediaFormat.MIMETYPE_AUDIO_AAC, ignoreCase = true) &&
            abs(gainDb) < 0.05f &&
            fadeInSec <= 0.01f &&
            fadeOutSec <= 0.01f

        if (canDirectMux) {
            var muxer: MediaMuxer? = null
            try {
                extractor.selectTrack(chosenTrack)
                val startUs = (startMs * 1000L).coerceAtLeast(0L)
                val endUs = if (endMs == Long.MAX_VALUE) Long.MAX_VALUE else endMs * 1000L
                if (startUs > 0L) extractor.seekTo(startUs, MediaExtractor.SEEK_TO_CLOSEST_SYNC)

                muxer = MediaMuxer(outputFile.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
                val dstTrack = muxer.addTrack(sourceFmt)
                muxer.start()

                val buffer = ByteBuffer.allocate(128 * 1024)
                val info = MediaCodec.BufferInfo()
                var basePts = -1L

                while (true) {
                    val size = extractor.readSampleData(buffer, 0)
                    if (size < 0) break
                    val pts = extractor.sampleTime
                    if (pts > endUs) break
                    if (basePts < 0L) basePts = pts
                    info.offset = 0
                    info.size = size
                    info.presentationTimeUs = (pts - basePts).coerceAtLeast(0L)
                    info.flags = extractor.sampleFlags
                    muxer.writeSampleData(dstTrack, buffer, info)
                    extractor.advance()
                }
                onProgress(1f)
                return@withContext outputFile
            } finally {
                try {
                    muxer?.stop()
                    muxer?.release()
                } catch (_: Exception) {
                }
                try {
                    extractor.release()
                } catch (_: Exception) {
                }
            }
        }

        // Otherwise decode PCM -> DSP (Gain / Fade In / Fade Out) -> WAV or AAC M4A
        val pcmTemp = File(context.cacheDir, "pcm_temp_${System.currentTimeMillis()}.raw")
        var decoder: MediaCodec? = null
        var sampleRate = if (sourceFmt.containsKey(MediaFormat.KEY_SAMPLE_RATE)) sourceFmt.getInteger(MediaFormat.KEY_SAMPLE_RATE) else 44100
        var channels = if (sourceFmt.containsKey(MediaFormat.KEY_CHANNEL_COUNT)) sourceFmt.getInteger(MediaFormat.KEY_CHANNEL_COUNT) else 2
        val totalSpanUs = if (endMs == Long.MAX_VALUE) Long.MAX_VALUE else (endMs - startMs) * 1000L

        try {
            extractor.selectTrack(chosenTrack)
            val startUs = (startMs * 1000L).coerceAtLeast(0L)
            val endUs = if (endMs == Long.MAX_VALUE) Long.MAX_VALUE else endMs * 1000L
            if (startUs > 0L) extractor.seekTo(startUs, MediaExtractor.SEEK_TO_CLOSEST_SYNC)

            decoder = MediaCodec.createDecoderByType(sourceMime)
            decoder.configure(sourceFmt, null, null, 0)
            decoder.start()

            val info = MediaCodec.BufferInfo()
            var inputDone = false
            var outputDone = false
            var firstPtsUs = -1L

            FileOutputStream(pcmTemp).use { rawOut ->
                while (!outputDone) {
                    if (!inputDone) {
                        val inIdx = decoder.dequeueInputBuffer(10_000)
                        if (inIdx >= 0) {
                            val inBuf = decoder.getInputBuffer(inIdx)!!
                            val sampleSize = extractor.readSampleData(inBuf, 0)
                            val pts = extractor.sampleTime
                            if (sampleSize < 0 || pts > endUs) {
                                decoder.queueInputBuffer(inIdx, 0, 0, 0L, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
                                inputDone = true
                            } else {
                                decoder.queueInputBuffer(inIdx, 0, sampleSize, pts, 0)
                                extractor.advance()
                            }
                        }
                    }

                    val outIdx = decoder.dequeueOutputBuffer(info, 10_000)
                    when {
                        outIdx == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> {
                            val newFmt = decoder.outputFormat
                            if (newFmt.containsKey(MediaFormat.KEY_SAMPLE_RATE)) {
                                sampleRate = newFmt.getInteger(MediaFormat.KEY_SAMPLE_RATE)
                            }
                            if (newFmt.containsKey(MediaFormat.KEY_CHANNEL_COUNT)) {
                                channels = newFmt.getInteger(MediaFormat.KEY_CHANNEL_COUNT)
                            }
                        }
                        outIdx >= 0 -> {
                            if (info.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) {
                                outputDone = true
                            }
                            val outBuf = decoder.getOutputBuffer(outIdx)
                            if (outBuf != null && info.size > 0) {
                                if (firstPtsUs < 0L) firstPtsUs = info.presentationTimeUs
                                outBuf.position(info.offset)
                                outBuf.limit(info.offset + info.size)
                                val chunk = ByteArray(info.size)
                                outBuf.get(chunk)
                                rawOut.write(chunk)
                                if (totalSpanUs in 1L until Long.MAX_VALUE) {
                                    val elapsed = (info.presentationTimeUs - firstPtsUs).coerceAtLeast(0L)
                                    onProgress((elapsed.toFloat() / totalSpanUs.toFloat() * 0.7f).coerceIn(0f, 0.7f))
                                }
                            }
                            decoder.releaseOutputBuffer(outIdx, false)
                        }
                    }
                }
            }

            // Apply Gain & Fade In/Out directly on 16-bit PCM file
            applyDspToPcmFile(
                pcmFile = pcmTemp,
                sampleRate = sampleRate,
                channels = channels,
                gainDb = gainDb,
                fadeInSec = fadeInSec,
                fadeOutSec = fadeOutSec
            )

            if (format == ExportAudioFormat.WAV) {
                writeWavFromPcm(pcmTemp, outputFile, sampleRate, channels)
            } else {
                encodePcmToAacM4a(pcmTemp, outputFile, sampleRate, channels, bitrateBps)
            }
            onProgress(1f)
            outputFile
        } finally {
            pcmTemp.delete()
            try {
                decoder?.stop()
                decoder?.release()
            } catch (_: Exception) {
            }
            try {
                extractor.release()
            } catch (_: Exception) {
            }
        }
    }

    private fun applyDspToPcmFile(
        pcmFile: File,
        sampleRate: Int,
        channels: Int,
        gainDb: Float,
        fadeInSec: Float,
        fadeOutSec: Float
    ) {
        if (abs(gainDb) < 0.05f && fadeInSec <= 0.01f && fadeOutSec <= 0.01f) return
        val bytes = pcmFile.readBytes()
        val totalSamples = bytes.size / 2
        if (totalSamples <= 0) return
        val totalFrames = totalSamples / channels.coerceAtLeast(1)
        val fadeInFrames = (fadeInSec * sampleRate).toInt().coerceIn(0, totalFrames)
        val fadeOutFrames = (fadeOutSec * sampleRate).toInt().coerceIn(0, totalFrames)
        val linearGain = 10.0.pow((gainDb / 20.0)).toFloat()

        val shortBuf = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer()
        for (frame in 0 until totalFrames) {
            var env = linearGain
            if (fadeInFrames > 0 && frame < fadeInFrames) {
                env *= (frame.toFloat() / fadeInFrames.toFloat())
            }
            if (fadeOutFrames > 0 && frame >= totalFrames - fadeOutFrames) {
                val rem = (totalFrames - frame).coerceAtLeast(0)
                env *= (rem.toFloat() / fadeOutFrames.toFloat())
            }
            for (ch in 0 until channels) {
                val idx = frame * channels + ch
                val scaled = (shortBuf.get(idx) * env).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
                shortBuf.put(idx, scaled.toShort())
            }
        }
        pcmFile.writeBytes(bytes)
    }

    private fun writeWavFromPcm(pcmFile: File, outFile: File, sampleRate: Int, channels: Int) {
        val pcmBytes = pcmFile.length()
        val byteRate = sampleRate * channels * 2
        val header = ByteBuffer.allocate(44).order(ByteOrder.LITTLE_ENDIAN).apply {
            put("RIFF".toByteArray())
            putInt((36 + pcmBytes).toInt())
            put("WAVE".toByteArray())
            put("fmt ".toByteArray())
            putInt(16) // PCM chunk size
            putShort(1.toShort()) // AudioFormat PCM
            putShort(channels.toShort())
            putInt(sampleRate)
            putInt(byteRate)
            putShort((channels * 2).toShort())
            putShort(16.toShort())
            put("data".toByteArray())
            putInt(pcmBytes.toInt())
        }.array()

        FileOutputStream(outFile).use { fos ->
            fos.write(header)
            pcmFile.inputStream().use { input ->
                input.copyTo(fos, bufferSize = 32 * 1024)
            }
        }
    }

    private fun encodePcmToAacM4a(
        pcmFile: File,
        outFile: File,
        sampleRate: Int,
        channels: Int,
        bitrateBps: Int
    ) {
        val encFmt = MediaFormat.createAudioFormat(MediaFormat.MIMETYPE_AUDIO_AAC, sampleRate, channels).apply {
            setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC)
            setInteger(MediaFormat.KEY_BIT_RATE, bitrateBps.coerceIn(64_000, 320_000))
            setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, 16 * 1024)
        }
        val encoder = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_AUDIO_AAC)
        encoder.configure(encFmt, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
        encoder.start()

        val muxer = MediaMuxer(outFile.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
        var muxerTrack = -1
        var muxerStarted = false

        val bytes = pcmFile.readBytes()
        var readOffset = 0
        val bytesPerFrame = channels * 2
        var totalFramesFed = 0L
        val info = MediaCodec.BufferInfo()
        var inputDone = false
        var outputDone = false

        try {
            while (!outputDone) {
                if (!inputDone) {
                    val inIdx = encoder.dequeueInputBuffer(10_000)
                    if (inIdx >= 0) {
                        val inBuf = encoder.getInputBuffer(inIdx)!!
                        inBuf.clear()
                        val remaining = bytes.size - readOffset
                        if (remaining <= 0) {
                            val ptsUs = totalFramesFed * 1_000_000L / sampleRate
                            encoder.queueInputBuffer(inIdx, 0, 0, ptsUs, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
                            inputDone = true
                        } else {
                            val toWrite = min(remaining, inBuf.capacity())
                            inBuf.put(bytes, readOffset, toWrite)
                            val ptsUs = totalFramesFed * 1_000_000L / sampleRate
                            readOffset += toWrite
                            totalFramesFed += toWrite / bytesPerFrame.coerceAtLeast(1)
                            encoder.queueInputBuffer(inIdx, 0, toWrite, ptsUs, 0)
                        }
                    }
                }

                val outIdx = encoder.dequeueOutputBuffer(info, 10_000)
                when {
                    outIdx == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> {
                        if (!muxerStarted) {
                            muxerTrack = muxer.addTrack(encoder.outputFormat)
                            muxer.start()
                            muxerStarted = true
                        }
                    }
                    outIdx >= 0 -> {
                        val outBuf = encoder.getOutputBuffer(outIdx)
                        if (outBuf != null && info.size > 0 && (info.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG) == 0 && muxerStarted) {
                            outBuf.position(info.offset)
                            outBuf.limit(info.offset + info.size)
                            muxer.writeSampleData(muxerTrack, outBuf, info)
                        }
                        encoder.releaseOutputBuffer(outIdx, false)
                        if (info.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) {
                            outputDone = true
                        }
                    }
                }
            }
        } finally {
            try {
                encoder.stop()
                encoder.release()
            } catch (_: Exception) {
            }
            try {
                if (muxerStarted) muxer.stop()
                muxer.release()
            } catch (_: Exception) {
            }
        }
    }

    suspend fun saveToPublicMediaStore(
        context: Context,
        sourceFile: File,
        isVideo: Boolean,
        mimeType: String
    ): Uri? = withContext(Dispatchers.IO) {
        try {
            val resolver = context.contentResolver
            val collection = if (isVideo) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                } else {
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                }
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                } else {
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                }
            }

            val values = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, sourceFile.name)
                put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val relDir = if (isVideo) "${Environment.DIRECTORY_MOVIES}/ObsidianAV" else "${Environment.DIRECTORY_MUSIC}/ObsidianAV"
                    put(MediaStore.MediaColumns.RELATIVE_PATH, relDir)
                    put(MediaStore.MediaColumns.IS_PENDING, 1)
                }
            }
            val destUri = resolver.insert(collection, values) ?: return@withContext null
            resolver.openOutputStream(destUri)?.use { out ->
                sourceFile.inputStream().use { input ->
                    input.copyTo(out)
                }
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                values.clear()
                values.put(MediaStore.MediaColumns.IS_PENDING, 0)
                resolver.update(destUri, values, null, null)
            }
            destUri
        } catch (_: Exception) {
            null
        }
    }

    fun formatSize(bytes: Long): String {
        return when {
            bytes >= 1_073_741_824L -> String.format(Locale.US, "%.2f GB", bytes / 1_073_741_824.0)
            bytes >= 1_048_576L -> String.format(Locale.US, "%.1f MB", bytes / 1_048_576.0)
            bytes >= 1024L -> "${bytes / 1024} KB"
            bytes > 0L -> "$bytes B"
            else -> "--"
        }
    }
}
