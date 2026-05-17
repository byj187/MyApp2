# 在 iPhone 上安装应用指南（无需 Mac）

本指南将帮助你在没有 Mac 电脑的情况下，在 iPhone 6s 上安装这个应用。

## 前置要求

1. **iPhone 6s**（系统版本 iOS 12-15.8.3）
2. **GitHub 账号**（免费）
3. **有效的 Apple ID**
4. **Git 客户端**（如 GitHub Desktop 或 SourceTree）

## 方法一：使用 GitHub Actions（推荐，免费）

### 第一步：将代码推送到 GitHub

#### 1.1 创建 GitHub 仓库

1. 访问 [GitHub](https://github.com) 并登录
2. 点击右上角的 **"+"** 按钮，选择 **"New repository"**
3. 填写仓库信息：
   - Repository name: `MyApp2` 或你喜欢的名字
   - Description: 可选
   - 选择 **Public**（免费账户需要公开仓库才能使用 GitHub Actions）
   - **不要**勾选 "Add a README file"
   - **不要**勾选 "Add .gitignore"
4. 点击 **"Create repository"**

#### 1.2 初始化本地仓库并推送

打开终端（PowerShell 或命令提示符），执行以下命令：

```bash
# 进入项目目录
cd d:\Users\BAI\AppData\MyApp2

# 初始化 Git 仓库（如果还没有初始化）
git init

# 添加所有文件
git add .

# 提交所有更改
git commit -m "Initial commit - iOS 12.0 support"

# 添加远程仓库（将 YOUR_USERNAME 和 YOUR_REPO 替换为你的 GitHub 用户名和仓库名）
git remote add origin https://github.com/YOUR_USERNAME/YOUR_REPO.git

# 推送代码到 GitHub
git branch -M main
git push -u origin main
```

### 第二步：触发 GitHub Actions 构建

#### 2.1 查看工作流状态

1. 在 GitHub 仓库页面，点击 **"Actions"** 标签
2. 你会看到 "Build iOS App for Real Device" 工作流
3. 如果没有自动运行，点击 **"Run workflow"** 按钮
4. 选择 `main` 分支，点击 **"Run workflow"**

#### 2.2 等待构建完成

- 构建过程通常需要 **10-20 分钟**
- 你可以在 Actions 页面查看构建日志
- 构建成功后，你会看到绿色的勾选标记 ✓

### 第三步：下载构建产物

#### 3.1 下载 IPA 文件

1. 点击构建完成的 workflow 运行
2. 在 "Artifacts" 部分找到 **"ios-app-device"**
3. 点击下载链接，下载 `iosApp-device.zip` 文件
4. 解压下载的 ZIP 文件，得到 `iosApp.ipa` 文件

### 第四步：安装到 iPhone

#### 4.1 使用 AltStore 安装（推荐）

AltStore 是一个免费工具，可以在没有付费开发者账号的情况下安装 iOS 应用。

**准备工作：**

1. 在 iPhone 上打开 **设置 > 通用 > 设备管理**
2. 信任你的 Apple ID（如果还没有）
3. 在 iPhone 上安装 **AltStore**：
   - 在 Safari 中访问 [AltStore.io](https://altstore.io)
   - 按照说明在 iPhone 上安装 AltStore

**安装 IPA：**

1. 在电脑上打开 AltServer（需要在电脑上运行）
2. 确保 iPhone 通过 USB 连接电脑
3. 在 AltServer 托盘图标中点击 "Install AltStore"
4. 选择你的 iPhone 和 Apple ID
5. 等待安装完成

**另一种方法：使用 Cydia Impactor（已停止更新，不推荐）**

如果你之前使用过 Cydia Impactor，可以尝试：
1. 下载 Cydia Impactor
2. 将 iPhone 连接到电脑
3. 拖动 IPA 文件到 Cydia Impactor
4. 输入你的 Apple ID 和密码
5. 等待安装完成

> ⚠️ **注意**：Cydia Impactor 已停止更新，某些 Apple ID 可能无法使用。

#### 4.2 首次运行应用

1. 在 iPhone 主屏幕找到应用图标
2. 首次打开时，系统会提示"未受信任的企业级开发者"
3. 进入 **设置 > 通用 > VPN与设备管理**
4. 找到你的应用描述文件，点击"信任"
5. 现在可以正常打开应用了

### 故障排除

#### 问题 1：GitHub Actions 构建失败

**解决方案：**
- 检查构建日志中的错误信息
- 常见错误：
  - Java 版本问题：确保使用 JDK 17
  - Gradle 问题：检查 gradle-wrapper.properties
  - Swift 版本问题：确保 Xcode 版本支持

#### 问题 2：无法下载构建产物

**解决方案：**
- 确保 GitHub 登录状态正常
- 检查是否有存储空间限制（免费账户有存储限制）
- 尝试重新触发构建

#### 问题 3：iPhone 上无法安装

**解决方案：**
- 确保 iPhone 系统版本在 iOS 12-15.8.3 之间
- 确保有足够的存储空间
- 重新信任描述文件（设置 > 通用 > 设备管理）
- 尝试重启 iPhone

#### 问题 4：应用崩溃

**解决方案：**
- iPhone 6s 硬件较旧，某些新功能可能不兼容
- 检查是否有针对旧设备的特殊处理需求

## 方法二：租用 Mac 云服务

如果你需要更灵活的解决方案，可以租用 Mac 云服务：

### 推荐的 Mac 云服务

1. **MacStadium** (https://www.macstadium.com)
   - 提供专用的 Mac 云服务器
   - 价格：$99/月起

2. **MacinCloud** (https://www.macincloud.com)
   - 按小时或月租用 Mac
   - 价格：$1/小时 或 $20/月起

### 使用步骤

1. 注册并登录云 Mac 服务
2. 通过远程桌面（如 VNC）连接到 Mac
3. 在 Mac 上安装 Xcode
4. 从 GitHub 拉取代代码
5. 使用 Xcode 构建并打包 iOS 应用
6. 使用 Xcode 或 Application Loader 安装到 iPhone

## 常见问题

### Q: iPhone 6s 支持哪个版本的 iOS？

A: iPhone 6s 最高支持 iOS 15.8.3。我们已将应用最低支持版本设置为 iOS 12，以兼容更多设备。

### Q: 是否需要付费的 Apple 开发者账号？

A: 使用 AltStore 安装不需要付费账号。使用免费 Apple ID 即可。

### Q: 构建的 IPA 文件可以用多久？

A: 使用个人 Apple ID 安装的应用有效期为 7 天。7 天后需要重新安装或续签。

### Q: 如何续签应用？

A: 在电脑上打开 AltServer，确保 iPhone 保持连接，然后 AltServer 会自动续签所有已安装的应用。

### Q: 应用数据会丢失吗？

A: 应用数据存储在 iPhone 本地，不受应用续签影响。

## 维护和更新

### 更新应用

1. 修改代码或更新功能
2. 推送到 GitHub
3. GitHub Actions 自动重新构建
4. 下载新的 IPA 文件
5. 使用 AltStore 重新安装（会保留数据）

### 备份重要数据

虽然应用数据通常保存在本地，但建议定期备份：
- 导出重要数据
- 使用 iCloud 备份（设置 > iCloud > iCloud 云备份）

## 技术支持

如果在安装过程中遇到问题：

1. 查看 GitHub Actions 构建日志
2. 检查 AltStore 官方文档
3. 在 GitHub 仓库提交 Issue

## 安全注意事项

1. **仅安装来自可信来源的应用**
2. **不要分享你的 Apple ID 密码给不可信的服务**
3. **使用 AltStore 时确保从官方来源下载**
4. **定期更新 iOS 系统以获得安全补丁**

## 总结

使用 GitHub Actions + AltStore 的组合是在没有 Mac 的情况下在 iPhone 上安装应用的最佳免费方案。虽然过程稍显复杂，但一旦设置完成，后续更新将变得非常简单。

祝你安装顺利！🎉
