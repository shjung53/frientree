package com.d101.presentation.mypage.state

sealed class MyPageViewState {
    abstract val id: String
    abstract val nickname: String
    abstract val alarmStatus: AlarmStatus
    abstract val backgroundMusicStatus: BackgroundMusicStatus
    abstract val backgroundMusic: String

    data class Default(
        override val id: String,
        override val nickname: String,
        override val backgroundMusicStatus: BackgroundMusicStatus,
        override val alarmStatus: AlarmStatus,
        override val backgroundMusic: String,
    ) : MyPageViewState()
}
