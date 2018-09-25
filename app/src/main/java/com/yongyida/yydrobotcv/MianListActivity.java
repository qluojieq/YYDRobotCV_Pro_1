package com.yongyida.yydrobotcv;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityCompat.OnRequestPermissionsResultCallback;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.yongyida.yydrobotcv.customview.HorizontalSideBar;
import com.yongyida.yydrobotcv.useralbum.User;
import com.yongyida.yydrobotcv.useralbum.UserDataSupport;
import com.yongyida.yydrobotcv.utils.ChineseCharacterUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import static android.support.v7.widget.RecyclerView.SCROLL_STATE_DRAGGING;
import static android.support.v7.widget.RecyclerView.SCROLL_STATE_IDLE;
import static android.support.v7.widget.RecyclerView.SCROLL_STATE_SETTLING;

/**
 * @author Brandon on 2018/3/13
 **/
public class MianListActivity extends AppCompatActivity implements OnRequestPermissionsResultCallback, HorizontalSideBar.OnChooseChangeListener {

    private static final int BASE_INFO_REQUEST = 10;
    private static final int NEW_ADD_REQUEST = 11;
    public static final String TAG = MianListActivity.class.getSimpleName();

    UserDataSupport dataSupport;
    // Used to load the 'native-lib' library on application startup.
    public static List<User> usersData;

    public static Handler mHandler;

