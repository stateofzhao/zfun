package com.diagramsf.domain.example;

import com.diagramsf.executor.BaseInteractor;
import com.diagramsf.executor.FireMainThread;
import com.diagramsf.executor.FireThread;

/**
 * Created by Diagrams on 2016/8/11 10:25
 */
public class GetTeacherByIdInteractor extends BaseInteractor implements GetTeacherById {
  FireThread postThread;
  Callback callback;
  Object tag;
  TeacherRepository repository;
  String id;

  public GetTeacherByIdInteractor(TeacherRepository repository) {
    postThread = new FireMainThread(this);
    this.repository = repository;
  }

  @Override public void run() {
    //如果这里也是使用了线程开请求数据，那么run()方法会立即返回，那么此Interactor就会超出
    //Executor的作用域（因为Interactor对于Executor来说是已经完成状态）。
    //所以取消任务时，不光要让Executor取消此任务，还需要Repository来取消它的网络加载。
    repository.getTeacherById(id, tag, new Callback() {
      @Override public void onResponse(TeacherEntity teacherEntity) {
        notifyResponse(teacherEntity);
      }

      @Override public void onError(Exception e) {
        notifyError(e);
      }
    });
  }

  @Override public void execute(String id, Object tag, Callback callback) {
    this.callback = callback;
    this.tag = tag;
    this.id = id;
    run();//直接执行即可，没必要非得使用Executor来执行。
    //InteractorHandler.instance().execute(this, tag);
  }

  @Override public void cancel(Object tag) {
    //InteractorHandler.instance().cancel(tag);
    cancel();
    repository.cancel(tag);
  }

  private void notifyResponse(final TeacherEntity teacherEntity) {
    postThread.post(new Runnable() {
      @Override public void run() {
        callback.onResponse(teacherEntity);
      }
    });
  }

  private void notifyError(final Exception e) {
    postThread.post(new Runnable() {
      @Override public void run() {
        callback.onError(e);
      }
    });
  }
}
