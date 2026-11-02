package com.monkeycode.liquidui.ui.features.overlayterms

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.monkeycode.liquidui.R
import com.monkeycode.liquidui.ui.theme.AppIcons
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * 弹窗与提示术语（F17）的真实 Compose 演示：P01–P12 每条都能实际操作。
 * 全部用 Compose/Material3 与系统 API 真实实现——真 Toast、真 AlertDialog、真 Dialog、
 * 真 DropdownMenu 锚定、真 SnackbarHost（可撤销）、真 NotificationManager 发送、
 * 真 ModalBottomSheet、真进度三档。术语定义与提示词见 [OverlayTerms]。
 */

/** 预览分发：详情页按条目 id 调用。 */
@Composable
fun OverlayTermsPreview(term: OverlayTerm) {
    when (term.id) {
        "P01" -> PreviewP01()
        "P02" -> PreviewP02()
        "P03" -> PreviewP03()
        "P04" -> PreviewP04()
        "P05" -> PreviewP05()
        "P06" -> PreviewP06()
        "P07" -> PreviewP07()
        "P08" -> PreviewP08()
        "P09" -> PreviewP09()
        "P10" -> PreviewP10()
        "P11" -> PreviewP11()
        else -> PreviewP12()
    }
}

// ─── 共用原子控件 ───────────────────────────────────────────────────────────

@Composable
private fun OTChip(label: String, selected: Boolean, enabled: Boolean = true, onClick: () -> Unit) {
    val bg = when {
        !enabled -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.28f)
        selected -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
    }
    val fg = when {
        !enabled -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f)
        selected -> MaterialTheme.colorScheme.onPrimary
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    Text(
        label,
        style = MaterialTheme.typography.labelMedium,
        color = fg,
        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bg)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 7.dp),
    )
}

@Composable
private fun OTButton(text: String, enabled: Boolean = true, primary: Boolean = true, loading: Boolean = false, onClick: () -> Unit) {
    val bg = when {
        !enabled -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        primary -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
    }
    val fg = when {
        !enabled -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f)
        primary -> MaterialTheme.colorScheme.onPrimary
        else -> MaterialTheme.colorScheme.onSurface
    }
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(bg)
            .clickable(enabled = enabled && !loading, onClick = onClick)
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (loading) {
            CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp, color = fg)
            Spacer(Modifier.width(8.dp))
        }
        Text(if (loading) "处理中…" else text, color = fg, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleSmall)
    }
}

@Composable
private fun OTNote(text: String, accent: Boolean = false) {
    Text(
        text,
        style = MaterialTheme.typography.labelSmall,
        color = if (accent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

// ─── P01 Alert 警告框 ───────────────────────────────────────────────────────

@Composable
private fun PreviewP01() {
    var open by remember { mutableStateOf(false) }
    var blocked by remember { mutableStateOf(true) }
    var seen by remember { mutableIntStateOf(0) }
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        OTNote("Alert = 阻塞式告知：只有一个「知道了」，不征求选择。")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OTChip("不可绕过（点外面/返回键都关不掉）", selected = blocked, onClick = { blocked = true })
            OTChip("可绕过", selected = !blocked, onClick = { blocked = false })
        }
        OTButton("弹出 Alert：存储即将写满") { open = true }
        OTNote("已确认 $seen 次。")
        OTNote("反例对照：把「保存成功」做成 Alert 就是滥用——那该是 Toast（P02）。")
    }
    if (open) {
        AlertDialog(
            onDismissRequest = { if (!blocked) { open = false; seen += 1 } },
            properties = androidx.compose.ui.window.DialogProperties(
                dismissOnBackPress = !blocked,
                dismissOnClickOutside = !blocked,
            ),
            title = { Text("存储即将写满") },
            text = { Text("剩余空间不足 200MB，继续录制可能中断。这是 Alert：只告知事实，不让你选。") },
            confirmButton = {
                TextButton(onClick = { open = false; seen += 1 }) { Text("知道了") }
            },
        )
    }
}

// ─── P02 Toast 轻提示 ───────────────────────────────────────────────────────

@Composable
private fun PreviewP02() {
    val context = LocalContext.current
    var light by remember { mutableStateOf<String?>(null) }
    var fired by remember { mutableIntStateOf(0) }
    LaunchedEffect(light) {
        val msg = light
        if (msg != null) {
            delay(2200)
            light = null
        }
    }
    Box {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            OTNote("Toast = 最弱反馈：不打断、无按钮、自动消失。")
            OTButton("发一条系统 Toast（真 Toast.makeText）") {
                Toast.makeText(context, "已保存到草稿", Toast.LENGTH_SHORT).show()
                fired += 1
            }
            OTButton("显示自绘轻提示（可定制样式，2.2s 自动消失）", primary = false) {
                light = "已复制到剪贴板"
                fired += 1
            }
            OTNote("已触发 $fired 次。系统 Toast 的限制：Android 12+ 应用在后台时不显示、样式不可定制、不能带按钮。")
            OTNote("要品牌样式/多条堆叠就自绘，但语义不能变：仍然不可交互、自动消失。")
            OTNote("反例对照：报错别用 Toast（读不完且无法重试）——该用 Snackbar（P07）或内联错误。")
        }
        if (light != null) {
            Surface(
                Modifier.align(Alignment.BottomCenter).padding(top = 12.dp),
                shape = RoundedCornerShape(percent = 50),
                color = MaterialTheme.colorScheme.inverseSurface,
                contentColor = MaterialTheme.colorScheme.inverseOnSurface,
            ) {
                Text(light ?: "", style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(horizontal = 16.dp, vertical = 9.dp))
            }
        }
    }
}

