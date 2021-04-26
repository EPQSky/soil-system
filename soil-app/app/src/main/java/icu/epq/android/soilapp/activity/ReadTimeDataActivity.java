package icu.epq.android.soilapp.activity;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NotificationCompat;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.navigation.NavigationView;
import com.google.gson.Gson;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;

import icu.epq.android.soilapp.MainActivity;
import icu.epq.android.soilapp.R;
import icu.epq.android.soilapp.dto.Soil;
import icu.epq.android.soilapp.fragment.ReadTimeDataDeviceOneFragment;
import icu.epq.android.soilapp.fragment.ReadTimeDataDeviceTwoFragment;
import icu.epq.android.soilapp.model.DataStoreOwner;
import icu.epq.android.soilapp.model.DeviceDataViewModel;

/**
 * 实时监测数据 Activity
 *
 * @author EPQ
 */
public class ReadTimeDataActivity extends AppCompatActivity {

    private final String TAG = "EMQ X Cloud Mqtt";
    /**
     * Mqtt 连接参数
     * <p>
     * HOST : EMQ X Cloud 域名
     * CLIENT_ID : 客户端 ID
     * USERNAME : 用户名
     * PASSWORD : 密码
     * SUB_TOPIC : 订阅主题
     * PUB_TOPIC : 发布主题
     */
    private final String HOST = "tcp://racf312b.cn.emqx.cloud:12672";
    private final String CLIENT_ID = "soil_info_app";
    private final String USERNAME = "app";
    private final String PASSWORD = "123";
    private final String SUB_TOPIC = "esp8266/soil";
    private final String PUB_TOPIC = "esp8266/control";

    private MqttAndroidClient mqttAndroidClient;

    private DeviceDataViewModel model;

