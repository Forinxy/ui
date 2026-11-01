package com.monkeycode.liquidui.ui.features.adminquery

import java.time.LocalDate

/**
 * 后台数据查询规范（手册 F16）目录数据与模拟数据集。
 *
 * 规范条文提炼自 B 站 UP「KD一对A」《查数据查到崩溃？这套烂后台连改了四刀》
 * （BV1Vuxr6BEDp，2026-10-31，标签：B端设计 / 后台设计 / 交互设计 / 设计规范），
 * 本仓库按其公开表述独立实现为真实可操作的 Compose 演示，未复制其任何视频素材；
 * 出处与口径见 CREDITS.md 与 素材库/后台数据查询规范/参考来源.md。
 *
 * 四刀：筛选分层 + 慢查询批量提交 / 时间区间写明起止并超限置灰 /
 * 已选条件标签聚合 / 看板点柱下钻并导出明细转负责人。
 */
data class AdminQueryNorm(
    val id: String,
    val name: String,
    val category: String,
    val difficulty: String,
    val summary: String,
    val rule: String,
    val badCase: String,
    val params: String,
    val prompt: String,
)

val AdminQueryCategories = listOf("筛选", "时间", "标签", "看板", "组合")

/** 规范条目（Q01–Q05）。Q 编号独立于手册 F 编号与 UI2 的 F 编号，引用时写「Q02」。 */
val AdminQueryNorms: List<AdminQueryNorm> = listOf(
    AdminQueryNorm(
        id = "Q01",
        name = "筛选分层与批量提交",
        category = "筛选",
        difficulty = "★★☆",
        summary = "常用条件常显、不常用收进「更多筛选」；查询慢时改成选完一起查，用一颗按钮提交，而不是每改一个条件就打一次接口。",
        rule = """1. 条件按使用频率分层：高频（时间、状态、部门）常显，低频（渠道、金额区间、仅看异常）收进「更多筛选」，折叠时若有已选值必须在入口上显示计数徽标（更多筛选 ·2）。
2. 单次查询耗时 > 300ms 或条件互相牵制时，启用批量提交：改条件只更新「待生效草稿」，不请求；出现「查询（n 项待生效）」按钮，点了才发一次请求。
3. 结果区必须回显本次生效的条件与耗时，草稿与已生效条件视觉可区分（草稿态加「待生效」标记），避免用户以为已经查过了。
4. 请求期间按钮进入 loading 且不可重复点击，回来前不允许再发第二次；连续改 10 个条件只应产生 1 次请求。""",
        badCase = "20 个筛选条件全摊在页面上占三屏；每勾一个复选框就发一次请求，页面转圈十几次，接口被打爆，用户还不知道哪次是最后的结果。",
        params = "分层阈值：常显 ≤4 组 · 折叠入口带计数徽标 · 批量提交触发线 300ms · 模拟耗时 450ms · 请求计数实时可见",
        prompt = """做一个后台查询条件区，实现「常用外置 + 不常用收起 + 慢查询批量提交」。
【分层】高频条件（时间快捷、状态、部门）常显；低频条件（渠道多选、金额区间、仅看异常开关）收进「更多筛选」折叠区，折叠时入口显示已选计数徽标（如「更多筛选 ·2」），展开后可逐项修改。
【批量提交】提供「即时查询 / 选完一起查」模式开关。即时模式：任一条件变化立刻执行查询并累加请求计数；批量模式：条件变化只写入草稿，不动结果，底部出现「查询（n 项待生效）」按钮，点击才执行一次查询；草稿与已生效条件在视觉上可区分（草稿项加「待生效」小标）。
【执行】查询为异步：进入 loading（450ms 模拟耗时），期间按钮禁用防重复提交；完成后回显「命中 n 条 · 耗时 450ms · 累计请求 m 次」与前 5 行明细。
【反馈】结果区顶部用一行标签回显本次生效的全部条件；无命中时给可操作的空态（提示放宽哪个条件），不要只写「暂无数据」。
【自检】批量模式下连改 10 个条件，请求计数只 +1；折叠区有已选值时入口徽标数字正确；loading 中连点按钮不产生并发请求；切回即时模式后每次改动请求计数 +1。""",
    ),
    AdminQueryNorm(
        id = "Q02",
        name = "时间区间写明起止",
        category = "时间",
        difficulty = "★☆☆",
        summary = "快捷区间必须写出明确起止日期、说清含不含当天；超出数据保留期的选项直接置灰并给出原因，不要让用户自己算「近7天算不算今天」。",
        rule = """1. 快捷区间（今日 / 昨日 / 近7日 / 近30日 / 本季度 / 近一年）每个都要写明实际起止日期，例如「近7日 10-26 ~ 11-01」，并标注口径「含首尾，共 7 天」。
2. 超出数据保留期（示例设 90 天）的选项置灰不可点，就地给出原因与最早可查日期，而不是点了之后报错。
3. 自定义起止必须做校验：起 > 止 时禁止查询并说明原因；止 > 今天时钳制到今天。
4. 禁止只写「近7天」这类模糊文案——同一条文案在不同人嘴里含不含当天不一样，是后台最常见的扯皮来源。""",
        badCase = "按钮只写「近7天」，用户 A 以为含今天、用户 B 以为不含，两人对同一份报表数出两个数；选「近一年」点了才弹「超出查询范围」，白等一次 loading。",
        params = "保留期 90 天 · 快捷区间 6 档 · 口径「含首尾」 · 自定义步进 ±1 天 · 起止倒置即禁用查询",
        prompt = """做后台时间范围选择器，实现「写明起止 + 超限置灰 + 倒置校验」。
【快捷区间】今日 / 昨日 / 近7日 / 近30日 / 本季度 / 近一年六档；每档按钮内两行：第一行档位名，第二行实际起止日期（MM-dd ~ MM-dd），选中项额外显示口径「含首尾，共 n 天」。
【保留期】数据保留期 90 天：起止早于「今天 - 89 天」的档位禁用（灰底、不可点、降低不透明度），并在下方一行说明「超出数据保留期 90 天，最早可查 MM-dd」。
【自定义】起止各一组 -/+ 天按钮与日期文本；起 > 止 时标红并禁用查询按钮，文案说明「开始日期晚于结束日期」；止 > 今天时钳制回今天。
【对照】提供「模糊文案（反例）」开关：开启后按钮只显示「近7天」这类字样，用于对比歧义；关闭时恢复明确起止。
【执行】点查询后按 [起, 止] 闭区间过滤数据，回显「MM-dd ~ MM-dd · 共 n 天 · 命中 m 条」。
【自检】今天为任意日期时六档的起止与天数都自洽；禁用档点击无反应且原因可见；起止倒置时查询按钮不可用；含首尾天数 = 止 - 起 + 1。""",
    ),
    AdminQueryNorm(
        id = "Q03",
        name = "已选条件标签聚合",
        category = "标签",
        difficulty = "★★☆",
        summary = "部门全选就收成一个「全部部门 · 20」标签，选超过阈值收成一个「部门 · n」，不要把二十个小组全摊在条件栏里把结果挤出屏幕。",
        rule = """1. 同维度全选时聚合为一个标签：「全部部门 · 20」，并保留可点开的入口查看/编辑具体项。
2. 同维度已选数量超过阈值（示例 3）时聚合为「部门 · n」，点开展开明细；≤ 阈值时逐个显示，每个标签带独立删除（×）。
3. 标签区必须限高（示例最多 2 行），超出用「+n」或聚合标签承接，条件栏不许把结果区挤出首屏。
4. 删除标签即回退对应筛选并重新计算命中数；提供「清空全部条件」且清空后回到默认状态。
5. 反例：把 20 个小组逐个摊成标签，条件区占五行，用户滚动才能看到数据——这是最常见的一刀。""",
        badCase = "勾了「全部部门」，条件栏哗啦摊出 20 个小组标签占满五行，结果表格被挤到屏幕外；删条件要一个个点 ×，点 20 次。",
        params = "维度 20 个部门 · 聚合阈值 >3 · 全选聚合「全部部门 · 20」 · 标签区限高 2 行 · 单标签可删 + 一键清空",
        prompt = """做后台「已选条件」标签区，实现聚合规则。
【数据】部门维度共 20 个小组，另含状态、渠道两个维度；标签区显示当前所有已选条件。
【聚合】同维度全选 → 一个标签「全部部门 · 20」；同维度已选 > 3 → 一个标签「部门 · n」，点按展开逐项列出（展开态可再收起）；≤ 3 → 逐个标签显示。非多选维度（如状态=已退款）直接一个标签。
【编辑】每个标签右侧 × 可单独删除并立即重算命中数；聚合标签点按可打开部门选择器（20 项多选 + 全选/清空）；提供「清空全部条件」回到默认。
【限高】标签区最多 2 行，超出以聚合标签或「+n」承接；提供「摊开所有标签（反例）」开关，开启后逐个平铺 20 个标签并显示占了几行，用于对比。
【反馈】标签变化时同步显示「命中 n 条」，全清空时显示默认全量条数。
【自检】全选 20 个部门时标签区只有 1 个标签；选 4 个时收成「部门 · 4」；选 3 个时是 3 个独立标签；删任一标签命中数立刻变化；反例开关打开后标签区行数明显增加。""",
    ),
    AdminQueryNorm(
        id = "Q04",
        name = "看板下钻与明细转交",
        category = "看板",
        difficulty = "★★☆",
        summary = "点一根柱子，旁边就只看这块：维度标题、聚合数、明细一起联动；找到问题直接导出明细 CSV，再一键生成转交话术发给负责人。",
        rule = """1. 图表每根柱子/扇区都可点，点选后详情区只呈现该维度切片：标题写明「渠道 · 直播」，聚合数（金额、笔数、占比）与明细同步过滤。
2. 未选中时给可操作的引导空态（「点任意一根柱子，这里只看这块」），不要只写「暂无数据」。
3. 切换聚合维度时清空已选切片并说明原因，避免旧切片名与新维度不匹配。
4. 明细必须可导出：生成带表头的 CSV，回显行数与体积，并展示前几行预览供核对；导出动作给触感/提示反馈。
5. 发现问题要能就地转交：一键生成含维度、时间范围、金额、笔数、占比、负责人的交接文本，复制即可粘贴给对应负责人，不用回聊天窗口手打。""",
        badCase = "柱子只能看不能点，想知道「直播渠道到底哪几笔异常」只能回去改筛选条件重查；查到了还得自己抄数字到聊天框，负责人问「哪天的」又要再查一遍。",
        params = "维度 3 组（渠道 / 部门前 8 / 状态）· 柱高按金额归一 · 下钻联动明细 · CSV 含表头 · 转交文本含 6 要素",
        prompt = """做可下钻的后台看板，实现「点柱只看这块 + 导出明细 + 转负责人」。
【图表】按维度（渠道 / 部门前 8 / 状态）聚合金额，横向并排柱子，柱高按最大值归一；每根柱子可点，选中项用主色高亮、其余降不透明度，柱下显示维度名与金额。
【下钻】未选中时详情区显示引导空态「点任意一根柱子，这里只看这块」；选中后标题变为「维度 · 值」，显示金额合计、笔数、占全量比例、负责人，并列出该切片前 6 行明细（日期 / 部门 / 渠道 / 状态 / 金额）。
【维度切换】切换维度时清空选中切片，并提示「已切换维度，切片选择已重置」。
【导出】「导出明细（CSV）」把当前切片全部行生成 CSV（表头 date,dept,channel,status,amount,owner），复制到剪贴板 + 触感反馈，回显「已导出 n 行 · x.x KB」并预览前 3 行。
【转交】「转给负责人」生成交接文本（维度=值｜时间范围｜金额｜笔数｜占比｜负责人｜已导出行数），点按复制并提示可粘贴给负责人。
【自检】点任一柱子详情与明细同步变化；导出行数 = 明细笔数；切维度后选中态清空；未选中时无 misleading 的旧数据残留。""",
    ),
    AdminQueryNorm(
        id = "Q05",
        name = "四刀合体样板页",
        category = "组合",
        difficulty = "★★★",
        summary = "把筛选分层、时间起止、标签聚合、看板下钻装进同一个查询页，作为可直接照抄的后台样板：一次查询、条件回显、下钻导出转交全链路闭环。",
        rule = """1. 页面顺序固定：条件区（常用外置 + 更多收起）→ 时间快捷（写明起止、超限置灰）→ 已选条件标签（聚合）→ 查询按钮（批量提交）→ 结果概览 → 看板 → 下钻明细 → 导出/转交。
2. 四刀的状态必须互相一致：标签区反映全部生效条件（含时间与部门聚合）、看板与明细只受生效条件影响、导出内容等于当前下钻切片。
3. 草稿条件与生效条件严格分离，未按查询前看板与明细保持上一次结果并标注「结果为上次查询」。
4. 全链路都要有回显：命中条数、耗时、累计请求次数、时间口径、切片占比，用户任何时刻都能回答「我现在看的是什么」。""",
        badCase = "四条规范各做各的：筛选改了时间没跟着变，标签区漏掉时间条件，导出的明细还是全量而不是当前切片——用户拿到的表和屏幕上的数对不上。",
        params = "条件区 4 组常显 + 3 组折叠 · 时间 6 档 · 标签聚合阈值 >3 · 看板 3 维度 · 单次查询 450ms",
        prompt = """做一个完整的后台数据查询页，把四条规范装进同一页面并保持状态一致。
【结构】自上而下：① 条件区（常显：时间快捷、状态、部门聚合入口；折叠：渠道多选、金额区间、仅看异常）② 已选条件标签区（聚合规则：全选→「全部部门 · 20」，>3→「部门 · n」，每个可删，附「清空全部条件」）③ 查询按钮（批量提交，显示待生效项数与累计请求次数）④ 结果概览（命中条数、金额合计、耗时、时间口径）⑤ 看板（3 维度柱状，可点柱下钻）⑥ 明细（当前切片前 8 行）⑦ 导出 CSV 与转负责人。
【时间】快捷档位写明起止日期与「含首尾，共 n 天」；超出 90 天保留期的档位置灰并给原因；自定义起止倒置时禁用查询。
【一致性】未按查询前，看板与明细保持上次结果并标注「结果为上次查询（条件有 n 项待生效）」；查询后标签区、概览、看板、明细同时更新；导出内容 = 当前下钻切片的全部行；切维度清空切片。
【反馈】查询期间按钮 loading 禁用；导出与转交均复制到剪贴板并给触感反馈与文字回执。
【自检】改 3 个条件只发 1 次请求；全选部门时标签区仅 1 个聚合标签；超限时间档不可点；点柱后导出行数 = 明细笔数；清空条件后回到全量默认态。""",
    ),
)

