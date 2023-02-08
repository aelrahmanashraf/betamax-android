package com.picassos.betamax.android.data.source.local.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "app_data")

@Singleton
class SharedDataManager @Inject constructor(@ApplicationContext val context: Context) {
    val dataStore = context.dataStore
}