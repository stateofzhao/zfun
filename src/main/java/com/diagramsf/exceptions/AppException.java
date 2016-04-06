package com.diagramsf.exceptions;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.os.Looper;
import com.diagramsf.R;
import com.diagramsf.StaticBuildConfig;
import com.diagramsf.helpers.AppManager;
import com.diagramsf.helpers.UIHelper;
import org.apache.http.HttpException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.Thread.UncaughtExceptionHandler;
import java.net.ConnectException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Date;

/**
 * 应用程序异常类：用于捕获异常和提示错误信息
 * <p>
 * 修改自：liux (http://my.oschina.net/liux)
 */
public class AppException extends Exception implements UncaughtExceptionHandler {

    public static final String LOG_PATH = StaticBuildConfig.CRASH_LOG_PATH;//日志存放目录
    public static final String CRASH_REPART_EMAIL = StaticBuildConfig.CRASH_REPORT_EMAIL;//崩溃通知邮箱
    public static final String CRASH_REPART_EXTRA_SUBJECT = StaticBuildConfig.CRASH_REPORT_EXTRA_SUBJECT;
    public static final String CRASH_REPART_CHOOSE_TITLE = StaticBuildConfig.CRASH_REPORT_CHOOSE_TITLE;

    private final static boolean Debug = false;// 是否保存错误日志

    /** 定义异常类型 */
    public final static byte TYPE_NETWORK = 0x01;
    public final static byte TYPE_SOCKET = 0x02;
    public final static byte TYPE_HTTP_CODE = 0x03;
    public final static byte TYPE_HTTP_ERROR = 0x04;
    public final static byte TYPE_JSON = 0x05;
    public final static byte TYPE_IO = 0x06;
    public final static byte TYPE_RUN = 0x07;
    public final static byte TYPE_AUTH = 0X08;

    private byte type; // 异常类型
    private int code; // HTTP 服务器返回结果异常，此值代表服务器返回的异常值

    /** 系统默认的UncaughtException处理类 */
    private UncaughtExceptionHandler mDefaultHandler;
    private Context mContext;

    private AppException() {
        this.mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
    }

    private AppException(Context context) {
        this.mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        mContext = context;
    }

    private AppException(byte type, int code, Exception excp) {
        super(excp);
        this.type = type;
        this.code = code;
        if (Debug) {
            this.saveErrorLog(excp);
        }
    }


    public int getCode() {
        return this.code;
    }

    public int getType() {
        return this.type;
    }

    /**
     * 提示友好的错误信息
     */
    public void makeToast(Context ctx) {
        String err = convertExceptionToText(ctx);
        UIHelper.showAppToast(ctx, err);
    }

    public String convertExceptionToText(Context ctx) {
        String text;
        switch (this.getType()) {
            case TYPE_HTTP_CODE:
                text = ctx.getString(R.string.com_diagramsf_http_status_code_error,
                        this.getCode());
                break;
            case TYPE_HTTP_ERROR:
                text = ctx.getResources().getString(R.string.com_diagramsf_http_exception_error);
                break;
            case TYPE_SOCKET:
                text = ctx.getResources().getString(R.string.com_diagramsf_socket_exception_error);
                break;
            case TYPE_NETWORK:
                text = ctx.getResources().getString(R.string.com_diagramsf_network_not_connected);
                break;
            case TYPE_JSON:
                text = ctx.getResources().getString(R.string.com_diagramsf_json_parser_failed);
                break;
            case TYPE_IO:
                text = ctx.getResources().getString(R.string.com_diagramsf_io_exception_error);
                break;
            case TYPE_RUN:
                text = ctx.getResources().getString(R.string.com_diagramsf_app_run_code_error);
                break;
            case TYPE_AUTH:
                text = ctx.getResources().getString(R.string.com_diagramsf_auth_error);
                break;
            default:
                text = "";
                break;

        }

        return text;
    }

    /**
     * 保存异常日志
     *
     * @param excp 异常信息
     */
    public void saveErrorLog(Exception excp) {
        String errorlog = "errorlog.txt";
        String savePath;
        String logFilePath = "";
        FileWriter fw = null;
        PrintWriter pw = null;
        try {
            // 判断是否挂载了SD卡
            String storageState = Environment.getExternalStorageState();
            if (storageState.equals(Environment.MEDIA_MOUNTED)) {
                savePath = Environment.getExternalStorageDirectory()
                        .getAbsolutePath() + LOG_PATH;
                File file = new File(savePath);
                if (!file.exists()) {
                    file.mkdirs();
                }
                logFilePath = savePath + errorlog;
            }
            // 没有挂载SD卡，无法写文件
            if (logFilePath == "") {
                return;
            }
            File logFile = new File(logFilePath);
            if (!logFile.exists()) {
                logFile.createNewFile();
            }
            fw = new FileWriter(logFile, true);
            pw = new PrintWriter(fw);
            pw.println("--------------------" + (new Date().toLocaleString())
                    + "---------------------");
            excp.printStackTrace(pw);
            pw.close();
            fw.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (pw != null) {
                pw.close();
            }
            if (fw != null) {
                try {
                    fw.close();
                } catch (IOException e) {
                }
            }
        }

    }

    /**
     * 连接服务器成功，但是服务器返回结果异常，
     *
     * @param code 服务器异常码
     */
    public static AppException http(int code) {
        return new AppException(TYPE_HTTP_CODE, code, null);
    }