// ─── 模拟数据集（本地生成，不请求网络） ─────────────────────────────────────

/** 明细行。字段名即 CSV 表头，导出时直接映射。 */
data class AdminRow(
    val date: LocalDate,
    val dept: String,
    val channel: String,
    val status: String,
    val amount: Int,
    val owner: String,
)

/** 20 个部门小组：Q03 标签聚合规则的样本量来源。 */
val AdminDepts: List<String> = (1..20).map { "第%02d组".format(it) }

val AdminChannels: List<String> = listOf("App", "小程序", "直播", "线下", "代理商")

val AdminStatuses: List<String> = listOf("已完成", "处理中", "已退款", "异常")

val AdminAmountBuckets: List<String> = listOf("全部", "< 1k", "1k–5k", "> 5k")

/** 数据保留期（天）：Q02 超限置灰的判定线。 */
const val ADMIN_RETENTION_DAYS = 90

/** 渠道 → 负责人（模拟）。 */
val AdminChannelOwners: Map<String, String> = mapOf(
    "App" to "陈曦 · 增长组",
    "小程序" to "林涛 · 平台组",
    "直播" to "赵敏 · 内容电商组",
    "线下" to "孙浩 · 渠道组",
    "代理商" to "周颖 · 商务组",
)

/**
 * 600 行模拟明细，按固定种子的线性同余生成：同一台机器每次进入结果一致，
 * 便于验收「命中条数」「导出行数」是否自洽。数据为本地生成，不涉及任何真实业务。
 */
