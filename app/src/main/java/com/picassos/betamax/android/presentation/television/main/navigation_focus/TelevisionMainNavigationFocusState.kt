package com.picassos.betamax.android.presentation.television.main.navigation_focus

data class TelevisionMainNavigationFocusState(
    val isNavigationMoviesFocused: Boolean = false,
    val isNavigationSeriesFocused: Boolean = false,
    val isNavigationLiveTvFocused: Boolean = false,
    val isNavigationMyListFocused: Boolean = false,
    val isNavigationProfileFocused: Boolean = false)