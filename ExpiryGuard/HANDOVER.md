# ExpiryGuard 项目交接文档

> 最后更新：2026-07-29

---

## 一、项目概述

| 项目 | 说明 |
|------|------|
| **项目名称** | ExpiryGuard（到期守护） |
| **定位** | Android 商品过期日期管理应用，支持拍照添加商品、自动计算到期日、保质期分组、到期提醒、统计仪表盘 |
| **技术栈** | Kotlin 2.0.21 + Jetpack Compose (Material3) + Hilt DI + Room + MVVM |
| **包名** | `com.expiryguard.app` |
| **minSdk** | 26 (Android 8.0) |
| **targetSdk / compileSdk** | 35 |
| **版本** | versionCode=1, versionName=1.0.0 |
| **仓库** | [待补充：Git 远程地址] — 当前源码来自本地目录，不含 .git 历史 |
| **Commit** | [待补充：无 .git 目录，无法获取 commit 哈希] |

---

## 二、开发环境

### 2.1 必需工具

| 工具 | 版本要求 | 说明 |
|------|---------|------|
| **操作系统** | macOS / Linux / Windows | 任意支持 Android Studio 的系统 |
| **JDK** | 17 | `JAVA_HOME` 必须指向 JDK 17 |
| **Gradle** | 8.9 | 项目自带 `gradlew`，无需单独安装 |
| **AGP** | 8.7.0 | 定义在 `build.gradle.kts` |
| **Kotlin** | 2.0.21 | 含 Compose 编译器插件 |
| **Android SDK** | Platform 35 + Build Tools 35.0.0 | 通过 Android Studio 或 sdkmanager 安装 |
| **Android Studio** | Hedgehog (2023.1.1) 或更高 | 推荐，也可用命令行构建 |

### 2.2 环境变量

```bash
export ANDROID_HOME=/path/to/android-sdk
export JAVA_HOME=/path/to/jdk-17
```

### 2.3 第三方账号/密钥

**当前项目为纯本地应用，无需任何第三方 API Key 或账号即可完整运行。**

- 无后端依赖
- 无第三方登录
- 无推送服务（通知使用系统 WorkManager 本地调度）
- 无地图/支付/广告 SDK

---

## 三、构建与运行

### 3.1 从零构建步骤

```bash
# 1. 解压源码
unzip project-handover.zip -d ExpiryGuard
cd ExpiryGuard

# 2. 创建 local.properties（如不存在）
echo "sdk.dir=$ANDROID_HOME" > local.properties

# 3. Debug 构建（推荐，可直接安装）
./gradlew assembleDebug
# APK 位置：app/build/outputs/apk/debug/app-debug.apk

# 4. Release 构建（无签名）
./gradlew assembleRelease
# APK 位置：app/build/outputs/apk/release/app-release-unsigned.apk

# 5. 安装到设备
adb install app/build/outputs/apk/debug/app-debug.apk
```

### 3.2 在 Android Studio 中打开

1. 用 Android Studio 打开 `ExpiryGuard/` 目录
2. 等待 Gradle Sync 完成
3. 选择 Run → Run 'app'

### 3.3 已知坑

| 问题 | 现象 | 解决方案 |
|------|------|----------|
| **内存不足导致 Gradle Daemon 崩溃** | `Gradle build daemon disappeared unexpectedly` | 在 `gradle.properties` 中降低 `-Xmx` 值到 1536m；或关闭其他进程释放内存 |
| **阿里云镜像** | settings.gradle.kts 使用了 `maven.aliyun.com` 镜像 | 国内网络无需额外配置；如果海外开发，可删除 ali 镜像行，仅保留 google()/mavenCentral() |
| **R8/ProGuard Release 构建** | 目前 `isMinifyEnabled = false`（关闭了代码混淆） | 如需开启混淆，改回 `true` 并确保内存充足（mergeDex 阶段内存消耗大） |
| **Gradle 版本** | 使用 gradle-wrapper.properties 中的腾讯云镜像下载 Gradle | 海外网络可改为 `https://services.gradle.org/distributions/gradle-8.9-bin.zip` |

