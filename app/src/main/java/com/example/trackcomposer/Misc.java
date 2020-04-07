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


    public static String getProgression(int index)
    {
        String progression [] = {
                " I- ii-  V-  I",
                " I- ii-  V-  V",
                " I- ii- IV-  V",
                " I- ii-  V- IV",
                " I-iii- IV-  V",
                " I-iii- IV- ii",
                " I- IV-  V- IV",
                " I-  V- IV-  V",
                " I-  V- vi- IV",
                " I- vi- IV-  V",
                " I- vi- ii-  V",
                "IV-  I-  V-  V",
                "vi- IV-  I-  V",
                "vi-  I-  V- IV",
                "vi- vi- IV-  I",
                "vi-  V- IV-  V",
                "vi-  V- IV-  I"
        };

        return progression[index];
    }

    // famous 4 chords songs
    public static int  getFifthsProgression(int index) {
        switch (index) {
            case 0: return  1;
            case 1: return -1;
            case 2: return  2;
            case 3: return 12;
        }

        return 0; //this should be an error
    }

    public static String getFifthsProgressionName(int index) {
        if (index>0)
        {
            return ChordTriad.getFifthsWheelMajorName(index-1);
        }
        else if (index<0)
        {
            return ChordTriad.getFifthsWheelMinorName(-index-1);
        }
        return "bad chord";
    }

    public static int getFifthsProgressionRootNumber(int index) {
        if (index>0)
        {
            return ChordTriad.getFifthsWheelMajorNumber(index-1);
        }
        else if (index<0)
        {
            return ChordTriad.getFifthsWheelMinorNumber(-index-1);
        }
        return -1;
    }


    public static int getTriadChord(int index, boolean isMajorScale) {
        if (isMajorScale)
        {
            return ChordTriad.getMajor(index);
        }
        else
        {
            return ChordTriad.getMinor(index);
        }
    }



}
