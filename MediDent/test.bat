@echo off
chcp 65001 >nul
cd /d "%~dp0"

set "JDBC=lib\mssql-jdbc-12.8.1.jre11.jar"
if not exist "build\classes\UI\DangNhap\DangNhapFrame.class" (
    call build.bat
    if errorlevel 1 exit /b 1
)

echo Đang biên dịch SmokeTest...
javac -encoding UTF-8 -cp "build\classes;src;%JDBC%" -d build\classes test\SmokeTest.java
if errorlevel 1 exit /b 1

echo Đang chạy SmokeTest...
java -Dfile.encoding=UTF-8 -cp "build\classes;src;%JDBC%" SmokeTest
