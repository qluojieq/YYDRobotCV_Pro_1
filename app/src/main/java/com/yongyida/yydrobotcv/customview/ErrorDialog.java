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
public class ErrorDialog extends Dialog implements View.OnClickListener {
    TextView confirmBtn;
    TextView markWordShowView;

    String markWord = "";

    public ErrorDialog(@NonNull Context context) {
        super(context);
    }

    OnCloseListener listener;

    public ErrorDialog(Context context, int themeResId, OnCloseListener listener , String showString){
        super(context,themeResId);
        this.listener = listener;
        markWord = showString;

    }

    public ErrorDialog(@NonNull Context context, int themeResId, OnCloseListener listener) {
        super(context, themeResId);
        this.listener = listener;
    }

    public ErrorDialog(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.custom_dialog_error);
        setCanceledOnTouchOutside(false);

        confirmBtn = findViewById(R.id.cancel_confirm);
        markWordShowView = findViewById(R.id.reference_dialog);
        confirmBtn.setOnClickListener(this);
        if (!TextUtils.isEmpty(markWord)){
            markWordShowView.setText(markWord);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.cancel_confirm:
                listener.clickConfirm();
                break;

        }

    }

    public interface OnCloseListener {
        void clickConfirm();
    }
}
