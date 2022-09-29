package com.zfun.learn.architecture.clean.album.entities.album.type;

import com.zfun.learn.architecture.clean.album.entities.type.Author;

public class AlbumItem {
    public final String name;
    public final Author author;

    public AlbumItem(String name, Author author) {
        this.name = name;
        this.author = author;
    }
}
