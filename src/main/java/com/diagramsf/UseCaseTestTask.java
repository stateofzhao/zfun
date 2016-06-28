package com.diagramsf;

/**
 * Created by Diagrams on 2016/6/28 15:16
 */
public class UseCaseTestTask extends UseCase<UseCaseTestTask.Request, UseCaseTestTask.Response, UseCaseTestTask.ErrorResponse> {

    @Override
    public void execute(Request requestValue) {
        try {
            //模拟耗时任务
            Thread.sleep(2000);
            getCallback().onSucceed(new Response());
        } catch (InterruptedException e) {
            e.printStackTrace();
            getErrorListener().onError(new ErrorResponse());
        }
    }

    public static class Request implements UseCase.RequestValue {}

    public static class Response implements UseCase.ResponseValue {}

    public static class ErrorResponse implements UseCase.ErrorValue {}

    public static void get() {
        UseCaseHandler.instance().request(new Request()).error(new ErrorListener() {
            @Override
            public void onError(ErrorValue error) {

            }
        }).listener(new Listener() {
            @Override
            public void onSucceed(ResponseValue response) {

            }
        }).priority(UseCase.NORMAL).execute(new UseCaseTestTask());
    }
}
