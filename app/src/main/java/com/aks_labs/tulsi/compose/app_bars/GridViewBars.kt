package com.aks_labs.tulsi.compose.app_bars

import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastDistinctBy
import androidx.compose.ui.util.fastFilter
import androidx.compose.ui.util.fastMap
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aks_labs.tulsi.MainActivity.Companion.applicationDatabase
import com.aks_labs.tulsi.MainActivity.Companion.mainViewModel
import com.aks_labs.tulsi.R
import com.aks_labs.tulsi.compose.dialogs.AlbumPathsDialog
import com.aks_labs.tulsi.compose.dialogs.ConfirmationDialog
import com.aks_labs.tulsi.compose.dialogs.ConfirmationDialogWithBody
import com.aks_labs.tulsi.compose.dialogs.LoadingDialog
import com.aks_labs.tulsi.compose.grids.MoveCopyAlbumListView
import com.aks_labs.tulsi.datastore.AlbumInfo
import com.aks_labs.tulsi.datastore.AlbumsList
import com.aks_labs.tulsi.datastore.BottomBarTab
import com.aks_labs.tulsi.datastore.Permissions
import com.aks_labs.tulsi.helpers.EncryptionManager
import com.aks_labs.tulsi.helpers.GetDirectoryPermissionAndRun
import com.aks_labs.tulsi.helpers.GetPermissionAndRun
import com.aks_labs.tulsi.helpers.appRestoredFilesDir
import com.aks_labs.tulsi.helpers.getParentFromPath
import com.aks_labs.tulsi.helpers.moveImageOutOfLockedFolder
import com.aks_labs.tulsi.helpers.permanentlyDeletePhotoList
import com.aks_labs.tulsi.helpers.permanentlyDeleteSecureFolderImageList
import com.aks_labs.tulsi.helpers.setTrashedOnPhotoList
import com.aks_labs.tulsi.helpers.shareMultipleSecuredImages
import com.aks_labs.tulsi.mediastore.MediaStoreData
import com.aks_labs.tulsi.mediastore.MediaType
import com.aks_labs.tulsi.mediastore.content_provider.LavenderContentProvider
import com.aks_labs.tulsi.mediastore.content_provider.LavenderMediaColumns
import com.aks_labs.tulsi.mediastore.getIv
import com.aks_labs.tulsi.mediastore.getOriginalPath
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

private const val TAG = "GRID_VIEW_BARS"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SingleAlbumViewTopBar(
    albumInfo: AlbumInfo?,
    selectedItemsList: SnapshotStateList<MediaStoreData>,
    showDialog: MutableState<Boolean>,
    currentView: MutableState<BottomBarTab>,
    onBackClick: () -> Unit
) {
    val show by remember {
        derivedStateOf {
            selectedItemsList.isNotEmpty()
        }
    }

    AnimatedContent(
        targetState = show,
        transitionSpec = {
            getAppBarContentTransition(show)
        },
        label = "SingleAlbumViewTopBarAnimatedContent"
    ) { target ->
        if (!target) {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                ),
                navigationIcon = {
                    IconButton(
                        onClick = { onBackClick() },
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
                title = {
                    Text(
                        text = albumInfo?.name ?: "相册",
                        fontSize = TextUnit(18f, TextUnitType.Sp),
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .width(160.dp)
                    )
                },
                actions = {
                    var showPathsDialog by remember { mutableStateOf(false) }

                    if (showPathsDialog && albumInfo != null) {
                        AlbumPathsDialog(
                            albumInfo = albumInfo,
                            onConfirm = { selectedPaths: List<String> ->
                                mainViewModel.settings.AlbumsList.editInAlbumsList(
                                    albumInfo = albumInfo,
                                    newInfo = albumInfo.copy(
                                        paths = selectedPaths
                                    )
                                )
                            },
                            onDismiss = {
                                showPathsDialog = false
                            }
                        )
                    }

                    IconButton(
                        onClick = {
                            showPathsDialog = true
                        },
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.add),
                            contentDescription = "向相册添加文件夹",
                            tint = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier
                                .size(24.dp)
                        )
                    }

                    IconButton(
                        onClick = {
                            showDialog.value = true
                        },
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.settings),
                            contentDescription = "显示相册视图的更多选项",
                            tint = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier
                                .size(24.dp)
                        )
                    }
                },
                scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
            )
        } else {
            IsSelectingTopBar(
                selectedItemsList = selectedItemsList,
                currentView = currentView
            )
        }
    }
}

