package com.jellyflix.tv.session

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jellyflix.tv.data.JellyfinClient
import com.jellyflix.tv.data.SessionStore
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@HiltViewModel
class SessionViewModel @Inject constructor(
    private val store: SessionStore,
    private val client: JellyfinClient,
) : ViewModel() {

    enum class Stage { Loading, NeedsServer, NeedsLogin, Authenticated }

    data class State(val stage: Stage = Stage.Loading)

    private val _state = MutableStateFlow(State())
    val state: StateFlow<State> = _state.asStateFlow()

    init { refresh() }

    fun refresh() = viewModelScope.launch {
        val s = store.session.first()
        _state.value = State(
            stage = when {
                s.isAuthenticated -> Stage.Authenticated
                s.hasServer -> Stage.NeedsLogin
                else -> Stage.NeedsServer
            }
        )
    }

    fun onServerSet(url: String) = viewModelScope.launch {
        store.setServer(url)
        client.invalidate()
        refresh()
    }

    fun signOut() = viewModelScope.launch {
        store.clear()
        client.invalidate()
        refresh()
    }

    /** Clear only the server URL so the user can re-enter a different one without nuking everything else. */
    fun clearServer() = viewModelScope.launch {
        store.setServer("")
        client.invalidate()
        refresh()
    }
}
