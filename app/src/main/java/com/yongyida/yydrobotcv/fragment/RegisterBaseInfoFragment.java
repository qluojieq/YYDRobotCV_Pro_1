package com.yongyida.yydrobotcv.fragment;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bigkoo.pickerview.adapter.ArrayWheelAdapter;
import com.bigkoo.pickerview.lib.WheelView;
import com.yongyida.yydrobotcv.R;
import com.yongyida.yydrobotcv.RegisterActivity;
import com.yongyida.yydrobotcv.useralbum.User;
import com.yongyida.yydrobotcv.useralbum.UserDataSupport;
import com.yongyida.yydrobotcv.utils.CommonUtils;

import java.util.ArrayList;

import dou.utils.ToastUtil;

/**
 * @author Brandon on 2018/3/15
 **/
public class RegisterBaseInfoFragment extends Fragment implements View.OnClickListener {

    private final String TAG = RegisterBaseInfoFragment.class.getSimpleName();
    int currentStep = 1;//一共会有四部分，分别注册对应的信息；
    TextView nextStepBtn;
    ImageView stepHintView;
    User registerUser2;
    WheelView genderWheelView;
    LinearLayout genderTableView;
    LinearLayout nameTableView;
    LinearLayout phoneTableView;
    LinearLayout isStepClickAbleView;
    FrameLayout birthdayTableView;

    TextView stepHint1;
    TextView stepHint2;
    TextView stepHint3;
    TextView stepHint4;

    TextView warnTextName;
    TextView warnTextPhone;

    FragmentManager fm;
    FragmentTransaction ft;

    BirthDayChoiceFragment birthDayChoiceFragment;

    EditText phoneNumView;
    EditText nameView;
    ArrayList<String> genderList;

