@echo off
echo Compiling ChabiVault...
javac -cp "lib\*" Pages\*.java Main.java
if %errorlevel% neq 0 (
    echo Compilation failed!
    pause
    exit /b %errorlevel%
)
echo Compilation successful. Starting ChabiVault...
java -cp "lib\*;." Main
