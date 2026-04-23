package com.jellyflix.tv.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jellyflix.tv.data.MediaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.jellyfin.sdk.model.api.BaseItemDto

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val repo: MediaRepository,
) : ViewModel() {

    data class State(val items: List<BaseItemDto> = emptyList(), val loading: Boolean = false)

    private val _state = MutableStateFlow(State())
    val state: StateFlow<State> = _state.asStateFlow()

    fun load(libraryId: String) = viewModelScope.launch {
        _state.value = _state.value.copy(loading = true)
        val items = runCatching { repo.itemsIn(UUID.fromString(libraryId)) }.getOrDefault(emptyList())
        _state.value = State(items = items, loading = false)
    }
}
