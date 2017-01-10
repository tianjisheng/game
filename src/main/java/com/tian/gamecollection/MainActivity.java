package com.tian.gamecollection;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;

import com.tian.gamecollection.activity.DiceActivity;
import com.tian.gamecollection.activity.GuessActivity;

public class MainActivity extends AppCompatActivity
{
     
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        
        return super.onTouchEvent(event);
    }

    public void onStartActivity(View view)
    {
        Intent intent = new Intent(this,DiceActivity.class);
        startActivity(intent);
    }
    
    public void onStartLogin(View view)
    {
        Intent intent = new Intent(this,GuessActivity.class);
        startActivity(intent);
    }

    public void onStartScrolling(View view)
    {
        Intent intent = new Intent(this,Main3Activity.class);
        startActivity(intent);
    }
    
    public void onStartSetting(View view)
    {
        Intent intent = new Intent(this,Main2Activity.class);
        startActivity(intent);
    }
    
    public void onStartDetail(View view)
    {
        Intent intent = new Intent(this,ItemListActivity.class);
        startActivity(intent);
    }
}
