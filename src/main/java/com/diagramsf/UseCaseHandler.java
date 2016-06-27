package com.diagramsf;

import android.support.annotation.NonNull;

/**
 * 处理{@link com.diagramsf.UseCase}
 * <p>
 * Created by Diagrams on 2016/6/27 11:52
 */
public class UseCaseHandler {

    private static volatile UseCaseHandler singleton;

    private UseCaseScheduler scheduler;

    private UseCaseHandler(UseCaseScheduler scheduler) {
        this.scheduler = scheduler;
    }

    public static UseCaseHandler instance() {
        if (null == singleton) {
            synchronized (UseCaseHandler.class) {
                if (null == singleton) {
                    singleton = new UseCaseHandler(new UseCaseThreadPoolScheduler());
                }
            }
        }
        return singleton;
    }

    private void execute(UseCase useCase) {

    }

    private <T extends UseCase.ResponseValue>
    void notifyResponse(T responseValue, final UseCase.Listener<T> listener) {
        scheduler.notifyResult(responseValue, listener);
    }

    private <E extends UseCase.ErrorValue>
    void error(E errorValue, final UseCase.ErrorListener<E> listener) {
        scheduler.error(errorValue, listener);
    }

    private static class ResultListenerWrapper<V extends UseCase.ResponseValue>
            implements UseCase.Listener<V> {
        UseCaseHandler handler;
        UseCase.Listener<V> listener;

        public ResultListenerWrapper(UseCaseHandler handler, UseCase.Listener<V> listener) {
            this.handler = handler;
            this.listener = listener;
        }

        @Override
        public void onSucceed(V response) {
            handler.notifyResponse(response, listener);
        }
    }//class ResultListenerWrapper end

    private static class ErrorListenerWrapper<E extends UseCase.ErrorValue> implements
            UseCase.ErrorListener<E> {
        UseCaseHandler handler;
        UseCase.ErrorListener<E> errorListener;

        public ErrorListenerWrapper(UseCaseHandler handler, UseCase.ErrorListener<E> errorListener){
            this.handler = handler;
            this.errorListener = errorListener;
        }

        @Override
        public void onError(E error) {
            handler.error(error,errorListener);
        }
    }// class ErrorListenerWrapper end

    private static class UseCaseCreator<E extends UseCase.ErrorValue, V extends UseCase.ResponseValue,
            R extends UseCase.RequestValue> {
        private UseCase.ErrorListener<E> errorListener;
        private UseCase.Listener<V> listener;
        private R requestValue;
        private Object tag;

        private UseCaseHandler handler;

        public UseCaseCreator(R requestValue,UseCaseHandler handler) {
            this.requestValue = requestValue;
            this.handler = handler;
        }

        public UseCaseCreator error(UseCase.ErrorListener<E> listener) {
            errorListener = listener;
            return this;
        }

        public UseCaseCreator listener(UseCase.Listener<V> listener) {
            this.listener = listener;
            return this;
        }

        public UseCaseCreator tag(Object tag) {
            this.tag = tag;
            return this;
        }

        public void execute(@NonNull UseCase<R, V, E> useCase) {
            useCase.setRequestValue(requestValue);
            useCase.setTag(tag);
            useCase.setListener(new ResultListenerWrapper<>(handler,listener));
            useCase.setErrorListener(new ErrorListenerWrapper<>(handler,errorListener));
            handler.execute(useCase);
        }

    }
}
