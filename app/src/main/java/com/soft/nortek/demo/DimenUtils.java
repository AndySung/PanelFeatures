package com.soft.nortek.demo;

import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.util.TypedValue;

public class DimenUtils {
    private static final Resources sResource = Resources.getSystem();

    public static float dp2px(float dp) {
        DisplayMetrics dm = sResource.getDisplayMetrics();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, dm);
    }

    public static int dp2pxInt(float dp) {
        return (int) dp2px( dp);
    }

    public static float sp2px(float sp) {
        DisplayMetrics dm = sResource.getDisplayMetrics();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, sp, dm);
    }

    public static int sp2pxInt(float sp) {
        return (int) sp2px(sp);
    }
}
