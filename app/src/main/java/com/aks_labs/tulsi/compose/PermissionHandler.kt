package com.aks_labs.tulsi.compose

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import com.aks_labs.tulsi.MainActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.aks_labs.tulsi.MainActivity.Companion.mainViewModel
import com.aks_labs.tulsi.R
import com.aks_labs.tulsi.compose.dialogs.ExplanationDialog
import com.aks_labs.tulsi.compose.dialogs.getDefaultShapeSpacerForPosition
import com.aks_labs.tulsi.datastore.Permissions
import com.aks_labs.tulsi.helpers.RowPosition

@Composable
fun PermissionHandler(
    continueToApp: MutableState<Boolean>
) {
    Scaffold { innerPadding ->
        val isLandscape by rememberDeviceOrientation()

        val safeDrawingPadding = if (isLandscape) {
            val safeDrawing = WindowInsets.safeDrawing.asPaddingValues()

            val layoutDirection = LocalLayoutDirection.current
            val left = safeDrawing.calculateStartPadding(layoutDirection)
            val right = safeDrawing.calculateEndPadding(layoutDirection)

            Pair(left, right)
        } else {
            Pair(0.dp, 0.dp)
        }

        Row(
            modifier = Modifier
				.padding(
					safeDrawingPadding.first,
					innerPadding.calculateTopPadding() + 8.dp,
					safeDrawingPadding.second,
					innerPadding.calculateBottomPadding()
				)
				.fillMaxSize()
				.background(MaterialTheme.colorScheme.background)
				.padding(16.dp, 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            val context = LocalContext.current

            var onGrantPermissionClicked by remember { mutableStateOf({}) }

            val showPermDeniedDialog = remember { mutableStateOf(false) }
            val showExplanationDialog = remember { mutableStateOf(false) }
            var whyButtonExplanation by rememberSaveable { mutableStateOf("") }

            if (showPermDeniedDialog.value) {
                PermissionDeniedDialog(
                    showDialog = showPermDeniedDialog,
                    showExplanationDialog = showExplanationDialog
                ) {
                    onGrantPermissionClicked()
                }
            }

            if (showExplanationDialog.value) {
                ExplanationDialog(
                    title = "权限说明",
                    explanation = whyButtonExplanation,
                    showDialog = showExplanationDialog,
                    showPreviousDialog = showPermDeniedDialog
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f),
                verticalArrangement = Arrangement.SpaceEvenly,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "权限",
                    fontSize = TextUnit(22f, TextUnitType.Sp),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(32.dp))

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(
                        space = 4.dp,
                        alignment = Alignment.Top
                    ),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .weight(1f)
                ) {
                    item {
                        PreferencesSeparatorText(
                            text = "权限"
                        )
                    }

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        item {
                            val readMediaImageLauncher = rememberLauncherForActivityResult(
                                contract = ActivityResultContracts.RequestPermission()
                            ) { granted ->
                                mainViewModel.onPermissionResult(
                                    permission = Manifest.permission.READ_MEDIA_IMAGES,
                                    isGranted = granted
                                )

                                showPermDeniedDialog.value = !granted
                            }

                            val appDetailsLauncher = rememberLauncherForActivityResult(
                                contract = ActivityResultContracts.StartActivityForResult()
                            ) { _ ->
                                val granted =
                                    context.checkSelfPermission(Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED

                                mainViewModel.onPermissionResult(
                                    permission = Manifest.permission.READ_MEDIA_IMAGES,
                                    isGranted = granted
                                )

                                showPermDeniedDialog.value = !granted
                            }

                            PermissionButton(
                                name = "读取图片",
                                description = "允许 Tulsi 图库发现设备上的照片",
                                position = RowPosition.Top,
                                granted = !mainViewModel.permissionQueue.contains(Manifest.permission.READ_MEDIA_IMAGES)
                            ) {
                                readMediaImageLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)

                                whyButtonExplanation = Explanations.READ_MEDIA

                                onGrantPermissionClicked = {
                                    val intent = Intent(
                                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                        Uri.fromParts("package", context.packageName, null)
                                    )

                                    appDetailsLauncher.launch(intent)
                                }
                            }
                        }

                        item {
                            val readMediaVideoLauncher = rememberLauncherForActivityResult(
                                contract = ActivityResultContracts.RequestPermission()
                            ) { granted ->
                                mainViewModel.onPermissionResult(
                                    permission = Manifest.permission.READ_MEDIA_VIDEO,
                                    isGranted = granted
                                )

                                showPermDeniedDialog.value = !granted
                            }

                            val appDetailsLauncher = rememberLauncherForActivityResult(
                                contract = ActivityResultContracts.StartActivityForResult()
                            ) { _ ->
                                val granted =
                                    context.checkSelfPermission(Manifest.permission.READ_MEDIA_VIDEO) == PackageManager.PERMISSION_GRANTED

                                mainViewModel.onPermissionResult(
                                    permission = Manifest.permission.READ_MEDIA_VIDEO,
                                    isGranted = granted
                                )

                                showPermDeniedDialog.value = !granted
                            }

                            PermissionButton(
                                name = "读取视频",
                                description = "允许 Tulsi 图库发现设备上的视频",
                                position = RowPosition.Middle,
                                granted = !mainViewModel.permissionQueue.contains(Manifest.permission.READ_MEDIA_VIDEO)
                            ) {
                                readMediaVideoLauncher.launch(Manifest.permission.READ_MEDIA_VIDEO)

                                whyButtonExplanation = Explanations.READ_MEDIA

                                onGrantPermissionClicked = {
                                    val intent = Intent(
                                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                        Uri.fromParts("package", context.packageName, null)
                                    )

                                    appDetailsLauncher.launch(intent)
                                }
                            }
                        }

                        item {
                            val postNotificationsLauncher = rememberLauncherForActivityResult(
                                contract = ActivityResultContracts.RequestPermission()
                            ) { granted ->
                                mainViewModel.onPermissionResult(
                                    permission = Manifest.permission.POST_NOTIFICATIONS,
                                    isGranted = granted
                                )

                                showPermDeniedDialog.value = !granted
                            }

                            val appDetailsLauncher = rememberLauncherForActivityResult(
                                contract = ActivityResultContracts.StartActivityForResult()
                            ) { _ ->
                                val granted =
                                    context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED

                                mainViewModel.onPermissionResult(
                                    permission = Manifest.permission.POST_NOTIFICATIONS,
                                    isGranted = granted
                                )

                                showPermDeniedDialog.value = !granted
                            }

                            PermissionButton(
                                name = "发送通知",
                                description = "允许 Tulsi 图库显示文字识别进度通知",
                                position = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) RowPosition.Middle else RowPosition.Bottom,
                                granted = !mainViewModel.permissionQueue.contains(Manifest.permission.POST_NOTIFICATIONS)
                            ) {
                                postNotificationsLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)

                                whyButtonExplanation = Explanations.POST_NOTIFICATIONS

                                onGrantPermissionClicked = {
                                    val intent = Intent(
                                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                        Uri.fromParts("package", context.packageName, null)
                                    )

                                    appDetailsLauncher.launch(intent)
                                }
                            }
                        }
                    } else {
                        item {
                            val readExternalStorageLauncher = rememberLauncherForActivityResult(
                                contract = ActivityResultContracts.RequestPermission()
                            ) { granted ->
                                mainViewModel.onPermissionResult(
                                    permission = Manifest.permission.READ_EXTERNAL_STORAGE,
                                    isGranted = granted
                                )

                                showPermDeniedDialog.value = !granted
                            }

                            val appDetailsLauncher = rememberLauncherForActivityResult(
                                contract = ActivityResultContracts.StartActivityForResult()
                            ) { _ ->
                                val granted =
                                    context.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED

                                mainViewModel.onPermissionResult(
                                    permission = Manifest.permission.READ_EXTERNAL_STORAGE,
                                    isGranted = granted
                                )

                                showPermDeniedDialog.value = !granted
                            }

                            PermissionButton(
                                name = "读取外部存储",
                                description = "允许 Tulsi 图库发现设备上的照片和视频",
                                position = RowPosition.Top,
                                granted = !mainViewModel.permissionQueue.contains(Manifest.permission.READ_EXTERNAL_STORAGE)
                            ) {
                                readExternalStorageLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)

                                whyButtonExplanation = Explanations.READ_MEDIA

                                onGrantPermissionClicked = {
                                    val intent = Intent(
                                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                        Uri.fromParts("package", context.packageName, null)
                                    )

                                    appDetailsLauncher.launch(intent)
                                }
                            }
                        }
                    }

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        item {
                            val manageMediaLauncher = rememberLauncherForActivityResult(
                                contract = ActivityResultContracts.StartActivityForResult()
                            ) { _ ->
                                val granted = MediaStore.canManageMedia(context)

                                mainViewModel.onPermissionResult(
                                    permission = Manifest.permission.MANAGE_MEDIA,
                                    isGranted = granted
                                )

                                mainViewModel.settings.Permissions.setIsMediaManager(granted)
                            }

                            PermissionButton(
                                name = "管理媒体",
                                description = "可选权限，用于加快删除与回收站操作",
                                position = RowPosition.Bottom,
                                granted = !mainViewModel.permissionQueue.contains(Manifest.permission.MANAGE_MEDIA)
                            ) {
                                val intent = Intent(Settings.ACTION_REQUEST_MANAGE_MEDIA)
                                manageMediaLauncher.launch(intent)

                                whyButtonExplanation = Explanations.MANAGE_MEDIA

                                onGrantPermissionClicked = {
                                    manageMediaLauncher.launch(intent)
                                }
                            }
                        }
                    }


                }

                if (!isLandscape) {
                    Box(
                        modifier = Modifier
							.fillMaxWidth(1f)
							.height(64.dp)
                    ) {
                        FilledTonalButton(
                            onClick = {
                                (context as Activity).finish()
                            },
                            modifier = Modifier
                                .align(Alignment.CenterStart)
                        ) {
                            Text(text = "退出")
                        }

                        Button(
                            onClick = {
                                Log.d("PermissionHandler", "Continue button clicked - transitioning to main app")
                                continueToApp.value = true

                                // Trigger OCR initialization directly as a backup
                                (context as? MainActivity)?.triggerOcrInitialization()
                            },
                            enabled = mainViewModel.checkCanPass(),
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                        ) {
                            Text(text = "继续")
                        }
                    }
                }
            }

            if (isLandscape) {
                Column(
                    modifier = Modifier
						.weight(1f)
						.fillMaxHeight(1f),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Button(
                        onClick = {
                            Log.d("PermissionHandler", "Continue button clicked (landscape) - transitioning to main app")
                            continueToApp.value = true

                            // Trigger OCR initialization directly as a backup
                            (context as? MainActivity)?.triggerOcrInitialization()
                        },
                        enabled = mainViewModel.checkCanPass()
                    ) {
                        Text(text = "继续")
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    FilledTonalButton(
                        onClick = {
                            (context as Activity).finish()
                        }
                    ) {
                        Text(text = "退出")
                    }
                }
            }
        }
    }
}

