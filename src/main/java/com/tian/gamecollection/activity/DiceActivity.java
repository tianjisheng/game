package com.tian.gamecollection.activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.AnimationDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener2;
import android.hardware.SensorManager;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log; 
import android.view.View; 
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.tian.gamecollection.R;
import com.tian.gamecollection.utils.UIUtil;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
/**
 *  骰子界面
 */
public class DiceActivity extends AppCompatActivity
{
    private final static String TAG = "DiceActivity";
    private final static int SPEED = 7;
    private RelativeLayout content = null;
    private int defaultNumber = 1;
    private int currentNumber = defaultNumber;
    private ImageView[] dices = null;
    private ArrayList<AnimationDrawable> animationDrawables = new ArrayList<AnimationDrawable>();
    private Point point = new Point();
    private SensorManager sensorManager = null;
    private boolean isActionIng = false;
    private ExecutorService pool = null;
    private int[] ids = {R.drawable.dice_1,R.drawable.dice_2,R.drawable.dice_3,R.drawable.dice_4,R.drawable.dice_5,R.drawable.dice_6};
    private int mode = 1;//1==shake,2==click
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dice);
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        UIUtil.DPI = dm.densityDpi;
        point.x = dm.widthPixels;
        point.y = dm.heightPixels;
        content = (RelativeLayout)findViewById(R.id.dice_content_layout); 
        findViewById(R.id.dice_plus_btn).setOnClickListener(floatBtnOnClick);
        findViewById(R.id.dice_minus_btn).setOnClickListener(floatBtnOnClick);
        addDice(defaultNumber);

        sensorManager = (SensorManager) this.getSystemService(Context.SENSOR_SERVICE);
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        if (mode ==1)
        {
            Sensor sensor = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
            sensorManager.registerListener(sensorEventListener2,sensor,SensorManager.SENSOR_DELAY_NORMAL);  
        }
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        if (mode == 1)
        {
            sensorManager.unregisterListener(sensorEventListener2); 
        }
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        sensorManager = null;
        pool.shutdownNow();
        pool = null;
    }

    /**
     * @param num = 界面上色子总数
     */
    private void addDice(int num)
    {
        if (dices!=null)
        {
            for (ImageView view:dices)
            {
                content.removeView(view);
            }
        }
        if (animationDrawables !=null)
        {
           for (AnimationDrawable drawable :animationDrawables)
           {
               drawable.stop();
           }
        }
        isActionIng = false;
        dices = null;
        dices = new ImageView[num];
        switch (num)
        {
            case 1:
            case 2:
            case 3:
                for(int i=0;i<num;i++)
                {
                    RelativeLayout.LayoutParams imgLp = new RelativeLayout.LayoutParams(UIUtil.px2dp(110-num*10),UIUtil.px2dp(110-num*10));
                    imgLp.addRule(RelativeLayout.CENTER_VERTICAL);
                    ImageView img = new ImageView(this);
                    img.setBackgroundResource(R.drawable.dice_1);
                    imgLp.leftMargin = (point.x -UIUtil.px2dp(110-num*10)*num)/(num+1)+i*(UIUtil.px2dp(110-num*10)+UIUtil.px2dp(10));
                    dices[i] = img;
                    content.addView(img,imgLp);
                }
               break;
            case 4:
            case 5:
            case 6:
                int num1 = num-3;
                for(int i=0;i<num1;i++)
                {
                    RelativeLayout.LayoutParams imgLp = new RelativeLayout.LayoutParams(UIUtil.px2dp(70),UIUtil.px2dp(70));
                    imgLp.topMargin = (point.y -UIUtil.px2dp(70)*2)/3;
                    imgLp.leftMargin = (point.x -UIUtil.px2dp(70)*3)/4+i*(UIUtil.px2dp(70)+UIUtil.px2dp(10));
                    ImageView img = new ImageView(this);
                    img.setBackgroundResource(R.drawable.dice_1);
                    dices[i] = img;
                    content.addView(img,imgLp);
                }
                
                for(int i=0;i<3;i++)
                {
                    RelativeLayout.LayoutParams imgLp = new RelativeLayout.LayoutParams(UIUtil.px2dp(70),UIUtil.px2dp(70));
                    imgLp.topMargin = (point.y -UIUtil.px2dp(70)*2)/3+UIUtil.px2dp(70)+UIUtil.px2dp(10);
                    imgLp.leftMargin = (point.x -UIUtil.px2dp(70)*3)/4+i*(UIUtil.px2dp(70)+UIUtil.px2dp(10));
                    ImageView img = new ImageView(this);
                    img.setBackgroundResource(R.drawable.dice_1);
                    dices[i+num1] = img;
                    content.addView(img,imgLp);
                }
                break;
        }
         
    }

    private View.OnClickListener floatBtnOnClick = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            diceNumberChange(v.getId());
        }
    };
    
    private SensorEventListener2 sensorEventListener2 = new SensorEventListener2()
    {
        @Override
        public void onFlushCompleted(Sensor sensor)
        {
            Log.i(TAG,"onFlushCompleted "+sensor.getName());
        }

        @Override
        public void onSensorChanged(SensorEvent event)
        {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];
            Log.i(TAG,"x = "+x + ",y = "+y+",z = "+z);
            if (x>SPEED||x<-SPEED||y>SPEED||y<-SPEED||z>SPEED||z<-SPEED)
            {
                shake();
            }
            
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy)
        {
            Log.i(TAG,"onAccuracyChanged "+sensor.getName()+",accuracy="+accuracy);
        }
    };
    
    private void shake()
    {
        Log.i(TAG,"shake()");
        if (!isActionIng)
        {
            isActionIng = true;
            for (int i = 0;i<dices.length;i++)
            {
                if (i>=animationDrawables.size())
                {
                    AnimationDrawable animationDrawable = (AnimationDrawable) getResources().getDrawable(R.drawable.dice_anim); 
                    animationDrawables.add(i,animationDrawable);
                }
                dices[i].setBackground(animationDrawables.get(i));
                animationDrawables.get(i).start();
            }
            
            if (pool == null)
            {
                pool = Executors.newSingleThreadExecutor();
            }
            pool.submit(new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        TimeUnit.MILLISECONDS.sleep(800);
                        Random random = new Random();
                        for (int i = 0;i<dices.length;i++)
                        {
                            animationDrawables.get(i).stop();
                            update(i,random.nextInt(6));
                            TimeUnit.MILLISECONDS.sleep(200);//想得到依次停止的效果
                        }
                        isActionIng = false;
                    } catch (InterruptedException e)
                    {
                        e.printStackTrace();
                    }
                }
            });
        }
    }
    
    private void update(final int index1,  final int index2)
    {
        DiceActivity.this.runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                dices[index1].setBackgroundResource(ids[index2]);
            }
        });
    }
    
    private void diceNumberChange(int id)
    {
        if (id == R.id.dice_minus_btn)
        {
            if (currentNumber<=1)
            {
                Snackbar.make(findViewById(R.id.dice_minus_btn),R.string.activity_dice_too_less,Snackbar.LENGTH_SHORT).setActionTextColor(Color.RED).show();
            }else
            {
                currentNumber--;
                addDice(currentNumber);
            }
        }else if (id == R.id.dice_plus_btn)
        {
            if (currentNumber>=6)
            {
                Snackbar.make(findViewById(R.id.dice_plus_btn),R.string.activity_dice_too_many,Snackbar.LENGTH_SHORT).setActionTextColor(Color.RED).show();
            }else
            {
                currentNumber++;
                addDice(currentNumber);
            }
        }
    }
    
    public void onChangeMode(View view)
    {
        if (mode == 1)
        {
            mode = 2;
            sensorManager.unregisterListener(sensorEventListener2);
            Snackbar.make(view,"触发新功能，开启点击模式，轻松、省电，适合长时间玩！",Snackbar.LENGTH_LONG).show();
            ((TextView)view).setText("触摸开始");
        }else
        {//第一次不开始摇动，没有什么特殊原因，这样的交互感觉舒服一点
            shake(); 
        }
       
    }
}
