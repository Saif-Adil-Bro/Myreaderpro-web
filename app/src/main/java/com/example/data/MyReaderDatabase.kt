package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        UserEntity::class,
        CategoryEntity::class,
        BookEntity::class,
        BookmarkEntity::class,
        NoteEntity::class,
        HistoryEntity::class,
        AchievementEntity::class,
        NotificationEntity::class,
        BookRequestEntity::class,
        CopyrightClaimEntity::class,
        AdBlockEntity::class,
        PostEntity::class,
        CommentEntity::class,
        WordEntity::class,
        FlashcardEntity::class
    ],
    version = 6,
    exportSchema = false
)
abstract class MyReaderDatabase : RoomDatabase() {
    abstract fun readerDao(): ReaderDao

    companion object {
        @Volatile
        private var INSTANCE: MyReaderDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): MyReaderDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MyReaderDatabase::class.java,
                    "my_reader_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }

        suspend fun populateInitialData(dao: ReaderDao) {
            // Default user
            dao.insertUser(
                UserEntity(
                    email = "guest@myreader.com",
                    name = "Guest User",
                    profilePicture = "avatar_default",
                    isGuest = true,
                    readingHours = 0.0f,
                    booksRead = 0,
                    totalDownloads = 0,
                    readingStreak = 0,
                    totalPagesRead = 0,
                    role = "USER"
                )
            )

            // Setup Admin Email Account
            dao.insertUser(
                UserEntity(
                    email = "rafuse2024@gmail.com",
                    name = "Official Admin",
                    profilePicture = "avatar_default",
                    isGuest = false,
                    readingHours = 12.5f,
                    booksRead = 8,
                    totalDownloads = 4,
                    readingStreak = 15,
                    totalPagesRead = 512,
                    membershipType = "VIP",
                    isSupporter = true,
                    role = "ADMIN"
                )
            )

            // Categories
            val categories = listOf(
                CategoryEntity("islamic", "Islamic", "mosque"),
                CategoryEntity("quran_tafsir", "Quran & Tafsir", "book_quran"),
                CategoryEntity("hadith", "Hadith", "library"),
                CategoryEntity("fiqh", "Fiqh", "gavel"),
                CategoryEntity("novel", "Novel", "menu_book"),
                CategoryEntity("science", "Science", "science"),
                CategoryEntity("history", "History", "history"),
                CategoryEntity("tech", "Technology", "developer_board"),
                CategoryEntity("bio", "Biography", "person"),
                CategoryEntity("self_dev", "Self Development", "trending_up"),
                CategoryEntity("kids", "Kids", "child_care")
            )
            dao.insertCategories(categories)

            // Achievements
            val achievements = listOf(
                AchievementEntity("first_book", "First Book", "Open and read your first book content", false, "ic_book"),
                AchievementEntity("streak_7", "7 Day Streak", "Maintain reading streak for 7 consecutive days", false, "ic_streak"),
                AchievementEntity("streak_30", "30 Day Streak", "Maintain reading streak for 30 consecutive days", false, "ic_streak_30"),
                AchievementEntity("read_100", "Top Reader", "Complete reading 100 books globally", false, "ic_top_reader"),
                AchievementEntity("pro_downloader", "Offline Explorer", "Download 3 or more books for offline reading", false, "ic_download")
            )
            dao.insertAchievements(achievements)

            // Books
            val dummyContentGeneral = """
                # Chapter 1: The New Horizon
                The light of the screen illuminated his face as the rain tapped gently on the windowpane. It was a cold Tuesday morning in June 2026, and the world seemed quiet, wrapped in a peaceful blanket of gray mist. 
                
                For readers, a book is not merely a collection of words on a page or bytes in a digital file. It is a doorway to another consciousness, a portal across time and space. Whether delving into the profound depth of Islamic texts, the beautiful complexity of Hadith literature, the analytical precision of science, the dramatic turns of modern novels, or the step-by-step challenges of technology, each scroll brings discovery.
                
                In this volume, we explore how human knowledge builds upon ancient foundations to reach for the stars. We will look at both classical philosophical frameworks and modern engineering breakthroughs.
                
                *Key Ideas in this Chapter:*
                1. Knowledge is progressive yet relies on historic pillars.
                2. Continuous focus is the ultimate engine of accomplishment.
                3. The digital age expands access to wisdom exponentially.
                
                # Chapter 2: The Path of Discipline
                Discipline is often misunderstood as restriction. True discipline is liberation. When you focus your energy on a specific study, whether it is Hadith narrative analysis or compiling complex software structures, you streamline your path toward mastery.
                
                Consider the classic scholars who traveled hundreds of miles on camelback simply to verify a single report. Their commitment holds a mirror to our modern convenience, where an entire library fits neatly in the palm of our hands. 
                
                # Chapter 3: Designing Your Daily Routine
                How much do you read? A mere fifteen minutes a day accumulates to dozens of books in a single calendar year. By setting aside specific blocks under consistent environments, your brain develops a physical reading reflex. 
                
                *Our Recommended Checklist:*
                - Switch your app to Sepia mode in low-light environments.
                - Double-tap highlighted passages to add a personalized note.
                - Track your reading streak using your profile tab metrics.
                
                # Chapter 4: The Core Principles of Development
                When creating apps or tools, the core principle is the preservation of simplicity. Every line of code should have a purpose. Similarly, every chapter in a book should propel your mind further.
            """.trimIndent()

            val dummyContentQuran = """
                # Chapter 1: Introduction to Quranic Sciences
                The Quran is the foundational scripture of Islam, revered by Muslims as the precise, literal word of God revealed to the Prophet Muhammad (peace be upon him). Tafsir refers to the systematic explanation, exegesis, and contextual analysis of these verses.
                
                To truly understand Tafsir, one must look at:
                1. *Asbab al-Nuzul* (The historical contexts and occasions of revelation).
                2. Linguistic nuances in classical Arabic poetry and dialogue.
                3. Intertextual references where different verses explain one another.
                
                # Chapter 2: Tafsir Methodology
                Classical scholars, such as Ibn Kathir, Al-Tabari, and Al-Qurtubi, established rigorous methodological frameworks to prevent subjective interpretation. 
                
                - *Tafsir al-Ma'thur*: Interpretation based on transmission (the Quran itself, Hadith, of companions).
                - *Tafsir al-Ray*: Analytical interpretation using language and grammatical analysis.
                
                # Chapter 3: The Verse of Light (Ayat al-Nur)
                Among the most beautiful and highly analyzed sections of the Quran is Ayat al-Nur. It presents a profound parable of light within light, a glass lamp shining like a brilliant star, fueled by the oil of a blessed olive tree.
            """.trimIndent()

            val dummyContentHadith = """
                # Chapter 1: Understanding Isnad and Matn
                Hadith, representing the recorded speech, actions, and approvals of the Prophet Muhammad, is preserved through a dual structure:
                1. *Isnad*: The chain of narrators who transmitted the report.
                2. *Matn*: The actual text or substance of the saying.
                
                # Chapter 2: The Classifications of Authenticity
                To assess if a report is genuine, scholars developed an advanced science of biography (*Ilm al-Rijal*) and textual verification:
                
                - *Sahih* (Rigorously Authentic): Flawless chain of trustworthy narrators with perfect memory, free from hidden defects.
                - *Hasan* (Good/Fair): Trustworthy chain but with minor memory deviations.
                - *Da'if* (Weak): Significant gaps or unverified narrators in the chain.
                
                # Chapter 3: Wisdom and Character
                Many Hadiths emphasize moral excellence. For instance, the famous teaching: "Actions are judged by intentions." This highlights that internal character and moral direction form the backbone of all human efforts.
            """.trimIndent()

            val books = listOf(
                BookEntity(
                    id = "b1",
                    title = "The Prophet's Character",
                    author = "Ibn Al-Qayyim",
                    categoryId = "islamic",
                    description = "A deep exploration of the moral authority, character, humility, and daily habits of the Prophet Muhammad (PBUH) drawing from classical authentic resources.",
                    pages = 18,
                    fileSize = "1.8 MB",
                    language = "English",
                    rating = 4.9f,
                    downloads = 4850,
                    isFeatured = true,
                    contentMarkdown = dummyContentHadith,
                    fileFormat = "EPUB"
                ),
                BookEntity(
                    id = "b2",
                    title = "Introduction to Quranic Tafsir",
                    author = "Ibn Kathir (Condensed)",
                    categoryId = "quran_tafsir",
                    description = "The condensated classical tafsir providing students of knowledge with contexts of major verses, linguistic breakdowns, and historical contexts.",
                    pages = 24,
                    fileSize = "3.2 MB",
                    language = "English",
                    rating = 4.9f,
                    downloads = 9320,
                    isFeatured = true,
                    contentMarkdown = dummyContentQuran,
                    fileFormat = "PDF"
                ),
                BookEntity(
                    id = "b3",
                    title = "Forty Hadith Commentary",
                    author = "Imam Al-Nawawi",
                    categoryId = "hadith",
                    description = "The classic compilation of forty fundamental sayings of the Prophet, outlining the complete core of Islamic jurisprudence, ethics, and theology.",
                    pages = 40,
                    fileSize = "2.1 MB",
                    language = "English",
                    rating = 5.0f,
                    downloads = 15400,
                    isFeatured = false,
                    contentMarkdown = dummyContentHadith,
                    fileFormat = "ARTICLE"
                ),
                BookEntity(
                    id = "b4",
                    title = "The Cosmic Echo",
                    author = "Arthur C. Clarke",
                    categoryId = "novel",
                    description = "An grand science-fiction expedition to the outer edges of the solar system, where astronauts discover a dormant crystalline structure sending signals into deep space.",
                    pages = 45,
                    fileSize = "1.5 MB",
                    language = "English",
                    rating = 4.6f,
                    downloads = 6320,
                    isFeatured = false,
                    contentMarkdown = dummyContentGeneral,
                    fileFormat = "EPUB"
                ),
                BookEntity(
                    id = "b5",
                    title = "The Golden Age: Al-Biruni biography",
                    author = "Dr. Ahmed Mansour",
                    categoryId = "bio",
                    description = "The life, scientific discoveries, astronomical instruments, and vast travels of Al-Biruni, one of history's greatest polymaths and geographers.",
                    pages = 32,
                    fileSize = "2.7 MB",
                    language = "English",
                    rating = 4.7f,
                    downloads = 2100,
                    isFeatured = false,
                    contentMarkdown = dummyContentGeneral,
                    fileFormat = "PDF"
                ),
                BookEntity(
                    id = "b6",
                    title = "The Art of Focus & Devotion",
                    author = "Al-Ghazali",
                    categoryId = "self_dev",
                    description = "A philosophical manual on training the soul, maintaining cognitive discipline, and achieving continuous spiritual presence in an age of continuous distractions.",
                    pages = 15,
                    fileSize = "12 MB",
                    language = "English",
                    rating = 4.9f,
                    downloads = 12500,
                    isFeatured = true,
                    contentMarkdown = dummyContentGeneral,
                    fileFormat = "ARTICLE"
                ),
                BookEntity(
                    id = "b7",
                    title = "Building Clean Apps with Android",
                    author = "JetBrains Academy",
                    categoryId = "tech",
                    description = "A modern step-by-step programming textbook on Jetpack Compose, state flow model, clean room databases, and testing parameters for Kotlin developer.",
                    pages = 55,
                    fileSize = "4.5 MB",
                    language = "English",
                    rating = 4.8f,
                    downloads = 8900,
                    isFeatured = false,
                    contentMarkdown = dummyContentGeneral,
                    fileFormat = "EPUB"
                ),
                BookEntity(
                    id = "b8",
                    title = "Advanced Universe and Astronomy",
                    author = "Carl Sagan",
                    categoryId = "science",
                    description = "Journey into space and physics. Understanding cosmic background radiation, relative time mechanics, and the search for extraterrestrial organic markers.",
                    pages = 38,
                    fileSize = "3.8 MB",
                    language = "English",
                    rating = 4.7f,
                    downloads = 7600,
                    isFeatured = false,
                    contentMarkdown = dummyContentGeneral,
                    fileFormat = "PDF"
                )
            )
            dao.insertBooks(books)

            // Prompt notifications
            dao.insertNotification(
                NotificationEntity(
                    title = "Welcome to MyReaderPro!",
                    message = "Explore our premium e-book collection with MyReaderPro. Enjoy advanced offline features!"
                )
            )
            dao.insertNotification(
                NotificationEntity(
                    title = "Featured Book Added",
                    message = "Check out 'The Prophet's Character' by Ibn Al-Qayyim with full SEO optimizations!"
                )
            )

            // Initial Book Requests
            dao.insertBookRequest(
                BookRequestEntity(
                    title = "The Alchemist",
                    author = "Paulo Coelho",
                    publisher = "HarperOne",
                    notes = "An incredible journey of self-discovery that is highly requested by readers.",
                    status = "PENDING",
                    requestCount = 12
                )
            )
            dao.insertBookRequest(
                BookRequestEntity(
                    title = "Clean Code",
                    author = "Robert C. Martin",
                    publisher = "Prentice Hall",
                    notes = "The ultimate guide to software craftsmanship.",
                    status = "COMPLETED",
                    requestCount = 28
                )
            )
            dao.insertBookRequest(
                BookRequestEntity(
                    title = "No Rules Rules",
                    author = "Reed Hastings",
                    publisher = "Kepler Press",
                    notes = "Netflix culture book.",
                    status = "REJECTED",
                    requestCount = 3
                )
            )

            // Initial Copyright Claims
            dao.insertCopyrightClaim(
                CopyrightClaimEntity(
                    fullName = "Johnathan Harper",
                    email = "jharper@harperrights.com",
                    organizationName = "HarperRights LLC",
                    contentTitle = "The Alchemist (Pirated Edition)",
                    contentUrl = "https://myreaderpro.com/books/b4_pirated",
                    description = "We hold exclusive rights to this book. Please take down the unauthorized chapter extracts immediately.",
                    status = "PENDING"
                )
            )

            // Initial Ad Blocks
            dao.insertAdBlock(
                AdBlockEntity(
                    id = "banner_home",
                    title = "SaaS Library Pro Premium Upgrade Banner",
                    adType = "BANNER",
                    isEnabled = true,
                    imageUrl = "promo_banner_upgrade",
                    clicks = 42,
                    impressions = 1530
                )
            )
            dao.insertAdBlock(
                AdBlockEntity(
                    id = "interstitial_read",
                    title = "Premium Subscription Interstitial Ad",
                    adType = "INTERSTITIAL",
                    isEnabled = true,
                    imageUrl = "banner_monetization_interstitial",
                    clicks = 18,
                    impressions = 412
                )
            )

            // Seed Forum Posts
            dao.insertPost(
                PostEntity(
                    id = 1,
                    title = "Welcome to MyReaderPro Social Hub! 🚀",
                    content = "Hey everyone! Welcome to our new integrated community space. Here you can write posts, ask questions, exchange notes, share tips, and discuss your reading journeys with other book enthusiasts worldwide. Explore other sections or create your very first book discussion directly inside the forum tab. Happy reading!",
                    type = "ANNOUNCEMENT",
                    authorName = "MyReader Admin Team",
                    authorEmail = "admin@myreaderpro.com",
                    authorAvatar = "avatar_1",
                    imageUrl = "",
                    bookTitleRef = "",
                    likesCount = 24,
                    commentsCount = 2,
                    timestamp = System.currentTimeMillis() - 7200000,
                    isLiked = true,
                    isSaved = false
                )
            )

            dao.insertPost(
                PostEntity(
                    id = 2,
                    title = "Tips on building a consistent morning reading habit 🌅",
                    content = "Consistency is much more powerful than volume. Reading just 10 pages over morning coffee adds up to more than 3,600 pages a year—that's roughly 12 full-sized books! Here are three fast tips: 1. Keep your current read next to your coffee maker, 2. Put your phone in another room until you read those 10 pages, 3. Focus on a high-interest genre to begin with.",
                    type = "TIPS",
                    authorName = "Sofia Rahman",
                    authorEmail = "sofia.r@gmail.com",
                    authorAvatar = "avatar_default",
                    imageUrl = "",
                    bookTitleRef = "",
                    likesCount = 18,
                    commentsCount = 1,
                    timestamp = System.currentTimeMillis() - 3600000,
                    isLiked = false,
                    isSaved = true
                )
            )

            dao.insertPost(
                PostEntity(
                    id = 3,
                    title = "Cosmos by Carl Sagan: Absolute Masterpiece or Too Dated?",
                    content = "Just started reading Carl Sagan's 'Cosmos' in MyReader. I'm absolutely blown away by his prose. It reads almost like poetry. He manages to connect history, physics, biological evolution, and chemistry in a singular narrative. Some scientific findings are updated now, but the philosophical impact remains completely untouched. What are your thoughts on chapter 2, 'One Voice in the Cosmic Fugue'?",
                    type = "DISCUSSION",
                    authorName = "Dr. Imran Karim",
                    authorEmail = "imran.kd@yahoo.com",
                    authorAvatar = "avatar_2",
                    imageUrl = "",
                    bookTitleRef = "Cosmos",
                    likesCount = 35,
                    commentsCount = 3,
                    timestamp = System.currentTimeMillis() - 1800000,
                    isLiked = false,
                    isSaved = false
                )
            )

            // Seed Comments for Post 3
            dao.insertComment(
                CommentEntity(
                    id = 1,
                    postId = 3,
                    authorName = "Nabil Chowdhury",
                    authorEmail = "nabil.chow@gmail.com",
                    authorAvatar = "avatar_3",
                    text = "I absolutely agree! Sagan's call for cosmic humility and perspective is something that never goes out of style. It transcends decades.",
                    timestamp = System.currentTimeMillis() - 1200000
                )
            )
            dao.insertComment(
                CommentEntity(
                    id = 2,
                    postId = 3,
                    authorName = "Aisha Khan",
                    authorEmail = "aisha.read@gmail.com",
                    authorAvatar = "avatar_4",
                    text = "Yes, Chapter 2 is spectacular. His explanation of artificial selection via Heike crabs is one of my favorite science explanations ever written!",
                    timestamp = System.currentTimeMillis() - 900000
                )
            )
            dao.insertComment(
                CommentEntity(
                    id = 3,
                    postId = 3,
                    authorName = "Sofia Rahman",
                    authorEmail = "sofia.r@gmail.com",
                    authorAvatar = "avatar_default",
                    text = "It's what inspired me to study astrobiology! I read it when I was in high school and it changed my life path completely.",
                    timestamp = System.currentTimeMillis() - 600000
                )
            )

            // Seed Comment for Post 1
            dao.insertComment(
                CommentEntity(
                    id = 4,
                    postId = 1,
                    authorName = "Dr. Imran Karim",
                    authorEmail = "imran.kd@yahoo.com",
                    authorAvatar = "avatar_2",
                    text = "This is brilliant! Having the community forum right inside our daily e-reader app is exactly what my book club was looking for.",
                    timestamp = System.currentTimeMillis() - 500000
                )
            )
            dao.insertComment(
                CommentEntity(
                    id = 5,
                    postId = 1,
                    authorName = "Aisha Khan",
                    authorEmail = "aisha.read@gmail.com",
                    authorAvatar = "avatar_4",
                    text = "Love the design and layout of the posts! Super clean and intuitive.",
                    timestamp = System.currentTimeMillis() - 300000
                )
            )
        }
    }
}
