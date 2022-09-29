package com.zfun.learn.architecture.reponsitory.entitymodel;

//只是简单的例子，这里看起来Entity于Repository的功能完全一致，但是还是有很大区别的：
// 这个UserEntity是保留在内存中的，它的状态会影响整个app的逻辑，
// 而Repository只是一个数仓，只负责存取数据，它不保留任何状态。
//ValueObject+Repository模式时，有时候就需要让Repository来承担一部分业务逻辑（保留业务状态），
// 所以ValueObject+Repository模式让Repository承担了不该有的职能，所以它不适合做复杂业务逻辑，
// 通常来说用ValueObject+Repository模式时也不会把逻辑写到Repository中，而是会把逻辑写到UI层，
// 总之对于复杂点的业务逻辑就要避免采用ValueObject+Repository模式。
public class UserEntity {
    private final IUserRepository repository;

    private UserType userType;

    public UserEntity(IUserRepository repository) {
        this.repository = repository;
    }

    public void init(){
        this.userType = repository.loadFromLocal();
    }

    public void login(final OnListener listener){
        repository.login(new IUserRepository.OnCallback() {
            @Override
            public void onSuc(UserType userType) {
                UserEntity.this.userType = userType;
                listener.onLoginSuc();
            }

            @Override
            public void onFail(int code, String msg) {
                listener.onFail(code,msg);
            }
        });
    }

    public UserType getLoginUser(){
        return userType;
    }

    public interface OnListener{
        void onLoginSuc();
        void onFail(int code,String msg);
    }//
}
