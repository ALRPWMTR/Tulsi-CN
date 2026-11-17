package com.aks_labs.tulsi.compose.settings

import android.content.Intent
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.aks_labs.tulsi.LocalNavController
import com.aks_labs.tulsi.R
import com.aks_labs.tulsi.compose.PreferencesRow
import com.aks_labs.tulsi.compose.dialogs.ExplanationDialog
import com.aks_labs.tulsi.compose.dialogs.FeatureNotAvailableDialog
import com.aks_labs.tulsi.helpers.MultiScreenViewType
import com.aks_labs.tulsi.helpers.RowPosition
import com.aks_labs.tulsi.ui.theme.GalleryTitleFont
import com.aks_labs.tulsi.ui.theme.TulsiTitleFont

private const val TAG = "ABOUT_PAGE"

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun AboutPage(popBackStack: () -> Unit) {
    val context = LocalContext.current
    val showVersionInfoDialog = remember { mutableStateOf(false) }
    val showOriginalRepoDialog = remember { mutableStateOf(false) }

    VersionInfoDialog(
        showDialog = showVersionInfoDialog,
        changelog = stringResource(id = R.string.changelog)
    )

    OriginalRepositoryDialog(
        showDialog = showOriginalRepoDialog
    )

    Column(
        modifier = Modifier
            .fillMaxSize(1f)
            .padding(8.dp)
            .background(MaterialTheme.colorScheme.background),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(1f)
                .wrapContentHeight(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(1f)
                    .padding(0.dp, 24.dp, 0.dp, 0.dp)
                    .wrapContentHeight(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                IconButton(
                    onClick = {
                        popBackStack()
                    }
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.back_arrow),
                        contentDescription = "返回上一页",
                        tint = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier
                            .size(24.dp)
                    )
                }
            }

            GlideImage(
                model = R.drawable.tulsi,
                contentDescription = "应用图标",
                // Removed color filter to show the PNG image with its original colors
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .size(128.dp)
            )


            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = " Tulsi ",
                    fontFamily = TulsiTitleFont,
                    fontStyle = FontStyle.Italic,
                    fontWeight = FontWeight.Normal,
                    fontSize = TextUnit(35f, TextUnitType.Sp),
                    letterSpacing = TextUnit(0.9f, TextUnitType.Sp),
                    textAlign = TextAlign.Center,
                    style = LocalTextStyle.current.copy(
                        color = MaterialTheme.colorScheme.onBackground
                    )
                )
                Text(
                    text = "图库",
                    fontFamily = TulsiTitleFont,
                    fontStyle = FontStyle.Italic,
                    fontWeight = FontWeight.Normal,
                    fontSize = TextUnit(31f, TextUnitType.Sp),
                    letterSpacing = TextUnit(1.3f, TextUnitType.Sp),
                    textAlign = TextAlign.Center,
                    style = LocalTextStyle.current.copy(
                        color = MaterialTheme.colorScheme.onBackground
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Column(
            modifier = Modifier
                .fillMaxSize(1f)
                .padding(8.dp)
                .background(MaterialTheme.colorScheme.background),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            PreferencesRow(
                title = "开发者",
                summary = "AKS-Labs",
                iconResID = R.drawable.code,
                position = RowPosition.Top
            ) {
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    setData("https://github.com/AKS-Labs/tulsi".toUri())
                }

                context.startActivity(intent)
            }

            val showPrivacyPolicy = remember { mutableStateOf(false) }
            if (showPrivacyPolicy.value) {
                ExplanationDialog(
                    title = "隐私政策",
                    explanation = "没有。你的数据不会离开这台设备。没有 AI 拿它训练，也没有算法搜集信息。Tulsi 图库现在是、以后也一直是以隐私为核心的图库应用。",
                    showDialog = showPrivacyPolicy,
                )
            }

            PreferencesRow(
                title = "隐私政策",
                summary = "我们真的不会使用你的数据",
                iconResID = R.drawable.privacy_policy,
                position = RowPosition.Middle
            ) {
                showPrivacyPolicy.value = true
            }

            val showNotImplDialog = remember { mutableStateOf(false) }
            if (showNotImplDialog.value) {
                FeatureNotAvailableDialog(showDialog = showNotImplDialog)
            }

            PreferencesRow(
                title = "支持与捐赠",
                summary = "帮我让应用活下去",
                iconResID = R.drawable.donation,
                position = RowPosition.Middle
            ) {
                showNotImplDialog.value = true
            }

            val versionName = try {
                context.packageManager.getPackageInfo(context.packageName, 0).versionName
            } catch (e: Throwable) {
                Log.e(TAG, e.toString())
                "无法获取版本号"
            }
            PreferencesRow(
                title = "版本信息",
                summary = versionName,
                iconResID = R.drawable.info,
                position = RowPosition.Middle,
            ) {
                showVersionInfoDialog.value = true
            }

            PreferencesRow(
                title = "源代码",
                summary = "查看或下载完整源代码",
                iconResID = R.drawable.code_blocks,
                position = RowPosition.Middle
            ) {
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    setData("https://github.com/AKS-Labs/Tulsi".toUri())
                }
                context.startActivity(intent)
            }

            PreferencesRow(
                title = "原始仓库",
                summary = "基于 kaii-lb 的 LavenderPhotos",
                iconResID = R.drawable.code_blocks,
                position = RowPosition.Bottom
            ) {
                showOriginalRepoDialog.value = true
            }
        }
    }
}

