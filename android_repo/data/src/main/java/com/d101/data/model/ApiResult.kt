package com.d101.data.model

sealed interface ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>

    sealed interface Failure : ApiResult<Nothing> {
        data class HttpError(
            val code: Int,
            val message: String,
        ) : Failure

        data class NetworkError(val throwable: Throwable) : Failure
        data class UnexpectedError(val throwable: Throwable) : Failure

        fun getThrowable(): Throwable =
            when (this) {
                is HttpError -> Throwable(message)
                is NetworkError -> throwable
                is UnexpectedError -> throwable
            }
    }

    val isSuccess: Boolean
        get() = this is Success

    val isFailure: Boolean
        get() = this is Failure

    fun getOrNull(): T? =
        when (this) {
            is Success -> data
            is Failure -> null
        }

    fun getOrThrow(): T =
        when (this) {
            is Success -> data
            is Failure -> throw getThrowable()
        }
}