@Composable
fun PermissionButton(
    name: String,
    description: String,
    position: RowPosition,
    granted: Boolean = false,
    onClick: () -> Unit
) {
    val (shape, _) = getDefaultShapeSpacerForPosition(position, 32.dp)

    val clickModifier = if (!granted) Modifier.clickable { onClick() } else Modifier

    Box(
        modifier = Modifier
			.fillMaxWidth(1f)
			.height(104.dp)
			.clip(shape)
			.background(if (!granted) MaterialTheme.colorScheme.surfaceContainer else MaterialTheme.colorScheme.primary)
			.then(clickModifier)
			.padding(16.dp, 12.dp)
    ) {
        Column(
            modifier = Modifier
				.wrapContentWidth()
				.fillMaxHeight(1f)
				.align(Alignment.CenterStart),
            verticalArrangement = Arrangement.SpaceEvenly,
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = name,
                color = if (!granted) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onPrimary,
                fontSize = TextUnit(18f, TextUnitType.Sp),
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = description,
                color = if (!granted) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onPrimary.copy(
                    alpha = 0.8f
                ),
                fontSize = TextUnit(14f, TextUnitType.Sp),
                fontWeight = FontWeight.Bold
            )
        }

        if (granted) {
            Column(
                modifier = Modifier
					.fillMaxHeight(1f)
					.width(32.dp)
					.background(MaterialTheme.colorScheme.primary)
					.align(Alignment.CenterEnd),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.file_is_selected_foreground),
                    contentDescription = name,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier
                        .size(28.dp)
                )
            }
        }
    }
}

