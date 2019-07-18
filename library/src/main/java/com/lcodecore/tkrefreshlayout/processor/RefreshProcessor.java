package com.lcodecore.tkrefreshlayout.processor;

import android.view.MotionEvent;
import android.view.ViewConfiguration;

import com.lcodecore.tkrefreshlayout.TwinklingRefreshLayout;
import com.lcodecore.tkrefreshlayout.utils.ScrollingUtil;

import static com.lcodecore.tkrefreshlayout.TwinklingRefreshLayout.CoContext.HEAD_ALL_SHOW;
import static com.lcodecore.tkrefreshlayout.TwinklingRefreshLayout.CoContext.HEAD_HIDE;
import static com.lcodecore.tkrefreshlayout.TwinklingRefreshLayout.CoContext.PULLING_BOTTOM_UP;
import static com.lcodecore.tkrefreshlayout.TwinklingRefreshLayout.CoContext.PULLING_HEAD_DOWN;
import static com.lcodecore.tkrefreshlayout.TwinklingRefreshLayout.CoContext.PULLING_HEAD_UP;
import static com.lcodecore.tkrefreshlayout.TwinklingRefreshLayout.CoContext.PULLING_TOP_DOWN;

/**
 * Created by lcodecore on 2017/3/1.
 */

public class RefreshProcessor implements IDecorator {
    protected TwinklingRefreshLayout.CoContext cp;
    private float mTouchX, mTouchY;
    private boolean intercepted = false;
    private boolean willAnimHead = false;
    private boolean willAnimBottom = false;
    private boolean downEventSent = false;
    private MotionEvent mLastMoveEvent;

