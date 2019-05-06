package com.example.mymusic;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.SeekBar;
import android.widget.VideoView;

import com.danikula.videocache.HttpProxyCacheServer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
      String  url = "http://www.ytmp3.cn/down/60946.mp3";
      String  url1 = "http://sc1.111ttt.cn/2018/1/03/13/396131229550.mp3";
      String  url3 = "http://sc1.111ttt.cn/2017/1/05/09/298092036393.mp3";
      List<String> list = new ArrayList<>();
      int  current = 0;
    private MediaPlayer mediaPlayer;
    private HttpProxyCacheServer proxy;
    private static SeekBar seekBar;

    public static Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Bundle data = msg.getData();
            int duration = data.getInt("duration");
            int currentPosition = data.getInt("currentPosition");

            seekBar.setMax(duration);
            seekBar.setProgress(currentPosition);
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        seekBar = findViewById(R.id.SeekBar);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mediaPlayer.seekTo(seekBar.getProgress());
            }
        });
        list.add(url);
        list.add(url1);
        list.add(url3);
        proxy = App.getProxy(this);
        String proxyUrl = proxy.getProxyUrl(list.get(current));
        mediaPlayer = new MediaPlayer();
        mediaPlayer.reset();
        try {
            mediaPlayer.setDataSource(proxyUrl);
            mediaPlayer.prepare();
            mediaPlayer.start();
            updateSeekBar();
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    next();
                }
            });
            seekBar.setMax(mediaPlayer.getDuration());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void next() {
        current++;
        if (current==list.size()){
            current=0;
        }
        String proxyUrl = proxy.getProxyUrl(list.get(current));
        try {
            mediaPlayer.stop();
            mediaPlayer.reset();
            mediaPlayer.setDataSource(proxyUrl);
            mediaPlayer.prepare();
            mediaPlayer.start();
            seekBar.setMax(mediaPlayer.getDuration());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void start(View view) {
        mediaPlayer.start();
    }

    public void stop(View view) {
        mediaPlayer.pause();
    }

    public void next(View view) {
        next();
    }
    /**
     * 更新SeekBar
     */
    private void updateSeekBar() {
        //获取总时长
        final int duration = mediaPlayer.getDuration();

        //开启线程发送数据
        new Thread() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    int currentPosition = mediaPlayer.getCurrentPosition();
                    //发送数据给activity
                    Message message = Message.obtain();
                    Bundle bundle = new Bundle();
                    bundle.putInt("duration", duration);
                    bundle.putInt("currentPosition", currentPosition);
                    message.setData(bundle);

                    MainActivity.handler.sendMessage(message);
                }
            }
        }.start();
    }
}
