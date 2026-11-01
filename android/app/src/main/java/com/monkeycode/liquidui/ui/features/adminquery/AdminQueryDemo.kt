package com.monkeycode.liquidui.ui.features.adminquery

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.monkeycode.liquidui.ui.components.GlassCard
import com.monkeycode.liquidui.ui.components.SectionTitle
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * 后台数据查询规范（F16）的真实 Compose 演示：Q01–Q05 各一个可操作预览。
 * 数据全部来自 [AdminRows]（本地生成的模拟数据），不请求网络、不写文件；
 * 「导出」= 生成 CSV 文本复制到剪贴板。规范条文与提示词见 [AdminQueryNorms]，
 * 出处见 素材库/后台数据查询规范/参考来源.md。
 */

/** 预览分发：详情页按条目 id 调用。 */
@Composable
fun AdminQueryPreview(norm: AdminQueryNorm) {
    when (norm.id) {
        "Q01" -> PreviewQ01()
        "Q02" -> PreviewQ02()
        "Q03" -> PreviewQ03()
        "Q04" -> PreviewQ04()
        else -> PreviewQ05()
    }
}

// ─── 共用原子控件（本模块自带，避免与全局控件签名耦合） ─────────────────────

/** 已选条件标签的描述：文本 + 点按动作（展开/编辑）+ 删除动作（回退筛选），两者语义分开。 */
private class QTagSpec(
    val text: String,
    val onDelete: (() -> Unit)? = null,
    val onClick: (() -> Unit)? = null,
)

@Composable
private fun QChip(
    label: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    sub: String? = null,
    enabled: Boolean = true,
    onClick: () -> Unit = {},
) {
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
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bg)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 7.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            color = fg,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
        )
        if (sub != null) {
            Text(sub, style = MaterialTheme.typography.labelSmall, color = fg.copy(alpha = 0.85f), fontSize = 10.sp)
        }
    }
}

@Composable
private fun QTag(spec: QTagSpec) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(percent = 50))
            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f))
            .clickable(enabled = spec.onClick != null, onClick = { spec.onClick?.invoke() })
            .padding(start = 10.dp, end = if (spec.onDelete != null) 4.dp else 10.dp, top = 5.dp, bottom = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(spec.text, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
        if (spec.onDelete != null) {
            Spacer(Modifier.width(4.dp))
            Box(
                Modifier
                    .size(18.dp)
                    .clip(RoundedCornerShape(percent = 50))
                    .background(MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.16f))
                    .clickable(onClick = spec.onDelete),
                contentAlignment = Alignment.Center,
            ) { Text("×", fontSize = 12.sp, color = MaterialTheme.colorScheme.onPrimaryContainer) }
        }
    }
}

@Composable
private fun QButton(
    text: String,
    enabled: Boolean = true,
    primary: Boolean = true,
    loading: Boolean = false,
    onClick: () -> Unit,
) {
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
    Box(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(bg)
            .clickable(enabled = enabled && !loading, onClick = onClick)
            .padding(vertical = 13.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            if (loading) "查询中…" else text,
            color = fg,
            fontWeight = FontWeight.SemiBold,
            style = MaterialTheme.typography.titleSmall,
        )
    }
}

