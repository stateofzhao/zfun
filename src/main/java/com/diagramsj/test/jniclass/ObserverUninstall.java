package com.diagramsj.test.jniclass;

/**
 * 监听卸载
 * <p/>
 * Created by Diagrams on 2015/11/20 11:06
 */
public class ObserverUninstall {

    static {
        System.loadLibrary("ObserverUninstall");
    }

    /**
     * 初始化监听卸载
     *
     * @param userSerial Android系统当前登录用户的描述
     * @param url        统计卸载的URL
     * @param host       根域名，例如：a.xcar.com.cn
     *
     * @return 返回pid，linux子进程id
     */
    public native int init(String userSerial, String url, String host);

    /**
     * 更新卸载监听的数据值
     *
     * @param userSerial Android系统当前登录用户的描述
     * @param url        统计卸载的URL
     * @param host       根域名，例如：a.xcar.com.cn
     */
    public native int updateData(String userSerial, String url, String host);
}
