package com.example.trackcomposer;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

import java.util.ArrayList;

class WaveFormDraw {

    private short[] mipChunk(short mipIn[]) {

        int length = mipIn.length;
        if ((mipIn.length % 2) == 1)
            length--;

        short[] mipOut = new short[length / 2];

        for (int i = 0; i < length; i += 2) {
            mipOut[i / 2] = (short) Math.max(mipIn[i + 0], mipIn[i + 1]);
        }

        return mipOut;
    }

    private ArrayList<short[]> vocalMips = new ArrayList<short[]>();

    private void mipVocals(InstrumentVocals inst, ArrayList<short[]> mips) {
        int frames = inst.getLengthInFrames();

        short[] tpoMip = new short[frames];

        short[] frame = new short[2];
        for (int i = 0; i < frames; i++) {
            frame[0] = 0;
            inst.mSample.copyFrame(0, frame, i, 3.0f);
            tpoMip[i] = frame[0];
        }

        short[] mipIn = tpoMip;
        while (mipIn.length > 2) {
            short[] mipOut = mipChunk(mipIn);
            mips.add(mipOut);
            mipIn = mipOut;
        }
    }

    int frames;
    static float lines[];

    public void init(InstrumentVocals inst) {
        frames = inst.getLengthInFrames();
        vocalMips =new ArrayList<short[]>();
        mipVocals(inst, vocalMips);
    }

    public void Draw(Canvas canvas, RectF rectParent, float screenFootPrint, Paint color)
    {
        // find optimal mip
        int optimalMip = -1;
        int minErr = Short.MAX_VALUE;
        for(int i=0;i<vocalMips.size();i++) {
            int err = Math.abs(vocalMips.get(i).length - (short)screenFootPrint);
            if (err<minErr)
            {
                minErr = err;
                optimalMip = i;
            }
        }

        short[] selectedMip =  vocalMips.get(optimalMip);
        float stepI = (float)frames / (float)selectedMip.length;

        {
            int step = 400;
            int index = 0;
            int left = selectedMip.length;
            int ii=0;

            if (lines==null)
                lines = new float[4*step];

            while(left>0) {

                int chunk = Math.min(step, left);
                index = 0;
                for (int i = ii; i < ii+chunk; i++) {
                    short value = selectedMip[i];
                    float y0 = Misc.map(-value, Short.MIN_VALUE, Short.MAX_VALUE, rectParent.top, rectParent.bottom);
                    float y1 = Misc.map(value, Short.MIN_VALUE, Short.MAX_VALUE, rectParent.top, rectParent.bottom);
                    float x = Misc.map((i * stepI), 0, frames, rectParent.left, rectParent.left + screenFootPrint);
                    lines[index++] = x;
                    lines[index++] = y0;
                    lines[index++] = x;
                    lines[index++] = y1;
                }
                canvas.drawLines(lines, 0, lines.length, color);

                ii += chunk;
                left -= chunk;
            }
        }
    }
}
