package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

// Data Models
data class Book(
    val id: String,
    val title: String,
    val category: String,
    val author: String,
    val totalPages: Int,
    var currentPage: MutableState<Int>,
    val description: String = "",
    val shortWord: String = ""
)

data class Article(
    val id: String,
    val title: String,
    val readTime: String,
    val author: String,
    val categoryPrefix: String,
    val content: String,
    val isBookmarked: MutableState<Boolean> = mutableStateOf(false)
)

enum class LibraryTab(val icon: ImageVector, val selectedIcon: ImageVector, val title: String, val titleBangla: String) {
    HOME(Icons.Outlined.Home, Icons.Filled.Home, "Home", "হোম"),
    LIBRARY(Icons.Outlined.MenuBook, Icons.Filled.MenuBook, "Library", "লাইব্রেরি"),
    SAVED(Icons.Outlined.BookmarkBorder, Icons.Filled.Bookmark, "Saved", "সংগৃহীত"),
    PROFILE(Icons.Outlined.Person, Icons.Filled.Person, "Profile", "প্রোফাইল")
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainAppScreen()
            }
        }
    }
}

@Composable
fun MainAppScreen() {
    // Application State
    var currentTab by remember { mutableStateOf(LibraryTab.HOME) }
    var searchQuery by remember { mutableStateOf("") }
    
    // Books State
    val bakaBook = remember {
        Book(
            id = "1",
            title = "সূরা আল-বাকারাহ",
            category = "তাফসীর",
            author = "ইবনে কাসীর",
            totalPages = 1200,
            currentPage = mutableStateOf(234),
            description = "তাফসীর ইবনে কাসীর হলো আল-কুরআনের অন্যতম জনপ্রিয় ও নির্ভেজাল তাফসীর গ্রন্থ। হাফেয ইবনে কাসীর (রহ) কর্তৃক রচিত এই সুবিশাল গ্রন্থে প্রতিটি আয়াতের সহজ-সরল ও বিশুদ্ধ ব্যাখ্যা প্রদান করা হয়েছে। সূরা বাকারাহ হলো কুরআনের দীর্ঘতম সূরা, যাতে মানুষের হেদায়েত, পারিবারিক আইন ও বিভিন্ন ঐতিহাসিক ঘটনার দীর্ঘ বর্ণনা রয়েছে। পৃষ্ঠা ২৪০ পর্যন্ত পড়াশোনা এগিয়ে নিয়ে যান এবং ইসলামের সঠিক জ্ঞান অর্জন করুন।",
            shortWord = "তা"
        )
    }
    
    val allBooks = remember {
        mutableStateListOf(
            bakaBook,
            Book("2", "সহীহ বুখারী - ১ম খণ্ড", "হাদীস", "ইমাম বুখারী", 450, mutableStateOf(45), "ইসলামী জ্ঞানের অন্যতম খাঁটি উৎস হলো সহীহ বুখারী শরীফ। শয্যাবস্থা, তিলাওয়াত ও আমলের ক্ষেত্রে এটি সর্বোত্তম গাইডবুক।", "বু"),
            Book("3", "আকীদাহ তাহাবীয়াহ", "আকীদা", "ইমাম তাহাবী", 150, mutableStateOf(10), "আহলুস সুন্নাহ ওয়াল জামায়াতের বিশুদ্ধ আকীদার সারসংক্ষেপ সম্বলিত বিশ্বখ্যাত ও সর্বজনগ্রাহ্য কিতাব।", "আ"),
            Book("4", "রিয়াদুস সালেহীন", "হাদীস", "ইমাম নববী", 480, mutableStateOf(112), "দৈনন্দিন আমল এবং নীতিশিক্ষা বিষয়ক হাদীস সমূহের অনন্য সঙ্কলন।", "রি")
        )
    }

    // Bookmarks state (for books, using simple mutable list/state)
    val bookmarkedBookIds = remember { mutableStateListOf<String>() }

    // Articles State
    val allArticles = remember {
        mutableStateListOf(
            Article(
                id = "art_1",
                title = "আদর্শ পরিবার গঠন",
                readTime = "৫ মিনিট পাঠ",
                author = "শায়খ ড. সালেহ",
                categoryPrefix = "আ",
                content = "একটি আদর্শ পরিবার গঠনে ইসলামী শরিয়তের নির্দেশনা এবং নৈতিক শিক্ষার গুরুত্ব অপরিসীম। সুশৃঙ্খল সমাজ গড়তে পরিবারের প্রত্যেক সদস্যের স্ব-স্ব দায়িত্ব সুন্দরভাবে পালন করা উচিত। পিতা-মাতার কর্তব্য সন্তানকে প্রথম থেকেই ধর্মের বুনিয়াদী শিক্ষা দেওয়া ও সঠিক নৈতিক মূল্যবোধ লালন করা। পরিবারের পারস্পরিক শ্রদ্ধা, মমতা ও আল্লাহর প্রতি আনুগত্যই শান্তি ও সৌভাগ্যের একমাত্র চাবিকাঠি।"
            ),
            Article(
                id = "art_2",
                title = "নামাযের গুরুত্ব ও ফজিলত",
                readTime = "৮ মিনিট পাঠ",
                author = "ইমাম নববী",
                categoryPrefix = "ন",
                content = "নামায মুমিনের মিরাজ স্বরূপ এবং এটি ইসলামের দ্বিতীয় সর্বাধিক গুরুত্বপূর্ণ স্তম্ভ। নিয়মানুযায়ী নামায আদায় মানুষকে সকল মন্দ কাজ ও অন্যায়-অশ্লীলতা থেকে দূরে রাখে। পাঁচ ওয়াক্ত নামায অন্তরে আল্লাহর ভীতি জাগ্রত করে এবং আল্লাহর সাথে আত্মিক সংযোগ স্থাপন করে। হাশরের ময়দানে সর্বপ্রথম মানুষ তার নামাযের হিসাব দেবে। তাই শত ব্যস্ততার মাঝেও জামায়াতের সাথে নামায আদায়ের অভ্যাস দীর্ঘ করতে হবে।"
            ),
            Article(
                id = "art_3",
                title = "কুরআন তিলাওয়াতের আদব",
                readTime = "৬ মিনিট পাঠ",
                author = "ইমাম সুয়ূতী",
                categoryPrefix = "কু",
                content = "কুরআন পাঠের সময় কিছু আদব বা পরিমিতি রক্ষা করা প্রত্যেক পাঠকের জন্য কল্যাণকর। তিলাওয়াত আরম্ভ করার পূর্বে অজু করা, পরিষ্কার-পরিচ্ছন্ন পোশাকে বসা ও কিবলামুখী হওয়া সুন্নত। তিলাওয়াত শুরু করার সময় আউযুবিল্লাহ-বিসমিল্লাহ পাঠ পূর্বক অন্তরে আল্লাহর স্মরণ আনতে হবে। তাড়াহুড়ো না করে ধীরস্থিরভাবে অর্থ বুঝে পড়ার চেষ্টা করলে তিলাওয়াতের আসল প্রবৃদ্ধি ও রূহানিয়াত লাভ সম্ভব হয়।"
            ),
            Article(
                id = "art_4",
                title = "রমজানের পবিত্রতা রক্ষা",
                readTime = "৭ মিনিট পাঠ",
                author = "শায়খ আল-উসাইমীন",
                categoryPrefix = "র",
                content = "রমজান আত্মশুদ্ধি এবং আল্লাহর নৈকট্য লাভের শ্রেষ্ঠ মাস। এ মাসে খাবার-দাবার পরিহারের পাশাপাশি সমস্ত খারাপ কাজ, পরনিন্দা ও মিথ্যা বর্জন করা আবশ্যক। রমজানের পবিত্রতা রক্ষায় যথাসম্ভব অধিক নফল ইবাদত, কুরআন অধ্যায়ন ও দরিদ্রদের সাহায্য-সহযোগিতা করা মুসলিম উম্মাহর প্রধান জিম্মাদারি।"
            )
        )
    }

    // Detail Sheet States
    var selectedBookForDetail by remember { mutableStateOf<Book?>(null) }
    var selectedBookForReading by remember { mutableStateOf<Book?>(null) }
    var selectedArticleForReading by remember { mutableStateOf<Article?>(null) }
    
    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
            .statusBarsPadding(),
        bottomBar = {
            CustomBottomNavigationBar(
                currentTab = currentTab,
                onTabSelected = { 
                    currentTab = it
                    // Reset search when switching tabs to make experience clean
                    searchQuery = "" 
                }
            )
        },
        containerColor = DarkBg
    ) { innerPadding ->
        
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Main Router
            when (currentTab) {
                LibraryTab.HOME -> {
                    HomeScreen(
                        searchQuery = searchQuery,
                        onSearchChange = { searchQuery = it },
                        continueBook = bakaBook,
                        articles = allArticles,
                        onBookClick = { selectedBookForDetail = it },
                        onArticleClick = { selectedArticleForReading = it },
                        allBooks = allBooks
                    )
                }
                LibraryTab.LIBRARY -> {
                    LibraryScreen(
                        searchQuery = searchQuery,
                        onSearchChange = { searchQuery = it },
                        books = allBooks,
                        articles = allArticles,
                        onBookClick = { selectedBookForDetail = it },
                        onArticleClick = { selectedArticleForReading = it }
                    )
                }
                LibraryTab.SAVED -> {
                    SavedScreen(
                        books = allBooks,
                        articles = allArticles,
                        bookmarkedBookIds = bookmarkedBookIds,
                        onBookClick = { selectedBookForDetail = it },
                        onArticleClick = { selectedArticleForReading = it },
                        onToggleGlobalBookmark = { id ->
                            if (bookmarkedBookIds.contains(id)) {
                                bookmarkedBookIds.remove(id)
                            } else {
                                bookmarkedBookIds.add(id)
                            }
                        }
                    )
                }
                LibraryTab.PROFILE -> {
                    ProfileScreen()
                }
            }

            // Book Details Modal / BottomSheet Representation
            selectedBookForDetail?.let { book ->
                BookDetailDialog(
                    book = book,
                    isBookmarked = bookmarkedBookIds.contains(book.id),
                    onBookmarkToggle = {
                        if (bookmarkedBookIds.contains(book.id)) {
                            bookmarkedBookIds.remove(book.id)
                        } else {
                            bookmarkedBookIds.add(book.id)
                        }
                    },
                    onStartReading = {
                        selectedBookForReading = book
                        selectedBookForDetail = null
                    },
                    onDismiss = { selectedBookForDetail = null }
                )
            }

            // Real Reading Screen for Books
            selectedBookForReading?.let { book ->
                BookReaderScreen(
                    book = book,
                    onDismiss = { selectedBookForReading = null }
                )
            }

            // Real Reading Screen for Articles
            selectedArticleForReading?.let { article ->
                ArticleReaderScreen(
                    article = article,
                    onDismiss = { selectedArticleForReading = null }
                )
            }
        }
    }
}

