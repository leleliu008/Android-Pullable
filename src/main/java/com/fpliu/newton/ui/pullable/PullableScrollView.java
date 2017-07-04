package com.fpliu.newton.ui.pullable;


import android.content.Context;
import android.util.AttributeSet;
import android.widget.ScrollView;

/**
 * 可下拉刷新、下拉加载更多的ScrollView
 *
 * @author 792793182@qq.com 2015-06-26
 */
public class PullableScrollView extends ScrollView implements Pullable {

    private boolean canPullUp_ = true;

    private boolean canPullDown_ = true;

    public PullableScrollView(Context context) {
        super(context);
    }

    public PullableScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PullableScrollView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public boolean canPullDown() {
        return canPullDown_ && (getScrollY() == 0);
    }

    @Override
    public boolean canPullUp() {
        return canPullUp_ && (getScrollY() >= (getChildAt(0).getHeight() - getMeasuredHeight()));
    }

    @Override
    public void canPullUp(boolean canPullUp) {
        this.canPullUp_ = canPullUp;
    }

    @Override
    public void canPullDown(boolean canPullDown) {
        this.canPullDown_ = canPullDown;
    }
}