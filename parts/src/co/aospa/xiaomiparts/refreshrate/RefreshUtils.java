/*
 * Copyright (C) 2020 The LineageOS Project
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

package co.aospa.xiaomiparts.refreshrate;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.UserHandle;
import android.view.Display;

import android.provider.Settings;
import androidx.preference.PreferenceManager;

import java.util.HashMap;
import java.util.Map;

public final class RefreshUtils {

    private static final String REFRESH_CONTROL = "refresh_control";

    private static float defaultMaxRate;
    private static float defaultMinRate;
    private static final String KEY_PEAK_REFRESH_RATE = "peak_refresh_rate";
    private static final String KEY_MIN_REFRESH_RATE = "min_refresh_rate";
    private Context mContext;
    protected static boolean isAppInList = false;

    protected static final int STATE_DEFAULT = 0;
    protected static final int STATE_MEDIUM = 1;
    protected static final int STATE_HIGH = 2;
    protected static final int STATE_EXTREME = 3;
    protected static final int STATE_ANOTHER = 4;
    protected static final int STATE_LOW = 5;

    private static final float REFRESH_STATE_DEFAULT = 144f;
    private static final float REFRESH_STATE_MEDIUM = 60f;
    private static final float REFRESH_STATE_HIGH = 90f;
    private static final float REFRESH_STATE_EXTREME = 120f;
    private static final float REFRESH_STATE_ANOTHER = 144f;
    private static final float REFRESH_STATE_LOW = 30f;

    private static final String[] REFRESH_MODES = {
            "refresh.medium=",
            "refresh.high=",
            "refresh.extreme=",
            "refresh.another=",
            "refresh.low="
    };

    private SharedPreferences mSharedPrefs;

    protected RefreshUtils(Context context) {
        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        mContext = context;
    }

    public static void startService(Context context) {
        context.startServiceAsUser(new Intent(context, RefreshService.class),
                UserHandle.CURRENT);
    }

    private void writeValue(String profiles) {
        mSharedPrefs.edit().putString(REFRESH_CONTROL, profiles).apply();
    }

   protected void getOldRate(){
        defaultMaxRate = Settings.System.getFloat(mContext.getContentResolver(), KEY_PEAK_REFRESH_RATE, REFRESH_STATE_DEFAULT);
        defaultMinRate = Settings.System.getFloat(mContext.getContentResolver(), KEY_MIN_REFRESH_RATE, REFRESH_STATE_DEFAULT);
    }


    private String getValue() {
        String value = mSharedPrefs.getString(REFRESH_CONTROL, null);

        if (value == null || value.isEmpty()) {
            value = String.join(":", REFRESH_MODES);
            writeValue(value);
        }

        String[] parsedModes = value.split(":");
        if (parsedModes.length != REFRESH_MODES.length) {
            Map<String, String> parsedData = new HashMap<>();

            for (String entry : parsedModes) {
                int eqIndex = entry.indexOf("=") + 1;
                if (eqIndex > 0 && eqIndex < entry.length()) {
                    String key = entry.substring(0, eqIndex).trim();
                    String val = entry.substring(eqIndex).trim();
                    parsedData.put(key, val);
                }
            }

            StringBuilder rebasedValue = new StringBuilder();
            for (String mode : REFRESH_MODES) {
                if (rebasedValue.length() > 0) {
                    rebasedValue.append(":");
                }
                rebasedValue.append(mode).append(parsedData.getOrDefault(mode, ""));
            }

            value = rebasedValue.toString();
            writeValue(value);
        }

        return value;
    }

    protected void writePackage(String packageName, int mode) {
        String value = getValue();
        value = value.replace(packageName + ",", "");
        String[] modes = value.split(":");
        String finalString;

        switch (mode) {
            case STATE_MEDIUM:
                modes[0] = modes[0] + packageName + ",";
                break;
            case STATE_HIGH:
                modes[1] = modes[1] + packageName + ",";
                break;
            case STATE_EXTREME:
                modes[2] = modes[2] + packageName + ",";
                break;
            case STATE_ANOTHER:
                modes[3] = modes[3] + packageName + ",";
                break;
            case STATE_LOW:
                modes[4] = modes[4] + packageName + ",";
                break;
        }

        finalString = modes[0] + ":" + modes[1] + ":" + modes[2] + ":" + modes[3] + ":" + modes[4];

        writeValue(finalString);
    }

    protected int getStateForPackage(String packageName) {
        String value = getValue();
        String[] modes = value.split(":");
        int state = STATE_DEFAULT;
        if (modes[0].contains(packageName + ",")) {
            state = STATE_MEDIUM;
        } else if (modes[1].contains(packageName + ",")) {
            state = STATE_HIGH;
        } else if (modes[2].contains(packageName + ",")) {
            state = STATE_EXTREME;
        } else if (modes[3].contains(packageName + ",")) {
            state = STATE_ANOTHER;
        } else if (modes[4].contains(packageName + ",")) {
            state = STATE_LOW;
        }
        return state;
    }

    protected void setRefreshRate(String packageName) {
        String value = getValue();
        String modes[];
        float maxrate = defaultMaxRate;
        float minrate = defaultMinRate;
        isAppInList = false;

        if (value != null) {
            modes = value.split(":");

            if (modes[0].contains(packageName + ",")) {
                maxrate = REFRESH_STATE_MEDIUM;
                if (minrate > maxrate){
                    minrate = maxrate;
                }
		        isAppInList = true;
            } else if (modes[1].contains(packageName + ",")) {
                maxrate = REFRESH_STATE_HIGH;
                if (minrate > maxrate) {
                    minrate = maxrate;
                }
                isAppInList = true;
            } else if (modes[2].contains(packageName + ",")) {
                maxrate = REFRESH_STATE_EXTREME;
                if (minrate > maxrate){
                    minrate = maxrate;
                }
		        isAppInList = true;
            } else if (modes[3].contains(packageName + ",")) {
                maxrate = REFRESH_STATE_ANOTHER;
                if (minrate > maxrate) {
                    minrate = maxrate;
                }
                isAppInList = true;
            } else if (modes[4].contains(packageName + ",")) {
                maxrate = REFRESH_STATE_LOW;
                if (minrate > maxrate) {
                    minrate = maxrate;
                }
                isAppInList = true;
            }
        }
	    Settings.System.putFloat(mContext.getContentResolver(), KEY_MIN_REFRESH_RATE, minrate);
        Settings.System.putFloat(mContext.getContentResolver(), KEY_PEAK_REFRESH_RATE, maxrate);
    }
}
