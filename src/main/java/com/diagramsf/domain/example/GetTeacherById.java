package com.diagramsf.domain.example;

/**
 * Created by Diagrams on 2016/8/11 10:30
 */
public interface GetTeacherById {
  interface Callback {
    void onResponse(TeacherEntity teacherEntity);

    void onError(Exception e);
  }

  void execute(String id, Object tag, Callback callback);

  void cancel(Object tag);
}
