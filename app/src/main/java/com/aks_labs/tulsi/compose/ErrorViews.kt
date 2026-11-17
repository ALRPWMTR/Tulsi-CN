package com.aks_labs.tulsi.compose

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.res.painterResource
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.aks_labs.tulsi.R
import com.aks_labs.tulsi.helpers.ImageFunctions

enum class ViewProperties(
    val emptyText: String,
    val emptyIconResId: Int,
    val prefix: String,
    val operation: ImageFunctions
) {
    Trash(
        emptyText = "已删除的项目会显示在这里",
        emptyIconResId = R.drawable.delete,
        prefix = "删除于 ",
        operation = ImageFunctions.LoadTrashedImage
    ),
    Album(
        emptyText = "此相册为空",
        emptyIconResId = R.drawable.error,
        prefix = "",
        operation = ImageFunctions.LoadNormalImage
    ),
    SearchLoading(
        emptyText = "搜索一些照片吧！",
        emptyIconResId = R.drawable.search,
        prefix = "",
        operation = ImageFunctions.LoadNormalImage
    ),
    SearchNotFound(
        emptyText = "找不到匹配项",
        emptyIconResId = R.drawable.error,
        prefix = "",
        operation = ImageFunctions.LoadNormalImage
    ),
    SecureFolder(
        emptyText = "在此添加项目进行加密",
        emptyIconResId = R.drawable.locked_folder,
        prefix = "加密于 ",
        operation = ImageFunctions.LoadSecuredImage
    ),
    Favourites(
        emptyText = "添加你最珍贵的回忆",
        emptyIconResId = R.drawable.favourite,
        prefix = "",
        operation = ImageFunctions.LoadNormalImage
    )
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun FolderIsEmpty(
    emptyText: String,
    emptyIconResId: Int,
    backgroundColor: Color = MaterialTheme.colorScheme.background
) {
    Column(
        modifier = Modifier
            .fillMaxSize(1f)
            .background(backgroundColor),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        GlideImage(
            model = emptyIconResId,
            contentDescription = "文件夹不存在图标",
            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onBackground),
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .size(56.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = emptyText,
            fontSize = TextUnit(16f, TextUnitType.Sp),
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier
                .wrapContentSize()
        )
    }
}

@Composable
fun ErrorPage(
    message: String,
    @DrawableRes iconResId: Int
) {
    Column(
        modifier = Modifier
            .fillMaxSize(1f)
            .background(MaterialTheme.colorScheme.background),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            painter = painterResource(id = iconResId),
            contentDescription = "错误页面图标",
            tint = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier
                .size(56.dp)
        )

        Spacer (modifier = Modifier.height(16.dp))

        Text(
            text = message,
            fontSize = TextUnit(16f, TextUnitType.Sp),
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier
                .wrapContentSize()
        )
    }
}

