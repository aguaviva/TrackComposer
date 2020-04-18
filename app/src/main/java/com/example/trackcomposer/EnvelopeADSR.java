package com.example.trackcomposer;

import org.json.JSONException;
import org.json.JSONObject;

public class EnvelopeADSR {
    float mAttackTime = 0;
    float mAttackVolume = 1;
    float mDecayTime = 0;
    float mSustainVolume = 1;
    float mSustainTime = 0;
    float mReleaseTime = 0;

    float unmap(float val, float minIn, float maxIn)
    {
        return (val-minIn)/(maxIn-minIn);
    }

    float map(float t, float minOut, float maxOut)
    {
        return (1.0f-t)*minOut + t*maxOut;
    }

    float map(float val, float minIn, float maxIn, float minOut, float maxOut)
    {
        float t = unmap(val, minIn,  maxIn);
        return map( t, minOut, maxOut);
    }

    public void setTimings(float attackTime, float attackVolume, float decayTime, float sustainVolume, float releaseTime)
    {
        mAttackTime = attackTime;
        mDecayTime = decayTime;
        mReleaseTime = releaseTime;

        mAttackVolume = attackVolume;
        mSustainVolume = sustainVolume;
    }

    public void setSustainTime(float sustainTime)
    {
        mSustainTime = sustainTime;
    }

    public float getEnvelope(float time)
    {
        if (time<mAttackTime)
        {
            return  map(time, 0,mAttackTime, 0,mAttackVolume);
        }
        else if (time<mAttackTime+mDecayTime)
        {
            return  map(time, mAttackTime,mAttackTime+mDecayTime, mAttackVolume, mSustainVolume);
        }
        else if (time<(mAttackTime + mDecayTime + mSustainTime))
        {
            return mSustainVolume;
        }
        else if (time<(mAttackTime + mDecayTime + mSustainTime + mReleaseTime))
        {
            return  map(time, (mAttackTime + mDecayTime + mSustainTime),(mAttackTime + mDecayTime + mSustainTime + mReleaseTime), mSustainVolume,0);
        }

        return 0;
    }

    void serializeToJson(JSONObject jsonObj) throws JSONException
    {
        JSONObject json = new JSONObject();
        json.put("attackTime", mAttackTime);
        json.put("decayTime", mDecayTime);
        json.put("releaseTime", mReleaseTime);

        json.put("attackVolume", mAttackVolume);
        json.put("sustainVolume", mSustainVolume);

        json.put("ADSR",json);
    }

    void serializeFromJson(JSONObject jsonObj) throws JSONException
    {
        if (jsonObj.has("ADSR"))
        {
            JSONObject jsonObj2 = jsonObj.getJSONObject("tremolo");
            mAttackTime = (float)jsonObj2.getDouble("attackTime");
            mDecayTime = (float)jsonObj2.getDouble("decayTime");
            mReleaseTime = (float)jsonObj.getDouble("releaseTime");

            mAttackVolume = (float)jsonObj.getDouble("attackVolume");
            mSustainVolume = (float)jsonObj.getDouble("sustainVolume");
        }

    }

    public float getEnvelopeDurationInSeconds() {
        return (mAttackTime + mDecayTime + mSustainTime + mReleaseTime);
    }

}
