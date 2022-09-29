package com.zfun.learn.architecture.clean.album.entities.music;

import com.zfun.learn.architecture.clean.album.entities.type.Author;

public class MusicItem {
    public final String title;
    public final Author author;

    public MusicItem(String title, Author author) {
        this.title = title;
        this.author = author;
    }
}
