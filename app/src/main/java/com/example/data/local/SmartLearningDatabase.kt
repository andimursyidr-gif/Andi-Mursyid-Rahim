package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.*

@Database(
    entities = [
        ClassEntity::class,
        StudentEntity::class,
        AttendanceEntity::class,
        VideoEntity::class,
        TeachingModuleEntity::class,
        LearningMediaEntity::class,
        WorksheetEntity::class,
        WorksheetSubmissionEntity::class,
        QuizEntity::class,
        QuestionEntity::class,
        QuizAttemptEntity::class,
        AnnouncementEntity::class,
        SchoolSettingsEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class SmartLearningDatabase : RoomDatabase() {
    abstract fun smartLearningDao(): SmartLearningDao

    companion object {
        @Volatile
        private var INSTANCE: SmartLearningDatabase? = null

        fun getDatabase(context: Context): SmartLearningDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SmartLearningDatabase::class.java,
                    "smart_learning.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
