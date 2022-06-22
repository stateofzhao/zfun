package com.zfun.sharelib.init;

public interface IHttpPicDownloader {
    //根据url来获取图片，返回图片存储的本地路径
    String downPic(String url);
}
