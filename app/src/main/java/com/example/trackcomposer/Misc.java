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

    public static int getOctave(int n)
    {
        return (n + 8) / 12;
    }

    public static int setOctave(int oct)
    {
        return (oct * 12) - 8;
    }


    public static int  getFifthsMajor(int root, int n) {
        //               C   G   D   A   E   B  F#  Db  Ab  Eb  Bb   F
        int[] notes = {  4, 11,  6,  1,  8,  3, 10,  5, 12,  7,  2,  9};
        //t[] notes = {  1,  3,  4,  6,  8,  9, 11,  a,  a,  a,  a,  a};

        int octave = getOctave(root);

        int noteIdx = ((root + 8) % 12);
        if (n<0)
            n=noteIdx + 12+n;
        else
            n=noteIdx+n;

        noteIdx =  notes[n]-4;

        return noteIdx + setOctave(octave);
    }

    public static int  getFifthsMinor(int root) {
        return getFifthsMajor(root , 3) +12;
    }

    public static int  getTriadChord(int root, int index, boolean major) {
        switch (index) {
            case 0:
                return root + 0;
            case 1:
                return root + ((major) ? 4 : 3);
            case 2:
                return root + 7;
        }

        return -1;
    }

    public static int getFifthsProgression(int root, int index)
    {
        switch(index)
        {
            case 0: return Misc.getFifthsMajor(root, + 0);
            case 1: return Misc.getFifthsMajor(root, + 1);
            case 2: return Misc.getFifthsMajor(root, - 1);
            case 3: return Misc.getFifthsMinor(root);
        }
        return -1;
    }

}
