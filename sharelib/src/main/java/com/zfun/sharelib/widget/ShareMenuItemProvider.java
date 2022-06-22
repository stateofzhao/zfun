package com.zfun.sharelib.widget;

import java.io.Serializable;

/**
 */
public class ShareMenuItemProvider implements Serializable {
    private static final long serialVersionUID = 1383453713093558451L;
    public int icon;
    public String name;
    public int type;
    public boolean isNew;
    public ShareMenuItemProvider(final int pIcon, final String pName, final int pType) {
        this.icon = pIcon;
        this.name = pName;
        this.type = pType;
    }
}
