package com.zfun.learn.architecture.effective.datalayer.cache.db.dao;

import com.zfun.learn.architecture.effective.datalayer.entity.CommentEntity;

import java.util.List;

//数据库链接接口
public interface CommentDao {
    List<CommentEntity> loadCommentsSync(int productId);

    void insertAll(List<CommentEntity> comments);
}
