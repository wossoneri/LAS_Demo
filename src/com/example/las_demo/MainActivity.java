package com.example.las_demo;

import java.util.List;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager.LayoutParams;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;

import com.example.helper.SizeHelper;
import com.example.helper.StringKey;
import com.example.style_button.CheckSwitchButton;

public class MainActivity extends Activity {

	private SharedPreferences sp;
	private CheckSwitchButton mBtnFloaterShow;
	private CheckSwitchButton mBtnRunAtStartup;
	private CheckSwitchButton mBtnStatusbarOverlay;
	private CheckSwitchButton mBtnSnapToEdge;
	private CheckSwitchButton mBtnExcludeHome;
	private LinearLayout layout;

	private Intent intent;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		getActionBar().hide();
		
		SizeHelper.getScreenSize(this);
		
		initPreferences();
		initView();
		initEvent();

	}

	private void initPreferences() {
		sp = getSharedPreferences("las_demo", Context.MODE_PRIVATE);

		if (!sp.contains("las_demo")) { // check whether there exists
										// las_demo.xml, if false, create one
			Editor editor = sp.edit();
//			editor.putBoolean(StringKey.FloaterShow, false);
			editor.putBoolean(StringKey.RunAtStart, false);
			editor.putBoolean(StringKey.SnapToEdge, true);
			editor.putBoolean(StringKey.StatusBarOverlay, false);
			editor.putBoolean(StringKey.ExcludeHome, true);

			editor.commit();
		}
	}

	private void initView() {
		intent  = new Intent(MainActivity.this, FloatButtonService.class);
		
		mBtnFloaterShow = (CheckSwitchButton) findViewById(R.id.btn_floater_show);
		mBtnRunAtStartup = (CheckSwitchButton) findViewById(R.id.btn_run_at_startup);
		mBtnStatusbarOverlay = (CheckSwitchButton) findViewById(R.id.btn_statusbar_overlay);
		mBtnSnapToEdge = (CheckSwitchButton) findViewById(R.id.btn_snap_to_edge);
		mBtnExcludeHome = (CheckSwitchButton) findViewById(R.id.btn_exclude_home);

//		 if (isServiceRunning("com.example.las_demo.FloatButtonService"))
		if (sp.getBoolean(StringKey.FloaterShow, false))
			mBtnFloaterShow.setChecked(true);
		else
			mBtnFloaterShow.setChecked(false);

		if (sp.getBoolean(StringKey.RunAtStart, false))
			mBtnRunAtStartup.setChecked(true);
		else
			mBtnRunAtStartup.setChecked(false);

		if (sp.getBoolean(StringKey.SnapToEdge, false))
			mBtnSnapToEdge.setChecked(true);
		else
			mBtnSnapToEdge.setChecked(false);

		if (sp.getBoolean(StringKey.StatusBarOverlay, false))
			mBtnStatusbarOverlay.setChecked(true);
		else
			mBtnStatusbarOverlay.setChecked(false);
		
		if (sp.getBoolean(StringKey.ExcludeHome, false))
			mBtnExcludeHome.setChecked(true);
		else
			mBtnExcludeHome.setChecked(false);
	}

	private void initEvent() {
		mBtnFloaterShow.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				// TODO Auto-generated method stub
				if (isChecked) {
					startService(intent);
					editBoolKey(StringKey.FloaterShow, true);
				} else {
					stopService(intent);
					editBoolKey(StringKey.FloaterShow, false);
				}
			}
		});

		mBtnRunAtStartup.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				// TODO Auto-generated method stub
				if (isChecked)
					editBoolKey(StringKey.RunAtStart, true);
				else
					editBoolKey(StringKey.RunAtStart, false);

			}
		});
		
		mBtnStatusbarOverlay.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				// TODO Auto-generated method stub
				if (isChecked)
					editBoolKey(StringKey.StatusBarOverlay, true);
				else
					editBoolKey(StringKey.StatusBarOverlay, false);

				stopService(intent);
				startService(intent);//restart the service when change the option
			}
		});
		
		mBtnSnapToEdge.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				// TODO Auto-generated method stub
				if (isChecked)
					editBoolKey(StringKey.SnapToEdge, true);
				else
					editBoolKey(StringKey.SnapToEdge, false);
				stopService(intent);
				startService(intent);
			}
		});
		
		mBtnExcludeHome.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				// TODO Auto-generated method stub
				if (isChecked)
					editBoolKey(StringKey.ExcludeHome, true);
				else
					editBoolKey(StringKey.ExcludeHome, false);
			}
		});
	}

	private void editBoolKey(String str, boolean b) {
		Editor editor = sp.edit();
		editor.putBoolean(str, b);
		editor.apply();
	}

	private boolean isServiceRunning(String serviceClassName) {
		final ActivityManager activityManager = (ActivityManager) this.getSystemService(Context.ACTIVITY_SERVICE);
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
