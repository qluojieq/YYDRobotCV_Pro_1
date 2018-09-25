package com.yongyida.yydrobotcv.customview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.yongyida.yydrobotcv.R;


/**
 * @author Brandon on 2018/8/22
 **/
public class HorizontalSideBar extends View {


    private static final String TAG = HorizontalSideBar.class.getSimpleName();
    String letters[] = {"#", "A","B","C","D","E","F","G","H","I","J","K","L","M","N","O","P","Q","R","S","T","U","V","W","X","Y","Z"};
    int textSize = 50;
    int textColor = 0x00000;
    int selectColor = 0x000000;
    int choose = 1;
    int letterOffset = textSize/6*2; // 字间距
    int viewHeight = 2*textSize;
    OnChooseChangeListener onChooseChangeListener;

    public HorizontalSideBar(Context context) {
        this(context,null);
    }

    public HorizontalSideBar(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public HorizontalSideBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        // 设置属性
        TypedArray typedArray = context.getTheme().obtainStyledAttributes(attrs, R.styleable.HorizontalSideBar, defStyleAttr, 0);
        textSize = typedArray.getDimensionPixelSize(R.styleable.HorizontalSideBar_textSize, 100);
        textColor = typedArray.getColor(R.styleable.HorizontalSideBar_textColor, 0xffffff);
        selectColor = typedArray.getColor(R.styleable.HorizontalSideBar_selectTextColor,0xffffff);
        typedArray.recycle();
        viewHeight = textSize*2;
        letterOffset = textSize*6/5;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        for (int i = 0 ; i<letters.length ; i++){
            Paint p = new Paint();
            p.setTextSize(textSize);
            Paint.FontMetricsInt fm = p.getFontMetricsInt();
            int charX = letterOffset*(i+1);
            int charY = viewHeight/3 + (fm.bottom-fm.top)/2 ;
            if (i== choose){
                p.setColor(selectColor);
                p.setFakeBoldText(true);
                canvas.drawText(letters[i], charX, charY, p);
                Bitmap b = BitmapFactory.decodeResource(getResources(),R.mipmap.ic_letter_choiced);
                int pad = 10;
                int left = charX -pad;
                int top = charY - (fm.bottom-fm.top)/2 - pad;
                int right = charX + (fm.bottom-fm.top)/2 + pad;
                int bottom = charY + pad;
                canvas.drawBitmap(b,null,new Rect(left,top,right,bottom),p);
            }else {
                p.setColor(textColor);
                canvas.drawText(letters[i], charX, charY, p);
            }

            p.reset();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int widthSpecSize = MeasureSpec.getSize(widthMeasureSpec);
        int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);

        int heightSpecSize = MeasureSpec.getSize(heightMeasureSpec);
        int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);

        int w = widthSpecSize;
        int h = heightSpecSize;

        //处理wrap_content的几种特殊情况
        if (widthSpecMode == MeasureSpec.AT_MOST && heightSpecMode == MeasureSpec.AT_MOST) {
            w = letterOffset*(letters.length+1);  //单位是px
            h = viewHeight;
        } else if (widthSpecMode == MeasureSpec.AT_MOST) {
            //只要宽度布局参数为wrap_content， 宽度给固定值200dp(处理方式不一，按照需求来)
            w = 400;
            //按照View处理的方法，查看View#getDefaultSize可知
            h = heightSpecSize;
        } else if (heightSpecMode == MeasureSpec.AT_MOST) {
            w = widthSpecSize;
            h = viewHeight;
        }
        setMeasuredDimension(w,h);
    }
    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {

        float x = event.getX();
        int oldChoose = choose;
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
            case MotionEvent.ACTION_UP:
                choose = (int) x/(letterOffset) -1 ;
                if (choose<0){
                    choose = 0;
                }else if(choose>=letters.length){
                    choose = letters.length - 1;
                }
                if (oldChoose!= choose){
                    if (onChooseChangeListener!=null)
                    onChooseChangeListener.chooseLetter(letters[choose]);
                    Log.e(TAG,letters[oldChoose]+"touch been clicked " + letters[choose]);
                    invalidate();
                }
                break;
        }
        return super.dispatchTouchEvent(event);
    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return true;
    }

    public void setLetters(String[] letters,int choose) {
        for (int i = 0;i<letters.length;i++){
            Log.e(TAG, "letters " + letters[i]);
        }
        this.choose = choose;
        this.letters = letters;
        invalidate();
        requestLayout();
    }
   public interface OnChooseChangeListener{
        void chooseLetter(String letter);
    }

    public void setOnChooseChangeListener(OnChooseChangeListener onChooseChangeListener) {
        this.onChooseChangeListener = onChooseChangeListener;
    }
}
