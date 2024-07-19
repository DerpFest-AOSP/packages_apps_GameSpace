/*
 * Copyright (C) 2021 Chaldeaprjkt
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.chaldeaprjkt.gamespace.data

import android.content.Context
import android.os.RemoteException;
import android.os.UserHandle
import android.provider.Settings
import android.util.Log;
import io.chaldeaprjkt.gamespace.utils.GameModeUtils
import javax.inject.Inject

import vendor.lineage.fastcharge.V1_0.IFastCharge

class SystemSettings @Inject constructor(
    context: Context,
    private val gameModeUtils: GameModeUtils
) {
    private val TAG = "GameSpaceSystemSettings"

    private val resolver = context.contentResolver
    private var mFastCharge: IFastCharge? = null

    var headsUp
        get() =
            Settings.Global.getInt(resolver, Settings.Global.HEADS_UP_NOTIFICATIONS_ENABLED, 1) == 1
        set(it) {
            Settings.Global.putInt(
                resolver,
                Settings.Global.HEADS_UP_NOTIFICATIONS_ENABLED,
                it.toInt()
            )
        }

    var island
        get() =
            Settings.System.getIntForUser(
                resolver, Settings.System.ISLAND_NOTIFICATION, 0,
                UserHandle.USER_CURRENT) == 1
        set(it) {
            Settings.System.putIntForUser(
                resolver,
                Settings.System.ISLAND_NOTIFICATION,
                it.toInt(),
                UserHandle.USER_CURRENT
            )
        }

    var autoBrightness
        get() =
            Settings.System.getIntForUser(
                resolver,
                Settings.System.SCREEN_BRIGHTNESS_MODE,
                Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC,
                UserHandle.USER_CURRENT
            ) ==
                    Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC
        set(auto) {
            Settings.System.putIntForUser(
                resolver,
                Settings.System.SCREEN_BRIGHTNESS_MODE,
                if (auto) Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC
                else Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL,
                UserHandle.USER_CURRENT
            )
        }

    var threeScreenshot
        get() = Settings.System.getIntForUser(
            resolver, Settings.System.THREE_FINGER_GESTURE, 0,
            UserHandle.USER_CURRENT
        ) == 1
        set(it) {
            Settings.System.putIntForUser(
                resolver, Settings.System.THREE_FINGER_GESTURE,
                it.toInt(), UserHandle.USER_CURRENT
            )
        }

    var suppressFullscreenIntent
        get() = Settings.System.getIntForUser(
            resolver,
            Settings.System.GAMESPACE_SUPPRESS_FULLSCREEN_INTENT,
            0,
            UserHandle.USER_CURRENT
        ) == 1
        set(it) {
            Settings.System.putIntForUser(
                resolver,
                Settings.System.GAMESPACE_SUPPRESS_FULLSCREEN_INTENT,
                it.toInt(),
                UserHandle.USER_CURRENT
            )
        }

    var userGames
        get() =
            Settings.System.getStringForUser(
                resolver, Settings.System.GAMESPACE_GAME_LIST,
                UserHandle.USER_CURRENT
            )
                ?.split(";")
                ?.toList()?.filter { it.isNotEmpty() }
                ?.map { UserGame.fromSettings(it) } ?: emptyList()
        set(games) {
            Settings.System.putStringForUser(
                resolver,
                Settings.System.GAMESPACE_GAME_LIST,
                if (games.isEmpty()) "" else
                    games.joinToString(";") { it.toString() },
                UserHandle.USER_CURRENT
            )
            gameModeUtils.setupBatteryMode(games.isNotEmpty())
        }

    var doubleTapToSleep
        get() = Settings.System.getIntForUser(
                resolver, Settings.System.DOUBLE_TAP_SLEEP_GESTURE,1,
                UserHandle.USER_CURRENT
            )==1
            set(it){
                Settings.System.putIntForUser(
                    resolver,Settings.System.DOUBLE_TAP_SLEEP_GESTURE,
                    it.toInt(),UserHandle.USER_CURRENT
            )
        }

    var fastChargeDisabler
        get() = try {
            mFastCharge?.isEnabled() ?: true
        } catch (e: RemoteException) {
            Log.e(TAG, "Failed to get fast charge state", e)
        }
        set(it) {
            try {
                mFastCharge?.setEnabled(false)
            } catch (e: RemoteException) {
                Log.e(TAG, "Failed to disable fast charge", e)
            }
        }
    private fun Boolean.toInt() = if (this) 1 else 0
}
