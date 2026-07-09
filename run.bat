@echo off
echo Compiling ChabiVault...
javac -cp "lib\*" Pages\CryptoUtils.java Pages\PasswordGenerator.java Pages\DatabaseManager.java Pages\LoginPage.java Pages\RegisterPage.java Pages\VaultPage.java Pages\ChabiVaultApp.java Main.java
if %errorlevel% neq 0 (
    echo Compilation failed!
    pause
    exit /b %errorlevel%
)
echo Compilation successful. Starting ChabiVault...
java -cp "lib\*;." Main
