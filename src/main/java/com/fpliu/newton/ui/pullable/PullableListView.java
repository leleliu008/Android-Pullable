package com.fpliu.newton.ui.pullable;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.AbsListView;
import android.widget.ListView;

/**
 * 可下拉刷新、下拉加载更多的ListView
 *
 * @author 792793182@qq.com 2015-06-26
 */
public class PullableListView extends ListView implements Pullable {

    /**
     * 是否自动刷新
     */
    private boolean autoRefresh;

    /**
     * 是否自动加载更多
     */
    private boolean autoLoadMore;

    private boolean canPullUp_ = true;

    private boolean canPullDown_ = true;

    public PullableListView(Context context) {
        super(context);
    }

    public PullableListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PullableListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    /**
     * 设置是否自动刷新
     *
     * @param autoRefresh
     */
    public void setAutoRefresh(boolean autoRefresh) {
        this.autoRefresh = autoRefresh;

        if (autoRefresh) {
            autoRefreshOrLoadMore();
        } else {
            if (!autoLoadMore) {
                setOnScrollListener(null);
            }
        }
    }

    /**
     * 设置是否自动加载更多
     *
     * @param autoLoadMore
     */
    public void setAutoLoadMore(boolean autoLoadMore) {
        this.autoLoadMore = autoLoadMore;

        if (autoLoadMore) {
            autoRefreshOrLoadMore();
        } else {
            if (!autoRefresh) {
                setOnScrollListener(null);
            }
        }
    }

    @Override
    public void canPullUp(boolean canPullUp) {
        this.canPullUp_ = canPullUp;
    }

    @Override
    public void canPullDown(boolean canPullDown) {
        this.canPullDown_ = canPullDown;
    }

    private void autoRefreshOrLoadMore() {
        //设置自动刷新和自动加载更多
        setOnScrollListener(new OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                // 当滚动停下来时
                if (scrollState == OnScrollListener.SCROLL_STATE_IDLE) {
                    //下拉刷新
                    if (canPullDown()) {

                    }
                    //上滑加载更多
                    else if (canPullUp()) {

                    }
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

            }
        });
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
