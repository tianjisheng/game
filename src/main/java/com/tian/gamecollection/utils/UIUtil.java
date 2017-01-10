package com.tian.gamecollection.utils;

/**
 * @author jisheng ,为写下的每行代码负责
 * @date 2017/1/6
 * @describe
 */

public class UIUtil
{
    public static int DPI = 160;
    public static float SDP = 1;
    public static int px2dp(int px)
    {
         return px * DPI/160;
    }
    
    public static int px2sp(int px)
    {
        return (int) (px / SDP + 0.5f);
    }
}