    RecyclerView userRecycleView;
    UsersAdapter userDataAdapter;
    HorizontalSideBar mSiderBar;
    final GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2, GridLayoutManager.HORIZONTAL, false);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                return false;
            }
        });
        setContentView(R.layout.main_activity);
        dataSupport = UserDataSupport.getInstance(this);
        userRecycleView = (RecyclerView) findViewById(R.id.user_recycle);
        mSiderBar = findViewById(R.id.side_bar);
        mSiderBar.setOnChooseChangeListener(this);
        userRecycleView.setLayoutManager(gridLayoutManager);

        //  获取数据
        usersData = dataSupport.getAllUsers("list");
        userDataAdapter = new UsersAdapter(this);
        userDataAdapter.setOnItemClickListener(new UsersAdapter.OnItemClickListener() {
            @Override
            public void onClick(View view, int position) {
                Log.e(TAG, "这个位置白点击了 " + position);
                if (position == 0) {
                    addNewUser(null);
                } else {
                    Intent intent = new Intent(MianListActivity.this, BaseInfoShowActivity.class);
                    usersData = dataSupport.getAllUsers("list"); // 更新一下数据
                    intent.putExtra("one_user", usersData.get(position));
                    Log.e(TAG, "最后访问次数 " + usersData.get(position).getIdentifyCount());
                    startActivityForResult(intent, 10);
                }
            }
        });
        userRecycleView.setAdapter(userDataAdapter);
        userRecycleView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                switch (newState) {
                    case SCROLL_STATE_IDLE:
                        Log.e(TAG, "静止");
                        break;
                    case SCROLL_STATE_DRAGGING:
                        Log.e(TAG, "拖动");
                        break;
                    case SCROLL_STATE_SETTLING:
                        Log.e(TAG, "设置");
                        break;

                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int firstVisibleItemPosition = gridLayoutManager.findFirstVisibleItemPosition() + 1;//可见范围内的第一项的位置
                String lettersIndex[] = indexLetter.toArray(new String[indexLetter.size()]);
                if (usersData.size() == 1) {
                    mSiderBar.setLetters(lettersIndex, 0);
                } else {
                    if (!isOver) {
                        String temp = ChineseCharacterUtil.getFirstChar(usersData.get(firstVisibleItemPosition).getUserName());// 获取首字母
                        historyChoose = indexLetter.indexOf(temp);
                        mSiderBar.setLetters(lettersIndex, indexLetter.indexOf(temp));
                    }

                }

            }
        });
    }

    int historyChoose = 0;

    @Override
    protected void onResume() {
        super.onResume();
        callpremission();
        indexLetter = dataSupport.getIndexLetter();
//        usersData = getTestUsersData(); // 测试数据
        mSiderBar.setLetters(indexLetter.toArray(new String[indexLetter.size()]), historyChoose); // 初始化的值
    }


    //添加按钮
    public void addNewUser(View view) {
        Intent intent = new Intent(MianListActivity.this, RegisterActivity.class);
        startActivityForResult(intent, NEW_ADD_REQUEST);
    }

    public void mainBack(View view) {
        this.finish();
    }


    @Override
    public void chooseLetter(String letter) {
//        userRecycleView.smoothScrollToPosition(12);
        historyChoose = scrollString(letter);
        Log.e(TAG, "touch been clicked show " + historyChoose);
        gridLayoutManager.scrollToPositionWithOffset(historyChoose, 0);
    }

    public static class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.MyViewHolder> implements View.OnClickListener {

        public interface OnItemClickListener {
            void onClick(View view, int position);
        }

        OnItemClickListener mOnItemClickListener = null;

        public void setOnItemClickListener(OnItemClickListener listener) {
            mOnItemClickListener = listener;
        }

        Context mContext;


        public UsersAdapter(Context context) {
            mContext = context;
        }

        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.main_recycle_item, parent, false);
            view.setOnClickListener(this);
            MyViewHolder holder = new MyViewHolder(view);
            return holder;
        }

        @Override
        public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
            TextView textView = holder.itemView.findViewById(R.id.item_name);
            ImageView portraitView = holder.itemView.findViewById(R.id.item_portrait);
            String name = usersData.get(position).getUserName();
            Bitmap bigMap = null;

            if (position == 0) {
                bigMap = BitmapFactory.decodeResource(mContext.getResources(), R.mipmap.ic_add);
                portraitView.setImageBitmap(bigMap);
            } else {

                try {
                    File avaterFile = new File(mContext.getCacheDir() + "/" + usersData.get(position).getPersonId() + ".jpg");
                    if (avaterFile.exists()) {
                        bigMap = BitmapFactory.decodeFile(avaterFile.getAbsolutePath());
                        RoundedBitmapDrawable roundedBitmapDrawable = RoundedBitmapDrawableFactory.create(mContext.getResources(), bigMap);
                        roundedBitmapDrawable.setCircular(true);
                        portraitView.setImageDrawable(roundedBitmapDrawable);
                    }

                } catch (Exception e) {
                }

            }
            textView.setText(name);
            holder.itemView.setTag(position);
        }


        @Override
        public int getItemCount() {
            int size = usersData.size();
            Log.e(TAG, "拥有登记数目 " + size);
            return size;
        }

        @Override
        public void onClick(View v) {
            if (mOnItemClickListener != null) {
                mOnItemClickListener.onClick(v, (int) v.getTag());
            }
        }

        static class MyViewHolder extends RecyclerView.ViewHolder {
            View itemView;

            public MyViewHolder(View itemView) {
                super(itemView);
                this.itemView = itemView;
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.e(TAG, "requestCode " + requestCode + "  resultCode" + resultCode);
        switch (requestCode) {
            case BASE_INFO_REQUEST:

                if (resultCode == BaseInfoShowActivity.DELETE_SUCCESS_RESULT_CODE) {
                    Log.e(TAG, "删除成功，更新一下数据" + usersData.size());
                    usersData.clear();
                    usersData = dataSupport.getAllUsers("list");
                    userDataAdapter.notifyDataSetChanged();

                }
                break;
            case NEW_ADD_REQUEST:
                if (resultCode == RegisterActivity.ADD_SUCCESS_RESULT_CODE) {
                    usersData.clear();
                    usersData = dataSupport.getAllUsers("list");
                    userDataAdapter.notifyDataSetChanged();
                    Log.e(TAG, "添加成功，更新一下数据" + usersData.size());
                }
                break;

        }

    }


    boolean isOver = false;

    public int scrollString(String targetChar) {
        int ret = 0;
        int i = 1;
        for (; i < usersData.size(); i++) {
            if (targetChar.equals(ChineseCharacterUtil.getFirstChar(usersData.get(i).getUserName()))) {
                break;
            }
        }
        ret = i;
        Log.e(TAG, "点击到啊字母 " + targetChar + "首次出现该字母的位置 " + ret);
        isOver = false;
        if (usersData.size() > 9 && i > usersData.size() - 10) {
            mHandler.sendEmptyMessage(usersData.size() - 10);
            Log.e(TAG, "超出滑动范围 " + usersData.get(usersData.size() - 10).getUserName());
            isOver = true;
            if (usersData.size() / 2 == 0) {
                ret = usersData.size() - 10;
            } else {
                ret = usersData.size() - 9;
            }

        }
        return ret;
    }

    //获取权限
    public void callpremission() {
        //系统版本号23/6.0之后/api23
        if (Build.VERSION.SDK_INT >= 23) {
            //检查有没有所需的权限 PackageManager.PERMISSION_GRANTED：授权了权限
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                //请求获取所需的权限，第二个参数：需要的权限（可以多个集合）第三个参数：请求码
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUIRE_CODE_CALL_CAMERA);
                return;
            }
        }
    }

    private final static int REQUIRE_CODE_CALL_CAMERA = 10;

    //权限获取回调的方法
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUIRE_CODE_CALL_CAMERA:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.e("权限log", "回调");
                } else {
                    // Permission Denied拒绝
                    Toast.makeText(this, "CAMERA Denied", Toast.LENGTH_SHORT)
                            .show();
                    SharedPreferences gosetting = getSharedPreferences("gosetting", MODE_PRIVATE);
                    boolean isGoSetting = gosetting.getBoolean("isGoSetting", false);
                    //用户首次拒绝申请权限时，不需弹窗提示去设置申请权限
                    if (isGoSetting) {
                        //当缺少权限时弹窗提示
                        AlertDialog.Builder builder = new AlertDialog.Builder(this);
                        builder.setIcon(R.mipmap.ic_launcher)
                                .setTitle("缺少权限")
                                .setMessage("去设置权限")
                                .setPositiveButton("GoSetting", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        //打开App的设置
                                        getAppDetailSettingIntent(getBaseContext());
                                    }
                                }).show();
                    }
                    SharedPreferences.Editor edit = gosetting.edit();
                    edit.putBoolean("isGoSetting", true).commit();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    //打开App的设置
    private void getAppDetailSettingIntent(Context context) {
        Intent localIntent = new Intent();
        localIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (Build.VERSION.SDK_INT >= 9) {
            localIntent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
            localIntent.setData(Uri.fromParts("package", getPackageName(), null));
        } else if (Build.VERSION.SDK_INT <= 8) {
            localIntent.setAction(Intent.ACTION_VIEW);
            localIntent.setClassName("com.android.settings", "com.android.settings.InstalledAppDetails");
            localIntent.putExtra("com.android.settings.ApplicationPkgName", getPackageName());
        }
        startActivity(localIntent);
    }

    ArrayList<String> indexLetter = new ArrayList();

    public List<User> getTestUsersData() {
        String index[] = {"#", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z", "1", "2", "3", "4", "5", "6", "7", "8", "9", "0"};

        List<User> allUsers = new ArrayList<>();
        User user1 = new User();
        user1.setUserId("");
        user1.setPersonId("");
        user1.setUserName("添加");
        user1.setBirthDay("");
        user1.setGender("");
        user1.setPhoneNum("");
        user1.setVipRate("");
        user1.setHeadPortrait("");
        user1.setIdentifyCount("");
        user1.setTag("");
        Random random = new Random();
        allUsers.add(user1);

        for (int i = 0; i < 10; i++) {
            User user2 = new User();
            user2.setUserId("");
            user2.setPersonId("");
            user2.setUserName(index[random.nextInt(37)]);
            user2.setBirthDay("");
            user2.setGender("");
            user2.setPhoneNum("");
            user2.setVipRate("");
            user2.setHeadPortrait("");
            user2.setIdentifyCount("");
            user2.setTag("");
            allUsers.add(user2);
        }
        Collections.sort(allUsers, new Comparator<User>() {
            @Override
            public int compare(User o1, User o2) {

                if (!indexLetter.contains(ChineseCharacterUtil.getFirstChar(o1.getUserName()).charAt(0) + "")) {
                    indexLetter.add(ChineseCharacterUtil.getFirstChar(o1.getUserName()).charAt(0) + "");
                    Log.e(TAG, ChineseCharacterUtil.getFirstChar(o1.getUserName()).charAt(0) + "比较遍历");
                }
                if (o1.getUserName().equals("添加") || o2.getUserName().equals("添加")) {
                    return 1;
                } else {
                    Log.e(TAG, ChineseCharacterUtil.getFirstChar(o1.getUserName()).charAt(0) + " 值");
                    return ChineseCharacterUtil.getFirstChar(o1.getUserName()).charAt(0) - ChineseCharacterUtil.getFirstChar(o2.getUserName()).charAt(0);
                }
            }
        });
        Collections.sort(indexLetter);
        Log.e(TAG, "indexLetter length " + indexLetter.size());
        return allUsers;
    }
}
