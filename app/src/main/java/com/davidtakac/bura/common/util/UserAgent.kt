/*
 * Copyright 2024 David Takač
 *
 * This file is part of Bura.
 *
 * Bura is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * Bura is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Bura. If not, see <https://www.gnu.org/licenses/>.
 */

package com.davidtakac.bura.common.util

import android.content.Context

fun getPackageName(context: Context): String =
    context.packageName

fun getAppVersionName(context: Context): String =
    context.packageManager.getPackageInfo(
        getPackageName(context),
        0
    ).versionName!!

fun getUserAgent(context: Context): String =
    "Bura/${getPackageName(context)}/${getAppVersionName(context)} (https://github.com/davidtakac/bura)"