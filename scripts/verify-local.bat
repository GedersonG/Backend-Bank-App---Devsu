@echo off
echo 🔍 Verificando proyecto localmente...

echo.
echo 📦 Build del proyecto...
call .\gradlew.bat clean build -x test
if %errorlevel% neq 0 (
    echo ❌ Build fallo
    exit /b 1
)
echo ✅ Build exitoso

echo.
echo 🧪 Ejecutando tests...
call .\gradlew.bat test --tests *UseCaseImplTest
if %errorlevel% neq 0 (
    echo ❌ Tests fallaron
    exit /b 1
)
echo ✅ Tests exitosos

echo.
echo 📊 Verificando cobertura...
call .\gradlew.bat jacocoTestReport jacocoTestCoverageVerification
if %errorlevel% neq 0 (
    echo ❌ Cobertura no alcanza el minimo
    exit /b 1
)
echo ✅ Cobertura OK

echo.
echo 🐳 Construyendo imagen Docker...
docker build -t bank-app-test .
if %errorlevel% neq 0 (
    echo ❌ Docker build fallo
    exit /b 1
)
echo ✅ Docker build exitoso

echo.
echo 🎉 ¡Todo funciona correctamente! Puedes hacer commit.