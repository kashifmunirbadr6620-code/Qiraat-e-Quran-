package com.example.ui

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.TextDecrease
import androidx.compose.material.icons.filled.TextIncrease
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.screens.DirectoryScreen
import com.example.ui.screens.EditorScreen
import com.example.ui.screens.ReaderScreen
import com.example.ui.screens.SurahListScreen
import com.example.ui.theme.DarkThemeMode
import com.example.ui.theme.QiraatAsharahTheme

object QiraatRoutes {
    const val SURAHS = "surahs"
    const val READER = "reader"
    const val DIRECTORY = "directory"
    const val EDITOR = "editor"
}

private data class NavDestinationItem(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val testTag: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QiraatApp(viewModel: QiraatViewModel = viewModel()) {
    val userPrefs by viewModel.userPrefs.collectAsStateWithLifecycle()
    val surahsState by viewModel.surahsUiState.collectAsStateWithLifecycle()
    val readerState by viewModel.readerUiState.collectAsStateWithLifecycle()
    val bookmarks by viewModel.bookmarksFlow.collectAsStateWithLifecycle()
    val allDiffs by viewModel.allDiffsFlow.collectAsStateWithLifecycle()
    val editorState by viewModel.editorState.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(Unit) {
        viewModel.snackbarEvents.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route ?: QiraatRoutes.SURAHS

    val destinations = remember {
        listOf(
            NavDestinationItem(
                route = QiraatRoutes.SURAHS,
                label = "سورتیں",
                icon = Icons.Default.MenuBook,
                testTag = "nav_surahs"
            ),
            NavDestinationItem(
                route = QiraatRoutes.READER,
                label = "تلاوت",
                icon = Icons.AutoMirrored.Filled.MenuBook,
                testTag = "nav_reader"
            ),
            NavDestinationItem(
                route = QiraatRoutes.DIRECTORY,
                label = "قراء و بُک مارکس",
                icon = Icons.Default.Bookmarks,
                testTag = "nav_directory"
            ),
            NavDestinationItem(
                route = QiraatRoutes.EDITOR,
                label = "ایڈیٹر",
                icon = Icons.Default.EditNote,
                testTag = "nav_editor"
            )
        )
    }

    fun navigateTopLevel(route: String) {
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    QiraatAsharahTheme(darkThemeMode = userPrefs.darkThemeMode) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val useNavRail = maxWidth >= 600.dp

                Scaffold(
                    contentWindowInsets = WindowInsets.safeDrawing,
                    snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                    topBar = {
                        TopAppBar(
                            title = {
                                val titleText = when (currentRoute) {
                                    QiraatRoutes.READER -> "سورة ${readerState.surahName}"
                                    QiraatRoutes.DIRECTORY -> "قراء عشرہ و بُک مارکس"
                                    QiraatRoutes.EDITOR -> "قراءاتِ عشرہ ایڈیٹر"
                                    else -> "QiraatAsharah"
                                }
                                Text(
                                    text = titleText,
                                    fontWeight = FontWeight.Bold
                                )
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                titleContentColor = MaterialTheme.colorScheme.onPrimary,
                                actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            actions = {
                                if (currentRoute == QiraatRoutes.READER) {
                                    IconButton(
                                        onClick = { viewModel.adjustFontSize(-2f) },
                                        modifier = Modifier.testTag("decrease_font_button")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.TextDecrease,
                                            contentDescription = "فونٹ چھوٹا کریں"
                                        )
                                    }
                                    IconButton(
                                        onClick = { viewModel.adjustFontSize(2f) },
                                        modifier = Modifier.testTag("increase_font_button")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.TextIncrease,
                                            contentDescription = "فونٹ بڑا کریں"
                                        )
                                    }
                                }
                                IconButton(
                                    onClick = { viewModel.cycleThemeMode() },
                                    modifier = Modifier.testTag("theme_toggle_button")
                                ) {
                                    val iconToShow = if (userPrefs.darkThemeMode == DarkThemeMode.DARK) {
                                        Icons.Default.LightMode
                                    } else {
                                        Icons.Default.DarkMode
                                    }
                                    Icon(
                                        imageVector = iconToShow,
                                        contentDescription = "تھیم تبدیل کریں"
                                    )
                                }
                            }
                        )
                    },
                    bottomBar = {
                        if (!useNavRail) {
                            NavigationBar {
                                destinations.forEach { dest ->
                                    NavigationBarItem(
                                        selected = currentRoute == dest.route,
                                        onClick = { navigateTopLevel(dest.route) },
                                        icon = {
                                            Icon(
                                                imageVector = dest.icon,
                                                contentDescription = dest.label
                                            )
                                        },
                                        label = { Text(dest.label) },
                                        modifier = Modifier.testTag(dest.testTag)
                                    )
                                }
                            }
                        }
                    }
                ) { innerPadding ->
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        if (useNavRail) {
                            NavigationRail {
                                destinations.forEach { dest ->
                                    NavigationRailItem(
                                        selected = currentRoute == dest.route,
                                        onClick = { navigateTopLevel(dest.route) },
                                        icon = {
                                            Icon(
                                                imageVector = dest.icon,
                                                contentDescription = dest.label
                                            )
                                        },
                                        label = { Text(dest.label) },
                                        modifier = Modifier.testTag(dest.testTag)
                                    )
                                }
                            }
                        }

                        NavHost(
                            navController = navController,
                            startDestination = QiraatRoutes.SURAHS,
                            modifier = Modifier.weight(1f)
                        ) {
                            composable(QiraatRoutes.SURAHS) {
                                SurahListScreen(
                                    uiState = surahsState,
                                    onSearchQueryChange = viewModel::onSearchQueryChanged,
                                    onRefreshCatalog = viewModel::refreshCatalog,
                                    onOpenSurah = { surahId, highlightAyah ->
                                        viewModel.loadSurahInReader(surahId, highlightAyah)
                                        navigateTopLevel(QiraatRoutes.READER)
                                    }
                                )
                            }

                            composable(QiraatRoutes.READER) {
                                ReaderScreen(
                                    uiState = readerState,
                                    onSelectRiwayah = viewModel::selectRiwayah,
                                    onToggleCompare = viewModel::toggleCompareMode,
                                    onBookmarkAyah = viewModel::toggleBookmark,
                                    onNavigateSurah = { targetId ->
                                        viewModel.loadSurahInReader(targetId)
                                    },
                                    onRetry = {
                                        viewModel.loadSurahInReader(readerState.surahId)
                                    },
                                    onEditDiffInEditor = { entry ->
                                        viewModel.loadEditorSurah(
                                            surahId = entry.surahId,
                                            initialAyah = entry.ayahHafs,
                                            prefillWord = entry.wordTitle
                                        )
                                        navigateTopLevel(QiraatRoutes.EDITOR)
                                    },
                                    findWordDiff = viewModel::findWordDiffs
                                )
                            }

                            composable(QiraatRoutes.DIRECTORY) {
                                DirectoryScreen(
                                    bookmarks = bookmarks,
                                    onReadWithRiwayah = { riwayahCode ->
                                        viewModel.loadSurahInReader(
                                            surahId = readerState.surahId,
                                            overrideRiwayah = riwayahCode
                                        )
                                        navigateTopLevel(QiraatRoutes.READER)
                                    },
                                    onOpenBookmark = { bm ->
                                        viewModel.loadSurahInReader(
                                            surahId = bm.surahId,
                                            highlightAyah = bm.ayahNumber,
                                            overrideRiwayah = bm.riwayahCode
                                        )
                                        navigateTopLevel(QiraatRoutes.READER)
                                    },
                                    onDeleteBookmark = viewModel::deleteBookmark
                                )
                            }

                            composable(QiraatRoutes.EDITOR) {
                                EditorScreen(
                                    uiState = editorState,
                                    surahs = surahsState.surahs,
                                    savedDiffs = allDiffs,
                                    onSelectSurah = { viewModel.loadEditorSurah(it) },
                                    onSelectAyah = viewModel::selectEditorAyah,
                                    onSelectWord = viewModel::selectEditorWord,
                                    onUpdateFields = viewModel::updateEditorFields,
                                    onToggleReaderChip = viewModel::toggleEditorReaderChip,
                                    onAddPendingVariant = viewModel::addPendingVariant,
                                    onRemovePendingVariant = viewModel::removePendingVariant,
                                    onSaveEntry = viewModel::saveEditorEntry,
                                    onDeleteSavedEntry = viewModel::deleteSavedDiff,
                                    onBuildExportJson = viewModel::buildExportJson
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
