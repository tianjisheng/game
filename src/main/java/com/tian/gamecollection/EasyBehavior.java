package com.tian.gamecollection;

import android.content.Context;
import android.support.design.widget.CoordinatorLayout;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

/**
 * @author jisheng ,为写下的每行代码负责
 * @date 2017/1/5
 * @describe
 */

public class EasyBehavior extends CoordinatorLayout.Behavior<TextView>
{
    public EasyBehavior(Context context, AttributeSet atts)
    {
        super(context,atts);
    }

    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, TextView child, View dependency)
    {
        return dependency instanceof Button;
    }

    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, TextView child, View dependency)
    {
        child.setX(dependency.getX()+100);
        child.setY(dependency.getY()+200);
        child.setText("TEST");
        return true;
    }
}
