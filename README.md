<div align="center">
  <h1>🚗 Hệ Thống Xe Tự Hành Thông Minh (IoT Smart Car)</h1>
  <p><strong>Nền tảng xe tự hành IoT thế hệ mới, điều khiển trực tiếp qua cảm biến Smartphone</strong></p>

  <!-- Badges -->
  <img src="https://img.shields.io/badge/Hardware-ESP32-blue?style=flat-square&logo=espressif" alt="ESP32" />
  <img src="https://img.shields.io/badge/Platform-Android-green?style=flat-square&logo=android" alt="Android" />
  <img src="https://img.shields.io/badge/Cloud-Firebase_RTDB-FFCA28?style=flat-square&logo=firebase" alt="Firebase" />
  <img src="https://img.shields.io/badge/UI-Jetpack_Compose-4285F4?style=flat-square&logo=android" alt="Jetpack Compose" />
</div>

<br>

## 📋 Tổng Quan Sản Phẩm

**IoT Smart Car** là một hệ sinh thái phần cứng và phần mềm được tích hợp chặt chẽ, nhằm tái định nghĩa cách con người điều khiển phương tiện từ xa. Phá vỡ những giới hạn của sóng vô tuyến (RF) hay Bluetooth truyền thống, sản phẩm tận dụng sức mạnh của Internet và công nghệ Điện toán đám mây (Cloud Computing) để cho phép điều khiển với độ trễ cực thấp từ bất cứ đâu trên thế giới.

Bằng cách trích xuất dữ liệu thô từ cảm biến góc nghiêng (Gravity) bên trong điện thoại di động, hệ thống mang lại một trải nghiệm lái xe cực kỳ chân thực, mô phỏng lại thao tác đánh lái vô lăng. Kết hợp với hệ thống sóng âm trên xe, nó tự động bảo vệ phần cứng thông qua hệ thống phanh khẩn cấp (AEB).

