package com.d101.presentation.main.event

sealed class MainActivityEvent {
    data class ShowErrorEvent(
        val message: String,
    ) : MainActivityEvent()

    data class OnServerMaintaining(val message: String) : MainActivityEvent()
}
