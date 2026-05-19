package com.ben.inly.data.local.prefs

import android.content.SharedPreferences
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class AndroidSettingsManager(
    private val sharedPreferences: SharedPreferences
) : SettingsManager {

    override val sortTypeFlow: Flow<String> = callbackFlow {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { prefs, key ->
            if (key == SyncConstants.KEY_SORT_TYPE) {
                trySend(prefs.getString(SyncConstants.KEY_SORT_TYPE, SyncConstants.DEFAULT_SORT_TYPE) ?: SyncConstants.DEFAULT_SORT_TYPE)
            }
        }
        sharedPreferences.registerOnSharedPreferenceChangeListener(listener)

        trySend(sharedPreferences.getString(SyncConstants.KEY_SORT_TYPE, SyncConstants.DEFAULT_SORT_TYPE) ?: SyncConstants.DEFAULT_SORT_TYPE)

        awaitClose { sharedPreferences.unregisterOnSharedPreferenceChangeListener(listener) }
    }

    override val sortOrderFlow: Flow<String> = callbackFlow {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { prefs, key ->
            if (key == SyncConstants.KEY_SORT_ORDER) {
                trySend(prefs.getString(SyncConstants.KEY_SORT_ORDER, SyncConstants.DEFAULT_SORT_ORDER) ?: SyncConstants.DEFAULT_SORT_ORDER)
            }
        }
        sharedPreferences.registerOnSharedPreferenceChangeListener(listener)

        trySend(sharedPreferences.getString(SyncConstants.KEY_SORT_ORDER, SyncConstants.DEFAULT_SORT_ORDER) ?: SyncConstants.DEFAULT_SORT_ORDER)

        awaitClose { sharedPreferences.unregisterOnSharedPreferenceChangeListener(listener) }
    }

    override val lastOpenedDesktopStateFlow: Flow<String> = callbackFlow {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { prefs, key ->
            if (key == SyncConstants.KEY_LAST_OPENED_STATE) {
                trySend(prefs.getString(SyncConstants.KEY_LAST_OPENED_STATE, "") ?: "")
            }
        }
        sharedPreferences.registerOnSharedPreferenceChangeListener(listener)

        trySend(sharedPreferences.getString(SyncConstants.KEY_LAST_OPENED_STATE, "") ?: "")

        awaitClose { sharedPreferences.unregisterOnSharedPreferenceChangeListener(listener) }
    }

    override fun saveSortSettings(type: String, order: String) {
        sharedPreferences.edit()
            .putString(SyncConstants.KEY_SORT_TYPE, type)
            .putString(SyncConstants.KEY_SORT_ORDER, order)
            .apply()
    }

    override fun saveLastOpenedDesktopState(state: String) {
        sharedPreferences.edit()
            .putString(SyncConstants.KEY_LAST_OPENED_STATE, state)
            .apply()
    }

    override fun getLastSyncTimestamp(): Long {
        return sharedPreferences.getLong(SyncConstants.KEY_SYNC_TIMESTAMP, 0L)
    }

    override fun saveLastSyncTimestamp(timestamp: Long) {
        sharedPreferences.edit().putLong(SyncConstants.KEY_SYNC_TIMESTAMP, timestamp).apply()
    }

    override fun getSyncAuthToken(): String {
        return sharedPreferences.getString(SyncConstants.KEY_SYNC_AUTH_TOKEN, "") ?: ""
    }

    override fun saveSyncAuthToken(token: String) {
        sharedPreferences.edit().putString(SyncConstants.KEY_SYNC_AUTH_TOKEN, token).apply()
    }

    override fun getSyncIpAddress(): String {
        return sharedPreferences.getString(SyncConstants.KEY_SYNC_IP_ADDRESS, "") ?: ""
    }

    override fun saveSyncIpAddress(ip: String) {
        sharedPreferences.edit().putString(SyncConstants.KEY_SYNC_IP_ADDRESS, ip).apply()
    }

    override fun getSyncPort(): Int {
        return sharedPreferences.getInt(SyncConstants.KEY_SYNC_PORT, SyncConstants.DEFAULT_PORT)
    }

    override fun saveSyncPort(port: Int) {
        sharedPreferences.edit().putInt(SyncConstants.KEY_SYNC_PORT, port).apply()
    }

    override fun getSyncEncryptionKey(): String {
        return sharedPreferences.getString(SyncConstants.KEY_SYNC_ENCRYPTION_KEY, "") ?: ""
    }

    override fun saveSyncEncryptionKey(key: String) {
        sharedPreferences.edit().putString(SyncConstants.KEY_SYNC_ENCRYPTION_KEY, key).apply()
    }
}