    public static Handler mHandler;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.enroll__baseinfo_fragment, container, false);
        initView(view);
        fm = getFragmentManager();
        ft = fm.beginTransaction();

        birthDayChoiceFragment = new BirthDayChoiceFragment();
        ft.add(R.id.insert_birthday_tap, birthDayChoiceFragment).commit();
        mHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                switch (msg.what) {
                    case 1:
                        warnTextPhone.setVisibility(View.GONE);
                        break;
                    case 2:
                        warnTextPhone.setVisibility(View.VISIBLE);
                        break;
                    case 3:
                        Log.e(TAG, "不符合条件");
//                        warnTextName.setVisibility(View.VISIBLE);
                        msg.obj.toString();
                        warnTextName.setText(msg.obj.toString());
                        break;

                }
                return true;
            }
        });
        return view;
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        Log.e(TAG, "onHiddenChanged" + hidden);
        if (hidden) {
            saveData();//隐藏的时候保存一下信息
        } else {
//            currentStep = 1;
        }
    }

    @Override
    public void onClick(View v) {
        Log.e(TAG, "next step is been pressed 当前步数" + currentStep);
        if (currentStep == 2) {

            int checkNameResult = CommonUtils.checkoutName(nameView.getText().toString(),getActivity());
            switch (checkNameResult) {
                case -1:
                    new ToastUtil(this.getActivity()).showSingletonToast("名字不能为空 ");
                    return;
                case -12:
                    new ToastUtil(this.getActivity()).showSingletonToast("超出字符长度");
                    return;
                case -11:
                    new ToastUtil(this.getActivity()).showSingletonToast("不能有特殊字符");
                    return;
                case -10:
                    new ToastUtil(this.getActivity()).showSingletonToast("不支持中英文混合名");
                    return;
                case -9:
                    new ToastUtil(this.getActivity()).showSingletonToast("用户名已被使用");
                    return;

            }
        }
        if (currentStep == 1) {
            String phoneNum = phoneNumView.getText().toString();
            if (TextUtils.isEmpty(phoneNum)) {
                new ToastUtil(this.getActivity()).showSingletonToast("手机号码不能空缺 ");
                return;
            } else if (!CommonUtils.isMatchPhone(phoneNum)) {
                new ToastUtil(this.getActivity()).showSingletonToast("手机号码不符合规则");
                return;
            } else if (UserDataSupport.getInstance(this.getActivity()).checkPhoneNum(phoneNum)){
                new ToastUtil(this.getActivity()).showSingletonToast("手机号码已经被注册");
                return;
            }
        }

        switch (v.getId()) {
            case R.id.btn_info_next:
                if (currentStep < 5) {
                    currentStep++;
                }
                switchTable(currentStep);
                break;
            case R.id.hint_info1:
                currentStep = 1;
                switchTable(currentStep);
                break;
            case R.id.hint_info2:
                currentStep = 2;
                switchTable(currentStep);
                break;
            case R.id.hint_info3:
                currentStep = 3;
                switchTable(currentStep);
                break;
            case R.id.hint_info4:
                currentStep = 4;
                switchTable(currentStep);
                break;
        }

    }

    public void initView(View view) {

        stepHint1 = view.findViewById(R.id.hint_info1);
        stepHint1.setOnClickListener(this);
        stepHint2 = view.findViewById(R.id.hint_info2);
        stepHint2.setOnClickListener(this);
        stepHint3 = view.findViewById(R.id.hint_info3);
        stepHint3.setOnClickListener(this);
        stepHint4 = view.findViewById(R.id.hint_info4);
        stepHint4.setOnClickListener(this);

        nextStepBtn = view.findViewById(R.id.btn_info_next);
        stepHintView = view.findViewById(R.id.info_step_hint);
        nextStepBtn.setOnClickListener(this);
        genderWheelView = view.findViewById(R.id.gender_choice);
        warnTextName = view.findViewById(R.id.input_warn_name);
        warnTextPhone = view.findViewById(R.id.input_warn_phone);

        genderTableView = view.findViewById(R.id.insert_gender_tap);
        nameTableView = view.findViewById(R.id.insert_name_tap);
        phoneTableView = view.findViewById(R.id.insert_phone_tap);



        birthdayTableView = view.findViewById(R.id.insert_birthday_tap);
        isStepClickAbleView = view.findViewById(R.id.btn_hint_clickable);

        phoneNumView = view.findViewById(R.id.edit_phone);

        //键盘显示监听
        phoneNumView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener(){

            //当键盘弹出隐藏的时候会 调用此方法。
            @Override
            public void onGlobalLayout() {
                final Rect rect = new Rect();
                getActivity().getWindow().getDecorView().getWindowVisibleDisplayFrame(rect);
                final int screenHeight = getActivity().getWindow().getDecorView().getRootView().getHeight();
                Log.e("TAG",rect.bottom+"#"+screenHeight);
                final int heightDifference = screenHeight - rect.bottom;
                boolean visible = heightDifference > screenHeight / 3;
                if(visible){
//                    Log.e(TAG,"显示");
                    changeKeyboardStateOut(phoneTableView);
                }else {

//                    Log.e(TAG,"键盘隐藏");
                    changeKeyboardStateIn(phoneTableView);
                }
            }
        });
        phoneNumView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                boolean isMatch = CommonUtils.isMatchPhone(s.toString());
                if (isMatch) {
                    mHandler.sendEmptyMessage(1);
                } else {
                    mHandler.sendEmptyMessage(2);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (TextUtils.isEmpty(nameView.getText())) {
                    isStepClickAbleView.setClickable(false);
                } else {
                    isStepClickAbleView.setClickable(true);
                }
            }
        });
        phoneNumView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEND
                        || actionId == EditorInfo.IME_ACTION_DONE
                        || (event != null
                        && KeyEvent.KEYCODE_ENTER == event.getKeyCode()
                        && KeyEvent.ACTION_DOWN == event.getAction())) {
                    Log.e(TAG, "actionId " + actionId + "event " + EditorInfo.IME_ACTION_NEXT);
                    nextStepBtn.performClick();
                    nextStepBtn.setFocusable(true);
                }

                return true;
            }
        });
        nameView = view.findViewById(R.id.edit_name);

        nameView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener(){

            //当键盘弹出隐藏的时候会 调用此方法。
            @Override
            public void onGlobalLayout() {
                final Rect rect = new Rect();
                getActivity().getWindow().getDecorView().getWindowVisibleDisplayFrame(rect);
                final int screenHeight = getActivity().getWindow().getDecorView().getRootView().getHeight();
                Log.e("TAG",rect.bottom+"#"+screenHeight);
                final int heightDifference = screenHeight - rect.bottom;
                boolean visible = heightDifference > screenHeight / 3;
                if(visible){
                    changeKeyboardStateOut(nameTableView);
                }else {
                    changeKeyboardStateIn(nameTableView);
                }
            }
        });
        nameView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                int checkoutResult = CommonUtils.checkoutName(s.toString(),getActivity());
                Message message = new Message();
                Log.e(TAG,"字节变化了" + checkoutResult);
                switch (checkoutResult) {
                    case -1:
                        message.obj = "名字不能为空";
                        break;
                    case -12:
                        message.obj = "请重新输入，汉字不超过6个字，英文不超过12个字符";
                        break;
                    case -11:
                        message.obj = "请重新输入，禁止含有特殊字符";
                        break;
                    case -10:
                        message.obj = "请重新输入，不支持中英文混合输入";
                        break;
                    case -9:
                        message.obj = "请重新输入，名字已被使用";
                        break;
                    case 0:
                        Log.e(TAG,"没有找到匹配项");
                        message.obj = "中文不超过6个字符，英文不超过12个字符，不支持中英混合输入";
                        break;
                }
                message.what = 3;
                mHandler.sendMessage(message);

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (TextUtils.isEmpty(nameView.getText())) {
                    isStepClickAbleView.setClickable(false);
                } else {
                    isStepClickAbleView.setClickable(true);
                }
            }
        });
        nameView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEND
                        || actionId == EditorInfo.IME_ACTION_DONE
                        || (event != null
                        && KeyEvent.KEYCODE_ENTER == event.getKeyCode()
                        && KeyEvent.ACTION_DOWN == event.getAction())) {
                    Log.e(TAG, "actionId " + actionId + "event " + EditorInfo.IME_ACTION_NEXT);
                    nextStepBtn.performClick();
                    nextStepBtn.setFocusable(true);
//                    if (!TextUtils.isEmpty(nameView.getText()) && CommonUtils.isMatchName(nameView.getText().toString())) {
//                        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
//                        imm.toggleSoftInputFromWindow(v.getWindowToken(), 0, 0);
//                    }
                }
                return true;
            }
        });
        registerUser2 = ((RegisterActivity) this.getActivity()).getRegisterUser();
        //初始化显示数据
        genderList = new ArrayList();
        genderList.add("保密");
        genderList.add("男");
        genderList.add("女");
        genderWheelView.setAdapter(new ArrayWheelAdapter(genderList));// 设置"年"的显示数据
        genderWheelView.setLabel("");// 添加文字
        Log.e(TAG, "从activity中获取的 年龄" + registerUser2.getAge() + " 年龄" + registerUser2.getGender());
        if (TextUtils.isEmpty(registerUser2.getGender())) {//初始化男女
            genderWheelView.setCurrentItem(0);//
        } else {
            if (registerUser2.getGender().equals("-1")) {
                genderWheelView.setCurrentItem(2);//
            } else {
                genderWheelView.setCurrentItem(1);//
            }
        }
        genderWheelView.setGravity(Gravity.CENTER);
        genderWheelView.setTextColorCenter(getResources().getColor(R.color.colorTextWrite));
        genderWheelView.setTextSize(16);
        genderWheelView.setCyclic(false);
        registerUser2.setUserName("Brandon");
    }

    public void switchTable(int position) {
        InputMethodManager im = ((InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE));

        switch (position){
            case 3:
            case 4:
                if (im.isActive()){
                    Log.e(TAG,"hide is done");
                    im.hideSoftInputFromWindow(getActivity().getWindow().getDecorView().getWindowToken(), 0);
                }
                break;
        }
        switch (position) {
            case 1:
                nextStepBtn.setText("下一步");
                stepHintView.setImageResource(R.mipmap.info_step_1);
                phoneTableView.setVisibility(View.VISIBLE);
                phoneNumView.setFocusable(true);
                phoneNumView.setFocusableInTouchMode(true);
                phoneNumView.requestFocus();
                nameTableView.setVisibility(View.INVISIBLE);
                genderTableView.setVisibility(View.INVISIBLE);
                birthdayTableView.setVisibility(View.INVISIBLE);
                break;
            case 2:
                nextStepBtn.setText("下一步");
                stepHintView.setImageResource(R.mipmap.info_step_2);
                phoneTableView.setVisibility(View.INVISIBLE);
                nameTableView.setVisibility(View.VISIBLE);
                genderTableView.setVisibility(View.INVISIBLE);
                nameView.setFocusable(true);
                nameView.setFocusableInTouchMode(true);
                nameView.requestFocus();
                birthdayTableView.setVisibility(View.INVISIBLE);
                break;
            case 3:
                nextStepBtn.setText("下一步");
                stepHintView.setImageResource(R.mipmap.info_step_3);
                phoneTableView.setVisibility(View.INVISIBLE);
                nameTableView.setVisibility(View.INVISIBLE);
                genderTableView.setVisibility(View.VISIBLE);
                birthdayTableView.setVisibility(View.INVISIBLE);
                if (!TextUtils.isEmpty(registerUser2.getAge())) {
                    birthDayChoiceFragment.setCurrentDate(registerUser2.getAge());
                }
                break;
            case 4:
                nextStepBtn.setText("完成");
                stepHintView.setImageResource(R.mipmap.info_step_4);
                phoneTableView.setVisibility(View.INVISIBLE);
                nameTableView.setVisibility(View.INVISIBLE);
                genderTableView.setVisibility(View.INVISIBLE);
                birthdayTableView.setVisibility(View.VISIBLE);
                break;
            case 5:
                saveData();
                long ret = ((RegisterActivity) this.getActivity()).doEnd();
                if (ret > 0) {
                    ((RegisterActivity) this.getActivity()).setResult(RegisterActivity.ADD_SUCCESS_RESULT_CODE);
                    this.getActivity().finish();
                } else {

                }

                this.getActivity().finish();
                break;
        }
    }

    public void saveData() {
        if (null != phoneNumView) {
            String phoneNum = phoneNumView.getText().toString();
            String nameString = nameView.getText().toString();
            String genderString = genderList.get(genderWheelView.getCurrentItem());
            String birthdayString = birthDayChoiceFragment.getBirthday();
            Log.e(TAG, "电话号码 " + phoneNum + "名字 " + nameString);
            registerUser2.setPhoneNum(phoneNum);
            registerUser2.setUserName(nameString);
            registerUser2.setGender(genderString);
            registerUser2.setBirthDay(birthdayString);
            ((RegisterActivity) this.getActivity()).setRegisterUser(registerUser2, 2);
        }
    }


    // 键盘出现的时候
    public void changeKeyboardStateOut(View view){
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) view.getLayoutParams();
        params.removeRule(RelativeLayout.CENTER_VERTICAL);
        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
//        params.addRule(RelativeLayout.CENTER_HORIZONTAL);
        view.setLayoutParams(params);
    }

    // 键盘隐藏的时候
    public void changeKeyboardStateIn(View view){
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) view.getLayoutParams();
        params.removeRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
//        params.removeRule(RelativeLayout.CENTER_HORIZONTAL);
        params.addRule(RelativeLayout.CENTER_VERTICAL);
        view.setLayoutParams(params);
    }


}
