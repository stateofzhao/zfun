package com.zfun.processor.init;

public interface IInit {

    default int level(){ return Integer.MAX_VALUE;}

    /**
     * 会被多次调用，实现者应该根据 参数 来判断调用时机，然后做相应的初始化操作
     *
     * @param lifetime {@link InitLifetime}
     * */
    void init(InitLifetime lifetime);
}
