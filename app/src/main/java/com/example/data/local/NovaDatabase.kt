package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        PlaybackHistoryItem::class,
        SceneNoteItem::class,
        BookmarkItem::class,
        StudyCollection::class,
        FavoriteItem::class,
        VideoPlaylistEntity::class,
        PlaylistItemEntity::class,
        RecycleBinItem::class,
        UserPreference::class
    ],
    version = 3,
    exportSchema = false
)
abstract class NovaDatabase : RoomDatabase() {
    abstract fun mediaDao(): MediaDao

    companion object {
        @Volatile
        private var INSTANCE: NovaDatabase? = null

        fun getDatabase(context: Context): NovaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NovaDatabase::class.java,
                    "nova_player_database"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
