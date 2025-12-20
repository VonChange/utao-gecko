#!/bin/bash
# Gecko Release 版本编译部署脚本
# 功能：编译 release 版本并部署到 Android 设备

set -e

# 脚本所在目录
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

echo "====================================="
echo "     Gecko Release 编译部署脚本"
echo "====================================="

# 清理并编译 Release 版本
echo ""
echo "[1/4] 清理旧的构建文件..."
./gradlew clean

echo ""
echo "[2/4] 编译 Release 版本..."
./gradlew assembleRelease

# 查找生成的 APK 文件
APK_DIR="$SCRIPT_DIR/app/build/outputs/apk/release"

echo ""
echo "[3/4] APK 编译成功！"
echo "📦 APK 目录: $APK_DIR"
echo ""
echo "生成的 APK 文件："
ls -lh "$APK_DIR"/*.apk 2>/dev/null || echo "未找到 APK 文件"

# 检查 ADB 连接
echo ""
echo "[4/4] 部署到设备..."

# 检查是否有设备连接
DEVICE_COUNT=$(adb devices | grep -v "List" | grep -v "^$" | wc -l)

if [ "$DEVICE_COUNT" -eq 0 ]; then
    echo "⚠️  警告：未检测到 Android 设备"
    echo "请连接设备后手动执行安装"
    exit 0
fi

echo "🔌 检测到 $DEVICE_COUNT 个设备"
adb devices

# 选择要安装的 APK（优先 arm64-v8a）
APK_FILE=$(find "$APK_DIR" -name "*v8a*.apk" -type f 2>/dev/null | head -n 1)
if [ -z "$APK_FILE" ]; then
    APK_FILE=$(find "$APK_DIR" -name "*v7a*.apk" -type f 2>/dev/null | head -n 1)
fi
if [ -z "$APK_FILE" ]; then
    APK_FILE=$(find "$APK_DIR" -name "*.apk" -type f 2>/dev/null | head -n 1)
fi

if [ -z "$APK_FILE" ]; then
    echo "❌ 错误：未找到 APK 文件"
    exit 1
fi

echo ""
echo "📲 正在安装: $(basename "$APK_FILE")"
adb install -r "$APK_FILE"

echo ""
echo "====================================="
echo "✅ 编译部署完成！"
echo "====================================="

# 启动应用（可选）
read -p "是否启动应用？(y/n) " -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]; then
    echo "🚀 启动应用..."
    adb shell am start -n com.vonchange.utao.gecko/.StartActivity
    echo "✅ 应用已启动"
fi

