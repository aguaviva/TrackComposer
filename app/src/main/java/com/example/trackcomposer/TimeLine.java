package com.example.trackcomposer;

public class TimeLine {

    PatternBase mPattern;

    Viewport mViewport = new Viewport();

    public void init(PatternBase pattern, float bias) {
        mPattern = pattern;
        mViewport.setLodFactor(1.0f/bias); // 4 is the number of
    }

    public void setViewSize(float width, float height) {

        mViewport.setViewSize(width, height);

        int length = 16;
        if (mPattern!=null)
            length = (int)mPattern.GetLength();
    }

    public float getLength() {
        return mPattern.GetLength();
    }

    float mTime = 0;
    public void setTime(float time) {
        mTime = time;
    }
    public float getTime() {
        return mTime;
    }

    public void setTimeSpan(float t1, float t2)
    {
        mViewport.setSpanHorizontal(t1, t2);
    }

    public int getLeftTick(float tickWidth) {
        return (int) Math.max(Math.floor(mViewport.mRect.left * tickWidth), 0); // 0 ticks
    }

    public int getRightTick(float tickWidth) {
        return (int)Math.ceil(mViewport.mRect.right * tickWidth); // 256 ticks
    }

    public float getTimeFromScreen(float x)
    {
        return mViewport.removePosScaleX(x);
    }

    public float getRoundedTimeFromScreen(float x)
    {
        x = mViewport.removePosScaleX(x);
        return (float)Math.floor(x *mViewport.getLod())/mViewport.getLod();
    }

}
