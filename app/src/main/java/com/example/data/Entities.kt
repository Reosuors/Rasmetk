package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "drawings")
data class DrawingEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String = "",
    val drawingIdString: String, // Public unique id (e.g. "R-3982")
    val imagePath: String, // Local storage file path or base64
    val isPublic: Boolean,
    val authorName: String,
    val likesCount: Int = 0,
    val likedByUsersJson: String = "[]", // JSON array of user names who liked this drawing
    val timestamp: Long = System.currentTimeMillis(),
    val isLocalOriginal: Boolean = false // True if painted on this device
)

@Entity(tableName = "comments")
data class CommentEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val drawingId: Int,
    val authorName: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "followed_artists")
data class FollowEntity(
    @PrimaryKey val artistName: String,
    val timestamp: Long = System.currentTimeMillis()
)
