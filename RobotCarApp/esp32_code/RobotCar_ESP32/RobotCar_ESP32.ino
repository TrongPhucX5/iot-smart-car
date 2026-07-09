#include <WiFi.h>
#include <Firebase_ESP_Client.h>

// Thư viện hỗ trợ Firebase
#include "addons/TokenHelper.h"
#include "addons/RTDBHelper.h"

// ==========================================
// 1. CẤU HÌNH MẠNG & FIREBASE
// ==========================================
#define WIFI_SSID "Trong Phuc"
#define WIFI_PASSWORD "25042005"

// Thay bằng Web API Key của dự án Firebase của bạn (Lấy trong Project Settings)
#define API_KEY "AIzaSyCyUiq9jg91xBoTeiwQ8Rwb9lQhdl0O9v4"
// Link Database bạn đã cấp
#define DATABASE_URL "iot-smart-caa20-default-rtdb.asia-southeast1.firebasedatabase.app" 

FirebaseData fbdo;       // Dùng để Gửi dữ liệu (Cảm biến)
FirebaseData fbStream;   // Dùng để Nhận dữ liệu (Đọc lệnh)
FirebaseAuth auth;
FirebaseConfig config;

bool signupOK = false;
unsigned long sendDataPrevMillis = 0;
String commandPath = "/vehicles/1/command";
String sensorPath = "/vehicles/1/sensors";
String speedPath = "/vehicles/1/speed"; // Nhánh lưu tốc độ (0-255)
String logsPath = "/vehicles/1/logs"; // Nhánh lưu lịch sử
int currentSpeed = 150; // Tốc độ mặc định
unsigned long lastWarningMillis = 0; // Chống spam cảnh báo
bool startupLogged = false; // Biến kiểm tra xem đã ghi log khởi động chưa
bool streamReady = false;   // Biến kiểm tra stream đã chạy chưa

// ==========================================
// 2. KHAI BÁO CHÂN MOTOR VÀ CẢM BIẾN
// ==========================================
const int ENA = 14; 
const int IN1 = 27; // MOTOR PHẢI
const int IN2 = 26; // MOTOR PHẢI
const int IN3 = 25; // MOTOR TRÁI
const int IN4 = 33; // MOTOR TRÁI
const int ENB = 32;

#define TRIG_PIN 5
#define ECHO_PIN 18

// ==========================================
// HÀM KHỞI TẠO (SETUP)
// ==========================================
void setup() {
  Serial.begin(115200);
  
  // Setup Motor
  pinMode(ENA, OUTPUT);
  pinMode(IN1, OUTPUT);
  pinMode(IN2, OUTPUT);
  pinMode(IN3, OUTPUT);
  pinMode(IN4, OUTPUT);
  pinMode(ENB, OUTPUT);
  stopCar(); // Dừng xe lúc mới bật nguồn

  // Setup Cảm biến siêu âm
  pinMode(TRIG_PIN, OUTPUT);
  pinMode(ECHO_PIN, INPUT);

  // Kết nối WiFi
  Serial.print("Đang kết nối WiFi");
  WiFi.begin(WIFI_SSID, WIFI_PASSWORD);
  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }
  Serial.println("\nWiFi OK! IP: " + WiFi.localIP().toString());

  // Kết nối Firebase
  config.api_key = API_KEY;
  config.database_url = DATABASE_URL;
  if (Firebase.signUp(&config, &auth, "", "")) {
    Serial.println("Đăng nhập Firebase Ẩn danh thành công");
    signupOK = true;
  } else {
    Serial.printf("%s\n", config.signer.signupError.message.c_str());
  }

  config.token_status_callback = tokenStatusCallback;
  Firebase.begin(&config, &auth);
  Firebase.reconnectWiFi(true);
}

