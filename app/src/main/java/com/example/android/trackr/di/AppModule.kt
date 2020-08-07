package com.example.android.trackr.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.android.trackr.DefaultScheduler
import com.example.android.trackr.data.SeedData
import com.example.android.trackr.db.AppDatabase
import com.example.android.trackr.db.dao.TaskDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Singleton

@Module
@InstallIn(ApplicationComponent::class)
object AppModule {

    private lateinit var appDatabase: AppDatabase

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        appDatabase =  Room.databaseBuilder(
            context,
            AppDatabase::class.java, "trackr-db"
        )
            .addCallback(object : RoomDatabase.Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)
                    DefaultScheduler.execute {
                        with(appDatabase) {
                            runInTransaction {
                                with(taskDao()) {
                                    insertUsers(SeedData.Users)
                                    insertTags(SeedData.Tags)
                                    insertTasks(SeedData.Tasks)
                                    insertTaskTags(SeedData.TaskTags)
                                }
                            }
                        }
                    }
                }
            })
            .build()
        return appDatabase
    }

    @Provides
    fun provideTaskDao(appDatabase: AppDatabase): TaskDao {
        return appDatabase.taskDao()
    }
}