package com.lanrhyme.shardlauncher.ui.account

import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.lanrhyme.shardlauncher.R
import com.lanrhyme.shardlauncher.game.account.ACCOUNT_TYPE_LOCAL
import com.lanrhyme.shardlauncher.game.account.Account
import com.lanrhyme.shardlauncher.game.account.getDisplayName
import com.lanrhyme.shardlauncher.game.account.wardrobe.validateSkinFile
import com.lanrhyme.shardlauncher.ui.components.filemanager.FileSelectorConfig
import com.lanrhyme.shardlauncher.ui.components.filemanager.FileSelectorMode
import com.lanrhyme.shardlauncher.ui.components.filemanager.FileSelectorResult
import com.lanrhyme.shardlauncher.ui.components.filemanager.FileSelectorScreen
import com.lanrhyme.shardlauncher.ui.components.layout.LocalCardLayoutConfig
import com.lanrhyme.shardlauncher.ui.components.basic.*
import dev.chrisbanes.haze.hazeEffect
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AccountCard(
        account: Account,
        isSelected: Boolean,
        onClick: () -> Unit,
        onLongClick: () -> Unit = {},
        onDelete: (Account) -> Unit,
        onEdit: (Account) -> Unit,
        cardAlpha: Float,
        onSkinSaved: (Account) -> Unit = {}
) {
        val (isCardBlurEnabled, _, hazeState) = LocalCardLayoutConfig.current
        val context = LocalContext.current
        var showMenu by remember { mutableStateOf(false) }
        var showSkinSelector by remember { mutableStateOf(false) }
        var skinKey by remember { mutableIntStateOf(0) }
        val scope = rememberCoroutineScope()
        val interactionSource = remember { MutableInteractionSource() }
        val isPressed by interactionSource.collectIsPressedAsState()

        Box {
                ShardCard(
                        style = CardStyle.GLASS,
                        modifier = Modifier
                                .selectableCard(
                                        isSelected = isSelected,
                                        isPressed = isPressed
                                )
                                .width(130.dp)
                                .height(175.dp)
                                .combinedClickable(
                                        onClick = onClick,
                                        onLongClick = {
                                                showMenu = true
                                                onLongClick()
                                        },
                                        interactionSource = interactionSource,
                                        indication = null
                                )
                                .then(if (isSelected) Modifier.border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(24.dp)) else Modifier),
                        shape = RoundedCornerShape(24.dp)
                ) {
                        Column(
                                modifier = Modifier.fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                                // Avatar
                                val localSkinFile =
                                        java.io.File(
                                                context.filesDir,
                                                "skins/${account.profileId}.png"
                                        )
                                val imageUrl =
                                        if (localSkinFile.exists()) {
                                                localSkinFile
                                        } else {
                                                "https://mineskin.eu/helm/${account.username}"
                                        }

                                val imageRequest =
                                        remember(skinKey) {
                                                ImageRequest.Builder(context)
                                                        .data(imageUrl)
                                                        .error(R.drawable.img_steve)
                                                        .crossfade(true)
                                                        // 本地皮肤文件每次保存后需重新读取，禁用磁盘缓存避免读到旧图
                                                        .diskCachePolicy(if (localSkinFile.exists()) CachePolicy.DISABLED else CachePolicy.ENABLED)
                                                        .memoryCachePolicy(CachePolicy.DISABLED)
                                                        .build()
                                        }

                                SubcomposeAsyncImage(
                                        model = imageRequest,
                                        contentDescription = "Account Avatar",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxWidth().weight(3.5f).clip(RoundedCornerShape(16.dp)),
                                        loading = {
                                                Box(
                                                        modifier = Modifier.fillMaxSize(),
                                                        contentAlignment = Alignment.Center
                                                ) {
                                                        CircularProgressIndicator(
                                                                modifier = Modifier.size(28.dp),
                                                                strokeWidth = 3.dp
                                                        )
                                                }
                                        }
                                )

                                // Info Section
                                Column(
                                        modifier = Modifier.fillMaxWidth().weight(1.5f).padding(top = 8.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                ) {
                                        Text(
                                            text = account.username,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    Text(
                                        text = account.getDisplayName(),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                        }
                }

                ShardDropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                        DropdownMenuItem(
                                text = { Text("删除账户档案") },
                                onClick = {
                                        onDelete(account)
                                        showMenu = false
                                }
                        )
                        DropdownMenuItem(
                                text = { Text("保存皮肤") },
                                onClick = {
                                        showSkinSelector = true
                                        showMenu = false
                                }
                        )
                        if (account.accountType == ACCOUNT_TYPE_LOCAL) {
                                DropdownMenuItem(
                                        text = { Text("修改用户名") },
                                        onClick = {
                                                onEdit(account)
                                                showMenu = false
                                        },
                                        trailingIcon = {
                                                IconButton(onClick = { onEdit(account) }) {
                                                        Icon(
                                                                imageVector = Icons.Default.Edit,
                                                                contentDescription = "Edit Account",
                                                                tint =
                                                                        MaterialTheme.colorScheme
                                                                                .onSurfaceVariant
                                                        )
                                                }
                                        }
                                )
                        }
                }
        }

        if (showSkinSelector) {
                FileSelectorScreen(
                        visible = showSkinSelector,
                        config = FileSelectorConfig(
                                initialPath = android.os.Environment.getExternalStorageDirectory(),
                                mode = FileSelectorMode.FILE_ONLY,
                                showHiddenFiles = true,
                                allowCreateDirectory = false,
                                fileFilter = { file ->
                                        file.isFile && (file.extension.equals("png", true) || file.extension.equals("jpg", true) || file.extension.equals("jpeg", true))
                                }
                        ),
                        onDismissRequest = { showSkinSelector = false },
                        onSelection = { result ->
                                when (result) {
                                        is FileSelectorResult.Selected -> {
                                                val sourceFile = result.path
                                                scope.launch {
                                                        try {
                                                                val valid = validateSkinFile(sourceFile)
                                                                if (!valid) {
                                                                        Toast.makeText(context, "皮肤无效：需 64x64 或 64x32 像素", Toast.LENGTH_LONG).show()
                                                                } else {
                                                                        val skinDir = File(context.filesDir, "skins")
                                                                        if (!skinDir.exists()) skinDir.mkdirs()
                                                                        val target = File(skinDir, "${account.profileId}.png")
                                                                        sourceFile.copyTo(target, overwrite = true)
                                                                        skinKey++
                                                                        onSkinSaved(account)
                                                                        Toast.makeText(context, "皮肤已保存", Toast.LENGTH_SHORT).show()
                                                                }
                                                        } catch (e: Exception) {
                                                                Toast.makeText(context, "保存皮肤失败: ${e.message}", Toast.LENGTH_SHORT).show()
                                                        }
                                                }
                                        }
                                        FileSelectorResult.Cancelled -> { }
                                        is FileSelectorResult.MultipleSelected -> { }
                                }
                                showSkinSelector = false
                        }
                )
        }
}