// --------------------------------------------------------------------------
// SCREENS
// --------------------------------------------------------------------------

@Composable
fun HomeScreen(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    continueBook: Book,
    articles: List<Article>,
    allBooks: List<Book>,
    onBookClick: (Book) -> Unit,
    onArticleClick: (Article) -> Unit
) {
    val filteredArticles = if (searchQuery.isBlank()) {
        articles
    } else {
        articles.filter { 
            it.title.contains(searchQuery, ignoreCase = true) || 
            it.author.contains(searchQuery, ignoreCase = true) 
        }
    }

    val filteredBooks = if (searchQuery.isBlank()) {
        emptyList()
    } else {
        allBooks.filter { 
            it.title.contains(searchQuery, ignoreCase = true) || 
            it.author.contains(searchQuery, ignoreCase = true) 
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("home_screen_column"),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // App Header
        item {
            HomeHeader()
        }

        // Elegant Search Bar
        item {
            CustomSearchBar(
                query = searchQuery,
                onQueryChange = onSearchChange,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
            )
        }

        if (searchQuery.isNotBlank()) {
            // Search results section
            item {
                Text(
                    text = "অনুসন্ধানের ফলাফল",
                    style = TextStyle(
                        color = PrimaryGreen,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        letterSpacing = 1.sp
                    ),
                    modifier = Modifier.padding(start = 24.dp, top = 20.dp, bottom = 12.dp)
                )
            }

            if (filteredBooks.isEmpty() && filteredArticles.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp, horizontal = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.SearchOff,
                            contentDescription = "Not Found",
                            tint = TextMuted,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "কোন ফলাফল পাওয়া যায়নি",
                            fontSize = 14.sp,
                            color = TextMuted,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                items(filteredBooks) { book ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 6.dp)
                            .background(CardBgAlt, RoundedCornerShape(16.dp))
                            .border(1.dp, BorderColor, RoundedCornerShape(16.dp))
                            .clickable { onBookClick(book) }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(GreenMuted),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(book.shortWord, color = PrimaryGreen, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(book.title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("${book.category} • ${book.author}", color = TextMuted, fontSize = 11.sp)
                        }
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "Read",
                            tint = TextMuted,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                items(filteredArticles) { article ->
                    RecentArticleRow(article = article, onArticleClick = { onArticleClick(article) })
                }
            }
        } else {
            // "Continue reading" primary layout
            item {
                SectionHeader(title = "পড়া চালিয়ে যান")
                Spacer(modifier = Modifier.height(12.dp))
                ContinueReadingCard(book = continueBook, onBookClick = { onBookClick(continueBook) })
            }

            // "Recent Articles" section
            item {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(4.dp)
                                .clip(CircleShape)
                                .background(PrimaryGreen)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "সাম্প্রতিক প্রবন্ধ",
                            style = TextStyle(
                                color = TextMuted,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                letterSpacing = 1.sp
                            )
                        )
                    }
                    Text(
                        text = "সব দেখুন",
                        color = PrimaryGreen,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { /* Tab or action */ }
                    )
                }
            }

            // Scrollable Recent Articles Column
            items(articles.take(2)) { article ->
                RecentArticleRow(article = article, onArticleClick = { onArticleClick(article) })
            }
        }
    }
}