@Composable
private fun QSwitchRow(label: String, checked: Boolean, hint: String? = null, onChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
            if (hint != null) Text(hint, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Box(
            Modifier
                .size(width = 46.dp, height = 28.dp)
                .clip(RoundedCornerShape(percent = 50))
                .background(if (checked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                .clickable { onChange(!checked) }
                .padding(3.dp),
            contentAlignment = if (checked) Alignment.CenterEnd else Alignment.CenterStart,
        ) {
            Box(Modifier.size(22.dp).clip(RoundedCornerShape(percent = 50)).background(MaterialTheme.colorScheme.onPrimary))
        }
    }
}

@Composable
private fun QNote(text: String, accent: Boolean = false) {
    Text(
        text,
        style = MaterialTheme.typography.labelSmall,
        color = if (accent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun QMono(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace, fontSize = 11.sp),
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f), RoundedCornerShape(10.dp))
            .padding(10.dp),
    )
}

@Composable
private fun QRowLine(r: AdminRow) {
    Row(Modifier.fillMaxWidth().padding(vertical = 3.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(r.date.toString().substring(5), Modifier.width(46.dp), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(r.dept, Modifier.width(58.dp), style = MaterialTheme.typography.labelSmall)
        Text(r.channel, Modifier.width(52.dp), style = MaterialTheme.typography.labelSmall)
        Text(
            r.status,
            Modifier.width(46.dp),
            style = MaterialTheme.typography.labelSmall,
            color = if (r.status == "异常") MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text("¥${r.amount}", Modifier.weight(1f), style = MaterialTheme.typography.labelSmall, textAlign = TextAlign.End)
    }
}

/** 草稿与已生效条件的差异项数（用于「n 项待生效」）。 */
private fun diffCount(draft: AdminFilter, applied: AdminFilter): Int = listOf(
    draft.start != applied.start || draft.end != applied.end,
    draft.statuses != applied.statuses,
    draft.depts != applied.depts,
    draft.channels != applied.channels,
    draft.amountBucket != applied.amountBucket,
    draft.onlyAbnormal != applied.onlyAbnormal,
).count { it }

/** MM-dd 短日期，用于按钮副标题。 */
private fun LocalDate.short(): String = "$monthValue-$dayOfMonth"

// ─── Q01 筛选分层与批量提交 ─────────────────────────────────────────────────

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PreviewQ01() {
    val scope = rememberCoroutineScope()
    val today = remember { LocalDate.now() }
    var batchMode by remember { mutableStateOf(true) }
    var moreOpen by remember { mutableStateOf(false) }
    var deptPickerOpen by remember { mutableStateOf(false) }

    // 草稿条件（改了不一定生效）
    var dDays by remember { mutableIntStateOf(7) }
    var dStatuses by remember { mutableStateOf(emptySet<String>()) }
    var dDepts by remember { mutableStateOf(emptySet<String>()) }
    var dChannels by remember { mutableStateOf(emptySet<String>()) }
    var dAmount by remember { mutableStateOf("全部") }
    var dAbnormal by remember { mutableStateOf(false) }

    // 已生效条件
    var applied by remember { mutableStateOf(AdminFilter(start = today.minusDays(6), end = today)) }
    var rows by remember { mutableStateOf(filterAdminRows(AdminRows, applied)) }
    var loading by remember { mutableStateOf(false) }
    var queryCount by remember { mutableIntStateOf(0) }

    /** 读当前草稿（在调用点即时读取状态，避免捕获旧值）。 */
    fun draftNow() = AdminFilter(
        start = today.minusDays((dDays - 1).toLong()), end = today,
        statuses = dStatuses, depts = dDepts, channels = dChannels,
        amountBucket = dAmount, onlyAbnormal = dAbnormal,
    )
    val pending = diffCount(draftNow(), applied)

    fun runQuery() {
        if (loading) return
        loading = true
        queryCount += 1
        val f = draftNow()
        scope.launch {
            delay(450)
            applied = f
            rows = filterAdminRows(AdminRows, f)
            loading = false
        }
    }

    /** 条件变化：批量模式只改草稿，即时模式立刻查一次。 */
    fun onDraftChange(block: () -> Unit) {
        block()
        if (!batchMode) runQuery()
    }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        QSwitchRow(
            label = if (batchMode) "选完一起查（推荐）" else "即时查询（反例：每改一次查一次）",
            checked = batchMode,
            hint = "单次查询模拟 450ms。即时模式下连改 10 个条件会打 10 次请求。",
        ) { batchMode = it }

        SectionTitle(text = "常用条件（常显）")
        QNote("时间：写明起止，口径见 Q02")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(1 to "今日", 7 to "近7日", 30 to "近30日").forEach { (days, label) ->
                val start = today.minusDays((days - 1).toLong())
                QChip(
                    label = label,
                    sub = if (days == 1) today.short() else "${start.short()} ~ ${today.short()}",
                    selected = dDays == days,
                    onClick = { onDraftChange { dDays = days } },
                )
            }
        }
        QNote("状态（多选）")
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            AdminStatuses.forEach { s ->
                QChip(
                    label = s, selected = s in dStatuses,
                    onClick = { onDraftChange { dStatuses = if (s in dStatuses) dStatuses - s else dStatuses + s } },
                )
            }
        }
        QNote("部门（20 个小组收成一个入口，不摊开）")
        QChip(
            label = when {
                dDepts.size == AdminDepts.size -> "全部部门 · ${AdminDepts.size}"
                dDepts.isEmpty() -> "选择部门"
                else -> "部门 · ${dDepts.size}"
            },
            sub = if (dDepts != applied.depts) "待生效" else null,
            selected = dDepts.isNotEmpty(),
            onClick = { deptPickerOpen = !deptPickerOpen },
        )
        if (deptPickerOpen) {
            GlassCard(Modifier.fillMaxWidth(), contentPadding = PaddingValues(12.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    QChip(
                        "全选", selected = dDepts.size == AdminDepts.size,
                        onClick = { onDraftChange { dDepts = if (dDepts.size == AdminDepts.size) emptySet() else AdminDepts.toSet() } },
                    )
                    QChip("清空", selected = false, onClick = { onDraftChange { dDepts = emptySet() } })
                }
                Spacer(Modifier.height(6.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    AdminDepts.forEach { d ->
                        QChip(
                            label = d, selected = d in dDepts,
                            onClick = { onDraftChange { dDepts = if (d in dDepts) dDepts - d else dDepts + d } },
                        )
                    }
                }
            }
        }

        SectionTitle(text = "更多筛选（不常用，收起）")
        val moreCount = listOf(dChannels.isNotEmpty(), dAmount != "全部", dAbnormal).count { it }
        QChip(
            label = if (moreOpen) "收起更多筛选" else "更多筛选",
            sub = if (moreCount > 0) "已选 $moreCount 组" else null,
            selected = moreOpen,
            onClick = { moreOpen = !moreOpen },
        )
        if (moreOpen) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                QNote("渠道（多选）")
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AdminChannels.forEach { c ->
                        QChip(
                            label = c, selected = c in dChannels,
                            onClick = { onDraftChange { dChannels = if (c in dChannels) dChannels - c else dChannels + c } },
                        )
                    }
                }
                QNote("金额区间（单选）")
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AdminAmountBuckets.forEach { b ->
                        QChip(label = b, selected = dAmount == b, onClick = { onDraftChange { dAmount = b } })
                    }
                }
                QSwitchRow(label = "仅看异常", checked = dAbnormal, hint = "低频条件，收在更多筛选里") { v -> onDraftChange { dAbnormal = v } }
            }
        }

        QButton(
            text = when {
                !batchMode -> "查询"
                pending > 0 -> "查询（$pending 项待生效）"
                else -> "查询（条件未变化）"
            },
            enabled = !batchMode || pending > 0,
            loading = loading,
        ) { runQuery() }

        SectionTitle(text = "结果（本次生效条件）")
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            QTag(QTagSpec("${applied.start.short()} ~ ${applied.end.short()}"))
            if (applied.statuses.isNotEmpty()) QTag(QTagSpec("状态 · ${applied.statuses.size}"))
            if (applied.depts.size == AdminDepts.size) QTag(QTagSpec("全部部门 · ${AdminDepts.size}"))
            else if (applied.depts.isNotEmpty()) QTag(QTagSpec("部门 · ${applied.depts.size}"))
            if (applied.channels.isNotEmpty()) QTag(QTagSpec("渠道 · ${applied.channels.size}"))
            if (applied.amountBucket != "全部") QTag(QTagSpec(applied.amountBucket))
            if (applied.onlyAbnormal) QTag(QTagSpec("仅看异常"))
        }
        QNote("命中 ${rows.size} 条 / ${AdminRows.size} 条 · 上次耗时 450ms · 累计请求 $queryCount 次", accent = true)
        if (batchMode && pending > 0) QNote("结果为上次查询：还有 $pending 项条件待生效，点「查询」才提交。")
        if (rows.isEmpty()) {
            QNote("无命中。建议先关掉「仅看异常」或把时间放宽到近30日——空态要给下一步动作，不只写「暂无数据」。")
        } else {
            rows.take(5).forEach { QRowLine(it) }
            if (rows.size > 5) QNote("… 其余 ${rows.size - 5} 条见导出（Q04）")
        }
    }
}

