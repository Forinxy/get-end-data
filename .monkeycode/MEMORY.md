# User Instruction Memory

This file records user instructions, preferences, and teachings for reference in future interactions.

## Format

### User Instruction Entry
User instruction entries should follow this format:

[User Instruction Summary]
- Date: [YYYY-MM-DD]
- Context: [Mentioned scenario or time]
- Instructions:
  - [Content of user teaching or instruction, described line by line]

### Project Knowledge Entry
Entries discovered by the Agent during task execution should follow this format:

[Project Knowledge Summary]
- Date: [YYYY-MM-DD]
- Context: Discovered by Agent while performing [specific task description]
- Category: [Operations & Deployment|Build Methods|Testing Methods|Troubleshooting & Debugging|Workflow & Collaboration|Environment Configuration]
- Instructions:
  - [Specific knowledge points, described line by line]

## Deduplication Strategy
- Before adding a new entry, check for similar or identical instructions.
- If a duplicate is found, skip the new entry or merge it with the existing one.
- When merging, update the context or date information.
- This helps avoid redundant entries and keeps the memory file tidy.

## Entries

[User Instruction Summary]
- Date: 2026-08-14
- Context: 用户反馈重新安装后记录全部丢失，并约定发布命名规范
- Instructions:
  - GitHub Release 发布的 APK 命名规范为「应用名+版本号.apk」，例如 ExpiryGuard_1.0.1.apk，不再使用含 debug/unsigned 后缀的命名
  - 每次发布 GitHub Release 时，必须同时附带源码压缩包（应用名+版本号+源码.zip 或类似命名）
  - 用户重视数据不丢失：应用需提供可靠的备份/恢复能力，数据库升级必须使用显式 Migration 而非 destructive 兜底

[Project Knowledge Summary]
- Date: 2026-08-14
- Context: Discovered by Agent while diagnosing 用户重新安装后数据丢失问题
- Category: Troubleshooting & Debugging
- Instructions:
  - 卸载应用会删除本地 Room 数据库（expiry_guard.db），重新安装后数据必然丢失，需依赖应用内「备份与恢复」功能导出/导入数据
  - 该应用旧版本数据库版本为 v1/v2/v3，若从 v2/v1 直接升级到 v4 且无完整迁移链，会触发 fallbackToDestructiveMigration 清空数据
  - 排查数据丢失时先对比新旧 APK 签名（apksigner verify --print-certs）排除签名变更导致的卸载重装
