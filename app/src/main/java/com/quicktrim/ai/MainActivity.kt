package com.quicktrim.ai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import com.google.accompanist.navigation.material.ModalBottomSheetLayout
import com.google.accompanist.navigation.material.bottomSheet
import com.google.accompanist.navigation.material.rememberBottomSheetNavigator
import com.quicktrim.MainScreenSideEffect
import com.quicktrim.MainViewModel
import com.quicktrim.ai.ui.NavArgs
import com.quicktrim.ai.ui.Routes
import com.quicktrim.ai.ui.common.QTDialog
import com.quicktrim.ai.ui.common.QTModalDrawerSheet
import com.quicktrim.ai.ui.common.QuickTrimAppBar
import com.quicktrim.ai.ui.edit.EditScreen
import com.quicktrim.ai.ui.edit.EditViewModel
import com.quicktrim.ai.ui.fillerwords.UpdateFillerWordsScreen
import com.quicktrim.ai.ui.login.LoginScreen
import com.quicktrim.ai.ui.theme.QuicktrimandroidTheme
import com.quicktrim.ai.ui.upload.UploadScreen
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


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
                val drawerState = rememberDrawerState(DrawerValue.Closed)
                val bottomSheetNavigator = rememberBottomSheetNavigator()
                val navController = rememberNavController(bottomSheetNavigator)
                val scope = rememberCoroutineScope()
                var showLogoutDialog by remember { mutableStateOf(false) }
                val activity = LocalActivity.current

                LaunchedEffect(true) {
                    mainViewModel.sideEffect.collectLatest { effect ->
                        when (effect) {
                            is MainScreenSideEffect.Navigate -> {
                                navController.navigate(effect.path)
                            }
                        }
                    }
                }

                LaunchedEffect(true) {
                    mainViewModel.checkAndLaunchLoginFlow(activity = this@MainActivity)
                }

                ModalNavigationDrawer(
                    drawerContent = {
                        QTModalDrawerSheet(
                            modifier = Modifier.fillMaxWidth(),
                            mainViewModel = mainViewModel,
                            onLogoutClick = {
                                scope.launch {
                                    drawerState.close()
                                    showLogoutDialog = true
                                }
                            }
                        )
                    },
                    drawerState = drawerState
                ) {
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        topBar = {
                            QuickTrimAppBar(
                                navController = navController,
                                editViewModel = editViewModel,
                                mainViewModel = mainViewModel,
                                onNavigationDrawerMenuClick = {
                                    scope.launch {
                                        drawerState.open()
                                    }
                                }
                            )
                        }
                    ) { innerPadding ->
                        ModalBottomSheetLayout(
                            bottomSheetNavigator = bottomSheetNavigator
                        ) {
                            NavHost(
                                navController = navController,
                                startDestination = Routes.Login.path,
                                modifier = Modifier.padding(innerPadding)
                            ) {
                                composable(
                                    route = Routes.Login.path
                                ) {
                                    LoginScreen(
                                        modifier = Modifier,
                                        navController = navController,
                                    )
                                }

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
                                }
                            }
                        }
                    }

                    //Logout Dialog
                    if (showLogoutDialog) {
                        QTDialog(
                            heroTitle = stringResource(R.string.cta_logout_hero_title),
                            message = stringResource(R.string.cta_logout_message),
                            onClick = {
                                showLogoutDialog = false
                                mainViewModel.logout(this)
                            },
                            onDismissRequest = {
                                showLogoutDialog = false
                            },
                            ctaTitle = stringResource(R.string.cta_logout_cta_title),
                            showDismissCta = false
                        )
                    }
                }
            }
        }
    }
}