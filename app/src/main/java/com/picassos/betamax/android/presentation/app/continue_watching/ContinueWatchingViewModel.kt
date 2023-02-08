package com.picassos.betamax.android.presentation.app.continue_watching

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.picassos.betamax.android.core.resource.Resource
import com.picassos.betamax.android.domain.usecase.continue_watching.ContinueWatchingUseCases
import com.picassos.betamax.android.presentation.app.continue_watching.delete_continue_watching.DeleteContinueWatchingState
import com.picassos.betamax.android.presentation.app.continue_watching.update_continue_watching.UpdateContinueWatchingState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ContinueWatchingViewModel @Inject constructor(val app: Application, private val continueWatchingUseCases: ContinueWatchingUseCases): AndroidViewModel(app) {
    private val _continueWatching = MutableStateFlow(ContinueWatchingState())
    val continueWatching = _continueWatching.asStateFlow()

    fun requestContinueWatching() {
        viewModelScope.launch {
            continueWatchingUseCases.getLocalAccountUseCase.invoke().collect { account ->
                continueWatchingUseCases.getContinueWatchingUseCase(account.token).collect { result ->
                    when (result) {
                        is Resource.Loading -> {
                            _continueWatching.emit(ContinueWatchingState(
                                isLoading = result.isLoading))
                        }
                        is Resource.Success -> {
                            _continueWatching.emit(ContinueWatchingState(
                                response = result.data))
                        }
                        is Resource.Error -> {
                            _continueWatching.emit(ContinueWatchingState(
                                error = result.message))
                        }
                    }
                }
            }
        }
    }

    private val _updateContinueWatching = MutableStateFlow(UpdateContinueWatchingState())
    val updateContinueWatching = _updateContinueWatching.asStateFlow()

    fun requestUpdateContinueWatching(contentId: Int, url: String, thumbnail: String, currentPosition: Int) {
        viewModelScope.launch {
            continueWatchingUseCases.getLocalAccountUseCase.invoke().collect { account ->
                continueWatchingUseCases.updateContinueWatchingUseCase(account.token, contentId, url, thumbnail, currentPosition).collect { result ->
                    when (result) {
                        is Resource.Loading -> {
                            _updateContinueWatching.emit(UpdateContinueWatchingState(
                                isLoading = result.isLoading))
                        }
                        is Resource.Success -> {
                            _updateContinueWatching.emit(UpdateContinueWatchingState(
                                responseCode = result.data))
                        }
                        is Resource.Error -> {
                            _updateContinueWatching.emit(UpdateContinueWatchingState(
                                error = result.message))
                        }
                    }
                }
            }
        }
    }

    private val _deleteContinueWatching = MutableStateFlow(DeleteContinueWatchingState())
    val deleteContinueWatching = _deleteContinueWatching.asStateFlow()

    fun requestDeleteContinueWatching(contentId: Int) {
        viewModelScope.launch {
            continueWatchingUseCases.getLocalAccountUseCase.invoke().collect { account ->
                continueWatchingUseCases.deleteContinueWatchingUseCase(account.token, contentId).collect { result ->
                    when (result) {
                        is Resource.Loading -> {
                            _deleteContinueWatching.emit(DeleteContinueWatchingState(
                                isLoading = result.isLoading))
                        }
                        is Resource.Success -> {
                            _deleteContinueWatching.emit(DeleteContinueWatchingState(
                                responseCode = result.data))
                        }
                        is Resource.Error -> {
                            _deleteContinueWatching.emit(DeleteContinueWatchingState(
                                error = result.message))
                        }
                    }
                }
            }
        }
    }
}