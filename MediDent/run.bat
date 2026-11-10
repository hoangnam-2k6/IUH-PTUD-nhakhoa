@echo off
chcp 65001 >nul
cd /d "%~dp0"

if not exist "build\classes\UI\DangNhap\DangNhapFrame.class" (
    call build.bat
    if errorlevel 1 exit /b 1
)

java -Dfile.encoding=UTF-8 -cp "build\classes;src;lib\mssql-jdbc-12.8.1.jre11.jar" UI.DangNhap.DangNhapFrame
