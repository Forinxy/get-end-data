# 过期管家 (ExpiryGuard) — 项目交接文档

> 最后更新：2026-07-29  
> 版本：1.0.0 (versionCode=1)  
> 源码位置：[待补充: Git 仓库地址]  
> 当前 commit：[待补充: 项目非 Git 仓库，无法获取 commit 哈希]

---

## 1. 项目概述

**过期管家** 是一款 Android 原生应用，帮助用户管理物品保质期，到期前自动提醒退货/处理，避免食品和日用品过期浪费。

| 维度 | 详情 |
|------|------|
| 应用名 | 过期管家 |
| 包名 | `com.expiryguard.app` |
| 技术栈 | Kotlin 2.0.21 + Jetpack Compose (Material3) |
| 架构模式 | MVVM + Hilt 依赖注入 + Room 本地数据库 |
| minSdk | 26 (Android 8.0) |
| targetSdk / compileSdk | 35 |
| UI 风格 | 毛玻璃拟态 (Glassmorphism)，支持浅色/深色主题 + Android 12+ Monet 动态取色 |
| 离线能力 | 完全离线，无需网络，无后端 |

---

## 2. 开发环境

### 2.1 必备工具

| 工具 | 版本要求 | 验证命令 |
|------|---------|---------|
| 操作系统 | macOS / Windows / Linux | — |
| JDK | **17** | `java -version` |
| Android SDK | API 35 (Android 15) | `sdkmanager --list` |
| Gradle | **8.9**（由 wrapper 自动下载） | `./gradlew --version` |
| Android Studio | Hedgehog (2023.1.1) 或更新 | 打开项目自动检测 |

### 2.2 环境变量

| 变量 | 说明 |
|------|------|
| `ANDROID_HOME` | Android SDK 路径，如 `~/Android/Sdk` |
| `JAVA_HOME` | JDK 17 安装路径（如 `JAVA_HOME` 未设，Gradle 自动检测） |

### 2.3 第三方账号/密钥

**无需任何第三方账号或 API 密钥。** 应用完全离线运行，所有数据存储在本地 Room 数据库中。

如未来需要接入：
- 云同步功能 → [待补充: 选择后端方案]
- 条形码扫描识别 → [待补充: 选择 OCR/扫码 API]

---

## 3. 构建运行

### 3.1 从零构建（精确步骤）

```bash
# 1. 克隆仓库（如为 Git 仓库）
git clone [待补充: 仓库地址]
cd ExpiryGuard

# 2. 确认 JDK 17
java -version
# 预期：openjdk version "17.0.x" ...

# 3. 设置 Android SDK 路径（如未自动检测）
export ANDROID_HOME=~/Android/Sdk
# 或在项目根目录创建 local.properties 文件：
# echo "sdk.dir=$ANDROID_HOME" > local.properties

# 4. 构建 Debug APK
./gradlew assembleDebug

# 5. 安装到设备
adb install app/build/outputs/apk/debug/app-debug.apk

# 6. 或在 Android Studio 中直接 Run
# 打开项目 → 选择 Run 'app' (Shift+F10)
```

### 3.2 已知构建注意事项

1. **Gradle 镜像源**：`settings.gradle.kts` 中配置了阿里云镜像，在国内网络下构建更快。海外环境可删除镜像配置，使用默认 Google/Maven Central。
2. **内存限制**：`gradle.properties` 中 `org.gradle.jvmargs=-Xmx1536m`。低内存机器（<4GB RAM）可能需降低此值。
3. **并行构建已关闭**：`org.gradle.parallel=false`，构建缓存也关闭，首次构建较慢（约 2-5 分钟）。
4. **KSP**：项目使用 KSP 而非 KAPT 进行 Room 和 Hilt 的注解处理，编译速度更快。
5. **Gradle Wrapper 使用腾讯云镜像**：`gradle-wrapper.properties` 中 `distributionUrl` 指向腾讯云。如不可用，改回官方 `https://services.gradle.org/distributions/gradle-8.9-bin.zip`。

### 3.3 构建 Release APK

```bash
./gradlew assembleRelease
# 注意：当前未配置签名，需在 app/build.gradle.kts 中添加 signingConfigs
```

---

## 4. 开发进度

### 4.1 已完成

| 功能 | 状态 | 说明 |
|------|:---:|------|
| 清单 CRUD | ✅ | 添加/编辑/删除/查看详情，支持拍照、生产日期、保质期预设+自定义天数 |
| 到期状态引擎 | ✅ | 5 种状态：安全/即将到期/可退货/紧急/过期，支持自定义退货阈值 |
| 保质期分组 | ✅ | 5 个默认分组（当天/3月以下/3~6月/6月~1年/1年以上），可自定义管理 |
| 首页今日待办 | ✅ | 今日到期+可退货+已过期+预警（1天），支持左右滑动标记完成，下拉刷新 |
| 清单列表 | ✅ | 列表/网格双视图，筛选（全部/安全/即将到期/可退货/过期），搜索，多选批量删除/备注 |
| 统计页 | ✅ | 状态分布、到期趋势、分类统计图表（Vico 图表库） |
| 分类管理 | ✅ | 自定义分类，支持颜色 |
| 回收站 | ✅ | 软删除→恢复/永久删除 |
| 设置页 | ✅ | 深色模式/Monet 取色/通知开关/提醒天数/自动清理 |
| 数据备份恢复 | ✅ | 导出 CSV/JSON，备份到本地文件，从文件恢复 |
| 通知提醒 | ✅ | 启动时检查待处理清单发送通知，支持测试通知 |
| 毛玻璃 UI | ✅ | 浅色/深色主题，自适应，毛玻璃拟态风格 |
| 状态标记视觉 | ✅ | 左侧彩色竖条 + StatusBadge 标签，已处理用绿色压过原始状态 |

