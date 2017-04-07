package org.net.sunger;

import android.app.Activity;
import android.graphics.Point;
import android.os.Build;
import android.view.Display;

import java.lang.reflect.Method;

/**
 * Created by Administrator on 2017/4/7.
 */

public class Util {

    public static Point getScreenWidthAndHeight(Activity content) {
        Point point = new Point();
        Display display = content.getWindowManager().getDefaultDisplay();
        if (Build.VERSION.SDK_INT >= 17) {
            display.getRealSize(point);
        } else if (Build.VERSION.SDK_INT >= 14) {
            try {
                Method mGetRawH = Display.class.getMethod("getRawHeight");
                Method mGetRawW = Display.class.getMethod("getRawWidth");
                point.x = (Integer) mGetRawW.invoke(display);
                point.y = (Integer) mGetRawH.invoke(display);
            } catch (Exception e) {
                display.getSize(point);
            }
        } else {
            display.getSize(point);
        }
        return point;
    }
}
