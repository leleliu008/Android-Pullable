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
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnLoadmoreListener;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;

import java.lang.reflect.Constructor;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author 792793182@qq.com 2015-06-26
 */
public final class PullableViewContainer<T extends View> extends RelativeLayout {

    private RefreshOrLoadMoreCallback<T> callback;

    private SmartRefreshLayout refreshLayout;

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
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);

        refreshLayout = new SmartRefreshLayout(context);
        refreshLayout.addView(pullableView = newView(pullableViewClass, context), new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

        addView(refreshLayout, lp);

        addView(stateView = new StateView(context), lp);
    }

    public static void setStartPageNumber(int startPageNumber) {
        PullableViewContainer.startPageNumber = startPageNumber;
    }

    public SmartRefreshLayout getRefreshLayout() {
        return refreshLayout;
    }

    public T getPullableView() {
        return pullableView;
    }

    public void refresh() {
        if (callback != null) {
            callback.onRefreshOrLoadMore(PullableViewContainer.this, PullType.DOWN, pageNum = startPageNumber, pageSize);
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
    public void finishRequest(PullType type, boolean isSuccess, String pullableStatusText, String stateViewText) {
        switch (type) {
            case DOWN:
                refreshLayout.finishRefresh(2000, isSuccess);
                break;
            case UP:
                refreshLayout.finishLoadmore(2000, isSuccess);
                break;
        }

        if (isSuccess) {
            if (TextUtils.isEmpty(stateViewText)) {
                stateView.setVisibility(GONE);
            } else {
                stateView.setVisibility(VISIBLE);
                stateView.showErrorTextOnly(stateViewText);
            }
        } else {
            stateView.setVisibility(VISIBLE);
            stateView.showErrorTextOnly(stateViewText);
        }

        isRequesting.set(false);
    }

    /**
     * 返回结果后，必须调用此方法，改变状态
     *
     * @param type
     */
    public void finishRequestSuccess(PullType type) {
        String pullableStatusText = "";
        switch (type) {
            case DOWN:
                pullableStatusText = "刷新成功";
                break;
            case UP:
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
    public void finishRequestWithRefresh(PullType type, boolean isSuccess, String pullableStatusText, String stateViewText) {
        switch (type) {
            case DOWN:
                refreshLayout.finishRefresh(2000, isSuccess);
                stateView.setVisibility(VISIBLE);
                stateView.showErrorWithAction(stateViewText, "刷新", () -> {
                    //正在请求的过程中，忽略
                    if (isRequesting.get()) {
                        return;
                    }

                    isRequesting.set(true);

                    callback.onRefreshOrLoadMore(PullableViewContainer.this, PullType.DOWN, pageNum = startPageNumber, pageSize);
                });
                break;
            case UP:
                refreshLayout.finishLoadmore(2000, isSuccess);
                stateView.setVisibility(VISIBLE);
                stateView.showErrorWithAction(stateViewText, "刷新", () -> {
                    //正在请求的过程中，忽略
                    if (isRequesting.get()) {
                        return;
                    }

                    isRequesting.set(true);

                    callback.onRefreshOrLoadMore(PullableViewContainer.this, PullType.UP, --pageNum, pageSize);
                });
                break;
        }

        isRequesting.set(false);
    }

    public void setRefreshOrLoadMoreCallback(final RefreshOrLoadMoreCallback callback) {
        this.callback = callback;

        if (callback != null) {
            refreshLayout.setOnRefreshListener(refreshLayout -> {
                //正在请求的过程中，忽略
                if (isRequesting.get()) {
                    return;
                }

                isRequesting.set(true);

                callback.onRefreshOrLoadMore(PullableViewContainer.this, PullType.DOWN, pageNum = startPageNumber, pageSize);
            });
            refreshLayout.setOnLoadmoreListener(refreshLayout -> {
                //正在请求的过程中，忽略
                if (isRequesting.get()) {
                    return;
                }

                isRequesting.set(true);

                callback.onRefreshOrLoadMore(PullableViewContainer.this, PullType.UP, ++pageNum, pageSize);
            });

            if (isNetworkAvailable(getContext())) {
                stateView.showProgress("有一大波数据来袭...");
                //第一次主动调用
                refreshLayout.autoRefresh();
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

    public void setErrorTextColor(int color) {
        if (stateView != null) {
            stateView.setErrorTextColor(color);
        }
    }

    public void setActionTextColor(int color) {
        if (stateView != null) {
            stateView.setActionTextColor(color);
        }
    }

    /**
     * @param effectType EffectFactory.TYPE_XX
     */
    public void setErrorEffectType(int effectType) {
        if (stateView != null) {
            stateView.setErrorEffectType(effectType);
        }
    }

    /**
     * @param effectType EffectFactory.TYPE_XX
     */
    public void setActionEffectType(int effectType) {
        if (stateView != null) {
            stateView.setActionEffectType(effectType);
        }
    }
}