// ─── P03 Modal 模态框 ───────────────────────────────────────────────────────

@Composable
private fun PreviewP03() {
    var open by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var saved by remember { mutableStateOf<String?>(null) }
    var outsideClosable by remember { mutableStateOf(false) }
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        OTNote("Modal = 带遮罩的小房间：必须在里面完成或明确放弃，才能回主流程。")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OTChip("点外面不可关（防丢数据）", selected = !outsideClosable, onClick = { outsideClosable = false })
            OTChip("点外面可关", selected = outsideClosable, onClick = { outsideClosable = true })
        }
        OTButton("打开 Modal：重命名项目") { name = saved ?: "未命名项目"; open = true }
        if (saved != null) OTNote("当前名称：$saved", accent = true)
        OTNote("含输入框的 Modal 一定要给「取消」，且主按钮在内容为空时禁用。")
        OTNote("反例对照：Modal 里装长列表/设置页 → 该用 Drawer（P04）或 BottomSheet（P10）。")
    }
    if (open) {
        Dialog(
            onDismissRequest = { if (outsideClosable) open = false },
            properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = outsideClosable),
        ) {
            Surface(shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surface) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("重命名项目", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    OutlinedTextField(
                        value = name, onValueChange = { name = it },
                        label = { Text("项目名称") }, singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    OTNote("这是 Modal：背景被遮罩挡住、焦点关在框内，必须处理完才能回去。")
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { open = false }) { Text("取消") }
                        TextButton(onClick = { saved = name; open = false }, enabled = name.isNotBlank()) { Text("保存") }
                    }
                }
            }
        }
    }
}

// ─── P04 Drawer 抽屉 ────────────────────────────────────────────────────────

@Composable
private fun PreviewP04() {
    var open by remember { mutableStateOf(false) }
    var picked by remember { mutableStateOf("概览") }
    val progress by animateFloatAsState(if (open) 1f else 0f, tween(260), label = "drawer")
    BackHandler(enabled = open) { open = false }
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        OTNote("Drawer = 从边缘滑出的面板，承载导航或一批内容；遮罩透明度与面板位移共用同一 progress。")
        Box(
            Modifier.fillMaxWidth().height(280.dp).clip(RoundedCornerShape(14.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f))
        ) {
            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("主页面内容", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Text("当前选中：$picked", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                OTButton("打开抽屉") { open = true }
                OTNote("点遮罩、按返回键都能关；面板宽 210dp 固定，不用百分比像素写死。")
            }
            if (progress > 0.001f) {
                Box(
                    Modifier.matchParentSize()
                        .background(Color.Black.copy(alpha = 0.4f * progress))
                        .clickable { open = false }
                )
                Surface(
                    Modifier.align(Alignment.CenterStart).fillMaxHeight().width(210.dp)
                        .offset { IntOffset(((progress - 1f) * 210.dp.toPx()).roundToInt(), 0) },
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp,
                ) {
                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("导航", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        listOf("概览", "订单", "报表", "成员", "设置").forEach { item ->
                            Text(
                                item,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (item == picked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                fontWeight = if (item == picked) FontWeight.SemiBold else FontWeight.Normal,
                                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp))
                                    .clickable { picked = item; open = false }.padding(horizontal = 10.dp, vertical = 11.dp),
                            )
                        }
                    }
                }
            }
        }
        OTNote("反例对照：手机竖屏用左侧 Drawer 装长表单 → 该用 BottomSheet（P10）或独立页。")
    }
}

