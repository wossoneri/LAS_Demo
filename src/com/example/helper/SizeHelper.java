package com.example.helper;

import android.app.Activity;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.DisplayMetrics;

public final class SizeHelper {

	public static final Point getScreenSize(Activity activity) {
		Point p = new Point();
		DisplayMetrics dm = new DisplayMetrics();
		activity.getWindowManager().getDefaultDisplay().getMetrics(dm);

		p.x = dm.widthPixels;
		p.y = dm.heightPixels;
		return p;
	}

	public static final int getStatusHeight(Activity activity) {
		int status_bar_height = 0;
		int resourceId = activity.getResources().getIdentifier("status_bar_height", "dimen", "android");
		if (resourceId > 0) {
			status_bar_height = activity.getResources().getDimensionPixelSize(resourceId);

			Rect frame = new Rect();
			activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
			int statusBarBottom = frame.top;
			if (statusBarBottom - status_bar_height < 0) { // status bar's top < 0 means status bar is hidden
				status_bar_height = 0;
			}
		}
		return status_bar_height;
	}

	public static final int getNavigationHeight(Activity activity){
		int navition_bar_height = 0;
		int resourceId = activity.getResources().getIdentifier("navigation_bar_height", "dimen", "android");
		if (resourceId > 0) 
			navition_bar_height = activity.getResources().getDimensionPixelSize(resourceId);
		
		return navition_bar_height;
	}
}