@Composable
fun PermissionDeniedDialog(
    showDialog: MutableState<Boolean>,
    showExplanationDialog: MutableState<Boolean>,
    onGrantPermissionClicked: () -> Unit
) {
    Dialog(
        onDismissRequest = {
            showDialog.value = false
        },
        properties = DialogProperties(
            dismissOnClickOutside = true,
            dismissOnBackPress = true
        ),
    ) {
        Column(
            modifier = Modifier
				.wrapContentSize()
				.clip(RoundedCornerShape(32.dp))
				.background(MaterialTheme.colorScheme.background)
				.padding(16.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "必要权限",
                fontSize = TextUnit(18f, TextUnitType.Sp),
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.wrapContentSize()
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "此权限是应用正常运行所必需的",
                fontSize = TextUnit(14f, TextUnitType.Sp),
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.wrapContentSize()
            )

            Spacer(modifier = Modifier.height(32.dp))

            FullWidthDialogButton(
                text = "授予权限",
                color = MaterialTheme.colorScheme.primary,
                textColor = MaterialTheme.colorScheme.onPrimary,
                position = RowPosition.Top
            ) {
                showDialog.value = false
                onGrantPermissionClicked()
            }

            FullWidthDialogButton(
                text = "为什么？",
                color = MaterialTheme.colorScheme.surfaceContainer,
                textColor = MaterialTheme.colorScheme.onSurface,
                position = RowPosition.Middle
            ) {
                showExplanationDialog.value = true
            }

            FullWidthDialogButton(
                text = "关闭",
                color = MaterialTheme.colorScheme.surfaceContainer,
                textColor = MaterialTheme.colorScheme.onSurface,
                position = RowPosition.Bottom
            ) {
                showDialog.value = false
            }
        }
    }
}