// ─── Q02 时间区间写明起止 ───────────────────────────────────────────────────

@Composable
private fun PreviewQ02() {
    val today = remember { LocalDate.now() }
    val earliest = remember { today.minusDays((ADMIN_RETENTION_DAYS - 1).toLong()) }
    var presetIndex by remember { mutableIntStateOf(2) }
    var customOpen by remember { mutableStateOf(false) }
    var startOffset by remember { mutableIntStateOf(-6) }
    var endOffset by remember { mutableIntStateOf(0) }
    var vague by remember { mutableStateOf(false) }
    var result by remember { mutableStateOf<String?>(null) }

    val start = if (customOpen) today.plusDays(startOffset.toLong()) else AdminRangePresets[presetIndex].range(today).first
    val end = if (customOpen) today.plusDays(endOffset.toLong()) else AdminRangePresets[presetIndex].range(today).second
    val days = (ChronoUnit.DAYS.between(start, end) + 1).toInt()
    val invalid = days <= 0
    val outOfRetention = start.isBefore(earliest)

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        QSwitchRow(
            label = "模糊文案（反例）",
            checked = vague,
            hint = "开启后按钮只写「近7天」，不写起止——同一句文案两个人能算出两个数。",
        ) { vague = it }

        SectionTitle(text = "快捷区间（写明起止 · 含首尾）")
        Row(
            Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            AdminRangePresets.forEachIndexed { i, p ->
                val (s, e) = p.range(today)
                val enabled = !s.isBefore(earliest)
                QChip(
                    label = if (vague) p.label.replace("近7日", "近7天").replace("近30日", "近30天").replace("近一年", "近1年") else p.label,
                    sub = if (vague) null else "${s.short()} ~ ${e.short()}",
                    selected = !customOpen && presetIndex == i,
                    enabled = enabled,
                    onClick = { customOpen = false; presetIndex = i; result = null },
                )
            }
        }
        if (!customOpen) {
            QNote("已选「${AdminRangePresets[presetIndex].label}」：$start ~ $end，含首尾共 $days 天。", accent = true)
        }
        QNote("超出数据保留期 $ADMIN_RETENTION_DAYS 天的档位置灰不可点：最早可查 ${earliest.short()}（$earliest）。")

        SectionTitle(text = "自定义起止（±1 天步进）")
        QSwitchRow(label = "使用自定义区间", checked = customOpen, hint = "开启后快捷档位失效，按下面的起止算") { customOpen = it }
        if (customOpen) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("开始", style = MaterialTheme.typography.labelMedium)
                QChip("−", selected = false, onClick = { startOffset -= 1; result = null })
                Text(start.toString(), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                QChip("+", selected = false, enabled = startOffset < endOffset, onClick = { startOffset += 1; result = null })
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("结束", style = MaterialTheme.typography.labelMedium)
                QChip("−", selected = false, enabled = endOffset > startOffset, onClick = { endOffset -= 1; result = null })
                Text(end.toString(), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                QChip("+", selected = false, enabled = endOffset < 0, onClick = { endOffset += 1; result = null })
            }
            QNote("结束日期最大钳制到今天（$today），不许选未来；开始可往前推到 ${earliest.short()} 为止。")
        }

        if (invalid) {
            QNote("开始日期晚于结束日期：查询已禁用，请修正起止。", accent = true)
        } else if (outOfRetention) {
            QNote("所选区间超出保留期（最早 $earliest）：查询已禁用。", accent = true)
        }

        QButton(
            text = "查询 ${start.short()} ~ ${end.short()}",
            enabled = !invalid && !outOfRetention,
        ) {
            val hit = filterAdminRows(AdminRows, AdminFilter(start = start, end = end)).size
            result = "$start ~ $end · 共 $days 天（含首尾） · 命中 $hit 条 / ${AdminRows.size} 条"
        }

        SectionTitle(text = "查询口径回显")
        if (result != null) {
            QNote(result!!, accent = true)
        } else {
            QNote("尚未查询。点上方按钮后这里回显明确口径。")
        }
        QNote("口径固定：闭区间 [起, 止]，含当天 00:00–23:59；「今日」= $today，共 1 天。")
    }
}

