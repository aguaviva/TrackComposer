package com.example.trackcomposer;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;

class SoundNative {
    /*
     * jni function declarations
     */
/*
    static native void createSLEngine(int rate, int framesPerBuf, long delayInMs, float decay);
    static native void deleteSLEngine();
    static native boolean configureEcho(int delayInMs, float decay);
    static native boolean createSLBufferQueueAudioPlayer();
    static native void deleteSLBufferQueueAudioPlayer();

    static native boolean createAudioRecorder();
    static native void deleteAudioRecorder();
    static native void startPlay();
    static native void stopPlay();
    static { System.loadLibrary("echo"); }

    public native String stringFromJNI();

    public boolean Init(Context ctx)
    {
        AudioManager myAudioMgr = (AudioManager) ctx.getSystemService(Context.AUDIO_SERVICE);

        String nativeSampleRate  =  myAudioMgr.getProperty(AudioManager.PROPERTY_OUTPUT_SAMPLE_RATE);
        String nativeSampleBufSize =myAudioMgr.getProperty(AudioManager.PROPERTY_OUTPUT_FRAMES_PER_BUFFER);

        // hardcoded channel to mono: both sides -- C++ and Java sides
        int recBufSize = AudioRecord.getMinBufferSize(
                Integer.parseInt(nativeSampleRate),
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT);
        if (recBufSize == AudioRecord.ERROR || recBufSize == AudioRecord.ERROR_BAD_VALUE) {
            return false;
        }

        createSLEngine(
                Integer.parseInt(nativeSampleRate),
                Integer.parseInt(nativeSampleBufSize),
                0,
                0);

        boolean res = createSLBufferQueueAudioPlayer();
        startPlay();
        return res;

    }

    public static int getAnswer(boolean a) {
        return 42;
    }
*/
}
