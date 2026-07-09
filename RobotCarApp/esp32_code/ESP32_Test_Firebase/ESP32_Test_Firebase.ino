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

#define API_KEY "AIzaSyCyUiq9jg91xBoTeiwQ8Rwb9lQhdl0O9v4"
#define DATABASE_URL "iot-smart-caa20-default-rtdb.asia-southeast1.firebasedatabase.app" 

FirebaseData fbdo;
FirebaseAuth auth;
FirebaseConfig config;

bool signupOK = false;
unsigned long sendDataPrevMillis = 0;
int count = 0;

void setup() {
  Serial.begin(115200);
  delay(1000);
  Serial.println("\n\n--- BẮT ĐẦU CHƯƠNG TRÌNH TEST ESP32 ĐỘC LẬP ---");
  
  // 1. KẾT NỐI WIFI
  Serial.print("Đang kết nối WiFi: ");
  Serial.println(WIFI_SSID);
  
  WiFi.begin(WIFI_SSID, WIFI_PASSWORD);
  int attempts = 0;
  while (WiFi.status() != WL_CONNECTED && attempts < 20) {
    delay(500);
    Serial.print(".");
    attempts++;
  }
  
  if (WiFi.status() != WL_CONNECTED) {
    Serial.println("\nLỖI: Không thể kết nối WiFi. Hãy kiểm tra lại Tên/Mật khẩu WiFi hoặc xem Anten ESP32 có bị lỗi không.");
    return;
  }
  Serial.println("\nWiFi OK! IP: " + WiFi.localIP().toString());

  // 2. KẾT NỐI FIREBASE
  Serial.println("Đang cấu hình Firebase...");
  config.api_key = API_KEY;
  config.database_url = DATABASE_URL;
  
  // Đăng nhập ẩn danh
  if (Firebase.signUp(&config, &auth, "", "")) {
    Serial.println("-> Firebase: Đăng nhập Ẩn danh thành công!");
    signupOK = true;
  } else {
    Serial.printf("-> LỖI Firebase Auth: %s\n", config.signer.signupError.message.c_str());
  }

  config.token_status_callback = tokenStatusCallback;
  Firebase.begin(&config, &auth);
  Firebase.reconnectWiFi(true);
  
  Serial.println("Đã cài đặt xong Setup. Chuyển sang Vòng lặp chính...");
}

void loop() {
  if (Firebase.ready() && signupOK) {
    // Đẩy một con số đếm lên Firebase mỗi 5 giây để test xem có ghi dữ liệu được không
    if (millis() - sendDataPrevMillis > 5000 || sendDataPrevMillis == 0) {
      sendDataPrevMillis = millis();
      count++;
      
      Serial.print("Đang ghi dữ liệu Test (Count = " + String(count) + ") lên Firebase... ");
      
      if (Firebase.RTDB.setInt(&fbdo, "/test/count", count)) {
        Serial.println("THÀNH CÔNG!");
      } else {
        Serial.println("THẤT BẠI: " + fbdo.errorReason());
      }
    }
  }
}
