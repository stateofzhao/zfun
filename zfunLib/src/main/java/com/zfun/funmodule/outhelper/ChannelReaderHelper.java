package com.zfun.funmodule.outhelper;

import com.zfun.funmodule.util.androidZipSinger.V1ChannelUtil;
import com.zfun.funmodule.util.androidZipSinger.read.ChannelInfo;
import com.zfun.funmodule.util.androidZipSinger.read.ChannelReader;

import java.io.File;

/**
 * 多渠道打完包后读取写入到的渠道信息。
 * <br/>
 * Created by zfun on 2021/12/20 2:18 PM
 */
public class ChannelReaderHelper {
    public static String CHANNEL_NULL = "unknown";

    public static String readChannel(String apkPath, boolean useV1) {
        if (useV1) {
            return V1ChannelUtil.getChannelId(apkPath, "", CHANNEL_NULL);
        } else {
            ChannelInfo channelInfo = ChannelReader.get(new File(apkPath));
            String channel = null == channelInfo ? "" : channelInfo.getChannel();
            if (null == channel || channel.length() == 0) {
                channel = CHANNEL_NULL;
            }
            return channel;
        }
    }

    public static String readChannel(String apkPath) {
        String channel = V1ChannelUtil.getChannelId(apkPath, "", CHANNEL_NULL);
        if (CHANNEL_NULL.equals(channel)) {
            ChannelInfo channelInfo = ChannelReader.get(new File(apkPath));
            channel = null == channelInfo ? "" : channelInfo.getChannel();
        }
        if (null == channel || channel.length() == 0) {
            channel = CHANNEL_NULL;
        }
        return channel;
    }


    //jar
    public static void main(String... args){

    }
}
