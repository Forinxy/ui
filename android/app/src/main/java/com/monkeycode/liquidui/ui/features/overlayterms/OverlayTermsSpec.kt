package com.monkeycode.liquidui.ui.features.overlayterms

/**
 * 弹窗与提示术语（手册 F17）目录数据：12 个「突然冒出来的东西」到底各叫什么、
 * 什么时候用哪个、与邻居的区别、以及在 Android / Jetpack Compose 里的对应实现。
 *
 * P01–P06 六个术语来自 B 站 ZTough《Vibe Coding 术语课：别再管所有弹窗都叫「弹窗」了》
 * （BV14Utf6QEnB，2026-09-03，时长 04:45）视频页面公开简介点名（Alert、Toast、Modal、
 * Drawer、Popconfirm、Tooltip）。P07–P12 是本仓库按前端通用体系补足的同族术语
 * （视频页面文字未点名，标注见 素材库/弹窗与提示术语/参考来源.md）。
 * 定义、区别对照、反例与提示词全部为本仓库独立撰写，未复制视频画面/音频/字幕。
 */
data class OverlayTerm(
    val id: String,
    val en: String,
    val name: String,
    val category: String,
    val difficulty: String,
    val summary: String,
    val definition: String,
    val whenToUse: String,
    val vsNeighbor: String,
    val androidImpl: String,
    val badCase: String,
    val params: String,
    val prompt: String,
)

val OverlayCategories = listOf("阻塞类", "轻提示类", "锚定类", "面板类", "状态类")

