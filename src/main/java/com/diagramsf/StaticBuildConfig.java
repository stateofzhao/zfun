package com.diagramsf;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Diagrams on 2015/6/30 10:42
 * <p>
 * 框架的静态配置文件，这样方便动态的配置整个框架
 */
public class StaticBuildConfig {

    //======================文件存储相关
    public final static String ROOT_FILE_PATH = "Diagrams";
    public final static String IMAGE_FILE_SAVE_HEAD_PATH = ROOT_FILE_PATH + "/cache/image/"; // 图片缓存
    public final static String DATA_FILE_SAVE_HEAD_PATH = ROOT_FILE_PATH + "/"+ "COMM"+"/";// 数据存储

    //=====================异常相关
    public static final String CRASH_LOG_PATH = "/xcar/Log/";//日志存放目录
    public static final String CRASH_REPORT_EMAIL = "";//崩溃通知邮箱
    public static final String CRASH_REPORT_EXTRA_SUBJECT = "爱卡汽车Android客户端 - 错误报告";
    public static final String CRASH_REPORT_CHOOSE_TITLE = "发送错误报告";

    //======================服务器返回的结果码配置,不是报头信息中的结果码，是解析出的数据中的结果码
    public static final Map<String, String> SERVICE_ERROR_CODE = new HashMap<>();

    static {
        SERVICE_ERROR_CODE.put("1", "无错误");
        SERVICE_ERROR_CODE.put("16", "服务器操作失败");
        SERVICE_ERROR_CODE.put("200", "尚未登录");
        SERVICE_ERROR_CODE.put("201", "密码错误");
        SERVICE_ERROR_CODE.put("202", "用户名不存在");
        SERVICE_ERROR_CODE.put("203", "未知原因的登录失败");
        SERVICE_ERROR_CODE.put("300", "绑定第三方失败");
        SERVICE_ERROR_CODE.put("301", "此用户名已经被使用");
        SERVICE_ERROR_CODE.put("400", "使用的Token已过期");
        SERVICE_ERROR_CODE.put("404", "话题不存在");
    }

}