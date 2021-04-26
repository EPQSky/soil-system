package icu.epq.android.soilapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;

import com.google.gson.Gson;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import icu.epq.android.soilapp.activity.ReadTimeDataActivity;
import icu.epq.android.soilapp.dto.AuthToken;
import icu.epq.android.soilapp.model.DataStoreOwner;
import icu.epq.android.soilapp.model.DeviceDataViewModel;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * 土壤监测系统首页 Activity
 * <p>
 * 登陆注册界面
 *
 * @author EPQ
 */
public class MainActivity extends AppCompatActivity {

    private EditText username;
    private EditText password;
    private Button logIn;
    private Button logUp;

    private static final String HOST = "http://119.45.248.45:8085";
    private static final String SERVICE_NAME = "/auth";

    private DeviceDataViewModel model;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        model = new ViewModelProvider(new DataStoreOwner()).get(DeviceDataViewModel.class);

        Toolbar toolbar = findViewById(R.id.main_toolbar);
        username = findViewById(R.id.username);
        password = findViewById(R.id.password);
        logIn = findViewById(R.id.log_in);
        logUp = findViewById(R.id.log_up);

        // 设置应用栏
        setSupportActionBar(toolbar);

        // 监听登陆按钮
        logIn.setOnClickListener(v -> {
            if (username.getText() == null && username.getText().toString().trim().length() < 1) {
                Toast.makeText(getApplicationContext(), "账号为空", Toast.LENGTH_SHORT).show();
                return;
            }
            if (password.getText() == null && password.getText().toString().trim().length() < 1) {
                Toast.makeText(getApplicationContext(), "密码为空", Toast.LENGTH_SHORT).show();
                return;
            }

            Thread thread = new Thread(() -> postLogIn(username.getText().toString(), password.getText().toString()));
            thread.start();
        });

        // 监听注册按钮
        logUp.setOnClickListener(v -> {
            if (username.getText() == null && username.getText().toString().trim().length() < 1) {
                Toast.makeText(getApplicationContext(), "账号为空", Toast.LENGTH_SHORT).show();
                return;
            }
            if (password.getText() == null && password.getText().toString().trim().length() < 1) {
                Toast.makeText(getApplicationContext(), "密码为空", Toast.LENGTH_SHORT).show();
                return;
            }

            Thread thread = new Thread(() -> postLogUp(username.getText().toString(), password.getText().toString()));
            thread.start();
        });

    }

    /**
     * post 请求登陆
     *
     * @param name 账号
     * @param pwd  密码
     */
    private synchronized void postLogIn(String name, String pwd) {
        Map<String, String> map = new HashMap<>(2);
        map.put("username", name);
        map.put("password", pwd);

        // 设置数据格式为 json
        MediaType mediaType = MediaType.parse("application/json;charset=utf-8");

        Request request = new Request.Builder().url(HOST + SERVICE_NAME + "/login")
                .post(RequestBody.create(mediaType, new Gson().toJson(map)))
                .build();

        // 发起请求
        try {
            OkHttpClient client = new OkHttpClient.Builder().connectTimeout(20, TimeUnit.SECONDS)
                    .writeTimeout(20, TimeUnit.SECONDS)
                    .readTimeout(20, TimeUnit.SECONDS)
                    .build();
            Response response = client.newCall(request).execute();
            // 获取响应体
            Map body = new Gson().fromJson(response.body().string(), Map.class);
            // 判断响应状态
            if (Double.parseDouble(body.get("code").toString()) == 200d) {
                Object data = body.get("data");
                AuthToken authToken = new Gson().fromJson(new Gson().toJson(data), AuthToken.class);
                System.out.println(authToken.getToken());
                this.runOnUiThread(() -> {
                    model.getAuthorization().setValue(name + ":" + authToken.getToken());
                    Toast.makeText(getApplicationContext(), "登录成功", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent().setClass(getApplicationContext(), ReadTimeDataActivity.class);
                    startActivity(intent);
                });
            } else {
                this.runOnUiThread(() -> {
                    Toast.makeText(getApplicationContext(), "登录失败", Toast.LENGTH_SHORT).show();
                });
            }
        } catch (IOException e) {
            Log.i("LogIn", e.getMessage());
        }
    }

    /**
     * post 请求注册
     *
     * @param name
     * @param pwd
     */
    private synchronized void postLogUp(String name, String pwd) {
        Map<String, String> map = new HashMap<>(2);
        map.put("username", name);
        map.put("password", pwd);

        // 设置请求格式为 json
        MediaType mediaType = MediaType.parse("application/json;charset=utf-8");

        Request request = new Request.Builder().url(HOST + SERVICE_NAME + "/logUp")
                .post(RequestBody.create(mediaType, new Gson().toJson(map)))
                .build();

        // 发起请求
        try {
            OkHttpClient client = new OkHttpClient.Builder().connectTimeout(20, TimeUnit.SECONDS)
                    .writeTimeout(20, TimeUnit.SECONDS)
                    .readTimeout(20, TimeUnit.SECONDS)
                    .build();
            Response response = client.newCall(request).execute();
            // 获取响应体并抛出
            Map body = new Gson().fromJson(response.body().string(), Map.class);
            this.runOnUiThread(() -> Toast.makeText(getApplicationContext(), body.get("msg").toString(), Toast.LENGTH_SHORT).show());
        } catch (IOException e) {
            Log.i("LogIn", e.getMessage());
        }
    }
}