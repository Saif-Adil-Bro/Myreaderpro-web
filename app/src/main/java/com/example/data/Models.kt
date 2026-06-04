package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val email: String,
    val name: String,
    val profilePicture: String = "avatar_default",
    val isGuest: Boolean = false,
    val readingHours: Float = 1.2f,
    val booksRead: Int = 2,
    val totalDownloads: Int = 3,
    val readingStreak: Int = 5,
    val totalPagesRead: Int = 114,
    val membershipType: String = "VIP", // FREE, PREMIUM, VIP
    val isSupporter: Boolean = true,
    val totalDonationAmount: Float = 25.0f,
    val role: String = "USER", // USER, MODERATOR, ADMIN
    val isLoggedIn: Boolean = false,
    val dailyReadingGoalMinutes: Int = 30,
    val todayMinutesRead: Float = 0.0f
)

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey val id: String,
    val name: String,
    val iconName: String
)

@Entity(tableName = "books")
data class BookEntity(
    @PrimaryKey val id: String,
    val title: String,
    val author: String,
    val categoryId: String,
    val description: String,
    val coverUrl: String = "",
    val pages: Int = 120,
    val fileSize: String = "2.4 MB",
    val language: String = "English",
    val rating: Float = 4.8f,
    val downloads: Int = 1240,
    val isFeatured: Boolean = false,
    val isFavorite: Boolean = false,
    val isDownloaded: Boolean = false,
    val downloadStatus: String = "NONE", // NONE, DOWNLOADING, DOWNLOADED, FAILED
    val downloadProgress: Float = 0f,
    val lastReadPosition: Int = 0,
    val contentMarkdown: String = "", // Hold dummy chapters
    val isPremium: Boolean = false,
    val vipOnly: Boolean = false,
    val adFreeAvailable: Boolean = false,
    val seoUrl: String = "",
    val metaTitle: String = "",
    val metaDescription: String = "",
    val tags: String = "",
    val fileFormat: String = "EPUB" // EPUB, PDF, ARTICLE
)

@Entity(tableName = "bookmarks")
data class BookmarkEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val bookId: String,
    val pageNumber: Int,
    val title: String,
    val snippet: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "reading_notes")
data class NoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val bookId: String,
    val pageNumber: Int,
    val text: String,
    val highlightText: String = "",
    val colorHex: String = "#F59E0B",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "reading_history")
data class HistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val bookId: String,
    val timestamp: Long = System.currentTimeMillis(),
    val progressPercent: Float = 0f,
    val pageNumber: Int = 0
)

@Entity(tableName = "achievements")
data class AchievementEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val isUnlocked: Boolean = false,
    val iconName: String,
    val unlockTime: Long = 0L
)

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val type: String = "SYSTEM"
)

@Entity(tableName = "book_requests")
data class BookRequestEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val author: String,
    val publisher: String,
    val notes: String = "",
    val status: String = "PENDING", // PENDING, COMPLETED, REJECTED
    val requestedAt: Long = System.currentTimeMillis(),
    val userEmail: String = "",
    val requestCount: Int = 1
)

@Entity(tableName = "copyright_claims")
data class CopyrightClaimEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val fullName: String,
    val email: String,
    val organizationName: String = "",
    val contentTitle: String,
    val contentUrl: String,
    val description: String,
    val supportingDocumentsInfo: String = "",
    val status: String = "PENDING", // PENDING, REVIEWING, APPROVED, REJECTED
    val claimDate: Long = System.currentTimeMillis(),
    val reviewerEmail: String? = null,
    val decisionNotes: String = "",
    val temporaryHidden: Boolean = false
)

@Entity(tableName = "ad_blocks")
data class AdBlockEntity(
    @PrimaryKey val id: String,
    val title: String,
    val adType: String, // BANNER, NATIVE, INTERSTITIAL, REWARDED
    val isEnabled: Boolean = true,
    val imageUrl: String = "default_ad_banner",
    val clicks: Int = 0,
    val impressions: Int = 0
)

@Entity(tableName = "forum_posts")
data class PostEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val content: String,
    val type: String = "TEXT", // TEXT, IMAGE, DISCUSSION, TIPS, ANNOUNCEMENT
    val authorName: String,
    val authorEmail: String,
    val authorAvatar: String = "avatar_default",
    val imageUrl: String = "",
    val bookTitleRef: String = "",
    val likesCount: Int = 0,
    val commentsCount: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val isLiked: Boolean = false,
    val isSaved: Boolean = false,
    val reportCount: Int = 0
)

@Entity(tableName = "forum_comments")
data class CommentEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val postId: Int,
    val authorName: String,
    val authorEmail: String,
    val authorAvatar: String = "avatar_default",
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val parentCommentId: Int = -1
)

@Entity(tableName = "words")
data class WordEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val word: String,
    val definition: String = "",
    val translation: String = "",
    val pronunciation: String = "",
    val bookTitle: String = "",
    val sentenceContext: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "ai_flashcards")
data class FlashcardEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val bookId: String,
    val question: String,
    val answer: String,
    val timestamp: Long = System.currentTimeMillis()
)