    /** 服务器应答错误 */
    public static AppException server(Exception e) {
        return new AppException(TYPE_HTTP_CODE, 0, e);
    }

    /** 表示网络连接超时 */
    public static AppException http(Exception e) {
        return new AppException(TYPE_HTTP_ERROR, 0, e);
    }

    /** 读取数据超时 */
    public static AppException socket(Exception e) {
        return new AppException(TYPE_SOCKET, 0, e);
    }

    /** 文件流异常 */
    public static AppException io(Exception e) {
        if (e instanceof UnknownHostException || e instanceof ConnectException) {
            return new AppException(TYPE_NETWORK, 0, e);
        } else if (e instanceof IOException) {
            return new AppException(TYPE_IO, 0, e);
        }
        return run(e);
    }

    /** 解析异常 */
    public static AppException json(Exception e) {
        return new AppException(TYPE_JSON, 0, e);
    }

    /** 无网络连接 */
    public static AppException noNet(Exception e) {
        return new AppException(TYPE_NETWORK, 0, e);
    }

    /** 当无法解析主机，或者没有联网时，生成 “检测是否联网”异常 */
    public static AppException network(Exception e) {
        if (e instanceof UnknownHostException || e instanceof ConnectException) {
            return new AppException(TYPE_NETWORK, 0, e);
        } else if (e instanceof HttpException) {
            return http(e);
        } else if (e instanceof SocketException) {
            return socket(e);
        }
        return http(e);
    }

    /** 应用程序运行时异常 */
    public static AppException run(Exception e) {
        return new AppException(TYPE_RUN, 0, e);
    }

    /** 身份验证错误 */
    public static AppException auth(Exception e) {
        return new AppException(TYPE_AUTH, 0, e);
    }

    /**
     * 获取APP异常崩溃处理对象
     *
     * @return
     */
    public static AppException getAppExceptionHandler() {
        return new AppException();
    }

    public static AppException getAppExceptionHandler(Context context) { return new AppException(context);}

    @Override
    public void uncaughtException(Thread thread, Throwable ex) {

        if (!handleException(ex) && mDefaultHandler != null) {
            mDefaultHandler.uncaughtException(thread, ex);
        }

    }

    /**
     * 自定义异常处理:收集错误信息&发送错误报告
     *
     * @param ex
     *
     * @return true:处理了该异常信息;否则返回false
     */
    private boolean handleException(Throwable ex) {
        if (ex == null) {
            return false;
        }

        //        final Context context = MyApplication.getInstance();

        if (mContext == null) {
            return false;
        }

        final String crashReport = getCrashReport(mContext, ex);
        // 显示异常信息&发送报告
        new Thread() {
            public void run() {
                Looper.prepare();
                sendAppCrashReport(mContext, crashReport);
                Looper.loop();
            }

        }.start();
        return true;
    }

    /**
     * 获取APP崩溃异常报告
     *
     * @param ex
     *
     * @return
     */
    private String getCrashReport(Context context, Throwable ex) {

        PackageInfo info = null;
        try {
            info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace(System.err);
        }
        if (info == null)
            info = new PackageInfo();

        StringBuffer exceptionStr = new StringBuffer();
        exceptionStr.append("Version: " + info.versionName + "("
                + info.versionCode + ")\n");
        exceptionStr.append("Android: " + android.os.Build.VERSION.RELEASE
                + "(" + android.os.Build.MODEL + ")\n");
        exceptionStr.append("Exception: " + ex.getMessage() + "\n");
        StackTraceElement[] elements = ex.getStackTrace();
        for (int i = 0; i < elements.length; i++) {
            exceptionStr.append(elements[i].toString() + "\n");
        }
        return exceptionStr.toString();
    }

    /**
     * 发送App异常崩溃报告
     *
     * @param cont
     * @param crashReport
     */
    private void sendAppCrashReport(final Context cont,
                                    final String crashReport) {
        AlertDialog.Builder builder = new AlertDialog.Builder(cont);
        builder.setIcon(android.R.drawable.ic_dialog_info);
        builder.setTitle(R.string.com_diagramsf_app_error);
        builder.setMessage(R.string.com_diagramsf_app_error_message);
        builder.setPositiveButton(R.string.com_diagramsf_submit_report,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        // 发送异常报告
                        Intent i = new Intent(Intent.ACTION_SEND);
                        // i.setType("text/plain"); //模拟器
                        i.setType("message/rfc822"); // 真机
                        i.putExtra(Intent.EXTRA_EMAIL, new String[]{CRASH_REPART_EMAIL});// 输入邮箱地址，格式是：用户名@xxx.com
                        i.putExtra(Intent.EXTRA_SUBJECT,
                                CRASH_REPART_EXTRA_SUBJECT);
                        i.putExtra(Intent.EXTRA_TEXT, crashReport);
                        cont.startActivity(Intent.createChooser(i, CRASH_REPART_CHOOSE_TITLE));
                        // 退出
                        AppManager.getAppManager().AppExit(cont);
                    }
                });
        builder.setNegativeButton(R.string.com_diagramsf_sure,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        // 退出
                        AppManager.getAppManager().AppExit(cont);
                    }
                });
        builder.show();
    }


}
