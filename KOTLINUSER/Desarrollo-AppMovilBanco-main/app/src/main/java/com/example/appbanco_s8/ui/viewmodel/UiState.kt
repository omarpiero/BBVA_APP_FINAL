package com.example.appbanco_s8.ui.viewmodel


sealed class DataUiState<out T> {
    object Loading                        : DataUiState<Nothing>()
    data class Success<T>(val data: T)    : DataUiState<T>()
    data class Error(val mensaje: String) : DataUiState<Nothing>()
}