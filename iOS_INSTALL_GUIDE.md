# iOS 安装指南 (Windows + iPhone)

## 📋 准备工作

### 需要准备的内容：
1. **GitHub 账号** - 用于存储代码和触发构建
2. **iPhone 手机** - 用于安装应用
3. **AltStore** - 在 Windows 上安装 iOS 应用的工具
4. **iCloud 账号** - AltStore 需要用于签名

---

## 第一步：将代码上传到 GitHub

### 1.1 创建 GitHub 仓库

1. 打开 [GitHub](https://github.com) 并登录
2. 点击右上角 **+** → **New repository**
3. 填写仓库名称（如 `anxincaiguan`）
4. 选择 **Private**（私有）或 **Public**（公开）
5. 点击 **Create repository**

### 1.2 上传代码到 GitHub

在 Windows 上打开命令提示符（CMD）或 PowerShell：

```bash
# 进入项目目录
cd D:\Users\BAI\AppData\MyApp2

# 初始化 Git 仓库（如果还没有）
git init

# 添加所有文件
git add .

# 提交代码
git commit -m "Initial commit"

# 添加远程仓库（替换 YOUR_USERNAME 为你的 GitHub 用户名）
git remote add origin https://github.com/YOUR_USERNAME/anxincaiguan.git

# 推送到 GitHub
git branch -M main
git push -u origin main
```

### 1.3 验证工作流

1. 打开 GitHub 仓库页面
2. 点击 **Actions** 标签
3. 应该能看到 **Build iOS App** 工作流正在运行
4. 等待构建完成（约 10-15 分钟）
5. 构建完成后，点击工作流，选择 **ios-app** 下载构建产物

---

## 第二步：安装 AltStore

### 2.1 下载 AltStore

1. 打开 [AltStore.io](https://altstore.io/)
2. 下载 **AltServer**（Windows 版本）
3. 下载 **AltStore**（iOS 应用，需要通过 iTunes 手动安装一次）

### 2.2 在 Windows 上安装 AltServer

1. 运行下载的 AltServer 安装程序
2. 按照提示完成安装
3. 安装完成后，AltServer 图标会出现在系统托盘

### 2.3 在 iPhone 上安装 AltStore

1. 使用 USB 线将 iPhone 连接到电脑
2. 在 iPhone 上信任此电脑
3. 打开 iTunes（确保已安装 iTunes for Windows）
4. 在 iPhone 上手动安装 AltStore：
   - 将 AltStore.ipa 文件拖入 iTunes
   - 在 iPhone 的"设置 → 通用 → VPN与设备管理"中信任开发者

### 2.4 连接 AltServer 和 AltStore

1. 确保 iPhone 和电脑在同一 WiFi 网络下
2. 在 Windows 上打开 AltServer
3. 在 iPhone 上打开 AltStore
4. AltStore 应该能自动发现 AltServer

---

## 第三步：安装应用

### 3.1 下载构建产物

1. 打开 GitHub 仓库的 **Actions** 页面
2. 点击最新的构建任务
3. 在 **Artifacts** 部分下载 **ios-app**
4. 解压下载的 zip 文件

### 3.2 安装 IPA

#### 方法一：通过 AltStore 安装（推荐）

1. 确保 AltServer 正在运行
2. 打开 AltStore 应用
3. 点击右上角 **+** 按钮
4. 选择下载的 IPA 文件
5. 选择要安装的应用（如果需要）
6. 等待安装完成

#### 方法二：通过 iTunes 安装

1. 将 IPA 文件重命名为 `.zip`
2. 解压得到 `.app` 文件夹
3. 打开 iTunes
4. 将 iPhone 连接到电脑
5. 在 iTunes 中选择设备
6. 拖动 `.app` 文件夹到设备上

---

## 第四步：信任应用

首次运行时，iOS 会阻止未签名应用：

1. 打开 **设置 → 通用 → VPN与设备管理**
2. 找到你的开发者证书（可能显示为个人开发者）
3. 点击信任

---

## ⚠️ 注意事项

### 证书有效期
- AltStore 使用的证书有效期为 **7 天**
- 证书过期后，需要重新安装应用
- 每次重新安装前，请先在 GitHub 上重新构建（点击 Actions → 重新运行）

### 如何重新构建
1. 打开 GitHub 仓库的 **Actions** 页面
2. 点击左侧 **Build iOS App**
3. 点击 **Run workflow** 按钮
4. 选择 main 分支
5. 等待 10-15 分钟
6. 下载新的构建产物
7. 通过 AltStore 重新安装

### WiFi 要求
- AltServer 和 AltStore 需要在**同一 WiFi 网络**下才能通信
- 如果无法发现设备，请检查防火墙设置

---

## 🔧 故障排除

### AltStore 无法发现设备
1. 确保 Windows 防火墙允许 AltServer
2. 确保设备和电脑在同一网络
3. 尝试重启 AltServer 和 AltStore

### 构建失败
1. 检查 GitHub Actions 日志
2. 确保代码没有语法错误
3. 查看错误信息并修复

### 安装失败
1. 确保 iPhone 已信任电脑
2. 确保有足够的存储空间
3. 尝试重启设备

---

## 📞 获取帮助

如果遇到问题，可以：
1. 查看 GitHub Actions 构建日志
2. 查看 AltStore 官方文档
3. 提交 GitHub Issue