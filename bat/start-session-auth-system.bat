@echo off
echo ======================================================
echo      DÉMARRAGE SYSTÈME AUTHENTIFICATION SESSION
echo         AfroStyle Backend + Frontend Angular
echo ======================================================

echo.
echo 1. Vérification des prérequis...
where java >nul 2>nul
if %errorlevel% neq 0 (
    echo ❌ Java non trouvé! Installez Java 21 ou plus.
    pause
    exit /b 1
)

where node >nul 2>nul
if %errorlevel% neq 0 (
    echo ❌ Node.js non trouvé! Installez Node.js pour le frontend.
    pause
    exit /b 1
)

echo ✅ Java et Node.js détectés

echo.
echo 2. Compilation du backend Spring Boot...
cd /d "%~dp0"
call mvnw.cmd clean compile -q
if %errorlevel% neq 0 (
    echo ❌ Erreur de compilation backend
    pause
    exit /b 1
)
echo ✅ Backend compilé avec succès

echo.
echo 3. Démarrage de la base de données MySQL...
docker-compose up -d afrostyle-mysql
timeout /t 10 /nobreak > nul
echo ✅ MySQL démarré (attente 10s pour initialisation)

echo.
echo 4. Démarrage du backend Spring Boot (port 7777)...
start "Backend AfroStyle" cmd /k "cd /d %~dp0 && mvnw.cmd spring-boot:run"
echo ✅ Backend en cours de démarrage...

echo.
echo 5. Attente du démarrage backend (60s)...
timeout /t 60 /nobreak > nul

echo.
echo 6. Test de connectivité backend...
curl -s http://localhost:7777/actuator/health > nul
if %errorlevel% neq 0 (
    echo ⚠️ Backend pas encore prêt, attendez quelques secondes de plus
) else (
    echo ✅ Backend opérationnel sur http://localhost:7777
)

echo.
echo 7. Démarrage du frontend Angular (port 4200)...
cd /d "C:\Users\jeanb\Desktop\SalonBookingFront\mon-app-angular"
start "Frontend Angular" cmd /k "npm start"
echo ✅ Frontend en cours de démarrage...

echo.
echo ======================================================
echo              SYSTÈME DÉMARRÉ AVEC SUCCÈS!
echo ======================================================
echo.
echo 🌐 URLs d'accès:
echo    • Frontend Angular: http://localhost:4200
echo    • Backend API:      http://localhost:7777
echo    • Health Check:     http://localhost:7777/actuator/health
echo    • phpMyAdmin:       http://localhost:8081
echo.
echo 🔐 Configuration d'authentification:
echo    • Type: Session-based (Spring Security)
echo    • Cookies: Gérés automatiquement
echo    • CORS: Configuré pour localhost:4200
echo    • Compatible: AuthResponse Angular
echo.
echo 🧪 Test d'authentification:
echo    node C:\Users\jeanb\Desktop\test-session-auth.js
echo.
echo 📚 Compte de test:
echo    • Username: admin2
echo    • Password: admin123
echo    • Role: ADMIN
echo.
echo ✋ Pour arrêter le système:
echo    • Fermer les fenêtres de commande
echo    • docker-compose down
echo.
pause