@Composable
fun VersionInfoDialog(
    showDialog: MutableState<Boolean>,
    changelog: String
) {
    if (showDialog.value) {
        AlertDialog(
            onDismissRequest = {
                showDialog.value = false
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDialog.value = false
                    }
                ) {
                    Text(
                        text = "关闭",
                        fontSize = TextUnit(14f, TextUnitType.Sp),
                    )
                }
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.SpaceEvenly,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "更新日志",
                        fontSize = TextUnit(18f, TextUnitType.Sp),
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    LazyColumn(
                        modifier = Modifier
                            .height(320.dp),
                        verticalArrangement = Arrangement.SpaceEvenly,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        item {
                            Text(
                                text = changelog,
                                fontSize = TextUnit(14f, TextUnitType.Sp),
                            )
                        }
                    }
                }
            }
        )
    }
}

@Composable
fun OriginalRepositoryDialog(
    showDialog: MutableState<Boolean>
) {
    if (showDialog.value) {
        AlertDialog(
            onDismissRequest = {
                showDialog.value = false
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDialog.value = false
                    }
                ) {
                    Text(
                        text = "关闭",
                        fontSize = TextUnit(14f, TextUnitType.Sp),
                    )
                }
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.SpaceEvenly,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "原始项目",
                        fontSize = TextUnit(18f, TextUnitType.Sp),
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Tulsi 是 LavenderPhotos 的分支，后者是一款以 GNU 通用公共许可证 v3.0 发布的开源图库应用。",
                        fontSize = TextUnit(14f, TextUnitType.Sp),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "原作者：kaii-lb",
                        fontSize = TextUnit(14f, TextUnitType.Sp),
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    val context = LocalContext.current

                    Row(
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(
                            onClick = {
                                val intent = Intent(Intent.ACTION_VIEW).apply {
                                    setData("https://github.com/kaii-lb/LavenderPhotos".toUri())
                                }
                                context.startActivity(intent)
                            }
                        ) {
                            Text(
                                text = "原始仓库",
                                fontSize = TextUnit(14f, TextUnitType.Sp),
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Button(
                            onClick = {
                                val intent = Intent(Intent.ACTION_VIEW).apply {
                                    setData("https://github.com/AKS-Labs/Tulsi".toUri())
                                }
                                context.startActivity(intent)
                            }
                        ) {
                            Text(
                                text = "Tulsi 源代码",
                                fontSize = TextUnit(14f, TextUnitType.Sp),
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "许可证：GNU 通用公共许可证 v3.0",
                        fontSize = TextUnit(14f, TextUnitType.Sp),
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }
            }
        )
    }
}
