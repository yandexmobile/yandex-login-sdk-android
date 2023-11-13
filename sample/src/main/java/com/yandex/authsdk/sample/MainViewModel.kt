package com.yandex.authsdk.sample

import android.app.Application
import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.ActivityResultLauncher
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.yandex.authsdk.YandexAuthException
import com.yandex.authsdk.YandexAuthLoginOptions
import com.yandex.authsdk.YandexAuthOptions
import com.yandex.authsdk.YandexAuthResult
import com.yandex.authsdk.YandexAuthSdk
import com.yandex.authsdk.YandexAuthToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch

class MainViewModel(
    app: Application,
) : AndroidViewModel(app) {

    private val sdk: YandexAuthSdk =
        YandexAuthSdk.create(YandexAuthOptions(app, true))

    private val _progress = MutableStateFlow(false)
    val progress: StateFlow<Boolean> = _progress

    private val _jwtState = MutableStateFlow<Result<String>?>(null)
    val jwtState: Flow<Result<String>> = _jwtState.filterNotNull()

    private val _tokenState = MutableStateFlow<YandexAuthResult?>(null)
    val tokenState: Flow<YandexAuthResult> = _tokenState.filterNotNull()

    fun getTokenLauncher(caller: ActivityResultCaller): ActivityResultLauncher<YandexAuthLoginOptions> {
        return caller.registerForActivityResult(sdk.contract) {
            viewModelScope.launch {
                _tokenState.emit(it)
            }
        }
    }

    fun requestJwt(token: YandexAuthToken) {
        viewModelScope.launch(Dispatchers.IO) {
            _progress.emit(true)
            try {
                val jwt = sdk.getJwt(token)
                _jwtState.emit(Result.success(jwt))
            } catch (e: YandexAuthException) {
                _jwtState.emit(Result.failure(e))
            }
            _progress.emit(false)
        }
    }
}
