package com.diagramsf.datalayer.example;

import com.diagramsf.domain.example.StudentEntity;

/**
 * {@link com.diagramsf.domain.example.StudentRepository} 实现类
 *
 * Created by Diagrams on 2016/8/9 11:53
 */
public class StudentDataRepository implements com.diagramsf.domain.example.StudentRepository {
  @Override public StudentEntity getStudentById(String id) {
    return new StudentEntity();
  }
}
