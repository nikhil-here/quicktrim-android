package com.quicktrim.ai.ui.common

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.quicktrim.MainViewModel
import com.quicktrim.ai.R
import com.quicktrim.ai.ui.Routes
import com.quicktrim.ai.ui.toAppBarTitle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickTrimAppBar(
    modifier: Modifier = Modifier,
    navController: NavController,
    mainViewModel: MainViewModel
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val canGoBack = backStackEntry?.destination?.route in listOf(
        Routes.Edit.path,
        Routes.Export.path
    )
    val showExportIcon = backStackEntry?.destination?.route in listOf(Routes.Edit.path)

    TopAppBar(
        modifier = modifier,
        title = {
            Text(
                text = backStackEntry?.destination?.route.toAppBarTitle()
            )
        },
        navigationIcon = {
            if (canGoBack) {
                IconButton(
                    onClick = { navController.popBackStack() }
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.cd_navigation_cta)
                    )
                }
            }
        },
        actions = {
            if (showExportIcon) {
                IconButton(
                    onClick = {
                        mainViewModel.export()
                    }
                ) {
                    Icon(
                        imageVector = Icons.Filled.Done,
                        contentDescription = stringResource(R.string.cd_export)
                    )
                }

            }

        }
    )
}