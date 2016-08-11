package com.diagramsf.domain.example;

import com.diagramsf.datalayer.example.StudentDataRepository;
import com.diagramsf.executor.FireMainThread;
import com.diagramsf.executor.FireThread;
import com.diagramsf.executor.Interactor;
import com.diagramsf.executor.InteractorHandler;

/**
 * {@link Interactor}示例
 *
 * Created by Diagrams on 2016/8/9 11:48
 */
public class GetStudentByIdInteractor extends BaseInteractor {
  private static final long WAIT_TIME = 1500;

  private StudentRepository repository;
  private String id;
  private FireThread postThread;
  private StudentRepository.Callback callback;

  public GetStudentByIdInteractor(String id) {
    repository = new StudentDataRepository();
    postThread = new FireMainThread(this);
    this.id = id;
  }

  public void execute(Object tag, StudentRepository.Callback callback) {
    this.callback = callback;
    InteractorHandler.instance().execute(this, tag);
  }

  @Override public void run() {
    waitToDoThisSampleMoreInteresting();
    try {
      final StudentEntity studentEntity = repository.getStudentById(id);
      notifyResponse(studentEntity);
    } catch (Exception e) {
      notifyError();
    }
  }

  private void waitToDoThisSampleMoreInteresting() {
    try {
      Thread.sleep(WAIT_TIME);
    } catch (InterruptedException e) {
      notifyError();
    }
  }

  private void notifyResponse(final StudentEntity studentEntity) {
    postThread.post(new Runnable() {
      @Override public void run() {
        callback.onResponse(studentEntity);
      }
    });
  }

  private void notifyError() {
    postThread.post(new Runnable() {
      @Override public void run() {
        callback.onError();
      }
    });
  }
}