@Composable
fun LibraryScreen(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    books: List<Book>,
    articles: List<Article>,
    onBookClick: (Book) -> Unit,
    onArticleClick: (Article) -> Unit
) {
    var selectedFilterCategory by remember { mutableStateOf("সব") }
    val categories = listOf("সব", "তাফসীর", "হাদীস", "আকীদা", "ফরয আমল")

    val filteredBooks = remember(books, searchQuery, selectedFilterCategory) {
        books.filter { book ->
            val matchQuery = searchQuery.isBlank() || book.title.contains(searchQuery, ignoreCase = true) || book.author.contains(searchQuery, ignoreCase = true)
            val matchCat = selectedFilterCategory == "সব" || book.category == selectedFilterCategory
            matchQuery && matchCat
        }
    }

    val filteredArticles = remember(articles, searchQuery, selectedFilterCategory) {
        articles.filter { article ->
            val matchQuery = searchQuery.isBlank() || article.title.contains(searchQuery, ignoreCase = true) || article.author.contains(searchQuery, ignoreCase = true)
            val matchCat = selectedFilterCategory == "সব" || (selectedFilterCategory == "ফরয আমল" && article.title.contains("নামায"))
            matchQuery && matchCat
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("library_screen_column")
    ) {
        item {
            Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)) {
                Text(
                    text = "লাইব্রেরী কালেকশন",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "ইসলামী জ্ঞান ও প্রবন্ধের সুবিশাল ভাণ্ডার",
                    fontSize = 12.sp,
                    color = TextMuted
                )
            }
        }

        // Search Input
        item {
            CustomSearchBar(
                query = searchQuery,
                onQueryChange = onSearchChange,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
            )
        }

        // Horizontal Category Chips
        item {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { category ->
                    val isSelected = category == selectedFilterCategory
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) PrimaryGreen else CardBgAlt)
                            .border(1.dp, if (isSelected) Color.Transparent else BorderColor, RoundedCornerShape(12.dp))
                            .clickable { selectedFilterCategory = category }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = category,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) DarkBg else TextSecondary
                        )
                    }
                }
            }
        }

        // Book title header
        if (filteredBooks.isNotEmpty()) {
            item {
                SectionHeader(title = "সংকলিত কিতাবসমূহ")
                Spacer(modifier = Modifier.height(8.dp))
            }

            items(filteredBooks) { book ->
                LibraryBookItem(book = book, onBookClick = { onBookClick(book) })
            }
        }

        // Articles title header
        if (filteredArticles.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                SectionHeader(title = "গবেষণামূলক নিবন্ধ ও প্রবন্ধ")
                Spacer(modifier = Modifier.height(8.dp))
            }

            items(filteredArticles) { article ->
                RecentArticleRow(article = article, onArticleClick = { onArticleClick(article) })
            }
        }

        if (filteredBooks.isEmpty() && filteredArticles.isEmpty()) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 80.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.MenuBook,
                        contentDescription = "Empty",
                        tint = TextMuted.copy(alpha = 0.5f),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("এই ক্যাটাগরিতে কোন কিতাব বা নিবন্ধ মেলেনি", color = TextMuted, fontSize = 13.sp)
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
fun SavedScreen(
    books: List<Book>,
    articles: List<Article>,
    bookmarkedBookIds: List<String>,
    onBookClick: (Book) -> Unit,
    onArticleClick: (Article) -> Unit,
    onToggleGlobalBookmark: (String) -> Unit
) {
    val bookmarkedBooksList = books.filter { bookmarkedBookIds.contains(it.id) }
    // Since bookmarks of articles are locally managed, let's look at isBookmarked state or standard bookmarks
    val bookmarkedArticlesList = articles.filter { it.isBookmarked.value }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("saved_screen_column")
    ) {
        item {
            Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)) {
                Text(
                    text = "সংগৃহীত আইটেম",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "আপনার বুকমার্ক করা কিতাব ও প্রবন্ধ সমূহ",
                    fontSize = 12.sp,
                    color = TextMuted
                )
            }
        }

        if (bookmarkedBooksList.isEmpty() && bookmarkedArticlesList.isEmpty()) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 100.dp, bottom = 40.dp, start = 24.dp, end = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(CardBgAlt),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.BookmarkBorder,
                            contentDescription = "No Saved Items",
                            tint = TextMuted,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "সংগৃহীত কোন বুকমার্ক পাওয়া যায়নি",
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "পড়ার সময়ে কোনো বই বা প্রবন্ধ বুকমার্ক বোতাম টিপে এখানে যুক্ত করুন।",
                        color = TextMuted,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }
        } else {
            // Bookmarks Books
            if (bookmarkedBooksList.isNotEmpty()) {
                item {
                    SectionHeader(title = "সংরক্ষিত কিতাব")
                    Spacer(modifier = Modifier.height(8.dp))
                }

                items(bookmarkedBooksList) { book ->
                    LibraryBookItem(book = book, onBookClick = { onBookClick(book) })
                }
            }

            // Bookmarks Articles
            if (bookmarkedArticlesList.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(20.dp))
                    SectionHeader(title = "সংরক্ষিত প্রবন্ধ")
                    Spacer(modifier = Modifier.height(8.dp))
                }

                items(bookmarkedArticlesList) { article ->
                    RecentArticleRow(article = article, onArticleClick = { onArticleClick(article) })
                }
            }
        }
    }
}

