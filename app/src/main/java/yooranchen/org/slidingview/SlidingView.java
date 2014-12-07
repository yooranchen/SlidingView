package yooranchen.org.slidingview;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.Scroller;


/**
 * Created by Yooran on 2014/12/7.
 */
public class SlidingView extends RelativeLayout {
    private FrameLayout leftMenu;
    private FrameLayout middleMenu;
    private FrameLayout rightMenu;
    private FrameLayout middleMask;//模糊效果
    public static final int LEFT_ID = 0xaabbcc;
    public static final int MIDDLE_ID = 0xaaccbb;
    public static final int RIGHT_ID = 0xbbaacc;
    private float leftOffSet = 0.8f;//左侧侧滑菜单宽度>>默认为屏幕寬度*0.8
    private float rightOffSet = 0.8f;//右侧策划菜单宽度>>默认为屏幕寬度*0.8
    private Mode mode = Mode.BOTH;
    private float maskAlpha;//透明度
    private boolean isSlidEnable = true;//控制是否可以滑动

    public void setSlidEnable(boolean isSlidEnable) {
        this.isSlidEnable = isSlidEnable;
        invalidate();
    }

    /**
     * 设置模式,左,右,左右
     */
    public void setMode(Mode mode) {
        this.mode = mode;
        invalidate();
    }

    /**
     * 设置左侧菜单大小
     *
     * @param leftOffSet
     */
    public void setLeftOffSet(float leftOffSet) {
        this.leftOffSet = leftOffSet;
    }

