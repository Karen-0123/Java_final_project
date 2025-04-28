@echo off
cd /d D:\Java_final_project\javaqimo

echo Compile Java File...
javac -cp ".;lib\json-20240303.jar" MovieApi.java

if %errorlevel% neq 0 (
    echo Fail！
    pause
    exit /b
)

echo Compile successfully, running the program...
java -cp ".;lib\json-20240303.jar" MovieApi

pause