val AdminRows: List<AdminRow> = run {
    val today = LocalDate.now()
    var seed = 20261031L
    fun next(): Int {
        seed = seed * 6364136223846793005L + 1442695040888963407L
        return ((seed ushr 33) % 100_000).toInt()
    }
    (0 until 600).map { i ->
        val r = next()
        val channel = AdminChannels[(r / 7) % AdminChannels.size]
        val status = AdminStatuses[(r / 13) % AdminStatuses.size]
        AdminRow(
            date = today.minusDays(((i * 7 + r) % 400).toLong()),
            dept = AdminDepts[r % AdminDepts.size],
            channel = channel,
            status = status,
            amount = 120 + (r * 37) % 9_800,
            owner = if (status == "异常") AdminChannelOwners[channel] ?: "值班组长" else "值班组长 · ${AdminDepts[(r / 3) % AdminDepts.size]}",
        )
    }
}

/** 时间快捷档位。days=区间天数；本季度按季度首日实算。 */
data class AdminRangePreset(val label: String, val days: Int, val quarterBased: Boolean = false)

val AdminRangePresets: List<AdminRangePreset> = listOf(
    AdminRangePreset("今日", 1),
    AdminRangePreset("昨日", 1), // 区间由 range() 特例给出：昨天单日
    AdminRangePreset("近7日", 7),
    AdminRangePreset("近30日", 30),
    AdminRangePreset("本季度", 0, quarterBased = true),
    AdminRangePreset("近一年", 365),
)

