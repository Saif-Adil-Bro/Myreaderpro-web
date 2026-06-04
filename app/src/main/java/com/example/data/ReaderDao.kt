package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ReaderDao {
    // Users
    @Query("SELECT * FROM users WHERE isLoggedIn = 1 LIMIT 1")
    fun getActiveUser(): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE isLoggedIn = 1 LIMIT 1")
    suspend fun getActiveUserSync(): UserEntity?

    @Query("UPDATE users SET isLoggedIn = 0")
    suspend fun clearActiveSessions()

    @Query("SELECT * FROM users")
    fun getAllUsers(): Flow<List<UserEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Update
    suspend fun updateUser(user: UserEntity)

    // Categories
    @Query("SELECT * FROM categories")
    fun getCategories(): Flow<List<CategoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<CategoryEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: CategoryEntity)

    @Query("DELETE FROM categories WHERE id = :id")
    suspend fun deleteCategoryById(id: String)

    // Books
    @Query("SELECT * FROM books")
    fun getAllBooks(): Flow<List<BookEntity>>

    @Query("SELECT * FROM books WHERE id = :id")
    fun getBookById(id: String): Flow<BookEntity?>

    @Query("SELECT * FROM books WHERE id = :id")
    suspend fun getBookByIdSync(id: String): BookEntity?

    @Query("SELECT * FROM books WHERE isFavorite = 1")
    fun getFavoriteBooks(): Flow<List<BookEntity>>

    @Query("SELECT b.* FROM books b INNER JOIN reading_history h ON b.id = h.bookId ORDER BY h.timestamp DESC")
    fun getReadingHistoryBooks(): Flow<List<BookEntity>>

    @Query("SELECT * FROM books WHERE downloadStatus = 'DOWNLOADED'")
    fun getDownloadedBooks(): Flow<List<BookEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBooks(books: List<BookEntity>)

    @Update
    suspend fun updateBook(book: BookEntity)

    // Bookmarks
    @Query("SELECT * FROM bookmarks WHERE bookId = :bookId ORDER BY pageNumber ASC")
    fun getBookmarksForBook(bookId: String): Flow<List<BookmarkEntity>>

    @Query("SELECT * FROM bookmarks ORDER BY timestamp DESC")
    fun getAllBookmarks(): Flow<List<BookmarkEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(bookmark: BookmarkEntity)

    @Delete
    suspend fun deleteBookmark(bookmark: BookmarkEntity)

    // Notes
    @Query("SELECT * FROM reading_notes WHERE bookId = :bookId ORDER BY pageNumber ASC")
    fun getNotesForBook(bookId: String): Flow<List<NoteEntity>>

    @Query("SELECT * FROM reading_notes ORDER BY timestamp DESC")
    fun getAllNotes(): Flow<List<NoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: NoteEntity)

    @Delete
    suspend fun deleteNote(note: NoteEntity)

    // History
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: HistoryEntity)

    @Query("SELECT * FROM reading_history ORDER BY timestamp DESC")
    fun getRawReadingHistory(): Flow<List<HistoryEntity>>

    @Query("DELETE FROM reading_history WHERE bookId = :bookId")
    suspend fun deleteHistoryForBook(bookId: String)

    // Achievements
    @Query("SELECT * FROM achievements")
    fun getAchievements(): Flow<List<AchievementEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAchievements(achievements: List<AchievementEntity>)

    @Update
    suspend fun updateAchievement(achievement: AchievementEntity)

    // Notifications
    @Query("SELECT * FROM notifications ORDER BY timestamp DESC")
    fun getNotifications(): Flow<List<NotificationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity)

    @Query("UPDATE notifications SET isRead = 1 WHERE id = :id")
    suspend fun markNotificationAsRead(id: Int)

    @Query("UPDATE notifications SET isRead = 1")
    suspend fun markAllNotificationsAsRead()

    @Query("DELETE FROM notifications WHERE id = :id")
    suspend fun deleteNotificationById(id: Int)

    // Book Requests
    @Query("SELECT * FROM book_requests ORDER BY requestedAt DESC")
    fun getAllBookRequests(): Flow<List<BookRequestEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookRequest(request: BookRequestEntity)

    @Update
    suspend fun updateBookRequest(request: BookRequestEntity)

    @Delete
    suspend fun deleteBookRequest(request: BookRequestEntity)

    @Query("SELECT * FROM book_requests WHERE title LIKE '%' || :query || '%' OR author LIKE '%' || :query || '%'")
    fun searchBookRequests(query: String): Flow<List<BookRequestEntity>>

    // Copyright / DMCA Claims
    @Query("SELECT * FROM copyright_claims ORDER BY claimDate DESC")
    fun getAllCopyrightClaims(): Flow<List<CopyrightClaimEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCopyrightClaim(claim: CopyrightClaimEntity)

    @Update
    suspend fun updateCopyrightClaim(claim: CopyrightClaimEntity)

    // Ad Blocks management
    @Query("SELECT * FROM ad_blocks")
    fun getAllAdBlocks(): Flow<List<AdBlockEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAdBlock(adBlock: AdBlockEntity)

    @Update
    suspend fun updateAdBlock(adBlock: AdBlockEntity)

    // Forum Posts & Comments
    @Query("SELECT * FROM forum_posts ORDER BY timestamp DESC")
    fun getAllPosts(): Flow<List<PostEntity>>

    @Query("SELECT * FROM forum_posts WHERE isSaved = 1 ORDER BY timestamp DESC")
    fun getSavedPosts(): Flow<List<PostEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPost(post: PostEntity)

    @Update
    suspend fun updatePost(post: PostEntity)

    @Delete
    suspend fun deletePost(post: PostEntity)

    @Query("SELECT * FROM forum_comments WHERE postId = :postId ORDER BY timestamp ASC")
    fun getCommentsForPost(postId: Int): Flow<List<CommentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComment(comment: CommentEntity)

    @Delete
    suspend fun deleteComment(comment: CommentEntity)

    @Query("SELECT * FROM forum_posts WHERE id = :id")
    suspend fun getPostById(id: Int): PostEntity?

    // Wordbook / Vocabulary Booklet
    @Query("SELECT * FROM words ORDER BY timestamp DESC")
    fun getAllWords(): Flow<List<WordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWord(word: WordEntity)

    @Delete
    suspend fun deleteWord(word: WordEntity)

    // Flashcards / study materials
    @Query("SELECT * FROM ai_flashcards WHERE bookId = :bookId ORDER BY timestamp DESC")
    fun getFlashcardsForBook(bookId: String): Flow<List<FlashcardEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFlashcard(flashcard: FlashcardEntity)

    @Delete
    suspend fun deleteFlashcard(flashcard: FlashcardEntity)

    @Query("DELETE FROM ai_flashcards WHERE bookId = :bookId")
    suspend fun clearFlashcardsForBook(bookId: String)
}

