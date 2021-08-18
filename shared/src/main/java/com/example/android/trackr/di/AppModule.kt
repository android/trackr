/*
 * Copyright (C) 2021 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.trackr.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.withTransaction
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.android.trackr.data.SeedData
import com.example.android.trackr.data.User
import com.example.android.trackr.db.AppDatabase
import com.example.android.trackr.db.dao.TaskDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.time.Clock
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    private lateinit var appDatabase: AppDatabase
    private lateinit var clock: Clock

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        appDatabase = Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "trackr-db"
        )
            .fallbackToDestructiveMigration()
            .addCallback(object : RoomDatabase.Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)
                    GlobalScope.launch(Dispatchers.IO) {
                        insertSeedData()
                    }
                }
            })
            .build()
        return appDatabase
    }

    suspend fun insertSeedData() {
        with(appDatabase) {
            withTransaction {
                with(taskDao()) {
                    insertUsers(SeedData.Users)
                    insertTags(SeedData.Tags)
                    insertTasks(SeedData.Tasks)
                    insertTaskTags(SeedData.TaskTags)
                    insertUserTasks(SeedData.UserTasks)
                }
            }
        }
    }

    @Provides
    fun provideCurrentUser(): User {
        // For simplicity of sample, assign first user as current user
        return SeedData.Users[0]
    }

    @Provides
    fun provideTaskDao(appDatabase: AppDatabase): TaskDao {
        return appDatabase.taskDao()
    }

    @Provides
    @Singleton
    fun provideClock(): Clock {
        clock = Clock.systemDefaultZone()
        return clock
    }
}
