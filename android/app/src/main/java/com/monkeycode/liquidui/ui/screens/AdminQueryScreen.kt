package com.monkeycode.liquidui.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.monkeycode.liquidui.ui.components.GlassCard
import com.monkeycode.liquidui.ui.components.SectionTitle
import com.monkeycode.liquidui.ui.features.adminquery.AdminQueryCategories
import com.monkeycode.liquidui.ui.features.adminquery.AdminQueryNorm
import com.monkeycode.liquidui.ui.features.adminquery.AdminQueryNorms
import com.monkeycode.liquidui.ui.features.adminquery.AdminQueryPreview

/**
 * 后台数据查询规范（手册 F16）：把「查数据查到崩溃」的四刀规范做成本 App 内
 * 可操作的真实 Compose 演示。条目 Q01–Q05，规范条文/反例/提示词见
 * `ui/features/adminquery/AdminQuerySpec.kt`，演示实现见 `AdminQueryDemo.kt`。
 * 列表与筛选状态一律可保存：点进详情返回后停在原处，不回到顶部。
 */
@Composable
fun AdminQueryCatalogSection(onOpen: (AdminQueryNorm) -> Unit) {
    var query by rememberSaveable { mutableStateOf("") }
    var category by rememberSaveable { mutableStateOf<String?>(null) }
    val chipRowState = rememberSaveable(saver = androidx.compose.foundation.ScrollState.Saver) { androidx.compose.foundation.ScrollState(0) }
    val filtered = remember(query, category) {
        AdminQueryNorms.filter { n ->
            (category == null || n.category == category) &&
                (query.isBlank() || n.name.contains(query) || n.summary.contains(query) || n.id.contains(query, true))
        }
    }
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(
            value = query, onValueChange = { query = it },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 22.dp),
            placeholder = { Text("搜索规范…（如 时间 / Q02）") }, singleLine = true,
        )
        SectionTitle(text = "分类")
        Row(
            Modifier.fillMaxWidth().horizontalScroll(chipRowState).padding(horizontal = 22.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            AdminQueryChip("全部", category == null) { category = null }
            AdminQueryCategories.forEach { c ->
                AdminQueryChip(c, category == c) { category = if (category == c) null else c }
            }
        }
        Text(
            "共 ${filtered.size} 条 · 点卡片进入可操作演示，长按详情页按钮复制提示词",
            Modifier.padding(horizontal = 22.dp),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
    Spacer(Modifier.height(4.dp))
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        filtered.forEach { n -> AdminQueryCard(n) { onOpen(n) } }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AdminQueryChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.combinedClickable(onClick = onClick),
        shape = RoundedCornerShape(percent = 50),
        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        contentColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
    ) {
        Text(label, style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp))
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AdminQueryCard(norm: AdminQueryNorm, onClick: () -> Unit) {
    GlassCard(Modifier.fillMaxWidth().padding(horizontal = 16.dp).combinedClickable(onClick = onClick)) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(10.dp)).padding(horizontal = 8.dp, vertical = 6.dp),
            ) {
                Text(norm.id, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer, style = MaterialTheme.typography.labelMedium)
            }
            Spacer(Modifier.padding(horizontal = 6.dp))
            Column(Modifier.weight(1f)) {
                Text(norm.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(norm.summary, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2)
            }
            Text(norm.difficulty, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AdminQueryDetailScreen(norm: AdminQueryNorm, onBack: () -> Unit) {
    BackHandler(onBack = onBack)
    val clipboard = LocalClipboardManager.current
    val haptics = LocalHapticFeedback.current
    var copied by remember { mutableStateOf(false) }
    var showPrompt by remember { mutableStateOf(false) }
    val listState = rememberSaveable(saver = LazyListState.Saver) { LazyListState() }
    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize().statusBarsPadding(),
        contentPadding = PaddingValues(top = 12.dp, bottom = 108.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Row(Modifier.padding(horizontal = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = onBack) { Text("‹ 返回素材库") }
                Text(
                    "${norm.id} · ${norm.category} · ${norm.difficulty}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(norm.name, Modifier.padding(horizontal = 22.dp), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(6.dp))
            Text(norm.summary, Modifier.padding(horizontal = 22.dp), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(4.dp))
            Text(norm.params, Modifier.padding(horizontal = 22.dp), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
        }
        item {
            SectionTitle(text = "规范条文（照着做）")
            GlassCard(Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                Text(norm.rule, style = MaterialTheme.typography.bodySmall)
            }
        }
        item {
            SectionTitle(text = "反例（别这么做）")
            GlassCard(Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                Text(norm.badCase, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
            }
        }
        item {
            SectionTitle(text = "功能体验（真实实现，可直接操作）")
            GlassCard(Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                Column(Modifier.padding(vertical = 4.dp)) { AdminQueryPreview(norm) }
            }
        }
        item {
            SectionTitle(text = "提示词")
            GlassCard(Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "点按查看全文，长按按钮把整段提示词复制到剪贴板，可直接粘贴给 AI 复现这条规范。",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Box(
                        Modifier.fillMaxWidth()
                            .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(14.dp))
                            .combinedClickable(
                                onClick = { showPrompt = !showPrompt },
                                onLongClick = {
                                    clipboard.setText(AnnotatedString(norm.prompt))
                                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                    copied = true
                                },
                            )
                            .padding(vertical = 14.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            if (copied) "✓ 已复制，去粘贴给 AI 吧" else "长按复制提示词",
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                    if (showPrompt) {
                        Text(
                            norm.prompt,
                            style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace, fontSize = 12.sp),
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                                .padding(12.dp),
                        )
                    }
                    Text(
                        "来源：B 站 KD一对A《查数据查到崩溃？这套烂后台连改了四刀》（BV1Vuxr6BEDp）公开表述提炼；条文与提示词为本仓库独立撰写，实现方案见 素材库/后台数据查询规范/。",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}
