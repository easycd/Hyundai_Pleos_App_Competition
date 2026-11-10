package com.easycd.nextplace.feature.remind.ui

import com.easycd.nextplace.feature.remind.pipeline.RemindState

data class RemindUiState(val remind: RemindState = RemindState.Hidden)
