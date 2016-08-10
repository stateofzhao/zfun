package com.diagramsf.domain.example;

/**
 * 与Data Layer交互的示例接口。
 *
 * Created by Diagrams on 2016/8/9 11:35
 */
public interface StudentRepository {
  interface Callback {
    void onResponse(StudentEntity entity);

    void onError();
  }

  StudentEntity getStudentById(String id);
}
