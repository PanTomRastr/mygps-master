package ru.startandroid.develop.p1381location;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import 	android.net.wifi.WifiManager;
import android.content.Context;
import 	android.net.wifi.WifiConfiguration;

public class StartActivityOnBoot extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if(Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())){
            Intent i = new Intent(context, MainActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(i);
        }
    }

}