@Composable
fun ProfileScreen() {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("profile_screen_column"),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Profile Avatar Gradient Circle
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.sweepGradient(
                                listOf(PrimaryGreen, AccentGreen, PrimaryGreen)
                            )
                        )
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(CardBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "মমিন",
                            color = PrimaryGreen,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "আব্দুর রহমান",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "ammamun94@gmail.com",
                    fontSize = 13.sp,
                    color = TextMuted
                )
            }
        }

        // Stats Dashboard
        item {
            Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                Text(
                    text = "অধ্যয়ন চিত্র",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryGreen,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ProfileStatCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Timeline,
                        title = "মোট পড়ার সময়",
                        value = "২ ঘণ্টা ৪৫ মি."
                    )
                    ProfileStatCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.LocalFireDepartment,
                        title = "দিনের ধারাবাহিকতা",
                        value = "৭ দিন",
                        iconColor = Color(0xFFF97316) // Flame orange
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ProfileStatCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.MenuBook,
                        title = "অধ্যয়নকৃত কিতাব",
                        value = "৪ টি বিষয়"
                    )
                    ProfileStatCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.LibraryBooks,
                        title = "সম্পূর্ণ প্রবন্ধ",
                        value = "৭ টি প্রবন্ধ"
                    )
                }
            }
        }

        // App Information Card
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 20.dp)
                    .background(
                        Brush.verticalGradient(listOf(CardBg, CardBgAlt)),
                        RoundedCornerShape(24.dp)
                    )
                    .border(1.dp, BorderColor, RoundedCornerShape(24.dp))
                    .padding(18.dp)
            ) {
                Column {
                    Text(
                        text = "MyReaderPro সম্পর্কে",
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "এটি একটি আধুনিক অ্যান্ড্রয়েড ডিজিটাল লাইব্রেরি যা বাংলা ভাষায় নির্ভরযোগ্য একাডেমিক ইসলামি কিতাব, তাফসীর ও সমসাময়িক প্রবন্ধ পড়ার জন্য নিবেদিত। সরল মানসম্মত ইন্টারফেস এবং ডার্ক থিম চোখের সুরক্ষায় সহায়ক।",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        lineHeight = 20.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Divider(color = BorderColor)
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("সংস্করণ", fontSize = 11.sp, color = TextMuted)
                        Text("v১.০.৪ (Sophisticated Dark Build)", fontSize = 11.sp, color = PrimaryGreen, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// --------------------------------------------------------------------------
// REUSABLE COMPONENTS
// --------------------------------------------------------------------------

@Composable
fun HomeHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "আস-সালামু আলাইকুম",
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = PrimaryGreen,
                letterSpacing = 1.sp
            )
            Text(
                text = "MyReaderPro",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }

        // Header Avatar Mockup
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(CardBg)
                .border(1.dp, BorderColor, CircleShape)
                .padding(2.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(PrimaryGreen, AccentGreen)
                        )
                    )
            )
        }
    }
}

