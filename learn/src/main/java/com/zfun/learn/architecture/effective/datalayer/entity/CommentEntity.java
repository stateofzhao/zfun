package com.zfun.learn.architecture.effective.datalayer.entity;

import com.zfun.learn.architecture.effective.datalayer.model.Comment;

import java.util.Date;

public class CommentEntity implements Comment {
    @Override
    public int getId() {
        return 0;
    }

    @Override
    public int getProductId() {
        return 0;
    }

    @Override
    public String getText() {
        return null;
    }

    @Override
    public Date getPostedAt() {
        return null;
    }
}
