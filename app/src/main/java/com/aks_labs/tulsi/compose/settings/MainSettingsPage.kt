package com.aks_labs.tulsi.compose.settings

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import com.aks_labs.tulsi.LocalNavController
import com.aks_labs.tulsi.R
import com.aks_labs.tulsi.compose.PreferencesRow
import com.aks_labs.tulsi.helpers.MultiScreenViewType
import com.aks_labs.tulsi.helpers.RowPosition

@Composable
fun MainSettingsPage() {
    Scaffold(
        topBar = {
            MainSettingsTopBar()
        },
        contentWindowInsets = WindowInsets.systemBars
    ) { innerPadding ->
        val navController = LocalNavController.current

        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                PreferencesRow(
                    title = "通用",
                    summary = "应用偏好与个性化设置",
                    iconResID = R.drawable.settings,
                    position = RowPosition.Top,
                    showBackground = false,
                    titleTextSize = 20f,
                    modifier = Modifier
                    	.padding(0.dp, 6.dp)
                ) {
                    navController.navigate(MultiScreenViewType.SettingsGeneralView.name)
                }
            }

            item {
                PreferencesRow(
                    title = "隐私与安全",
                    summary = "精细掌控你的数据",
                    iconResID = R.drawable.privacy_policy,
                    position = RowPosition.Middle,
                    showBackground = false,
                    titleTextSize = 20f,
                    modifier = Modifier
                    	.padding(0.dp, 6.dp)
                ) {
                    navController.navigate(MultiScreenViewType.PrivacyAndSecurity.name)
                }
            }

            item {
                PreferencesRow(
                    title = "外观",
                    summary = "更改应用外观",
                    iconResID = R.drawable.palette,
                    position = RowPosition.Middle,
                    showBackground = false,
                    titleTextSize = 20f,
                    modifier = Modifier
                    	.padding(0.dp, 6.dp)
                ) {
                    navController.navigate(MultiScreenViewType.SettingsLookAndFeelView.name)
                }
            }

            item {
                PreferencesRow(
                    title = "内存与存储",
                    summary = "性能与空间选项",
                    iconResID = R.drawable.privacy_policy,
                    position = RowPosition.Middle,
                    showBackground = false,
                    titleTextSize = 20f,
                    modifier = Modifier
                    	.padding(0.dp, 6.dp)
                ) {
                    navController.navigate(MultiScreenViewType.SettingsMemoryAndStorageView.name)
                }
            }

            item {
                PreferencesRow(
                    title = "文字识别语言模型",
                    summary = "配置文字提取语言",
                    iconResID = R.drawable.ocr,
                    position = RowPosition.Middle,
                    showBackground = false,
                    titleTextSize = 20f,
                    modifier = Modifier
                    	.padding(0.dp, 6.dp)
                ) {
                    navController.navigate(MultiScreenViewType.OcrLanguageModelsView.name)
                }
            }

            item {
                PreferencesRow(
                    title = "调试",
                    summary = "问题调试工具",
                    iconResID = R.drawable.memory,
                    position = RowPosition.Bottom,
                    showBackground = false,
                    titleTextSize = 20f,
                    modifier = Modifier
                    	.padding(0.dp, 6.dp)
                ) {
                    navController.navigate(MultiScreenViewType.SettingsDebuggingView.name)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainSettingsTopBar() {
    val navController = LocalNavController.current

    val localConfig = LocalConfiguration.current
    var isLandscape by remember { mutableStateOf(localConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) }

    LaunchedEffect(localConfig) {
        isLandscape = localConfig.orientation == Configuration.ORIENTATION_LANDSCAPE
    }

    TopAppBar(
        title = {
            Text(
                text = "设置",
                fontSize = TextUnit(22f, TextUnitType.Sp)
            )
        },
        navigationIcon = {
            IconButton(
                onClick = {
                    navController.popBackStack()
                },
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.back_arrow),
                    contentDescription = "返回上一页",
                    tint = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier
                        .size(24.dp)
                )
            }
        },
        scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(),
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background
        )
    )
}