// ─── Q03 已选条件标签聚合 ───────────────────────────────────────────────────

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PreviewQ03() {
    var depts by remember { mutableStateOf(emptySet<String>()) }
    var statuses by remember { mutableStateOf(emptySet<String>()) }
    var channel by remember { mutableStateOf<String?>(null) }
    var pickerOpen by remember { mutableStateOf(false) }
    var deptTagOpen by remember { mutableStateOf(false) }
    var spreadAll by remember { mutableStateOf(false) }

    val rows = remember(depts, statuses, channel) {
        filterAdminRows(
            AdminRows,
            AdminFilter(
                start = LocalDate.now().minusDays(399), end = LocalDate.now(),
                statuses = statuses, depts = depts, channels = setOfNotNull(channel),
            ),
        )
    }
    val deptAll = depts.size == AdminDepts.size
    val deptAggregated = depts.size > 3

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SectionTitle(text = "选条件（20 个部门 + 状态 + 渠道）")
        Row(
            Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            QChip("全选部门（20）", selected = deptAll, onClick = { depts = if (deptAll) emptySet() else AdminDepts.toSet() })
            QChip("选 4 个部门", selected = depts.size == 4, onClick = { depts = AdminDepts.take(4).toSet() })
            QChip("选 2 个部门", selected = depts.size == 2, onClick = { depts = AdminDepts.take(2).toSet() })
            QChip(if (pickerOpen) "收起部门列表" else "逐个挑部门", selected = pickerOpen, onClick = { pickerOpen = !pickerOpen })
        }
        if (pickerOpen) {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                AdminDepts.forEach { d ->
                    QChip(label = d, selected = d in depts, onClick = { depts = if (d in depts) depts - d else depts + d })
                }
            }
        }
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            AdminStatuses.forEach { s ->
                QChip(label = s, selected = s in statuses, onClick = { statuses = if (s in statuses) statuses - s else statuses + s })
            }
            AdminChannels.take(3).forEach { c ->
                QChip(label = c, selected = channel == c, onClick = { channel = if (channel == c) null else c })
            }
        }

        QSwitchRow(
            label = "摊开所有标签（反例）",
            checked = spreadAll,
            hint = "开启后把已选部门逐个平铺，看条件区被撑成几行。",
        ) { spreadAll = it }

        SectionTitle(text = "已选条件（聚合规则）")
        val tags: List<QTagSpec> = buildList {
            if (spreadAll) {
                depts.sorted().forEach { d -> add(QTagSpec(d, onDelete = { depts = depts - d })) }
            } else if (deptAll) {
                add(QTagSpec("全部部门 · ${AdminDepts.size}", onDelete = { depts = emptySet() }, onClick = { pickerOpen = true }))
            } else if (deptAggregated) {
                add(QTagSpec("部门 · ${depts.size}", onDelete = { depts = emptySet() }, onClick = { deptTagOpen = !deptTagOpen }))
            } else {
                depts.sorted().forEach { d -> add(QTagSpec(d, onDelete = { depts = depts - d })) }
            }
            statuses.sorted().forEach { s -> add(QTagSpec(s, onDelete = { statuses = statuses - s })) }
            channel?.let { c -> add(QTagSpec(c, onDelete = { channel = null })) }
        }
        if (tags.isEmpty()) {
            QNote("未选任何条件：显示全量 ${AdminRows.size} 条。标签区空着比摊一堆强。")
        } else {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                tags.forEach { QTag(it) }
            }
            val perRow = 3
            val approxRows = (tags.size + perRow - 1) / perRow
            if (spreadAll) {
                QNote("反例：${tags.size} 个标签 ≈ $approxRows 行（每行约 $perRow 个），条件区把结果挤出首屏。", accent = true)
            } else {
                QNote("聚合后 ${tags.size} 个标签 ≈ $approxRows 行，控制在 2 行内。", accent = true)
            }
        }
        if (deptTagOpen && deptAggregated && !spreadAll && !deptAll) {
            GlassCard(Modifier.fillMaxWidth(), contentPadding = PaddingValues(12.dp)) {
                QNote("「部门 · ${depts.size}」展开：")
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    depts.sorted().forEach { d -> QTag(QTagSpec(d, onDelete = { depts = depts - d })) }
                }
            }
        }
        QChip("清空全部条件", selected = false, onClick = { depts = emptySet(); statuses = emptySet(); channel = null })
        QNote("命中 ${rows.size} 条 / ${AdminRows.size} 条 · 删标签即刻重算，不需要再点查询。", accent = true)
    }
}