    /**
     * 设置右侧菜单大小
     *
     * @param rightOffSet
     */
    public void setRightOffSet(float rightOffSet) {
        this.rightOffSet = rightOffSet;
    }

    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if (maskAlpha == 0) {
                middleMask.setVisibility(View.GONE);
            } else {
                middleMask.setVisibility(View.VISIBLE);
            }
            return false;
        }
    });

    public SlidingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public SlidingView(Context context) {
        super(context);
        initView(context);
    }

    private void initView(Context context) {
        //宽度为屏幕宽度的80%;
        scroller = new Scroller(context, new DecelerateInterpolator());
        leftMenu = new FrameLayout(context);
        middleMenu = new FrameLayout(context);
        rightMenu = new FrameLayout(context);
        middleMask = new FrameLayout(context);
        leftMenu.setBackgroundColor(Color.RED);
        leftMenu.setId(getResources().getInteger(R.integer.leftId));
        middleMenu.setBackgroundColor(Color.GREEN);
        middleMenu.setId(getResources().getInteger(R.integer.middleId));
        rightMenu.setBackgroundColor(Color.RED);
        rightMenu.setId(getResources().getInteger(R.integer.rightId));
        middleMask.setBackgroundColor(Color.LTGRAY);
        addView(leftMenu);
        addView(middleMenu);
        addView(rightMenu);
        addView(middleMask);
        middleMask.setAlpha(0);//开始完全透明
        middleMask.setOnClickListener(maskListener);
    }

    private OnClickListener maskListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if (maskAlpha == 1) {
                toggle();
            }
        }
    };

    /**
     * 设置左菜单视图
     *
     * @param fm       FragmentManager
     * @param fragment Fragment>>需要是片段
     */
    public void setLeftMenuView(FragmentManager fm, Fragment fragment) {
        FragmentTransaction ft = fm.beginTransaction();
        ft.add(LEFT_ID, fragment);
        ft.commit();
        invalidate();
    }

    /**
     * 设置右菜单视图
     *
     * @param fm       FragmentManager
     * @param fragment Fragment>>需要是片段
     */
    public void setRightMenuView(FragmentManager fm, Fragment fragment) {
        FragmentTransaction ft = fm.beginTransaction();
        ft.add(RIGHT_ID, fragment);
        ft.commit();
        invalidate();
    }

    /**
     * 设置主视图
     *
     * @param fm       FragmentManager
     * @param fragment Fragment>>需要是片段
     */
    public void setContentView(FragmentManager fm, Fragment fragment) {
        FragmentTransaction ft = fm.beginTransaction();
        ft.add(MIDDLE_ID, fragment);
        ft.commit();
        invalidate();
    }

    @Override
    public void scrollTo(int x, int y) {
        super.scrollTo(x, y);
        //根据滑动距离的变化来决定图层的透明度
        int curX = Math.abs(getScrollX());
        maskAlpha = curX / (float) leftMenu.getMeasuredWidth();//>>0-1
        middleMask.setAlpha(maskAlpha);
        if (maskAlpha == 0 || maskAlpha == 1)
            handler.sendEmptyMessage(1);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        middleMenu.measure(widthMeasureSpec, heightMeasureSpec);
        middleMask.measure(widthMeasureSpec, heightMeasureSpec);
        int realWidth = MeasureSpec.getSize(widthMeasureSpec);
        int leftWidth = MeasureSpec.makeMeasureSpec((int) (realWidth * leftOffSet), MeasureSpec.EXACTLY);
        int rightWidth = MeasureSpec.makeMeasureSpec((int) (realWidth * rightOffSet), MeasureSpec.EXACTLY);
        if (mode == Mode.BOTH) {
            leftMenu.measure(leftWidth, heightMeasureSpec);
            rightMenu.measure(rightWidth, heightMeasureSpec);
        } else if (mode == Mode.LEFT) {//左菜单模式
            leftMenu.measure(leftWidth, heightMeasureSpec);
        } else if (mode == Mode.RIGHT) {//右菜单模式
            rightMenu.measure(rightWidth, heightMeasureSpec);
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        middleMenu.layout(l, t, r, b);
        middleMask.layout(l, t, r, b);
        leftMenu.layout(l - leftMenu.getMeasuredWidth(), t, r, b);
        rightMenu.layout(l + middleMenu.getMeasuredWidth(), t,
                l + middleMenu.getMeasuredWidth() + rightMenu.getMeasuredWidth(), b);
    }

    private boolean isTest;
    private boolean isLeftRightEvent;
    private Scroller scroller;

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (isSlidEnable) {
            if (!isTest) {
                getEventType(ev);
                return true;
            }
            if (isLeftRightEvent) {
                int curScrollX;//滚动距离
                switch (ev.getActionMasked()) {
                    case MotionEvent.ACTION_MOVE:
                        curScrollX = getScrollX();
                        int dis_x = (int) (ev.getX() - point.x);//滑动距离
                        int expectX = -dis_x + curScrollX;
                        int finalX = 0;
                        if (expectX < 0) {//向右滑动
                            finalX = Math.max(expectX, -leftMenu.getMeasuredWidth());
                        } else {
                            finalX = Math.min(expectX, rightMenu.getMeasuredWidth());
                        }
                        scrollTo(finalX, 0);
                        point.x = (int) ev.getX();
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        curScrollX = getScrollX();
                        if (Math.abs(curScrollX) > leftMenu.getMeasuredWidth() >> 1) {
                            //滑动距离小于 边菜单的一半距离
                            if (curScrollX < 0) {
                                //出现右菜单
                                scroller.startScroll(curScrollX, 0,
                                        -leftMenu.getMeasuredWidth() - curScrollX, 0, 200);
                            } else {
                                scroller.startScroll(curScrollX, 0,
                                        leftMenu.getMeasuredWidth() - curScrollX, 0, 200);
                            }
                        } else {
                            scroller.startScroll(curScrollX, 0, -curScrollX, 0);
                        }
                        invalidate();
                        isTest = false;
                        isLeftRightEvent = false;
                        break;
                }
            } else {
                switch (ev.getActionMasked()) {
                    case MotionEvent.ACTION_UP:
                        isTest = false;
                        isLeftRightEvent = false;
                        break;
                }
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public void computeScroll() {
        //使用Scroller,必须重写computeScroll方法
        super.computeScroll();
        if (!scroller.computeScrollOffset()) {
            return;
        }
        int currX = scroller.getCurrX();
        scrollTo(currX, 0);
    }

    private Point point = new Point();
    private static final int TEXT_DIS = 20;

    private void getEventType(MotionEvent ev) {
        switch (ev.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                initPoint(ev);
                super.dispatchTouchEvent(ev);//上下事件不拦截
                break;
            case MotionEvent.ACTION_MOVE:
                int dX = (int) Math.abs(ev.getX() - point.x);
                int dY = (int) Math.abs(ev.getY() - point.y);
                if (dX >= TEXT_DIS && dX > dY) {
                    //左右滑动
                    isLeftRightEvent = true;
                    isTest = true;
                    initPoint(ev);
                } else if (dY > TEXT_DIS & dY > dX) {
                    //上下滑动
                    isLeftRightEvent = false;
                    isTest = true;
                    initPoint(ev);
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                isLeftRightEvent = false;
                isTest = false;
                super.dispatchTouchEvent(ev);//上下事件不拦截
                break;
        }
    }


    private void initPoint(MotionEvent ev) {
        point.x = (int) ev.getX();
        point.y = (int) ev.getY();
    }

    /**
     * 关闭菜单
     */
    public void toggle() {
        scroller.startScroll(0, 0, 0, 0, 400);
        invalidate();
    }

}
