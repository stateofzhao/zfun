package com.zfun.learn.architecture.clean.album.entities.type;

//构造函数参数比较多or相同类型并列时，可以考虑采用Builder模式来构建
public class Author {
    public final String name;
    public final String sex;
    public final String age;

    public Author(String name, String sex, String age) {
        this.name = name;
        this.sex = sex;
        this.age = age;
    }
}
