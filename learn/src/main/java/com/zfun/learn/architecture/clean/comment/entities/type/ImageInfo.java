package com.zfun.learn.architecture.clean.comment.entities.type;

public class ImageInfo {
    public final String imageUrl;
    public final int imageWidth;
    public final int imageHeight;

    public ImageInfo(String imageUrl, int imageWidth, int imageHeight){
        this.imageUrl = imageUrl;

        this.imageWidth = imageWidth;
        this.imageHeight = imageHeight;
    }
}
