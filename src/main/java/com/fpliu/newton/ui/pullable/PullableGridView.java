package com.fpliu.newton.ui.pullable;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.GridView;

/**
 * @author 792793182@qq.com 2016-07-17.
 */
public class PullableGridView extends GridView implements Pullable {

    public PullableGridView(Context context) {
        super(context);
    }

    public PullableGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PullableGridView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    private boolean canPullUp_ = true;

    private boolean canPullDown_ = true;

    @Override
    public void canPullUp(boolean canPullUp) {
        this.canPullUp_ = canPullUp;
    }

    @Override
    public void canPullDown(boolean canPullDown) {
        this.canPullDown_ = canPullDown;
    }

    @Override
    public boolean canPullDown() {
        if (canPullDown_) {
            if (getCount() == 0) {
                // 没有item的时候也可以下拉刷新
                return true;
            } else if (getChildAt(0) != null) {
                if (getFirstVisiblePosition() == 0

                        && getChildAt(0).getTop() >= 0) {
                    // 滑到ListView的顶部了
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean canPullUp() {
        if (canPullUp_) {
            if (getCount() == 0) {
                // 没有item的时候也可以上拉加载
                return true;
            } else if (getLastVisiblePosition() == (getCount() - 1)) {
                // 滑到底部了
                if (getChildAt(getLastVisiblePosition() - getFirstVisiblePosition()) != null
                        && getChildAt(
                        getLastVisiblePosition()
                                - getFirstVisiblePosition()).getBottom() <= getMeasuredHeight()) {
                    return true;
                }
            }
        }
        return false;
    }

}