@Composable
fun CustomSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(CardBg.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            .border(1.dp, BorderColor, RoundedCornerShape(16.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = "Search icon",
            tint = TextMuted.copy(alpha = 0.6f),
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        
        Box(modifier = Modifier.weight(1f)) {
            if (query.isEmpty()) {
                Text(
                    text = "বই বা প্রবন্ধ খুঁজুন...",
                    style = TextStyle(
                        color = TextMuted.copy(alpha = 0.5f),
                        fontSize = 13.sp
                    )
                )
            }
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                textStyle = TextStyle(
                    color = TextPrimary,
                    fontSize = 13.sp
                ),
                cursorBrush = SolidColor(PrimaryGreen),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("search_field"),
                singleLine = true
            )
        }
        
        if (query.isNotEmpty()) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Clear",
                tint = TextMuted,
                modifier = Modifier
                    .size(18.dp)
                    .clickable { onQueryChange("") }
            )
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(4.dp)
                .clip(CircleShape)
                .background(PrimaryGreen)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            style = TextStyle(
                color = TextMuted,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                letterSpacing = 1.sp
            )
        )
    }
}

@Composable
fun ContinueReadingCard(
    book: Book,
    onBookClick: () -> Unit
) {
    val progress = book.currentPage.value.toFloat() / book.totalPages.toFloat()
    val progressPct = (progress * 100).toInt()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .background(
                Brush.linearGradient(colors = listOf(CardGradientStart, CardGradientEnd)),
                RoundedCornerShape(24.dp)
            )
            .border(1.dp, BorderColor, RoundedCornerShape(24.dp))
            .clickable { onBookClick() }
            .padding(18.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Book cover representation
            Box(
                modifier = Modifier
                    .width(72.dp)
                    .height(96.dp)
                    .background(Color(0xFF2D3139), RoundedCornerShape(12.dp))
                    .padding(6.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = book.category,
                        color = PrimaryGreen,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.5f)
                            .height(1.dp)
                            .background(PrimaryGreen.copy(alpha = 0.3f))
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = book.author,
                        color = TextSecondary,
                        fontSize = 9.sp,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Details and progress
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = book.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "পৃষ্ঠা ${book.currentPage.value} / ${book.totalPages}",
                    fontSize = 12.sp,
                    color = TextMuted
                )
                Spacer(modifier = Modifier.height(14.dp))
                
                // M3 styled indicator with neon/glow effect
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.05f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progress)
                            .height(6.dp)
                            .clip(CircleShape)
                            .background(PrimaryGreen)
                    )
                }
            }
        }
    }
}

