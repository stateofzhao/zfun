package com.zfun.learn.architecture.clean.comment.entities;

import com.zfun.learn.architecture.clean.comment.entities.type.HostInfo;
import com.zfun.learn.architecture.clean.comment.entities.type.OptResult;
import com.zfun.learn.architecture.clean.comment.entities.type.Comment;

import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * 【示例】<br/>
 * 评论业务实体
 */
public class CommentEntity {
    private final HostInfo hostInfo;
    private final IRep rep;
    private final ITools tools;
    //
    private final Set<OnCallback> callbacks = new CopyOnWriteArraySet<>();
    private final Map<Integer, List<Comment>> commentMap = Collections.synchronizedMap(new HashMap<>());
    private boolean isRelease = false;

    public CommentEntity(HostInfo hostInfo, IRep rep, ITools tools) {
        this.hostInfo = hostInfo;
        this.rep = rep;
        this.tools = tools;
        assert null != rep && null != hostInfo && null != tools;
    }

    public void addCallback(OnCallback callback) {
        if (null == callback) {
            return;
        }
        callbacks.add(callback);
    }

    public void removeCallback(OnCallback callback) {
        if (null == callback) {
            return;
        }
        callbacks.remove(callback);
    }

    public void release(){
        isRelease = true;
        rep.release();
        tools.release();
        callbacks.clear();
        commentMap.clear();
    }

    public OptResult<Comment> postComment(Comment comment) {
        final OptResult<Comment> result = rep.postComment(comment, hostInfo);
        if (result.isOk()) {
            insertComment(result.result);
            notifyCallbacks("post", result.result);
        }
        return result;
    }

    public OptResult<List<Comment>> getCommentList(int type) {
        return rep.getCommentList(type, hostInfo);
        //todo lzf 更新 commentMap
    }

    public OptResult<Comment> getCommentInfo(long commentId) {
        return rep.getCommentInfo(commentId, hostInfo);
        //todo lzf 更新 commentMap
    }

    public OptResult<Long> delete(long commentId) {
        final OptResult<Long> result = rep.delete(commentId, hostInfo);
        if (result.isOk()) {
            removeComment(commentId);
            notifyCallbacks("delete", result.result);
        }
        return result;
    }

    public OptResult<Comment> editComment(Comment comment){
        return rep.editComment(comment,hostInfo);
        //todo lzf 更新 commentMap
    }

    private void insertComment(Comment comment) {
        tools.runMainThread(() -> {
            if(isRelease){
                return;
            }
            final int type = comment.type;
            List<Comment> typeCommentList = commentMap.get(type);
            if (null != typeCommentList) {
                typeCommentList.add(comment);
            } else {
                typeCommentList = new ArrayList<>();
                typeCommentList.add(comment);
                commentMap.put(type, typeCommentList);
            }
        });
    }

    private void removeComment(final long commentId) {
        tools.runMainThread(() -> {
            if(isRelease){
                return;
            }
            for (Integer aKey : commentMap.keySet()) {
                final List<Comment> typeCommentList = commentMap.get(aKey);
                if (null != typeCommentList) {
                    for (Comment aComment : typeCommentList) {
                        if (commentId == aComment.id) {
                            typeCommentList.remove(aComment);
                            break;
                        }
                    }
                }
            }
        });
    }

    private void notifyCallbacks(final String opt, final Object object) {
        tools.runMainThread(() -> {
            if(isRelease){
                return;
            }
            if ("post".equals(opt)) {
                for (OnCallback aCallback : callbacks) {
                    aCallback.onCommentAdd(hostInfo, (Comment) object);
                }
            } else if ("delete".equals(opt)) {
                for (OnCallback aCallback : callbacks) {
                    aCallback.onCommentDelete(hostInfo, (Long) object);
                }
            }
        });
    }

    interface OnCallback {
        void onDataChanged(HostInfo hostInfo);

        void onCommentDelete(HostInfo hostInfo, long commentId);

        void onCommentAdd(HostInfo hostInfo, Comment comment);

        void onCommentItemUpdate(HostInfo hostInfo, Comment comment);
    }//
}
