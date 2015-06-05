# LAS_Demo
an imitation of Last App Switcher

&emsp;&emsp;在Android众多工具类app中，`Last App Switcher`绝对算是一个让人用过就不会卸载的工具。又因为系统机制不同导致iOS上没有如此方便的工具，这一点也成为部分Android用户坚守自己阵营的理由～当然，这话并没有引战的意思，作为iOS和Android用户，我是欣赏两边所有的优点。考虑到Google马上推出Android M，从爆出的一些介绍来看，Android和iOS的差距在慢慢减小，Android M的新特性和界面让开发者们很期待啊！回到`LAS`这个应用，它的功能很简单，就是在两个应用之间一键切换，但是很实用啊，尤其是在边玩边聊天需要频繁切换应用的时候。所以可以看出，想开发一款受欢迎的应用，一定要注重用户体验，只要用户用的爽，功能再再再简单，它也会受欢迎。那么这功能到底有多简单呢？跟我来实现一下就好了。

&emsp;&emsp;我就不截图了，下面用官方的截图来说明。这里真心推荐读者下载用一下。谷歌商店的下载地址：[Last App Switcher](https://play.google.com/store/apps/details?id=com.abhi.lastappswitcher 'Last App Switcher') 看博客园的应该都会翻墙吧

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

###任务间的切换 unfinished
将自身Activity从最近任务列表中删除后，我们就可以考虑获取最后两次的任务，然后互相一键切换了。
在浮动按钮的单击事件中添加
首先需要获得ActivityManager的对象
```java
ActivityManager mActivityManager = (ActivityManager) this.getSystemService(Context.ACTIVITY_SERVICE);
```
要获取任务还需要对应权限
```xml
<uses-permission android:name="android.permission.GET_TASKS"/>
```
取消切换动画


```java
mAppList = mActivityManager.getRecentTasks(3, ActivityManager.RECENT_IGNORE_UNAVAILABLE);// 最近使用过的app在list最前面

ActivityManager.RecentTaskInfo info = mAppList.get(1);
if (null == info)
	Toast.makeText(FloatButtonService.this, "No other apps", Toast.LENGTH_SHORT).show();
else
	startActivity(info.baseIntent);
```
可以参考http://www.cnblogs.com/JohnTsai/p/4550144.html 去掉Activity切换的动画
基本功能完成后就可以按照LAS的设置来一一添加其他功能


###开机启动
Android开机启动结束会发送一个BOOT_COMPLETED的广播，我们在程序中建立一个广播接收器来接收这个广播，接收成功就直接启动服务来显示浮动按钮即可。
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
那么，我们就开始写我们自己的sharedPreferences文件吧

####悬浮按钮显示在status bar上方
```java
wmParams.flags =LayoutParams.FLAG_NOT_TOUCH_MODAL | LayoutParams.FLAG_NOT_FOCUSABLE | LayoutParams.FLAG_LAYOUT_IN_SCREEN;
```