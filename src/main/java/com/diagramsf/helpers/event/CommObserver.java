package com.diagramsf.helpers.event;

import java.io.Serializable;


/** 一般订阅者，只有一个订阅方法*/
public interface CommObserver<T extends Serializable> {
	 void doSomething(T obj);
}
