package com.lcodecore.tkrefreshlayout;

import android.view.ViewGroup;

public class HeaderBo {
    public static final String HEAD_STICKER = "head_sticker";
    public static final String HEAD_FOLLOW = "head_follow";
    public static final String HEAD_PULL_TO_REF = "head_pull_to_ref";
    public static final String HEAD_PURE = "head_pure";
    private ViewGroup headLayout;
    private String upType;//sticker,follow,pullToRef,pure
    private int id;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public ViewGroup getHeadLayout() {
        return headLayout;
    }

    public void setHeadLayout(ViewGroup headLayout) {
        this.headLayout = headLayout;
    }

    public String getUpType() {
        return upType;
    }

    public void setUpType(String upType) {
        this.upType = upType;
    }

    public ViewGroup getMyHeader() {
        getHeadLayout().setId(getId());
        return getHeadLayout();
    }
}
