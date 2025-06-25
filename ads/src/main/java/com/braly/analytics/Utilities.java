package com.braly.analytics;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.util.Log;
import android.view.View;

public class Utilities {

    public static int getIdView(Context context, String nameId) {
        return context.getResources().getIdentifier(nameId, "id", context.getPackageName());
    }

    public static int getIdLayout(Context context, String nameLayout) {
        return context.getResources().getIdentifier(nameLayout, "layout", context.getPackageName());
    }

    public static int getIdDrawable(Context context, String nameDrawable) {
        return context.getResources().getIdentifier(nameDrawable, "drawable", context.getPackageName());
    }

    public static void setGone(Activity activity, String nameView) {
        if (nameView != null && !nameView.equalsIgnoreCase("") && activity.findViewById(Utilities.getIdView(activity, nameView)) != null) {
            {
                if (activity.findViewById(Utilities.getIdView(activity, nameView)).getVisibility() == View.VISIBLE) {
                    activity.findViewById(Utilities.getIdView(activity, nameView)).setVisibility(View.GONE);
                }
            }
        }
    }

    public static void setVisible(Activity activity, String nameView) {
        if (nameView != null && !nameView.equalsIgnoreCase("") && activity.findViewById(Utilities.getIdView(activity, nameView)) != null) {
            if (activity.findViewById(Utilities.getIdView(activity, nameView)).getVisibility() == View.GONE) {
                activity.findViewById(Utilities.getIdView(activity, nameView)).setVisibility(View.VISIBLE);
            }
        }
    }


    public static boolean isConnected(Context context) {
        final ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetworkInfo = connMgr.getActiveNetworkInfo();
        if (activeNetworkInfo != null) { // connected to the internet
            if (activeNetworkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                // connected to wifi
                return true;
            } else if (activeNetworkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
                // connected to the mobile provider's data plan
                return true;
            }
        }
        Log.d("TAG", "No internet!");
        return false;
    }

    public static void gotoMarket(Context mContext, String pkm) {
        Uri uri = Uri.parse("market://details?id=" + pkm);
        Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
        goToMarket.addFlags(
                Intent.FLAG_ACTIVITY_NEW_DOCUMENT |
                        Intent.FLAG_ACTIVITY_NEW_TASK |
                        Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        try {
            mContext.startActivity(goToMarket);
        } catch (ActivityNotFoundException e) {
            mContext.startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("http://play.google.com/store/apps/details?id=" + pkm)));
        }
    }

    public static void nextActivity(Context context, String className) {
        context.startActivity(new Intent().addFlags(Intent.FLAG_ACTIVITY_NEW_TASK).setClassName(context, className));
    }


    public static boolean isInstalled(String pkm, Context context) {
        PackageManager packageManager = context.getPackageManager();
        try {
            packageManager.getPackageInfo(pkm, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    public static boolean isScreenOn(String pkm, Context context) {
        PackageManager packageManager = context.getPackageManager();
        try {
            packageManager.getPackageInfo(pkm, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    public static String getClassNameCurrent(Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ComponentName cn = am.getRunningTasks(1).get(0).topActivity;
        return cn.getClassName();
    }

    public static Class<? extends Activity> getLauncherActivity(Context context) {
        Intent intent = context.getPackageManager().getLaunchIntentForPackage(context.getPackageName());
        if (intent != null && intent.getComponent() != null) {
            try {
                return (Class<? extends Activity>) Class.forName(intent.getComponent().getClassName());
            } catch (ClassNotFoundException e) {
                //Should not happen, print it to the log!
                Log.e("Fail: ", "Failed when resolving the restart activity class via getLaunchIntentForPackage, stack trace follows!", e);
            }
        }

        return null;
    }
}