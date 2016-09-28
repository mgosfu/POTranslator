package com.mgodevelopment.potranslator;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Martin on 9/27/2016.
 */

public class SharedPreferencesUtils {

    public static int getSpeechModeIndex(Context context) {

        SharedPreferences preferences = context.getSharedPreferences(Constants.SPEECH_TO_TEXT_PREFERENCES,
                Context.MODE_PRIVATE);
        return preferences.getInt(Constants.SPEECH_MODE_INDEX, 0);

    }

    public static void updateSpeechModeIndex(Context context, int speechModeIndex) {

        SharedPreferences preferences = context.getSharedPreferences(Constants.SPEECH_TO_TEXT_PREFERENCES,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(Constants.SPEECH_MODE_INDEX, speechModeIndex);
        editor.apply();

    }

    public static int getBaseLanguageIndex(Context context) {

        SharedPreferences preferences = context.getSharedPreferences(Constants.SPEECH_TO_TEXT_PREFERENCES,
                Context.MODE_PRIVATE);
        return preferences.getInt(Constants.BASE_LANGUAGE_INDEX, 0);

    }

    public static void updateBaseLanguageIndex(Context context, int languageIndex) {

        SharedPreferences preferences = context.getSharedPreferences(Constants.SPEECH_TO_TEXT_PREFERENCES,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(Constants.BASE_LANGUAGE_INDEX, languageIndex);
        editor.apply();

    }

}