@Composable
fun RecentArticleRow(
    article: Article,
    onArticleClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 6.dp)
            .background(CardBgAlt, RoundedCornerShape(16.dp))
            .border(1.dp, BorderColor, RoundedCornerShape(16.dp))
            .clickable { onArticleClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Book class icon wrapper
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(CardBg, RoundedCornerShape(12.dp))
                .border(0.5.dp, BorderColor, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = article.categoryPrefix,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryGreen
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Headline & read details
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = article.title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "${article.readTime} • ${article.author}",
                fontSize = 10.sp,
                color = TextMuted
            )
        }

        // Action circle Button
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(CardBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Read article",
                tint = TextSecondary,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
fun LibraryBookItem(
    book: Book,
    onBookClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 6.dp)
            .background(CardBgAlt, RoundedCornerShape(16.dp))
            .border(1.dp, BorderColor, RoundedCornerShape(16.dp))
            .clickable { onBookClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(GreenMuted),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = book.shortWord,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryGreen
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = book.title,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                fontSize = 13.sp
            )
            Text(
                text = "${book.category} • ${book.author}",
                color = TextMuted,
                fontSize = 10.sp
            )
        }

        Icon(
            imageVector = Icons.Default.ArrowForward,
            contentDescription = "Detail",
            tint = TextMuted,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
fun ProfileStatCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    title: String,
    value: String,
    iconColor: Color = PrimaryGreen
) {
    Column(
        modifier = modifier
            .background(CardBg, RoundedCornerShape(16.dp))
            .border(1.dp, BorderColor, RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = title,
            fontSize = 10.sp,
            color = TextMuted
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}

@Composable
fun CustomBottomNavigationBar(
    currentTab: LibraryTab,
    onTabSelected: (LibraryTab) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars),
        color = NavBg,
        border = BorderStroke(1.dp, BorderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 12.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            LibraryTab.values().forEach { tab ->
                val isSelected = tab == currentTab
                
                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onTabSelected(tab) }
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                        .testTag("tab_${tab.name.lowercase()}"),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) GreenMuted else Color.Transparent)
                            .padding(horizontal = 14.dp, vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isSelected) tab.selectedIcon else tab.icon,
                            contentDescription = tab.title,
                            tint = if (isSelected) PrimaryGreen else TextMuted,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = tab.titleBangla,
                        fontSize = 10.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) PrimaryGreen else TextMuted
                    )
                }
            }
        }
    }
}

