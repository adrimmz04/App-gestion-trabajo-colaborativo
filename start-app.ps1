$projectRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
$mailConfigPath = Join-Path $projectRoot "app-mail.local.ps1"

Set-Location $projectRoot

$profiles = "local"
$skipTestsArg = '"-DskipTests"'

if (Test-Path $mailConfigPath) {
    . $mailConfigPath

    if (-not $env:APP_MAIL_USERNAME -or -not $env:APP_MAIL_PASSWORD) {
        Write-Error "El fichero app-mail.local.ps1 existe, pero faltan APP_MAIL_USERNAME o APP_MAIL_PASSWORD."
        exit 1
    }

    if (-not $env:APP_MAIL_FROM) {
        $env:APP_MAIL_FROM = $env:APP_MAIL_USERNAME
    }

    if (-not $env:APP_MAIL_SENDER_NAME) {
        $env:APP_MAIL_SENDER_NAME = "APP-GESTION"
    }

    if (-not $env:APP_MAIL_SMTP_LOCALHOST) {
        $env:APP_MAIL_SMTP_LOCALHOST = "localhost"
    }

    $profiles = "local-gmail"
    Write-Host "Arrancando App Gestion con correo real (perfil: $profiles)..."
} else {
    Write-Host "Arrancando App Gestion en modo local sin correo real (perfil: $profiles)."
    Write-Host "Si quieres activar correo real, copia app-mail.local.example.ps1 a app-mail.local.ps1 y rellena tus credenciales."
}

mvn spring-boot:run "-Dspring-boot.run.profiles=$profiles" $skipTestsArg