// ─── P05 Popconfirm 气泡确认框 ──────────────────────────────────────────────

@Composable
private fun PreviewP05() {
    var items by remember { mutableStateOf(listOf("报表 A", "报表 B", "报表 C")) }
    var pending by remember { mutableStateOf<String?>(null) }
    var undo by remember { mutableStateOf<Pair<Int, String>?>(null) }
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        OTNote("Popconfirm = 贴着按钮的小气泡，就地二次确认轻量操作，不遮整页。")
        if (items.isEmpty()) OTNote("列表已空。点下面的「恢复全部」再来一遍。")
        items.forEachIndexed { index, name ->
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(name, Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                Box {
                    TextButton(onClick = { pending = name }) { Text("删除") }
                    DropdownMenu(expanded = pending == name, onDismissRequest = { pending = null }) {
                        Column(Modifier.padding(horizontal = 14.dp, vertical = 10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("确定删除「$name」？", style = MaterialTheme.typography.bodyMedium)
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                TextButton(onClick = { pending = null }) { Text("取消") }
                                TextButton(onClick = {
                                    undo = index to name
                                    items = items.filterNot { it == name }
                                    pending = null
                                }) { Text("删除", color = MaterialTheme.colorScheme.error) }
                            }
                        }
                    }
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OTChip("恢复全部", selected = false, onClick = { items = listOf("报表 A", "报表 B", "报表 C"); undo = null })
            undo?.let { (i, n) ->
                OTChip("撤销上一次（$n 回到第 ${i + 1} 位）", selected = false, onClick = {
                    val list = items.toMutableList(); list.add(i.coerceAtMost(list.size), n); items = list; undo = null
                })
            }
        }
        OTNote("Android 对应：DropdownMenu 自动锚定并在空间不足时翻到上方；要自定义箭头/位置才用底层 Popup。")
        OTNote("反例对照：确认「清空全部数据」这种重操作 → 该用 Modal（P03）写清后果。")
    }
}

// ─── P06 Tooltip 文字提示 ───────────────────────────────────────────────────

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PreviewP06() {
    var tipKey by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(tipKey) {
        if (tipKey != null) {
            delay(1800)
            tipKey = null
        }
    }
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        OTNote("Tooltip = 纯文字注解，长按出现、自动消失、不可交互。移动端没有 hover，等价触发是长按。")
        Row(horizontalArrangement = Arrangement.spacedBy(18.dp), verticalAlignment = Alignment.CenterVertically) {
            listOf(
                "notif" to ("通知" to "控制是否推送与免打扰时段"),
                "sync" to ("同步" to "上次同步 12 分钟前，仅 Wi-Fi 下自动"),
                "lock" to ("锁定" to "已禁用：需要先完成实名认证"),
            ).forEach { (key, pair) ->
                val (label, text) = pair
                Box {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            if (key == "notif") AppIcons.Notifications else if (key == "sync") AppIcons.RotateRight else AppIcons.Shield,
                            contentDescription = text,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(26.dp).combinedClickable(onClick = { }, onLongClick = { tipKey = key }),
                        )
                        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    DropdownMenu(expanded = tipKey == key, onDismissRequest = { tipKey = null }) {
                        Text(text, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp))
                    }
                }
            }
        }
        OTNote("长按任意图标试试：1.8s 后自动消失，点外部也可关。")
        OTNote("无障碍要点：图标已设 contentDescription，读屏拿得到同样信息——必要说明不能只藏在 Tooltip 里。")
        OTNote("反例对照：Tooltip 里塞按钮/链接 → 那是 Popover（P09）。")
    }
}

