package com.zfun.learn.architecture.effective.datalayer.repository;

import com.zfun.learn.architecture.effective.datalayer.cache.db.Database;
import com.zfun.learn.architecture.effective.datalayer.entity.CommentEntity;
import com.zfun.learn.architecture.effective.datalayer.net.WebService;

import java.util.List;

public class Repository {
    private WebService mWebService;
    private Database mDatabase;

    public List<CommentEntity> loadCommentsSync(int productId){
        refreshComments(productId);
        return mDatabase.loadCommentsSync(productId);
    }

    private void refreshComments(int productId){
        List<CommentEntity> comments = mDatabase.loadCommentsSync(productId);
        if(null == comments||comments.size() == 0){
            comments = mWebService.getComment(productId);
            mDatabase.insertAll(comments);
        }
    }

    private static Repository sInstance;
    private Repository(){}
    private Repository(WebService webService, Database database) {
        mWebService = webService;
        mDatabase = database;
    }

    public static Repository getInstance(WebService webService, Database database) {
        if (null == sInstance) {
            synchronized (Repository.class) {
                if (null == sInstance) {
                    sInstance = new Repository(webService,database);
                }
            }
        }
        return sInstance;
    }
}