// --------------------------------------------------------------------------
// DIALOGS & SHEET READERS
// --------------------------------------------------------------------------

@Composable
fun BookDetailDialog(
    book: Book,
    isBookmarked: Boolean,
    onBookmarkToggle: () -> Unit,
    onStartReading: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardBg,
        textContentColor = TextSecondary,
        titleContentColor = TextPrimary,
        shape = RoundedCornerShape(28.dp),
        confirmButton = {
            Button(
                onClick = onStartReading,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen, contentColor = DarkBg),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("সংকলন পড়ুন", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("বন্ধ করুন", color = TextMuted)
            }
        },
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = book.category,
                        fontSize = 10.sp,
                        color = PrimaryGreen,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = book.title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
                
                IconButton(onClick = onBookmarkToggle) {
                    Icon(
                        imageVector = if (isBookmarked) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                        contentDescription = "Bookmark",
                        tint = if (isBookmarked) PrimaryGreen else TextMuted
                    )
                }
            }
        },
        text = {
            Column {
                Divider(color = BorderColor)
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "লেখক: ${book.author}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = book.description,
                    fontSize = 11.sp,
                    lineHeight = 18.sp,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CardBgAlt, RoundedCornerShape(12.dp))
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("বর্তমান অগ্রগতি:", fontSize = 11.sp, color = TextMuted)
                    Text("পৃষ্ঠা ${book.currentPage.value} / ${book.totalPages} (${((book.currentPage.value.toFloat() / book.totalPages) * 100).toInt()}%)", fontSize = 11.sp, color = PrimaryGreen, fontWeight = FontWeight.Bold)
                }
            }
        }
    )
}

