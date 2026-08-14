package com.expiryguard.app.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.expiryguard.app.data.db.dao.CategoryDao
import com.expiryguard.app.data.db.dao.ProductDao
import com.expiryguard.app.data.db.dao.ShelfLifeGroupDao
import com.expiryguard.app.data.db.entity.CategoryEntity
import com.expiryguard.app.data.db.entity.ProductEntity
import com.expiryguard.app.data.db.entity.ShelfLifeGroupEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Room 数据库，version=4，包含产品和分类、保质期分组三张表
 *
 * 版本历史：
 * v1 - 初始版本
 * v2 - 新增 isCompleted 字段（ProductEntity）
 * v3 - 新增 shelf_life_groups 表（ShelfLifeGroupEntity）
 * v4 - 新增 completedAt 字段（ProductEntity），用于区分今日/历史已完成
 */
@Database(
    entities = [ProductEntity::class, CategoryEntity::class, ShelfLifeGroupEntity::class],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun productDao(): ProductDao
    abstract fun categoryDao(): CategoryDao
    abstract fun shelfLifeGroupDao(): ShelfLifeGroupDao

    companion object {
        private const val DATABASE_NAME = "expiry_guard.db"

        /**
         * v3 -> v4 迁移：products 表新增 completedAt 列，记录完成时间
         */
        private val MIGRATION_3_4 = object : androidx.room.migration.Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE products ADD COLUMN completedAt INTEGER")
            }
        }

        /**
         * 单例实例，防止同时创建多个数据库实例
         */
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * 获取数据库实例，使用双重检查锁定确保线程安全
         *
         * @param context 应用上下文
         * @return AppDatabase 实例
         */
        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
            }
        }

        /**
         * 构建数据库
         */
        private fun buildDatabase(context: Context): AppDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                DATABASE_NAME
            )
                .addMigrations(MIGRATION_3_4)
                .fallbackToDestructiveMigration()
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        // 在数据库创建时插入默认分组数据
                        INSTANCE?.let { database ->
                            CoroutineScope(Dispatchers.IO).launch {
                                insertDefaultGroups(database.shelfLifeGroupDao())
                            }
                        }
                    }
                })
                .build()
        }

        /**
         * 插入默认的 5 个保质期分组
         */
        private suspend fun insertDefaultGroups(dao: ShelfLifeGroupDao) {
            val defaultGroups = listOf(
                ShelfLifeGroupEntity(
                    name = "当天到期",
                    description = "今日到期提醒",
                    minDays = 0,
                    maxDays = 1,
                    reminderThreshold = 0,
                    colorHex = "#EF4444",
                    sortOrder = 0,
                    isDefault = true
                ),
                ShelfLifeGroupEntity(
                    name = "3个月以下",
                    description = "今日到期提醒",
                    minDays = 1,
                    maxDays = 90,
                    reminderThreshold = 0,
                    colorHex = "#F97316",
                    sortOrder = 1,
                    isDefault = true
                ),
                ShelfLifeGroupEntity(
                    name = "3~6个月",
                    description = "提前15天提醒",
                    minDays = 90,
                    maxDays = 183,
                    reminderThreshold = 15,
                    colorHex = "#F59E0B",
                    sortOrder = 2,
                    isDefault = true
                ),
                ShelfLifeGroupEntity(
                    name = "6个月~1年",
                    description = "提前20天提醒",
                    minDays = 183,
                    maxDays = 365,
                    reminderThreshold = 20,
                    colorHex = "#10B981",
                    sortOrder = 3,
                    isDefault = true
                ),
                ShelfLifeGroupEntity(
                    name = "1年以上",
                    description = "提前45天提醒",
                    minDays = 365,
                    maxDays = Int.MAX_VALUE,
                    reminderThreshold = 45,
                    colorHex = "#6B7280",
                    sortOrder = 4,
                    isDefault = true
                )
            )
            dao.insertDefaultGroups(defaultGroups)
        }
    }
}