@Composable
fun FullWidthDialogButton(
    text: String,
    color: Color,
    textColor: Color,
    position: RowPosition,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    val (shape, spacerHeight) = getDefaultShapeSpacerForPosition(
        position,
        cornerRadius = 24.dp,
        innerCornerRadius = 4.dp
    )

    Row(
        modifier = Modifier
			.fillMaxWidth(1f)
			.height(48.dp)
			.clip(shape)
			.background(
				if (enabled) color else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
			)
			.then(
				if (enabled) {
					Modifier.clickable {
						onClick()
					}
				} else Modifier
			)
			.padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = TextUnit(14f, TextUnitType.Sp)
        )
    }

    Spacer(modifier = Modifier.height(spacerHeight))
}

private object Explanations {
    const val READ_MEDIA =
        "需要此权限才能找到设备上的图片和视频。Tulsi 图库对读取哪些文件有严格限制，绝不会分享或滥用这些信息。"
    const val MANAGE_MEDIA =
        "This permission is optional, but is highly recommended. Manage Media permission allows Tulsi Gallery to use Android's Content Resolver API to trash/delete/move/copy media, which makes the process much smoother and more interoperable with other apps."
    const val POST_NOTIFICATIONS =
        "此权限允许 Tulsi 图库显示文字识别进度通知。你可以查看图片文字提取进度，并通过通知控制处理。"
}