### 3.4 内存建议

构建此项目建议至少 4GB 可用内存。如果构建时 Gradle Daemon 频繁崩溃，可在 `gradle.properties` 中调整：

```properties
org.gradle.jvmargs=-Xmx2048m -XX:MaxMetaspaceSize=512m
org.gradle.daemon=true
org.gradle.parallel=false
org.gradle.caching=false
```

---

## 四、开发进度

### 4.1 已完成

| 功能模块 | 说明 |
|---------|------|
| 商品 CRUD | 添加/编辑/删除/查看商品，含拍照、生产日期、保质期、分类 |
| 保质期计算 | 预设天数（3天/5天/7天/15天/21天/1月/3月/6月/9月/12月/120天/270天/3年/5年）+ 自定义天数输入 |
| 到期日自动计算 | 生产日期 + (保质期天数 - 1)，生产当天算第 1 天 |
| 保质期分组 | 5 个默认分组（当天到期/3个月以下/3~6个月/6个月~1年/1年以上），支持自定义分组管理 |
| 过期规则引擎 | 根据保质期和到期日计算状态（Safe/Returnable/Urgent/Expired），支持退货阈值 |
| 首页仪表盘 | 统计卡片（总数/即将到期/已过期/可退货），快捷入口 |
| 清单列表 | 搜索、筛选（全部/即将到期/可退货/已过期）、批量操作、分类筛选 |
| 统计页面 | Vico 图表展示到期分布、状态分布、分类分布 |
| 回收站 | 软删除、恢复、永久删除 |
| 备份恢复 | JSON 导出/导入（含商品、分类、分组全部数据） |
| 通知提醒 | WorkManager 定期检查 + 系统通知渠道 |
| 分类管理 | 自定义商品分类增删改 |
| Dark Mode 支持 | 自动跟随系统主题 |
| UI 主题 | 毛玻璃卡片效果、自定义配色 |

### 4.2 进行中 / 最近修复

| 项目 | 说明 |
|------|------|
| 分组匹配 Bug 修复 | 原来用剩余天数（而非保质期天数）匹配分组，已修复（2026-07-29） |
| 到期日 Off-by-One 修复 | 原来 `生产日期 + 保质期天数` 多算一天，已修正为 `生产日期 + (天数 - 1)`（2026-07-29） |

### 4.3 已搁置 / 待启动

[待补充：基于产品需求的 backlog]

---

## 五、待开发内容（Backlog）

基于代码分析，以下为建议的待开发内容：

| 优先级 | 功能 | 估算工作量 | 依赖 |
|--------|------|-----------|------|
| **P0** | 编辑页面分组匹配同样使用保质期天数而非剩余天数（`EditProductViewModel.loadProduct` 第74行仍用 `daysBetween` 匹配分组） | 0.5h | 无 |
| **P1** | 商品名称支持自定义（当前添加时固定 name="清单"） | 1h | 无 |
| **P1** | 条形码扫描识别（`ProductEntity` 已预留 `barcode` 字段，`CameraX` 已集成） | 4h | 需集成 ML Kit Barcode Scanning |
| **P1** | 通知提醒点击跳转到对应商品详情 | 1h | 无 |
| **P2** | 多语言国际化（当前仅中文） | 4h | 无 |
| **P2** | 云同步备份（当前仅本地 JSON 导出） | 8h | 需后端/云存储账号 |
| **P2** | Widget 桌面小组件 | 4h | 无 |
| **P3** | 单元测试覆盖 | 8h | 无 |
| **P3** | UI 测试 | 4h | 无 |

---

## 六、架构与关键模块

### 6.1 整体架构

