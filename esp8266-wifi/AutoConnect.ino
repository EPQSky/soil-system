#include <ESP8266WiFi.h>          //https://github.com/esp8266/Arduino

// 实现网页配置WiFi需要引入的依赖包
#include <DNSServer.h>
#include <ESP8266WebServer.h>
#include <WiFiManager.h>         //https://github.com/tzapu/WiFiManager

// 用于MQTT连接EMQ X Cloud
#include <PubSubClient.h>        //https://github.com/knolleary/pubsubclient

// MQTT Broker
const char *mqtt_broker = "racf312b.cn.emqx.cloud";
const char *topic = "esp8266/soil";
const char *mqtt_username = "esp8266";
const char *mqtt_password = "123";
const int mqtt_port = 12672;

// 获取串口字节流数据
String msg = "";

// 创建客户端
WiFiClient espClient;
PubSubClient client(espClient);

// ESP8266 配置
void setup() {
  // 设置串口波特率
  Serial.begin(115200);

  WiFiManager wifiManager;

  // 配置 ESP8266 热点 SSID 和 PassWord，启动网页配置 WiFi
  wifiManager.autoConnect("ESP204a1", "qwertyuiop");

  // WiFi 配置连接成功，串口打印
  Serial.println("connected...yeey :)");

  // 设置服务端接口
  client.setServer(mqtt_broker, mqtt_port);
}

// ESP8266 配置成功后，进入循环
void loop() {
  delay(3000);
  // 自动重连 MQTT 服务端
  while (!client.connected()) {
    String client_id = "esp8266-client-";
    // 设置客户端ID为 ESP8266 的 MAC 地址
    client_id += String(WiFi.macAddress());
    Serial.println("Connecting to public emqx mqtt broker.....");
    if (client.connect(client_id.c_str(), mqtt_username, mqtt_password)) {
      // MQTT连接完成，串口打印
      Serial.println("Public emqx mqtt broker connected");
    } else {
      // MQTT连接失败，打印错误信息
      Serial.print("failed with state ");
      Serial.println(client.state());
      delay(2000);
    }
  }
  // 客户端定时刷新状态
  client.loop();
  // 循环接收串口数据
  while (Serial.available()) {
    while (Serial.available() > 0) {
      msg += char(Serial.read());
    }
    // 数据发布到服务器
    client.publish(topic, msg.c_str());
    Serial.println(msg);
    msg = "";
  }
}
