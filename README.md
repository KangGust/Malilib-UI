# Malilib UI

masa 系列模组（所有使用 **MaLiLib** 配置界面的模组：Litematica / MiniHUD / Tweakeroo / MaLiLib 本体…）的现代化配置界面。

半透明玻璃质感、左侧模组导航、跨模组搜索、行内收藏与置顶快捷条。**不 fork malilib**，全部通过 Mixin 在运行时接管原界面与控制件样式。

![版本](https://img.shields.io/badge/version-2.0.0%2B26.3-blue)
![MC](https://img.shields.io/badge/Minecraft-26.3-green)
![Loader](https://img.shields.io/badge/Fabric-0.20.1-orange)
![Java](https://img.shields.io/badge/Java-25-red)
![Build](https://github.com/KangGust/Malilib-UI/actions/workflows/build.yml/badge.svg)

![界面预览](docs/preview.jpg)

---

## 功能

### 整页重绘
- 右侧配置页整体重绘：自绘标签（中文名 + 英文原名双行）、行悬停高亮、**分组标题胶囊**、行分隔线、列表底色、滚动条、空结果提示、悬停提示卡片
- 控件**位置与交互沿用 malilib**，只替换绘制 → 不会出现错位/点不准
- 一个总开关「启用Malilib重绘」：开启=整页重绘，关闭=完全退回 malilib 原版；**切换即时生效，无需退出重进**
- 另有独立开关「接管其它界面控件」：可选择是否把样式套用到其它模组的界面上（只接管控件，永不接管整页）

### 导航
- 左侧模组列表：图标、名称、版本；展开查看各功能页
- **拖动手柄**（行尾 grip 图标）按住排序，其余行平滑让位动画
- 三种滚动方式：滚轮、拖滚动条、按住拖动（触屏友好）

### 搜索
- 顶部常驻搜索框，与右栏对齐，占位提示 `Search...`，支持退格/选区/清空
- 范围可选「当前模组 / 全部模组」；全范围时左栏列出命中功能（中英双行），点击直达
- 匹配中文显示名、英文原名、模组名；装了 Searchables 时自动使用其模糊匹配

### 收藏与置顶
- **每个功能行内自带收藏星标**（名字右侧），点击收藏/取消，收藏后变实心强调色
- 置顶显示方式三选一：**仅置顶 / 复制到上方 / 同时启用**
- 收藏项**最多 3 种快捷操作**：胶囊主体开/关或循环取值、放大镜定位（写入搜索框可清除）、**每颗星单独取消**
- 收藏条（置顶条）**自动换行、按名字宽度自适应、留最小宽度**，全部收藏都显示不溢出
- 收藏顺序 = 点击顺序，取消后回到原位置；全部即时生效

### 外观
- 半透明：**面板 / 控件 / 遮罩三层透明度独立调节**
- 颜色：强调色（10 套预设 + 颜色选择器）、面板底色、文字颜色；面板底色可跟随强调色自动推导
- 圆角 0~16、侧栏宽度 140~320、图标 / 模糊 / 紧凑开关
- 功能名与控件间距按行宽**比例动态计算**
- 图标系统基于 **Tabler Icons**（19 个），支持运行时按状态染色

### 其他
- 配置改动**即时保存**，记住上次打开的模组与功能页
- **Mod Menu 集成**：每个 masa 模组条目都有齿轮入口
- 中文 / English 双语；配置项已分类（接管范围 / 外观 / 显示 / 收藏与排序）

---

## 已知限制

1. 右侧列表滚动按整行推进（malilib 列表虚拟化所致），暂未实现像素级平滑滚动
2. 热键冲突暂无提示
3. 收藏条暂不支持拖动排序（顺序为收藏先后）
4. 滑杆/输入框等复杂控件的内部细节仍由 malilib 提供

---

## 构建要求

| 项目 | 要求 |
|---|---|
| JDK | **25** |
| Gradle | **9.8.2**（或使用仓库自带 wrapper） |
| Minecraft | **26.3** |
| Fabric Loader | **0.20.1** |
| Fabric Loom | **1.19-SNAPSHOT** |
| 平台 | Fabric（客户端 mod） |

### 依赖 jar（不随仓库提供）

放入项目根目录 `libs/`（CI 会自动从 Modrinth 拉取所需 jar）：

```
libs/
├── malilib-fabric-26.3-0.30.2.jar        # 必需
├── modmenu-21.0.0.jar                    # 可选（Mod Menu 集成）
└── Searchables-fabric-26.3-1.0.3.jar     # 可选（搜索增强）
```

### 命令

```bash
./gradlew build              # 在线构建
./gradlew --offline build    # 离线构建（依赖来自 libs/ 与本地缓存）
```

产物：`build/libs/malilib-ui-<version>.jar`

---

## 配置文件

```
.minecraft/config/malilib-ui/
├── settings.json   # 外观 / 显示 / 收藏与排序等配置
└── layout.json     # 自定义顺序 / 收藏列表 / 上次停留位置
```

两个文件即可完整备份或迁移界面布局。

---

## 致谢 / 许可

- 界面灵感与协议基础：[malilib](https://github.com/maruohon/malilib)（masa 系列）
- 图标：[Tabler Icons](https://github.com/tabler/tabler-icons)（MIT）
- 作者：**KangGust**
- 许可：**MIT**（见 [LICENSE](LICENSE)）
