package com.example

import com.example.media.MediaEngine
import com.example.model.AudioEditorUiState
import com.example.model.ExtractUiState
import com.example.model.VideoEditorUiState
import com.example.util.formatSecondsLabel
import com.example.util.formatTimecodeCentis
import com.example.util.formatTimecodeMillis
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun timeFormatters_formatValuesAccurately() {
    assertEquals("00:00.000", formatTimecodeMillis(0L))
    assertEquals("01:05.250", formatTimecodeMillis(65_250L))
    assertEquals("01:05.25", formatTimecodeCentis(65_250L))
    assertEquals("1.50s", formatSecondsLabel(1_500L))
  }

  @Test
  fun mediaEngine_formatSize_formatsUnitsCorrectly() {
    assertEquals("--", MediaEngine.formatSize(0L))
    assertEquals("512 B", MediaEngine.formatSize(512L))
    assertEquals("2 KB", MediaEngine.formatSize(2048L))
    assertEquals("1.5 MB", MediaEngine.formatSize(1_572_864L))
  }

  @Test
  fun initialUiStates_startCleanWithoutDemoData() {
    val extract = ExtractUiState()
    assertFalse(extract.hasMedia)
    assertEquals("", extract.fileName)
    assertTrue(extract.detectedTracks.isEmpty())
    assertEquals("0.0", extract.estimatedSizeMb)

    val audio = AudioEditorUiState()
    assertFalse(audio.hasMedia)
    assertEquals("", audio.sourceFileName)
    assertTrue(audio.waveformBars.isEmpty())
    assertEquals("", audio.tagTitle)

    val video = VideoEditorUiState()
    assertFalse(video.hasMedia)
    assertEquals("", video.fileName)
    assertTrue(video.filmstripBitmaps.isEmpty())
    assertEquals(0f, video.estimatedSizeMb, 0.001f)
  }
}
