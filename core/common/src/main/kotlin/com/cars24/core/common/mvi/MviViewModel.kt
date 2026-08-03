package com.cars24.core.common.mvi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

abstract class MviViewModel<I : MviIntent, S : MviState, E : MviEffect>(
    initialState: S,
) : ViewModel() {

    private val _state = MutableStateFlow(initialState)
    val state: StateFlow<S> = _state.asStateFlow()

    private val _effects = Channel<E>(Channel.BUFFERED)
    val effects: Flow<E> = _effects.receiveAsFlow()

    private val intents = Channel<I>(Channel.UNLIMITED)

    val currentState: S get() = _state.value

    init {
        viewModelScope.launch {
            for (intent in intents) {
                handleIntent(intent)
            }
        }
    }

    fun dispatch(intent: I) {
        intents.trySend(intent)
    }

    protected abstract suspend fun handleIntent(intent: I)

    protected fun setState(reducer: S.() -> S) {
        _state.update(reducer)
    }

    protected suspend fun emitEffect(effect: E) {
        _effects.send(effect)
    }

    override fun onCleared() {
        intents.close()
        _effects.close()
        super.onCleared()
    }
}
