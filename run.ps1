Write-Host "Compiling ChabiVault..." -ForegroundColor Cyan
javac -cp "lib\*" Pages\CryptoUtils.java Pages\PasswordGenerator.java Pages\DatabaseManager.java Pages\LoginPage.java Pages\RegisterPage.java Pages\VaultPage.java Pages\ChabiVaultApp.java Main.java
if ($LASTEXITCODE -ne 0) {
    Write-Host "Compilation failed!" -ForegroundColor Red
    pause
    exit $LASTEXITCODE
}
Write-Host "Compilation successful. Starting ChabiVault..." -ForegroundColor Green
java -cp "lib\*;." Main
