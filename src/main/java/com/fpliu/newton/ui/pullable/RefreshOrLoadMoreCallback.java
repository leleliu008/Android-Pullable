package com.fpliu.newton.ui.pullable;

import android.view.View;

/**
 * @author 792793182@qq.com 2016-07-30.
 */
public interface RefreshOrLoadMoreCallback<T extends View> {
    void onRefreshOrLoadMore(PullableViewContainer<T> pullableViewContainer, PullType type, int pageNum, int pageSize);
}
