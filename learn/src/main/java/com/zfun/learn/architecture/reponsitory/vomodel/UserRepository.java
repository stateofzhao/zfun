package com.zfun.learn.architecture.reponsitory.vomodel;

//针对单一可信源解释下：
//为了保证数据的一致性，我们需要从同一个来源读取数据，比如，本地要展示的数据有两个来源：
//本地缓存CacheData，和网络数据NetData（获取后需要保存到CacheData中），
// 那么我们在读取数据时为了保证数据一致性，就需要始终以本地为准！
// 过程是先读取网络然后保存到本地之后再从本地读取，而不能是读取到网络后就显示网络数据（同时进行本地保存），
// 这样一旦保存出问题就不能做到数据一致性了（本地数据和显示的数据不一样）；
// 如果只有一个来源，那么就无所谓了，直接使用即可。
public class UserRepository {
    private final INetSource netSource;
    private final ICacheSource cacheSource;

    public UserRepository(INetSource netSource, ICacheSource cacheSource) {
        this.netSource = netSource;
        this.cacheSource = cacheSource;
    }

    public User getLoginUser() {
        return new User("test", 0);
    }
}
