package com.zfun.learn.architecture.clean.comment.entities.type;

import java.util.List;

public class Comment {
    public final long id;
    public final int type;
    public final String commentStr;
    public final List<ImageInfo> imageInfos;

    public Comment(long id,int type,String commentStr, List<ImageInfo> imageInfos) {
        this.id = id;
        this.type = type;
        this.commentStr = commentStr;
        this.imageInfos = imageInfos;
    }
}
