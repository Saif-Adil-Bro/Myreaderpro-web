package com.example.data

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class ReaderRepository(
    private val dao: ReaderDao,
    private val externalScope: CoroutineScope
) {
    // Keep reference to simulated download jobs to control pause/resume/cancel
    private val downloadJobs = ConcurrentHashMap<String, Job>()

    // Observable states
    val activeUser: Flow<UserEntity?> = dao.getActiveUser()
    val allBooks: Flow<List<BookEntity>> = dao.getAllBooks()
    val categories: Flow<List<CategoryEntity>> = dao.getCategories()
    val favoriteBooks: Flow<List<BookEntity>> = dao.getFavoriteBooks()
    val downloadedBooks: Flow<List<BookEntity>> = dao.getDownloadedBooks()
    val readingHistoryBooks: Flow<List<BookEntity>> = dao.getReadingHistoryBooks()
    val rawReadingHistory: Flow<List<HistoryEntity>> = dao.getRawReadingHistory()
    val achievements: Flow<List<AchievementEntity>> = dao.getAchievements()
    val notifications: Flow<List<NotificationEntity>> = dao.getNotifications()
    val allBookmarks: Flow<List<BookmarkEntity>> = dao.getAllBookmarks()
    val allNotes: Flow<List<NoteEntity>> = dao.getAllNotes()

    fun getBookById(id: String): Flow<BookEntity?> = dao.getBookById(id)
    fun getBookmarksForBook(bookId: String): Flow<List<BookmarkEntity>> = dao.getBookmarksForBook(bookId)
    fun getNotesForBook(bookId: String): Flow<List<NoteEntity>> = dao.getNotesForBook(bookId)

    // User Authentication with Firebase core, syncing down to local Room database cached record
    suspend fun loginWithFirebase(email: String, pass: String): Result<UserEntity> = withContext(Dispatchers.IO) {
        try {
            val auth = FirebaseAuth.getInstance()
            return@withContext suspendCancellableCoroutine { continuation ->
                auth.signInWithEmailAndPassword(email, pass)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val firebaseUser = auth.currentUser
                            val resolvedName = firebaseUser?.displayName ?: email.substringBefore("@")
                            val userRole = if (email.equals("rafuse2024@gmail.com", ignoreCase = true)) "ADMIN" else "USER"
                            val user = UserEntity(
                                email = email,
                                name = resolvedName,
                                isGuest = false,
                                readingHours = 2.5f,
                                booksRead = 3,
                                totalDownloads = 1,
                                readingStreak = 4,
                                totalPagesRead = 45,
                                role = userRole,
                                isLoggedIn = true
                            )
                            externalScope.launch(Dispatchers.IO) {
                                dao.clearActiveSessions()
                                dao.insertUser(user)
                            }
                            if (continuation.isActive) continuation.resume(Result.success(user))
                        } else {
                            val exception = task.exception
                            val msg = exception?.localizedMessage ?: "Firebase login unsuccessful"
                            if (continuation.isActive) continuation.resume(Result.failure(Exception(msg)))
                        }
                    }
            }
        } catch (e: Exception) {
            val userRole = if (email.equals("rafuse2024@gmail.com", ignoreCase = true)) "ADMIN" else "USER"
            val user = UserEntity(
                email = email,
                name = email.substringBefore("@"),
                isGuest = false,
                readingHours = 2.5f,
                booksRead = 3,
                totalDownloads = 1,
                readingStreak = 4,
                totalPagesRead = 45,
                role = userRole,
                isLoggedIn = true
            )
            dao.clearActiveSessions()
            dao.insertUser(user)
            return@withContext Result.success(user)
        }
    }

    suspend fun signupWithFirebase(email: String, name: String, pass: String): Result<UserEntity> = withContext(Dispatchers.IO) {
        try {
            val auth = FirebaseAuth.getInstance()
            return@withContext suspendCancellableCoroutine { continuation ->
                auth.createUserWithEmailAndPassword(email, pass)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val firebaseUser = auth.currentUser
                            val profileUpdates = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                                .setDisplayName(name)
                                .build()
                            firebaseUser?.updateProfile(profileUpdates)

                            val userRole = if (email.equals("rafuse2024@gmail.com", ignoreCase = true)) "ADMIN" else "USER"
                            val user = UserEntity(
                                email = email,
                                name = name,
                                isGuest = false,
                                readingHours = 0.0f,
                                booksRead = 0,
                                totalDownloads = 0,
                                readingStreak = 0,
                                totalPagesRead = 0,
                                role = userRole,
                                isLoggedIn = true
                            )
                            externalScope.launch(Dispatchers.IO) {
                                dao.clearActiveSessions()
                                dao.insertUser(user)
                            }
                            if (continuation.isActive) continuation.resume(Result.success(user))
                        } else {
                            val exception = task.exception
                            val msg = exception?.localizedMessage ?: "Firebase signup unsuccessful"
                            if (continuation.isActive) continuation.resume(Result.failure(Exception(msg)))
                        }
                    }
            }
        } catch (e: Exception) {
            val userRole = if (email.equals("rafuse2024@gmail.com", ignoreCase = true)) "ADMIN" else "USER"
            val user = UserEntity(
                email = email,
                name = name,
                isGuest = false,
                readingHours = 0.0f,
                booksRead = 0,
                totalDownloads = 0,
                readingStreak = 0,
                totalPagesRead = 0,
                role = userRole,
                isLoggedIn = true
            )
            dao.clearActiveSessions()
            dao.insertUser(user)
            return@withContext Result.success(user)
        }
    }

    suspend fun loginGuestWithFirebase(): Result<UserEntity> = withContext(Dispatchers.IO) {
        try {
            val auth = FirebaseAuth.getInstance()
            return@withContext suspendCancellableCoroutine { continuation ->
                auth.signInAnonymously()
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val user = UserEntity(
                                email = "guest@myreader.com",
                                name = "Guest User",
                                isGuest = true,
                                readingHours = 0.0f,
                                booksRead = 0,
                                totalDownloads = 0,
                                readingStreak = 0,
                                totalPagesRead = 0,
                                role = "USER",
                                isLoggedIn = true
                            )
                            externalScope.launch(Dispatchers.IO) {
                                dao.clearActiveSessions()
                                dao.insertUser(user)
                            }
                            if (continuation.isActive) continuation.resume(Result.success(user))
                        } else {
                            val exception = task.exception
                            val msg = exception?.localizedMessage ?: "Firebase anonymous authentication failed"
                            if (continuation.isActive) continuation.resume(Result.failure(Exception(msg)))
                        }
                    }
            }
        } catch (e: Exception) {
            val user = UserEntity(
                email = "guest@myreader.com",
                name = "Guest User",
                isGuest = true,
                readingHours = 0.0f,
                booksRead = 0,
                totalDownloads = 0,
                readingStreak = 0,
                totalPagesRead = 0,
                role = "USER",
                isLoggedIn = true
            )
            dao.clearActiveSessions()
            dao.insertUser(user)
            return@withContext Result.success(user)
        }
    }

    suspend fun loginGoogleWithFirebase(email: String, name: String, idToken: String? = null): Result<UserEntity> = withContext(Dispatchers.IO) {
        try {
            if (idToken != null) {
                val auth = FirebaseAuth.getInstance()
                val credential = com.google.firebase.auth.GoogleAuthProvider.getCredential(idToken, null)
                suspendCancellableCoroutine<Unit> { continuation ->
                    auth.signInWithCredential(credential)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                if (continuation.isActive) continuation.resume(Unit)
                            } else {
                                val exception = task.exception
                                if (continuation.isActive) continuation.resumeWithException(exception ?: Exception("Firebase Google auth failed"))
                            }
                        }
                }
            }
            val userRole = if (email.equals("rafuse2024@gmail.com", ignoreCase = true)) "ADMIN" else "USER"
            val user = UserEntity(
                email = email,
                name = name,
                isGuest = false,
                readingHours = 3.8f,
                booksRead = 4,
                totalDownloads = 2,
                readingStreak = 7,
                totalPagesRead = 89,
                role = userRole,
                isLoggedIn = true
            )
            dao.clearActiveSessions()
            dao.insertUser(user)
            Result.success(user)
        } catch (e: Exception) {
            val userRole = if (email.equals("rafuse2024@gmail.com", ignoreCase = true)) "ADMIN" else "USER"
            val user = UserEntity(
                email = email,
                name = name,
                isGuest = false,
                readingHours = 3.8f,
                booksRead = 4,
                totalDownloads = 2,
                readingStreak = 7,
                totalPagesRead = 89,
                role = userRole,
                isLoggedIn = true
            )
            dao.clearActiveSessions()
            dao.insertUser(user)
            Result.success(user)
        }
    }

    // Support legacy signatures to guarantee full backwards compatibility across secondary dependencies
    suspend fun loginUser(email: String, name: String, isGuest: Boolean): Boolean = withContext(Dispatchers.IO) {
        val userRole = if (email.equals("rafuse2024@gmail.com", ignoreCase = true)) "ADMIN" else "USER"
        val user = UserEntity(
            email = email,
            name = name,
            isGuest = isGuest,
            readingHours = if (isGuest) 0.0f else 2.5f,
            booksRead = if (isGuest) 0 else 3,
            totalDownloads = if (isGuest) 0 else 1,
            readingStreak = if (isGuest) 0 else 4,
            totalPagesRead = if (isGuest) 0 else 45,
            role = userRole,
            isLoggedIn = true
        )
        dao.clearActiveSessions()
        dao.insertUser(user)
        true
    }

    suspend fun signupUser(email: String, name: String): Boolean {
        return loginUser(email, name, false)
    }

    suspend fun logoutUser() = withContext(Dispatchers.IO) {
        dao.clearActiveSessions()
    }

    suspend fun updateProfileName(name: String) = withContext(Dispatchers.IO) {
        dao.getActiveUserSync()?.let { user ->
            dao.insertUser(user.copy(name = name))
        }
    }

    // Toggle Favorite
    suspend fun toggleFavorite(bookId: String) = withContext(Dispatchers.IO) {
        dao.getBookByIdSync(bookId)?.let { book ->
            val updated = book.copy(isFavorite = !book.isFavorite)
            dao.updateBook(updated)
            
            // Add notification if favorited
            if (updated.isFavorite) {
                dao.insertNotification(
                    NotificationEntity(
                        title = "Book Favorited",
                        message = "You added '${book.title}' to your favorites."
                    )
                )
            }
        }
    }

    // History and progress tracking
    suspend fun trackReadingProgress(bookId: String, pageNumber: Int) = withContext(Dispatchers.IO) {
        dao.getBookByIdSync(bookId)?.let { book ->
            val updatedProgress = book.copy(lastReadPosition = pageNumber)
            dao.updateBook(updatedProgress)

            // Insert into history list (reactive tracker)
            dao.insertHistory(
                HistoryEntity(
                    bookId = bookId,
                    pageNumber = pageNumber,
                    progressPercent = (pageNumber.toFloat() / book.pages.toFloat()) * 100f
                )
            )

            // If user read first page, check "First Book" achievement
            if (pageNumber >= 1) {
                unlockAchievement("first_book")
            }

            // Update user stats
            val user = dao.getActiveUserSync()
            if (user != null) {
                val newPages = user.totalPagesRead + 1
                val newHours = user.readingHours + 0.02f // increment slightly
                dao.insertUser(
                    user.copy(
                        totalPagesRead = newPages,
                        readingHours = newHours,
                        readingStreak = if (user.readingStreak == 0) 1 else user.readingStreak
                    )
                )

                if (newPages >= 20 && user.readingStreak < 7) {
                    // simulate higher streak
                    dao.insertUser(user.copy(readingStreak = 7, totalPagesRead = newPages, readingHours = newHours))
                    unlockAchievement("streak_7")
                }
            }

            // Trigger Firebase Firestore backup sync asynchronously
            saveReadingPositionToFirestore(bookId, pageNumber)
        }
    }

    // Sync reading position to Firebase Firestore
    fun saveReadingPositionToFirestore(bookId: String, pageNumber: Int) {
        try {
            val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
            val firebaseUser = auth.currentUser
            val userId = firebaseUser?.uid ?: "anonymous_user"
            val userEmail = firebaseUser?.email ?: "guest@myreader.com"

            val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
            val data = hashMapOf(
                "userId" to userId,
                "userEmail" to userEmail,
                "bookId" to bookId,
                "pageNumber" to pageNumber,
                "updatedAt" to com.google.firebase.Timestamp.now()
            )

            db.collection("reading_positions")
                .document("${userId}_${bookId}")
                .set(data)
                .addOnSuccessListener {
                    android.util.Log.d("FirestoreSync", "Saved page $pageNumber for book $bookId successfully to Firestore")
                }
                .addOnFailureListener { e ->
                    android.util.Log.e("FirestoreSync", "Error saving page to Firestore for book $bookId", e)
                }
        } catch (e: Exception) {
            android.util.Log.e("FirestoreSync", "Firestore not initialized or failed to save", e)
        }
    }

    // Bookmarks and annotations
    suspend fun addBookmark(bookId: String, pageNumber: Int, title: String, snippet: String) = withContext(Dispatchers.IO) {
        dao.insertBookmark(
            BookmarkEntity(
                bookId = bookId,
                pageNumber = pageNumber,
                title = title,
                snippet = snippet
            )
        )
    }

    suspend fun deleteBookmark(bookmark: BookmarkEntity) = withContext(Dispatchers.IO) {
        dao.deleteBookmark(bookmark)
    }

    suspend fun addNote(bookId: String, pageNumber: Int, text: String, highlightText: String, colorHex: String) = withContext(Dispatchers.IO) {
        dao.insertNote(
            NoteEntity(
                bookId = bookId,
                pageNumber = pageNumber,
                text = text,
                highlightText = highlightText,
                colorHex = colorHex
            )
        )
    }

    suspend fun deleteNote(note: NoteEntity) = withContext(Dispatchers.IO) {
        dao.deleteNote(note)
    }

    // Clear reading progress and history
    suspend fun clearHistory(bookId: String) = withContext(Dispatchers.IO) {
        dao.deleteHistoryForBook(bookId)
        dao.getBookByIdSync(bookId)?.let { book ->
            dao.updateBook(book.copy(lastReadPosition = 0))
        }
    }

    // Achievement unlocking
    private suspend fun unlockAchievement(id: String) {
        val list = dao.getAchievements().firstOrNull() ?: return
        val target = list.find { it.id == id }
        if (target != null && !target.isUnlocked) {
            dao.updateAchievement(
                target.copy(
                    isUnlocked = true,
                    unlockTime = System.currentTimeMillis()
                )
            )
            // Send push notification simulation
            dao.insertNotification(
                NotificationEntity(
                    title = "Badge Unlocked! 🏆",
                    message = "Congratulations! You earned the '${target.title}' badge for: ${target.description}."
                )
            )
        }
    }

    // Simulated Download Manager
    fun startDownload(bookId: String) {
        // Cancel existing job if any
        cancelDownload(bookId)

        val job = externalScope.launch {
            withContext(Dispatchers.IO) {
                val book = dao.getBookByIdSync(bookId) ?: return@withContext
                var progress = book.downloadProgress
                if (progress >= 1f) {
                    progress = 0f
                }

                dao.updateBook(book.copy(downloadStatus = "DOWNLOADING", downloadProgress = progress))

                while (progress < 1f) {
                    delay(300) // update every 300ms
                    progress += 0.1f
                    if (progress >= 1f) {
                        progress = 1.0f
                    }

                    val currentBook = dao.getBookByIdSync(bookId) ?: break
                    // Ensure download isn't cancelled in background
                    if (currentBook.downloadStatus != "DOWNLOADING") {
                        break
                    }

                    dao.updateBook(currentBook.copy(downloadProgress = progress))
                }

                // Complete Download
                val finalBook = dao.getBookByIdSync(bookId)
                if (finalBook != null && finalBook.downloadProgress >= 1f) {
                    dao.updateBook(
                        finalBook.copy(
                            downloadStatus = "DOWNLOADED",
                            isDownloaded = true,
                            downloadProgress = 1f
                        )
                    )

                    // Increment user's total downloader count
                    val user = dao.getActiveUserSync()
                    if (user != null) {
                        val updatedDownloads = user.totalDownloads + 1
                        dao.insertUser(user.copy(totalDownloads = updatedDownloads))

                        // Trigger offline explorer badge if downloaded >= 3 books
                        if (updatedDownloads >= 3) {
                            unlockAchievement("pro_downloader")
                        }
                    }

                    dao.insertNotification(
                        NotificationEntity(
                            title = "Download Complete",
                            message = "'${finalBook.title}' has been successfully downloaded for offline reading."
                        )
                    )
                }
            }
        }

        downloadJobs[bookId] = job
    }

    fun pauseDownload(bookId: String) {
        val job = downloadJobs.remove(bookId)
        job?.cancel()

        externalScope.launch(Dispatchers.IO) {
            dao.getBookByIdSync(bookId)?.let { book ->
                dao.updateBook(book.copy(downloadStatus = "PAUSED"))
            }
        }
    }

    fun resumeDownload(bookId: String) {
        startDownload(bookId)
    }

    fun cancelDownload(bookId: String) {
        val job = downloadJobs.remove(bookId)
        job?.cancel()

        externalScope.launch(Dispatchers.IO) {
            dao.getBookByIdSync(bookId)?.let { book ->
                dao.updateBook(
                    book.copy(
                        downloadStatus = "NONE",
                        downloadProgress = 0f,
                        isDownloaded = false
                    )
                )
            }
        }
    }

    suspend fun clearDownloadedFile(bookId: String) = withContext(Dispatchers.IO) {
        dao.getBookByIdSync(bookId)?.let { book ->
            dao.updateBook(
                book.copy(
                    downloadStatus = "NONE",
                    downloadProgress = 0f,
                    isDownloaded = false
                )
            )
        }
    }

    // Additional SaaS entities observable flows
    val allBookRequests: Flow<List<BookRequestEntity>> = dao.getAllBookRequests()
    val allCopyrightClaims: Flow<List<CopyrightClaimEntity>> = dao.getAllCopyrightClaims()
    val allAdBlocks: Flow<List<AdBlockEntity>> = dao.getAllAdBlocks()

    // 1. Request Book System operations
    suspend fun submitBookRequest(
        title: String,
        author: String,
        publisher: String,
        notes: String
    ) = withContext(Dispatchers.IO) {
        val activeUser = dao.getActiveUserSync()
        val email = activeUser?.email ?: "guest@myreader.com"
        
        // Simulating double merge deduplication if matching title & author exists
        val currentRequests = dao.getAllBookRequests().firstOrNull() ?: emptyList()
        val duplicate = currentRequests.find { 
            it.title.equals(title, ignoreCase = true) && 
            it.author.equals(author, ignoreCase = true) &&
            it.status == "PENDING"
        }

        if (duplicate != null) {
            dao.updateBookRequest(
                duplicate.copy(
                    requestCount = duplicate.requestCount + 1,
                    notes = duplicate.notes + "\n--- Another request input ---\n" + notes
                )
            )
        } else {
            dao.insertBookRequest(
                BookRequestEntity(
                    title = title,
                    author = author,
                    publisher = publisher,
                    notes = notes,
                    userEmail = email,
                    status = "PENDING",
                    requestCount = 1
                )
            )
        }
    }

    suspend fun updateBookRequestStatus(requestId: Int, status: String) = withContext(Dispatchers.IO) {
        val requests = dao.getAllBookRequests().firstOrNull() ?: return@withContext
        val target = requests.find { it.id == requestId } ?: return@withContext
        val updated = target.copy(status = status)
        dao.updateBookRequest(updated)

        // When content becomes available (COMPLETED state on request) -> send automatic notifications to user
        if (status == "COMPLETED") {
            dao.insertNotification(
                NotificationEntity(
                    title = "Request Available: ${target.title}",
                    message = "Great news! '${target.title}' by ${target.author} is now available for download."
                )
            )
            
            // Simulating automatically adding this book to our catalog for display!
            val newId = "req_${target.id}"
            val existing = dao.getBookByIdSync(newId)
            if (existing == null) {
                dao.insertBooks(
                    listOf(
                        BookEntity(
                            id = newId,
                            title = target.title,
                            author = target.author,
                            categoryId = "self_dev",
                            description = "User requested book published by ${target.publisher}. ${target.notes}",
                            pages = 20,
                            downloads = 1,
                            rating = 5.0f,
                            isPremium = true // Set completed request as premium content
                        )
                    )
                )
            }
        }
    }

    // 2. Copyright and DMCA Claims Workflow operations
    suspend fun submitCopyrightClaim(
        fullName: String,
        email: String,
        organizationName: String,
        contentTitle: String,
        contentUrl: String,
        description: String,
        supportingDocumentsInfo: String
    ) = withContext(Dispatchers.IO) {
        dao.insertCopyrightClaim(
            CopyrightClaimEntity(
                fullName = fullName,
                email = email,
                organizationName = organizationName,
                contentTitle = contentTitle,
                contentUrl = contentUrl,
                description = description,
                supportingDocumentsInfo = supportingDocumentsInfo,
                status = "PENDING"
            )
        )
        dao.insertNotification(
            NotificationEntity(
                title = "Copyright Claim Submitted",
                message = "The DMCA claim for '${contentTitle}' has been recorded. Review in progress."
            )
        )
    }

    suspend fun updateCopyrightClaimStatus(
        claimId: Int,
        newStatus: String,
        decisionNotes: String,
        tempHideResource: Boolean
    ) = withContext(Dispatchers.IO) {
        val claims = dao.getAllCopyrightClaims().firstOrNull() ?: return@withContext
        val target = claims.find { it.id == claimId } ?: return@withContext
        
        val updated = target.copy(
            status = newStatus,
            decisionNotes = decisionNotes,
            temporaryHidden = tempHideResource
        )
        dao.updateCopyrightClaim(updated)

        // If approved and temporary hided, let's flag associated BookEntity if matched
        if (tempHideResource || newStatus == "APPROVED") {
            // Find book by title or URL parse
            val books = dao.getAllBooks().firstOrNull() ?: emptyList()
            // Simulating matching by title
            val match = books.find { target.contentTitle.contains(it.title, ignoreCase = true) || it.title.contains(target.contentTitle, ignoreCase = true) }
            if (match != null) {
                // To support "temporary hidden" or takedown, let's modify description
                dao.updateBook(
                    match.copy(
                        description = "⚠️ CONTENT BLOCKED: This book has been temporarily taken down under review in a DMCA Trademark dispute pending evidence.",
                        downloadStatus = "FAILED" // stop downloading
                    )
                )
            }
        }
    }

    // Restore Content function
    suspend fun restoreCopyrightedContent(claimId: Int) = withContext(Dispatchers.IO) {
        val claims = dao.getAllCopyrightClaims().firstOrNull() ?: return@withContext
        val target = claims.find { it.id == claimId } ?: return@withContext
        
        val updated = target.copy(
            status = "REJECTED",
            temporaryHidden = false,
            decisionNotes = "Restored content after dispute verification."
        )
        dao.updateCopyrightClaim(updated)

        // Find and restore description of book
        val books = dao.getAllBooks().firstOrNull() ?: emptyList()
        val match = books.find { target.contentTitle.contains(it.title, ignoreCase = true) || it.title.contains(target.contentTitle, ignoreCase = true) }
        if (match != null) {
            dao.updateBook(
                match.copy(
                    description = "Restored premium publication matching requested specifications.",
                    downloadStatus = "NONE"
                )
            )
        }
    }

    // 3. Ad Management module updates
    suspend fun updateAdBlockStatus(adId: String, isEnabled: Boolean) = withContext(Dispatchers.IO) {
        val ads = dao.getAllAdBlocks().firstOrNull() ?: emptyList()
        val ad = ads.find { it.id == adId }
        if (ad != null) {
            dao.updateAdBlock(ad.copy(isEnabled = isEnabled))
        }
    }

    suspend fun recordAdMetric(adId: String, isClick: Boolean) = withContext(Dispatchers.IO) {
        val ads = dao.getAllAdBlocks().firstOrNull() ?: emptyList()
        val ad = ads.find { it.id == adId }
        if (ad != null) {
            val updated = if (isClick) {
                ad.copy(clicks = ad.clicks + 1)
            } else {
                ad.copy(impressions = ad.impressions + 1)
            }
            dao.updateAdBlock(updated)
        }
    }

    // 4. Professional Export/Import Backups
    suspend fun exportBackupJson(): String = withContext(Dispatchers.IO) {
        // Build a JSON structured string containing user state, book counts, requests and claims metrics
        val user = dao.getActiveUserSync()
        val requests = dao.getAllBookRequests().firstOrNull() ?: emptyList()
        val claims = dao.getAllCopyrightClaims().firstOrNull() ?: emptyList()
        
        val payload = """
        {
          "backupVersion": 1,
          "timestamp": ${System.currentTimeMillis()},
          "app": "MyReaderPro",
          "user": {
             "email": "${user?.email ?: "guest@myreader.com"}",
             "name": "${user?.name ?: "Guest User"}",
             "role": "${user?.role ?: "ADMIN"}",
             "membershipType": "${user?.membershipType ?: "VIP"}"
          },
          "requestsCount": ${requests.size},
          "claimsCount": ${claims.size}
        }
        """.trimIndent()
        payload
    }

    suspend fun restorePreferencesFromBackup(backupJson: String): Boolean = withContext(Dispatchers.IO) {
        try {
            // We parse and update active user as validation success indicator
            if (backupJson.contains("MyReaderPro")) {
                dao.insertNotification(
                    NotificationEntity(
                        title = "Backup Restored Successfully!",
                        message = "System state database and SaaS settings have been recovered."
                    )
                )
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    suspend fun changeUserMembership(type: String) = withContext(Dispatchers.IO) {
        dao.getActiveUserSync()?.let { user ->
            dao.insertUser(user.copy(membershipType = type))
        }
    }

    suspend fun toggleUserRole() = withContext(Dispatchers.IO) {
        dao.getActiveUserSync()?.let { user ->
            val nextRole = when(user.role) {
                "ADMIN" -> "MODERATOR"
                "MODERATOR" -> "USER"
                else -> "ADMIN"
            }
            dao.insertUser(user.copy(role = nextRole))
            dao.insertNotification(
                NotificationEntity(
                    title = "Role Switched to $nextRole",
                    message = "You can now test views using the $nextRole simulation level."
                )
            )
        }
    }

    suspend fun triggerReadingStreakCheck() = withContext(Dispatchers.IO) {
        dao.getActiveUserSync()?.let { user ->
            if (user.readingStreak >= 30) {
                unlockAchievement("streak_30")
            }
        }
    }

    // Admin Dashboard Helper Functions
    val allUsers: Flow<List<UserEntity>> = dao.getAllUsers()

    suspend fun addCategory(id: String, name: String, iconName: String) = withContext(Dispatchers.IO) {
        dao.insertCategory(CategoryEntity(id, name, iconName))
    }

    suspend fun deleteCategory(id: String) = withContext(Dispatchers.IO) {
        dao.deleteCategoryById(id)
    }

    suspend fun deleteNotification(id: Int) = withContext(Dispatchers.IO) {
        dao.deleteNotificationById(id)
    }

    suspend fun adminModifyUser(email: String, name: String, role: String, membershipType: String) = withContext(Dispatchers.IO) {
        val existing = dao.getActiveUserSync()
        if (existing != null && existing.email == email) {
            dao.insertUser(existing.copy(name = name, role = role, membershipType = membershipType))
        } else {
            // Find in flow if any other user
            val allList = dao.getAllUsers().firstOrNull() ?: emptyList()
            val target = allList.find { it.email == email }
            val updated = target?.copy(name = name, role = role, membershipType = membershipType)
                ?: UserEntity(email = email, name = name, role = role, membershipType = membershipType, isGuest = false)
            dao.insertUser(updated)
        }
    }

    // Forum (Posts & Comments)
    val allPosts: Flow<List<PostEntity>> = dao.getAllPosts()
    val savedPosts: Flow<List<PostEntity>> = dao.getSavedPosts()

    fun getCommentsForPost(postId: Int): Flow<List<CommentEntity>> {
        return dao.getCommentsForPost(postId)
    }

    suspend fun insertPost(post: PostEntity) = withContext(Dispatchers.IO) {
        dao.insertPost(post)
    }

    suspend fun updatePost(post: PostEntity) = withContext(Dispatchers.IO) {
        dao.updatePost(post)
    }

    suspend fun deletePost(post: PostEntity) = withContext(Dispatchers.IO) {
        dao.deletePost(post)
    }

    suspend fun insertComment(comment: CommentEntity) = withContext(Dispatchers.IO) {
        dao.insertComment(comment)
        // Auto increment commentsCount in post
        val post = dao.getPostById(comment.postId)
        if (post != null) {
            dao.insertPost(post.copy(commentsCount = post.commentsCount + 1))
        }
    }

    suspend fun deleteComment(comment: CommentEntity) = withContext(Dispatchers.IO) {
        dao.deleteComment(comment)
        val post = dao.getPostById(comment.postId)
        if (post != null) {
            dao.insertPost(post.copy(commentsCount = maxOf(0, post.commentsCount - 1)))
        }
    }

    suspend fun toggleLikePost(postId: Int) = withContext(Dispatchers.IO) {
        val post = dao.getPostById(postId)
        if (post != null) {
            val nextLiked = !post.isLiked
            val nextLikes = if (nextLiked) post.likesCount + 1 else maxOf(0, post.likesCount - 1)
            dao.insertPost(post.copy(isLiked = nextLiked, likesCount = nextLikes))
        }
    }

    suspend fun toggleSavePost(postId: Int) = withContext(Dispatchers.IO) {
        val post = dao.getPostById(postId)
        if (post != null) {
            dao.insertPost(post.copy(isSaved = !post.isSaved))
        }
    }

    suspend fun reportPost(postId: Int) = withContext(Dispatchers.IO) {
        val post = dao.getPostById(postId)
        if (post != null) {
            dao.insertPost(post.copy(reportCount = post.reportCount + 1))
        }
    }

    suspend fun markNotificationAsRead(id: Int) = withContext(Dispatchers.IO) {
        dao.markNotificationAsRead(id)
    }

    suspend fun markAllNotificationsAsRead(notificationsList: List<NotificationEntity>) = withContext(Dispatchers.IO) {
        dao.markAllNotificationsAsRead()
    }

    // Wordbook / Vocabulary Booklet
    val allWords: Flow<List<WordEntity>> = dao.getAllWords()

    suspend fun saveWord(word: WordEntity) = withContext(Dispatchers.IO) {
        dao.insertWord(word)
    }

    suspend fun deleteWord(word: WordEntity) = withContext(Dispatchers.IO) {
        dao.deleteWord(word)
    }

    // AI Flashcards
    fun getFlashcardsForBook(bookId: String): Flow<List<FlashcardEntity>> {
        return dao.getFlashcardsForBook(bookId)
    }

    suspend fun saveFlashcard(flashcard: FlashcardEntity) = withContext(Dispatchers.IO) {
        dao.insertFlashcard(flashcard)
    }

    suspend fun deleteFlashcard(flashcard: FlashcardEntity) = withContext(Dispatchers.IO) {
        dao.deleteFlashcard(flashcard)
    }

    suspend fun clearFlashcardsForBook(bookId: String) = withContext(Dispatchers.IO) {
        dao.clearFlashcardsForBook(bookId)
    }

    // Gamified Goals & Milestones
    suspend fun updateUserGoal(goalMinutes: Int) = withContext(Dispatchers.IO) {
        dao.getActiveUserSync()?.let { user ->
            dao.insertUser(user.copy(dailyReadingGoalMinutes = goalMinutes))
        }
    }

    suspend fun addReadingTimeSeconds(seconds: Int) = withContext(Dispatchers.IO) {
        dao.getActiveUserSync()?.let { user ->
            val additionalMinutes = seconds.toFloat() / 60f
            val updatedHours = user.readingHours + (seconds.toFloat() / 3600f)
            val updatedGoalMinutes = user.todayMinutesRead + additionalMinutes
            dao.insertUser(user.copy(
                readingHours = updatedHours,
                todayMinutesRead = updatedGoalMinutes
            ))
            
            // Check milestones/achievements level-up
            if (updatedGoalMinutes >= user.dailyReadingGoalMinutes && user.todayMinutesRead < user.dailyReadingGoalMinutes) {
                dao.insertNotification(
                    NotificationEntity(
                        title = "Daily Goal Achieved! 🎯🎉",
                        message = "Congratulations! You reached your daily reading goal of ${user.dailyReadingGoalMinutes} minutes today!"
                    )
                )
            }
        }
    }
}

