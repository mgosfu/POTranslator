package com.mgodevelopment.potranslator;

import com.memetix.mst.language.Language;

/**
 * Created by Martin on 9/26/2016.
 */

public class Constants {

    public static final String PRIMARY_SUBSCRIPTION_KEY = "1f1b7ba8b5aa43c89d3480153ff8f591";
    public static final String SECONDARY_SUBSCRIPTION_KEY = "cc40367652814432b5c9c82660603b97";

    public static final String[] LANGUAGE_CODES = {
            "en-us",
            "en-gb",
            "fr-fr",
            "de-de",
            "it-it",
            "zh-cn",
            "es-es"
    };

    public static final String SPEECH_TO_TEXT_PREFERENCES = "SpeechToTextPreferences";
    public static final String SPEECH_MODE_INDEX = "SpeechModeIndex";
    public static final String BASE_LANGUAGE_INDEX = "BaseLanguageIndex";
    public static final String CONVERT_LANGUAGE_INDEX = "ConvertLanguageIndex";

    public static final String CLIENT_ID_VALUE = "com_mgodevelopment_speechtotext";
    public static final String CLIENT_SECRET_VALUE = "WEVCvnqBJeX4astpyIel816Qfl4k6vvCfB//Gbo4PzI=";

    public static final Language[] LANGUAGES = {
            Language.ENGLISH,
            Language.ENGLISH,
            Language.FRENCH,
            Language.GERMAN,
            Language.ITALIAN,
            Language.CHINESE_TRADITIONAL,
            Language.SPANISH
    };
}
