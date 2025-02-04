/*
 * Copyright (C) 2018 The LineageOS Project
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

package co.aospa.xiaomiparts;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import android.util.Log;

import co.aospa.xiaomiparts.thermal.ThermalUtils;
import co.aospa.xiaomiparts.refreshrate.RefreshUtils;
import co.aospa.xiaomiparts.display.ColorService;

public class BootCompletedReceiver extends BroadcastReceiver {

    private static final String TAG = "XiaomiParts-BCR";

    @Override
    public void onReceive(final Context context, Intent intent) {
        Log.d(TAG, "Received intent: " + intent.getAction());
        if (!intent.getAction().equals(Intent.ACTION_LOCKED_BOOT_COMPLETED)) {
            return;
        }

        Log.i(TAG, "Boot completed, starting services");
        ThermalUtils.startService(context);
        RefreshUtils.startService(context);
        ColorService.startService(context);
    }

}