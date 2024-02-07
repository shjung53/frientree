package com.d101.presentation.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.d101.domain.model.Result
import com.d101.domain.usecase.usermanagement.GetUserInfoUseCase
import com.d101.domain.usecase.appStatus.GetAppStatusUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import utils.MutableEventFlow
import utils.asEventFlow
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val getUserInfoUseCase: GetUserInfoUseCase,
    private val getAppStatusUseCase: GetAppStatusUseCase,
) : ViewModel() {

    private val _eventFlow = MutableEventFlow<SplashViewEvent>()
    val eventFlow = _eventFlow.asEventFlow()

    init {
        onSplashShow()
    }

    fun showSplash() {
        viewModelScope.launch(Dispatchers.IO) {
            delay(3_000L)
            checkAppVersion()
        }
    }

    private suspend fun checkAppVersion() {
        when (val result = getAppStatusUseCase()) {
            is Result.Success -> emitEvent(
                SplashViewEvent.CheckAppStatus(
                    result.data.appAvailable,
                    result.data.minVersion,
                    result.data.storeUrl,
                ),
            )

            is Result.Failure -> emitEvent(SplashViewEvent.OnFailCheckAppStatus)
        }
    }

    private fun checkSignInStatus() {
        viewModelScope.launch {
            getUserInfoUseCase().collect {
                when (it) {
                    is Result.Success -> {
                        if (it.data.isBackgroundMusicEnabled) {
                            onSetBackGroundMusic(it.data.backgroundMusicName)
                        }
                        onSignInSuccess()
                    }
                    is Result.Failure -> {
                        onSignInFailed()
                    }
                }
            }
        }
    }
    private fun emitEvent(event: SplashViewEvent) {
        viewModelScope.launch {
            _eventFlow.emit(event)
        }
    }

    private fun onSignInSuccess() {
        emitEvent(SplashViewEvent.AutoSignInSuccess)
    }

    private fun onSetBackGroundMusic(musicName: String) {
        emitEvent(SplashViewEvent.SetBackGroundMusic(musicName))
    }

    private fun onSignInFailed() {
        emitEvent(SplashViewEvent.AutoSignInFailure)
    }

    private fun onSplashShow() {
        emitEvent(SplashViewEvent.ShowSplash)
    }
}
