package com.yongyida.yydrobotcv.customview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.yongyida.yydrobotcv.R;

/**
 * @author Brandon on 2018/3/14
 **/
public class SiderBar extends View {

    public static final String TAG = SiderBar.class.getSimpleName();
    int width = 55;
    private Paint paint = new Paint();

    private int choose = 0;//默认还是要选择开头

    public void setRecycleView(RecyclerView mRecycleView) {
        this.recycleView = mRecycleView;
    }

    RecyclerView recycleView;

    public static String[] letters = {"#", "A", "B", "C", "D", "E", "F", "G", "H",
            "I", "J", "K", "L"};

//    , "M", "N", "O", "P", "Q", "R", "S", "T", "U",
//            "V", "W", "X", "Y", "Z"

    private OnChooseLetterChangedListener onChooseLetterChangedListener;

    public SiderBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public SiderBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SiderBar(Context context) {
        super(context);
    }

    protected void onDraw(Canvas canvas) {

        super.onDraw(canvas);

        int height = getHeight()/2+12;

        //平均每个字母占的高度width / letters.length
        int singleWidth = width;
        for (int i = 0; i < letters.length; i++) {
            //字体颜色
            paint.setColor(getResources().getColor(R.color.colorText));
            paint.setAntiAlias(true);
            //字体大小
            paint.setTextSize(44);
            int y = height;
            int x = singleWidth * i + singleWidth/2;
            if (i == choose) {
                //画text所占的区域
                Paint.FontMetricsInt fm = paint.getFontMetricsInt();
                int top = y + fm.top;
                int bottom = y + fm.bottom;
                int width1 = (int)paint.measureText(letters[i]);
                Rect rect = new Rect(x,top,x+width1,bottom);
                paint.setColor(Color.BLUE);
//                canvas.drawCircle(x+width1/2,y-width1/2,20,paint);
                Bitmap letterBackground = BitmapFactory.decodeResource(getResources(),R.mipmap.ic_letter_choiced);
                canvas.drawBitmap(letterBackground,x-17,y-46,paint);

                paint.setColor(Color.parseColor("#ffffffff"));
//                recycleView.scrollToPosition(i);
                paint.setFakeBoldText(true);
            }

            canvas.drawText(letters[i], x, y, paint);

            paint.reset();
        }
    }


    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        int action = event.getAction();
        float x = event.getX();
        int oldChoose = choose;
        int c = (int) (x / getWidth() * letters.length);
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                if (oldChoose != c ) {
                    if (c > -1 && c < letters.length) {
                        Log.e(TAG,"ACTION_DOWN"+c);
                        if (onChooseLetterChangedListener != null)
                        onChooseLetterChangedListener.onChooseLetter(letters[c]);
                        choose = c;
                        invalidate();
                    }
                }
                break;
            case MotionEvent.ACTION_MOVE:

                if (oldChoose != c) {
                    if (c > -1 && c < letters.length) {
                        Log.e(TAG,"ACTION_MOVE"+c);
                        if (onChooseLetterChangedListener != null)
                        onChooseLetterChangedListener.onChooseLetter(letters[c]);
                        choose = c;
                        invalidate();
                    }

                }
                break;
//            case MotionEvent.ACTION_UP:
//                Log.e(TAG,"ACTION_UP"+c);
//                showBackground = false;
//                choose = -1;
//                if (onChooseLetterChangedListener != null) {
//                    onChooseLetterChangedListener.onNoChooseLetter();
//                }
//                invalidate();
//                break;
        }
        return true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return true;
    }

    public void setOnTouchingLetterChangedListener(OnChooseLetterChangedListener onChooseLetterChangedListener) {
        this.onChooseLetterChangedListener = onChooseLetterChangedListener;
    }

    public interface OnChooseLetterChangedListener {

        void onChooseLetter(String s);

    }

    public void setLetters(String s){

            if (!s.equals(choose)){
                for (int i = 0;i<letters.length;i++){
                    if (s.equals(letters[i])){
                        choose=i;
                        break;
                    }
                }
                invalidate();
            }
    }

}


