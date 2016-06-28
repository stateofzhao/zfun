package com.diagramsf;

import android.support.annotation.NonNull;

/**
 * 处理{@link com.diagramsf.UseCase}
 * <p/>
 * Created by Diagrams on 2016/6/27 11:52
 */
public class UseCaseHandler {

    private static volatile UseCaseHandler singleton;

    private UseCaseScheduler scheduler;

    private UseCaseHandler() {
    }

    private void setUseCaseScheduler(UseCaseScheduler scheduler) {
        this.scheduler = scheduler;
    }

    public static UseCaseHandler instance() {
        if (null == singleton) {
            synchronized (UseCaseHandler.class) {
                if (null == singleton) {
                    singleton = new UseCaseHandler();
                    singleton.setUseCaseScheduler(new UseCaseThreadPoolScheduler());
                }
            }
        }
        return singleton;
    }

    public <T extends UseCase.RequestValue> UseCaseDecorator request(T requestValue) {
        return new UseCaseDecorator(requestValue, this);
    }

    public void cancel(Object tag) {
        scheduler.cancel(tag);
    }

    private void execute(UseCase useCase) {
        scheduler.execute(useCase);
    }

    private <T extends UseCase.ResponseValue> void notifyResponse(T responseValue, @NonNull UseCase.Listener<T> listener) {
        scheduler.notifyResult(responseValue, listener);
    }

    private <E extends UseCase.ErrorValue> void error(E errorValue, @NonNull UseCase.ErrorListener<E> listener) {
        scheduler.error(errorValue, listener);
    }

    private static class ResultListenerWrapper<V extends UseCase.ResponseValue>
            implements UseCase.Listener<V> {
        UseCaseHandler handler;
        UseCase.Listener<V> listener;
        UseCase useCase;

        public ResultListenerWrapper(UseCase useCase, UseCaseHandler handler, UseCase.Listener<V> listener) {
            this.handler = handler;
            this.listener = listener;
            this.useCase = useCase;
        }

        @Override
        public void onSucceed(V response) {
            if (!useCase.isCacnel()) {
                handler.notifyResponse(response, listener);
            }
        }
    }//class ResultListenerWrapper end

    private static class ErrorListenerWrapper<E extends UseCase.ErrorValue> implements
            UseCase.ErrorListener<E> {
        UseCaseHandler handler;
        UseCase.ErrorListener<E> errorListener;
        UseCase useCase;

        public ErrorListenerWrapper(UseCase useCase, UseCaseHandler handler, UseCase.ErrorListener<E> errorListener) {
            this.handler = handler;
            this.errorListener = errorListener;
            this.useCase = useCase;
        }

        @Override
        public void onError(E error) {
            if (!useCase.isCacnel()) {
                handler.error(error, errorListener);
            }
        }
    }// class ErrorListenerWrapper end

    public static class UseCaseDecorator {
        private UseCase.ErrorListener errorListener;
        private UseCase.Listener listener;
        private UseCase.RequestValue requestValue;
        private Object tag;
        private int priority = UseCase.NORMAL;

        private UseCaseHandler handler;

        public UseCaseDecorator(UseCase.RequestValue requestValue, UseCaseHandler handler) {
            this.requestValue = requestValue;
            this.handler = handler;
        }

        public UseCaseDecorator error(UseCase.ErrorListener listener) {
            errorListener = listener;
            return this;
        }

        public UseCaseDecorator listener(UseCase.Listener listener) {
            this.listener = listener;
            return this;
        }

        public UseCaseDecorator tag(Object tag) {
            this.tag = tag;
            return this;
        }

        public UseCaseDecorator priority(@UseCase.Type int priority) {
            this.priority = priority;
            return this;
        }

        public void execute(@NonNull UseCase useCase) {
            useCase.setRequestValue(requestValue);
            useCase.setTag(tag);
            useCase.setListener(new ResultListenerWrapper<>(useCase, handler, listener));
            useCase.setErrorListener(new ErrorListenerWrapper<>(useCase, handler, errorListener));
            useCase.setPriority(priority);
            handler.execute(useCase);
        }
    } // class UseCaseDecorator end
}
