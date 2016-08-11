package com.diagramsf.domain.example;

/**
 * Created by Diagrams on 2016/8/11 10:25
 */
public interface TeacherRepository {
  void getTeacherById(String id, Object tag, GetTeacherById.Callback callback);

  void cancel(Object tag);
}
