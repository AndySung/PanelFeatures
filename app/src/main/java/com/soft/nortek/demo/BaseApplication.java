package com.soft.nortek.demo;

import android.app.Activity;
import android.app.Application;
import android.os.Looper;

import java.lang.ref.WeakReference;
import java.util.Locale;
import java.util.Stack;

import cat.ereza.customactivityoncrash.activity.DefaultErrorActivity;
import me.jessyan.autosize.AutoSize;
import me.jessyan.autosize.AutoSizeConfig;
import me.jessyan.autosize.external.ExternalAdaptInfo;
import me.jessyan.autosize.external.ExternalAdaptManager;
import me.jessyan.autosize.internal.CustomAdapt;
import me.jessyan.autosize.onAdaptListener;
import me.jessyan.autosize.utils.LogUtils;


/**
 * 继承Application主要做一些初始化以及获取全局Context的引用
 * **/

public class BaseApplication extends Application {
    private static final String TAG = BaseApplication.class.getSimpleName();
    private static android.os.Handler mMainThreadHandler;
    private static Looper mMainThreadLooper;
    private static Thread mMainThread;

    public static BaseApplication mContext;//需要使用的上下文对象

    /*** 寄存整个应用Activity **/
    private final Stack<WeakReference<Activity>> mActivities = new Stack<>();

    @Override
    public void onCreate() {
        super.onCreate();
        initMainParams();
        CrashHandler.getInstance().init(this);

        AutoSize.initCompatMultiProcess(this);
        AutoSizeConfig.getInstance()

                //是否让框架支持自定义 Fragment 的适配参数, 由于这个需求是比较少见的, 所以须要使用者手动开启
                //如果没有这个需求建议不开启
                .setCustomFragment(true)

                //是否屏蔽系统字体大小对 AndroidAutoSize 的影响, 如果为 true, App 内的字体的大小将不会跟随系统设置中字体大小的改变
                //如果为 false, 则会跟随系统设置中字体大小的改变, 默认为 false
//                .setExcludeFontScale(true)

                //屏幕适配监听器
                .setOnAdaptListener(new onAdaptListener() {
                    @Override
                    public void onAdaptBefore(Object target, Activity activity) {
                        //使用以下代码, 可支持 Android 的分屏或缩放模式, 但前提是在分屏或缩放模式下当用户改变您 App 的窗口大小时
                        //系统会重绘当前的页面, 经测试在某些机型, 某些情况下系统不会重绘当前页面, ScreenUtils.getScreenSize(activity) 的参数一定要不要传 Application!!!
//                        AutoSizeConfig.getInstance().setScreenWidth(ScreenUtils.getScreenSize(activity)[0]);
//                        AutoSizeConfig.getInstance().setScreenHeight(ScreenUtils.getScreenSize(activity)[1]);
                        LogUtils.d(String.format(Locale.ENGLISH, "%s onAdaptBefore!", target.getClass().getName()));
                    }

                    @Override
                    public void onAdaptAfter(Object target, Activity activity) {
                        LogUtils.d(String.format(Locale.ENGLISH, "%s onAdaptAfter!", target.getClass().getName()));
                    }
                });

        customAdaptForExternal();
        /**全局捕捉异常**/
       //CrashHandler.getInstance().init(this);
    }

    private void initMainParams() {
        mContext = this;
        mMainThreadLooper = getMainLooper();
        mMainThreadHandler = new android.os.Handler(mMainThreadLooper);
        mMainThread = Thread.currentThread();
    }

