package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.model.StudioRoutes
import com.example.ui.components.EngineSettingsDialog
import com.example.ui.components.StudioBottomNavBar
import com.example.ui.components.StudioNavigationRail
import com.example.ui.components.StudioTopBar
import com.example.ui.screens.AudioScreen
import com.example.ui.screens.ExtractScreen
import com.example.ui.screens.HubScreen
import com.example.ui.screens.VideoScreen
import com.example.ui.theme.ObsidianAvTheme
import com.example.ui.theme.ObsidianBackground
import com.example.viewmodel.StudioViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val darkScrim = ObsidianBackground.toArgb()
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(darkScrim),
            navigationBarStyle = SystemBarStyle.dark(darkScrim)
        )
        setContent {
            ObsidianAvTheme {
                ObsidianAvApp()
            }
        }
    }
}

@Composable
fun ObsidianAvApp(
    viewModel: StudioViewModel = viewModel()
) {
    val context = LocalContext.current
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route ?: StudioRoutes.HUB

    val hubState by viewModel.hubState.collectAsStateWithLifecycle()
    val extractState by viewModel.extractState.collectAsStateWithLifecycle()
    val audioState by viewModel.audioState.collectAsStateWithLifecycle()
    val videoState by viewModel.videoState.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.toastMessages.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    val videoGalleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            viewModel.loadPickedMedia(uri, preferredRoute = StudioRoutes.VIDEO)
        }
    }

    val albumArtLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            viewModel.updateAlbumArt(uri)
        }
    }

    val audioFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            viewModel.loadPickedMedia(uri, preferredRoute = StudioRoutes.AUDIO)
        }
    }

    val anyMediaFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            viewModel.loadPickedMedia(uri)
        }
    }

    fun navigateToTab(route: String) {
        if (route == currentRoute) return
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBackground)
    ) {
        val useNavigationRail = maxWidth >= 600.dp

        Row(modifier = Modifier.fillMaxSize()) {
            if (useNavigationRail) {
                StudioNavigationRail(
                    currentRoute = currentRoute,
                    onNavigate = ::navigateToTab
                )
            }

            Scaffold(
                modifier = Modifier.weight(1f),
                containerColor = ObsidianBackground,
                snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                topBar = {
                    StudioTopBar(
                        currentRoute = currentRoute,
                        onSettingsClick = { viewModel.toggleEngineSettingsSheet(true) }
                    )
                },
                bottomBar = {
                    if (!useNavigationRail) {
                        StudioBottomNavBar(
                            currentRoute = currentRoute,
                            onNavigate = ::navigateToTab
                        )
                    }
                }
            ) { innerPadding ->
                NavHost(
                    navController = navController,
                    startDestination = StudioRoutes.HUB,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    composable(StudioRoutes.HUB) {
                        HubScreen(
                            uiState = hubState,
                            onNavigate = ::navigateToTab,
                            onPickFromGallery = {
                                videoGalleryLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                                )
                            },
                            onPickAudioFile = {
                                audioFileLauncher.launch(arrayOf("audio/*"))
                            },
                            onPickMediaFile = {
                                anyMediaFileLauncher.launch(arrayOf("video/*", "audio/*"))
                            },
                            onClearCompletedQueue = viewModel::clearCompletedQueue,
                            onToggleQueuePlay = viewModel::toggleQueueItemPlayback,
                            onShareQueueItem = { item ->
                                val shareUri = viewModel.getShareUriForFile(item.file)
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = item.mimeType
                                    putExtra(Intent.EXTRA_STREAM, shareUri)
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                context.startActivity(
                                    Intent.createChooser(shareIntent, "分享导出文件")
                                )
                            }
                        )
                    }

                    composable(StudioRoutes.EXTRACT) {
                        ExtractScreen(
                            uiState = extractState,
                            onPickFromGallery = {
                                videoGalleryLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                                )
                            },
                            onPickFromFiles = {
                                anyMediaFileLauncher.launch(arrayOf("video/*"))
                            },
                            onTogglePreviewPlay = viewModel::toggleExtractPreviewPlay,
                            onSeek = viewModel::seekExtractPosition,
                            onSelectFormat = viewModel::selectExtractFormat,
                            onSelectBitrateMode = viewModel::selectExtractBitrateMode,
                            onSelectBitrateKbps = viewModel::selectExtractBitrateKbps,
                            onSelectTrack = viewModel::selectExtractTrack,
                            onToggleAdvanced = viewModel::toggleExtractAdvanced,
                            onSelectMethod = viewModel::selectExtractMethod,
                            onAdjustFadeIn = viewModel::adjustExtractFadeIn,
                            onAdjustFadeOut = viewModel::adjustExtractFadeOut,
                            onRunExtraction = viewModel::runExtraction,
                            onOpenInAudioEditor = {
                                viewModel.openLastExtractedInAudioEditor()
                                navigateToTab(StudioRoutes.AUDIO)
                            }
                        )
                    }

                    composable(StudioRoutes.AUDIO) {
                        AudioScreen(
                            uiState = audioState,
                            onPickAudioFile = {
                                audioFileLauncher.launch(arrayOf("audio/*"))
                            },
                            onTogglePlay = viewModel::toggleAudioPlay,
                            onToggleLoop = viewModel::toggleAudioLoop,
                            onJumpToIn = viewModel::jumpAudioToInPoint,
                            onJumpToOut = viewModel::jumpAudioToOutPoint,
                            onToggleZoom = viewModel::toggleAudioZoom,
                            onUpdateInPoint = viewModel::updateAudioInPoint,
                            onUpdateOutPoint = viewModel::updateAudioOutPoint,
                            onUpdatePlayhead = viewModel::updateAudioPlayhead,
                            onSplitInAtPlayhead = viewModel::splitAudioInAtPlayhead,
                            onResetSelection = viewModel::resetAudioSelection,
                            onUpdateFadeIn = viewModel::updateAudioFadeIn,
                            onUpdateFadeOut = viewModel::updateAudioFadeOut,
                            onUpdateGain = viewModel::updateAudioGain,
                            onPickAlbumArt = {
                                albumArtLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            onUpdateId3 = viewModel::updateId3Fields,
                            onSelectOutputFormat = viewModel::selectAudioOutputFormat,
                            onSelectOutputBitrate = viewModel::selectAudioOutputBitrate,
                            onExportMaster = viewModel::exportAudioMaster
                        )
                    }

                    composable(StudioRoutes.VIDEO) {
                        VideoScreen(
                            uiState = videoState,
                            onPickFromGallery = {
                                videoGalleryLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                                )
                            },
                            onPickFromFiles = {
                                anyMediaFileLauncher.launch(arrayOf("video/*"))
                            },
                            onTogglePlay = viewModel::toggleVideoPlay,
                            onPlaybackTick = viewModel::onVideoPlaybackPositionTick,
                            onPlaybackCompleted = viewModel::onVideoPlaybackCompleted,
                            onStepFrames = viewModel::stepVideoFrames,
                            onSeekSeconds = viewModel::seekVideoSeconds,
                            onUpdateInPoint = viewModel::updateVideoInPoint,
                            onUpdateOutPoint = viewModel::updateVideoOutPoint,
                            onUpdatePlayhead = viewModel::updateVideoPlayhead,
                            onSetInAtPlayhead = viewModel::setVideoInAtPlayhead,
                            onToggleGridOverlay = viewModel::toggleVideoGridOverlay,
                            onToggleFullscreen = viewModel::toggleVideoFullscreen,
                            onToggleIncludeAudio = viewModel::toggleVideoIncludeAudio,
                            onExportCutVideo = viewModel::exportCutVideo
                        )
                    }
                }
            }
        }
    }

    if (hubState.showEngineSettingsSheet) {
        EngineSettingsDialog(
            hardwareAccelName = hubState.hardwareAccelName,
            exportsCount = hubState.pipelineQueue.size,
            exportsMb = hubState.studioExportsMb,
            onClearCache = {
                viewModel.clearCache()
                viewModel.toggleEngineSettingsSheet(false)
            },
            onDismiss = { viewModel.toggleEngineSettingsSheet(false) }
        )
    }
}
