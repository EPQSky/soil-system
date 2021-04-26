#include <ESP8266WiFi.h>          //https://github.com/esp8266/Arduino

// 实现网页配置WiFi需要引入的依赖包
#include <DNSServer.h>
#include <ESP8266WebServer.h>
#include <WiFiManager.h>         //https://github.com/tzapu/WiFiManager

// 用于MQTT连接EMQ X Cloud
#include <PubSubClient.h>        //https://github.com/knolleary/pubsubclient

// MQTT Broker
const char *mqtt_broker = "racf312b.cn.emqx.cloud";
const char *topic = "esp8266/control";
const char *mqtt_username = "water";
const char *mqtt_password = "123";
const int mqtt_port = 12672;

#define FIRST_DEVICE 12
#define SECOND_DEVICE 14

// 创建客户端
WiFiClient espClient;
PubSubClient client(espClient);

void setup() {
  pinMode(FIRST_DEVICE, OUTPUT);
  pinMode(SECOND_DEVICE, OUTPUT);
  digitalWrite(FIRST_DEVICE, HIGH);
  digitalWrite(SECOND_DEVICE, HIGH);
  
  // 设置串口波特率
  Serial.begin(115200);

  WiFiManager wifiManager;

  // 配置 ESP8266 热点 SSID 和 PassWord，启动网页配置 WiFi
  wifiManager.autoConnect("ESPwater0", "qwertyuiop");

  // WiFi 配置连接成功，串口打印
  Serial.println("connected...yeey :)");

  // 设置服务端接口
  client.setServer(mqtt_broker, mqtt_port);
  // 设置回调
  client.setCallback(callback);

  while(!client.connected()){
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
  client.subscribe(topic);
}

// 接收消息，控制水启动
void callback(char *topic, byte *payload, unsigned int length) {

  String msg = "";

  for (int i = 0; i < length; i++) {
    msg += (char) payload[i];
  }

  if (msg == "1:10") {
    digitalWrite(FIRST_DEVICE, HIGH);
  } else if (msg == "1:11") {
    digitalWrite(FIRST_DEVICE, LOW);
  } else if (msg == "2:20") {
    digitalWrite(SECOND_DEVICE, HIGH);
  } else if (msg == "2:21") {
    digitalWrite(SECOND_DEVICE, LOW);
  }
  
}


// the loop function runs over and over again forever
void loop() {
  client.loop();
}
