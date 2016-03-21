/**
 Capturing Audio : a simple audio recorder on the android sd card.
 Copyright (C) 2016 Laurent Bernabé

 This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.

 You should have received a copy of the GNU General Public License along with this program. If not, see http://www.gnu.org/licenses/.
 */
package com.loloof64.android.capturing_audio;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ScrollingView;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements
        RecorderFragment.FileRenamingManager, RecorderFragment.RecordingStatusListener,
        SimpleTimer.TimerListener {

    private ImageButton recordingButton;
    private TextView timerTextView;


    private static final String RECORDER_FRAGMENT_TAG = "RecorderFragment";
    private RecorderFragment recorderFragment;

    private int hours, minutes, seconds;

    private final static String DATE_TIME_FORMAT = "%02d:%02d:%02d";
    private final static String DEFAULT_FILE_NAME_FORMAT = "dd_MM_yyyy-HH_mm_ss";
    private final static String FINAL_FILE_NAME_FORMAT = "%s.mp3";
    private final static String HOURS_TAG = "HOURS";
    private final static String MINUTES_TAG = "MINUTES";
    private final static String SECONDS_TAG = "SECONDS";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recordingButton = (ImageButton) findViewById(R.id.button_recording);
        timerTextView = (TextView) findViewById(R.id.timer_text_view);

        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        audioManager.setMode(AudioManager.MODE_NORMAL);

        FragmentManager fragmentManager = getSupportFragmentManager();
        recorderFragment = (RecorderFragment) fragmentManager.findFragmentByTag(RECORDER_FRAGMENT_TAG);

        SimpleTimer.setTimerListener(this);

        if (recorderFragment == null){
            recorderFragment = new RecorderFragment();
            recorderFragment.setFileRenamingManager(this);
            recorderFragment.setRecordingStatusListener(this);
            fragmentManager.beginTransaction().add(recorderFragment, RECORDER_FRAGMENT_TAG).commit();
        }

        processStatusChange(recorderFragment.isInRecordingStatus());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_activity_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_help:
                Intent intent = new Intent(MainActivity.this, HelpActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void recordingButtonClicked(View view){
        try {
            recorderFragment.toggleRecordingState();
        } catch (IOException e) {
            Log.e("RecordingAudio", e.getMessage(), e);
            ///////////////////////////////////////////////////////////////////////
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
            dialogBuilder.setTitle("Error");

            ScrollView scrollingView = new ScrollView(dialogBuilder.getContext());
            scrollingView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            EditText textView = new EditText(dialogBuilder.getContext());
            textView.setLayoutParams(new ScrollView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            textView.setText(Arrays.toString(e.getStackTrace()));

            scrollingView.addView(textView);
            dialogBuilder.setView(scrollingView);

            dialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            dialogBuilder.show();
            ////////////////////////////////////////////////////////////////////////
            Toast.makeText(this, R.string.could_not_create_temporary_file, Toast.LENGTH_SHORT).show();
        }
    }

    public void purposeFileRenaming(final File externalStorageDir, final File tempAudioFile,
                                    final Calendar captureStartDate){
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setTitle(R.string.renaming_temporary_file);

        final LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.HORIZONTAL);

        final TextView label = new TextView(this);
        label.setText(R.string.defining_file_name_label);
        layout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));

        final EditText input = new EditText(this);
        input.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        input.setInputType(InputType.TYPE_CLASS_TEXT);

        layout.addView(label);
        layout.addView(input);
        dialogBuilder.setView(layout);

        dialogBuilder.setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String defaultNameRadix = new SimpleDateFormat(DEFAULT_FILE_NAME_FORMAT, Locale.getDefault())
                        .format(captureStartDate.getTime());
                String nameRadix = input.getText().toString();

                if (nameRadix.isEmpty()){
                    nameRadix = defaultNameRadix;
                }

                // Append a number to the name radix if necessary
                File testedFile = new File(externalStorageDir, String.format(FINAL_FILE_NAME_FORMAT, nameRadix));
                boolean fileNameAlreadyUsed = testedFile.exists();
                if (fileNameAlreadyUsed){
                    int number = 1;
                    String chosenNameRadix;
                    while(true){
                        chosenNameRadix = String.format(Locale.getDefault(), "%s%d", nameRadix, number);
                        testedFile = new File(externalStorageDir, String.format(FINAL_FILE_NAME_FORMAT, chosenNameRadix));
                        fileNameAlreadyUsed = testedFile.exists();
                        if ( ! fileNameAlreadyUsed ) break;
                        number++;
                    }
                    nameRadix = chosenNameRadix;
                }

                String fileName = String.format(FINAL_FILE_NAME_FORMAT, nameRadix);
                File newFile = new File(externalStorageDir, fileName);
                boolean renameSuccess = tempAudioFile.renameTo(newFile);

                if (renameSuccess) {
                    Toast.makeText(MainActivity.this, R.string.renamed_file, Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(MainActivity.this, R.string.file_renaming_failure, Toast.LENGTH_SHORT).show();
                }
            }
        });

        dialogBuilder.show();
    }

    @Override
    public void processTime(int hours, int minutes, int seconds) {
        this.hours = hours;
        this.minutes = minutes;
        this.seconds = seconds;
        timerTextView.setText(String.format(Locale.getDefault(), DATE_TIME_FORMAT, hours, minutes, seconds));
    }

    @Override
    public void processStatusChange(boolean isRecording) {
        int pictureId = isRecording ? R.mipmap.ic_recording_stop : R.mipmap.ic_recording;
        recordingButton.setImageDrawable(getResources().getDrawable(pictureId));
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(HOURS_TAG, hours);
        outState.putInt(MINUTES_TAG, minutes);
        outState.putInt(SECONDS_TAG, seconds);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        hours = savedInstanceState.getInt(HOURS_TAG);
        minutes = savedInstanceState.getInt(MINUTES_TAG);
        seconds = savedInstanceState.getInt(SECONDS_TAG) + 1;
        timerTextView.setText(String.format(Locale.getDefault(), DATE_TIME_FORMAT, hours, minutes, seconds));
    }

    @Override
    public void onBackPressed() {
        if (recorderFragment.isInRecordingStatus()){
            Toast.makeText(this, R.string.must_stop_recording_before, Toast.LENGTH_LONG).show();
        }
        else {
            super.onBackPressed();
        }
    }
}
