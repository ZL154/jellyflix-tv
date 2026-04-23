package com.jellyflix.tv.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jellyflix.tv.data.MediaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.jellyfin.sdk.model.api.BaseItemDto
import timber.log.Timber

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repo: MediaRepository,
) : ViewModel() {

    data class State(
        val continueWatching: List<BaseItemDto> = emptyList(),
        val nextUp: List<BaseItemDto> = emptyList(),
        val libraries: List<BaseItemDto> = emptyList(),
        val loading: Boolean = true,
        val error: String? = null,
    )

    private val _state = MutableStateFlow(State())
    val state: StateFlow<State> = _state.asStateFlow()

    init { load() }

    fun load() = viewModelScope.launch {
        _state.value = _state.value.copy(loading = true, error = null)
        try {
            val libs = runCatching { repo.libraries() }.getOrDefault(emptyList())
            val cw = runCatching { repo.continueWatching() }.getOrDefault(emptyList())
            val nu = runCatching { repo.nextUp() }.getOrDefault(emptyList())
            _state.value = State(continueWatching = cw, nextUp = nu, libraries = libs, loading = false)
        } catch (t: Throwable) {
            Timber.e(t, "Failed to load home")
            _state.value = _state.value.copy(loading = false, error = t.localizedMessage)
        }
    }
}
