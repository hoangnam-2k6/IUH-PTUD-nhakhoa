@echo off
setlocal EnableExtensions EnableDelayedExpansion
REM PORTABLE-JDK17: chay theo thu muc du an, khong phu thuoc duong dan tuyet doi.
cd /d "%~dp0"
set "JAVA_EXE=java"
if defined JAVA_HOME if exist "%JAVA_HOME%\bin\java.exe" set "JAVA_EXE=%JAVA_HOME%\bin\java.exe"
REM PORTABLE-JDK17: lay phien ban java.exe dang dung, khong tu chon Java 21.
set "VERSION_FILE=%TEMP%\MediDent_Java_%RANDOM%_%RANDOM%.txt"
"%JAVA_EXE%" -version > "%VERSION_FILE%" 2>&1
set "JAVA_EXIT=%ERRORLEVEL%"
type "%VERSION_FILE%"
if not "%JAVA_EXIT%"=="0" (
    del /q "%VERSION_FILE%" >nul 2>nul
    echo [LOI] Khong chay duoc Java. Hay cai JDK 17 va dat JAVA_HOME tro den JDK 17.
    pause
    exit /b 1
)
for /f "tokens=3" %%V in ('findstr /i /c:"version" "%VERSION_FILE%"') do set "JAVA_VERSION=%%~V"
del /q "%VERSION_FILE%" >nul 2>nul
if not "!JAVA_VERSION:~0,3!"=="17." (
    echo [LOI] Dang chon Java !JAVA_VERSION!, can JDK 17.
    echo Sua JAVA_HOME thanh duong dan JDK 17 da cai tren may nay.
    pause
    exit /b 1
)
if not exist "dist\MediDent-JDK17.jar" (
    echo [LOI] Khong tim thay dist\MediDent-JDK17.jar
    pause
    exit /b 1
)
if not exist "db.properties" (
    echo [LOI] Thieu db.properties. Tao tu db.properties.example va cau hinh SQL Server.
    pause
    exit /b 1
)
echo [OK] Dung JDK 17; dang mo MediDent...
"%JAVA_EXE%" -jar "dist\MediDent-JDK17.jar"
if errorlevel 1 (
    echo [LOI] Ung dung chay loi. Kiem tra thong bao o tren.
    pause
)
endlocal
