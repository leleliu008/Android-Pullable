package com.fpliu.newton.ui.pullable;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;

import com.fpliu.newton.ui.stateview.StateView;

import java.lang.reflect.Constructor;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 可下拉刷新、下拉加载更多的ListView，包含头和尾
 *
 * @author 792793182@qq.com 2015-06-26
 */
public final class PullableViewContainer<T extends View> extends RelativeLayout {

    private RefreshOrLoadMoreCallback<T> callback;

    private PullToRefreshLayout pullToRefreshLayout;

    private T pullableView;

    private StateView stateView;

    //起始页（默认从0开始）
    private static int startPageNumber = 0;

    /**
     * 请求的页数（分页）
     */
    private int pageNum = startPageNumber;

    /**
     * 一页的记录数（分页）
     */
    private int pageSize = 10;

    /**
     * 是否正在请求网络
     */
    private AtomicBoolean isRequesting = new AtomicBoolean(false);

    public PullableViewContainer(Context context, Class<T> pullableViewClass) {
        super(context);
        initView(context, pullableViewClass);
    }

    public PullableViewContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context, null);
    }

    public PullableViewContainer(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initView(context, null);
    }

    private void initView(Context context, Class<T> pullableViewClass) {
        pullToRefreshLayout = new PullToRefreshLayout(context);
        View refreshView = inflate(context, R.layout.pullable_refresh_head, null);
        View loadMoreView = inflate(context, R.layout.pullable_load_more, null);
        pullToRefreshLayout.addView(refreshView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        pullToRefreshLayout.addView(pullableView = newView(pullableViewClass, context), new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        pullToRefreshLayout.addView(loadMoreView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

        addView(pullToRefreshLayout);

        stateView = new StateView(context);
        addView(stateView);
    }

    public static void setStartPageNumber(int startPageNumber) {
        PullableViewContainer.startPageNumber = startPageNumber;
    }

    public T getPullableView() {
        return pullableView;
    }

    public void refresh() {
        if (callback != null) {
            callback.onRefreshOrLoadMore(PullableViewContainer.this, Type.REFRESH, pageNum = startPageNumber, pageSize);
        }
    }

    /**
     * 返回结果后，必须调用此方法，改变状态
     *
     * @param type
     * @param isSuccess
     * @param pullableStatusText
     * @param stateViewText
     */
    public void finishRequest(Type type, boolean isSuccess, String pullableStatusText, String stateViewText) {
        int refreshResult = isSuccess ? PullToRefreshLayout.SUCCEED : PullToRefreshLayout.FAIL;
        switch (type) {
            case REFRESH:
                pullToRefreshLayout.refreshFinish(refreshResult, pullableStatusText);
                break;
            case LOAD_MORE:
                pullToRefreshLayout.loadmoreFinish(refreshResult, pullableStatusText);
                break;
        }

        if (isSuccess) {
            if (TextUtils.isEmpty(stateViewText)) {
                stateView.setVisibility(View.GONE);
            } else {
                stateView.showErrorTextOnly(stateViewText);
            }
        } else {
            stateView.showErrorTextOnly(stateViewText);
        }

        isRequesting.set(false);
    }

    /**
     * 返回结果后，必须调用此方法，改变状态
     *
     * @param type
     */
    public void finishRequestSuccess(Type type) {
        String pullableStatusText = "";
        switch (type) {
            case REFRESH:
                pullableStatusText = "刷新成功";
                break;
            case LOAD_MORE:
                pullableStatusText = "加载成功";
                break;
            default:
                break;
        }

        finishRequest(type, true, pullableStatusText, "");
    }

    /**
     * 带刷新按钮
     *
     * @param type
     * @param pullableStatusText
     * @param stateViewText
     */
    public void finishRequestWithRefresh(Type type, boolean isSuccess, String pullableStatusText, String stateViewText) {
        switch (type) {
            case REFRESH:
                pullToRefreshLayout.refreshFinish(isSuccess ? PullToRefreshLayout.SUCCEED : PullToRefreshLayout.FAIL, pullableStatusText);
                stateView.showErrorWithAction(stateViewText, "刷新", () -> {
                    //正在请求的过程中，忽略
                    if (isRequesting.get()) {
                        return;
                    }

                    isRequesting.set(true);

                    callback.onRefreshOrLoadMore(PullableViewContainer.this, Type.REFRESH, pageNum = startPageNumber, pageSize);
                });
                break;
            case LOAD_MORE:
                pullToRefreshLayout.loadmoreFinish(isSuccess ? PullToRefreshLayout.SUCCEED : PullToRefreshLayout.FAIL, pullableStatusText);
                stateView.showErrorWithAction(stateViewText, "刷新", () -> {
                    //正在请求的过程中，忽略
                    if (isRequesting.get()) {
                        return;
                    }

                    isRequesting.set(true);

                    callback.onRefreshOrLoadMore(PullableViewContainer.this, Type.LOAD_MORE, --pageNum, pageSize);
                });
                break;
        }

        isRequesting.set(false);
    }

    public void setRefreshOrLoadMoreCallback(final RefreshOrLoadMoreCallback callback) {
        this.callback = callback;

        if (callback != null) {
            PullToRefreshLayout.OnRefreshListener onRefreshListener = new PullToRefreshLayout.OnRefreshListener() {

                @Override
                public void onRefresh(PullToRefreshLayout pullToRefreshLayout) {
                    //正在请求的过程中，忽略
                    if (isRequesting.get()) {
                        return;
                    }

                    isRequesting.set(true);

                    callback.onRefreshOrLoadMore(PullableViewContainer.this, Type.REFRESH, pageNum = startPageNumber, pageSize);
                }

                @Override
                public void onLoadMore(PullToRefreshLayout pullToRefreshLayout) {
                    //正在请求的过程中，忽略
                    if (isRequesting.get()) {
                        return;
                    }

                    isRequesting.set(true);

                    callback.onRefreshOrLoadMore(PullableViewContainer.this, Type.LOAD_MORE, ++pageNum, pageSize);
                }
            };
            pullToRefreshLayout.setOnRefreshListener(onRefreshListener);

            if (isNetworkAvailable(getContext())) {
                stateView.showProgress("有一大波数据来袭...");
                //第一次主动调用
                onRefreshListener.onRefresh(pullToRefreshLayout);
            } else {
                stateView.showErrorBecauseNoNetworking();
            }
        }
    }

    private static boolean isNetworkAvailable(Context context) {
        // 获取系统的连接服务
        ConnectivityManager connectivityManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetInfo != null && activeNetInfo.isConnected();
    }

    public final boolean isRequesting() {
        return isRequesting.get();
    }

    private static <K extends View> K newView(Class<K> viewClass, Context context) {
        try {
            Constructor<K> constructor = viewClass.getDeclaredConstructor(Context.class);
            constructor.setAccessible(true);
            return constructor.newInstance(context);
        } catch (Exception e) {
            Log.e("PullableViewContainer", "newView()", e);
            return null;
        }
    }
}
