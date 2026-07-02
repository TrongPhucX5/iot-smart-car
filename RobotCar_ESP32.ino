#include <WiFi.h>
#include <HTTPClient.h>
#include <ArduinoJson.h>

// ==========================================
// 1. CẤU HÌNH MẠNG & SERVER
// ==========================================
const char* ssid = "Trong Phuc";
const char* password = "25042005";

// ĐỊA CHỈ IP MÁY TÍNH: Thay 192.168.1.X bằng IPv4 của máy tính đang chạy Spring Boot
const char* serverUrl = "http://123.31.203.247.X:8080/api/iot/sensor";

// TOKEN BẢO MẬT: Dán chuỗi JWT lấy từ Postman vào đây
const char* jwtToken = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbnJvYm90MiIsImlhdCI6MTc4MTY5NDg4MiwiZXhwIjoxNzgxNzgxMjgyfQ._R2lx7WUmiFLgcD4hCgSUHhb1Ub7UehX_4_6f6EOJj8";

// ID của xe trong Database Azure
const int vehicleId = 1; 

// ==========================================
// 2. KHAI BÁO CHÂN CẢM BIẾN (Ví dụ)
// ==========================================
#define TRIG_PIN 5
#define ECHO_PIN 18
#define LIGHT_SENSOR_PIN 34 // Chân Analog đọc quang trở
#define LINE_SENSOR_LEFT 12
#define LINE_SENSOR_RIGHT 14

void setup() {
  Serial.begin(115200);
  
  // Setup các chân cảm biến
  pinMode(TRIG_PIN, OUTPUT);
  pinMode(ECHO_PIN, INPUT);
  pinMode(LIGHT_SENSOR_PIN, INPUT);
  pinMode(LINE_SENSOR_LEFT, INPUT);
  pinMode(LINE_SENSOR_RIGHT, INPUT);

  // Kết nối WiFi
  Serial.print("Đang kết nối WiFi");
  WiFi.begin(ssid, password);
  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }
  Serial.println("\nKết nối WiFi thành công! IP: " + WiFi.localIP().toString());
}

void loop() {
  if (WiFi.status() == WL_CONNECTED) {
    // 1. Đọc dữ liệu từ các cảm biến thực tế
    float distance = readUltrasonicDistance();
    float lightLevel = analogRead(LIGHT_SENSOR_PIN);
    String lineStatus = readLineStatus();

    // 2. Đóng gói thành JSON
    StaticJsonDocument<200> jsonDoc;
    jsonDoc["vehicleId"] = vehicleId;
    jsonDoc["distance"] = distance;
    jsonDoc["lightLevel"] = lightLevel;
    jsonDoc["lineStatus"] = lineStatus;

    String requestBody;
    serializeJson(jsonDoc, requestBody);

    // 3. Gửi HTTP POST lên Spring Boot
    HTTPClient http;
    http.begin(serverUrl);
    
    // Thêm Headers chuẩn bị vượt qua Spring Security
    http.addHeader("Content-Type", "application/json");
    http.addHeader("Authorization", String("Bearer ") + jwtToken);

    // Bắn gói tin lên
    int httpResponseCode = http.POST(requestBody);

    if (httpResponseCode > 0) {
      Serial.print("Gửi thành công! Mã phản hồi: ");
      Serial.println(httpResponseCode);
    } else {
      Serial.print("Lỗi gửi dữ liệu HTTP: ");
      Serial.println(httpResponseCode);
    }
    
    http.end();
  }

  // Chờ 1 giây trước khi gửi lần tiếp theo để tránh bão Request lên server
  delay(1000); 
}

// ==========================================
// CÁC HÀM XỬ LÝ CẢM BIẾN
// ==========================================

float readUltrasonicDistance() {
  digitalWrite(TRIG_PIN, LOW);
  delayMicroseconds(2);
  digitalWrite(TRIG_PIN, HIGH);
  delayMicroseconds(10);
  digitalWrite(TRIG_PIN, LOW);
  
  long duration = pulseIn(ECHO_PIN, HIGH);
  float distanceCm = duration * 0.034 / 2;
  return distanceCm;
}

String readLineStatus() {
  int left = digitalRead(LINE_SENSOR_LEFT);
  int right = digitalRead(LINE_SENSOR_RIGHT);
  
  // Logic giả định (tùy vào module cảm biến line của bạn trả về LOW hay HIGH khi gặp vạch đen)
  if (left == HIGH && right == HIGH) return "CENTER";
  if (left == LOW && right == HIGH) return "LEFT";
  if (left == HIGH && right == LOW) return "RIGHT";
  return "LOST";
}
