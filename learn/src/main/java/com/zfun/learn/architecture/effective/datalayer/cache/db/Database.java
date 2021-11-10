package com.zfun.learn.architecture.effective.datalayer.cache.db;

import com.zfun.learn.architecture.effective.datalayer.cache.db.dao.CommentDao;
import com.zfun.learn.architecture.effective.datalayer.entity.CommentEntity;

import java.util.List;
//一般为单例
public class Database implements CommentDao {
    @Override
    public List<CommentEntity> loadCommentsSync(int productId) {
        return null;
    }

    @Override
    public void insertAll(List<CommentEntity> comments) {

    }
}
