package com.example.aman.blowcandle;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    private MediaRecorder mediaRecorder = null;
    private Boolean recording = false;
    private Button startBtn;
    private TextView dbTextView;
    private ImageView candleImageView;
    private File soundFile = null;  //录音文件

    private double DB = 0;// 分贝
    private DevicePolicyManager devicePolicyManager;
    private boolean isAdminActive = false;
    private boolean flag = false;

    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        GetPermissions.isGrantExternalRW(this,1);

        startBtn = findViewById(R.id.startBtn);
        dbTextView = findViewById(R.id.dbTextView);
        candleImageView = findViewById(R.id.candleImageView);

        devicePolicyManager = (DevicePolicyManager)getSystemService(Context.DEVICE_POLICY_SERVICE);
        // 申请权限
        ComponentName componentName = new ComponentName(this, MyAdmin.class);
        // 判断该组件是否有系统管理员的权限
        isAdminActive = devicePolicyManager.isAdminActive(componentName);
        if(!isAdminActive){
            //这一句一定要有...
            Intent intent = new Intent();
            //指定动作
            intent.setAction(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
            //指定给那个组件授权
            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, componentName);
            startActivity(intent);
            isAdminActive = true;
        }
    }

    public void startBtnOnClick(View v){
        if(!recording){
            startRecord();
            runnable.run();
        }else{
            stopRecord();
            handler.removeCallbacks(runnable);
        }
    }

    private void startRecord(){
        if(mediaRecorder == null){
            // 存放录音文件的文件夹sounds
            File soundsFolder = new File(Environment.getExternalStorageDirectory(), "temp_sounds");
            if(!soundsFolder.exists()){
                soundsFolder.mkdirs();
            }
            // 获取当前时间作为文件名创建一个以.amr为后缀的录音文件
            soundFile = new File(soundsFolder,"temp_record.amr");
            if(!soundFile.exists()){
                try {
                    soundFile.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            mediaRecorder = new MediaRecorder();
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);  //音频输入源
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_WB);   //设置输出格式
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_WB);   //设置编码格式
            mediaRecorder.setOutputFile(soundFile.getAbsolutePath());
            try {
                mediaRecorder.prepare();
                mediaRecorder.start();  //开始录制
                recording = true;
                startBtn.setText("STOP");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void stopRecord(){
        if(mediaRecorder != null){
            mediaRecorder.stop();
            mediaRecorder.release();
            mediaRecorder = null;
            recording = false;
            startBtn.setText("START");
        }
    }

    Runnable runnable=new Runnable(){
        @Override
        public void run() {
            if(mediaRecorder != null){
                double ratio = (double) mediaRecorder.getMaxAmplitude() / 100;
                if (ratio > 1)
                    DB = 20 * Math.log10(ratio);
                dbTextView.setText(String.valueOf(DB));
            }
            if(flag){
                if(isAdminActive) {
                    try {
                        Thread.sleep(1000);
                        flag = false;
                        devicePolicyManager.lockNow();
                        Log.i("即将锁屏。", "321");
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            //每100毫秒刷新一次
            handler.postDelayed(this, 100);
            if(DB < 18){
                flag = false;
                candleImageView.setImageResource(R.drawable.init);
            }else if(DB < 45){
                flag = false;
                candleImageView.setImageResource(R.drawable.windiness);
            }else{
                DB = 0; //DB要清零，否则这个未被停掉的线程会一直得到这个条件而循环运行这里
                candleImageView.setImageResource(R.drawable.distinguish);
                flag = true;
                stopRecord();
            }
        }
    };
}
