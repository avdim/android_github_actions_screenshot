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

package com.insiderser.android.template.core.domain.prefs.theme

import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.Q
import androidx.appcompat.app.AppCompatDelegate

/**
 * Enum that represents the theme of our app.
 *
 * @param storageKey Key used to serialize & deserialize the given [Theme].
 */
enum class Theme(val storageKey: String) {
    /** Always use the day (light) theme. */
    LIGHT(KEY_LIGHT),

    /** Always use the night (dark) theme. */
    DARK(KEY_DARK),

    /**
     * (default) This setting follows the system’s setting,
     * which on Android Q and above is a system setting.
     */
    FOLLOW_SYSTEM(KEY_SYSTEM),

    /** Changes to dark when the device has its ‘Battery Saver’ feature enabled, light otherwise. */
    AUTO_BATTERY(KEY_BATTERY);

    companion object {

        /**
         * Find [Theme] for the given [Theme.storageKey].
         * @throws NoSuchElementException if not found.
         */
        @JvmStatic
        fun fromStorageKey(storageKey: String): Theme =
            values().first { it.storageKey == storageKey }
    }
}

private const val KEY_LIGHT = "light"
private const val KEY_DARK = "dark"
private const val KEY_SYSTEM = "system"
private const val KEY_BATTERY = "battery"

val DEFAULT_THEME = if (SDK_INT >= Q) Theme.FOLLOW_SYSTEM else Theme.AUTO_BATTERY

/**
 * Get [AppCompatDelegate] night mode for the given [Theme].
 */
fun Theme.toAppCompatNightMode(): Int = when (this) {
    Theme.LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
    Theme.DARK -> AppCompatDelegate.MODE_NIGHT_YES
    Theme.FOLLOW_SYSTEM -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
    Theme.AUTO_BATTERY -> AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY
}