// ─── Q04 看板下钻与明细转交 ─────────────────────────────────────────────────

@Composable
private fun PreviewQ04() {
    val clipboard = LocalClipboardManager.current
    val haptics = LocalHapticFeedback.current
    var dim by remember { mutableStateOf("渠道") }
    var selected by remember { mutableStateOf<String?>(null) }
    var exported by remember { mutableStateOf<String?>(null) }
    var csvPreview by remember { mutableStateOf<String?>(null) }
    var handed by remember { mutableStateOf(false) }
    var note by remember { mutableStateOf<String?>(null) }

    val rangeStart = remember { LocalDate.now().minusDays(29) }
    val rangeEnd = remember { LocalDate.now() }
    val source = remember { filterAdminRows(AdminRows, AdminFilter(start = rangeStart, end = rangeEnd)) }
    val agg = remember(dim, source) {
        val all = aggregateAdminRows(source, dim)
        if (dim == "部门") all.take(8) else all
    }
    val maxAmount = (agg.maxOfOrNull { it.second } ?: 1).coerceAtLeast(1)
    val slice = remember(selected, dim, source) {
        val key = selected
        if (key == null) emptyList() else source.filter {
            when (dim) {
                "渠道" -> it.channel == key
                "状态" -> it.status == key
                else -> it.dept == key
            }
        }
    }
    val sliceAmount = slice.sumOf { it.amount }
    val share = if (source.isEmpty()) 0 else slice.size * 100 / source.size
    val selectedKey = selected // 委托属性不可智能转换，取本地 val 后再查表
    val owner = when {
        selectedKey == null -> ""
        dim == "渠道" -> AdminChannelOwners[selectedKey] ?: "值班组长"
        dim == "状态" -> "数据值班 · 王磊（$selectedKey）"
        else -> "值班组长 · $selectedKey"
    }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SectionTitle(text = "看板（点柱子只看这块）")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("渠道", "部门", "状态").forEach { d ->
                QChip(
                    label = if (d == "部门") "部门前8" else d, selected = dim == d,
                    onClick = {
                        if (dim != d) {
                            dim = d; selected = null; exported = null; csvPreview = null; handed = false
                            note = "已切换维度，切片选择已重置"
                        }
                    },
                )
            }
        }
        QNote("${rangeStart.short()} ~ ${rangeEnd.short()} · 命中 ${source.size} 条 · 金额合计 ¥${source.sumOf { it.amount }}（模拟数据）")
        Row(Modifier.fillMaxWidth().height(146.dp), verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            agg.forEach { (key, amount, count) ->
                val isSelected = selected == key
                Column(
                    Modifier.weight(1f).clickable {
                        selected = if (isSelected) null else key
                        exported = null; csvPreview = null; handed = false; note = null
                    },
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text("¥${amount / 1000}k", style = MaterialTheme.typography.labelSmall, fontSize = 9.sp, color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(2.dp))
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height((8 + 100 * amount / maxAmount).dp)
                            .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.primary.copy(alpha = if (selected == null) 0.55f else 0.22f)
                            )
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        if (key.length > 4) key.take(3) + "…" else key,
                        style = MaterialTheme.typography.labelSmall, fontSize = 9.sp, maxLines = 1,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text("$count 笔", style = MaterialTheme.typography.labelSmall, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        if (note != null) QNote(note!!, accent = true)

        SectionTitle(text = "下钻详情")
        if (selected == null) {
            QNote("点任意一根柱子，这里只看这块：维度标题、金额、笔数、占比、明细一起联动。")
            QNote("（空态给下一步动作，不写「暂无数据」。）")
        } else {
            QNote("$dim · $selectedKey", accent = true)
            QNote("金额合计 ¥$sliceAmount ｜ 笔数 ${slice.size} ｜ 占全量 $share% ｜ 负责人 $owner")
            slice.take(6).forEach { QRowLine(it) }
            if (slice.size > 6) QNote("… 其余 ${slice.size - 6} 条在导出的 CSV 里")
            QButton(text = "导出明细（CSV · ${slice.size} 行）") {
                val csv = adminRowsToCsv(slice)
                clipboard.setText(AnnotatedString(csv))
                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                exported = "已导出 ${slice.size} 行 · ${"%.1f".format(csv.toByteArray(Charsets.UTF_8).size / 1024.0)} KB（已复制到剪贴板）"
                csvPreview = csv.lineSequence().take(4).joinToString("\n")
            }
            QButton(text = if (handed) "✓ 转交话术已复制" else "转给负责人（复制交接话术）", primary = false) {
                val text = adminHandoffText(dim, selectedKey ?: "", AdminFilter(start = rangeStart, end = rangeEnd), sliceAmount, slice.size, share, owner, slice.size)
                clipboard.setText(AnnotatedString(text))
                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                handed = true
            }
            if (exported != null) {
                QNote(exported!!, accent = true)
                if (csvPreview != null) QMono(csvPreview!!)
            }
            if (handed) {
                QNote("转交话术（已复制，可直接粘贴给 $owner）：")
                QMono(adminHandoffText(dim, selectedKey ?: "", AdminFilter(start = rangeStart, end = rangeEnd), sliceAmount, slice.size, share, owner, slice.size))
            }
        }
    }
}

// ─── Q05 四刀合体样板页 ─────────────────────────────────────────────────────

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PreviewQ05() {
    val scope = rememberCoroutineScope()
    val clipboard = LocalClipboardManager.current
    val haptics = LocalHapticFeedback.current
    val today = remember { LocalDate.now() }
    val earliest = remember { today.minusDays((ADMIN_RETENTION_DAYS - 1).toLong()) }

    var moreOpen by remember { mutableStateOf(false) }
    var deptPickerOpen by remember { mutableStateOf(false) }
    var deptTagOpen by remember { mutableStateOf(false) }
    var presetIndex by remember { mutableIntStateOf(2) }

    var dStatuses by remember { mutableStateOf(emptySet<String>()) }
    var dDepts by remember { mutableStateOf(emptySet<String>()) }
    var dChannels by remember { mutableStateOf(emptySet<String>()) }
    var dAmount by remember { mutableStateOf("全部") }
    var dAbnormal by remember { mutableStateOf(false) }

    var applied by remember {
        val r = AdminRangePresets[2].range(LocalDate.now())
        mutableStateOf(AdminFilter(start = r.first, end = r.second))
    }
    var rows by remember { mutableStateOf(filterAdminRows(AdminRows, applied)) }
    var loading by remember { mutableStateOf(false) }
    var queryCount by remember { mutableIntStateOf(0) }

    var dim by remember { mutableStateOf("渠道") }
    var selected by remember { mutableStateOf<String?>(null) }
    var exported by remember { mutableStateOf<String?>(null) }
    var handed by remember { mutableStateOf(false) }
    var note by remember { mutableStateOf<String?>(null) }

    /** 读当前草稿（调用点即时读取，避免捕获旧值）。 */
    fun draftNow(): AdminFilter {
        val r = AdminRangePresets[presetIndex].range(today)
        return AdminFilter(
            start = r.first, end = r.second,
            statuses = dStatuses, depts = dDepts, channels = dChannels,
            amountBucket = dAmount, onlyAbnormal = dAbnormal,
        )
    }
    val presetRange = AdminRangePresets[presetIndex].range(today)
    val presetEnabled = !presetRange.first.isBefore(earliest)
    val pending = if (presetEnabled) diffCount(draftNow(), applied) else -1
    val days = (ChronoUnit.DAYS.between(applied.start, applied.end) + 1).toInt()

    fun runQuery() {
        val f = draftNow()
        if (loading || f.start.isBefore(earliest)) return
        loading = true
        queryCount += 1
        scope.launch {
            delay(450)
            applied = f
            rows = filterAdminRows(AdminRows, f)
            selected = null
            exported = null
            handed = false
            note = "已按当前条件查询，切片选择已重置"
            loading = false
        }
    }

    val agg = remember(dim, rows) {
        val all = aggregateAdminRows(rows, dim)
        if (dim == "部门") all.take(8) else all
    }
    val maxAmount = (agg.maxOfOrNull { it.second } ?: 1).coerceAtLeast(1)
    val slice = remember(selected, dim, rows) {
        val key = selected
        if (key == null) rows else rows.filter {
            when (dim) {
                "渠道" -> it.channel == key
                "状态" -> it.status == key
                else -> it.dept == key
            }
        }
    }
    val sliceAmount = slice.sumOf { it.amount }
    val share = if (rows.isEmpty()) 0 else slice.size * 100 / rows.size
    val selectedKey = selected // 委托属性不可智能转换，取本地 val 后再查表
    val owner = when {
        selectedKey == null -> ""
        dim == "渠道" -> AdminChannelOwners[selectedKey] ?: "值班组长"
        dim == "状态" -> "数据值班 · 王磊（$selectedKey）"
        else -> "值班组长 · $selectedKey"
    }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SectionTitle(text = "① 时间（写明起止 · 超限置灰）")
        Row(
            Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            AdminRangePresets.forEachIndexed { i, p ->
                val (s, e) = p.range(today)
                QChip(
                    label = p.label,
                    sub = "${s.short()} ~ ${e.short()}",
                    selected = presetIndex == i,
                    enabled = !s.isBefore(earliest),
                    onClick = { presetIndex = i },
                )
            }
        }
        if (!presetEnabled) {
            QNote("当前档位超出保留期 $ADMIN_RETENTION_DAYS 天（最早 ${earliest.short()}）：查询已禁用。", accent = true)
        } else {
            QNote("口径：含首尾，共 ${(ChronoUnit.DAYS.between(presetRange.first, presetRange.second) + 1).toInt()} 天")
        }

        SectionTitle(text = "② 常用条件（常显）")
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            AdminStatuses.forEach { s ->
                QChip(label = s, selected = s in dStatuses, onClick = { dStatuses = if (s in dStatuses) dStatuses - s else dStatuses + s })
            }
            val deptAll = dDepts.size == AdminDepts.size
            QChip(
                label = when {
                    deptAll -> "全部部门 · ${AdminDepts.size}"
                    dDepts.isEmpty() -> "选择部门"
                    else -> "部门 · ${dDepts.size}"
                },
                selected = dDepts.isNotEmpty(),
                sub = if (dDepts != applied.depts) "待生效" else null,
                onClick = { deptPickerOpen = !deptPickerOpen },
            )
        }
        if (deptPickerOpen) {
            GlassCard(Modifier.fillMaxWidth(), contentPadding = PaddingValues(12.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    QChip(
                        "全选", selected = dDepts.size == AdminDepts.size,
                        onClick = { dDepts = if (dDepts.size == AdminDepts.size) emptySet() else AdminDepts.toSet() },
                    )
                    QChip("清空", selected = false, onClick = { dDepts = emptySet() })
                }
                Spacer(Modifier.height(6.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    AdminDepts.forEach { d ->
                        QChip(label = d, selected = d in dDepts, onClick = { dDepts = if (d in dDepts) dDepts - d else dDepts + d })
                    }
                }
            }
        }

        SectionTitle(text = "③ 更多筛选（收起）")
        val moreCount = listOf(dChannels.isNotEmpty(), dAmount != "全部", dAbnormal).count { it }
        QChip(
            label = if (moreOpen) "收起更多筛选" else "更多筛选",
            sub = if (moreCount > 0) "已选 $moreCount 组" else null,
            selected = moreOpen,
            onClick = { moreOpen = !moreOpen },
        )
        if (moreOpen) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AdminChannels.forEach { c ->
                        QChip(label = c, selected = c in dChannels, onClick = { dChannels = if (c in dChannels) dChannels - c else dChannels + c })
                    }
                }
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AdminAmountBuckets.forEach { b -> QChip(label = b, selected = dAmount == b, onClick = { dAmount = b }) }
                }
                QSwitchRow(label = "仅看异常", checked = dAbnormal) { dAbnormal = it }
            }
        }

        SectionTitle(text = "④ 已选条件标签（聚合 · 可删）")
        val tags: List<QTagSpec> = buildList {
            add(QTagSpec("${applied.start.short()} ~ ${applied.end.short()}（$days 天）"))
            applied.statuses.sorted().forEach { s -> add(QTagSpec(s, onDelete = { dStatuses = dStatuses - s; runQuery() })) }
            when {
                applied.depts.size == AdminDepts.size ->
                    add(QTagSpec("全部部门 · ${AdminDepts.size}", onDelete = { dDepts = emptySet(); runQuery() }, onClick = { deptPickerOpen = true }))
                applied.depts.size > 3 ->
                    add(QTagSpec("部门 · ${applied.depts.size}", onDelete = { dDepts = emptySet(); runQuery() }, onClick = { deptTagOpen = !deptTagOpen }))
                else -> applied.depts.sorted().forEach { d -> add(QTagSpec(d, onDelete = { dDepts = dDepts - d; runQuery() })) }
            }
            if (applied.channels.isNotEmpty()) add(QTagSpec("渠道 · ${applied.channels.size}", onDelete = { dChannels = emptySet(); runQuery() }))
            if (applied.amountBucket != "全部") add(QTagSpec(applied.amountBucket, onDelete = { dAmount = "全部"; runQuery() }))
            if (applied.onlyAbnormal) add(QTagSpec("仅看异常", onDelete = { dAbnormal = false; runQuery() }))
        }
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            tags.forEach { QTag(it) }
        }
        if (deptTagOpen && applied.depts.size in 4 until AdminDepts.size) {
            QNote("「部门 · ${applied.depts.size}」展开：${applied.depts.sorted().joinToString("、")}")
        }
        QChip(
            label = "清空全部条件", selected = false,
            onClick = {
                dStatuses = emptySet(); dDepts = emptySet(); dChannels = emptySet()
                dAmount = "全部"; dAbnormal = false; presetIndex = 2
                runQuery()
            },
        )

        SectionTitle(text = "⑤ 查询（批量提交）")
        QButton(
            text = if (pending > 0) "查询（$pending 项待生效）" else "查询（条件未变化）",
            enabled = pending > 0,
            loading = loading,
        ) { runQuery() }
        QNote("累计请求 $queryCount 次 · 上次耗时 450ms · 改 3 个条件只发 1 次请求", accent = true)
        if (pending > 0) QNote("下面看板与明细仍是上次查询结果（有 $pending 项待生效）。")

        SectionTitle(text = "⑥ 结果概览")
        QNote(
            "命中 ${rows.size} 条 / ${AdminRows.size} 条 · 金额合计 ¥${rows.sumOf { it.amount }} · 时间 ${applied.start.short()} ~ ${applied.end.short()}（含首尾 $days 天）",
            accent = true,
        )
        if (note != null) QNote(note!!)

        SectionTitle(text = "⑦ 看板下钻")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("渠道", "部门", "状态").forEach { d ->
                QChip(
                    label = if (d == "部门") "部门前8" else d, selected = dim == d,
                    onClick = {
                        if (dim != d) {
                            dim = d; selected = null; exported = null; handed = false
                            note = "已切换维度，切片选择已重置"
                        }
                    },
                )
            }
        }
        if (rows.isEmpty()) {
            QNote("当前条件无命中：先放宽时间或清空「仅看异常」，再来看板。")
        } else {
            Row(Modifier.fillMaxWidth().height(136.dp), verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                agg.forEach { (key, amount, count) ->
                    val isSelected = selected == key
                    Column(
                        Modifier.weight(1f).clickable {
                            selected = if (isSelected) null else key
                            exported = null; handed = false; note = null
                        },
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text("¥${amount / 1000}k", style = MaterialTheme.typography.labelSmall, fontSize = 9.sp, color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(2.dp))
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .height((8 + 92 * amount / maxAmount).dp)
                                .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.primary.copy(alpha = if (selected == null) 0.55f else 0.22f)
                                )
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(if (key.length > 4) key.take(3) + "…" else key, style = MaterialTheme.typography.labelSmall, fontSize = 9.sp, maxLines = 1, color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("$count 笔", style = MaterialTheme.typography.labelSmall, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        SectionTitle(text = "⑧ 明细（当前切片）")
        if (selected == null) QNote("未下钻：显示全部命中前 8 行。点柱子可只看某一维度。")
        else QNote("$dim · $selectedKey ｜ ¥$sliceAmount ｜ ${slice.size} 笔 ｜ 占 $share% ｜ 负责人 $owner", accent = true)
        slice.take(8).forEach { QRowLine(it) }
        if (slice.size > 8) QNote("… 其余 ${slice.size - 8} 条在导出的 CSV 里")

        SectionTitle(text = "⑨ 导出与转交")
        QButton(text = "导出明细（CSV · ${slice.size} 行）", enabled = slice.isNotEmpty()) {
            val csv = adminRowsToCsv(slice)
            clipboard.setText(AnnotatedString(csv))
            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
            exported = "已导出 ${slice.size} 行 · ${"%.1f".format(csv.toByteArray(Charsets.UTF_8).size / 1024.0)} KB（已复制到剪贴板）"
        }
        QButton(
            text = if (handed) "✓ 转交话术已复制" else "转给负责人（复制交接话术）",
            primary = false,
            enabled = selected != null,
        ) {
            clipboard.setText(
                AnnotatedString(adminHandoffText(dim, selectedKey ?: "全部", applied, sliceAmount, slice.size, share, owner, slice.size))
            )
            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
            handed = true
        }
        if (selected == null) QNote("未选切片时「转给负责人」禁用：不知道转哪块就不该让人转。")
        if (exported != null) QNote(exported!!, accent = true)
        if (handed && selected != null) QNote("转交话术已复制，可直接粘贴给 $owner。")
        QNote("本页数据为本地生成的模拟数据（${AdminRows.size} 行 / 跨 400 天），不请求网络、不写文件；导出 = 复制到剪贴板。")
    }
}
