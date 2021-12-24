package com.zfun.learn.binary;

import java.math.BigInteger;

/**
 * 学习二进制。
 * <p/>
 * Created by zfun on 2021/12/23 3:14 PM
 */
public class BinaryUtils {
    /**
     * 将有符号的十进制整数转换成有符号的二进制（正常二进制，非计算机中存储的补码）
     *
     * @param i 有符号整数字符串
     */
    public static String intToBinaryStr(String i) {
        BigInteger src = new BigInteger(i, 10);
        return src.toString(2);
    }

    /** 将有符号的二进制（正常二进制，非计算机中存储的补码）字符串转换成有符号的十进制整数 */
    public static String binaryToIntStr(String binary) {
        //第一个参数是数源，第二个参数是数源进制
        BigInteger src = new BigInteger(binary, 2);
        return src.toString(10);
    }

    /**
     * 将有符号十进制整数转换成二进制字符串（这个转换的二进制不是数学上的二进制，是计算机中存储的二进制（补码））
     *
     * @param i 有符号整数字符串
     */
    public static String intToBinaryStrComplement(long i) {
        return Long.toBinaryString(i);//这个就是取得i在计算机中的二进制表示，所以返回的就是补码
    }

    /** 将二进制（计算机中存储的补码）字符串转换成有符号的十进制整数 */
    public static String binaryToIntStrComplement(String binary) {
        BigInteger src = new BigInteger(binary, 2);
        return src.intValue() + "";
    }
}
