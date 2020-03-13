package com.example.trackcomposer;

public class Misc {
    public static String getNoteName(int n) {
        String[] notes = {"A", "A#", "B", "C", "C#", "D", "D#", "E", "F", "F#", "G", "G#"};
        int octave = (n + 8) / 12;
        int noteIdx = (n - 1) % 12;
        return notes[noteIdx] + String.valueOf(octave);
    }

    public static float GetFrequency(int n) {
        float exponent = ((float) n - 49.0f) / 12.0f;
        return (float) Math.pow(2.0f, exponent) * 440.0f;
    }
}
