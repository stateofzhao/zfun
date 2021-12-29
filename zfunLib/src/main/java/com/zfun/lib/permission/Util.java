package com.zfun.lib.permission;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import com.zfun.lib.permission.core.IamUI;

import java.util.List;

/**
 * Created by zfun on 2018/3/21 18:13
 */
public class Util {

    //获取默认 UI 弹窗
    public static IamUI createDefaultIamUI(final Context context) {
        check(null != context);
        final IamUI iamUI = new IamUI() {
            @Override
            public void showRequestPermissionTip(String[] permissions,
                    final IamUI.OnClickCancel onClickCancel, final OnClickOk onClickOk) {
                StringBuilder messageBuild = new StringBuilder("我们需要获取下列权限来保证程序正常运行：");
                List<String> text = TransformText.transformText(permissions);
                for (int i = 0, size = text.size(); i < size; i++) {
                    messageBuild.append("【").append(text.get(i)).append("】");
                    if (size > 1) {
                        if (i == size - 2) {
                            messageBuild.append("和");
                        } else if (i != size - 1) {
                            messageBuild.append("，");
                        }
                    }
                }

                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setMessage(messageBuild.toString());
                builder.setCancelable(false);
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        onClickOk.onClick();
                    }
                });
                builder.show().setCanceledOnTouchOutside(false);
            }

            @Override
            public void showGoSettingsTip(String[] permissions, final OnClickCancel onClickCancel,
                    final OnClickOk onClickOk) {
                StringBuilder messageBuild = new StringBuilder("我们需要获取下列权限来保证程序正常运行：");
                List<String> text = TransformText.transformText(permissions);
                for (int i = 0, size = text.size(); i < size; i++) {
                    messageBuild.append("【").append(text.get(i)).append("】");
                    if (size > 1) {
                        if (i == size - 2) {
                            messageBuild.append("和");
                        } else if (i != size - 1) {
                            messageBuild.append("，");
                        }
                    }
                }
                messageBuild.append("\n请在【设置-应用-酷我音乐-权限】中开启相应权限");

                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setMessage(messageBuild.toString());
                builder.setCancelable(false);
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        onClickOk.onClick();
                    }
                });
                builder.setNeutralButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        onClickCancel.onClick();
                    }
                });
                builder.show().setCanceledOnTouchOutside(false);
            }
        };
        return iamUI;
    }

    /**
     * 根据 权限字符串 permissions 和 已经拒绝的权限字符串 deniedPermission 生成权限结果码。
     *
     * @param permissions 要申请的权限
     * @param deniedPermission 已经拒绝的权限
     */
    public static int[] createResultCode(String[] permissions, List<String> deniedPermission) {
        int[] result = new int[permissions.length];
        for (int i = 0, length = permissions.length; i < length; i++) {
            String one = permissions[i];
            boolean isDenied = false;
            for (int j = 0, jLength = deniedPermission.size(); j < jLength; j++) {
                if (one.equals(deniedPermission.get(j))) {
                    isDenied = true;
                    break;
                }
            }
            if (isDenied) {
                result[i] = PackageManager.PERMISSION_DENIED;
            } else {
                result[i] = PackageManager.PERMISSION_GRANTED;
            }
        }
        return result;
    }

    //FIXME LZF 迁移的时候，改一下这个即可
    public static void check(boolean value) {
        //KwDebug.classicAssert(value);
    }
}
