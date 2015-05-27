package com.example.las_demo;

import java.util.List;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class MainActivity extends Activity {

	private CheckSwitchButton mBtnFloatButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		getActionBar().hide();

		initView();
		initEvent();

	}

	private void initView() {
		mBtnFloatButton = (CheckSwitchButton) findViewById(R.id.btn_float_button);

		if (isServiceRunning("com.example.las_demo.FloatButtonService"))
			mBtnFloatButton.setChecked(true);
		else
			mBtnFloatButton.setChecked(false);
	}

	private void initEvent() {
		mBtnFloatButton.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				// TODO Auto-generated method stub
				if (isChecked) {
					Intent intent = new Intent(MainActivity.this, FloatButtonService.class);
					startService(intent);
				} else {
					Intent intent = new Intent(MainActivity.this, FloatButtonService.class);
					stopService(intent);
				}
			}
		});
	}

	// @Override
	// protected void onPause() {
	// // TODO Auto-generated method stub
	// finishAndRemoveTask();
	// super.onPause();
	//
	// }
	//
	// @Override
	// public void onBackPressed() {
	// // TODO Auto-generated method stub
	// finishAndRemoveTask();
	// super.onBackPressed();
	//
	// }

	private boolean isServiceRunning(String serviceClassName) {
		final ActivityManager activityManager = (ActivityManager) this
				.getSystemService(Context.ACTIVITY_SERVICE);
		final List<RunningServiceInfo> services = activityManager.getRunningServices(40);

		for (RunningServiceInfo runningServiceInfo : services) {
			if (runningServiceInfo.service.getClassName().equals(serviceClassName)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
