package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.*
import com.example.ui.theme.TealPrimary
import com.example.ui.theme.OrangeAccent
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class PresetBackdrop(val id: String, val name: String, val brush: Brush, val textColor: Color)

val PRESET_BACKDROPS = listOf(
    PresetBackdrop("preset_midnight", "Cosmic Midnight", Brush.verticalGradient(listOf(Color(0xFF1E1B4B), Color(0xFF0F172A))), Color.White),
    PresetBackdrop("preset_sunset", "Warm Sunset", Brush.verticalGradient(listOf(Color(0xFFED4F74), Color(0xFFF97316))), Color.White),
    PresetBackdrop("preset_forest", "Emerald Forest", Brush.verticalGradient(listOf(Color(0xFF064E3B), Color(0xFF059669))), Color.White),
    PresetBackdrop("preset_morning", "Fresh Morning", Brush.verticalGradient(listOf(Color(0xFF0284C7), Color(0xFF06B6D4))), Color.White),
    PresetBackdrop("preset_rose", "Rose Garden", Brush.verticalGradient(listOf(Color(0xFF881337), Color(0xFFEC4899))), Color.White)
)

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ForumHubTab(viewModel: ReaderViewModel, activeUser: UserEntity?) {
    val postsList by viewModel.allPosts.collectAsState(initial = emptyList())
    val allBooksList by viewModel.allBooks.collectAsState(initial = emptyList())
    
    var searchQuery by remember { mutableStateOf("") }
    var activeFeedType by remember { mutableStateOf("RECENT") } // RECENT, TRENDING, FOLLOWING, POPULAR
    var activeTopicFilter by remember { mutableStateOf("ALL") } // ALL, DISCUSSION, TIPS, ANNOUNCEMENT, KNOWLEDGE, ASK, ARTICLE, EXPERIENCE, IMAGE
    var followedAuthors by remember { mutableStateOf(setOf("Sofia Rahman", "Dr. Imran Karim")) }
    
    var showCreateDialog by remember { mutableStateOf(false) }
    var selectedDetailPost by remember { mutableStateOf<PostEntity?>(null) }
    var activeSharePost by remember { mutableStateOf<PostEntity?>(null) }
    var showShareSuccessMessage by remember { mutableStateOf<String?>(null) }
    
    val scope = rememberCoroutineScope()

    // Dynamic filtering & scoring
    val filteredPosts = remember(postsList, searchQuery, activeFeedType, activeTopicFilter, followedAuthors) {
        var baseList = postsList
        
        // Category / Topic Tag filter
        if (activeTopicFilter != "ALL") {
            if (activeTopicFilter == "IMAGE") {
                baseList = baseList.filter { it.imageUrl.isNotBlank() }
            } else {
                baseList = baseList.filter { it.type.equals(activeTopicFilter, ignoreCase = true) }
            }
        }
        
        // Live search filter
        if (searchQuery.isNotBlank()) {
            baseList = baseList.filter {
                it.title.contains(searchQuery, ignoreCase = true) ||
                it.content.contains(searchQuery, ignoreCase = true) ||
                it.authorName.contains(searchQuery, ignoreCase = true)
            }
        }
        
        // Feed type sorting & subset
        when (activeFeedType) {
            "RECENT" -> baseList.sortedByDescending { it.timestamp }
            "TRENDING" -> baseList.sortedByDescending { it.likesCount + (it.commentsCount * 2) }
            "FOLLOWING" -> baseList.filter { followedAuthors.contains(it.authorName) }
            "POPULAR" -> baseList.filter { it.type == "DISCUSSION" }.sortedByDescending { it.commentsCount }
            else -> baseList
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = TealPrimary,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier
                    .padding(bottom = 8.dp)
                    .testTag("forum_create_post_fab")
            ) {
                Icon(
                    imageVector = Icons.Default.AddComment,
                    contentDescription = "Create Forum Thread",
                    tint = Color.White
                )
            }
        },
        modifier = Modifier.fillMaxSize().testTag("forum_hub_tab")
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Elegant Cosmic Banner Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(TealPrimary, TealPrimary.copy(alpha = 0.85f))
                        )
                    )
                    .padding(horizontal = 20.dp, vertical = 20.dp)
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            val greetingName = activeUser?.name ?: "Reader"
                            Text(
                                text = "Bonjour, $greetingName! 🌅",
                                color = Color.White,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Black
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Explore shared wisdom & book insights today",
                                color = Color.White.copy(alpha = 0.75f),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.15f)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = OrangeAccent, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                               Text("COSMOS PRO", color = OrangeAccent, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Feed Search Field
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search community threads, articles, contributors...", color = Color.White.copy(alpha = 0.55f), fontSize = 13.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.White.copy(alpha = 0.7f)) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = null, tint = Color.White.copy(alpha = 0.7f))
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White.copy(alpha = 0.9f),
                            focusedContainerColor = Color.White.copy(alpha = 0.15f),
                            unfocusedContainerColor = Color.White.copy(alpha = 0.08f),
                            focusedBorderColor = Color.White.copy(alpha = 0.25f),
                            unfocusedBorderColor = Color.White.copy(alpha = 0.12f),
                            cursorColor = OrangeAccent
                        ),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    )
                }
            }

            // Quick Create trigger header bar
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .clickable { showCreateDialog = true },
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(TealPrimary.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = (activeUser?.name ?: "R").take(1).uppercase(),
                            color = TealPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                    Text(
                        text = "Share translation keynotes, ask reading inquiries, or review a chapter...",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        imageVector = Icons.Default.BorderColor,
                        contentDescription = null,
                        tint = TealPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Feed Selection & Main Segmented Controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val feeds = listOf("RECENT", "TRENDING", "FOLLOWING", "POPULAR")
                feeds.forEach { f ->
                    val isSel = activeFeedType == f
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSel) TealPrimary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                            .clickable { activeFeedType = f }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = f,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            // Categories / Topic list row
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val topics = listOf("ALL", "DISCUSSION", "TIPS", "ANNOUNCEMENT", "KNOWLEDGE", "ASK", "ARTICLE", "EXPERIENCE", "IMAGE")
                items(topics) { topic ->
                    val isSel = activeTopicFilter == topic
                    ElevatedFilterChip(
                        selected = isSel,
                        onClick = { activeTopicFilter = topic },
                        label = { Text(topic, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        colors = FilterChipDefaults.elevatedFilterChipColors(
                            selectedContainerColor = OrangeAccent,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            // Main feed scroll list
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                if (filteredPosts.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 40.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("No insightful threads found here.", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                            Text("Be the first contributor and write a post!", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f))
                        }
                    }
                } else {
                    items(filteredPosts, key = { it.id }) { post ->
                        CommunityPostCard(
                            post = post,
                            activeUser = activeUser,
                            onLike = { viewModel.toggleLikePost(post.id) },
                            onSave = { viewModel.toggleSavePost(post.id) },
                            onReport = { viewModel.reportPost(post.id) },
                            onDelete = { viewModel.deletePost(post) },
                            onShare = {
                                activeSharePost = post
                                showShareSuccessMessage = "Co-sharing link copied! Direct clipboard updated."
                                scope.launch {
                                    delay(2000)
                                    showShareSuccessMessage = null
                                }
                            },
                            onClick = { selectedDetailPost = post }
                        )
                    }
                }
            }
        }
    }

    // Interactive Success Share Toast Snackbar
    AnimatedVisibility(
        visible = showShareSuccessMessage != null,
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.inverseSurface),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.shadow(8.dp)
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Share, contentDescription = null, tint = TealPrimary)
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = showShareSuccessMessage ?: "",
                    color = MaterialTheme.colorScheme.inverseOnSurface,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }

    // Creating post Dialog
    if (showCreateDialog) {
        CommunityCreatePostDialog(
            allBooks = allBooksList,
            onDismiss = { showCreateDialog = false },
            onCreate = { title, content, type, bookRef, imageUrl ->
                viewModel.createPost(title, content, type, bookRef, imageUrl)
                showCreateDialog = false
            }
        )
    }

    // Detail Dialog thread
    if (selectedDetailPost != null) {
        val currentPost = selectedDetailPost!!
        CommunityDetailDialog(
            post = currentPost,
            viewModel = viewModel,
            activeUser = activeUser,
            onDismiss = { selectedDetailPost = null }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CommunityPostCard(
    post: PostEntity,
    activeUser: UserEntity?,
    onLike: () -> Unit,
    onSave: () -> Unit,
    onReport: () -> Unit,
    onDelete: () -> Unit,
    onShare: () -> Unit,
    onClick: () -> Unit
) {
    // Check if background image holds a preset ID
    val preset = PRESET_BACKDROPS.find { it.id == post.imageUrl }
    val cardModifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(16.dp))
        .combinedClickable(
            onClick = onClick,
            onDoubleClick = onLike
        )
        .shadow(4.dp)

    Card(
        modifier = cardModifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
    ) {
        Box(
            modifier = if (preset != null) {
                Modifier
                    .fillMaxWidth()
                    .background(preset.brush)
                    .padding(16.dp)
            } else {
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            }
        ) {
            val contentColor = preset?.textColor ?: MaterialTheme.colorScheme.onSurface
            val secondaryColor = if (preset != null) contentColor.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant

            Column {
                // Header rows author profiles
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(TealPrimary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = post.authorName.take(1).uppercase(),
                                color = if (preset != null) OrangeAccent else TealPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = post.authorName,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = contentColor
                            )
                            Text(
                                text = formatTimestamp(post.timestamp),
                                fontSize = 10.sp,
                                color = secondaryColor
                            )
                        }
                    }

                    Row {
                        IconButton(onClick = onSave, modifier = Modifier.size(32.dp)) {
                            Icon(
                                imageVector = if (post.isSaved) Icons.Default.Bookmark else Icons.Outlined.BookmarkBorder,
                                contentDescription = "Save Thread",
                                tint = if (post.isSaved) OrangeAccent else secondaryColor,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        
                        // Delete button if owns the thread
                        if (activeUser != null && activeUser.email == post.authorEmail) {
                            IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete Thread", tint = Color.Red, modifier = Modifier.size(18.dp))
                            }
                        } else {
                            IconButton(onClick = onReport, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Default.Report, contentDescription = "Report Thread", tint = if (post.reportCount > 0) Color.Red else secondaryColor, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Post Types Indicator
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (preset != null) Color.White.copy(alpha = 0.2f) else TealPrimary.copy(alpha = 0.1f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = post.type,
                            color = if (preset != null) Color.White else TealPrimary,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (post.bookTitleRef.isNotBlank()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (preset != null) Color.White.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Icon(Icons.Default.MenuBook, contentDescription = null, tint = if (preset != null) Color.White else TealPrimary, modifier = Modifier.size(10.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = post.bookTitleRef,
                                color = if (preset != null) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 8.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Post details title & text layout
                Text(
                    text = post.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = contentColor,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = post.content,
                    fontSize = 13.sp,
                    color = contentColor.copy(alpha = 0.9f),
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 18.sp
                )

                // Render image preview if provided a URL that is not a preset
                if (post.imageUrl.isNotBlank() && preset == null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color.Gray.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        // Displaying stylized visual image frame
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Image, contentDescription = null, tint = TealPrimary, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Premium Image: ${post.imageUrl.take(30)}...", fontSize = 11.sp, color = TealPrimary)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Divider(color = if (preset != null) Color.White.copy(alpha = 0.2f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                Spacer(modifier = Modifier.height(8.dp))

                // Engagement buttons panel
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { onLike() }
                        ) {
                            Icon(
                                imageVector = if (post.isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Like thread",
                                tint = if (post.isLiked) Color.Red else secondaryColor,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = post.likesCount.toString(),
                                fontSize = 12.sp,
                                color = secondaryColor,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { onClick() }
                        ) {
                            Icon(Icons.Default.ModeComment, contentDescription = "View Replies", tint = secondaryColor, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = post.commentsCount.toString(),
                                fontSize = 12.sp,
                                color = secondaryColor,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    IconButton(onClick = onShare, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Share, contentDescription = "Share", tint = secondaryColor, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityDetailDialog(
    post: PostEntity,
    viewModel: ReaderViewModel,
    activeUser: UserEntity?,
    onDismiss: () -> Unit
) {
    val commentsList by viewModel.getCommentsForPost(post.id).collectAsState(initial = emptyList())
    var commentText by remember { mutableStateOf("") }
    var replyingToComment by remember { mutableStateOf<CommentEntity?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 24.dp),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Top header controls
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close details")
                    }
                    Text("Discussion Room", fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = TealPrimary)
                    
                    // Display details about total interactions
                    Box(
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(OrangeAccent.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("${post.likesCount + post.commentsCount} Threads Insights", fontSize = 10.sp, color = OrangeAccent, fontWeight = FontWeight.Bold)
                    }
                }

                Divider(color = MaterialTheme.colorScheme.outlineVariant)

                // Scroll area
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    item {
                        Spacer(modifier = Modifier.height(14.dp))
                        // Embed thread post inside dialog
                        CommunityPostCard(
                            post = post,
                            activeUser = activeUser,
                            onLike = { viewModel.toggleLikePost(post.id) },
                            onSave = { viewModel.toggleSavePost(post.id) },
                            onReport = { viewModel.reportPost(post.id) },
                            onDelete = { viewModel.deletePost(post); onDismiss() },
                            onShare = {},
                            onClick = {}
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Insightful Contributions (${commentsList.size})",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TealPrimary
                        )
                    }

                    if (commentsList.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("No thoughts shared. Break the silence!", fontSize = 12.sp, color = Color.Gray)
                            }
                        }
                    } else {
                        // Separate root comments and child replies
                        val rootComments = commentsList.filter { it.parentCommentId == -1 }
                        val childReplies = commentsList.filter { it.parentCommentId != -1 }

                        items(rootComments) { rootComment ->
                            // Render root comment
                            RootCommentCard(
                                comment = rootComment,
                                activeUser = activeUser,
                                replies = childReplies.filter { it.parentCommentId == rootComment.id },
                                onDelete = { viewModel.deleteComment(rootComment) },
                                onReply = { replyingToComment = rootComment }
                            )
                        }
                    }
                }

                // Reply indicator banner
                if (replyingToComment != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(TealPrimary.copy(alpha = 0.1f))
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Reply, contentDescription = null, tint = TealPrimary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Replying to thread: @${replyingToComment!!.authorName}", fontSize = 11.sp, color = TealPrimary, fontWeight = FontWeight.Bold)
                        }
                        IconButton(onClick = { replyingToComment = null }, modifier = Modifier.size(20.dp)) {
                            Icon(Icons.Default.Cancel, contentDescription = "Cancel Reply", tint = Color.Gray, modifier = Modifier.size(16.dp))
                        }
                    }
                }

                // Input text contribution field
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = commentText,
                        onValueChange = { commentText = it },
                        placeholder = { Text("Share translation reviews, definition suggestions...", fontSize = 13.sp) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        maxLines = 3,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = TealPrimary,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface
                        )
                    )

                    Button(
                        onClick = {
                            if (commentText.isNotBlank()) {
                                val replyId = replyingToComment?.id ?: -1
                                viewModel.addComment(post.id, commentText, replyId)
                                commentText = ""
                                replyingToComment = null
                            }
                        },
                        enabled = commentText.isNotBlank(),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.size(44.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
                    ) {
                        Icon(Icons.Default.Send, contentDescription = "Publish Comment", tint = Color.White, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun RootCommentCard(
    comment: CommentEntity,
    activeUser: UserEntity?,
    replies: List<CommentEntity>,
    onDelete: () -> Unit,
    onReply: () -> Unit
) {
    var expandedReplies by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                // Author row layout
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(TealPrimary.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(comment.authorName.take(1).uppercase(), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TealPrimary)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(comment.authorName, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("• " + formatTimestamp(comment.timestamp), fontSize = 9.sp, color = Color.Gray)
                    }

                    if (activeUser != null && activeUser.email == comment.authorEmail) {
                        IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete Comment", tint = Color.Red, modifier = Modifier.size(14.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(comment.text, fontSize = 12.sp, lineHeight = 16.sp)

                Spacer(modifier = Modifier.height(8.dp))

                // Reply and collapse actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier
                            .clickable { onReply() }
                            .padding(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Reply, contentDescription = null, tint = TealPrimary, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Reply", fontSize = 11.sp, color = TealPrimary, fontWeight = FontWeight.Bold)
                    }

                    if (replies.isNotEmpty()) {
                        Row(
                            modifier = Modifier
                                .clickable { expandedReplies = !expandedReplies }
                                .padding(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (expandedReplies) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = null,
                                tint = OrangeAccent,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (expandedReplies) "Hide replies (${replies.size})" else "Show replies (${replies.size})",
                                fontSize = 11.sp,
                                color = OrangeAccent,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Child replies indented layout
        if (expandedReplies && replies.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .padding(start = 18.dp, top = 8.dp)
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.4f))
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                replies.forEach { reply ->
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(18.dp)
                                        .clip(CircleShape)
                                        .background(OrangeAccent.copy(alpha = 0.1f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(reply.authorName.take(1).uppercase(), fontSize = 8.sp, fontWeight = FontWeight.Bold, color = OrangeAccent)
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(reply.authorName, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = OrangeAccent)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(formatTimestamp(reply.timestamp), fontSize = 8.sp, color = Color.Gray)
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(reply.text, fontSize = 11.sp, lineHeight = 15.sp)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun CommunityCreatePostDialog(
    allBooks: List<BookEntity>,
    onDismiss: () -> Unit,
    onCreate: (title: String, content: String, type: String, bookRef: String, imageUrl: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf("DISCUSSION") } // DISCUSSION, TIPS, ANNOUNCEMENT, KNOWLEDGE, ASK, ARTICLE, EXPERIENCE
    
    // Choose format option
    var isImagePostFormat by remember { mutableStateOf(false) }
    var imageSettingMode by remember { mutableStateOf("PRESET") } // PRESET, CUSTOM url
    var selectedPresetBackdropId by remember { mutableStateOf("preset_midnight") }
    var inputCustomImageUrl by remember { mutableStateOf("") }

    // Reference Book linkage
    var selectedReferenceBookTitle by remember { mutableStateOf("") }
    var isBookDropdownOpen by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            shape = RoundedCornerShape(20.dp),
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Draft Insight Thread", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = TealPrimary)
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close dialog")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Post Title Input code
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Catchy Thread Title", fontSize = 12.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = TealPrimary)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Post Content Input
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Review, suggest keynotes, translate keywords...", fontSize = 12.sp) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = TealPrimary)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Link to active books dropdown
                ExposedDropdownMenuBox(
                    expanded = isBookDropdownOpen,
                    onExpandedChange = { isBookDropdownOpen = it }
                ) {
                    OutlinedTextField(
                        value = if (selectedReferenceBookTitle.isEmpty()) "Tap to tag a book (Optional)" else selectedReferenceBookTitle,
                        onValueChange = {},
                        readOnly = true,
                        leadingIcon = { Icon(Icons.Default.MenuBook, contentDescription = null, tint = TealPrimary) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isBookDropdownOpen) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = TealPrimary,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface
                        )
                    )

                    ExposedDropdownMenu(
                        expanded = isBookDropdownOpen,
                        onDismissRequest = { isBookDropdownOpen = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("No Book Reference", fontWeight = FontWeight.Bold, color = Color.Gray) },
                            onClick = {
                                selectedReferenceBookTitle = ""
                                isBookDropdownOpen = false
                            }
                        )
                        allBooks.forEach { book ->
                            DropdownMenuItem(
                                text = { Text(book.title) },
                                onClick = {
                                    selectedReferenceBookTitle = book.title
                                    isBookDropdownOpen = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Post Type selector chips
                Text("Content Segment:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = TealPrimary)
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val types = listOf("DISCUSSION", "TIPS", "ANNOUNCEMENT", "KNOWLEDGE", "ASK", "ARTICLE", "EXPERIENCE")
                    items(types) { t ->
                        val isSel = selectedType == t
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSel) OrangeAccent else MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { selectedType = t }
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Text(t, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Enable Image formatting option switcher
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Brush, contentDescription = null, tint = OrangeAccent, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add Image Backdrop Styles", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                    Switch(
                        checked = isImagePostFormat,
                        onCheckedChange = { isImagePostFormat = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = TealPrimary)
                    )
                }

                AnimatedVisibility(visible = isImagePostFormat) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { imageSettingMode = "PRESET" },
                                colors = ButtonDefaults.buttonColors(containerColor = if (imageSettingMode == "PRESET") OrangeAccent else MaterialTheme.colorScheme.surfaceVariant),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.weight(1f).height(32.dp),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text("Presets", fontSize = 11.sp, color = if (imageSettingMode == "PRESET") Color.White else MaterialTheme.colorScheme.onSurface)
                            }

                            Button(
                                onClick = { imageSettingMode = "CUSTOM" },
                                colors = ButtonDefaults.buttonColors(containerColor = if (imageSettingMode == "CUSTOM") OrangeAccent else MaterialTheme.colorScheme.surfaceVariant),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.weight(1f).height(32.dp),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text("Custom URL", fontSize = 11.sp, color = if (imageSettingMode == "CUSTOM") Color.White else MaterialTheme.colorScheme.onSurface)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        if (imageSettingMode == "PRESET") {
                            LazyRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(PRESET_BACKDROPS) { bd ->
                                    val isSel = selectedPresetBackdropId == bd.id
                                    Box(
                                        modifier = Modifier
                                            .size(width = 90.dp, height = 48.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(bd.brush)
                                            .border(
                                                width = if (isSel) 2.dp else 1.dp,
                                                color = if (isSel) OrangeAccent else Color.Gray.copy(alpha = 0.5f),
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                            .clickable { selectedPresetBackdropId = bd.id },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(bd.name, fontSize = 9.sp, color = bd.textColor, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                                    }
                                }
                            }
                        } else {
                            OutlinedTextField(
                                value = inputCustomImageUrl,
                                onValueChange = { inputCustomImageUrl = it },
                                label = { Text("Enter Web Image URL here", fontSize = 11.sp) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Submit block
                Button(
                    onClick = {
                        if (title.isNotBlank() && content.isNotBlank()) {
                            val resolvedImageUrl = if (isImagePostFormat) {
                                if (imageSettingMode == "PRESET") selectedPresetBackdropId else inputCustomImageUrl
                            } else ""
                            val finalType = if (isImagePostFormat) "IMAGE" else selectedType
                            onCreate(title, content, finalType, selectedReferenceBookTitle, resolvedImageUrl)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                    enabled = title.isNotBlank() && content.isNotBlank()
                ) {
                    Text(
                        text = "🚀 Publish Insight Thread",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
        }
    }
}

fun formatTimestamp(timestamp: Long): String {
    val date = Date(timestamp)
    val sdf = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
    return sdf.format(date)
}
