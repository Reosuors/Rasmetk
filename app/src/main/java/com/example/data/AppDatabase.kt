package com.example.data

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface DrawingDao {
    @Query("SELECT * FROM drawings ORDER BY timestamp DESC")
    fun getAllDrawingsFlow(): Flow<List<DrawingEntity>>

    @Query("SELECT * FROM drawings WHERE id = :id")
    fun getDrawingByIdFlow(id: Int): Flow<DrawingEntity?>

    @Query("SELECT * FROM drawings WHERE id = :id")
    suspend fun getDrawingById(id: Int): DrawingEntity?

    @Query("SELECT * FROM drawings WHERE drawingIdString = :code OR title LIKE :query OR authorName LIKE :query")
    fun searchDrawingsFlow(code: String, query: String): Flow<List<DrawingEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDrawing(drawing: DrawingEntity): Long

    @Update
    suspend fun updateDrawing(drawing: DrawingEntity)

    @Query("DELETE FROM drawings WHERE id = :id")
    suspend fun deleteDrawingById(id: Int)

    @Query("SELECT COUNT(*) FROM drawings")
    suspend fun getDrawingCount(): Int
}

@Dao
interface CommentDao {
    @Query("SELECT * FROM comments WHERE drawingId = :drawingId ORDER BY timestamp ASC")
    fun getCommentsForDrawingFlow(drawingId: Int): Flow<List<CommentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComment(comment: CommentEntity)

    @Query("DELETE FROM comments WHERE drawingId = :drawingId")
    suspend fun deleteCommentsForDrawing(drawingId: Int)
}

@Dao
interface FollowDao {
    @Query("SELECT * FROM followed_artists")
    fun getAllFollowedFlow(): Flow<List<FollowEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM followed_artists WHERE artistName = :name)")
    fun isArtistFollowedFlow(name: String): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun followArtist(follow: FollowEntity)

    @Query("DELETE FROM followed_artists WHERE artistName = :name")
    suspend fun unfollowArtist(name: String)
}

@Database(
    entities = [DrawingEntity::class, CommentEntity::class, FollowEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun drawingDao(): DrawingDao
    abstract fun commentDao(): CommentDao
    abstract fun followDao(): FollowDao
}
