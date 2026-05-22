package com.engineerstech.weathersnap.util

import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class DraftState<T>(
    private val savedStateHandle: SavedStateHandle,
    private val key: String,
    default: T
) {
    private val _flow = MutableStateFlow(savedStateHandle.get<T>(key) ?: default)
    val flow: StateFlow<T> = _flow.asStateFlow()

    fun update(value: T) {
        _flow.value = value
        savedStateHandle[key] = value
    }

    fun clear(default: T) {
        _flow.value = default
        savedStateHandle.remove<T>(key)
    }
}
