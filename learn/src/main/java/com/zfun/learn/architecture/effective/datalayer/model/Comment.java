package com.zfun.learn.architecture.effective.datalayer.model;

import java.util.Date;

public interface Comment {
    int getId();
    int getProductId();
    String getText();
    Date getPostedAt();
}
