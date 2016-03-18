/**
 Capturing Audio : a simple audio recorder on the android sd card.
 Copyright (C) 2016 Laurent Bernabé

 This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.

 You should have received a copy of the GNU General Public License along with this program. If not, see http://www.gnu.org/licenses/.
 */
package com.loloof64.android.capturing_audio;

import android.media.MediaRecorder;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;

public class RecorderFragment extends Fragment {


    private MediaRecorder mediaRecorder;
    private boolean isRecording;

    private File tempAudioFile;
    private File externalStorageDir;
    private Calendar captureStartDate;

    private FileRenamingManager fileRenamingManager;
    private RecordingStatusListener recordingStatusListener;

    public interface FileRenamingManager {
        void purposeFileRenaming(final File externalStorageDir, final File tempAudioFile,
                                 final Calendar captureStartDate);
    }

    public interface RecordingStatusListener {
        void processStatusChange(boolean isRecording);
    }

    public void setFileRenamingManager(FileRenamingManager manager){
        fileRenamingManager = manager;
    }

    public void setRecordingStatusListener(RecordingStatusListener listener) {
        recordingStatusListener = listener;
    }

    public boolean isInRecordingStatus() {
        return isRecording;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    public void toggleRecordingState() throws IOException {
        if (!isRecording){
            String extStoragePath = getExternalStoragePath();
            File externalStorageFile = new File(extStoragePath);
            externalStorageDir = new File(externalStorageFile, "CapturingAudio");
            externalStorageDir.mkdir();
            tempAudioFile = File.createTempFile("tmpAudio", ".tmp", externalStorageDir);
            captureStartDate = Calendar.getInstance();
            startRecording(tempAudioFile.getAbsolutePath());
        }
        else {
            stopRecording();
            recordingStatusChanged(false);
        }
    }

    private String getExternalStoragePath() {
        String externalStorage = System.getenv("EXTERNAL_STORAGE");
        String secondaryStorage = System.getenv("SECONDARY_STORAGE");
        String emulatedStorage = System.getenv("EMULATED_STORAGE_TARGET");
        String defaultExternalStorage = "/storage/sdcard0";

        if (externalStorage == null) externalStorage = "";
        if (secondaryStorage == null) secondaryStorage = "";
        if (emulatedStorage == null) emulatedStorage = "";

        if ( ! secondaryStorage.isEmpty() ) return secondaryStorage;
        if ( ! externalStorage.isEmpty() ) return externalStorage;
        if ( ! emulatedStorage.isEmpty() ) return emulatedStorage;
        return defaultExternalStorage;
    }

    private void recordingStatusChanged(boolean isRecording) {
        recordingStatusListener.processStatusChange(isRecording);
    }

    private void startRecording(String outputFilePath) throws IOException {
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mediaRecorder.setOutputFile(outputFilePath);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);

        try {
            mediaRecorder.prepare();
            isRecording = true;
            mediaRecorder.start();
            SimpleTimer.start();
            recordingStatusChanged(true);
        }
        catch (IOException e){
            recordingStatusChanged(false);
            throw e;
        }
    }

    private void stopRecording(){
        if (isRecording) {
            mediaRecorder.stop();
            SimpleTimer.stop();
            isRecording = false;
            fileRenamingManager.purposeFileRenaming(externalStorageDir, tempAudioFile, captureStartDate);
            tempAudioFile = null;
        }
        mediaRecorder.release();
        mediaRecorder = null;
        recordingStatusChanged(false);
    }

}