/** 12 条术语（P 编号独立于手册 F 编号与 UI2 的 F 编号，引用时写「P05」）。 */
val OverlayTerms: List<OverlayTerm> = listOf(
    OverlayTerm(
        id = "P01", en = "Alert", name = "警告框", category = "阻塞类", difficulty = "★☆☆",
        summary = "系统级阻塞提示：只告知一件事，通常只有一个「知道了」，用户必须确认才能继续。",
        definition = "Alert 是最强的打断：出现在页面之上、带遮罩、抢走焦点，内容通常是「已经发生或即将发生的、用户必须知道的事实」，动作只有一个确认。它不征求选择，只要求知晓。",
        whenToUse = "权限被拒后需要解释、存储空间不足、会话即将过期、不可逆操作已经发生（不是即将发生）。判据：如果用户不点掉这条提示就可能造成损失，才用 Alert。",
        vsNeighbor = "与 Modal 的区别：Modal 里通常有表单/多个动作，是「让你在这做完一件事」；Alert 是「让你知道一件事」。与 Toast/Snackbar 的区别：那两者不打断、会自动消失，Alert 不会自己走。",
        androidImpl = "AlertDialog 只给 confirmButton、不给 dismissButton；语义上不可绕过时把 dismissOnBackPress / dismissOnClickOutside 设为 false，文案用陈述句 + 单一动作（「知道了」而不是「确定/取消」）。",
        badCase = "把「保存成功」做成 Alert——用户每次保存都要多点一下，这是最常见的滥用；也不要用 Alert 问「你要 A 还是 B」（那是 Modal 或 ActionSheet 的活）。",
        params = "单按钮 · 遮罩不可点穿 · 标题 ≤12 字 · 正文 ≤2 行 · 按钮文案「知道了」",
        prompt = """实现一个 Alert（警告框），并把它和 Modal、Toast 区分开。
【语义】Alert = 阻塞式告知，只有一个确认动作，不征求选择；用户不处理就不能继续。
【Web】用 <div role="alertdialog" aria-modal="true"> + 遮罩层；焦点进弹窗后 trapped（Tab 只在弹窗内循环），Esc 是否可关由「是否可绕过」决定；关闭后焦点回到触发元素。
【Android/Compose】用 Material3 AlertDialog，只传 confirmButton（文案「知道了」），不传 dismissButton；不可绕过时 DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)。
【内容】标题是事实陈述（如「存储即将写满」），正文一句话说清后果与建议动作，不要写成疑问句。
【禁用场景】成功提示、可选操作、需要输入的内容——分别应改用 Toast/Snackbar、ActionSheet、Modal。
【自检】弹窗出现时背景不可交互；关闭后焦点/滚动位置回到原处；同一流程里 Alert 出现次数 ≤1（多了说明设计有问题）。""",
    ),
    OverlayTerm(
        id = "P02", en = "Toast", name = "轻提示", category = "轻提示类", difficulty = "★☆☆",
        summary = "最弱的一档反馈：短暂浮出、自动消失、不可交互、不打断当前操作。",
        definition = "Toast 只是「回个话」，告诉用户刚才那一下生效了。它没有按钮、没有遮罩，几秒后自己消失，用户不看也不影响继续操作。",
        whenToUse = "复制成功、已保存草稿、已加入收藏、表单某一项已生效。判据：这条信息错过了也没关系，不需要用户做任何事。",
        vsNeighbor = "与 Snackbar 的区别：Snackbar 可以带一个操作（撤销）且在你的界面内，Toast 是系统级、无操作。与 Alert 的区别：Alert 阻塞、必须点掉，Toast 完全不阻塞。",
        androidImpl = "系统 Toast.makeText(context, text, LENGTH_SHORT).show()：零成本但样式受限——Android 12+ 后台不显示、文本长度有限、不能加图标/按钮。要品牌样式或多条堆叠时用 Compose 自绘轻提示（状态驱动 + LaunchedEffect 定时清空），语义保持不变：不打断、自动消失、无操作。",
        badCase = "用 Toast 报错误（用户还没读完就没了，且无法重试）；把 Toast 当确认框用（「已删除」却没有撤销入口）；一次操作连弹三条 Toast。",
        params = "停留 2.0–2.2s · 自动消失 · 无按钮 · 同时最多 1 条（新的顶掉旧的）",
        prompt = """实现 Toast（轻提示），并提供「系统 Toast」与「自绘轻提示」两种做法。
【语义】非阻塞、无操作、自动消失；错过的信息不应造成损失。
【Web】一个 fixed 定位的浮层，进入 260ms 弹簧曲线、停留 2.2s、退出 200ms；role="status" + aria-live="polite"；多条时竖直堆叠最多 3 条，超出挤掉最老的。
【Android/Compose】做法一：Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()（注意 Android 12+ 后台不显示、样式不可定制）。做法二：自绘——用状态 var msg: String? 驱动，LaunchedEffect(msg) { delay(2200); msg = null }，浮层用 Surface + 圆角 + 半透明底，放在 Box 的 BottomCenter/TopCenter。
【禁用场景】错误与失败（改用可重试的 Snackbar 或内联错误）、需要确认的操作（改用 Modal/Popconfirm）。
【自检】连续触发 5 次不叠加成一堆；自动消失后不残留状态；轻提示出现期间页面仍可正常滚动与点击。""",
    ),
    OverlayTerm(
        id = "P03", en = "Modal", name = "模态框", category = "阻塞类", difficulty = "★★☆",
        summary = "带遮罩的对话框：打断主流程，让用户在里面完成一件事（确认、输入、选择）再继续。",
        definition = "Modal 是「临时的小房间」：背景被遮罩挡住、焦点被关在里面，用户必须在里面完成或明确放弃，才能回到主流程。它可以有标题、正文、表单、两个以上动作。",
        whenToUse = "重命名/新建、删除确认（重要且不可逆）、填写少量必填信息、选择一项后立刻生效。判据：这件事必须在当前上下文里立刻做完，且做完要回到原页面原位置。",
        vsNeighbor = "与 Alert 的区别：Alert 只告知、单按钮；Modal 要你做事、可含输入与多按钮。与 Drawer/BottomSheet 的区别：那两者从边缘滑出、通常承载更多内容与导航，Modal 居中、内容更聚焦。与 Popconfirm 的区别：Popconfirm 是锚在触发元素旁的小气泡，不改整页焦点。",
        androidImpl = "androidx.compose.ui.window.Dialog（自定义内容）或 Material3 AlertDialog（标题+正文+双按钮）。涉及数据丢失时把 dismissOnClickOutside 设为 false 并保留「取消」按钮；含输入框时记得 IME 遮挡处理（imePadding）。",
        badCase = "用 Modal 装长列表或设置页（该用 Drawer/BottomSheet/独立页）；Modal 里再弹 Modal；只有「确定」没有「取消」却要求用户输入内容。",
        params = "遮罩 40–60% 黑 · 宽度 ≤ 页面 90% · 动作按钮 ≤3 个 · 主按钮在右/在上 · 焦点陷入 + 关闭后归还",
        prompt = """实现一个 Modal（模态框），含输入内容与双动作。
【语义】打断主流程，用户在框内完成一件事；背景不可交互；必须提供明确的放弃路径（取消/关闭）。
【Web】role="dialog" + aria-modal="true" + aria-labelledby 指向标题；打开时焦点移入第一个可聚焦元素并 trap，Esc 关闭，关闭后焦点回到触发元素；遮罩点击是否关闭取决于「会不会丢数据」。
【Android/Compose】用 Dialog(onDismissRequest, properties = DialogProperties(dismissOnBackPress, dismissOnClickOutside)) 包 Surface + Column；或用 AlertDialog(title/text/confirmButton/dismissButton)。含 TextField 时外层加 Modifier.imePadding()，确定按钮在内容为空时禁用。
【内容】标题一句话说清要做什么；正文说明后果；按钮文案写动作本身（「删除」「保存」）而不是「确定」。
【自检】背景滚动被锁；键盘弹出不遮按钮；空输入时主按钮禁用；关闭后回到原页面且滚动位置不变；不允许 Modal 内再开 Modal。""",
    ),
    OverlayTerm(
        id = "P04", en = "Drawer", name = "抽屉", category = "面板类", difficulty = "★★☆",
        summary = "从屏幕边缘滑出的面板，承载导航或一批内容；可覆盖（带遮罩）也可推挤主内容。",
        definition = "Drawer 是「从边上拉出来的一块面板」，尺寸接近半屏到全屏，通常放导航菜单、筛选器、详情。它比 Modal 承载更多内容，比整页跳转更轻，关掉就回到原处。",
        whenToUse = "侧边导航（大屏/后台）、筛选条件面板、查看某条记录的详情但不想离开列表、多步表单的分步承载。",
        vsNeighbor = "与 Modal 的区别：Modal 居中、内容聚焦、通常一件事；Drawer 从边缘来、内容更多、可以边看主页面边操作。与 BottomSheet 的区别：只是滑出方向不同（侧 vs 底），移动端更常用 BottomSheet，桌面/后台更常用 Drawer。",
        androidImpl = "Material3 ModalNavigationDrawer（覆盖式，带遮罩与手势滑动）或 DismissibleNavigationDrawer（推挤式）；也可用 Box + animateFloatAsState 自绘（scrim 透明度与面板位移共用同一进度值，保证视觉一致）。返回键与点遮罩都要能关。",
        badCase = "在手机竖屏上用左侧 Drawer 装长表单（该用 BottomSheet 或独立页）；Drawer 打开后遮罩点了不关；面板宽度写死像素导致大屏/小屏都别扭。",
        params = "宽度 240–320dp（或页面 80%）· 滑入 240–280ms · 遮罩 0→40% 同步 · 支持手势滑开/滑关 · 返回键可关",
        prompt = """实现一个 Drawer（抽屉面板），覆盖式，带遮罩与动画。
【语义】从屏幕边缘滑出的面板，承载导航或一批内容；关闭后回到原上下文。
【Web】固定定位面板 + transform: translateX(-100%)→0（240ms 缓动），遮罩 opacity 0→0.4 与面板共用同一进度；打开时 body 滚动锁定，Esc 与点遮罩关闭；role="dialog" 或 role="navigation"（导航用途时）。
【Android/Compose】做法一：Material3 ModalNavigationDrawer(drawerContent = {...}) { 主内容 }。做法二：自绘——Box 内先放主内容，再放 scrim（background(Color.Black.copy(alpha = 0.4f * progress)) + clickable 关闭）与面板（Modifier.offset { IntOffset((progress - 1) * 面板宽度px, 0) }），progress 由 animateFloatAsState 驱动；BackHandler(enabled = open) 关闭。
【内容】面板内是导航项或筛选器；宽度用 dp（240–320）不用百分比像素写死。
【自检】动画中连点不出现两个面板；点遮罩/返回键都能关；关闭后主页面滚动位置不变；面板宽度在大小屏都合理。""",
    ),
    OverlayTerm(
        id = "P05", en = "Popconfirm", name = "气泡确认框", category = "锚定类", difficulty = "★★☆",
        summary = "锚在触发元素旁边的小确认气泡：就地二次确认轻量操作，不打断整页。",
        definition = "Popconfirm 是「贴着按钮冒出来的小气泡」，里面就一句问话 + 取消/确认两个小按钮。它不像 Modal 那样把整页遮住，用户视线不用离开原来的位置。",
        whenToUse = "删除一条列表项、取消关注、移除一个标签、清空筛选——这类「可逆或影响范围小、但需要防误触」的操作。",
        vsNeighbor = "与 Modal 的区别：Modal 遮全屏、抢焦点，Popconfirm 只锚在触发元素旁，上下文仍在视野里。与 Tooltip 的区别：Tooltip 是纯文字说明、无按钮、不可交互；Popconfirm 必须能点。与 Snackbar 撤销的区别：Popconfirm 是「事前拦一下」，Snackbar 撤销是「事后给一次反悔」。",
        androidImpl = "Material3 DropdownMenu（自动锚定在触发元素下方、空间不足时翻到上方，点外部自动关）承载「问话 + 取消/删除」；需要自定义位置/箭头时用底层 Popup(alignment, offset) + PopupProperties(focusable = true)。确认按钮用 destructive 色。",
        badCase = "用 Popconfirm 确认「清空整个数据库」这类重操作（该用 Modal 打明确后果）；一个页面里每个按钮都挂 Popconfirm（防误触变成三次点击）；气泡超出屏幕边缘被裁掉。",
        params = "气泡宽 200–260dp · 一句问话 ≤20 字 · 取消在左确认在右 · 确认色用 error · 点外部/返回键关闭",
        prompt = """实现 Popconfirm（气泡确认框），锚定在触发按钮旁。
【语义】就地二次确认轻量、影响范围小的操作；不遮整页、用户视线不离开触发位置。
【Web】触发元素外包一层 relative 容器，气泡 absolute 定位并带小箭头；出现 120–160ms 缩放 0.96→1 + 淡入；点外部/Esc 关闭；role="dialog" 且焦点移入气泡，关闭后回到触发按钮；靠近视口边缘时自动翻转方向不被裁切。
【Android/Compose】做法一（推荐）：Box { 触发按钮; DropdownMenu(expanded = pending == item, onDismissRequest = { pending = null }) { Column { Text("确定删除「$item」？"); Row { TextButton(取消); TextButton(删除, color = error) } } } }。做法二：Popup(alignment, offset, PopupProperties(focusable = true)) 自定义位置与箭头。
【内容】问话一句话点名对象（「删除『报表A』？」），确认按钮写动作（「删除」）不写「确定」。
【自检】列表滚动时气泡跟随或自动关闭（不要留在原地错位）；边缘项不被裁切；连点不叠加多个气泡；确认后该项立即从列表消失并给 Snackbar 撤销。""",
    ),
    OverlayTerm(
        id = "P06", en = "Tooltip", name = "文字提示", category = "锚定类", difficulty = "★☆☆",
        summary = "悬停/长按出现的纯文字说明，解释一个图标或术语；不可交互、不承载操作。",
        definition = "Tooltip 是「贴在元素旁的一句话注解」，只在用户表示疑问时出现（桌面端 hover、移动端长按），移开就消失。里面只有文字，没有按钮，也不该放关键信息。",
        whenToUse = "图标按钮没有文字标签、缩写与专业术语（如「ARPU」）、被禁用按钮的原因说明、数值单位的补充解释。",
        vsNeighbor = "与 Popover 的区别：Popover 是富内容气泡（可含列表、按钮、图片），Tooltip 只有纯文字。与 Popconfirm 的区别：Popconfirm 要用户做决定，Tooltip 只是解释。与 Alert/Toast 的区别：那两者是系统主动说话，Tooltip 是用户主动问。",
        androidImpl = "移动端没有 hover，等价触发是长按：用 Modifier.combinedClickable(onLongClick = ...) 或 pointerInput + detectTapGestures(onLongPress) 触发，气泡用 DropdownMenu/Popup 锚定；Material3 也有 TooltipBox（实验 API）。禁用按钮的原因不要只放在 Tooltip 里，要能被读屏拿到。",
        badCase = "把必要说明只放在 Tooltip 里（触屏用户可能永远发现不了）；Tooltip 里塞按钮和链接；多个 Tooltip 同时出现；出现后不自动消失。",
        params = "触发 长按 400ms / 桌面 hover 300ms 延迟 · 停留 ≤2s 或松手即消失 · 文字 ≤2 行 · 无按钮",
        prompt = """实现 Tooltip（文字提示），移动端长按触发、桌面端 hover 触发。
【语义】纯文字注解，解释图标或术语；不可交互、不承载关键信息、会自动消失。
【Web】触发元素加 aria-describedby 指向 tooltip 节点；hover 300ms 延迟出现、离开 100ms 后消失，focus 也触发；出现动画 120ms 淡入 + 轻微位移；靠近边缘自动翻转；role="tooltip"，绝不能包含可聚焦元素。
【Android/Compose】用 Modifier.combinedClickable(onClick = {}, onLongClick = { show = true })（需 @OptIn(ExperimentalFoundationApi::class)）或 pointerInput { detectTapGestures(onLongPress = { show = true }) }；气泡用 DropdownMenu(expanded = show) 或 Popup 锚定；LaunchedEffect(show) { delay(1800); show = false } 自动消失；同时给触发元素设置 contentDescription 供读屏使用。
【内容】一句话解释，不写操作指引；禁用按钮的原因同时写进 contentDescription。
【自检】长按出现、松手或 1.8s 后消失；连续长按不同元素不出现两个气泡；气泡不遮挡触发元素本身；读屏能拿到同样信息。""",
    ),
    OverlayTerm(
        id = "P07", en = "Snackbar", name = "条幅提示", category = "轻提示类", difficulty = "★★☆",
        summary = "底部条幅反馈，可带一个操作（最常见是「撤销」）；比 Toast 强、比 Modal 弱。",
        definition = "Snackbar 是应用内的底部横条：说明刚发生了什么，并且可以给一个补救动作。它不遮内容、几秒后自动消失，但因为能带按钮，所以适合「可逆操作」的善后。",
        whenToUse = "删除后给撤销、发送失败给重试、切换设置后给「打开设置」入口、批量操作后给结果与回退。判据：操作已发生，但用户可能想反悔。",
        vsNeighbor = "与 Toast 的区别：Snackbar 在应用内、可带一个操作、可被程序控制时长；Toast 是系统级、无操作。与 Modal/Alert 的区别：Snackbar 不阻塞，用户不理它也能继续干活。",
        androidImpl = "Material3 SnackbarHost + SnackbarHostState：scope.launch { val r = hostState.showSnackbar(msg, actionLabel = \"撤销\", duration = SnackbarDuration.Short); if (r == SnackbarResult.ActionPerformed) 恢复数据 }。宿主放在 Scaffold 的 snackbarHost 或 Box 底部对齐；同时只显示一条，新的顶掉旧的。",
        badCase = "Snackbar 里放两个以上操作（变成工具栏）；用它报致命错误（用户没时间读）；撤销窗口太短（<3s）或撤销后数据回不到原位置。",
        params = "停留 4s（Short）/ 10s（Long）· 单行文案 + 1 个操作 · 同时只 1 条 · 底部避让导航栏 ≥16dp",
        prompt = """实现 Snackbar（条幅提示），带一个「撤销」操作并真的能撤销。
【语义】非阻塞的操作性反馈：告知结果 + 给一次补救机会。
【Web】底部 fixed 条幅，进入 240ms 上滑 + 淡入，停留 4–10s，退出 200ms；role="status" + aria-live="polite"；动作按钮是唯一可聚焦元素；同时只显示一条，新的替换旧的。
【Android/Compose】SnackbarHostState + SnackbarHost(hostState, Modifier.align(Alignment.BottomCenter).navigationBarsPadding())；调用方 scope.launch { val r = hostState.showSnackbar("已删除「X」", actionLabel = "撤销"); if (r == SnackbarResult.ActionPerformed) { 把 X 插回原下标 } }；注意 showSnackbar 是挂起函数，撤销逻辑要在它返回后判断结果。
【数据】删除前先记住对象与原下标，撤销要恢复到位（不是追加到末尾）。
【自检】点撤销后条目回到原位置；不点则 4s 后自动消失且数据保持删除；连续删除两条时 Snackbar 不叠加；底部不被导航栏遮挡。""",
    ),
    OverlayTerm(
        id = "P08", en = "Notification", name = "系统通知", category = "轻提示类", difficulty = "★★★",
        summary = "应用之外的系统级提示：能在锁屏和通知栏出现，可持久、可带操作、可分组。",
        definition = "Notification 不属于你的界面，它属于系统。应用没在前台也能送达，用户可以从锁屏直接处理。因为打扰面最大，所以门槛也最高：要有渠道、要能被用户单独关掉、Android 13+ 要运行时授权。",
        whenToUse = "消息与评论到达、下载/上传进度、日程与提醒、长时间任务的进行中状态、需要用户回到应用处理的异常。判据：用户此刻不在你的应用里，也值得知道这件事。",
        vsNeighbor = "与 Toast/Snackbar 的区别：那两者只在应用内、应用退到后台就没了；通知在系统层，应用被杀也能送达。与 Alert 的区别：Alert 只在应用内阻塞当前页面，通知不阻塞任何页面。",
        androidImpl = "先建 NotificationChannel（API 26+，重要性决定是否有声音/横幅），再用 Notification.Builder(context, channelId) 构造，NotificationManager.notify(id, n) 发出；Android 13+ 必须先取得 POST_NOTIFICATIONS 运行时授权（未授权时 notify 静默失败）。进度类用 setProgress + setOngoing，操作按钮用 addAction，同组用 setGroup。",
        badCase = "把应用内就能说完的事推成通知（打扰换不来留存）；不建渠道直接发（API 26+ 会抛异常或静默失败）；用同一个 id 覆盖导致列表类通知只剩一条；未申请 POST_NOTIFICATIONS 就以为发出去了。",
        params = "渠道必建（API 26+）· Android 13+ 需 POST_NOTIFICATIONS · 小图标必须纯色透明底 · 标题 ≤30 字 · 进度类 setOngoing",
        prompt = """实现系统通知（Notification），含渠道创建、权限门控与真实发送。
【语义】应用之外的系统级提示，可在锁屏/通知栏处理；打扰面最大，因此必须可被用户单独关闭。
【Android/Compose】① 建渠道：if (SDK_INT >= 26) manager.createNotificationChannel(NotificationChannel(id, 名称, IMPORTANCE_DEFAULT).apply { description = ... })；② 权限：Android 13+ 用 rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) 申请 POST_NOTIFICATIONS，未授权时不要静默失败，要在界面上说明并给「去开启」入口；③ 发送：Notification.Builder(context, channelId).setContentTitle(..).setContentText(..).setSmallIcon(纯色图标).setAutoCancel(true)，进度类加 setProgress(max, cur, false) + setOngoing(true)；④ id 语义化：同类可覆盖用固定 id，列表类用条目 id。
【Web 对照】Notification API + Push：先 Notification.requestPermission()，granted 后 new Notification(title, { body, icon, tag })；tag 相当于 Android 的 id。
【自检】拒绝权限后界面有明确解释而不是无声失败；渠道在系统设置里可见可关；进度通知结束后 setOngoing(false) 并能被划掉；小图标在深色状态栏不糊成白块。""",
    ),
    OverlayTerm(
        id = "P09", en = "Popover", name = "气泡卡片", category = "锚定类", difficulty = "★★☆",
        summary = "锚定弹出的富内容气泡：可放列表、按钮、小表单；Tooltip 的「能干活」版本。",
        definition = "Popover 是贴着触发元素弹出的一张小卡片，里面可以有菜单项、开关、输入框、图片。它比 Tooltip 重（可交互）、比 Modal 轻（不遮整页），点外部就关。",
        whenToUse = "更多操作菜单、筛选器快捷面板、账号切换、颜色/字号选择、带说明的图标详情。",
        vsNeighbor = "与 Tooltip 的区别：Tooltip 只有文字且不可交互，Popover 有内容且可点。与 Popconfirm 的区别：Popconfirm 只问一个是/否，Popover 承载一组操作或控件。与 Dropdown（下拉菜单）的区别：下拉菜单是选中后收起的选择器，Popover 更像临时小面板，可以含非选项内容。",
        androidImpl = "Material3 DropdownMenu（自动锚定 + 边缘翻转 + 点外关闭，内容可自定义为 Column 而非 MenuItem）；需要富布局/箭头/自定义动画时用 Popup + PopupProperties(focusable = true)； Material3 的 ExposedDropdownMenuBox 用于「选择器」语义，别拿来做 Popover。",
        badCase = "Popover 里再弹 Popover；内容超过半屏还硬用气泡（该上 BottomSheet/独立页）；点击外部不关闭；位置不跟随触发元素导致滚动后错位。",
        params = "卡片宽 200–300dp · 圆角 12–16dp · 阴影 elevation 4–8 · 项高 ≥44dp · 点外部/返回键关闭",
        prompt = """实现 Popover（气泡卡片），锚定触发元素、可交互、点外部关闭。
【语义】临时的小面板：承载一组操作或控件，不遮整页，用完即走。
【Web】触发元素 relative，卡片 absolute + 小箭头；出现 140ms 缩放 0.97→1 + 淡入；点击外部、Esc、滚动容器时关闭；卡片内可聚焦元素参与 Tab 循环；role="dialog"（含操作时）或 role="menu"（纯菜单时）；靠边自动翻转。
【Android/Compose】Box { 触发按钮; DropdownMenu(expanded = open, onDismissRequest = { open = false }) { Column { 若干行（图标 + 标题 + 副标题）+ HorizontalDivider + 开关行 } } }；DropdownMenu 自带锚定与边缘翻转；需要自定义位置/箭头时改用 Popup(alignment, offset, PopupProperties(focusable = true))。
【内容】每行热区 ≥44dp；不要嵌套第二层气泡；超过 6 项或需要滚动时改用 BottomSheet。
【自检】点外部与返回键都能关；列表滚动后不错位（跟随或自动关闭）；深浅色下阴影与描边都看得清；行点击后立即关闭并执行动作。""",
    ),
    OverlayTerm(
        id = "P10", en = "BottomSheet / ActionSheet", name = "底部面板 / 动作面板", category = "面板类", difficulty = "★★☆",
        summary = "从底部升起的面板：ActionSheet 是一组互斥动作，BottomSheet 可承载内容与半展开/全展开两档。",
        definition = "两者都是从屏幕底部滑出的面板。ActionSheet 特指「列出一组动作让你挑一个」（含取消），BottomSheet 是更通用的容器，可以半展开露出一部分、上拉全展开，里面能放列表、表单、详情。",
        whenToUse = "分享/更多操作菜单（ActionSheet）；移动端筛选器、评论列表、图片预览、长表单分步、地图上的详情卡（BottomSheet）。移动端优先于 Drawer，因为拇指够得到底部。",
        vsNeighbor = "与 Drawer 的区别：方向不同（底 vs 侧），移动端底部更顺手、桌面/后台侧边更合适。与 Modal 的区别：Modal 居中且内容聚焦，BottomSheet 可以承载长内容并与主页面共存（半展开时还看得见背景）。与 Popover 的区别：Popover 锚在触发元素旁，BottomSheet 贴屏幕底边、宽度占满。",
        androidImpl = "Material3 ModalBottomSheet（模态、带遮罩、可拖拽关闭，需 @OptIn(ExperimentalMaterial3Api::class)）+ rememberModalBottomSheetState(skipPartiallyExpanded = false) 控制两档；非模态常驻用 BottomSheetScaffold。ActionSheet 用 ModalBottomSheet 内放动作列表 + 「取消」项。",
        badCase = "把整页内容塞进 BottomSheet 造成滚动嵌套打架；ActionSheet 不给「取消」；面板高度写死导致键盘弹出遮挡输入；拖拽关闭与内部列表滚动冲突未处理。",
        params = "半展开 ≈ 页面 50% · 全展开留顶部 ≥56dp · 顶部抓手 32×4dp · 动作项高 ≥52dp · 取消项在最下并分隔",
        prompt = """实现 BottomSheet / ActionSheet（底部面板），支持半展开与全展开两档。
【语义】移动端优先的底部容器：ActionSheet 是一组互斥动作 + 取消；BottomSheet 是可承载长内容的两档面板。
【Web】fixed 底部容器 + transform: translateY(100%)→0（280ms 缓动），遮罩 0→0.4；支持两档高度（50% / calc(100% - 56px)），拖拽把手可切换；打开时 body 滚动锁定，内部滚动到顶再下拉才关闭面板（避免滚动打架）；Esc 与点遮罩关闭。
【Android/Compose】@OptIn(ExperimentalMaterial3Api::class) + val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)；ModalBottomSheet(onDismissRequest = { open = false }, sheetState = sheetState) { 顶部抓手 + 动作列表（每项高 ≥52dp）+ HorizontalDivider + 「取消」 }；关闭用 scope.launch { sheetState.hide() } 再置 open = false 以获得下滑动画。
【内容】ActionSheet 每项写动作本身（「分享到微信」「删除」），破坏性动作用 error 色并放在取消之上。
【自检】半展开上拉能到全展开、下拉能关；内部列表滚动与面板拖拽不冲突；键盘弹出时输入框不被遮；关闭后主页面状态不变。""",
    ),
    OverlayTerm(
        id = "P11", en = "Loading / Progress", name = "加载与进度", category = "状态类", difficulty = "★★☆",
        summary = "等待的三种表达：非阻塞进度条、按钮内加载、整页阻塞遮罩；能不用遮罩就不用。",
        definition = "这一族不是「弹出来的东西」，而是「告诉用户系统在忙」。分三档强度：① 非阻塞（顶部细进度条/骨架屏，用户还能继续操作）② 局部阻塞（按钮变加载态，只锁这个动作）③ 全局阻塞（遮罩 + 转圈，什么都不让点）。强度越高，越要给进度与取消。",
        whenToUse = "① 后台刷新、分页加载、上传中的全局进度 → 非阻塞；② 提交表单、点赞、发送 → 按钮内加载（防重复提交）；③ 初始化必须完成才能继续、支付/写入等不可中断的关键操作 → 全局遮罩，且必须给取消或超时兜底。",
        vsNeighbor = "与 Toast/Snackbar 的区别：那两者是「结果反馈」，Loading 是「过程反馈」，完成后应接一个结果反馈。与 Alert 的区别：Alert 阻塞是因为要告知事实，Loading 阻塞是因为不能被打断，后者必须可取消或有超时。",
        androidImpl = "非阻塞：LinearProgressIndicator(progress = { p }) 放顶部，或骨架屏；按钮内：按钮禁用 + CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp) + 文案换成「提交中…」且宽度不变（防跳动）；全局：Box 遮罩 + CircularProgressIndicator + 「取消」按钮。不确定时长用 indeterminate，确定时长必须传真实进度。",
        badCase = "点一下按钮就整页转圈（该用按钮内加载）；加载态没有超时/取消，网络断了永远转；进度条从 0 直接跳 100；按钮加载时宽度变化导致布局跳动；加载完成后不给结果反馈。",
        params = "按钮内圆环 16dp/描边 2dp · 全局遮罩 40% 黑 + 必须可取消 · 顶部进度条高 2–3dp · >10s 必须有取消或错误兜底",
        prompt = """实现三档加载反馈（非阻塞进度 / 按钮内加载 / 全局遮罩），并可切换对比。
【语义】强度从低到高：非阻塞（用户仍可操作）→ 局部阻塞（只锁该动作）→ 全局阻塞（必须给取消或超时）。
【Web】非阻塞：顶部 2–3px 进度条 + 骨架屏；按钮内：按钮 disabled + 16px 旋转圆环 + 文案变「提交中…」但按钮宽度用 min-width 锁住不跳；全局：遮罩 rgba(0,0,0,.4) + 居中 spinner + 「取消」按钮，aria-busy="true"。
【Android/Compose】非阻塞：LinearProgressIndicator(progress = { p }, Modifier.fillMaxWidth())；按钮内：clickable(enabled = !loading) + Row { CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp, color = onPrimary); Text("提交中…") }；全局：Box(matchParentSize) 遮罩 + CircularProgressIndicator() + TextButton(取消)；进度用 LaunchedEffect(running) 推进，取消把 running 置 false。
【规则】确定时长必须传真实进度，不确定时长用 indeterminate；超过 10s 必须有取消或错误兜底；加载结束接一个结果反馈（Toast/Snackbar）。
【自检】按钮加载态宽度不跳；全局遮罩期间背景不可点；取消后状态复位不留半截进度；连点提交只发一次请求。""",
    ),
    OverlayTerm(
        id = "P12", en = "Badge / Dot", name = "角标与红点", category = "状态类", difficulty = "★☆☆",
        summary = "附着在图标上的计数或红点：不弹出、不打断，只表示「这里有新东西」。",
        definition = "Badge 是挂在图标右上角的小胶囊，写数字（未读数）或什么都不写（红点，只表示有更新）。它属于提示族里最弱的一档：不抢焦点、不需要用户处理，但会一直在那儿直到被消费。",
        whenToUse = "未读消息数、待处理任务数、有新版本、筛选项内有生效条件、购物车件数。红点用于「有更新但不值得计数」，数字用于「用户需要知道具体有多少」。",
        vsNeighbor = "与 Toast/Notification 的区别：那两个是「主动说一次就走」，Badge 是「持续状态」，直到用户看过才消失。与 Tooltip 的区别：Tooltip 解释含义，Badge 表达数量/状态。",
        androidImpl = "自绘最稳：Box 里放图标 + 右上角 Surface(RoundedCornerShape(percent = 50)) 写数字；Material3 也有 Badge/BadgeBox（BadgedBox）可直接用。规则：count > 99 显示「99+」，count == 0 时整个角标不渲染（不是显示 0），红点尺寸 8–10dp 且不含文字。",
        badCase = "数字为 0 还显示「0」角标；红点永远消不掉（没有消费逻辑）；角标遮挡图标主体超过 1/3；用红点承载必须处理的事项（该用 Notification 或页内提示）。",
        params = "数字胶囊高 18dp · 超 99 显示 99+ · 为 0 不渲染 · 红点 8–10dp · 2px 背景色描边把图标「挖」出来",
        prompt = """实现 Badge / Dot（角标与红点），含 99+ 与清零规则。
【语义】持续状态提示：表示「这里有新东西/有多少未处理」，不打断、不抢焦点，被消费后才消失。
【Web】图标外包 relative 容器，角标 absolute 右上角 + translate(30%, -30%)；min-width 18px + padding 0 4px，超过 99 显示「99+」，count === 0 时不渲染节点；红点是 8–10px 圆点无文字；用 2px 与背景同色的 border 把角标从图标里「挖」出来。
【Android/Compose】Box { Icon(...); if (count > 0) Surface(Modifier.align(Alignment.TopEnd).offset(x = 6.dp, y = (-6).dp), shape = RoundedCornerShape(percent = 50), color = error) { Text(if (count > 99) "99+" else "$count", fontSize = 10.sp, modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)) } }；红点则用 Box(Modifier.size(9.dp).background(error, CircleShape))；也可用 Material3 的 Badge/BadgeBox。
【数据】提供 +1 / −1 / 设为 100 / 清零 操作验证边界；清零后角标必须完全消失（含占位）。
【自检】0 不显示、99 显示 99、100 显示 99+；角标不遮挡图标关键部分；红点与数字不同时出现在同一图标；深色模式下描边仍能把角标分离出来。""",
    ),
)