👉 **[Đọc toàn bộ Báo cáo Kỹ thuật & Kiến trúc Hệ thống (LaTeX trên Overleaf)](https://www.overleaf.com/read/tgqvhdqmwytj#915f4e)**

---

## 🌟 Tính Năng Cốt Lõi

### 🎮 Trải nghiệm Tương tác Hiện đại (HCI)
* **Lái xe bằng cảm biến (Gravity Sensor Steering):** Khai thác cảm biến gia tốc và con quay hồi chuyển của điện thoại. Dữ liệu từ trục Roll và Pitch được tính toán thành các vector hướng, cho phép người dùng bẻ lái xe bằng cách nghiêng hoặc lắc điện thoại.
* **Joystick Ảo 360°:** Component UI được vẽ thủ công với phản hồi cảm ứng mượt mà. Hệ thống sử dụng thuật toán tính tọa độ Descartes (định lý Pythagoras) để điều hướng chính xác.
* **Tùy biến Vận tốc Động (Dynamic Speed):** Thanh trượt điều chỉnh tốc độ cho phép can thiệp trực tiếp vào xung PWM (Pulse Width Modulation) từ 0 đến 255 để thay đổi vòng tua động cơ DC.

### ⚡ Hạ Tầng Đám Mây Độ Trễ Cực Thấp
* **Kết nối Real-time Stream:** Vi điều khiển ESP32 duy trì một kết nối TCP liên tục với Google Firebase. Lệnh điều khiển đẩy từ Android App xuống mạch được thực thi với độ trễ (latency) dưới **100ms**.
* **Cơ chế Ping Watchdog:** Vi điều khiển phát ra một tín hiệu "nhịp tim" liên tục. App Android sẽ đếm ngược thời gian ping; nếu rớt gói tin quá 4 giây (xe mất điện/mất mạng), hệ thống lập tức kích hoạt báo động "OFFLINE" đỏ trên màn hình để chống mất kiểm soát.

### 🛡️ Tự Động Hóa & An Toàn (Active Safety)
* **Phanh Khẩn Cấp Tự Động (AEB):** Cảm biến siêu âm HC-SR04 liên tục quét quỹ đạo phía trước. Nếu vật cản vi phạm vùng an toàn (dưới 15cm), phần cứng ESP32 sẽ tự động vô hiệu hóa lệnh tiến tới từ mạng, đồng thời đóng phanh điện tử.
* **Lọc Nhiễu Kỹ Thuật Số (Digital Noise Filtering):** Thuật toán loại bỏ các sóng phản xạ ảo (khoảng cách < 2cm) và áp dụng cơ chế giới hạn thời gian (timeout) để tránh tình trạng treo vi điều khiển (Freezing).
* **Khử "Lệnh Ma" (Stale Command Mitigation):** Ngay khi vừa khởi động, ESP32 sẽ chủ động đẩy lệnh `STOP` lên Firebase nhằm xóa sạch các chỉ thị cũ, ngăn chặn hiện tượng xe tự động rồ ga ngoài ý muốn khi vừa cắm điện.

---

## 🛠️ Công Nghệ Sử Dụng (Tech Stack)

### 📱 Ứng Dụng Di Động (Android Client)
* **Ngôn ngữ:** 100% Kotlin.
* **Kiến trúc:** Tuân thủ chặt chẽ tiêu chuẩn phát triển Android hiện đại (MAD) với mô hình **MVVM** (Model-View-ViewModel).
* **Giao diện (UI Framework):** **Jetpack Compose** (Declarative UI).
* **Quản lý Trạng thái:** Sử dụng `StateFlow` và `Coroutines` để xử lý các tác vụ mạng và đọc cảm biến bất đồng bộ.
* **Theming:** Hỗ trợ Dynamic Material Theme (Giao diện Sáng/Tối chuyển đổi linh hoạt mà không cần reload ứng dụng).

### ⚙️ Phần Cứng (Hardware / Edge Device)
* **Vi điều khiển Trung tâm:** ESP32-WROOM-32 (Dual-core, tích hợp Wi-Fi).
* **Module Điều khiển Động cơ:** L298N Dual H-Bridge.
* **Cảm biến:** HC-SR04 Ultrasonic Distance Sensor.
* **Ngôn ngữ:** C/C++ (Sử dụng lõi Arduino Core cho ESP32).

---

## 📂 Cấu Trúc Mã Nguồn

```text
📦 iot-smart-car
 ┣ 📂 app/                           # Source Code App Android (Kotlin/Compose)
 ┃ ┣ 📂 src/main/java/com/robotcar/app/
 ┃ ┃ ┣ 📂 model/                     # Cấu trúc dữ liệu (Logs, SensorData)
 ┃ ┃ ┣ 📂 ui/screens/                # Giao diện UI (Dashboard, Auth, Stats)
 ┃ ┃ ┣ 📂 viewmodel/                 # Logic nghiệp vụ và đồng bộ Firebase
 ┃ ┃ ┗ 📂 utils/                     # Các hàm tiện ích (Location, Sensors)
 ┃ ┗ 📜 build.gradle.kts
 ┣ 📂 esp32_code/
 ┃ ┗ 📂 RobotCar_ESP32/              # Firmware C++ nhúng trên mạch
 ┃   ┗ 📜 RobotCar_ESP32.ino         # Logic điều khiển vòng lặp chính, PWM, Sonar
 ┗ 📜 README.md
```

## 🚀 Hướng Dẫn Sử Dụng (Getting Started)

1. **Đấu nối Phần cứng:** Nối dây từ ESP32 sang mạch L298N và cảm biến HC-SR04 theo đúng định nghĩa chân (pinouts) trong file `RobotCar_ESP32.ino`.
2. **Cài đặt Firebase:** Tạo dự án Firebase, bật Realtime Database và cấu hình Rules thành `true` để thử nghiệm.
3. **Nạp code ESP32 (Flash):** Đổi thông số `WIFI_SSID`, `WIFI_PASSWORD`, `API_KEY`, và `DATABASE_URL` trong file `.ino` rồi nạp qua phần mềm Arduino IDE.
4. **Biên dịch App Android:** Mở thư mục `app/` bằng Android Studio, đợi đồng bộ Gradle và chạy ứng dụng trên thiết bị Android thật (Cảm biến góc nghiêng sẽ không hoạt động trên máy ảo Emulator).
