package com.example.trackcomposer;

public class ChordTriad {
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
