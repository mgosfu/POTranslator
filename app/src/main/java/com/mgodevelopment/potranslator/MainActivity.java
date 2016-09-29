package com.mgodevelopment.potranslator;

import android.app.Dialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.memetix.mst.language.Language;
import com.memetix.mst.translate.Translate;
import com.mgodevelopment.potranslator.adapters.ItemAdapter;
import com.mgodevelopment.potranslator.models.Voices;
import com.mgodevelopment.potranslator.utils.SharedPreferencesUtils;
import com.microsoft.cognitiveservices.speechrecognition.ISpeechRecognitionServerEvents;
import com.microsoft.cognitiveservices.speechrecognition.MicrophoneRecognitionClient;
import com.microsoft.cognitiveservices.speechrecognition.RecognitionResult;
import com.microsoft.cognitiveservices.speechrecognition.RecognitionStatus;
import com.microsoft.cognitiveservices.speechrecognition.SpeechRecognitionMode;
import com.microsoft.cognitiveservices.speechrecognition.SpeechRecognitionServiceFactory;
import com.microsoft.speech.tts.Synthesizer;
import com.microsoft.speech.tts.Voice;

public class MainActivity extends AppCompatActivity
        implements ISpeechRecognitionServerEvents {

    public static final String TAG = "POTranslator";
    private MicrophoneRecognitionClient mMicClient = null;
    private SpeechRecognitionMode mSpeechMode = SpeechRecognitionMode.ShortPhrase;

    private String mLanguageCode = Constants.LANGUAGE_CODES[0];
    private Language mLanguageTranslation = Constants.LANGUAGES[0];
    private String mKey = Constants.PRIMARY_SUBSCRIPTION_KEY;

    private TextView mResultText;
    private FloatingActionButton mFab;

    private ItemAdapter mItemAdapter = new ItemAdapter(this);
    private View mSuggestionLayout;

    private int onlineIcon;
    private int busyIcon;

    private boolean mHasStartedRecording = false;
    private boolean mHasOptionChanged = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        mResultText = (TextView) findViewById(R.id.resultText);
        mSuggestionLayout = findViewById(R.id.suggestionLayout);

        onlineIcon = getResources().getIdentifier("@android:drawable/presence_audio_online", null, null);
        busyIcon = getResources().getIdentifier("@android:drawable/ic_voice_search", null, null);

        mFab = (FloatingActionButton) findViewById(R.id.mFab);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (hasInternetConnection()) {
                    mResultText.setText("");
                    mSuggestionLayout.setVisibility(View.GONE);
                    initRecording();
                    if (mMicClient != null) {
                        if (mSpeechMode.equals(SpeechRecognitionMode.ShortPhrase)) {
                            if (!mHasStartedRecording) {
                                mMicClient.startMicAndRecognition();
                            }
                        } else {
                            if (!mHasStartedRecording) {
                                mMicClient.startMicAndRecognition();
                            } else {
                                mMicClient.endMicAndRecognition();
                            }
                        }
                    }
                } else {
                    Toast.makeText(MainActivity.this, getString(R.string.check_connection), Toast.LENGTH_LONG).show();
                }
            }
        });

        initLanguageSpinner();
        initSpeechModeSpinner();

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

        super.onSaveInstanceState(outState);
        outState.putString("mResultText", mResultText.getText().toString());

    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mResultText.setText(savedInstanceState.getString("mResultText"));
    }

    private boolean hasInternetConnection() {

        ConnectivityManager connectivityManager = ((ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE));
        return connectivityManager != null
                && connectivityManager.getActiveNetworkInfo() != null
                && connectivityManager.getActiveNetworkInfo().isConnected();

    }

    private void initRecording() {

        if (mHasOptionChanged || mMicClient == null) {

            Log.d(TAG, "Language is " + mLanguageCode + "\nSpeech mode is " + mSpeechMode);

            if (mKey.equals(Constants.PRIMARY_SUBSCRIPTION_KEY)) {
                mResultText.append(getString(R.string.primary_connect));
            } else {
                mResultText.append(getString(R.string.secondary_connect));
            }

            mMicClient = SpeechRecognitionServiceFactory.createMicrophoneClient(this, mSpeechMode, mLanguageCode, this, mKey);
            mHasOptionChanged = false;

        }

        // discard previous items
        mItemAdapter.clear();
        // and hide the speaker button
        ImageButton speakButton = (ImageButton) findViewById(R.id.speak_button);
        if (speakButton != null) {
            speakButton.setVisibility(View.GONE);
        }

    }

    private void initLanguageSpinner() {

        final Spinner spinner = (Spinner) findViewById(R.id.language_spinner);
        spinner.setSaveEnabled(true);

        spinner.setSelection(SharedPreferencesUtils.getBaseLanguageIndex(this));
        mLanguageCode = Constants.LANGUAGE_CODES[SharedPreferencesUtils.getBaseLanguageIndex(this)];

        spinner.post(new Runnable() {
            @Override
            public void run() {
                spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                        Log.d(TAG, "in Language Spinner onItemSelected");
                        mLanguageCode = Constants.LANGUAGE_CODES[position];
                        mHasOptionChanged = true;
                        SharedPreferencesUtils.updateBaseLanguageIndex(MainActivity.this, position);

                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                        // no action required, but we must implement this method
                    }
                });
            }
        });

    }

    private void initSpeechModeSpinner() {

        final Spinner spinner = (Spinner) findViewById(R.id.speech_mode_spinner);
        spinner.setSaveEnabled(true);

        int pref = SharedPreferencesUtils.getSpeechModeIndex(this);
        spinner.setSelection(pref);
        mSpeechMode = pref == 0 ? SpeechRecognitionMode.ShortPhrase : SpeechRecognitionMode.LongDictation;

        spinner.post(new Runnable() {
            @Override
            public void run() {
                spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                        Log.d(TAG, "in Speech Mode Spinner onItemSelected");
                        mSpeechMode = position == 0 ? SpeechRecognitionMode.ShortPhrase : SpeechRecognitionMode.LongDictation;
                        mHasOptionChanged = true;
                        SharedPreferencesUtils.updateSpeechModeIndex(MainActivity.this, position);

                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                        // no action required, but we must implement this method
                    }
                });
            }
        });

    }

    @Override
    public void onPartialResponseReceived(String response) {
        mResultText.append("PARTIAL RESULT:\n");
        mResultText.append(response + "\n");
    }

    @Override
    public void onFinalResponseReceived(RecognitionResult recognitionResult) {

        // explanation of results at https://msdn.microsoft.com/en-us/library/mt613453.aspx
        mResultText.setText("");
        boolean isFinalDictationMessage = (
                mSpeechMode == SpeechRecognitionMode.LongDictation
                        && (
                        recognitionResult.RecognitionStatus == RecognitionStatus.EndOfDictation
                                || recognitionResult.RecognitionStatus == RecognitionStatus.DictationEndSilenceTimeout
                                || recognitionResult.RecognitionStatus == RecognitionStatus.RecognitionSuccess
                ));
        if (mSpeechMode == SpeechRecognitionMode.ShortPhrase || isFinalDictationMessage) {

            if (mMicClient != null) {
                mMicClient.endMicAndRecognition();
            }

            mFab.setEnabled(true);
            mFab.setImageResource(onlineIcon);

        }

        if (recognitionResult.Results.length > 0) {

            ListView listView = (ListView) findViewById(R.id.resultList);
            listView.setAdapter(mItemAdapter);
            mSuggestionLayout.setVisibility(View.VISIBLE);

            for (int i = 0; i < recognitionResult.Results.length; i++) {
                mItemAdapter.addItem(recognitionResult.Results[i].DisplayText);
            }

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {

                    final Dialog dialog = new Dialog(MainActivity.this);
                    dialog.setContentView(R.layout.dialog_content);

                    ListView translationList = (ListView) dialog.findViewById(R.id.translation_list);
                    final ItemAdapter translationAdapter = new ItemAdapter(MainActivity.this);
                    translationAdapter.setItems(getResources().getStringArray(R.array.languages));
                    translationList.setAdapter(translationAdapter);
                    translationAdapter.setSelected(SharedPreferencesUtils.getConvertLanguageIndex(MainActivity.this));
                    // Initialize the translation language to the stored preference
                    mLanguageTranslation = Constants.LANGUAGES[SharedPreferencesUtils.getConvertLanguageIndex(MainActivity.this)];

                    translationList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                            mLanguageTranslation = Constants.LANGUAGES[position];
                            SharedPreferencesUtils.updateConvertLanguageIndex(MainActivity.this, position);
                            translationAdapter.setSelected(position);

                        }
                    });

                    dialog.findViewById(R.id.translate_button).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            dialog.dismiss();
                            mResultText.setText("");
                            new TranslationTask(Constants.LANGUAGES[SharedPreferencesUtils.getBaseLanguageIndex(MainActivity.this)],
                                    mLanguageTranslation,
                                    (String) mItemAdapter.getItem(position)).execute();

                        }
                    });

                    dialog.findViewById(R.id.cancel_button).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                        }
                    });

                    dialog.setCancelable(true);
                    dialog.setTitle(getString(R.string.dialog_title));
                    dialog.show();

                }
            });

        }

