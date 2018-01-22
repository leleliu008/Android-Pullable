package com.fpliu.newton.ui.pullable;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;

import com.fpliu.newton.ui.stateview.StateView;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;

import java.lang.reflect.Constructor;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author 792793182@qq.com 2015-06-26
 */
public final class PullableViewContainer<T extends View> extends SmartRefreshLayout {

    private RefreshOrLoadMoreCallback<T> callback;

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

    public PullableViewContainer(Class<T> pullableViewClass, StateView stateView) {
        super(stateView.getContext());
        this.stateView = stateView;
        pullableView = newView(pullableViewClass, stateView.getContext());
    }

    public void setDefaultLayout() {
        RelativeLayout relativeLayout = new RelativeLayout(getContext());
        relativeLayout.addView(pullableView, new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        relativeLayout.addView(stateView, new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

        addView(relativeLayout, new SmartRefreshLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
    }

    public static void setStartPageNumber(int startPageNumber) {
        PullableViewContainer.startPageNumber = startPageNumber;
    }

    public T getPullableView() {
        return pullableView;
    }

    public StateView getStateView() {
        return stateView;
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
     */
    public void finishRequestSuccess(PullType type) {
        finishRequest(type, true, "");
    }

    /**
     * 返回结果后，必须调用此方法，改变状态
     *
     * @param type
     * @param isSuccess
     * @param stateViewText
     */
    public void finishRequest(PullType type, boolean isSuccess, String stateViewText) {
        switch (type) {
            case DOWN:
                finishRefresh(isSuccess);
                break;
            case UP:
                finishLoadmore(isSuccess);
                break;
        }

        if (isSuccess) {
            if (TextUtils.isEmpty(stateViewText)) {
                pullableView.setVisibility(VISIBLE);
                stateView.setVisibility(GONE);
            } else {
                pullableView.setVisibility(GONE);
                stateView.setVisibility(VISIBLE);
                stateView.showErrorText(stateViewText);
            }
        } else {
            pullableView.setVisibility(GONE);
            stateView.setVisibility(VISIBLE);
            stateView.showErrorText(stateViewText);
        }

        isRequesting.set(false);
    }

    /**
     * 返回结果后，必须调用此方法，改变状态
     *
     * @param type
     * @param isSuccess
     * @param stateViewImageResId
     */
    public void finishRequest(PullType type, boolean isSuccess, int stateViewImageResId) {
        switch (type) {
            case DOWN:
                finishRefresh(isSuccess);
                break;
            case UP:
                finishLoadmore(isSuccess);
                break;
        }

        if (isSuccess) {
            if (stateViewImageResId == 0) {
                pullableView.setVisibility(VISIBLE);
                stateView.setVisibility(GONE);
            } else {
                pullableView.setVisibility(GONE);
                stateView.setVisibility(VISIBLE);
                stateView.showErrorImage(stateViewImageResId);
            }
        } else {
            pullableView.setVisibility(GONE);
            stateView.setVisibility(VISIBLE);
            stateView.showErrorImage(stateViewImageResId);
        }

        isRequesting.set(false);
    }

    /**
     * 返回结果后，必须调用此方法，改变状态
     *
     * @param type
     * @param isSuccess
     * @param stateViewImageResId
     */
    public void finishRequest(PullType type, boolean isSuccess, int stateViewImageResId, String stateViewText) {
        switch (type) {
            case DOWN:
                finishRefresh(isSuccess);
                break;
            case UP:
                finishLoadmore(isSuccess);
                break;
        }

        if (isSuccess) {
            if (stateViewImageResId == 0 && TextUtils.isEmpty(stateViewText)) {
                pullableView.setVisibility(VISIBLE);
                stateView.setVisibility(GONE);
            } else {
                pullableView.setVisibility(GONE);
                stateView.setVisibility(VISIBLE);
                stateView.showErrorImageAndText(stateViewImageResId, stateViewText);
            }
        } else {
            pullableView.setVisibility(GONE);
            stateView.setVisibility(VISIBLE);
            stateView.showErrorImageAndText(stateViewImageResId, stateViewText);
        }

        isRequesting.set(false);
    }

    /**
     * 带刷新按钮
     *
     * @param type
     * @param isSuccess
     * @param stateViewText
     */
    public void finishRequestWithRefreshAction(PullType type, boolean isSuccess, String stateViewText) {
        switch (type) {
            case DOWN:
                finishRefresh(isSuccess);
                pullableView.setVisibility(GONE);
                stateView.setVisibility(VISIBLE);
                stateView.showErrorTextWithAction(stateViewText, "刷新", () -> {
                    //正在请求的过程中，忽略
                    if (isRequesting.get()) {
                        return;
                    }

                    isRequesting.set(true);

                    callback.onRefreshOrLoadMore(PullableViewContainer.this, PullType.DOWN, pageNum = startPageNumber, pageSize);
                });
                break;
            case UP:
                finishLoadmore(isSuccess);
                pullableView.setVisibility(GONE);
                stateView.setVisibility(VISIBLE);
                stateView.showErrorTextWithAction(stateViewText, "刷新", () -> {
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

    /**
     * 带刷新按钮
     *
     * @param type
     * @param isSuccess
     * @param stateViewImageResId
     */
    public void finishRequestWithRefreshAction(PullType type, boolean isSuccess, int stateViewImageResId) {
        switch (type) {
            case DOWN:
                finishRefresh(isSuccess);
                pullableView.setVisibility(GONE);
                stateView.setVisibility(VISIBLE);
                stateView.showErrorImageWithAction(stateViewImageResId, "刷新", () -> {
                    //正在请求的过程中，忽略
                    if (isRequesting.get()) {
                        return;
                    }

                    isRequesting.set(true);

                    callback.onRefreshOrLoadMore(PullableViewContainer.this, PullType.DOWN, pageNum = startPageNumber, pageSize);
                });
                break;
            case UP:
                finishLoadmore(isSuccess);
                pullableView.setVisibility(GONE);
                stateView.setVisibility(VISIBLE);
                stateView.showErrorImageWithAction(stateViewImageResId, "刷新", () -> {
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

    /**
     * 带刷新按钮
     *
     * @param type
     * @param isSuccess
     * @param stateViewImageResId
     */
    public void finishRequestWithRefreshAction(PullType type, boolean isSuccess, int stateViewImageResId, String stateViewText) {
        switch (type) {
            case DOWN:
                finishRefresh(isSuccess);
                pullableView.setVisibility(GONE);
                stateView.setVisibility(VISIBLE);
                stateView.showErrorImageAndTextWithAction(stateViewImageResId, stateViewText, "刷新", () -> {
                    //正在请求的过程中，忽略
                    if (isRequesting.get()) {
                        return;
                    }

                    isRequesting.set(true);

                    callback.onRefreshOrLoadMore(PullableViewContainer.this, PullType.DOWN, pageNum = startPageNumber, pageSize);
                });
                break;
            case UP:
                finishLoadmore(isSuccess);
                pullableView.setVisibility(GONE);
                stateView.setVisibility(VISIBLE);
                stateView.showErrorImageAndTextWithAction(stateViewImageResId, stateViewText, "刷新", () -> {
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

    /**
     * 带按钮
     *
     * @param type
     * @param isSuccess
     * @param stateViewImageResId
     */
    public void finishRequestWithAction(PullType type, boolean isSuccess, int stateViewImageResId, String actionText, Runnable action) {
        switch (type) {
            case DOWN:
                finishRefresh(isSuccess);
                break;
            case UP:
                finishLoadmore(isSuccess);
                break;
        }
        pullableView.setVisibility(GONE);
        stateView.setVisibility(VISIBLE);
        stateView.showErrorImageWithAction(stateViewImageResId, actionText, action);
        isRequesting.set(false);
    }

    /**
     * 带按钮
     *
     * @param type
     * @param isSuccess
     * @param stateViewText
     */
    public void finishRequestWithAction(PullType type, boolean isSuccess, String stateViewText, String actionText, Runnable action) {
        switch (type) {
            case DOWN:
                finishRefresh(isSuccess);
                break;
            case UP:
                finishLoadmore(isSuccess);
                break;
        }
        pullableView.setVisibility(GONE);
        stateView.setVisibility(VISIBLE);
        stateView.showErrorTextWithAction(stateViewText, actionText, action);
        isRequesting.set(false);
    }

    /**
     * 带按钮
     *
     * @param type
     * @param isSuccess
     * @param stateViewText
     */
    public void finishRequestWithAction(PullType type, boolean isSuccess, int stateViewImageResId, String stateViewText, String actionText, Runnable action) {
        switch (type) {
            case DOWN:
                finishRefresh(isSuccess);
                break;
            case UP:
                finishLoadmore(isSuccess);
                break;
        }
        pullableView.setVisibility(GONE);
        stateView.setVisibility(VISIBLE);
        stateView.showErrorImageAndTextWithAction(stateViewImageResId, stateViewText, actionText, action);
        isRequesting.set(false);
    }

    public void showErrorImage(int imageResId) {
        pullableView.setVisibility(GONE);
        stateView.setVisibility(VISIBLE);
        stateView.showErrorImage(imageResId);
    }

    public void showErrorText(CharSequence message) {
        pullableView.setVisibility(GONE);
        stateView.setVisibility(VISIBLE);
        stateView.showErrorText(message);
    }

    public void showErrorImageAndText(int imageResId, CharSequence message) {
        pullableView.setVisibility(GONE);
        stateView.setVisibility(VISIBLE);
        stateView.showErrorImageAndText(imageResId, message);
    }

    public void showErrorImageWithRefreshAction(int stateViewImageResId) {
        pullableView.setVisibility(GONE);
        stateView.setVisibility(VISIBLE);
        stateView.showErrorImageWithAction(stateViewImageResId, "刷新", () -> {
            //正在请求的过程中，忽略
            if (isRequesting.get()) {
                return;
            }

            isRequesting.set(true);

            callback.onRefreshOrLoadMore(PullableViewContainer.this, PullType.DOWN, pageNum = startPageNumber, pageSize);
        });
    }

    public void showErrorTextWithRefreshAction(CharSequence stateViewMessage) {
        pullableView.setVisibility(GONE);
        stateView.setVisibility(VISIBLE);
        stateView.showErrorTextWithAction(stateViewMessage, "刷新", () -> {
            //正在请求的过程中，忽略
            if (isRequesting.get()) {
                return;
            }

            isRequesting.set(true);

            callback.onRefreshOrLoadMore(PullableViewContainer.this, PullType.DOWN, pageNum = startPageNumber, pageSize);
        });
    }

    public void showErrorImageAndTextWithRefreshAction(int stateViewImageResId, CharSequence stateViewMessage) {
        pullableView.setVisibility(GONE);
        stateView.setVisibility(VISIBLE);
        stateView.showErrorImageAndTextWithAction(stateViewImageResId, stateViewMessage, "刷新", () -> {
            //正在请求的过程中，忽略
            if (isRequesting.get()) {
                return;
            }

            isRequesting.set(true);

            callback.onRefreshOrLoadMore(PullableViewContainer.this, PullType.DOWN, pageNum = startPageNumber, pageSize);
        });
    }

    public void showErrorImageWithAction(int imageResId, String actionText, Runnable action) {
        pullableView.setVisibility(GONE);
        stateView.setVisibility(VISIBLE);
        stateView.showErrorImageWithAction(imageResId, actionText, action);
    }

    public void showErrorTextWithAction(CharSequence message, String actionText, Runnable action) {
        pullableView.setVisibility(GONE);
        stateView.setVisibility(VISIBLE);
        stateView.showErrorTextWithAction(message, actionText, action);
    }

    public void showErrorImageAndTextWithAction(int imageResId, CharSequence message, String actionText, Runnable action) {
        pullableView.setVisibility(GONE);
        stateView.setVisibility(VISIBLE);
        stateView.showErrorImageAndTextWithAction(imageResId, message, actionText, action);
    }

    public void setRefreshOrLoadMoreCallback(final RefreshOrLoadMoreCallback callback) {
        this.callback = callback;

        if (callback != null) {
            setOnRefreshListener(refreshLayout -> {
                //正在请求的过程中，忽略
                if (isRequesting.get()) {
                    return;
                }

                isRequesting.set(true);

                callback.onRefreshOrLoadMore(PullableViewContainer.this, PullType.DOWN, pageNum = startPageNumber, pageSize);
            });
            setOnLoadmoreListener(refreshLayout -> {
                //正在请求的过程中，忽略
                if (isRequesting.get()) {
                    return;
                }

                isRequesting.set(true);

                callback.onRefreshOrLoadMore(PullableViewContainer.this, PullType.UP, ++pageNum, pageSize);
            });

            pullableView.setVisibility(VISIBLE);
            stateView.setVisibility(GONE);

            //第一次主动调用
            if (isEnableRefresh()) {
                autoRefresh();
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
