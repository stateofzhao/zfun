package com.zfun.example.io;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.zfun.learn.io.IOUtils;
import com.zfun.learn.io.LearnWriteByte;
import com.zfun.lib.executor.TaskHandler;
import com.zfun.lib.permission.Permission;
import com.zfun.lib.permission.SimpleCallback;
import com.zfun.lib.util.FileUtil;
import com.zfun.example.R;

import java.io.*;

/**
 * {@link com.zfun.learn.io.LearnWriteByte}
 */
public class IOActivity extends AppCompatActivity implements View.OnClickListener {
    public static final String DIR = "zfun_IO_Temp";
    private String rootFilePath;

    EditText writeET;
    Button writeBtn1;
    Button writeBtn2;

    Button readBtn1;
    TextView readTV1;
    Button readBtn2;
    TextView readTV2;
    Button readBtnAndroid;
    TextView readTVAndroid;

    public static void open(Context context) {
        Intent intent = new Intent();
        intent.setClass(context, IOActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_io);
        writeET = findViewById(R.id.ed_write_int_byte_1);
        writeBtn1 = findViewById(R.id.btn_write_int_byte_1);
        writeBtn2 = findViewById(R.id.btn_write_int_byte_2);

        readBtn1 = findViewById(R.id.btn_read_int_byte_1);
        readTV1 = findViewById(R.id.tv_read_int_byte_1);
        readBtn2 = findViewById(R.id.btn_read_int_byte_2);
        readTV2 = findViewById(R.id.tv_read_int_byte_2);
        readBtnAndroid = findViewById(R.id.btn_read_int_byte_android);
        readTVAndroid = findViewById(R.id.tv_read_int_byte_android);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Permission.requestPermissions(this, 1, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, new SimpleCallback() {
            @Override
            public void onSuccess(int requestCode) {
                rootFilePath = FileUtil.getExternalStorageRootPath();
                writeBtn1.setOnClickListener(IOActivity.this);
                writeBtn2.setOnClickListener(IOActivity.this);
                readBtn1.setOnClickListener(IOActivity.this);
                readBtn2.setOnClickListener(IOActivity.this);
                readBtnAndroid.setOnClickListener(IOActivity.this);
            }

            @Override
            public void onFail(int requestCode, String[] permissions, int[] grantResults) {
                Toast.makeText(IOActivity.this, "无法获取存储权限", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Permission.onActivityResult(this, requestCode, resultCode, data);
    }

    @Override
    public void onClick(View v) {
        if (TaskHandler.instance().isContainer("1")) {
            Toast.makeText(this, "已经存在任务，请稍等", Toast.LENGTH_SHORT).show();
            return;
        }
        if (writeBtn1 == v) {
            TaskHandler.instance().execute(new WriteTask(LearnWriteByte.FLAG_BIG_ENDIAN), "1");
        } else if (writeBtn2 == v) {
            TaskHandler.instance().execute(new WriteTask(LearnWriteByte.FLAG_LITTLE_ENDIAN), "1");
        } else if (readBtn1 == v) {
            TaskHandler.instance().execute(new ReadTask(LearnWriteByte.FLAG_BIG_ENDIAN),"1");
        } else if (readBtn2 == v) {
            TaskHandler.instance().execute(new ReadTask(LearnWriteByte.FLAG_LITTLE_ENDIAN),"1");
        } else if (readBtnAndroid == v) {
            TaskHandler.instance().execute(new ReadTask(-1),"1");
        }
    }

    private int getInputInt() throws Exception {
        String input = writeET.getText().toString();
        return Integer.parseInt(input);
    }

    private class WriteTask implements Runnable {
        private final int writeFlag;

        private WriteTask(int writeFlag) {
            this.writeFlag = writeFlag;
        }

        @Override
        public void run() {
            FileOutputStream fos = null;
            try {
                File dir = new File(rootFilePath + "/" + DIR);
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                File textFile = new File(dir, "testWriteByte.txt");
                if (textFile.exists()) {
                    textFile.delete();
                }
                textFile.createNewFile();
                int n = getInputInt();
                fos = new FileOutputStream(textFile);
                LearnWriteByte.writeInt(fos, n, writeFlag);
                toast("写入成功");
            } catch (Exception e) {
                toast("写入失败");
            } finally {
                IOUtils.close(fos);
            }
        }
    }//

    private class ReadTask implements Runnable {
        private final int readFlag;

        private ReadTask(int readFlag) {
            this.readFlag = readFlag;
        }

        @Override
        public void run() {
            FileInputStream fis = null;
            DataInputStream dataInputStream = null;
            try {
                final File dir = new File(rootFilePath + "/" + DIR);
                final File textFile = new File(dir, "testWriteByte.txt");
                if (!textFile.exists()) {
                    toast("文件不存在");
                    return;
                }
                fis = new FileInputStream(textFile);
                if(readFlag == -1){
                    dataInputStream = new DataInputStream(fis);
                    final int n = dataInputStream.readInt();
                    toast("读取成功");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            readTVAndroid.setText(String.valueOf(n));
                        }
                    });
                    return;
                }
                final int n = LearnWriteByte.readInt(fis, readFlag);
                toast("读取成功");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (readFlag == LearnWriteByte.FLAG_BIG_ENDIAN) {
                            readTV1.setText(String.valueOf(n));
                        } else {
                            readTV2.setText(String.valueOf(n));
                        }
                    }
                });
            } catch (Exception e) {
                toast("读取失败");
            } finally {
                IOUtils.close(fis);
                IOUtils.close(dataInputStream);
            }
        }
    }//

    private void toast(final String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(IOActivity.this, msg, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