### 4.2 进行中

| 功能 | 说明 |
|------|------|
| 状态标记调优 | 根据用户反馈持续调整过期/安全/可退货等标记的视觉表现 |

### 4.3 已搁置 / 未开始

| 功能 | 说明 |
|------|------|
| 条形码扫描 | 已声明 CAMERA 权限和 CameraX 依赖，但扫描功能未实现 |
| 定时后台通知 | 依赖 WorkManager 已引入，但未实现定时到期提醒 |
| 自动清理 | 设置中已有 UI 配置（autoCleanupDays），但未实现自动清理逻辑 |
| 云同步 | 未规划 |
| 小组件 (Widget) | 未规划 |
| 多语言 | 当前仅中文 |

---

## 5. 待开发内容 (Backlog)

| 优先级 | 功能 | 估算工作量 | 依赖 |
|:---:|------|:---:|------|
| P0 | 定时后台通知（WorkManager 定期检查到期清单并推送） | 3-5 天 | 无 |
| P1 | 条形码扫描识别（CameraX + ML Kit 或 ZXing） | 5-7 天 | 需选择扫码方案 |
| P1 | 自动清理过期清单 | 2-3 天 | 无 |
| P2 | 产品名称自动填充（取代默认"清单"名称） | 1-2 天 | 无 |
| P2 | 批量导入（从 CSV/JSON 恢复时支持合并而非覆盖） | 2-3 天 | 无 |
| P2 | 多语言支持（至少英文） | 3-5 天 | 需提取 strings.xml |
| P3 | 桌面小组件 (App Widget) | 5-7 天 | 无 |
| P3 | 分类统计图表交互优化 | 2-3 天 | 无 |
| P3 | 产品照片编辑/裁剪 | 2-3 天 | 无 |

---

## 6. 架构与关键模块

### 6.1 项目结构

```
app/src/main/java/com/expiryguard/app/
├── ExpiryGuardApp.kt          # Application 入口，初始化通知渠道
├── MainActivity.kt            # 唯一 Activity，Hilt 入口，加载主题和导航
├── data/
│   ├── db/
│   │   ├── entity/             # Room 实体：ProductEntity, CategoryEntity, ShelfLifeGroupEntity
│   │   ├── dao/                # DAO 接口：ProductDao, CategoryDao, ShelfLifeGroupDao
│   │   └── AppDatabase.kt     # Room 数据库（v3），单例模式
│   └── repository/
│       └── ProductRepository.kt # 数据仓库，封装所有 DAO 操作，返回 Result<T>
├── di/
│   ├── AppModule.kt           # Hilt 模块：提供 DataStore<Preferences>
│   └── DatabaseModule.kt      # Hilt 模块：提供 Room DB、DAO、Repository
├── domain/
│   ├── engine/
│   │   └── ExpiryRuleEngine.kt # 到期规则引擎（单例 object）
│   └── model/
│       └── ProductStatus.kt   # 密封类：Safe/ExpiringSoon/Returnable/Urgent/Expired
├── navigation/
│   └── AppNavigation.kt       # 路由定义 + 底部导航栏 + 相机集成
├── notification/
│   └── NotificationHelper.kt  # 通知渠道创建 + 测试通知 + 待办提醒
├── ui/
│   ├── components/            # 可复用组件：ProductCard, StatusBadge, GlassCard, StatCard, DatePicker, EmptyState
│   ├── home/                  # 首页：HomeScreen + HomeViewModel
│   ├── product/               # 清单：Add/Edit/Detail/List + 对应 ViewModel
│   ├── settings/              # 设置：Settings, BackupRestore, CategoryManagement, ShelfLifeGroupManagement, NotificationSettings, About
│   ├── stats/                 # 统计：StatsScreen + StatsViewModel
│   ├── theme/                 # 主题：Color, Theme, Type
│   └── trash/                 # 回收站：TrashScreen + TrashViewModel
└── util/
    ├── DateUtils.kt           # 日期工具（java.time API）
    └── ImageUtils.kt          # 图片保存工具
```

### 6.2 核心入口

| 入口 | 文件 | 职责 |
|------|------|------|
| Application | `ExpiryGuardApp.kt` | Hilt 入口，创建通知渠道 |
| Activity | `MainActivity.kt` | 加载 DataStore 偏好 → 初始化主题 → 渲染 `AppNavigation` |
| 导航 | `AppNavigation.kt` | 12 个路由，4 Tab 底部导航，相机拍照回调 |

