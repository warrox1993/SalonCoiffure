@echo off
echo ====================================
echo TEST DE VALIDATION DES PLACEHOLDERS
echo ====================================

echo.
echo Test 1: Compilation Maven
call mvnw.cmd clean compile -DskipTests
if %errorlevel% neq 0 (
    echo ECHEC: La compilation a echoue
    exit /b 1
) else (
    echo SUCCES: Compilation reussie
)

echo.
echo Test 2: Verification des fichiers de configuration
findstr /C:"${" src\main\resources\application*.properties > placeholder-test-results.txt
if %errorlevel% equ 0 (
    echo PLACEHOLDERS TROUVES dans les fichiers de configuration:
    type placeholder-test-results.txt
    echo.
    echo Verification que tous utilisent la syntaxe avec tirets...
    findstr /C:"${.*_.*}" src\main\resources\application*.properties > bad-placeholders.txt
    if %errorlevel% equ 0 (
        echo ATTENTION: Des placeholders avec underscores detectes:
        type bad-placeholders.txt
        del bad-placeholders.txt
    ) else (
        echo SUCCES: Tous les placeholders utilisent la syntaxe correcte avec tirets
    )
    del placeholder-test-results.txt
) else (
    echo SUCCES: Aucun placeholder trouve dans les fichiers de configuration
)

echo.
echo Test 3: Validation du SecretsValidator
findstr /n "JWT-SECRET\|JWT-EXPIRATION-MS\|JWT-REFRESH-EXPIRATION-MS" src\main\java\com\jb\afrostyle\security\validation\SecretsValidator.java
if %errorlevel% equ 0 (
    echo SUCCES: SecretsValidator utilise la syntaxe correcte avec tirets
) else (
    echo ATTENTION: SecretsValidator pourrait avoir des problemes
)

echo.
echo ====================================
echo RESUME DES CORRECTIONS APPLIQUEES:
echo ====================================
echo - JWT_EXPIRATION_MS => JWT-EXPIRATION-MS
echo - JWT_REFRESH_EXPIRATION_MS => JWT-REFRESH-EXPIRATION-MS 
echo - SMS_ENABLED => SMS-ENABLED
echo - GOOGLE_CALENDAR_ENABLED => GOOGLE-CALENDAR-ENABLED
echo - GOOGLE_CALENDAR_TIMEZONE => GOOGLE-CALENDAR-TIMEZONE
echo - GOOGLE_MAPS_ENABLED => GOOGLE-MAPS-ENABLED
echo - FRONTEND_URL => FRONTEND-URL
echo.
echo VALIDATION TERMINEE
echo ====================================