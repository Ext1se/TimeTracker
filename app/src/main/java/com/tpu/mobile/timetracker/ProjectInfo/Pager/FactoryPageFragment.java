package com.tpu.mobile.timetracker.ProjectInfo.Pager;

import android.support.v4.app.Fragment;

/**
 * Created by Igorek on 26.10.2017.
 */

public class FactoryPageFragment {
    public static final int TYPE_INFO = 0;
    public static final int TYPE_STATISTICS = 1;

    public static Fragment createFragment(int type)
    {
        switch (type)
        {
            case(TYPE_INFO):
                return PageMain.newInstance();
            case(TYPE_STATISTICS):
                return PageStatistics.newInstance();
            default:
                return null;
        }
    }
}
