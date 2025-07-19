package com.quicktrim.ai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import com.google.accompanist.navigation.material.ModalBottomSheetLayout
import com.google.accompanist.navigation.material.bottomSheet
import com.google.accompanist.navigation.material.rememberBottomSheetNavigator
import com.quicktrim.MainViewModel
import com.quicktrim.ai.ui.NavArgs
import com.quicktrim.ai.ui.Routes
import com.quicktrim.ai.ui.common.QuickTrimAppBar
import com.quicktrim.ai.ui.edit.EditScreen
import com.quicktrim.ai.ui.edit.EditViewModel
import com.quicktrim.ai.ui.fillerwords.UpdateFillerWordsScreen
import com.quicktrim.ai.ui.theme.QuicktrimandroidTheme
import com.quicktrim.ai.ui.upload.UploadScreen
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    val mainViewModel by viewModels<MainViewModel>()
    val editViewModel by viewModels<EditViewModel>()

    @OptIn(ExperimentalMaterialNavigationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            QuicktrimandroidTheme {
                val bottomSheetNavigator = rememberBottomSheetNavigator()
                val navController = rememberNavController(bottomSheetNavigator)

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        QuickTrimAppBar(
                            navController = navController,
                            editViewModel = editViewModel
                        )
                    }
                ) { innerPadding ->
                    ModalBottomSheetLayout(
                        bottomSheetNavigator = bottomSheetNavigator
                    ) {
                        NavHost(
                            navController = navController,
                            startDestination = Routes.Upload.path,
                            modifier = Modifier.padding(innerPadding)
                        ) {
                            composable(
                                route = Routes.Upload.path
                            ) {
                                UploadScreen(
                                    navController = navController,
                                    mainViewModel = mainViewModel
                                )
                            }

                            composable(
                                route = Routes.Edit.path,
                                arguments = listOf(
                                    navArgument(NavArgs.PROJECT_ID) { type = NavType.LongType }
                                )
                            ) {
                                val projectId = it.arguments?.getLong(NavArgs.PROJECT_ID)
                                requireNotNull(projectId)
                                EditScreen(
                                    projectId = projectId,
                                    navController = navController,
                                    editViewModel = editViewModel
                                )
                            }

                            bottomSheet(
                                route = Routes.UpdateFillerWords.path
                            ) {
                                UpdateFillerWordsScreen(
                                    editViewModel = editViewModel,
                                    navigator = navController
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}