    public RefreshProcessor(TwinklingRefreshLayout.CoContext processor) {
        if (processor == null) throw new NullPointerException("The coprocessor can not be null.");
        cp = processor;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                downEventSent = false;
                intercepted = false;
                mTouchX = ev.getX();
                mTouchY = ev.getY();

                if (cp.isEnableKeepIView()) {
                    if (!cp.isRefreshing()) {
                        cp.setPrepareFinishRefresh(false);
                    }
                    if (!cp.isLoadingMore()) {
                        cp.setPrepareFinishLoadMore(false);
                    }
                }

                cp.dispatchTouchEventSuper(ev);
                return true;
            case MotionEvent.ACTION_MOVE:
                mLastMoveEvent = ev;
                float dx = ev.getX() - mTouchX;
                float dy = ev.getY() - mTouchY;

//                if (direction < Math.abs(dy)) {
//                    direction = Math.abs(dy);
//                    System.out.println("==================A============" + direction);
//                } else {
//                    direction = 0;
//                    mTouchY = ev.getY();
//                    sendDownEvent();
//                    System.out.println("==================B============" + direction);
//                }

//                System.out.println("==============================" + cp.headerPos + "=============" + dy);
                if (Math.abs(dx) <= Math.abs(dy) && Math.abs(dy) > cp.getTouchSlop()) {//滑动允许最大角度为45度
                    if (!intercepted) {
                        if (dy > 0 && cp.allowPullDown()) {
                            if (!cp.headerPos.equals(HEAD_ALL_SHOW) && ScrollingUtil.isViewToTop(cp.getTargetView(), cp.getTouchSlop())) {
                                System.out.println("=================================PHD"+cp.lastSaveH);
                                cp.setStatePHD();
                                return callIntercepted(ev.getX(), ev.getY());
                            } else if (ScrollingUtil.isViewToTop(cp.getTargetView(), cp.getTouchSlop())) {
                                System.out.println("=================================PTD");
                                cp.lastSaveH = 0;
                                cp.setStatePTD();
                                return callIntercepted(ev.getX(), ev.getY());
                            }
                        } else if (dy < 0 && cp.allowPullUp()) {
                            if (!cp.headerPos.equals(HEAD_HIDE)) {
                                System.out.println("=================================PHU");
                                cp.setStatePHU();
                                return callIntercepted(ev.getX(), ev.getY());
                            } else if (ScrollingUtil.isViewToBottom(cp.getTargetView(), cp.getTouchSlop())) {
                                cp.setStatePBU();
                                return callIntercepted(ev.getX(), ev.getY());
                            }
                        }
                    } else {
                        if (dy < 0 && cp.headerPos.equals(HEAD_ALL_SHOW)) {
                            cp.setStatePHU();
                            System.out.println("=================================PHU out");
                            return callIntercepted(ev.getX(), ev.getY());
                        } else if (dy > 0 && cp.headerPos.equals(HEAD_HIDE)) {
                            cp.setStatePHD();
                            System.out.println("=================================PHD out");
                            return callIntercepted(ev.getX(), ev.getY());
                        }
                    }
                }
                if (intercepted) {
                    if (cp.isRefreshVisible() || cp.isLoadingVisible()) {
                        return cp.dispatchTouchEventSuper(ev);
                    }
                    switch (cp.state) {
                        case PULLING_TOP_DOWN:
                            if (!cp.isPrepareFinishRefresh()) {
                                if (dy < -cp.getTouchSlop() || !ScrollingUtil.isViewToTop(cp.getTargetView(), cp.getTouchSlop())) {
                                    cp.dispatchTouchEventSuper(ev);
                                }
                                dy = Math.min(cp.getMaxHeadHeight() * 2, dy);
                                dy = Math.max(0, dy);
                                cp.getAnimProcessor().scrollHeadByMove(dy);
                            }
                            break;
                        case PULLING_HEAD_DOWN:
                            if (!cp.isPrepareFinishRefresh()) {
                                if (dy < -cp.getTouchSlop() || cp.headerPos.equals(HEAD_ALL_SHOW)) {
                                    cp.dispatchTouchEventSuper(ev);
                                }//dy>0
                                dy += cp.lastSaveH;
                                dy = Math.max(-cp.allHeadH * 2, dy);
                                dy = Math.min(0, dy);
                                if (dy == 0f || dy == -cp.allHeadH * 2) {
                                    mTouchY = ev.getY();
                                    intercepted = false;
                                }
                                cp.getAnimProcessor().scrollStickerHeadByMoveUp(dy);
                            }
                            break;
                        case PULLING_HEAD_UP:
                            if (!cp.isPrepareFinishRefresh()) {
                                if (dy > cp.getTouchSlop() || cp.headerPos.equals(HEAD_HIDE)) {
                                    cp.dispatchTouchEventSuper(ev);
                                }//dy<0
                                dy += cp.lastSaveH;
                                dy = Math.max(-cp.allHeadH * 2, dy);
                                dy = Math.min(0, dy);
                                if (dy == 0f || dy == -cp.allHeadH * 2) {
                                    mTouchY = ev.getY();
                                    intercepted = false;
                                }
                                cp.getAnimProcessor().scrollStickerHeadByMoveUp(dy);
                            }
                            break;
                        case PULLING_BOTTOM_UP:
                            if (!cp.isPrepareFinishLoadMore()) {
                                if (dy > cp.getTouchSlop() || !ScrollingUtil.isViewToBottom(cp.getTargetView(), cp.getTouchSlop())) {
                                    cp.dispatchTouchEventSuper(ev);
                                }
                                dy = Math.max(-cp.getMaxBottomHeight() * 2, dy);
                                dy = Math.min(0, dy);
                                cp.getAnimProcessor().scrollBottomByMove(Math.abs(dy));
                            }
                            break;
                    }
                    if ((dy == 0 || Math.abs(dy) == cp.allHeadH * 2) && !downEventSent) {
                        downEventSent = true;
                        sendDownEvent();
                    }
                    return true;
                }
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                cp.lastSaveH += ev.getY() - mTouchY;
                cp.lastSaveH = Math.max(-cp.getMaxBottomHeight() * 2, cp.lastSaveH);
                cp.lastSaveH = Math.min(0, cp.lastSaveH);

                if (intercepted) {
                    if (cp.isStatePTD()) {
                        willAnimHead = true;
                    } else if (cp.isStatePBU()) {
                        willAnimBottom = true;
                    }
                    intercepted = false;
                    return true;
                }
                break;
        }
        return cp.dispatchTouchEventSuper(ev);
    }

    private boolean callIntercepted(float x, float y) {
        mTouchX = x;
        mTouchY = y;
        sendCancelEvent();
        intercepted = true;
        System.out.println("======================cancel");
        return true;
    }

    //发送cancel事件解决selection问题
    private void sendCancelEvent() {
        if (mLastMoveEvent == null) {
            return;
        }
        MotionEvent last = mLastMoveEvent;
        MotionEvent e = MotionEvent.obtain(last.getDownTime(), last.getEventTime() + ViewConfiguration.getLongPressTimeout(), MotionEvent.ACTION_CANCEL, last.getX(), last.getY(), last.getMetaState());
        cp.dispatchTouchEventSuper(e);
    }

    private void sendDownEvent() {
        final MotionEvent last = mLastMoveEvent;
        MotionEvent e = MotionEvent.obtain(last.getDownTime(), last.getEventTime(), MotionEvent.ACTION_DOWN, last.getX(), last.getY(), last.getMetaState());
        cp.dispatchTouchEventSuper(e);
    }

    @Override
    public boolean interceptTouchEvent(MotionEvent ev) {
        return false;
    }

    @Override
    public boolean dealTouchEvent(MotionEvent e) {
        return false;
    }

    @Override
    public void onFingerDown(MotionEvent ev) {
        System.out.println("==========================onFingerDown");
    }

    @Override
    public void onFingerUp(MotionEvent ev, boolean isFling) {
        System.out.println("==========================onFingerUp");
        if (!isFling && willAnimHead) {
            cp.getAnimProcessor().dealPullDownRelease();
        }
        if (!isFling && willAnimBottom) {
            cp.getAnimProcessor().dealPullUpRelease();
        }
        willAnimHead = false;
        willAnimBottom = false;
    }

    @Override
    public void onFingerScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY, float velocityX, float velocityY) {
        System.out.println("==========================onFingerScroll");
        //手指在屏幕上滚动，如果此时正处在刷新状态，可隐藏
        int mTouchSlop = cp.getTouchSlop();
        if (cp.isRefreshVisible() && distanceY >= mTouchSlop && !cp.isOpenFloatRefresh()) {
            cp.getAnimProcessor().animHeadHideByVy((int) velocityY);
        }
        if (cp.isLoadingVisible() && distanceY <= -mTouchSlop) {
            cp.getAnimProcessor().animBottomHideByVy((int) velocityY);
        }
    }

    @Override
    public void onFingerFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        System.out.println("==========================fling");
    }
}
