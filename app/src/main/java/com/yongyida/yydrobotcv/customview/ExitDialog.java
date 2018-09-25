package com.yongyida.yydrobotcv.customview;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.yongyida.yydrobotcv.R;

/**
 * @author Brandon on 2018/4/12
 **/
public class ExitDialog extends Dialog implements View.OnClickListener {
    TextView confirmBtn;
    TextView cancelBtn;
    TextView markWordShowView;

    String markWord = "";

    public ExitDialog(@NonNull Context context) {
        super(context);
    }

    OnCloseListener listener;

    public ExitDialog(Context context, int themeResId, OnCloseListener listener ,String showString){
        super(context,themeResId);
        this.listener = listener;
        markWord = showString;

    }

    public ExitDialog(@NonNull Context context, int themeResId, OnCloseListener listener) {
        super(context, themeResId);
        this.listener = listener;
    }

    public ExitDialog(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.custom_dialog);
        setCanceledOnTouchOutside(false);
        cancelBtn = findViewById(R.id.cancel_close);
        confirmBtn = findViewById(R.id.cancel_confirm);
        markWordShowView = findViewById(R.id.reference_dialog);
        confirmBtn.setOnClickListener(this);
        cancelBtn.setOnClickListener(this);
        if (!TextUtils.isEmpty(markWord)){
            markWordShowView.setText(markWord);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.cancel_close:
                listener.clickCancel();
                break;
            case R.id.cancel_confirm:
                listener.clickConfirm();
                break;

        }

    }

    public interface OnCloseListener {
        void clickConfirm();

        void clickCancel();

    }
}
