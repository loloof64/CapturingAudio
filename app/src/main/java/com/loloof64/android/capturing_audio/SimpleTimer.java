/**
 Capturing Audio : a simple audio recorder on the android sd card.
 Copyright (C) 2016 Laurent Bernabé

 This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.

 You should have received a copy of the GNU General Public License along with this program. If not, see http://www.gnu.org/licenses/.
 */
package com.loloof64.android.capturing_audio;

import android.os.Handler;
import android.os.SystemClock;

import java.util.Timer;
import java.util.TimerTask;

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
