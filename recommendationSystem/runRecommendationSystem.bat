@echo off
echo Compile...
javac -d . -cp "lib/*" *.java

if errorlevel 1 (
    echo Fail
    pause
    exit /b
)

echo compile successfully, run the program...
echo ----------------------------
java -cp ".;lib/*" compile/Main

echo ----------------------------
echo Finish!
pause