//        StringBuilder sb = new StringBuilder();
//        if (isFinalDictationMessage) {
//            sb.append("Final Dictation Message");
//        }
//
//        if (recognitionResult.Results.length > 0) {
//            sb.append("Text Suggestions\n");
//            for (int i = 0; i < recognitionResult.Results.length; i++) {
//                sb.append((i + 1) + " " + recognitionResult.Results[i].DisplayText + "\n");
//            }
//            sb.append("\n" + mResultText.getText().toString());
//            mResultText.setText(sb.toString());
//        }

    }

    @Override
    public void onIntentReceived(String response) {
        // We're not using speech recognition with intent (of the speaker),
        // but we must implement all the interface methods.
    }

    @Override
    public void onError(int errorCode, String response) {

        mFab.setEnabled(true);
        mFab.setImageResource(onlineIcon);
        Toast.makeText(this, getString(R.string.internet_error_text), Toast.LENGTH_SHORT).show();
        mResultText.append("Error " + errorCode + ": " + response + "\n");
        mMicClient = null; // Force an initialization when recording next time
        mKey = mKey == Constants.PRIMARY_SUBSCRIPTION_KEY ? Constants.SECONDARY_SUBSCRIPTION_KEY : Constants.PRIMARY_SUBSCRIPTION_KEY;

    }

    @Override
    public void onAudioEvent(boolean isRecording) {

        mHasStartedRecording = isRecording;
        if (!isRecording) {

            if (mMicClient != null) {
                mMicClient.endMicAndRecognition();
            }
            mFab.setEnabled(true);
            mFab.setImageResource(onlineIcon);

        } else {
            if (mSpeechMode == SpeechRecognitionMode.ShortPhrase) {
                mFab.setEnabled(false);
            }
            mFab.setImageResource(busyIcon);
        }

        mResultText.append(isRecording ? getString(R.string.recording_start) : getString(R.string.recording_end));

    }

    private class TranslationTask extends AsyncTask<Void, Void, Void> {

        private final Language baseLanguage;
        private final Language convertLanguage;
        private final String word;
        private String translatedText = "";

        public TranslationTask(Language baseLanguage, Language convertLanguage, String word) {
            this.baseLanguage = baseLanguage;
            this.convertLanguage = convertLanguage;
            this.word = word;
        }

        @Override
        protected void onPreExecute() {

            super.onPreExecute();
            mResultText.append("Word Selected: " + word);
            mResultText.append(getString(R.string.translation_start));

        }

        @Override
        protected Void doInBackground(Void... params) {

            Translate.setClientId(Constants.CLIENT_ID_VALUE);
            Translate.setClientSecret(Constants.CLIENT_SECRET_VALUE);

            try {
                translatedText = Translate.execute(word, baseLanguage, convertLanguage);
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;

        }

        @Override
        protected void onPostExecute(Void aVoid) {

            super.onPostExecute(aVoid);
            mResultText.setText(getString(R.string.translation_header));
            mResultText.append(translatedText);

            // set up the click listener for the Speak button
            ImageButton speakButton = (ImageButton) findViewById(R.id.speak_button);
            if (speakButton != null) {

                speakButton.setVisibility(View.VISIBLE);
                speakButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        // Get the language code that the translation is in
                        String speechLanguage = Constants.LANGUAGE_CODES[SharedPreferencesUtils.getConvertLanguageIndex(MainActivity.this)];
                        Log.d(TAG, "Speech language is: " + speechLanguage);
                        //Synthesizer synthesizer = new Synthesizer(getString(R.string.app_name), Constants.PRIMARY_SUBSCRIPTION_KEY); // deprecated ?
                        Synthesizer synthesizer = new Synthesizer(mKey);
                        Voice voice = Voices.getVoice(speechLanguage, 0);

                        if (voice != null) {
                            Log.d(TAG, voice.voiceName);
                            synthesizer.SetVoice(voice, voice);
                            Log.d(TAG, "Speaking: " + translatedText);
                            synthesizer.SpeakToAudio(translatedText);
                        }

                    }
                });

            }

        }

    }

}