package com.diagramsf.domain.example;

/**
 * Created by Diagrams on 2016/8/11 10:29
 */
public interface GetStudentById {
  interface Callback {
    void onResponse(StudentEntity entity);

    void onError();
  }

  void execute(Object tag, Callback callback);

  void cancel(Object tag);
}
