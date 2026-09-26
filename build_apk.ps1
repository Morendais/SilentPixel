$ErrorActionPreference = "Stop"

$BUILD_TOOLS = "$env:LOCALAPPDATA\Android\Sdk\build-tools\36.1.0"
$ANDROID_JAR = "$env:LOCALAPPDATA\Android\Sdk\platforms\android-36.1\android.jar"
$APP_DIR = "$PSScriptRoot\app"
$OUT_DIR = "$APP_DIR\build"

Write-Host "[*] Cleaning build dir..."
if (Test-Path $OUT_DIR) { Remove-Item $OUT_DIR -Recurse -Force }
New-Item -ItemType Directory -Path "$OUT_DIR\gen" | Out-Null
New-Item -ItemType Directory -Path "$OUT_DIR\obj" | Out-Null
New-Item -ItemType Directory -Path "$OUT_DIR\res" | Out-Null

Write-Host "[*] Compiling resources with aapt2..."
& "$BUILD_TOOLS\aapt2.exe" compile --dir "$APP_DIR\src\main\res" -o "$OUT_DIR\res\resources.zip"

Write-Host "[*] Linking resources with aapt2..."
& "$BUILD_TOOLS\aapt2.exe" link -I $ANDROID_JAR `
    --manifest "$APP_DIR\src\main\AndroidManifest.xml" `
    --min-sdk-version 26 --target-sdk-version 35 `
    --java "$OUT_DIR\gen" `
    -o "$OUT_DIR\app-unsigned.apk" `
    "$OUT_DIR\res\resources.zip" `
    --auto-add-overlay

Write-Host "[*] Compiling Java sources with javac..."
$shizukuJars = @(
    "$env:TEMP\shizuku-api\classes.jar",
    "$env:TEMP\shizuku-provider\classes.jar",
    "$env:TEMP\shizuku-shared\classes.jar",
    "$env:TEMP\shizuku-aidl\classes.jar"
)
$classpath = "$ANDROID_JAR;" + ($shizukuJars -join ";")
$sources = (Get-ChildItem -Path "$APP_DIR\src\main\java", "$OUT_DIR\gen" -Filter "*.java" -Recurse | Select-Object -ExpandProperty FullName)

& "javac" -d "$OUT_DIR\obj" -cp $classpath --release 11 $sources

Write-Host "[*] Converting class files to DEX with d8..."
$classFiles = (Get-ChildItem -Path "$OUT_DIR\obj" -Filter "*.class" -Recurse | Select-Object -ExpandProperty FullName)

& "$BUILD_TOOLS\d8.bat" --lib $ANDROID_JAR --output "$OUT_DIR" ($classFiles + $shizukuJars)

Write-Host "[*] Adding classes.dex to APK..."
Set-Location $OUT_DIR
& "jar" uf "$OUT_DIR\app-unsigned.apk" classes.dex

Write-Host "[*] Aligning APK with zipalign..."
& "$BUILD_TOOLS\zipalign.exe" -v -p 4 "$OUT_DIR\app-unsigned.apk" "$OUT_DIR\SilentPixel-unaligned.apk"

Write-Host "[*] Signing APK with apksigner..."
& "$BUILD_TOOLS\apksigner.bat" sign `
    --ks "$env:USERPROFILE\.android\debug.keystore" `
    --ks-pass "pass:android" `
    --key-pass "pass:android" `
    --ks-key-alias "androiddebugkey" `
    --out "$APP_DIR\..\SilentPixel.apk" `
    "$OUT_DIR\SilentPixel-unaligned.apk"

Write-Host "[+] SUCCESS: SilentPixel.apk has been built!"
