package com.example.las_demo;

import java.util.ArrayList;
import java.util.List;

import android.app.ActivityManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.graphics.PointF;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.helper.SizeHelper;
import com.example.helper.StringKey;

public class FloatButtonService extends Service {

	public static String LOG = "LAS_DEMO";
	private final String HOME_PACKAGE = "com.android.launcher";
	private final float INGORE_TOUCH_MOVE = 5.0f; // 触摸移动小于3，可以忽略
	// 定义浮动窗口布局
	private LinearLayout				mFloatLayout;
	private WindowManager.LayoutParams	wmParams;
	// 创建浮动窗口设置布局参数的对象
	private WindowManager				mWindowManager;
	private Button						mBtn;

	private ActivityManager mActivityManager;
	private List<ActivityManager.RecentTaskInfo> mAppList = new ArrayList<ActivityManager.RecentTaskInfo>();
	
	private SharedPreferences sp;
	private boolean bSnapToEdge;
	private PointF mLastTouchPoint;
	
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		
		mLastTouchPoint = new PointF();
		mActivityManager = (ActivityManager) this.getSystemService(Context.ACTIVITY_SERVICE);
		
		sp = getSharedPreferences("las_demo", Context.MODE_PRIVATE);
		
		bSnapToEdge = sp.getBoolean(StringKey.SnapToEdge, true);
		createFloatView();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if (mFloatLayout != null) {
			mWindowManager.removeView(mFloatLayout);
		}
	}

	private void createFloatView() {
		wmParams = new WindowManager.LayoutParams();
		getApplication();
		// 获取WindowManagerImpl.CompatModeWrapper 其实就是window manager对象
		mWindowManager = (WindowManager) getApplication().getSystemService(Context.WINDOW_SERVICE);

		wmParams.type = LayoutParams.TYPE_SYSTEM_ERROR;
		// 设置图片格式，效果为背景透明
		wmParams.format = PixelFormat.TRANSLUCENT;
		
		if(sp.getBoolean(StringKey.StatusBarOverlay, false))
			wmParams.flags =LayoutParams.FLAG_NOT_TOUCH_MODAL | LayoutParams.FLAG_NOT_FOCUSABLE | LayoutParams.FLAG_LAYOUT_IN_SCREEN;
		else
		// 设置浮动窗口不可聚焦（实现操作除浮动窗口外的其他可见窗口的操作）
			wmParams.flags = LayoutParams.FLAG_NOT_FOCUSABLE;
		
		
		
		
		// set float window gravity to screen
		wmParams.gravity = Gravity.START | Gravity.TOP;

		// the offset to the gravity edge
		wmParams.x = 30;
		wmParams.y = 20;


		// 设置悬浮窗口长宽数据
		wmParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
		wmParams.height = WindowManager.LayoutParams.WRAP_CONTENT;

		LayoutInflater inflater = LayoutInflater.from(getApplication());
		// 获取浮动窗口视图所在布局
		mFloatLayout = (LinearLayout) inflater.inflate(R.layout.float_layout, null);
		// 添加mFloatLayout
		mWindowManager.addView(mFloatLayout, wmParams);

		// 浮动窗口按钮
		mBtn = (Button) mFloatLayout.findViewById(R.id.btn_Float);

		mFloatLayout.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
				View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
		// 设置监听浮动窗口的触摸移动
		mBtn.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub

				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					mLastTouchPoint.x = event.getX();
					mLastTouchPoint.y = event.getY();
					break;
				
				case MotionEvent.ACTION_MOVE:
					if (Math.abs(mLastTouchPoint.x - event.getX()) > INGORE_TOUCH_MOVE || Math.abs(mLastTouchPoint.y - event.getY()) > INGORE_TOUCH_MOVE) {
						// getRawX是触摸位置相对于屏幕的坐标，getX是相对于按钮的坐标
						wmParams.x = (int) event.getRawX() - mBtn.getMeasuredWidth() / 2;
						wmParams.y = (int) event.getRawY() - mBtn.getMeasuredHeight() / 2 - 25;
						// 刷新
						mWindowManager.updateViewLayout(mFloatLayout, wmParams);
					}
					break;

				case MotionEvent.ACTION_UP:
					if (Math.abs(mLastTouchPoint.x - event.getX()) > INGORE_TOUCH_MOVE || Math.abs(mLastTouchPoint.y - event.getY()) > INGORE_TOUCH_MOVE) { // it`s move
						if (bSnapToEdge) {
							if (wmParams.x > SizeHelper.screen_width / 2)
								wmParams.x = SizeHelper.screen_width;
							else
								wmParams.x = 0;
						}
						// 刷新
						mWindowManager.updateViewLayout(mFloatLayout, wmParams);
					}else{ // click
						mAppList = mActivityManager.getRecentTasks(3, ActivityManager.RECENT_IGNORE_UNAVAILABLE);// 最近使用过的app在list最前面

						if (mAppList.size() > 2) {
							for(int i = 0; i< mAppList.size(); i++){
								Log.v(LOG, "~~~~~~~~~~~~~~ComponentName" + i + ":  " + mAppList.get(i).baseIntent.getComponent().toString());
								Log.v(LOG, "~~~~~~~~~~~~~~ClassName" + i + ":      " + mAppList.get(i).baseIntent.getComponent().getClassName());
								Log.v(LOG, "~~~~~~~~~~~~~~PackageName" + i + ":    " + mAppList.get(i).baseIntent.getComponent().getPackageName());
								Log.v(LOG, "~~~~~~~~~~~~~~ShortClassName" + i + ": " + mAppList.get(i).baseIntent.getComponent().getShortClassName());
							}
							getAndStartIntent(1);
						}
					}
					
					break;
				default:
					break;
				}
				
				return false;
			}
		});

//		mBtn.setOnClickListener(new OnClickListener() {
//
//			@Override
//			public void onClick(View v) {
//				// TODO Auto-generated method stub
//
//				
//			}
//		});
	}

	private void getAndStartIntent(int i){
		ActivityManager.RecentTaskInfo info = mAppList.get(i);
		if (null == info)
			Toast.makeText(FloatButtonService.this, "No other apps", Toast.LENGTH_SHORT).show();
		else if(sp.getBoolean(StringKey.ExcludeHome, false)){ // if set true, do follow func
			if(info.baseIntent.getComponent().getPackageName().equals(HOME_PACKAGE))	//exclude HOME
				getAndStartIntent(2);
		}else
			startActivityWithoutAnimation(info);
	}
	
	private void startActivityWithoutAnimation(ActivityManager.RecentTaskInfo info) {
		Intent intent = info.baseIntent;
		intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
		// activity.overridePendingTransition(0, 0);
		startActivity(intent);
	}
}
