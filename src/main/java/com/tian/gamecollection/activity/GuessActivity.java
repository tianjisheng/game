package com.tian.gamecollection.activity;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener2;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.tian.gamecollection.R;
import com.tian.gamecollection.utils.TianCountDownTimer;
import com.tian.gamecollection.utils.UIUtil;

import java.io.IOException;
import java.util.ArrayList; 
public class GuessActivity extends AppCompatActivity
{
    private Point point = new Point();
    private RelativeLayout content = null;
    private TextView numView = null;
    private TextView timeView = null;
    private TextView wordView = null;
    private SensorManager sensorManager = null;
    //加速度传感器数据  
    float accValues[]=new float[3];
    //地磁传感器数据  
    float magValues[]=new float[3];
    //旋转矩阵，用来保存磁场和加速度的数据  
    float r[]=new float[9];
    //模拟方向传感器的数据（原始数据为弧度）  
    float values[]=new float[3];
    private MediaPlayer player = null;
    private int mode = 1;//mode ==1 转动手机；实现切换 ==2，音量加减切换
    private final static int FORWARD = 0;//correct 
    private final static int MIDDLE = 1;//中间
    private final static int BEHIND = 2;//pass
    private int lt = -1;//上一个状态
    private int ct = MIDDLE;//当前状态
    private int totalTime = 60*3;//时间，单位是秒
    private ArrayList<String> words = new ArrayList<String>();
    private int gameState = 0;//0==未开始，1==开始  ，2==暂停，，3==结束
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_guess2);
        
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        UIUtil.DPI = dm.densityDpi;
        UIUtil.SDP = dm.scaledDensity;
        point.x = dm.widthPixels;
        point.y = dm.heightPixels;
        
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        
        content = (RelativeLayout)findViewById(R.id.container);

        numView = new TextView(this);
        RelativeLayout.LayoutParams numLp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
        numView.setText("5/15");
        numView.setTextSize(UIUtil.px2sp(80));
        numLp.addRule(RelativeLayout.CENTER_VERTICAL);
        numLp.leftMargin = point.x/30;
        content.addView(numView,numLp);
        
        timeView = new TextView(this);
        timeView.setId(Integer.valueOf(90));
        timeView.setBackgroundColor(Color.GREEN);
        timeView.setTextSize(UIUtil.px2sp(100));
        timeView.setText("00:00:00");
        RelativeLayout.LayoutParams timeLp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
        timeLp.addRule(RelativeLayout.CENTER_HORIZONTAL);
        Log.i("GuessActivity","point.y=="+point.y);
        timeLp.topMargin = point.y/7;
        content.addView(timeView,timeLp);
        
        wordView = new TextView(this);
        wordView.setId(Integer.valueOf(101));
        wordView.setBackgroundColor(Color.LTGRAY);
        wordView.setTextSize(UIUtil.px2sp(150));
        wordView.setText("前程万里");
        wordView.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                if (event.getAction() == MotionEvent.ACTION_DOWN)
                {
                    showDialog(0,0,0,0);
                }
                return true;
            }
        });
        RelativeLayout.LayoutParams wordLp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
        wordLp.addRule(RelativeLayout.CENTER_HORIZONTAL);
        wordLp.addRule(RelativeLayout.BELOW,timeView.getId());
        wordLp.topMargin = point.y/10;
        content.addView(wordView,wordLp);
        
        sensorManager  = (SensorManager)this.getSystemService(Context.SENSOR_SERVICE);
        player = new MediaPlayer(); 
    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        Log.i("Guess","onTouchEvent");
        if (gameState == 0)
        {
            gameState = 1;
            startGame();
            startListenSensor();
        }
        return super.onTouchEvent(event);
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        if (gameState == 0)
        {
            Snackbar.make(wordView,"点击空白处开始",Snackbar.LENGTH_LONG).show(); 
        }else if (gameState == 2)
        {//如果处于暂停状态，恢复开始
            startGame();
            startListenSensor();
        }
    }
    private TianCountDownTimer timer = null;
    private void pauseGame()
    {
        Log.i("guess","pauseGame()");
        if (gameState ==1)
        {
            gameState = 2;
        }
        if (timer !=null)
        {
            timer.pause();
        }
        if (player!=null && player.isPlaying())
        {
            player.pause(); 
        }
    }
    private void startGame()
    {
        if (timer == null)
        {
            timer = new TianCountDownTimer(totalTime*1000,1000)
            {
                @Override
                public void onTick(long millisUntilFinished)
                {
                    Message msg = uiHandler.obtainMessage();
                    msg.what = 5;
                    msg.obj = millisUntilFinished;
                    uiHandler.sendMessage(msg);
                }

                @Override
                public void onFinish()
                {
                    sensorManager.unregisterListener(sensorEventListener2);
                }
            };
        }
        timer.start(); 
    }
    
    private void startListenSensor()
    {
        Sensor sensor01 = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        Sensor sensor02 = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        sensorManager.registerListener(sensorEventListener2,sensor01,SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(sensorEventListener2,sensor02,SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        sensorManager.unregisterListener(sensorEventListener2);
        pauseGame();
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        sensorManager.unregisterListener(sensorEventListener2);
        if (player.isPlaying())
        {
            player.stop();
        }
        player.release();
        player = null;
    }

    private SensorEventListener2 sensorEventListener2 = new SensorEventListener2()
    {
        @Override
        public void onFlushCompleted(Sensor sensor)
        {
            
        }

        @Override
        public void onSensorChanged(SensorEvent event)
        {
            if(event.sensor.getType()==Sensor.TYPE_ACCELEROMETER)
            {
                accValues=event.values.clone();//这里是对象，需要克隆一份，否则共用一份数据  
            }
            else if(event.sensor.getType()==Sensor.TYPE_MAGNETIC_FIELD)
            {
                magValues=event.values.clone();//这里是对象，需要克隆一份，否则共用一份数据  
            }
            /**public static boolean getRotationMatrix (float[] R, float[] I, float[] gravity, float[] geomagnetic) 
             * 填充旋转数组r 
             * r：要填充的旋转数组 
             * I:将磁场数据转换进实际的重力坐标中 一般默认情况下可以设置为null 
             * gravity:加速度传感器数据 
             * geomagnetic：地磁传感器数据 
             */
            SensorManager.getRotationMatrix(r, null, accValues, magValues);
            /**
             * public static float[] getOrientation (float[] R, float[] values) 
             * R：旋转数组 
             * values ：模拟方向传感器的数据 
             */
            SensorManager.getOrientation(r, values);
             //将弧度转化为角度后输出  
            float value=(float) Math.toDegrees(values[2]);
            if (value<-110)
            {
                ct = FORWARD;
            }else if (value>-65)
            {
                ct = BEHIND;
            }else
            {
                ct = MIDDLE;
            }
            
            if (lt == MIDDLE)
            {
                if (ct == FORWARD)
                {
                    turn(1);
                }else if (ct == BEHIND)
                {
                    turn(2);
                }
            }
            lt = ct;
            Log.i("GuessActivity","onSensorChanged,value=="+value+",,lt  = "+lt+",ct = "+ct);
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy)
        {

        }
    };
    
    private long lastTime = 0;//防止过快误操作
    //direction =2,pass ;direction =1,correct
    private void turn(int direction)
    {
        if ((System.currentTimeMillis()-lastTime)<500)
        {
            return;
        }
        uiHandler.sendEmptyMessage(direction);
    }
    
    // 更新UI
    private Handler uiHandler = new Handler(Looper.getMainLooper())
    {
        @Override
        public void handleMessage(Message msg)
        {
             switch (msg.what)
             {
                 case 1://correct
                     playAudio(R.raw.correct,false);
                     break;
                 case 2://pass
                     playAudio(R.raw.pass,false);
                     break;
//                 case 3://countdown
//                     playAudio(R.raw.countdown,true);
//                     break;
                 case 4:// last 
                     playAudio(R.raw.lastseconds,true);
                     break;
                 case 5:// update time
                     String str = "";
                     long time = (long) msg.obj;
                     if ((int)(time/1000) ==10)
                     {
                         uiHandler.sendEmptyMessageDelayed(4,5*1000);
                     }
                     int hour = (int) (time/(1000*60*60));
                     if (hour<10)
                     {
                         str += "0"+hour;
                     }else
                     {
                         str += hour;
                     }
                     str+=":";
                     int minute = (int) (time/(1000*60))%61;
                     if (minute<10)
                     {
                         str +="0"+minute;
                     }else
                     {
                         str += minute;
                     } 
                     str+=":";
                     int second = (int) (time%(1000*60))/1000;
                     if (second<10)
                     {
                         str += "0"+second;
                     }else
                     {
                         str += second;
                     }
                     timeView.setText(str);
                     break;
                 case 6://update word
                     break;
             }
        }
    };
    
    private void playAudio(int id,boolean isLoop)
    {
        Uri uri = Uri.parse("android.resource://"+GuessActivity.this.getPackageName()+"/"+id);
        try
        {
            if (player.isPlaying())
            {
                player.stop();
            }
            player.reset();
            player.setLooping(isLoop);
            player.setDataSource(GuessActivity.this,uri);
            player.prepare();
            player.start();
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu_guess, menu);
        return true;
    }
 

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        Log.i("GuessActivity","onOptionsItemSelected:"+item.getItemId());
        return super.onOptionsItemSelected(item);
    }
    
    private void showDialog(int totalTime,int curTime,int totalWord,int rightWord)
    {
        Dialog dialog = new Dialog(this);
        dialog.setTitle("tttttttttttttttttttt");
        dialog.show();
    }
}
