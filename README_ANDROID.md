# GPS Mock Location - Android 项目

## 项目结构
```
android-app/
├── app/
│   ├── src/main/
│   │   ├── java/com/gpsmocklocation/
│   │   │   ├── MainActivity.kt
│   │   │   └── MockLocationService.kt
│   │   ├── res/
│   │   │   ├── layout/
│   │   │   └── values/
│   │   └── AndroidManifest.xml
│   └── build.gradle
├── build.gradle
├── settings.gradle
└── gradle.properties
```

## 使用步骤

### 1. 安装 Android Studio
- 下载: https://developer.android.com/studio
- 安装并配置 Android SDK

### 2. 打开项目
1. 打开 Android Studio
2. 选择 "Open an Existing Project"
3. 选择 `d:\xyp78\android-app` 文件夹
4. 等待 Gradle 同步完成

### 3. 配置开发者选项
1. 在手机上打开设置 → 关于手机
2. 连续点击"版本号"7次开启开发者选项
3. 进入设置 → 系统 → 开发者选项
4. 开启"USB 调试"
5. 找到"选择模拟位置信息应用"
6. 选择 "GPS Mock Location"

### 4. 构建 APK
1. 在 Android Studio 中，选择 Build → Build Bundle(s) / APK(s) → Build APK(s)
2. 等待构建完成
3. 点击通知中的 "locate" 找到 APK 文件
4. APK 位置: `android-app/app/build/outputs/apk/debug/app-debug.apk`

### 5. 安装 APK
1. 将 APK 复制到手机
2. 在手机上安装 APK
3. 安装时可能需要允许"未知来源"

### 6. 使用应用
1. 打开 "GPS Mock Location" 应用
2. 点击 "启动服务"
3. 在电脑端的桌面应用中输入手机 IP 地址
4. 开始模拟运动

## 桌面端连接
1. 确保手机和电脑在同一 WiFi 网络
2. 在手机设置 → 关于手机 → 状态信息中查看 IP 地址
3. 在桌面应用中输入: `手机IP:8080`
4. 点击"测试连接"
5. 开始运动后，位置会实时发送到手机

## 注意事项
- 必须在开发者选项中设置模拟位置应用
- 部分应用可能检测不到模拟位置
- 服务运行时会显示通知栏通知
- 默认端口是 8080

## 故障排除
- 如果连接失败，检查防火墙设置
- 确保手机和电脑在同一局域网
- 尝试关闭 VPN
