package com.d101.presentation.mypage.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.d101.domain.model.Result
import com.d101.domain.model.status.ErrorStatus
import com.d101.domain.model.status.PassWordChangeErrorStatus
import com.d101.domain.usecase.mypage.ChangePasswordUseCase
import com.d101.presentation.R
import com.d101.presentation.mypage.event.PasswordChangeEvent
import com.d101.presentation.mypage.state.PasswordChangeState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import utils.RegexPattern.PASSWORD_PATTERN
import javax.inject.Inject

@HiltViewModel
class PasswordChangeViewModel @Inject constructor(
    private val changePasswordUseCase: ChangePasswordUseCase,
) : ViewModel() {

    private val _uiState: MutableStateFlow<PasswordChangeState> =
        MutableStateFlow(PasswordChangeState())
    val uiState = _uiState.asStateFlow()

    private val _eventEvent: MutableStateFlow<PasswordChangeEvent> =
        MutableStateFlow(PasswordChangeEvent.PasswordChangeAttempt)
    val eventEvent = _eventEvent.asStateFlow()

    fun setCurrentPassword(currentPassword: String) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(currentPassword = currentPassword)
            }
        }
    }

    fun setNewPassword(newPassword: String) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    newPassword = newPassword,
                    newPasswordDescription = if (
                        newPassword.matches(PASSWORD_PATTERN.toRegex()) || newPassword.isEmpty()
                    ) {
                        R.string.empty_text
                    } else {
                        R.string.example_password
                    },
                )
            }
        }
    }

    fun setConfirmPassword(confirmPassword: String) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    confirmPassword = confirmPassword,
                    passwordConfirmDescription = if (confirmPassword == uiState.value.newPassword) {
                        R.string.empty_text
                    } else {
                        R.string.password_mismatch
                    },
                )
            }
        }
    }

    fun changePassword() {
        viewModelScope.launch {
            when (
                val result = changePasswordUseCase(
                    uiState.value.currentPassword,
                    uiState.value.newPassword,
                )
            ) {
                is Result.Success -> onSuccessPasswordChange()

                is Result.Failure -> {
                    when (result.errorStatus) {
                        PassWordChangeErrorStatus.PasswordPatternMismatch -> {
                            _uiState.update { currentState ->
                                currentState.copy(
                                    currentPasswordDescription = R.string.password_mismatch_in_use,
                                )
                            }
                        }

                        ErrorStatus.NetworkError -> onShowToast("네트워크 오류입니다.")
                        else -> onShowToast("알 수 없는 오류입니다.")
                    }
                }
            }
        }
    }

    fun onAttemptPasswordChange() = emitEvent(PasswordChangeEvent.PasswordChangeAttempt)
    private fun onSuccessPasswordChange() = emitEvent(PasswordChangeEvent.PasswordChangeSuccess)

    private fun onShowToast(message: String) = emitEvent(PasswordChangeEvent.ShowToast(message))
    private fun emitEvent(event: PasswordChangeEvent) {
        viewModelScope.launch {
            _eventEvent.emit(event)
        }
    }
}
