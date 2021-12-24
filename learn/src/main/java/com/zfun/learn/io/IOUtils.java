package com.zfun.learn.io;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by zfun on 2021/12/24 11:31 AM
 */
public class IOUtils {
    public static void close(OutputStream os){
        try {
            os.flush();
            os.close();
        }catch (Exception ignore){
        }
    }

    public static void close(InputStream is){
        try {
            is.close();
        }catch (Exception ignore){
        }
    }

    public static void close(BufferedReader is){
        try {
            is.close();
        }catch (Exception ignore){
        }
    }
}