// ==========================================
// VÒNG LẶP CHÍNH (LOOP)
// ==========================================
void loop() {
  if (Firebase.ready() && signupOK) {
    
    // Ghi log khởi động 1 lần duy nhất khi Firebase đã sẵn sàng
    if (!startupLogged) {
      logEvent("INFO", "Xe vừa được bật nguồn và hòa mạng thành công.");
      startupLogged = true;
    }

    // Khởi tạo Stream 1 lần duy nhất sau khi Firebase kết nối xong
    if (!streamReady) {
      // Xoá sạch lệnh cũ tồn đọng trên Firebase để tránh hiện tượng xe tự chạy khi vừa bật nguồn
      Firebase.RTDB.setString(&fbdo, commandPath, "STOP");
      
      if (Firebase.RTDB.beginStream(&fbStream, commandPath)) {
        Serial.println("Đã bắt đầu nghe lệnh từ điện thoại!");
        streamReady = true;
      } else {
        Serial.printf("Lỗi Stream: %s\n", fbStream.errorReason().c_str());
      }
    }

    // 1. NHẬN LỆNH TỪ APP NGAY LẬP TỨC (STREAM)
    if (streamReady) {
      if (!Firebase.RTDB.readStream(&fbStream)) {
        Serial.printf("Lỗi đọc Stream: %s\n", fbStream.errorReason().c_str());
      }
      if (fbStream.streamTimeout()) {
        Serial.println("Stream bị timeout, đang kết nối lại...");
      }
      if (fbStream.streamAvailable()) {
        if (fbStream.dataType() == "string") {
          String command = fbStream.stringData();
          Serial.println("Nhận lệnh: " + command);
          executeCommand(command);
        }
      }
    }

    // 2. GỬI DỮ LIỆU CẢM BIẾN LÊN APP (Mỗi 1 giây gửi 1 lần)
    if (millis() - sendDataPrevMillis > 1000 || sendDataPrevMillis == 0) {
      sendDataPrevMillis = millis();
      float distance = readUltrasonicDistance();
      
      // Nếu vật cản quá gần (lớn hơn 2cm để loại bỏ tín hiệu nhiễu)
      if (distance > 2.0 && distance < 15.0) {
        if (millis() - lastWarningMillis > 5000 || lastWarningMillis == 0) {
            lastWarningMillis = millis();
            logEvent("WARNING", "Phát hiện vật cản ở khoảng cách " + String(distance, 1) + " cm!");
        }
      }

      // Đẩy khoảng cách siêu âm lên Firebase
      Firebase.RTDB.setFloat(&fbdo, sensorPath + "/obstacle_distance", distance);
      // Gửi tín hiệu Ping liên tục để App duy trì trạng thái kết nối
      Firebase.RTDB.setInt(&fbdo, sensorPath + "/last_ping", millis());
      
      // Đọc tốc độ từ App (0-255)
      if (Firebase.RTDB.getInt(&fbdo, speedPath)) {
        currentSpeed = fbdo.intData();
      }
    }
  }
}

// ==========================================
// CÁC HÀM ĐIỀU KHIỂN MOTOR (Của Bạn)
// ==========================================
void executeCommand(String cmd) {
  if (cmd == "FORWARD") forward();
  else if (cmd == "BACKWARD") backward();
  else if (cmd == "LEFT") turnLeft();
  else if (cmd == "RIGHT") turnRight();
  else if (cmd == "SPIN_LEFT") turnLeft(); // Hoặc viết hàm xoay tại chỗ riêng
  else if (cmd == "SPIN_RIGHT") turnRight(); // Hoặc viết hàm xoay tại chỗ riêng
  else if (cmd == "STOP") stopCar();
}

void forward() {
  analogWrite(ENA, currentSpeed); 
  analogWrite(ENB, currentSpeed); 
  digitalWrite(IN1, LOW);
  digitalWrite(IN2, HIGH);
  digitalWrite(IN3, HIGH);
  digitalWrite(IN4, LOW);
}

void backward() {
  analogWrite(ENA, currentSpeed);
  analogWrite(ENB, currentSpeed);
  digitalWrite(IN1, HIGH);
  digitalWrite(IN2, LOW);
  digitalWrite(IN3, LOW);
  digitalWrite(IN4, HIGH);
}

void turnLeft() {
  analogWrite(ENA, currentSpeed);
  analogWrite(ENB, currentSpeed);
  digitalWrite(IN1, LOW);
  digitalWrite(IN2, HIGH); 
  digitalWrite(IN3, LOW);
  digitalWrite(IN4, HIGH); 
}

void turnRight() {
  analogWrite(ENA, currentSpeed);
  analogWrite(ENB, currentSpeed);
  digitalWrite(IN1, HIGH);
  digitalWrite(IN2, LOW); 
  digitalWrite(IN3, HIGH);
  digitalWrite(IN4, LOW); 
}

void stopCar() {
  digitalWrite(ENA, LOW);
  digitalWrite(ENB, LOW);
  digitalWrite(IN1, LOW);
  digitalWrite(IN2, LOW);
  digitalWrite(IN3, LOW);
  digitalWrite(IN4, LOW);
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
  
  // Thêm timeout 30000 microsecond (30ms) để tránh treo vi điều khiển
  long duration = pulseIn(ECHO_PIN, HIGH, 30000);
  float distanceCm = duration * 0.034 / 2;
  return distanceCm;
}

// ==========================================
// HÀM GHI NHẬT KÝ VÀO FIREBASE
// ==========================================
void logEvent(String type, String message) {
  FirebaseJson json;
  json.set("type", type);
  json.set("message", message);
  // Dùng .sv: timestamp để nhờ Server Google tự điền giờ
  json.set("timestamp/.sv", "timestamp");
  
  if (Firebase.RTDB.pushJSON(&fbdo, logsPath, &json)) {
    Serial.println("Đã ghi log: " + message);
  } else {
    Serial.println("Lỗi ghi log: " + fbdo.errorReason());
  }
}

