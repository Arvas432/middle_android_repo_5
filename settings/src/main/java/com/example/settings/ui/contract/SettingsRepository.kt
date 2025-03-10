package com.example.settings.ui.contract

import com.example.settings.data_store.SettingContainer
import kotlinx.coroutines.flow.StateFlow

interface SettingsRepository {
    val settingData: StateFlow<SettingContainer>
    suspend fun saveSetting(periodic: Long, delayed: Long)
    suspend fun readSetting()
}