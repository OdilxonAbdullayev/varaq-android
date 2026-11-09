$ErrorActionPreference = 'Stop'
$java = Get-Command java.exe -ErrorAction SilentlyContinue
if (-not $java) {
    $candidates = @(
        (Join-Path $env:ProgramFiles 'Android\Android Studio\jbr'),
        (Join-Path $env:ProgramFiles 'Eclipse Adoptium')
    )
    foreach ($candidate in $candidates) {
        if (Test-Path (Join-Path $candidate 'bin\java.exe')) {
            $env:JAVA_HOME = $candidate
            $env:PATH = (Join-Path $candidate 'bin') + ';' + $env:PATH
            break
        }
    }
}
if (-not (Get-Command java.exe -ErrorAction SilentlyContinue)) {
    throw 'Java 17+ topilmadi. Android Studio ichidagi JBR ni o‘rnating yoki JAVA_HOME ni sozlang.'
}
[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12
$root = Split-Path -Parent $MyInvocation.MyCommand.Path
$version = '9.4.1'
$cache = Join-Path $root '.gradle-local'
$zip = Join-Path $cache "gradle-$version-bin.zip"
$gradle = Join-Path $cache "gradle-$version\bin\gradle.bat"
New-Item -ItemType Directory -Force -Path $cache | Out-Null
if (-not (Test-Path $gradle)) {
    Write-Host "Gradle $version bepul yuklab olinmoqda (internet kerak)..."
    $url = "https://services.gradle.org/distributions/gradle-$version-bin.zip"
    if (-not (Test-Path $zip)) {
        Invoke-WebRequest -Uri $url -UseBasicParsing -OutFile $zip
    }
    Write-Host 'Gradle arxivi tekshirilmoqda...'
    $expected = (Invoke-WebRequest -Uri "$url.sha256" -UseBasicParsing).Content.Trim().Split(' ')[0].ToLower()
    $actual = (Get-FileHash -Path $zip -Algorithm SHA256).Hash.ToLower()
    if ($expected -ne $actual) { Remove-Item $zip -Force; throw 'Gradle SHA-256 tekshiruvidan o‘tmadi. Qayta urining.' }
    Expand-Archive -Path $zip -DestinationPath $cache -Force
}
$temp = Join-Path $cache 'wrapper-setup'
New-Item -ItemType Directory -Force -Path $temp | Out-Null
Set-Content -Path (Join-Path $temp 'settings.gradle.kts') -Value 'rootProject.name = "wrapper-setup"'
Write-Host 'Rasmiy Gradle Wrapper yaratilmoqda...'
& $gradle -p $temp wrapper --gradle-version $version --distribution-type bin
if ($LASTEXITCODE -ne 0) { throw 'Gradle Wrapper yaratilmadi' }
Copy-Item (Join-Path $temp 'gradle\wrapper\gradle-wrapper.jar') (Join-Path $root 'gradle\wrapper\gradle-wrapper.jar') -Force
Copy-Item (Join-Path $temp 'gradlew.bat') (Join-Path $root 'gradlew.bat') -Force
Copy-Item (Join-Path $temp 'gradlew') (Join-Path $root 'gradlew') -Force
Write-Host 'Gradle Wrapper tayyor. Android Studio: File > Open > VARAQ-Android.' -ForegroundColor Green
