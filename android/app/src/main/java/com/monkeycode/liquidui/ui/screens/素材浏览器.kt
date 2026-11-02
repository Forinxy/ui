package com.monkeycode.liquidui.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape






import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.monkeycode.liquidui.ui.components.GlassCard
import com.monkeycode.liquidui.ui.features.adminquery.AdminQueryNorms
import com.monkeycode.liquidui.ui.features.overlayterms.OverlayTerms
import com.monkeycode.liquidui.ui.features.ui2.Ui2ExtraFeatures
import com.monkeycode.liquidui.ui.features.ui2.Ui2Features
import com.monkeycode.liquidui.ui.components.InfoBanner
import com.monkeycode.liquidui.ui.components.SectionTitle
import com.monkeycode.liquidui.ui.components.Tag
import com.monkeycode.liquidui.ui.theme.AppIcons
import com.monkeycode.liquidui.ui.theme.AppShape

/**
 * 素材浏览器 — 功能点目录页
 *
 * 展示素材库中每个功能模块的元数据（名称、分类、难度、依赖、文件数），
 * 支持搜索和分类筛选，点击卡片可查看接入说明。
 */
@Composable
fun 素材浏览器(onOpenFeature: (FeatureTarget) -> Unit) {
    // 状态一律提到详情分支之前、并可跨组合保存：点卡片查看（UI2 详情或跳转功能页）后
    // 返回时，列表滚动位置（条目+像素偏移）、搜索词、分类筛选都停在原处，不回到顶部。
    var section by rememberSaveable { mutableStateOf(0) } // 0=模板素材 1=UI2体验馆 2=数据查询规范 3=弹窗提示术语
    // 详情选中态存 id 而非对象：可跨组合保存，返回列表后仍停在原处（见上方注释）
    var ui2SelectedId by rememberSaveable { mutableStateOf<String?>(null) }
    var adminSelectedId by rememberSaveable { mutableStateOf<String?>(null) }
    var overlaySelectedId by rememberSaveable { mutableStateOf<String?>(null) }
    var query by rememberSaveable { mutableStateOf("") }
    var selectedCategoryName by rememberSaveable { mutableStateOf<String?>(null) }
    val selectedCategory = FeatureCategory.entries.firstOrNull { it.name == selectedCategoryName }
    val listState = rememberSaveable(saver = LazyListState.Saver) { LazyListState() }
    val ui2Selected = ui2SelectedId?.let { id -> (Ui2Features + Ui2ExtraFeatures).firstOrNull { it.id == id } }
    if (ui2Selected != null) {
        Ui2DetailScreen(feature = ui2Selected, onBack = { ui2SelectedId = null })
        return
    }
    val adminSelected = adminSelectedId?.let { id -> AdminQueryNorms.firstOrNull { it.id == id } }
    if (adminSelected != null) {
        AdminQueryDetailScreen(norm = adminSelected, onBack = { adminSelectedId = null })
        return
    }
    val overlaySelected = overlaySelectedId?.let { id -> OverlayTerms.firstOrNull { it.id == id } }
    if (overlaySelected != null) {
        OverlayTermsDetailScreen(term = overlaySelected, onBack = { overlaySelectedId = null })
        return
    }

    val filtered = remember(query, selectedCategory) {
        全部素材().asSequence()
            .filter { feature ->
                selectedCategory == null || feature.category == selectedCategory
            }
            .filter { feature ->
                query.isBlank() ||
                    feature.name.contains(query, ignoreCase = true) ||
                    feature.description.contains(query, ignoreCase = true)
            }
            .toList()
    }

    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
        contentPadding = PaddingValues(top = 20.dp, bottom = 108.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Column(
                modifier = Modifier.padding(horizontal = 22.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "素材库",
                    style = MaterialTheme.typography.displayMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "点击功能模块，直接进入真实可操作的功能；不是介绍弹窗",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 22.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                分类芯片(label = "模板素材", selected = section == 0, onClick = { section = 0 })
                分类芯片(label = "UI2 体验馆 · 77", selected = section == 1, onClick = { section = 1 })
                分类芯片(label = "数据查询规范 · 5", selected = section == 2, onClick = { section = 2 })
                分类芯片(label = "弹窗提示术语 · 12", selected = section == 3, onClick = { section = 3 })
            }
        }

        if (section == 1) {
            item { Ui2CatalogSection(onOpen = { ui2SelectedId = it.id }) }
            item { Spacer(Modifier.height(24.dp)) }
        }

        if (section == 2) {
            item { AdminQueryCatalogSection(onOpen = { adminSelectedId = it.id }) }
            item { Spacer(Modifier.height(24.dp)) }
        }

        if (section == 3) {
            item { OverlayTermsCatalogSection(onOpen = { overlaySelectedId = it.id }) }
            item { Spacer(Modifier.height(24.dp)) }
        }

        if (section == 0) {
        item {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 22.dp),
                placeholder = { Text("搜索功能模块…") },
                leadingIcon = {
                    Icon(AppIcons.BlurOn, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                },
                shape = AppShape.control,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.55f),
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.40f),
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                    cursorColor = MaterialTheme.colorScheme.primary,
                ),
                singleLine = true
            )
        }

        item {
            Spacer(modifier = Modifier.height(4.dp))
        }

        item {
            SectionTitle(text = "全部分类")
        }
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 22.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                分类芯片(
                    label = "全部",
                    selected = selectedCategory == null,
                    onClick = { selectedCategoryName = null }
                )
                FeatureCategory.entries.forEach { category ->
                    分类芯片(
                        label = category.label,
                        selected = selectedCategory == category,
                        onClick = { selectedCategoryName = if (selectedCategory == category) null else category.name }
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(4.dp))
        }

        items(filtered) { feature ->
            功能卡片(
                feature = feature,
                onClick = { onOpenFeature(feature.target) }
            )
        }
        }
    }


}

