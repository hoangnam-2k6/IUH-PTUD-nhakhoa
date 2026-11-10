@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion
cd /d "%~dp0"

set "JDBC=lib\mssql-jdbc-12.8.1.jre11.jar"
if not exist "%JDBC%" (
    echo [LỖI] Không tìm thấy "%JDBC%".
    echo Hãy chép file mssql-jdbc vào thư mục lib của dự án.
    exit /b 1
)

if not exist build\classes mkdir build\classes

set "SOURCES="
for /r "src" %%f in (*.java) do set "SOURCES=!SOURCES! "%%f""

echo Đang biên dịch...
javac -encoding UTF-8 -cp "%JDBC%" -d build\classes !SOURCES!
if errorlevel 1 (
    echo.
    echo [LỖI] Biên dịch thất bại.
    exit /b 1
)

echo.
echo [OK] Biên dịch thành công. Chạy run.bat để mở ứng dụng.
exit /b 0