@Composable
fun BookReaderScreen(
    book: Book,
    onDismiss: () -> Unit
) {
    var userRating by remember { mutableStateOf(0) }
    var userTextSize by remember { mutableStateOf(16f) }
    
    // Virtual Bangla reading content for Tafsir Surah Al Baqarah
    val bakaPages = listOf(
        "সূরা আল-বাকারাহ হলো আল কুরআনের ২য় সূরা এবং এটি মাদানী সূরা হিসেবে অবতীর্ণ হয়। এই মানবজাতির পথ প্রদর্শনের মূল মহাসত্য বর্ণিত হয়েছে এতে। সূরা বাকারার ২৫৫তম আয়াতটি হলো অন্যতম বিখ্যাত 'আয়াতুল কুরসী', যা তিলাওয়াত করলে অনেক সওয়াব এবং প্রভূত হিকমত অন্বেষণ সম্ভব হয়। হযরত মুহাম্মদ (সা) সূরা বাকারার অসাধারণ গুরুত্ব নিয়ে অনেক হাদীস পরিবেশন করেছেন।",
        "সূরা বাকারার প্রারম্ভিক অংশে কালামুল্লাহ শরীফের প্রতি পূর্ণ বিশ্বাসের নির্দেশ দেওয়া হয়েছে এবং মুনাফিক ও কাফেরদের চিহ্নিত বৈশিষ্ট্যের বিশদ বিশ্লেষণ করা হয়েছে। আল-তাক্বওয়া বা খোদাভীতি অর্জন করা প্রত্যেক মুমিনের মৌলিক কর্তব্য যা এ সুরার মুখ্য বিষয়বস্তু। হে মুমিনগণ, তোমরা খাঁটি মুসলিম না হয়ে মৃত্যুবরণ করো না।",
        "এই সুরার মধ্যে আল্লাহর অসীম সৃষ্টির নিদর্শন তুলে ধরা হয়েছে—যেমন আকাশ ও পৃথিবীর সৃষ্টি, দিন ও রাত্রির আবর্তন, সাগরে ভেসে চলা জাহাজের উপমা। এসব জ্ঞানবান সম্প্রদায়ের জন্য আল্লাহর ক্ষমতার বড় বহিঃপ্রকাশ। এই তাওহীদের বাণী আমাদের আত্মায় সুদৃঢ় করার জন্য সর্বশক্তিমান আল্লাহর প্রতি প্রগাঢ় ভালোবাসা প্রয়োজন।"
    )

    val contentForPage = bakaPages.getOrElse((book.currentPage.value % bakaPages.size)) {
        "তাফসীর ইবনে কাসীর: পৃষ্ঠা ${book.currentPage.value}. এই অধ্যায়ে দ্বীনদার পরিবারের আত্মশুদ্ধি এবং ইসলামী জীবন ব্যবস্থার বিস্তারিত ব্যাখ্যা আলোচনা করা হচ্ছে। আল-কুরআনের প্রতিটি নির্দেশনার পেছনে রয়েছে মানুষের ইহকালীন ও পরকালীন শান্তি।"
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg),
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(NavBg)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Column {
                        Text(book.title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(book.author, color = TextMuted, fontSize = 11.sp)
                    }
                }
                
                // Font Size Adjuster
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { if (userTextSize > 12) userTextSize -= 2 }) {
                        Icon(imageVector = Icons.Default.Remove, contentDescription = "M", tint = TextSecondary, modifier = Modifier.size(16.dp))
                    }
                    Text("Aa", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    IconButton(onClick = { if (userTextSize < 24) userTextSize += 2 }) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "P", tint = TextSecondary, modifier = Modifier.size(16.dp))
                    }
                }
            }
        },
        bottomBar = {
            // Paginate back and forth
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(NavBg)
                    .border(BorderStroke(1.dp, BorderColor))
                    .padding(vertical = 12.dp, horizontal = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = { if (book.currentPage.value > 1) book.currentPage.value-- },
                    colors = ButtonDefaults.buttonColors(containerColor = CardBgAlt, contentColor = TextPrimary),
                    shape = RoundedCornerShape(12.dp),
                    enabled = book.currentPage.value > 1
                ) {
                    Text("পূর্ববর্তী", fontSize = 11.sp)
                }

                Text(
                    text = "পৃষ্ঠা ${book.currentPage.value}ী",
                    color = PrimaryGreen,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )

                Button(
                    onClick = { if (book.currentPage.value < book.totalPages) book.currentPage.value++ },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen, contentColor = DarkBg),
                    shape = RoundedCornerShape(12.dp),
                    enabled = book.currentPage.value < book.totalPages
                ) {
                    Text("পরবর্তী", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        },
        containerColor = DarkBg
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp)
        ) {
            // Reading Progress Indicator at top of cover page
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = book.category.uppercase(),
                    color = PrimaryGreen,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "${((book.currentPage.value.toFloat() / book.totalPages) * 100).toInt()}% সমাপ্ত",
                    color = TextMuted,
                    fontSize = 10.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Main Text block with adaptive siliguri-like font spacing
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(CardBgAlt.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                    .border(1.dp, BorderColor, RoundedCornerShape(20.dp))
                    .padding(18.dp)
            ) {
                LazyColumn {
                    item {
                        Text(
                            text = contentForPage,
                            fontSize = userTextSize.sp,
                            lineHeight = (userTextSize * 1.6f).sp,
                            color = TextSecondary,
                            textAlign = TextAlign.Justify,
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ArticleReaderScreen(
    article: Article,
    onDismiss: () -> Unit
) {
    var userTextSize by remember { mutableStateOf(16f) }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg),
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(NavBg)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Column {
                        Text(article.title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(article.author, color = TextMuted, fontSize = 11.sp)
                    }
                }

                // Bookmark action toggler inside reader
                IconButton(onClick = { article.isBookmarked.value = !article.isBookmarked.value }) {
                    Icon(
                        imageVector = if (article.isBookmarked.value) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                        contentDescription = "Save Option",
                        tint = if (article.isBookmarked.value) PrimaryGreen else TextPrimary
                    )
                }
            }
        },
        containerColor = DarkBg
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "প্রবন্ধ অধ্যায়ন",
                    fontSize = 11.sp,
                    color = PrimaryGreen,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = article.readTime,
                    fontSize = 11.sp,
                    color = TextMuted
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Text Frame Container
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(CardBgAlt.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                    .border(1.dp, BorderColor, RoundedCornerShape(20.dp))
                    .padding(20.dp)
            ) {
                LazyColumn {
                    item {
                        Text(
                            text = article.content,
                            fontSize = userTextSize.sp,
                            lineHeight = (userTextSize * 1.6f).sp,
                            color = TextSecondary,
                            textAlign = TextAlign.Justify,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen, contentColor = DarkBg),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("পড়া শেষ করেছি", fontWeight = FontWeight.Bold)
            }
        }
    }
}
