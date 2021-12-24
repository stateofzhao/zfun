package com.zfun.learn.io;

import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * 学习字节写入
 * <p/>
 * Created by zfun on 2021/12/24 11:27 AM
 */
public class LearnWriteByte {
    public static final String TAG = "LearnWriteByte";
    public static final int FLAG_BIG_ENDIAN = 1;//大端
    public static final int FLAG_LITTLE_ENDIAN = 2;//小端，android使用小端序

    private static final int BYTE_MASK = 0xff;//1111 1111

    public static void writeInt(String filePath, int ver) {
        FileOutputStream writeOS = null;
        try {
            writeOS = new FileOutputStream(new File(filePath));
            writeInt(writeOS, ver, FLAG_BIG_ENDIAN);
        } catch (Exception e) {
            Log.e(TAG, e.getLocalizedMessage());
        } finally {
            IOUtils.close(writeOS);
        }
    }

    //int 占4个字节（byte），每个字节占8位，所以写一个int型数，需要写入4个字节
    public static void writeInt(OutputStream os, int data, int flag) throws Exception {
        if (FLAG_BIG_ENDIAN == flag) {//大端序，低位数据写入到内存中的高地地址
            //OutputStream#write(int)方法，每次只能写入一个byte数据（二进制8位），但是参数是int型的，
            // 它的实现是写入int数据中的低8位，int数据中的高24位数据被忽略。
            //其实这里没必要 【& BYTE_MASK】 运算下，应该是便于阅读才这么些。
            os.write((data >> 0) & BYTE_MASK);
            os.write((data >> 8) & BYTE_MASK);
            os.write((data >> 16) & BYTE_MASK);
            os.write((data >> 24) & BYTE_MASK);
        } else if (FLAG_LITTLE_ENDIAN == flag) {//小端序
            os.write((data >> 24) & BYTE_MASK);
            os.write((data >> 16) & BYTE_MASK);
            os.write((data >> 8) & BYTE_MASK);
            os.write((data >> 0) & BYTE_MASK);
        } else {
            throw new IllegalArgumentException("unknown flag");
        }
    }

    //这里容易搞错
    public static int readInt(InputStream inputStream, int flag) throws Exception {
        if (FLAG_BIG_ENDIAN == flag) {
            int result = inputStream.read()<<0;
            result |= inputStream.read()<<8;
            result |= inputStream.read()<<16;
            result |= inputStream.read()<<24;
            return result;
        } else if (FLAG_LITTLE_ENDIAN == flag) {
            int result = inputStream.read()<<24;
            result |= inputStream.read()<<16;
            result |= inputStream.read()<<8;
            result |= inputStream.read()<<0;
            return result;
        } else {
            throw new IllegalArgumentException("unknown flag");
        }
    }
}