@Composable
fun SingleAlbumViewBottomBar(
    albumInfo: AlbumInfo,
    selectedItemsList: SnapshotStateList<MediaStoreData>
) {
    IsSelectingBottomAppBar {
        val context = LocalContext.current
        val coroutineScope = rememberCoroutineScope()

        val selectedItemsWithoutSection by remember {
            derivedStateOf {
                selectedItemsList.filter {
                    it.type != MediaType.Section && it != MediaStoreData()
                }
            }
        }

        BottomAppBarItem(
            text = "分享",
            iconResId = R.drawable.share,
            action = {
                coroutineScope.launch {
                    val hasVideos = selectedItemsWithoutSection.any {
                        it.type == MediaType.Video
                    }

                    val intent = Intent().apply {
                        action = Intent.ACTION_SEND_MULTIPLE
                        type = if (hasVideos) "video/*" else "images/*"
                    }

                    val fileUris = ArrayList<Uri>()
                    selectedItemsWithoutSection.forEach {
                        fileUris.add(it.uri)
                    }

                    intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, fileUris)

                    context.startActivity(Intent.createChooser(intent, null))
                }
            }
        )

        val show = remember { mutableStateOf(false) }
        var isMoving by remember { mutableStateOf(false) }
        MoveCopyAlbumListView(
            show = show,
            selectedItemsList = selectedItemsList,
            isMoving = isMoving,
            groupedMedia = null,
            insetsPadding = WindowInsets.statusBars
        )

        BottomAppBarItem(
            text = "移动",
            iconResId = R.drawable.cut,
            enabled = !albumInfo.isCustomAlbum,
            action = {
                isMoving = true
                show.value = true
            }
        )

        BottomAppBarItem(
            text = "复制",
            iconResId = R.drawable.copy,
            action = {
                isMoving = false
                show.value = true
            }
        )

        val showDeleteDialog = remember { mutableStateOf(false) }
        val runTrashAction = remember { mutableStateOf(false) }

        GetPermissionAndRun(
            uris = selectedItemsWithoutSection.map { it.uri },
            shouldRun = runTrashAction,
            onGranted = {
                mainViewModel.launch(Dispatchers.IO) {
                    setTrashedOnPhotoList(
                        context = context,
                        list = selectedItemsWithoutSection.map { Pair(it.uri, it.absolutePath) },
                        trashed = true
                    )

                    selectedItemsList.clear()
                }
            }
        )

        val confirmToDelete by mainViewModel.settings.Permissions.getConfirmToDelete()
            .collectAsStateWithLifecycle(initialValue = true)
        if (!albumInfo.isCustomAlbum) {
            BottomAppBarItem(
                text = "删除",
                iconResId = R.drawable.delete,
                cornerRadius = 16.dp,
                action = {
                    if (confirmToDelete) showDeleteDialog.value = true
                    else runTrashAction.value = true
                },
                dialogComposable = {
                    ConfirmationDialog(
                        showDialog = showDeleteDialog,
                        dialogTitle = "将所选项目移至回收站？",
                        confirmButtonLabel = "删除"
                    ) {
                        runTrashAction.value = true
                    }
                }
            )
        } else {
            BottomAppBarItem(
                text = "移除",
                iconResId = R.drawable.delete,
                cornerRadius = 16.dp,
                action = {
                    if (confirmToDelete) showDeleteDialog.value = true
                    else runTrashAction.value = true
                },
                dialogComposable = {
                    ConfirmationDialog(
                        showDialog = showDeleteDialog,
                        dialogTitle = "从相册中移除这些项目？",
                        confirmButtonLabel = "移除"
                    ) {
                        mainViewModel.launch(Dispatchers.IO) {
                            selectedItemsWithoutSection.forEach { item ->
                                context.contentResolver.delete(
                                    LavenderContentProvider.CONTENT_URI,
                                    "${LavenderMediaColumns.ID} = ?",
                                    arrayOf(item.id.toString())
                                )
                            }
                            selectedItemsList.clear()
                        }
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrashedPhotoGridViewTopBar(
    selectedItemsList: SnapshotStateList<MediaStoreData>,
    groupedMedia: List<MediaStoreData>,
    currentView: MutableState<BottomBarTab>,
    onBackClick: () -> Unit
) {
    val showDialog = remember { mutableStateOf(false) }

    val runEmptyTrashAction = remember { mutableStateOf(false) }
    val context = LocalContext.current

    LaunchedEffect(runEmptyTrashAction.value) {
        if (runEmptyTrashAction.value) {
            permanentlyDeletePhotoList(
                context = context,
                list = groupedMedia.filter { it.type != MediaType.Section }.map { it.uri }
            )

            runEmptyTrashAction.value = false
        }
    }

    ConfirmationDialogWithBody(
        showDialog = showDialog,
        dialogTitle = "清空回收站？",
        dialogBody = "这将删除回收站中的所有项目，且无法撤销",
        confirmButtonLabel = "清空"
    ) {
        runEmptyTrashAction.value = true
    }

    val show by remember {
        derivedStateOf {
            selectedItemsList.isNotEmpty()
        }
    }

    AnimatedContent(
        targetState = show,
        transitionSpec = {
            getAppBarContentTransition(show)
        },
        label = "TrashedPhotoGridViewTopBarAnimatedContent"
    ) { target ->
        if (!target) {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                ),
                navigationIcon = {
                    IconButton(
                        onClick = { onBackClick() },
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
                title = {
                    Text(
                        text = "回收站",
                        fontSize = TextUnit(18f, TextUnitType.Sp),
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .width(160.dp)
                    )
                },
                actions = {
                    IconButton(
                        onClick = {
                            showDialog.value = true
                        },
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.trash),
                            contentDescription = "清空回收站",
                            tint = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier
                                .size(24.dp)
                        )
                    }
                }
            )
        } else {
            IsSelectingTopBar(selectedItemsList = selectedItemsList, currentView = currentView)
        }
    }
}

@Composable
fun TrashedPhotoGridViewBottomBar(
    selectedItemsList: SnapshotStateList<MediaStoreData>,
) {
    IsSelectingBottomAppBar {

        val context = LocalContext.current
        val coroutineScope = rememberCoroutineScope()

        val selectedItemsWithoutSection by remember {
            derivedStateOf {
                selectedItemsList.filter {
                    it.type != MediaType.Section && it != MediaStoreData()
                }
            }
        }

        BottomAppBarItem(
            text = "分享",
            iconResId = R.drawable.share,
            action = {
                coroutineScope.launch {
                    val hasVideos = selectedItemsWithoutSection.any {
                        it.type == MediaType.Video
                    }

                    val intent = Intent().apply {
                        action = Intent.ACTION_SEND_MULTIPLE
                        type = if (hasVideos) "video/*" else "images/*"
                    }

                    val fileUris = ArrayList<Uri>()
                    selectedItemsWithoutSection.forEach {
                        fileUris.add(it.uri)
                    }

                    intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, fileUris)

                    context.startActivity(Intent.createChooser(intent, null))
                }
            }
        )

        val showRestoreDialog = remember { mutableStateOf(false) }
        val runRestoreAction = remember { mutableStateOf(false) }

        GetPermissionAndRun(
            uris = selectedItemsWithoutSection.map { it.uri },
            shouldRun = runRestoreAction,
            onGranted = {
                mainViewModel.launch(Dispatchers.IO) {
                    setTrashedOnPhotoList(
                        context = context,
                        list = selectedItemsWithoutSection.map { Pair(it.uri, it.absolutePath) },
                        trashed = false
                    )

                    selectedItemsList.clear()
                }
            }
        )

        BottomAppBarItem(
            text = "恢复",
            iconResId = R.drawable.untrash,
            cornerRadius = 16.dp,
            action = {
                showRestoreDialog.value = true
            },
            dialogComposable = {
                ConfirmationDialog(
                    showDialog = showRestoreDialog,
                    dialogTitle = "恢复这些项目？",
                    confirmButtonLabel = "恢复"
                ) {
                    runRestoreAction.value = true
                }
            }
        )

        val showPermaDeleteDialog = remember { mutableStateOf(false) }
        val runPermaDeleteAction = remember { mutableStateOf(false) }

        LaunchedEffect(runPermaDeleteAction.value) {
            if (runPermaDeleteAction.value) {
                permanentlyDeletePhotoList(
                    context,
                    selectedItemsWithoutSection.map { it.uri }
                )

                selectedItemsList.clear()

                runPermaDeleteAction.value = false
            }
        }

        BottomAppBarItem(
            text = "删除",
            iconResId = R.drawable.delete,
            cornerRadius = 16.dp,
            action = {
                if (selectedItemsWithoutSection.isNotEmpty()) {
                    showPermaDeleteDialog.value = true
                }
            },
            dialogComposable = {
                ConfirmationDialogWithBody(
                    showDialog = showPermaDeleteDialog,
                    dialogTitle = "永久删除这些项目？",
                    dialogBody = "此操作无法撤销！",
                    confirmButtonLabel = "删除"
                ) {
                    runPermaDeleteAction.value = true
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecureFolderViewTopAppBar(
    selectedItemsList: SnapshotStateList<MediaStoreData>,
    currentView: MutableState<BottomBarTab>,
    onBackClicked: () -> Unit
) {
    val show by remember {
        derivedStateOf {
            selectedItemsList.isNotEmpty()
        }
    }

    AnimatedContent(
        targetState = show,
        transitionSpec = {
            getAppBarContentTransition(show)
        },
        label = "SecureFolderGridViewBottomBarAnimatedContent"
    ) { target ->
        if (!target) {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                ),
                navigationIcon = {
                    IconButton(
                        onClick = { onBackClicked() },
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
                title = {
                    Text(
                        text = "安全文件夹",
                        fontSize = TextUnit(18f, TextUnitType.Sp),
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .width(160.dp)
                    )
                }
            )
        } else {
            IsSelectingTopBar(selectedItemsList = selectedItemsList, currentView = currentView)
        }
    }
}

@Composable
fun SecureFolderViewBottomAppBar(
    selectedItemsList: SnapshotStateList<MediaStoreData>,
    groupedMedia: MutableState<List<MediaStoreData>>,
    isGettingPermissions: MutableState<Boolean>
) {
    IsSelectingBottomAppBar {
        val context = LocalContext.current
        val coroutineScope = rememberCoroutineScope()

        val selectedItemsWithoutSection by remember {
            derivedStateOf {
                selectedItemsList.filter {
                    it.type != MediaType.Section
                }
            }
        }

        var showLoadingDialog by remember { mutableStateOf(false) }
        var loadingDialogTitle by remember { mutableStateOf("正在解密文件") }

        if (showLoadingDialog) {
            LoadingDialog(
                title = loadingDialogTitle,
                body = "正在处理媒体，请稍候"
            )
        }

        BottomAppBarItem(
            text = "分享",
            iconResId = R.drawable.share,
            action = {
                coroutineScope.launch(Dispatchers.IO) {
                    async {
                        loadingDialogTitle = "正在解密文件"
                        showLoadingDialog = true

                        val cachedPaths = emptyList<Pair<String, MediaType>>().toMutableList()

                        selectedItemsWithoutSection.forEach { item ->
                            val iv = item.bytes?.getIv()
                            if (iv == null) {
                                Log.e(TAG, "IV for ${item.displayName} was null, aborting decrypt")
                                return@async
                            }

                            val originalFile = File(item.absolutePath)
                            val cachedFile = File(context.cacheDir, item.displayName)

                            EncryptionManager.decryptInputStream(
                                inputStream = originalFile.inputStream(),
                                outputStream = cachedFile.outputStream(),
                                iv = iv
                            )

                            cachedFile.deleteOnExit()
                            cachedPaths.add(Pair(cachedFile.absolutePath, item.type))
                        }

                        showLoadingDialog = false

                        shareMultipleSecuredImages(paths = cachedPaths, context = context)
                    }.await()
                }
            }
        )

        val showRestoreDialog = remember { mutableStateOf(false) }
        val runRestoreAction = remember { mutableStateOf(false) }
        val restoredFilesDir = remember { context.appRestoredFilesDir }

        GetDirectoryPermissionAndRun(
            absoluteDirPaths =
                selectedItemsWithoutSection.fastMap {
                    it.bytes?.getOriginalPath()?.getParentFromPath() ?: restoredFilesDir
                }.fastDistinctBy {
                    it
                },
            shouldRun = runRestoreAction,
            onGranted = { grantedList ->
                mainViewModel.launch(Dispatchers.IO) {
                    val hasPermission = selectedItemsWithoutSection.fastFilter { selected ->
                        (selected.bytes?.getOriginalPath()?.getParentFromPath()
                            ?: restoredFilesDir) in grantedList
                    }

                    val newList = groupedMedia.value.toMutableList()

                    moveImageOutOfLockedFolder(
                        list = hasPermission,
                        context = context
                    ) {
                        showLoadingDialog = false
                        isGettingPermissions.value = false
                    }

                    newList.removeAll(selectedItemsList.fastFilter { selected ->
                        selected.section in hasPermission.fastMap { it.section }
                    }.toSet())

                    selectedItemsList.clear()
                    groupedMedia.value = newList
                }
            },
            onRejected = {
                isGettingPermissions.value = false
                showLoadingDialog = false
            }
        )

        BottomAppBarItem(
            text = "恢复",
            iconResId = R.drawable.unlock,
            cornerRadius = 16.dp,
            action = {
                showRestoreDialog.value = true
            },
            dialogComposable = {
                ConfirmationDialog(
                    showDialog = showRestoreDialog,
                    dialogTitle = "恢复这些项目？",
                    confirmButtonLabel = "恢复"
                ) {
                    loadingDialogTitle = "正在恢复文件"
                    showLoadingDialog = true

                    isGettingPermissions.value = true
                    runRestoreAction.value = true
                }
            }
        )

        val showPermaDeleteDialog = remember { mutableStateOf(false) }
        val runPermaDeleteAction = remember { mutableStateOf(false) }

        LaunchedEffect(runPermaDeleteAction.value) {
            if (runPermaDeleteAction.value) {
                loadingDialogTitle = "正在删除文件"
                showLoadingDialog = true

                mainViewModel.launch(Dispatchers.IO) {
                    val newList = groupedMedia.value.toMutableList()

                    permanentlyDeleteSecureFolderImageList(
                        list = selectedItemsWithoutSection.map { it.absolutePath },
                        context = context
                    )


                    selectedItemsWithoutSection.forEach {
                        newList.remove(it)
                    }

                    newList.filter {
                        it.type == MediaType.Section
                    }.forEach { item ->
                        // remove sections which no longer have any children
                        val filtered = newList.filter { newItem ->
                            newItem.getLastModifiedDay() == item.getLastModifiedDay()
                        }

                        if (filtered.size == 1) newList.remove(item)
                    }

                    selectedItemsList.clear()
                    groupedMedia.value = newList

                    showLoadingDialog = false
                    runPermaDeleteAction.value = false
                }
            }
        }

        BottomAppBarItem(
            text = "删除",
            iconResId = R.drawable.delete,
            cornerRadius = 16.dp,
            action = {
                showPermaDeleteDialog.value = true
            },
            dialogComposable = {
                ConfirmationDialogWithBody(
                    showDialog = showPermaDeleteDialog,
                    dialogTitle = "永久删除这些项目？",
                    dialogBody = "此操作无法撤销！",
                    confirmButtonLabel = "删除"
                ) {
                    runPermaDeleteAction.value = true
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavouritesViewTopAppBar(
    selectedItemsList: SnapshotStateList<MediaStoreData>,
    currentView: MutableState<BottomBarTab>,
    onBackClick: () -> Unit
) {
    val show by remember {
        derivedStateOf {
            selectedItemsList.isNotEmpty()
        }
    }

    AnimatedContent(
        targetState = show,
        transitionSpec = {
            getAppBarContentTransition(show)
        },
        label = "FavouritesGridViewTopBarAnimatedContent"
    ) { target ->
        if (!target) {
            TopAppBar(
                navigationIcon = {
                    IconButton(
                        onClick = { onBackClick() },
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
                title = {
                    Text(
                        text = "收藏",
                        fontSize = TextUnit(18f, TextUnitType.Sp),
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .width(160.dp)
                    )
                }
            )
        } else {
            IsSelectingTopBar(selectedItemsList = selectedItemsList, currentView = currentView)
        }
    }
}

@Composable
fun FavouritesViewBottomAppBar(
    selectedItemsList: SnapshotStateList<MediaStoreData>,
    groupedMedia: MutableState<List<MediaStoreData>>
) {
    IsSelectingBottomAppBar {
        val context = LocalContext.current
        val coroutineScope = rememberCoroutineScope()
        val dao = applicationDatabase.favouritedItemEntityDao()

        val selectedItemsWithoutSection by remember {
            derivedStateOf {
                selectedItemsList.filter {
                    it.type != MediaType.Section
                }
            }
        }

        BottomAppBarItem(
            text = "分享",
            iconResId = R.drawable.share,
            action = {
                coroutineScope.launch {
                    val hasVideos = selectedItemsWithoutSection.any {
                        it.type == MediaType.Video
                    }

                    val intent = Intent().apply {
                        action = Intent.ACTION_SEND_MULTIPLE
                        type = if (hasVideos) "video/*" else "images/*"
                    }

                    val fileUris = ArrayList<Uri>()
                    selectedItemsWithoutSection.forEach {
                        fileUris.add(it.uri)
                    }

                    intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, fileUris)

                    context.startActivity(Intent.createChooser(intent, null))
                }
            }
        )

        val show = remember { mutableStateOf(false) }
        MoveCopyAlbumListView(
            show = show,
            selectedItemsList = selectedItemsList,
            isMoving = false,
            groupedMedia = null,
            insetsPadding = WindowInsets.statusBars
        )

        BottomAppBarItem(
            text = "复制",
            iconResId = R.drawable.copy,
            action = {
                show.value = true
            }
        )

        val showUnFavDialog = remember { mutableStateOf(false) }
        BottomAppBarItem(
            text = "移除",
            iconResId = R.drawable.unfavourite,
            cornerRadius = 16.dp,
            action = {
                showUnFavDialog.value = true
            },
            dialogComposable = {
                ConfirmationDialog(
                    showDialog = showUnFavDialog,
                    dialogTitle = "从收藏中移除所选项目？",
                    confirmButtonLabel = "移除"
                ) {
                    coroutineScope.launch {
                        withContext(Dispatchers.IO) {
                            val newList = groupedMedia.value.toMutableList()
                            selectedItemsWithoutSection.forEach { item ->
                                dao.deleteEntityById(item.id)
                                newList.remove(item)
                            }

                            groupedMedia.value.filter {
                                it.type == MediaType.Section
                            }.forEach {
                                val filtered = newList.filter { new ->
                                    new.getLastModifiedDay() == it.getLastModifiedDay()
                                }

                                if (filtered.size == 1) newList.remove(it)
                            }

                            selectedItemsList.clear()
                            groupedMedia.value = newList
                        }
                    }
                }
            }
        )

        val showDeleteDialog = remember { mutableStateOf(false) }
        val runTrashAction = remember { mutableStateOf(false) }
        val confirmToDelete by mainViewModel.settings.Permissions.getConfirmToDelete()
            .collectAsStateWithLifecycle(initialValue = true)

        GetPermissionAndRun(
            uris = selectedItemsWithoutSection.map { it.uri },
            shouldRun = runTrashAction,
            onGranted = {
                mainViewModel.launch(Dispatchers.IO) {
                    setTrashedOnPhotoList(
                        context = context,
                        list = selectedItemsWithoutSection.map { Pair(it.uri, it.absolutePath) },
                        trashed = true
                    )

                    selectedItemsList.clear()
                }
            }
        )

        BottomAppBarItem(
            text = "删除",
            iconResId = R.drawable.delete,
            cornerRadius = 16.dp,
            action = {
                if (confirmToDelete) {
                    showDeleteDialog.value = true
                } else {
                    coroutineScope.launch {
                        selectedItemsList.forEach {
                            dao.deleteEntityById(it.id)
                        }
                        runTrashAction.value = true
                    }
                }
            },
            dialogComposable = {
                ConfirmationDialog(
                    showDialog = showDeleteDialog,
                    dialogTitle = "将所选项目移至回收站？",
                    confirmButtonLabel = "删除"
                ) {
                    coroutineScope.launch {
                        selectedItemsList.forEach {
                            dao.deleteEntityById(it.id)
                        }
                        runTrashAction.value = true
                    }
                }
            }
        )
    }
}

