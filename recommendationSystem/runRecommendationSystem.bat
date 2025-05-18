@echo off
echo Compile...
javac -cp "lib/*" *.java

if errorlevel 1 (
    echo Fail
    pause
    exit /b
)

echo compile successfully, run the program...
echo ----------------------------
java -cp ".;lib/*" Main

echo ----------------------------
echo Finish!
pause