    private NavigationView navigationView;
    private Button publishBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read_time_data);

        // 创建通知渠道
        this.createNotificationChannel();

        model = new ViewModelProvider(new DataStoreOwner()).get(DeviceDataViewModel.class);

        // mqtt 连接
        mqttConnect();

        publishBtn = findViewById(R.id.publish_btn);
        navigationView = findViewById(R.id.real_time_data_menus);
        Toolbar toolbar = findViewById(R.id.real_time_data_toolbar);

        // fragment 加载
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment, ReadTimeDataDeviceOneFragment.class, null)
                .commit();

        // 设置侧边菜单栏不可见
        navigationView.setVisibility(View.INVISIBLE);
        // 监听侧边菜单并跳转
        navigationView.setNavigationItemSelectedListener(item -> {
            Intent intent = new Intent();
            switch (item.getItemId()) {
                case R.id.history_data_view_menu_item:
                    intent = new Intent().setClass(getApplication(), HistoryDataViewActivity.class);
                    startActivity(intent);
                    return true;
                case R.id.data_statistic_analysis_menu_item:
                    intent = new Intent().setClass(getApplication(), DataStatisticAnalysisActivity.class);
                    startActivity(intent);
                    return true;
                case R.id.exit_menu_item:
                    intent = new Intent().setClass(getApplication(), MainActivity.class);
                    startActivity(intent);
                    return true;
                default:
                    return false;
            }
        });

        // 设置应用栏
        toolbar.inflateMenu(R.menu.read_time_data_menu);
        // 监听并响应
        toolbar.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.device_1:
                    model.getDeviceName().setValue(1);
                    Integer firstValue = model.getFirstStatus().getValue();
                    if (firstValue == 10) {
                        publishBtn.setText("开启浇水");
                    } else if (firstValue == 11) {
                        publishBtn.setText("关闭浇水");
                    }
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment, ReadTimeDataDeviceOneFragment.class, null)
                            .commit();
                    return true;
                case R.id.device_2:
                    model.getDeviceName().setValue(2);
                    Integer secondValue = model.getSecondStatus().getValue();
                    if (secondValue == 20) {
                        publishBtn.setText("开启浇水");
                    } else if (secondValue == 21) {
                        publishBtn.setText("关闭浇水");
                    }
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment, ReadTimeDataDeviceTwoFragment.class, null)
                            .commit();
                    return true;
                case R.id.real_time_data_menus_item:
                    if (navigationView.getVisibility() == View.VISIBLE) {
                        navigationView.setVisibility(View.INVISIBLE);
                    } else {
                        navigationView.setVisibility(View.VISIBLE);
                    }
                    return true;
                default:
                    return false;
            }
        });

        // 监听控制按钮
        publishBtn.setOnClickListener(v -> {
            Integer deviceValue = model.getDeviceName().getValue();
            Integer firstValue = model.getFirstStatus().getValue();
            Integer secondValue = model.getSecondStatus().getValue();

            if (deviceValue == 1) {
                if (firstValue == 10) {
                    publishMessage(deviceValue + ":11");
                    model.getFirstStatus().setValue(11);
                    publishBtn.setText("关闭浇水");
                    Toast.makeText(this, "正在开启浇水中...", Toast.LENGTH_SHORT).show();
                } else if (firstValue == 11) {
                    publishMessage(deviceValue + ":10");
                    model.getFirstStatus().setValue(10);
                    publishBtn.setText("开启浇水");
                    Toast.makeText(this, "正在关闭浇水中...", Toast.LENGTH_SHORT).show();
                }
            } else if (deviceValue == 2) {
                if (secondValue == 20) {
                    publishMessage(deviceValue + ":21");
                    model.getSecondStatus().setValue(21);
                    publishBtn.setText("关闭浇水");
                    Toast.makeText(this, "正在开启浇水中...", Toast.LENGTH_SHORT).show();
                } else if (secondValue == 21) {
                    publishMessage(deviceValue + ":20");
                    model.getSecondStatus().setValue(20);
                    publishBtn.setText("开启浇水");
                    Toast.makeText(this, "正在关闭浇水中...", Toast.LENGTH_SHORT).show();
                }
            }

        });
    }

    /**
     * Mqtt 服务 配置连接
     */
    private void mqttConnect() {
        /* 创建 MqttConnectOptions 对象并配置 username 和 password */
        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setAutomaticReconnect(true);
        mqttConnectOptions.setUserName(USERNAME);
        mqttConnectOptions.setPassword(PASSWORD.toCharArray());

        /* 创建MqttAndroidClient对象, 并设置回调接口 */
        mqttAndroidClient = new MqttAndroidClient(getApplicationContext(), HOST, CLIENT_ID);
        mqttAndroidClient.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {
                Log.i(TAG, "连接丢失");
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                Log.i(TAG, "topic: " + topic + ", msg: " + new String(message.getPayload()));
                String msg = new String(message.getPayload(), StandardCharsets.UTF_8);
                Soil soil = new Gson().fromJson(msg, Soil.class);
                if (soil != null) {
                    int id = Integer.parseInt(soil.getAddr16().split(":")[0]);

                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    // 数据检查并做出符合警告的通知
                    NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                    if (soil.getTemp() > 45f) {
                        notificationManager.notify(1, getNotification("temp", "土壤温度警告 - " + simpleDateFormat.format(new Date(System.currentTimeMillis())), "设备" + id + "：当前温度" + soil.getTemp() + "°C（过高）"));
                    } else if (soil.getTemp() < 15f) {
                        notificationManager.notify(1, getNotification("temp", "土壤温度警告 - " + simpleDateFormat.format(new Date(System.currentTimeMillis())), "设备" + id + "：当前温度" + soil.getTemp() + "°C（过低）"));
                    }
                    if (soil.getHumidity() > 50f) {
                        notificationManager.notify(2, getNotification("humidity", "土壤湿度警告 - " + simpleDateFormat.format(new Date(System.currentTimeMillis())), "设备" + id + "：当前湿度" + soil.getHumidity() + "%（过高）"));
                    } else if (soil.getHumidity() < 20f) {
                        notificationManager.notify(2, getNotification("humidity", "土壤湿度警告 - " + simpleDateFormat.format(new Date(System.currentTimeMillis())), "设备" + id + "：当前湿度" + soil.getHumidity() + "%（过低）"));
                    }
                    if (soil.getEc() > 3000f) {
                        notificationManager.notify(3, getNotification("humidity", "土壤盐度警告 - " + simpleDateFormat.format(new Date(System.currentTimeMillis())), "设备" + id + "：当前盐度" + soil.getEc() / 1000f + "ms/cm（过高）"));
                    } else if (soil.getEc() < 100f) {
                        notificationManager.notify(3, getNotification("humidity", "土壤盐度警告 - " + simpleDateFormat.format(new Date(System.currentTimeMillis())), "设备" + id + "：当前盐度" + soil.getEc() / 1000f + "ms/cm（过低）"));
                    }

                    // 根据设备选择存放数据
                    if (id == 1) {
                        model.getDeviceOneData().setValue(soil);
                    } else if (id == 2) {
                        model.getDeviceTwoData().setValue(soil);
                    }
                }
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                Log.i(TAG, "消息传递成功");
            }
        });

        /* Mqtt 建立连接 */
        try {
            mqttAndroidClient.connect(mqttConnectOptions, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.i(TAG, "连接成功");

                    subscribeTopic(SUB_TOPIC);
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.i(TAG, "连接失败");
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    /**
     * 主题订阅
     *
     * @param topic
     */
    private void subscribeTopic(String topic) {
        try {
            mqttAndroidClient.subscribe(topic, 0, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.i(TAG, "订阅成功");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.i(TAG, "订阅失败");
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    /**
     * 向默认的主题/user/update发布消息
     *
     * @param payload 消息载荷
     */
    public void publishMessage(String payload) {
        try {
            if (!mqttAndroidClient.isConnected()) {
                mqttAndroidClient.connect();
            }

            MqttMessage message = new MqttMessage();
            message.setPayload(payload.getBytes());
            message.setQos(0);
            mqttAndroidClient.publish(PUB_TOPIC, message, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.i(TAG, "发布成功");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.i(TAG, "发布失败");
                }
            });
        } catch (MqttException e) {
            Log.e(TAG, e.toString());
            e.printStackTrace();
        }
    }

    /**
     * 创建通知渠道
     */
    private void createNotificationChannel() {
        // 安卓版本要求 26+ 才能创建渠道
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel temp = new NotificationChannel("temp", name, importance);
            NotificationChannel humidity = new NotificationChannel("humidity", name, importance);
            NotificationChannel ec = new NotificationChannel("ec", name, importance);
            temp.setDescription(description);
            humidity.setDescription(description);
            ec.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(temp);
            notificationManager.createNotificationChannel(humidity);
            notificationManager.createNotificationChannel(ec);
        }
    }

    /**
     * 创建通知
     *
     * @param channelId
     * @param title
     * @param content
     * @return
     */
    private Notification getNotification(String channelId, String title, String content) {
        return new NotificationCompat.Builder(ReadTimeDataActivity.this, channelId)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(title)
                .setContentText(content)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .build();
    }

}