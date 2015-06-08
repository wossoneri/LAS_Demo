# LAS_Demo
an imitation of Last App Switcher

&emsp;&emsp;在Android众多工具类app中，`Last App Switcher`绝对算是一个让人用过就不会卸载的工具。`LAS`这个应用，它的功能很简单，就是通过一个浮动按钮实现在两个应用之间一键切换，但是非常实用，尤其是在边玩边聊天需要频繁切换应用的时候。所以可以看出，想开发一款受欢迎的应用，一定要注重用户体验，只要用户用的爽，功能再再再简单，它也会受欢迎。那么这功能到底有多简单呢？跟我来实现一下就好了。

[本文地址](http://www.cnblogs.com/rossoneri/p/4561057.html ):[http://www.cnblogs.com/rossoneri/p/4561057.html](http://www.cnblogs.com/rossoneri/p/4561057.html)

&emsp;&emsp;我就不截图了，下面用官方的截图来说明。这里真心推荐读者下载用一下。谷歌商店的下载地址：[Last App Switcher](https://play.google.com/store/apps/details?id=com.abhi.lastappswitcher 'Last App Switcher') 搞开发的应该都会翻墙吧

看下原始程序界面：
![LAS Main View](http://images.cnblogs.com/cnblogs_com/rossoneri/682731/o_unnamed.png)

&emsp;&emsp;可以看到主界面就是一系列开关选项，同时程序右边有一个浮动的圆形窗口。下面我会按照步骤一步步增加功能。

###仿iOS按钮
&emsp;&emsp;写demo不需要多好的界面，但也不能太丑，手里有看起来不错的控件就直接拖进来用了。下面是效果图，这一套按钮有好几种，都是仿iOS的，想要的可以点原作者的[这篇博客](http://blog.csdn.net/vipzjyno1/article/details/23707149#comments 'SwitchButton 开关按钮 的多种实现方式 （附源码DEMO）')，源码[Github地址](https://github.com/ikew0ng/SwitchButton)。

先添加一个开关主功能的按钮：
![初始界面](http://images.cnblogs.com/cnblogs_com/rossoneri/682731/o_1.png)

###浮动按钮
&emsp;&emsp;可以看到，这个应用的主要功能就在于那个红色的浮动按钮上面。根据程序功能可以知道，这个浮动按钮是由程序开启的服务中创建的。又因为程序的Activity在离开onStart()状态后就会销毁(这样做的原因后面说)，之后按钮仍保持其可用状态。所以可以知道是通过startService()启动的服务。下面我们就需要先写一个服务出来，再在服务中绘一个浮动按钮。具体有关服务的细节参考我上一篇博客：[博客传送门](http://www.cnblogs.com/rossoneri/p/4530216.html '[Android] Service服务详解以及如何使service服务不被杀死')。

写一个服务`FloatButtonService`，在AndroidManifest.xml文件添加服务
```xml
<service android:name=".FloatButtonService" >
</service>
```

服务中添加绘制浮动按钮方法，相关说明见注释
```java
private void createFloatView() {

}
```

方法添加完毕在服务相应的调用位置创建和销毁浮动按钮
```java
@Override
public void onCreate() {
	// TODO Auto-generated method stub
	super.onCreate();
	createFloatView();
}

@Override
public void onDestroy() {
	// TODO Auto-generated method stub
	super.onDestroy();
	if (mFloatLayout != null) {
		mWindowManager.removeView(mFloatLayout);
	}
}
```

使用浮动按钮还需要增加权限：
```xml
<uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW" />
```

这样，我们在MainActivity中就可以为按钮增加响应事件，进行开启和关闭服务了。


###将程序从最近任务(last recent tasks)中移除
&emsp;&emsp;按下系统导航栏第三个按钮我们就可以看到最近使用过的任务列表，当然，LAS切换程序也是在这里选择最后使用的两个应用程序切换的。所以在切换的时候，把自己的Activity从最近的任务中删掉是很必要的。
前面提到过，就是在Activity的onPause()状态或者onStop()状态中执行finishAndRemoveTask()方法删除任务。但这个方法在API 21也就是Android 5.0才引入。不过，我们还有一个更方便的方法，就是在配置文件的`<activity>`标签中增加
```xml
android:excludeFromRecents="true"
```
这样不论你是按下back键还是home键，程序都会从最近使用过的任务列表中删除

###任务间的切换
&emsp;&emsp;将自身Activity从最近任务列表中删除后，我们就可以考虑获取最后两次的任务，然后互相一键切换了。
在浮动按钮的单击事件中添加
首先需要获得ActivityManager的对象
```java
ActivityManager mActivityManager = (ActivityManager) this.getSystemService(Context.ACTIVITY_SERVICE);
```
要获取任务还需要对应权限
```xml
<uses-permission android:name="android.permission.GET_TASKS"/>
```
有了权限，就可以获取到任务列表了
```java
List<ActivityManager.RecentTaskInfo> mAppList = new ArrayList<ActivityManager.RecentTaskInfo>();
mAppList = mActivityManager.getRecentTasks(3, ActivityManager.RECENT_IGNORE_UNAVAILABLE);
```
建立一个装有`RecentTaskInfo`的列表，通过`getRecentTasks`方法获取系统的最近使用过的应用列表。

关于`getRecentTasks`方法，第一个参数是一个整型值，是你需要返回的应用数量，但实际上得到的数量可能会比这个值要小。比如
我要得到3个，但后台只开了1个，那么只返回1个。第二个参数是要返回的应用的状态，我选择的是忽略不可用的应用，应该是完全关闭，不在后台的应用。

再说一点，这个方法在Android5.0因为安全问题屏蔽掉了，也就是android5.0以上的版本不能用这个方法。所以我前一阵子在App Store上看到评论都是Android5.0用这个没有效果。现在行不行我倒不知道，闲了再研究吧。（每次我说闲了再做基本都是个坑- -|）

前面的参数我之所以要选择3，是因为我只需要获得最近使用的2个应用，因为每次开新应用，这个应用信息都会存在列表的最上面，所以获取前3个即可。

但为什么是3而不是2呢，因为Android的Home界面也是一个Activity（应该是），我可以选择是否要在切换的时候忽略掉Home界面。所以考虑到Home，就要用3。
Home的包名为`com.android.launcher`，以此为根据进行判断即可。
```java
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
```

启动一个应用的过程默认是有一个切换动画的，我们的程序就是用来切换程序的，所以取消启动动画是一个比较好的选择。
只用给要启动的intent加一个flag即可（有些情况下不会生效）
```java
intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
```


###开机启动
&emsp;&emsp;Android开机启动结束会发送一个BOOT_COMPLETED的广播，我们在程序中建立一个广播接收器来接收这个广播，接收成功就直接启动服务来显示浮动按钮即可。
先建立一个广播接收器 BootReceiver
```java
public class BootReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {// on boot
			Intent a = new Intent(context, FloatButtonService.class);
			context.startService(a);
		}
	}
}
```
在配置文件中，`<application>`标签下注册广播接收器
```xml
<receiver android:name=".BootReceiver" >
    <intent-filter>
        <action android:name="android.intent.action.BOOT_COMPLETED" />

        <category android:name="android.intent.category.LAUNCHER" />
    </intent-filter>
</receiver>
```
然后增加权限
```xml
<uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />
```
开机启动就完成了。但怎么用开关来控制其是否开机启动呢？

###SharedPreferences
&emsp;&emsp;用开关控制功能的开启状态，这个状态不能保存在程序中，因为程序是要被关闭的。那么就是要用一些方法保存开关的状态到系统中，然后服务从文件读取状态，控制自己的程序行为。Android中最适合保存配置状态的就是用SharedPreferences了。当我查看LAS应用的数据文件的时候，发现输出的结果的确是这样的。
```bash
cat /data/data/com.abhi.lastappswitcher/shared_prefs/com.inpen.lastAppSwitcher.APPLICATION_PREFS.xml
```
```xml
<?xml version='1.0' encoding='utf-8' standalone='yes' ?>
<map>
<boolean name="com.inpen.lastAppSwitcher.PREF_SNAP_TO_EDGE" value="true" />
<int name="com.inpen.lastAppSwitcher.PREF_LAND_HEIGHT" value="800" />
<int name="com.inpen.lastAppSwitcher.PREF_LAND_FLOATER_Y" value="485" />
<int name="com.inpen.lastAppSwitcher.PREF_LAND_WIDTH" value="1280" />
<int name="com.inpen.lastAppSwitcher.PREF_PORT_FLOATER_Y" value="776" />
<int name="com.inpen.lastAppSwitcher.PREF_PORT_WIDTH" value="800" />
<boolean name="com.inpen.lastAppSwitcher.PREF_ERROR_MSG" value="true" />
<boolean name="com.inpen.lastAppSwitcher.PREF_STATUS_BAR_OVERLAY" value="false" />
<int name="com.inpen.lastAppSwitcher.PREF_FLOATER_SIZE" value="55" />
<int name="com.inpen.lastAppSwitcher.PREF_PORT_FLOATER_X" value="765" />
<int name="com.inpen.lastAppSwitcher.PREF_PORT_HEIGHT" value="1280" />
<int name="com.inpen.lastAppSwitcher.PREF_FLOATER_TRANSPARENCY" value="75" />
<int name="currentQuote" value="6" />
<int name="com.inpen.lastAppSwitcher.PREF_SWITCHING_METHOD" value="1" />
<boolean name="com.inpen.lastAppSwitcher.PREF_FLOATER_MOVABLE" value="true" />
<boolean name="com.inpen.lastAppSwitcher.PREF_HAPTIC_FEEDBACK" value="false" />
<int name="com.inpen.lastAppSwitcher.PREF_FLOATER_COLOR" value="0" />
</map>
```
那么，我们就可以根据自己的需求来写sharedPreferences文件了
先获得 SharedPreferences 的实例
```java
SharedPreferences sp = getSharedPreferences("las_demo", Context.MODE_PRIVATE);
```
参数1是不带后缀的文件名，根据文件名获取实例，同一个名字的SharedPreferences对象只获得同一个实例；
参数2是模式操作模式：
* <font color="green">Context.MODE_PRIVATE:</font>
	为默认操作模式，代表该文件是私有数据，只能被应用本身访问，在该模式下，写入的内容会覆盖原文件的内容。
* <font color="green">Context.MODE_APPEND:</font>
	创建的文件是私有数据，该模式会检查文件是否存在，存在就往文件追加内容，否则就创建新文件。
* <font color="green">MODE_WORLD_READABLE:</font>
	表示当前文件可以被其他应用读取。
* <font color="green">MODE_WORLD_WRITEABLE:</font>
	表示当前文件可以被其他应用写入。

获得实例之后要进行初始化，写入一些设定值。这里因为初始化只需要一次，但我没找到判断sharedPreferences文件是否存在的方法(没想用File去查，这个文件存在系统路径，有权限问题，估计不行，有知道的可以告诉我)，有一个`public abstract boolean contains (String key)`方法，但用了感觉没效果，所以我又加了一个key，来保存第一次创建的状态，然后写入其他键-值，保存。
```java
if(!sp.getBoolean(StringKey.FirstCreate, true)){

	Editor editor = sp.edit();
	editor.putBoolean(StringKey.FirstCreate, true);
	editor.putBoolean(StringKey.RunAtStart, false);
	editor.putBoolean(StringKey.SnapToEdge, true);
	editor.putBoolean(StringKey.StatusBarOverlay, false);
	editor.putBoolean(StringKey.ExcludeHome, true);

	editor.commit();
}
```
设置好键-值后就可以根据这些值设置界面里按钮的开关状态和设置程序的一些行为。
```java
if (sp.getBoolean(StringKey.RunAtStart, false))
	mBtnRunAtStartup.setChecked(true);
else
	mBtnRunAtStartup.setChecked(false);
```
当然，在手动改变按钮状态的时候也要为某个key重新写入新的value
```java
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


private void editBoolKey(String str, boolean b) {
	Editor editor = sp.edit();
	editor.putBoolean(str, b);
	editor.apply();
}
```
改SharedPreferences的key-value的时候需要获得editor对象实例，设置完成用apply()方法或者commit()方法提交修改。如果有两个editor实例在同时修改，则以最后一次的提交为准。如果不关心返回值，且在应用的主线程里使用，用apply()要比commit()好。
至此，需要开关功能就在功能实现的地方加一层读取SP键值的过程，根据读到的结果决定功能。是否可用。


####悬浮按钮显示在status bar上方
&emsp;&emsp;按照下面设置windowManager的属性就好，没什么好解释的，放上文档看吧。
```java
wmParams.flags = LayoutParams.FLAG_NOT_TOUCH_MODAL
			   | LayoutParams.FLAG_NOT_FOCUSABLE
			   | LayoutParams.FLAG_LAYOUT_IN_SCREEN;
```
* <font color="green">int android.view.WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL = 32 [0x20]</font>
	Window flag: Even when this window is focusable (its is not set), allow any pointer events outside of the window to be sent to the windows behind it. Otherwise it will consume all pointer events itself, regardless of whether they are inside of the window.
* <font color="green">int android.view.WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE = 8 [0x8]</font>
	Window flag: this window won't ever get key input focus, so the user can not send key or other button events to it. Those will instead go to whatever focusable window is behind it. This flag will also enable FLAG_NOT_TOUCH_MODAL whether or not that is explicitly set.

	Setting this flag also implies that the window will not need to interact with a soft input method, so it will be Z-ordered and positioned independently of any active input method (typically this means it gets Z-ordered on top of the input method, so it can use the full screen for its content and cover the input method if needed. You can use FLAG_ALT_FOCUSABLE_IM to modify this behavior.
* <font color="green">int android.view.WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN = 256 [0x100]</font>
	Window flag: place the window within the entire screen, ignoring decorations around the border (a.k.a. the status bar). The window must correctly position its contents to take the screen decoration into account. This flag is normally set for you by Window as described in Window.setFlags.

####按钮边缘吸附效果
&emsp;&emsp;这个应该是最简单的了，在按钮的touch事件中，当移动结束，手指抬起行为ACTION_UP中对位置进行判断，如果按钮的x坐标在屏幕左半边，x设为0，即贴着屏幕左边缘显示，反之一个道理。

功能就粗略地实现了这么多，原应用中还有很多小功能就不一一实现了，代码我传到github了，地址点[这里](https://github.com/wossoneri/LAS_Demo)