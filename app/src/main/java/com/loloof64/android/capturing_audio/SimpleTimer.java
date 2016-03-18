package com.loloof64.android.capturing_audio;

import android.os.Handler;
import android.os.SystemClock;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by laurent-bernabe on 17/03/16.
 */
public class SimpleTimer {

    public interface TimerListener {
        void processTime(int hours, int minutes, int seconds);
    }

    public static TimerListener timerListener;

    public static void start(){
        handler = new Handler();
        timer = new Timer();
        timerListener.processTime(0,0,0);
        startTime = SystemClock.uptimeMillis();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                long elapsedTime = SystemClock.uptimeMillis() - startTime;
                int totalSeconds = (int) (elapsedTime / 1000);
                final int seconds = totalSeconds % 60;
                int totalMinutes = totalSeconds / 60;
                final int minutes = totalMinutes % 60;
                final int hours = totalMinutes / 60;

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        timerListener.processTime(hours, minutes, seconds);
                    }
                });
            }
        }, 1000, 1000);
    }

    public static void stop(){
        timer.cancel();
        timer.purge();
        timer = null;
    }

    public static void setTimerListener(TimerListener listener){
        timerListener = listener;
    }

    private static Timer timer;
    private static long startTime = 0L;
    private static Handler handler;

}
