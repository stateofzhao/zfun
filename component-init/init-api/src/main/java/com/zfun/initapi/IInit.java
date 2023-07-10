package com.zfun.initapi;

public interface IInit {
    interface Callback{
        void end();
    }//

    /**
     * 会被多次调用，实现者应该根据 参数 来判断调用时机，然后做相应的初始化操作
     *
     * @param lifecycle {@link IInitLifecycle}
     * @param callback 可以为空
     * */
    void init(IInitLifecycle lifecycle, Callback callback);
}
