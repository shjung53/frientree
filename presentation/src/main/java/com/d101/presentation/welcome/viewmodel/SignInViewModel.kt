package com.d101.presentation.welcome.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.d101.domain.model.Result
import com.d101.domain.model.status.ErrorStatus
import com.d101.domain.model.status.SignInErrorStatus
import com.d101.domain.usecase.usermanagement.GetNaverIdUseCase
import com.d101.domain.usecase.usermanagement.SignInByFrientreeUseCase
import com.d101.domain.usecase.usermanagement.SignInNaverUseCase
import com.d101.presentation.welcome.event.SignInViewEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import utils.MutableEventFlow
import utils.asEventFlow
import javax.inject.Inject

@HiltViewModel
class SignInViewModel @Inject constructor(
    private val signInByFrientreeUseCase: SignInByFrientreeUseCase,
    private val signInNaverUseCase: SignInNaverUseCase,
    private val getNaverIdUseCase: GetNaverIdUseCase,
) : ViewModel() {

    val id = MutableStateFlow("")
    val password = MutableStateFlow("")

    private val _eventFlow = MutableEventFlow<SignInViewEvent>()
    val eventFlow = _eventFlow.asEventFlow()

    fun signInByFrientree() {
        viewModelScope.launch {
            when (val result = signInByFrientreeUseCase(id.value, password.value)) {
                is Result.Success -> onSignInSuccess()

                is Result.Failure -> {
                    when (val errorStatus = result.errorStatus) {
                        ErrorStatus.ServerMaintenance() -> emitEvent(
                            SignInViewEvent.OnServerMaintaining(errorStatus.message),
                        )
                        ErrorStatus.NetworkError() -> onSignInFailed(errorStatus.message)
                        SignInErrorStatus.UserNotFound() -> onSignInFailed(errorStatus.message)
                        SignInErrorStatus.WrongPassword() -> onSignInFailed(errorStatus.message)
                        else -> onSignInFailed("로그인에 실패했습니다.")
                    }
                }
            }
        }
    }

    private fun emitEvent(event: SignInViewEvent) {
        viewModelScope.launch {
            _eventFlow.emit(event)
        }
    }

    fun onSignUpClicked() {
        emitEvent(SignInViewEvent.SignUpClicked)
    }

    fun onFindPasswordClicked() {
        emitEvent(SignInViewEvent.FindPasswordClicked)
    }

    fun onSignInAttemptByFrientree() {
        emitEvent(SignInViewEvent.SignInAttemptByFrientree)
    }

    fun onSignInAttemptByNaver() {
        emitEvent(SignInViewEvent.SignInAttemptByNaver)
    }

    private fun onSignInSuccess() {
        emitEvent(SignInViewEvent.SignInSuccess)
    }

    private fun onSignInFailed(message: String) {
        emitEvent(SignInViewEvent.SignInFailed(message))
    }

    fun onNaverSignInCompleted(loginResult: Result<String>) {
        viewModelScope.launch(Dispatchers.IO) {
            when (loginResult) {
                is Result.Success -> {
                    getNaverUserId(loginResult.data)
                }
                is Result.Failure -> onSignInFailed("네이버 로그인 실패")
            }
        }
    }

    private suspend fun getNaverUserId(accessToken: String) {
        when (val result = getNaverIdUseCase(accessToken)) {
            is Result.Success -> signInByNaver(result.data)
            is Result.Failure -> { onSignInFailed("네이버 로그인 실패") }
        }
    }

    private suspend fun signInByNaver(naverId: String) {
        when (val result = signInNaverUseCase(naverId)) {
            is Result.Success -> {
                onSignInSuccess()
            }

            is Result.Failure -> {
                when (val errorStatus = result.errorStatus) {
                    ErrorStatus.ServerMaintenance() -> emitEvent(
                        SignInViewEvent.OnServerMaintaining(errorStatus.message),
                    )
                    ErrorStatus.NetworkError() -> onSignInFailed(errorStatus.message)
                    else -> onSignInFailed("로그인에 실패했습니다.")
                }
            }
        }
    }
}
