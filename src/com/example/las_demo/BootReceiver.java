package com.example.las_demo;

import com.example.helper.StringKey;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

public class BootReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {// on boot
			
			SharedPreferences sp = context.getSharedPreferences("las_demo", Context.MODE_PRIVATE);
			
			if(sp.getBoolean(StringKey.RunAtStart, false)){	//if get true, we can start service
				Intent a = new Intent(context, FloatButtonService.class);
				context.startService(a);
			}

		}
	}
}