```
┌──────────────────────────────────────────────┐
│  UI Layer (Compose)                           │
│  HomeScreen  ProductListScreen  AddProduct... │
│  StatsScreen  TrashScreen  SettingsScreen...  │
├──────────────────────────────────────────────┤
│  ViewModel Layer (Hilt)                       │
│  HomeViewModel  AddProductViewModel  ...      │
├──────────────────────────────────────────────┤
│  Domain Layer                                 │
│  ProductStatus (sealed class)                 │
│  ExpiryRuleEngine (规则引擎)                   │
├──────────────────────────────────────────────┤
│  Data Layer                                   │
│  ProductRepository → Room DAOs                │
│  AppDatabase (Room)                           │
│  DataStore (Preferences)                      │
└──────────────────────────────────────────────┘
```

### 6.2 模块划分

```
app/src/main/java/com/expiryguard/app/
├── ExpiryGuardApp.kt          # @HiltAndroidApp 入口
├── MainActivity.kt            # 单 Activity 宿主
├── di/
│   ├── AppModule.kt           # DataStore 提供
│   └── DatabaseModule.kt      # Room DB + DAO + Repository 提供
├── navigation/
│   └── AppNavigation.kt       # NavHost 路由 + 底部导航栏
├── domain/
│   ├── engine/
│   │   └── ExpiryRuleEngine.kt # 过期状态计算 + 退货阈值
│   └── model/
│       └── ProductStatus.kt   # 状态密封类
├── data/
│   ├── db/
│   │   ├── AppDatabase.kt
│   │   ├── entity/
│   │   │   ├── ProductEntity.kt
│   │   │   ├── CategoryEntity.kt
│   │   │   └── ShelfLifeGroupEntity.kt
│   │   └── dao/
│   │       ├── ProductDao.kt
│   │       ├── CategoryDao.kt
│   │       └── ShelfLifeGroupDao.kt
│   └── repository/
│       └── ProductRepository.kt
├── ui/
│   ├── theme/                 # Color/Type/Theme
│   ├── components/            # 可复用 Compose 组件
│   ├── home/                  # 首页
│   ├── product/               # 商品 CRUD
│   ├── stats/                 # 统计
│   ├── trash/                 # 回收站
│   └── settings/              # 设置/分类/分组/备份/通知/关于
├── util/
│   ├── DateUtils.kt           # 日期工具
│   └── ImageUtils.kt          # 图片保存
└── notification/
    └── NotificationHelper.kt  # 通知渠道
```

### 6.3 核心入口类

| 类 | 路径 | 职责 |
|----|------|------|
| `ExpiryGuardApp` | `app/ExpiryGuardApp.kt` | `@HiltAndroidApp`，初始化通知渠道 |
| `MainActivity` | `app/MainActivity.kt` | 单 Activity，加载 `AppNavigation` |
| `AppNavigation` | `app/navigation/AppNavigation.kt` | 全部路由定义 + 底部导航栏 + 相机权限处理 |
| `ExpiryRuleEngine` | `app/domain/engine/ExpiryRuleEngine.kt` | 核心规则：根据保质期计算状态和退货阈值 |
| `ProductRepository` | `app/data/repository/ProductRepository.kt` | 数据层唯一入口，封装全部 DAO 操作 |

### 6.4 数据库表

| 表 | 实体 | 关键字段 |
|----|------|---------|
| `products` | `ProductEntity` | name, barcode, categoryId, photoPath, productionDate, expiryDate, shelfLifeDays, deletedAt(软删除), isCompleted |
| `categories` | `CategoryEntity` | name, colorHex, sortOrder |
| `shelf_life_groups` | `ShelfLifeGroupEntity` | name, minDays, maxDays, reminderThreshold, colorHex, isDefault |

### 6.5 已知技术债

| 问题 | 影响 | 建议 |
|------|------|------|
| `EditProductViewModel.loadProduct` 第74行用 `daysBetween`（剩余天数）而非 `shelfLifeDays`（保质期）匹配分组 | 编辑页面分组可能显示不正确 | 参照 `AddProductViewModel` 的修复逻辑处理 |
| 添加商品时 `name` 固定为 `"清单"` | 所有商品名称相同，无法区分 | 增加名称输入字段 |
| `DatabaseModule` 使用 `fallbackToDestructiveMigration()` | 数据库升级时会丢失数据 | 生产环境应提供 Migration 策略 |
| 缺少单元测试 | 重构风险高 | 优先对 `ExpiryRuleEngine` 和 `DateUtils` 补充测试 |
| BackupRestore 的 JSON 格式无版本号 | 未来字段变更后旧备份可能无法恢复 | 增加 schemaVersion 字段 |
| `AddProductViewModel` 和 `EditProductViewModel` 存在重复代码（`matchGroupByDays`、`estimateShelfLifeDays`、`loadShelfLifeGroups`） | 维护成本高 | 抽取公共基类或工具类 |

