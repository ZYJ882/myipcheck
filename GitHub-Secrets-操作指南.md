# myipcheck 自动发布配置指南

## 你需要做什么

仓库里的自动化脚本已经写好。以后把 Android 源码压缩包放到 `uploads/` 目录并提交后，GitHub Actions 会自动构建 APK 并创建 Release。

现在只需要做一次签名配置。签名配置的作用是让新版 APK 能覆盖安装当前已经安装的 `NetScope-debug.apk`。如果不配置同一签名，Android 会提示“无法安装”或“签名不一致”。

## 最简单的方法：在 GitHub 网页中设置

第一步，打开仓库的 Settings 页面：

```text
https://github.com/ZYJ882/myipcheck/settings/secrets/actions
```

第二步，找到 **Secrets and variables → Actions**，点击 **New repository secret**。

第三步，依次添加下表中的四个 Secret。名称必须完全一致，不能多空格；值按照表格填写。

| Secret 名称 | 填写内容 |
| --- | --- |
| `ANDROID_KEYSTORE_BASE64` | 复制附件 `myipcheck_signing_key_base64.txt` 中的全部内容，从第一个字符复制到最后一个字符。不要换行，不要加引号。 |
| `ANDROID_KEYSTORE_PASSWORD` | `android` |
| `ANDROID_KEY_ALIAS` | `androiddebugkey` |
| `ANDROID_KEY_PASSWORD` | `android` |

每个 Secret 都填写后点击 **Add secret**。添加完成后，页面上只能看到 Secret 名称，GitHub 不会再次显示 Secret 的值，这是正常的。

## 上传新的源码压缩包

以后把完整 Android 工程压缩为 ZIP，例如 `NetScope-source.zip`。压缩包里需要包含 `settings.gradle.kts`、根目录 `build.gradle.kts` 和 `app/` 文件夹。不要放入 `local.properties`、`.gradle`、`app/build` 或任何 keystore 文件。

在仓库网页中进入 `uploads` 目录，点击 **Add file → Upload files**，选择源码 ZIP，填写提交说明并点击 **Commit changes**。提交到 `main` 分支后，自动工作流会开始运行。

也可以使用命令行：

```bash
cp /你的路径/NetScope-source.zip uploads/NetScope-source.zip
git add uploads/NetScope-source.zip
git commit -m "chore: upload Android source archive"
git push origin main
```

## 查看构建结果

打开仓库的 **Actions** 页面，查看名为 **Build and Release Android APK** 的工作流。绿色勾表示成功；成功后打开仓库的 **Releases** 页面，就能下载新版本 APK。

工作流会自动生成递增的版本号，并使用同一个 `applicationId` 和同一份签名证书，因此新 APK 可以覆盖安装旧版本。

## 重要安全提醒

`myipcheck_signing_key_base64.txt` 是签名私钥的编码文件。它只用于设置 GitHub Secret，**不要把它上传到仓库、发到公开群组或提交到 Git**。如果你以后换了签名密钥，旧 APK 就不能直接覆盖安装；必须继续使用同一份签名密钥。
