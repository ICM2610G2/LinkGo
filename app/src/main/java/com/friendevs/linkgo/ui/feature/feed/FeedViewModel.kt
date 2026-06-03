package com.friendevs.linkgo.ui.feature.feed

import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.friendevs.linkgo.data.repository.FeedRepository
import com.friendevs.linkgo.data.repository.GroupRepository
import com.friendevs.linkgo.domain.model.FeedPost
import com.friendevs.linkgo.domain.model.GroupSummary
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.launch

data class FeedState(
    val posts: List<FeedPost> = emptyList(),
    val myGroups: List<GroupSummary> = emptyList(),
    val selectedGroupId: String? = null,
    val filter: FeedFilter = FeedFilter.ALL,
    val hasRevealed: Boolean = false,
    val isUploading: Boolean = false,
    val error: String? = null
)

/** Filtro del feed: todos los posts o solo los del circulo activo. */
enum class FeedFilter { ALL, CIRCLE }

class FeedViewModel : ViewModel() {

    var state by mutableStateOf(FeedState())
        private set

    private val feedRepo = FeedRepository()
    private val groupRepo = GroupRepository()
    private val auth = FirebaseAuth.getInstance()

    private var feedListener: ValueEventListener? = null
    private var allPosts: List<FeedPost> = emptyList()

    init {
        observeFeed()
        loadGroups()
        refreshRevealState()
    }

    private fun observeFeed() {
        feedListener = feedRepo.observeFeed { posts ->
            allPosts = posts
            applyFilter()
        }
    }

    private fun loadGroups() {
        val uid = auth.currentUser?.uid ?: return
        groupRepo.observeMyGroups(uid) { groups ->
            val selected = state.selectedGroupId?.takeIf { id -> groups.any { it.id == id } }
                ?: groups.firstOrNull()?.id
            state = state.copy(myGroups = groups, selectedGroupId = selected)
            applyFilter()
        }
    }

    fun setFilter(newFilter: FeedFilter) {
        state = state.copy(filter = newFilter)
        applyFilter()
    }

    fun selectGroup(groupId: String) {
        state = state.copy(selectedGroupId = groupId, filter = FeedFilter.CIRCLE)
        applyFilter()
    }

    private fun applyFilter() {
        val visible = when (state.filter) {
            FeedFilter.ALL -> allPosts
            FeedFilter.CIRCLE -> {
                val gid = state.selectedGroupId
                if (gid == null) allPosts else allPosts.filter { it.groupId == gid }
            }
        }
        state = state.copy(posts = visible)
    }

    private fun refreshRevealState() {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            runCatching { feedRepo.hasPostedToday(uid) }
                .onSuccess { state = state.copy(hasRevealed = it) }
        }
    }

    /** Publica una foto. Tras publicar se revela el feed (gating BeReal). */
    fun publishPhoto(fileUri: Uri, caption: String) {
        viewModelScope.launch {
            state = state.copy(isUploading = true, error = null)
            runCatching {
                feedRepo.createPost(fileUri, caption, state.selectedGroupId.orEmpty())
            }.onSuccess {
                state = state.copy(isUploading = false, hasRevealed = true)
            }.onFailure {
                Log.e("FeedViewModel", "Error publicando: ${it.message}")
                state = state.copy(isUploading = false, error = it.message)
            }
        }
    }

    fun toggleLike(postId: String) {
        viewModelScope.launch {
            feedRepo.toggleLike(postId)
        }
    }

    val currentUserId: String?
        get() = auth.currentUser?.uid

    override fun onCleared() {
        super.onCleared()
        feedListener?.let { feedRepo.removeFeedListener(it) }
    }
}
