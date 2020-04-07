package com.example.trackcomposer;

public class ChordTriad {

    public static String getFifthsWheelMajorName(int index)
    {
        String[] chord = {"C", "G", "D", "A", "E", "B", "F#", "C#", "Ab", "Eb", "Bb", "F"};
        return chord[index % chord.length];
    }

    public static String getFifthsWheelMinorName(int index)
    {
        String[] chord = {"Am", "Em", "Bm", "F#m", "C#m", "G#m", "Ebm", "Bbm", "Fm", "Cm", "Gm", "Dm"};
        return chord[index % chord.length];
    }

    public static int getFifthsWheelMajorNumber(int index)
    {
        int[] chord = {4, 11, 6, 13, 8, 15, 10, 5, 12, 7, 14, 9};
        return chord[index % chord.length] - 4;
    }

    public static int getFifthsWheelMinorNumber(int index)
    {
        int[] chord = {13, 8, 15, 10, 5, 12, 7, 14, 9, 4, 11, 6};
        return chord[index % chord.length] - 4;
    }

    // returns the semitones offset from the root
    public static int  getMajor(int index)
    {
        switch (index) {
            case 0: return 0;  // root
            case 1: return 4;  // perfect third
            case 2: return 7;  // perfect fifth
        }

        return -1;
    }

    public static int  getMinor(int index)
    {
        switch (index) {
            case 0: return 0;  // root
            case 1: return 3;  // perfect third
            case 2: return 7;  // perfect fifth
        }

        return -1;
    }

    public static int  getDiminished(int index)
    {
        switch (index) {
            case 0: return 0;  // root
            case 1: return 3;  // perfect third
            case 2: return 6;  // perfect fifth
        }

        return -1;
    }

    public static int  getAugmented(int index)
    {
        switch (index) {
            case 0: return 0;  // root
            case 1: return 4;  // perfect third
            case 2: return 8;  // perfect fifth
        }

        return -1;
    }
}
