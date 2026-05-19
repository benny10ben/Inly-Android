package com.ben.inly.data.local.room

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor

/**
 * The main Room database setup.
 * It registers the entities (tables) and DAOs so the app can query local metadata.
 */
@Database(
    entities = [NoteMetadataEntity::class, FolderEntity::class, TagEntity::class],
    version = 1,
    exportSchema = false // true to track schema changes in git
)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao
    abstract fun folderDao(): FolderDao
    abstract fun tagDao(): TagDao
}

/**
 * Required by Room KMP. The Room compiler automatically generates the `actual`
 * implementation of this object for Android and Desktop during the build process.
 */
@Suppress("NO_ACTUAL_FOR_EXPECT")
expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
    override fun initialize(): AppDatabase
}