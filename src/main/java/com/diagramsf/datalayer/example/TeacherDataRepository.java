package com.diagramsf.datalayer.example;

import com.diagramsf.domain.example.GetTeacherById;
import com.diagramsf.domain.example.TeacherRepository;

/**
 * Created by Diagrams on 2016/8/11 11:06
 */
public class TeacherDataRepository implements TeacherRepository {
  @Override public void getTeacherById(String id, Object tag, GetTeacherById.Callback callback) {
    String url =createURL(id);
    //在这里进行网络请求

  }

  @Override public void cancel(Object tag) {
    //在这里取消网络请求
  }

  String createURL(String id){
    return id;
  }
}