### 6.3 数据流

```
UI (Compose Screen)
  ↑ collectAsState()
ViewModel (Hilt @HiltViewModel)
  ↑ Flow
ProductRepository (单例, @Inject)
  ↑ Flow / suspend fun → Result<T>
Room DAO (ProductDao, CategoryDao, ShelfLifeGroupDao)
  ↑
Room Database (AppDatabase, v3, 单例)
```

### 6.4 到期规则引擎

`ExpiryRuleEngine.calculateStatus(shelfLifeDays, expiryDate)` 判定逻辑：

1. `remainingDays <= 0` → `Expired(daysOverdue)`
2. `remainingDays <= 3` → `Urgent(remainingDays)`
3. `remainingDays <= threshold` → `Returnable(remainingDays, threshold)`
4. 否则 → `Safe(remainingDays)`

退货阈值 (`getReturnThreshold`)：
- 保质期 >= 365 天 → 45 天
- 保质期 > 183 天 → 20 天
- 保质期 >= 90 天 → 15 天
- 保质期 < 90 天 → 0（不可退货）

### 6.5 首页今日待办筛选逻辑

`HomeViewModel.processProducts()` 合并四个来源：

| 来源 | 条件 |
|------|------|
| 今日到期 | `isToday(expiryDate)` + 未完成 |
| 可退货 | `Returnable` 状态 + 未完成 |
| 已过期 | `days < 0` + 未完成 |
| 预警 | `days == 1` + 未完成 + 不在上述列表中 |

已处理区仅显示以上四类中已完成的项目。

### 6.6 已知技术债

1. **数据库实例重复创建风险**：`DatabaseModule.kt` 通过 `Room.databaseBuilder().build()` 创建数据库，而 `AppDatabase.kt` 内部有 `getInstance()` 双重检查锁单例。两者不一致，理论上可能创建两个实例。建议统一使用一种方式（推荐 Hilt 单例 + `@Provides @Singleton`）。

2. **无 .gitignore**：项目根目录缺少 `.gitignore` 文件，`build/`、`.gradle/`、`local.properties` 等可能被误提交。

3. **ProGuard 规则不完整**：仅保留了 Gson 注解，Room、Hilt、Coil 等库需要额外的 ProGuard 规则才能正确混淆。

4. **WorkManager 未使用**：依赖已引入但无任何 Worker 实现，定时通知功能未完成。

5. **产品名称固定为"清单"**：`AddProductViewModel.saveProduct()` 中 `name = "清单"`，用户无法自定义名称（除非通过编辑页修改）。

6. **MainActivity 通知逻辑未同步**：`MainActivity.onCreate()` 中的待办筛选逻辑与 `HomeViewModel.processProducts()` 不一致——前者未包含已过期项，后者已包含。

7. **`ExpiringSoon` 状态未使用**：`ProductStatus` 密封类定义了 `ExpiringSoon`，但 `ExpiryRuleEngine` 中从未返回此状态。

---

## 7. 账号与密钥

**无需任何账号或密钥。** 应用完全离线，无后端依赖。

如未来需要接入的功能：
- 条形码识别 API → [待补充: 选择 ML Kit 或第三方 API]
- 云同步 → [待补充: 选择 Firebase / 自建后端]
- 崩溃上报 → [待补充: 选择 Firebase Crashlytics 或 Sentry]

---

## 8. 验收标准

新人按以下步骤操作，全部通过即视为成功接管：

### 第一步：环境准备
- [ ] JDK 17 安装并配置 `JAVA_HOME`
- [ ] Android SDK 35 安装，`ANDROID_HOME` 配置正确
- [ ] Android Studio 打开项目，Gradle Sync 成功（无红色报错）

### 第二步：构建运行
- [ ] `./gradlew assembleDebug` 构建成功，生成 `app-debug.apk`
- [ ] APK 安装到设备/模拟器，应用正常启动，无崩溃

### 第三步：功能验证
- [ ] 首页显示空状态（"还没有清单"）
- [ ] 点击 FAB → 添加清单页 → 选择生产日期 + 保质期 → 保存成功
- [ ] 首页显示刚添加的清单，左侧色条和 StatusBadge 颜色正确
- [ ] 添加一个过期清单（生产日期设为过去），首页出现深红色过期标记
- [ ] 左滑/右滑标记完成 → 绿色"已处理"标记覆盖原状态
- [ ] 清单列表页 → 筛选/搜索/多选/视图切换 正常
- [ ] 统计页 → 图表正常渲染
- [ ] 设置页 → 深色模式切换正常
- [ ] 设置页 → 备份/恢复 正常

### 第四步：代码理解
- [ ] 能说出 `ExpiryRuleEngine.calculateStatus` 的判定优先级
- [ ] 能说出首页 `todayExpiry` 的四个合并来源
- [ ] 能指出 `DatabaseModule` 和 `AppDatabase.getInstance()` 的重复实例化问题
- [ ] 能运行 `./gradlew assembleDebug` 并定位构建错误