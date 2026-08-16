# Android 源码上传目录

把新的 Android 完整源码压缩包放到这个目录并提交到 `main` 分支，即可触发自动构建与发布工作流。

## 压缩包要求

压缩包支持 `.zip`、`.tar`、`.tar.gz` 和 `.tgz`。压缩包内部必须包含完整 Gradle 工程，至少应有 `settings.gradle.kts` 或 `settings.gradle`、根目录 `build.gradle.kts` 或 `build.gradle`，以及 `app/build.gradle.kts` 或 `app/build.gradle`。允许压缩包外面再包一层项目文件夹，自动化脚本会自动寻找 Android 工程根目录。

不要把 `local.properties`、`.gradle`、`app/build` 或任何签名 keystore 放进压缩包。签名私钥只通过 GitHub Actions Secrets 提供。

## 上传示例

```bash
cp /path/to/NetScope-source.zip uploads/NetScope-source.zip
git add uploads/NetScope-source.zip
git commit -m "chore: upload Android source archive"
git push origin main
```

提交后，打开仓库的 **Actions → Build and Release Android APK** 查看构建过程。构建成功后，新的 APK 会出现在仓库的 **Releases** 页面；工作流同时会保留 `android/releases/` 下的 APK 副本。

如果同一个文件名被新版本替换，必须提交一个新的 Git commit，GitHub Actions 才会再次触发。也可以从 Actions 页面手动运行工作流，并填写 `archive_path`，例如 `uploads/NetScope-source.zip`。
