package bignews.myapplication;

import android.app.Application;
import android.content.Context;

/**
 * Created by lazycal on 2017/9/11.
 */

public class MyApplication extends Application {

    private static Context context;

    public void onCreate() {
        super.onCreate();
        MyApplication.context = getApplicationContext();
    }

    public static Context getAppContext() {
        return MyApplication.context;
    }
}
