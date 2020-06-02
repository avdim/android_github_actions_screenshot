/*
 * Copyright 2020 Oleksandr Bezushko
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.insiderser.android.template.core.data.prefs

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import androidx.annotation.WorkerThread
import androidx.core.content.edit
import com.insiderser.android.template.core.dagger.IODispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * Storage for app's and user's preferences.
 */
@WorkerThread
interface AppPreferencesStorage {

    /**
     * Theme storage key that is currently selected, or `null` if nothing selected.
     */
    var selectedTheme: String?

    /**
     * A [Flow] with up-to-date theme storage key that is currently selected.
     */
    val selectedThemeObservable: Flow<String?>
}

/**
 * Implementation of [AppPreferencesStorage] that uses [SharedPreferences] to store the data.
 */
@Singleton
class AppPreferencesStorageImpl @Inject constructor(
    context: Context,
    @IODispatcher ioDispatcher: CoroutineDispatcher
) : AppPreferencesStorage {

    private val storageScope = CoroutineScope(ioDispatcher)

    // Lazy to prevent IO on the main thread
    internal val sharedPreferences: SharedPreferences by lazy {
        context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE).apply {
            registerOnSharedPreferenceChangeListener(onChangedListener)
        }
    }

    private val onChangedListener = OnSharedPreferenceChangeListener { _, key ->
        when (key) {
            KEY_THEME -> selectedThemeChannel.offer(selectedTheme)
        }
    }

    override var selectedTheme: String? by StringPreference(KEY_THEME)

    private val selectedThemeChannel = ConflatedBroadcastChannel<String?>()
    override val selectedThemeObservable: Flow<String?>
        get() = selectedThemeChannel.asFlow()

    init {
        storageScope.launch {
            selectedThemeChannel.offer(selectedTheme)
        }
    }

    companion object {

        const val PREFS_NAME = "preferences"

        const val KEY_THEME = "selected_theme"
    }
}

/**
 * Property delegate that manages a single entry in [SharedPreferences].
 */
private class StringPreference(
    private val preferenceKey: String,
    private val defaultValue: String? = null
) : ReadWriteProperty<AppPreferencesStorageImpl, String?> {

    override fun getValue(thisRef: AppPreferencesStorageImpl, property: KProperty<*>): String? =
        thisRef.sharedPreferences.getString(preferenceKey, defaultValue)

    override fun setValue(thisRef: AppPreferencesStorageImpl, property: KProperty<*>, value: String?) {
        Timber.v("Changing value of property $preferenceKey to $value")
        thisRef.sharedPreferences.edit {
            putString(preferenceKey, value)
        }
    }
}
