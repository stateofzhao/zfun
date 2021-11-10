package com.zfun.learn.architecture.effective.domain.interactor;

import com.zfun.learn.architecture.effective.datalayer.entity.CommentEntity;
import com.zfun.learn.architecture.effective.datalayer.repository.Repository;

import java.util.List;

public class GetComments extends Interactor<GetComments.Request, GetComments.Response>{
    private final Repository mRepository;
    public GetComments(Repository repository){
        mRepository = repository;
    }

    @Override
    void execute(Request requestValues) {
        int productId = requestValues.productId;
        Response response = new Response();
        response.list = mRepository.loadCommentsSync(productId);
        getCallback().onSuccess(response);
    }

    public static class Request implements Interactor.RequestValues{
        public int productId;
    }

    public static class Response implements Interactor.ResponseValues{
        public List<CommentEntity> list;
    }
}