@Composable
private fun 分类芯片(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(percent = 50),
        color = if (selected) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        },
        contentColor = if (selected) {
            MaterialTheme.colorScheme.onPrimary
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        }
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
        )
    }
}

@Composable
private fun 功能卡片(
    feature: FeatureItem,
    onClick: () -> Unit
) {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(
                        MaterialTheme.colorScheme.primaryContainer,
                        RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = feature.icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = feature.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = feature.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(horizontalAlignment = Alignment.End) {
                Tag(
                    text = feature.category.label,
                    accent = when (feature.category) {
                        FeatureCategory.外观 -> Color(0xFF4CAF50)
                        FeatureCategory.动效 -> Color(0xFF2196F3)
                        FeatureCategory.导航 -> Color(0xFFFF9800)
                        FeatureCategory.设置 -> Color(0xFF9C27B0)
                        FeatureCategory.组件 -> Color(0xFF00BCD4)
                    }
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = feature.difficulty,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// ─── 数据模型 ───────────────────────────────────────────────────────────────

enum class FeatureTarget { Components, Motion, Settings, Permissions, About, Onboarding }

enum class FeatureCategory(val label: String) {
    外观("外观"),
    动效("动效"),
    导航("导航"),
    设置("设置"),
    组件("组件")
}

data class FeatureItem(
    val name: String,
    val description: String,
    val category: FeatureCategory,
    val difficulty: String,
    val instructions: String,
    val fileCount: Int,
    val codeDir: String,
    val icon: ImageVector,
    val target: FeatureTarget,
)

/** 扫描素材库，返回所有功能模块。 */
private fun 全部素材(): List<FeatureItem> = listOf(
    FeatureItem(
        name = "液态玻璃主题",
        description = "一套基于 Blur + backdrop 的玻璃拟态主题，支持多套配色和动态取色。",
        category = FeatureCategory.外观,
        difficulty = "入门",
        instructions = """
            1. 复制 液态玻璃主题/代码/ 下的全部 .kt 文件到项目的 ui/theme/ 包
            2. 复制 素材库/玻璃常用组件/代码/控件.kt 中的 liquidGlass modifier
            3. 在 Activity 的 setContent 外层包裹 LiquidUITheme()
            4. 可选：接入壁纸背景权限和 backdrop 依赖
        """.trimIndent(),
        fileCount = 5,
        codeDir = "素材库/液态玻璃主题/代码/",
        icon = AppIcons.Palette,
        target = FeatureTarget.Settings
    ),
    FeatureItem(
        name = "玻璃常用组件",
        description = "按钮、滑块、输入框、弹窗、反馈、行布局——全部自带玻璃风格。",
        category = FeatureCategory.组件,
        difficulty = "入门",
        instructions = """
            1. 复制 玻璃常用组件/代码/ 下的全部 .kt 文件到项目的 ui/components/ 包
            2. 确保依赖了 AppShape / liquidGlass / pressScale 等底层 modifier
            3. 直接在 @Composable 中调用：玻璃主按钮、玻璃滑块、玻璃文本输入框 等
        """.trimIndent(),
        fileCount = 7,
        codeDir = "素材库/玻璃常用组件/代码/",
        icon = AppIcons.Widgets,
        target = FeatureTarget.Components
    ),
    FeatureItem(
        name = "动效系统",
        description = "全局弹簧阻尼 token、按压回弹、常态呼吸、错峰入场——一处改全应用。",
        category = FeatureCategory.动效,
        difficulty = "进阶",
        instructions = """
            1. 复制 动效系统/代码/ 下的 .kt 文件到项目的 ui/motion/ 包
            2. 在 Motion.kt 中调整 Duration / Easing / Stagger 全局 token
            3. 用 运动.motionDamping 控制全局弹簧阻尼
            4. 组件中直接引用 运动.Duration.Fast、运动.snappy() 等
        """.trimIndent(),
        fileCount = 5,
        codeDir = "素材库/动效系统/代码/",
        icon = AppIcons.Animation,
        target = FeatureTarget.Motion
    ),
    FeatureItem(
        name = "底部导航栏",
        description = "可拖拽、带弹簧阻尼的液态玻璃底部导航栏；玻璃模式自动降级 Material3。",
        category = FeatureCategory.导航,
        difficulty = "进阶",
        instructions = """
            1. 复制 底部导航栏/代码/ 下的 .kt 文件到项目的 ui/components/ 包
            2. 在 AppRoot 中用 液态玻璃底部标签栏 包裹 AnimatedContent 页面切换
            3. 定义 Screen 枚举并提供 title / icon
            4. 确保 backdrop 层已正确录制
        """.trimIndent(),
        fileCount = 2,
        codeDir = "素材库/底部导航栏/代码/",
        icon = AppIcons.Widgets,
        target = FeatureTarget.Components
    ),
    FeatureItem(
        name = "主题色配置与切换",
        description = "8 套配色方案 + 自由取色盘，实时切换不重启。",
        category = FeatureCategory.设置,
        difficulty = "入门",
        instructions = """
            1. 复制 Theme.kt 中的 PaletteId / schemeFor / seedSchemeFor 到你的 theme 包
            2. 在 Settings 页引入 颜色选择器 composable
            3. 用 settings.updatePalette() 持久化用户选择
        """.trimIndent(),
        fileCount = 2,
        codeDir = "素材库/主题色配置与切换/代码/",
        icon = AppIcons.Palette,
        target = FeatureTarget.Settings
    ),
    FeatureItem(
        name = "应用图标切换",
        description = "在应用内切换桌面图标，选择后立即生效，无需重新安装。",
        category = FeatureCategory.设置,
        difficulty = "进阶",
        instructions = """
            1. 复制 应用图标切换/代码/ 下的 .kt 文件到项目的 ui/features/ 包
            2. 在 AndroidManifest 中为每个图标变体声明 <activity-alias>
            3. 调用 应用图标切换器.enableIcon(componentName) 切换
        """.trimIndent(),
        fileCount = 2,
        codeDir = "素材库/应用图标切换/代码/",
        icon = AppIcons.Settings,
        target = FeatureTarget.Settings
    ),
    FeatureItem(
        name = "桌面图标动态切换",
        description = "支持动态 Activity Alias 切换，含 ShortcutManager 快捷方式适配。",
        category = FeatureCategory.设置,
        difficulty = "进阶",
        instructions = """
            1. 复制 桌面图标动态切换/代码/ 下的 .kt 文件
            2. 在 AndroidManifest 中声明所有 alias activity
            3. 使用 桌面图标切换器 枚举切换
        """.trimIndent(),
        fileCount = 1,
        codeDir = "素材库/桌面图标动态切换/代码/",
        icon = AppIcons.Settings,
        target = FeatureTarget.Settings
    ),
    FeatureItem(
        name = "应用欢迎引导界面",
        description = "首次启动展示功能介绍、免费声明、开源仓库入口，点击开始使用后进入主页。",
        category = FeatureCategory.导航,
        difficulty = "入门",
        instructions = """
            1. 复制 应用欢迎引导界面/代码/ 下的 .kt 文件到项目的 ui/screens/ 包
            2. 在 AppSettingsState 中添加 onboardingCompleted 状态
            3. 在 MainActivity 的 setContent 中判断：首次启动显示引导页
            4. 引导页点击"开始使用"后调用 settings.completeOnboarding()
        """.trimIndent(),
        fileCount = 1,
        codeDir = "素材库/应用欢迎引导界面/代码/",
        icon = AppIcons.Speed,
        target = FeatureTarget.Onboarding
    ),
    FeatureItem(
        name = "状态反馈组件",
        description = "加载、骨架屏、空态、错误、成功与离线状态，可在组件页直接操作。",
        category = FeatureCategory.组件,
        difficulty = "入门",
        instructions = "复制状态反馈组件代码并按业务状态切换对应 composable。",
        fileCount = 1,
        codeDir = "素材库/状态反馈组件/代码/",
        icon = AppIcons.Notifications,
        target = FeatureTarget.Components
    ),
    FeatureItem(
        name = "权限请求组件",
        description = "真实请求运行时权限、显示授权状态，并支持拒绝后跳转系统设置。",
        category = FeatureCategory.设置,
        difficulty = "入门",
        instructions = "复制 PermissionToolkit，并按权限用途传入 PermissionSpec。",
        fileCount = 1,
        codeDir = "素材库/权限请求组件/代码/",
        icon = AppIcons.Shield,
        target = FeatureTarget.Permissions
    ),
    FeatureItem(
        name = "主题配置导入导出",
        description = "把当前主题完整导出为 JSON，并从剪贴板校验、导入和恢复。",
        category = FeatureCategory.设置,
        difficulty = "入门",
        instructions = "复制导入导出代码并连接统一的设置状态类。",
        fileCount = 1,
        codeDir = "素材库/主题配置导入导出/代码/",
        icon = AppIcons.RotateRight,
        target = FeatureTarget.Settings
    ),
    FeatureItem(
        name = "协议与开源许可页面",
        description = "完整页面展示隐私说明、全部参考项目、作者、链接和许可证。",
        category = FeatureCategory.设置,
        difficulty = "入门",
        instructions = "复制信息页面并在设置页提供明确入口。",
        fileCount = 1,
        codeDir = "素材库/协议与开源许可页面/代码/",
        icon = AppIcons.Shield,
        target = FeatureTarget.About
    )
)