// ─── P07 Snackbar 条幅提示 ──────────────────────────────────────────────────

@Composable
private fun PreviewP07() {
    val scope = rememberCoroutineScope()
    val host = remember { SnackbarHostState() }
    var items by remember { mutableStateOf(listOf("草稿 1", "草稿 2", "草稿 3")) }
    Box(Modifier.fillMaxWidth().height(230.dp)) {
        Column(Modifier.padding(bottom = 56.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            OTNote("Snackbar = 应用内底部条幅，可带一个操作；删除后给「撤销」是它的典型用法。")
            if (items.isEmpty()) {
                OTNote("三条草稿都删完了。等 Snackbar 消失后重进本页可复位。")
            }
            items.forEachIndexed { index, name ->
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(name, Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                    TextButton(onClick = {
                        scope.launch {
                            val list = items.toMutableList()
                            list.removeAt(index)
                            items = list
                            val result = host.showSnackbar("已删除「$name」", actionLabel = "撤销", duration = SnackbarDuration.Short)
                            if (result == SnackbarResult.ActionPerformed) {
                                val back = items.toMutableList()
                                back.add(index.coerceAtMost(back.size), name)
                                items = back
                            }
                        }
                    }) { Text("删除") }
                }
            }
            OTChip("恢复全部草稿", selected = false, onClick = { items = listOf("草稿 1", "草稿 2", "草稿 3") })
            OTNote("反例对照：Snackbar 里放两个以上操作 → 那已经是工具栏了；报致命错误 → 用 Modal（P03）。")
        }
        SnackbarHost(hostState = host, modifier = Modifier.align(Alignment.BottomCenter))
    }
}

// ─── P08 Notification 系统通知 ──────────────────────────────────────────────

@Composable
private fun PreviewP08() {
    val context = LocalContext.current
    var status by remember { mutableStateOf<String?>(null) }
    var sent by remember { mutableIntStateOf(0) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        status = if (granted) "已授权，现在点「发送真实通知」能收到" else "被拒绝：Android 13+ 通知发不出去，需到系统设置手动开启"
    }
    fun hasPermission(): Boolean =
        Build.VERSION.SDK_INT < 33 ||
            context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED

    fun send() {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "overlay_terms_demo"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "术语演示", NotificationManager.IMPORTANCE_DEFAULT)
            channel.description = "弹窗与提示术语 P08 的演示通知渠道"
            manager.createNotificationChannel(channel)
        }
        val notification = Notification.Builder(context, channelId)
            .setContentTitle("导出完成")
            .setContentText("报表 A.csv 已生成，点击查看")
            .setSmallIcon(R.drawable.ic_stat_launcher)
            .setAutoCancel(true)
            .build()
        manager.notify(2001, notification)
    }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        OTNote("Notification = 系统层提示，应用不在前台也能送达；因此门槛最高：要建渠道、要授权、要能被单独关掉。")
        OTNote("当前系统版本 SDK ${Build.VERSION.SDK_INT}；授权状态：${if (hasPermission()) "已授权" else "未授权（Android 13+ 必须先申请 POST_NOTIFICATIONS）"}", accent = true)
        if (!hasPermission()) {
            OTButton("申请通知权限", primary = false) { launcher.launch(Manifest.permission.POST_NOTIFICATIONS) }
        }
        OTButton("发送真实通知（走 NotificationManager）", enabled = hasPermission()) {
            send()
            sent += 1
            status = "已调用 notify(2001)，去通知栏看"
        }
        if (status != null) OTNote(status!!, accent = true)
        OTNote("本机已发送 $sent 次。未授权时点按钮是禁用态，不会静默失败——这是规范要求。")

        Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)) {
            Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(AppIcons.Notifications, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(22.dp))
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text("导出完成", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                    Text("报表 A.csv 已生成，点击查看", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text("现在", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        OTNote("上面是通知在通知栏里的样子（应用内预览，方便没授权时也能核对文案与图标）。")
        OTNote("反例对照：应用内就能说完的事推成通知 = 白送打扰；不建渠道直接发在 API 26+ 会失败。")
    }
}

// ─── P09 Popover 气泡卡片 ───────────────────────────────────────────────────

@Composable
private fun PreviewP09() {
    var open by remember { mutableStateOf(false) }
    var notifyOn by remember { mutableStateOf(true) }
    var picked by remember { mutableStateOf<String?>(null) }
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        OTNote("Popover = 锚定的富内容气泡：可放菜单项、开关、输入；Tooltip 的「能干活」版本。")
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Box {
                OTChip("更多操作 ⋯", selected = open, onClick = { open = !open })
                DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
                    Column(Modifier.padding(vertical = 4.dp)) {
                        listOf("分享" to "生成链接发给同事", "导出" to "CSV / PDF 两种格式", "归档" to "从列表移走但可恢复").forEach { (title, sub) ->
                            Row(
                                Modifier.fillMaxWidth().clickable { picked = title; open = false }.padding(horizontal = 16.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(AppIcons.Widgets, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(10.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(title, style = MaterialTheme.typography.bodyMedium)
                                    Text(sub, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                        HorizontalDivider(Modifier.padding(vertical = 4.dp))
                        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("接收通知", Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                            Box(
                                Modifier.size(width = 42.dp, height = 26.dp).clip(RoundedCornerShape(percent = 50))
                                    .background(if (notifyOn) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable { notifyOn = !notifyOn }.padding(3.dp),
                                contentAlignment = if (notifyOn) Alignment.CenterEnd else Alignment.CenterStart,
                            ) {
                                Box(Modifier.size(20.dp).clip(CircleShape).background(MaterialTheme.colorScheme.onPrimary))
                            }
                        }
                    }
                }
            }
        }
        if (picked != null) OTNote("已选择：$picked（气泡点外部或返回键即关）", accent = true)
        OTNote("气泡内每行热区 ≥44dp；内容超过 6 项或需要滚动时改用 BottomSheet（P10）。")
        OTNote("反例对照：气泡里再弹气泡、或点外部不关闭，都是常见错法。")
    }
}

// ─── P10 BottomSheet / ActionSheet ──────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PreviewP10() {
    var open by remember { mutableStateOf(false) }
    var chosen by remember { mutableStateOf<String?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    val scope = rememberCoroutineScope()
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        OTNote("BottomSheet / ActionSheet = 从底部升起的面板；移动端比 Drawer 顺手，拇指够得到。")
        OTButton("打开动作面板（ActionSheet）") { open = true }
        if (chosen != null) OTNote("已选择：$chosen", accent = true)
        OTNote("面板可半展开（露一半）与全展开两档，拖把手可切换；下拉关闭与内部滚动不打架。")
        OTNote("反例对照：ActionSheet 不给「取消」项，或把整页内容塞进面板造成滚动嵌套。")
    }
    if (open) {
        ModalBottomSheet(onDismissRequest = { open = false }, sheetState = sheetState) {
            Column(Modifier.padding(bottom = 20.dp)) {
                listOf("分享到微信", "复制链接", "导出 PDF", "移动到归档").forEach { action ->
                    Text(
                        action,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.fillMaxWidth().clickable {
                            chosen = action
                            scope.launch { sheetState.hide() }
                            open = false
                        }.padding(horizontal = 24.dp, vertical = 15.dp),
                    )
                }
                Text(
                    "删除",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.fillMaxWidth().clickable {
                        chosen = "删除"
                        scope.launch { sheetState.hide() }
                        open = false
                    }.padding(horizontal = 24.dp, vertical = 15.dp),
                )
                HorizontalDivider()
                Text(
                    "取消",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth().clickable {
                        scope.launch { sheetState.hide() }
                        open = false
                    }.padding(horizontal = 24.dp, vertical = 15.dp),
                )
            }
        }
    }
}

// ─── P11 Loading / Progress 三档 ────────────────────────────────────────────

@Composable
private fun PreviewP11() {
    var mode by remember { mutableStateOf("按钮内") }
    var running by remember { mutableStateOf(false) }
    var progress by remember { mutableStateOf(0f) }
    var done by remember { mutableIntStateOf(0) }
    LaunchedEffect(running) {
        while (running) {
            delay(90)
            progress = (progress + 0.04f).coerceAtMost(1f)
            if (progress >= 1f) {
                running = false
                done += 1
            }
        }
    }
    Box {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            OTNote("加载反馈分三档强度：非阻塞 → 按钮内 → 全局遮罩。强度越高越要给进度与取消。")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("非阻塞", "按钮内", "全局遮罩").forEach { m ->
                    OTChip(m, selected = mode == m, onClick = { mode = m })
                }
            }
            if (mode == "非阻塞") {
                LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth())
                OTNote("顶部细进度条：用户仍可继续操作，适合后台刷新/分页加载。", accent = true)
            }
            OTButton(
                text = when (mode) {
                    "按钮内" -> "提交表单"
                    "全局遮罩" -> "开始关键操作"
                    else -> "后台刷新"
                },
                loading = mode == "按钮内" && running,
                enabled = !running || mode != "按钮内",
            ) {
                progress = 0f
                running = true
            }
            OTNote(if (running) "进行中：${(progress * 100).toInt()}%" else "已完成 $done 次任务。", accent = running)
            if (mode == "按钮内") OTNote("按钮加载态：宽度不变（只换文案 + 16dp 圆环），防止布局跳动；同时禁止重复提交。")
            if (mode == "全局遮罩") OTNote("全局遮罩必须给取消或超时兜底，否则网络一断就永远转圈。")
            OTNote("反例对照：点一下按钮就整页转圈；加载完不给结果反馈（应接 Toast/Snackbar）。")
        }
        if (mode == "全局遮罩" && running) {
            Box(
                Modifier.matchParentSize().background(Color.Black.copy(alpha = 0.4f)),
                contentAlignment = Alignment.Center,
            ) {
                Surface(shape = RoundedCornerShape(18.dp), color = MaterialTheme.colorScheme.surface) {
                    Column(Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        CircularProgressIndicator()
                        Text("正在写入，请勿退出（${(progress * 100).toInt()}%）", style = MaterialTheme.typography.labelMedium)
                        TextButton(onClick = { running = false; progress = 0f }) { Text("取消") }
                    }
                }
            }
        }
    }
}

// ─── P12 Badge / Dot ────────────────────────────────────────────────────────

@Composable
private fun PreviewP12() {
    var count by remember { mutableIntStateOf(5) }
    var dot by remember { mutableStateOf(true) }
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        OTNote("Badge = 附着在图标上的持续状态：不弹出、不打断，被消费后才消失。")
        Row(horizontalArrangement = Arrangement.spacedBy(26.dp), verticalAlignment = Alignment.CenterVertically) {
            Box {
                Icon(AppIcons.Notifications, contentDescription = "未读 $count 条", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(30.dp))
                if (count > 0) {
                    Surface(
                        Modifier.align(Alignment.TopEnd).offset(x = 8.dp, y = (-6).dp),
                        shape = RoundedCornerShape(percent = 50),
                        color = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError,
                    ) {
                        Text(
                            if (count > 99) "99+" else "$count",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp),
                        )
                    }
                }
            }
            Box {
                Icon(AppIcons.Settings, contentDescription = if (dot) "有更新" else "无更新", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(30.dp))
                if (dot) {
                    Box(Modifier.align(Alignment.TopEnd).offset(x = 2.dp, y = (-2).dp).size(9.dp).background(MaterialTheme.colorScheme.error, CircleShape))
                }
            }
            Column {
                Text("计数角标 vs 红点", style = MaterialTheme.typography.labelMedium)
                Text("数字用于「有多少」，红点用于「有更新但不计数」", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OTChip("+1", selected = false, onClick = { count += 1 })
            OTChip("−1", selected = false, enabled = count > 0, onClick = { count -= 1 })
            OTChip("设为 100", selected = false, onClick = { count = 100 })
            OTChip("清零", selected = false, onClick = { count = 0; dot = false })
            OTChip("红点开", selected = dot, onClick = { dot = true })
        }
        OTNote("当前 count = $count → 显示 ${if (count == 0) "（不渲染角标）" else if (count > 99) "99+" else "$count"}", accent = true)
        OTNote("规则：count > 99 显示「99+」；count == 0 时整个角标不渲染，不是显示「0」。")
        OTNote("反例对照：红点永远消不掉（没有消费逻辑）、角标遮住图标主体超过 1/3。")
    }
}
