package com.loloof64.android.capturing_audio;

import android.media.MediaRecorder;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;

/**
 * Created by laurent-bernabe on 16/03/16.
 */
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

    /**
     *
     * @return true if we end in recording state.
     */
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