    /**
     * 给外部的三方库 {@link Activity} 自定义适配参数, 因为三方库的 {@link Activity} 并不能通过实现
     * {@link CustomAdapt} 接口的方式来提供自定义适配参数 (因为远程依赖改不了源码)
     * 所以使用 {@link ExternalAdaptManager} 来替代实现接口的方式, 来提供自定义适配参数
     */
    private void customAdaptForExternal() {
        /**
         * {@link ExternalAdaptManager} 是一个管理外部三方库的适配信息和状态的管理类, 详细介绍请看 {@link ExternalAdaptManager} 的类注释
         */
        AutoSizeConfig.getInstance().getExternalAdaptManager()

                //加入的 Activity 将会放弃屏幕适配, 一般用于三方库的 Activity, 详情请看方法注释
                //如果不想放弃三方库页面的适配, 请用 addExternalAdaptInfoOfActivity 方法, 建议对三方库页面进行适配, 让自己的 App 更完美一点
//                .addCancelAdaptOfActivity(DefaultErrorActivity.class)

                //为指定的 Activity 提供自定义适配参数, AndroidAutoSize 将会按照提供的适配参数进行适配, 详情请看方法注释
                //一般用于三方库的 Activity, 因为三方库的设计图尺寸可能和项目自身的设计图尺寸不一致, 所以要想完美适配三方库的页面
                //就需要提供三方库的设计图尺寸, 以及适配的方向 (以宽为基准还是高为基准?)
                //三方库页面的设计图尺寸可能无法获知, 所以如果想让三方库的适配效果达到最好, 只有靠不断的尝试
                //由于 AndroidAutoSize 可以让布局在所有设备上都等比例缩放, 所以只要您在一个设备上测试出了一个最完美的设计图尺寸
                //那这个三方库页面在其他设备上也会呈现出同样的适配效果, 等比例缩放, 所以也就完成了三方库页面的屏幕适配
                //即使在不改三方库源码的情况下也可以完美适配三方库的页面, 这就是 AndroidAutoSize 的优势
                //但前提是三方库页面的布局使用的是 dp 和 sp, 如果布局全部使用的 px, 那 AndroidAutoSize 也将无能为力
                //经过测试 DefaultErrorActivity 的设计图宽度在 380dp - 400dp 显示效果都是比较舒服的
                .addExternalAdaptInfoOfActivity(DefaultErrorActivity.class, new ExternalAdaptInfo(true, 400));
    }


    /**
     * 获取全局上下文
     * @return the context
     */

    public static BaseApplication getApplication(){
        return mContext;
    }

    /**
     * 获取主线程Handler
     * @return the mMainThreadHandler
     */
    public static android.os.Handler getMainThreadHandler() {
        return mMainThreadHandler;
    }


    /**
     * 获取主线程
     * @return the mMainThread
     */
    public static Thread getMainThread() {
        return mMainThread;
    }

    /**
     * 将Activity压入Application栈
     * @param task 将要压入栈的Activity对象
     */
    public void pushTask(WeakReference<Activity> task){
        mActivities.push(task);
    }

    /**
     * 将传入的Activity对象从栈中移除
     *
     * @param task 将要移除栈的Activity对象
     */
    public void removeTask(WeakReference<Activity> task) {
        mActivities.remove(task);
    }


    /**
     * 关闭某个activity
     *
     * @param activityCls 指定activity的类 eg：MainActivity.class
     */
    public void finishActivity(Class<? extends Activity> activityCls) {
        int end = mActivities.size();
        for (int i = end - 1; i >= 0; i--) {
            Activity cacheActivity = mActivities.get(i).get();
            if (cacheActivity.getClass().getSimpleName().equals(activityCls.getSimpleName()) && !cacheActivity.isFinishing()) {
                cacheActivity.finish();
                removeTask(i);
            }
        }
    }

    /**
     * 根据指定位置从栈中移除Activity
     *
     * @param taskIndex Activity栈索引
     */
    public void removeTask(int taskIndex) {
        if (mActivities.size() > taskIndex)
            mActivities.remove(taskIndex);
    }

    /** 获取顶层activity */
    public Activity getTopActivity() {
        if (mActivities.size() > 0) {
            return mActivities.get(mActivities.size() - 1).get();
        }
        return null;
    }

    /** 移除全部（用于整个应用退出） */
    public void removeAll() {
        int end = mActivities.size();
        for (int i = end - 1; i >= 0; i--) {
            Activity activity = mActivities.get(i).get();
            if (!activity.isFinishing()) {
                activity.finish();
            }
        }
        mActivities.clear();
    }


    /** 移除除第一个MainActivity之外的全部（主要用于回到MainActivity） */
    public void removeAllExceptFirst() {
        int end = mActivities.size();
        for (int i = end - 1; i >= 1; i--) {
            Activity activity = mActivities.get(i).get();
            if (!activity.isFinishing()) {
                activity.finish();
            }
            removeTask(i);
        }
    }
}
