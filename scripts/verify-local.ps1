# scripts/verify-local.ps1
Write-Host "🔍 Verificando proyecto localmente..." -ForegroundColor Yellow

# 1. Build
Write-Host "`n📦 Build del proyecto..." -ForegroundColor Yellow
.\gradlew.bat clean build -x test
if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ Build falló" -ForegroundColor Red
    exit 1
}
Write-Host "✅ Build exitoso" -ForegroundColor Green

# 2. Tests
Write-Host "`n🧪 Ejecutando tests..." -ForegroundColor Yellow
.\gradlew.bat test --tests *UseCaseImplTest
if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ Tests fallaron" -ForegroundColor Red
    exit 1
}
Write-Host "✅ Tests exitosos" -ForegroundColor Green

# 3. Cobertura
Write-Host "`n📊 Verificando cobertura..." -ForegroundColor Yellow
.\gradlew.bat jacocoTestReport jacocoTestCoverageVerification
if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ Cobertura no alcanza el mínimo" -ForegroundColor Red
    exit 1
}
Write-Host "✅ Cobertura OK" -ForegroundColor Green

# 4. Docker build
Write-Host "`n🐳 Construyendo imagen Docker..." -ForegroundColor Yellow
docker build -t bank-app-test .
if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ Docker build falló" -ForegroundColor Red
    exit 1
}
Write-Host "✅ Docker build exitoso" -ForegroundColor Green

Write-Host "`n🎉 ¡Todo funciona correctamente! Puedes hacer commit." -ForegroundColor Green