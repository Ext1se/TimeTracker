package com.tpu.mobile.timetracker.CustomViews;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;

/**
 * Created by Igorek on 05.11.2017.
 */

public class MyEditText extends android.support.v7.widget.AppCompatEditText {
    public MyEditText(Context context) {
        super(context);
    }

    public MyEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MyEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /*
    Модификация только ради этого метода, чтобы editText воспринимал нажатия
    */
    @Override
    public boolean onKeyPreIme(int keyCode, KeyEvent event) {
        //return super.onKeyPreIme(keyCode, event);
        return super.dispatchKeyEvent(event);
    }
}