---

## 七、账号与密钥

本应用为纯本地应用，**无需任何第三方账号或密钥**即可完整运行。所有功能均基于本地 Room 数据库和系统 API。

涉及系统权限（需用户授权）：
- `CAMERA` — 拍照添加商品
- `POST_NOTIFICATIONS` — 到期提醒（Android 13+）
- `SCHEDULE_EXACT_ALARM` — 定时检查到期

---

## 八、验收标准

新人按以下步骤操作，视为成功接管：

1. **[ ] 环境搭建** — 安装 JDK 17 + Android SDK 35，配置 `ANDROID_HOME` 和 `JAVA_HOME`
2. **[ ] 构建成功** — 执行 `./gradlew assembleDebug`，输出 `BUILD SUCCESSFUL`，生成 `app-debug.apk`
3. **[ ] 安装运行** — 安装 APK 到设备/模拟器，应用正常启动，显示首页
4. **[ ] 添加商品** — 拍照或选择图片 → 选择生产日期 → 选择保质期（如 21 天）→ 确认到期日正确 → 保存
5. **[ ] 验证分组** — 保质期 270 天应自动匹配到"6个月~1年"分组
6. **[ ] 查看清单** — 底部导航切换到"清单"Tab，能看到刚添加的商品
7. **[ ] 统计页面** — 切换到"统计"Tab，图表正常显示
8. **[ ] 阅读本文档** — 理解架构分层和模块划分，知道去哪改代码
9. **[ ] 进行一次修改** — 例如修改 `AddProductViewModel` 中 `saveProduct()` 的默认名称，重新构建并验证

---

## 附录 A：依赖清单

| 依赖 | 版本 | 用途 |
|------|------|------|
| Compose BOM | 2024.10.01 | Compose UI 套件版本管理 |
| Material3 | (BOM) | UI 组件 |
| Material Icons Extended | (BOM) | 图标库 |
| Activity Compose | 1.9.2 | Compose Activity 集成 |
| Lifecycle ViewModel Compose | 2.8.6 | ViewModel + Compose |
| Navigation Compose | 2.8.3 | 页面路由 |
| Room | 2.6.1 | 本地数据库 |
| Hilt | 2.51.1 | 依赖注入 |
| CameraX | 1.3.4 | 相机拍照 |
| Coil Compose | 2.7.0 | 图片加载 |
| Vico | 2.0.0-beta.2 | 统计图表 |
| DataStore Preferences | 1.1.1 | 键值对存储 |
| WorkManager | 2.9.1 | 后台任务调度 |
| Gson | 2.11.0 | JSON 序列化（备份恢复） |
| Core KTX | 1.13.1 | Android 核心扩展 |

## 附录 B：路由表

| 路由 | 页面 | 参数 |
|------|------|------|
| `home` | 首页 | 无 |
| `product_list` | 清单列表 | `searchQuery`, `filter` |
| `add_product` | 添加商品 | `photoPath` (可选) |
| `product_detail/{productId}` | 商品详情 | `productId: Long` |
| `edit_product/{productId}` | 编辑商品 | `productId: Long` |
| `stats` | 统计 | 无 |
| `trash` | 回收站 | 无 |
| `settings` | 设置 | 无 |
| `backup_restore` | 备份恢复 | 无 |
| `category_management` | 分类管理 | 无 |
| `shelf_life_group_management` | 分组管理 | 无 |
| `notification_settings` | 通知设置 | 无 |
| `about` | 关于 | 无 |