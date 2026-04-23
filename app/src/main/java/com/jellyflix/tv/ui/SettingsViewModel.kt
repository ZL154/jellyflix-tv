package com.jellyflix.tv.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jellyflix.tv.data.SettingsStore
import com.jellyflix.tv.plugin.PluginManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val store: SettingsStore,
    private val pluginManager: PluginManager,
) : ViewModel() {

    val prefs: Flow<SettingsStore.Prefs> = store.prefs

    private val _plugins = MutableStateFlow<List<PluginManager.LoadedPlugin>>(emptyList())
    val plugins: StateFlow<List<PluginManager.LoadedPlugin>> = _plugins.asStateFlow()

    init {
        _plugins.value = pluginManager.discover()
    }

    fun setPreferDirectPlay(v: Boolean) = viewModelScope.launch { store.setPreferDirectPlay(v) }
    fun setTunneledPlayback(v: Boolean) = viewModelScope.launch { store.setTunneledPlayback(v) }
    fun setAudioPassthrough(v: Boolean) = viewModelScope.launch { store.setAudioPassthrough(v) }
    fun setMatchRefreshRate(v: Boolean) = viewModelScope.launch { store.setMatchRefreshRate(v) }
    fun setPluginsEnabled(v: Boolean) = viewModelScope.launch { store.setPluginsEnabled(v) }
}