/** 档位实际起止（含首尾）。本季度 = 季度首日 ~ 今天；昨日 = 昨天单日起两天窗口取昨天。 */
fun AdminRangePreset.range(today: LocalDate): Pair<LocalDate, LocalDate> = when {
    quarterBased -> {
        val firstMonthOfQuarter = ((today.monthValue - 1) / 3) * 3 + 1
        LocalDate.of(today.year, firstMonthOfQuarter, 1) to today
    }
    label == "昨日" -> today.minusDays(1) to today.minusDays(1)
    else -> today.minusDays((days - 1).toLong()) to today
}

/** 生效条件（已提交查询的那一份）。 */
data class AdminFilter(
    val start: LocalDate,
    val end: LocalDate,
    val statuses: Set<String> = emptySet(),
    val depts: Set<String> = emptySet(),
    val channels: Set<String> = emptySet(),
    val amountBucket: String = "全部",
    val onlyAbnormal: Boolean = false,
) {
    /** 条件项数（用于「n 项待生效」计数）。 */
    val conditionCount: Int
        get() = listOf(
            statuses.isNotEmpty(), depts.isNotEmpty(), channels.isNotEmpty(),
            amountBucket != "全部", onlyAbnormal,
        ).count { it } + 1 // 时间始终算 1 项
}

