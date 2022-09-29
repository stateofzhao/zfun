/**
 * 数仓模式。<br/>
 * 有两种应用方式：<br/>
 * 1，Repository + ValueObject。<br/>
 *    public class Repository{
 *       ...
 *       //User为ValueObject类型
 *       public User getUser(){
 *           ...
 *       }
 *
 *       public void update(User user){
 *           ...
 *       }
 *       ...
 *    }
 * 2，充血实体 + Repository。<br/>
 *    public class User{
 *        private Repository rep;
 *        public User(Repository rep){
 *              this.rep = rep;
 *        }
 *
 *        public void update(String name){
 *              rep.update(name);
 *        }
 *    }
 *
 *  <P/>
 *  对于简单的需求，例如，纯页面展示的，使用数仓模式即可。<br/>
 *
 * */
package com.zfun.learn.architecture.reponsitory;