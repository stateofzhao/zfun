package com.zfun.learn.architecture.clean.comment.entities;

import com.zfun.learn.architecture.clean.comment.entities.type.HostInfo;
import com.zfun.learn.architecture.clean.comment.entities.type.OptResult;
import com.zfun.learn.architecture.clean.comment.entities.type.Comment;

import java.util.Collections;
import java.util.List;

public interface IRep {

    default OptResult<List<Comment>> getCommentList(int type, HostInfo hostInfo) {
        return new OptResult<List<Comment>>(-1, "default", Collections.emptyList()) {

            @Override
            public boolean isOk() {
                return false;
            }
        };
    }

    OptResult<Comment> postComment(Comment comment, HostInfo hostInfo);

    OptResult<Comment> getCommentInfo(long commentId, HostInfo hostInfo);

    OptResult<Comment> editComment(Comment comment, HostInfo hostInfo);

    OptResult<Long> delete(long commentId, HostInfo hostInfo);

    void release();
}