/** 纯函数：按生效条件过滤明细。 */
fun filterAdminRows(rows: List<AdminRow>, f: AdminFilter): List<AdminRow> = rows.filter { r ->
    !r.date.isBefore(f.start) && !r.date.isAfter(f.end) &&
        (f.statuses.isEmpty() || r.status in f.statuses) &&
        (f.depts.isEmpty() || r.dept in f.depts) &&
        (f.channels.isEmpty() || r.channel in f.channels) &&
        (f.amountBucket == "全部" || when (f.amountBucket) {
            "< 1k" -> r.amount < 1_000
            "1k–5k" -> r.amount in 1_000..5_000
            else -> r.amount > 5_000
        }) &&
        (!f.onlyAbnormal || r.status == "异常")
}

/** 纯函数：按维度聚合金额与笔数，降序。 */
fun aggregateAdminRows(rows: List<AdminRow>, dim: String): List<Triple<String, Int, Int>> {
    val key: (AdminRow) -> String = when (dim) {
        "渠道" -> { r -> r.channel }
        "状态" -> { r -> r.status }
        else -> { r -> r.dept }
    }
    return rows.groupBy(key)
        .map { (k, list) -> Triple(k, list.sumOf { it.amount }, list.size) }
        .sortedByDescending { it.second }
}

/** 纯函数：切片 → CSV（表头与 AdminRow 字段一一对应）。 */
fun adminRowsToCsv(rows: List<AdminRow>): String = buildString {
    appendLine("date,dept,channel,status,amount,owner")
    rows.forEach { r ->
        appendLine("${r.date},${r.dept},${r.channel},${r.status},${r.amount},${r.owner}")
    }
}

/** 纯函数：转交话术六要素（维度、时间、金额、笔数、占比、负责人）。 */
fun adminHandoffText(
    dim: String,
    slice: String,
    filter: AdminFilter,
    amount: Int,
    count: Int,
    sharePercent: Int,
    owner: String,
    exportedRows: Int,
): String = buildString {
    appendLine("【后台数据转交】$dim = $slice")
    appendLine("时间范围：${filter.start} ~ ${filter.end}（含首尾）")
    appendLine("金额合计：¥$amount｜笔数：$count｜占全量：$sharePercent%")
    appendLine("负责人：$owner")
    appendLine("明细：已导出 $exportedRows 行 CSV，可直接核对")
    append("请确认口径后回复处理结论；如需扩大范围请在同一时间口径下追加。")
}
