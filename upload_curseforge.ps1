# ColonyCraft CurseForge Upload Script
# Usage: .\upload_curseforge.ps1
# Requires: $env:CURSEFORGE_TOKEN set

$ErrorActionPreference = "Stop"

$token     = $env:CURSEFORGE_TOKEN
$projectId = "1514636"
$jarPath   = "C:\ColonyCraft\build\libs\colonycraft-1.0.0.jar"
$apiBase   = "https://minecraft.curseforge.com/api"

if (-not $token) {
    Write-Host "ERROR: CURSEFORGE_TOKEN not set!" -ForegroundColor Red
    exit 1
}
if (-not (Test-Path $jarPath)) {
    Write-Host "ERROR: JAR not found at $jarPath - run gradlew build first!" -ForegroundColor Red
    exit 1
}

Write-Host "=== ColonyCraft CurseForge Upload ===" -ForegroundColor Cyan

# Step 1: Fetch game versions
Write-Host "Fetching game versions..." -ForegroundColor Yellow
try {
    $headers = @{ "X-Api-Token" = $token }
    $versions = Invoke-RestMethod -Uri "$apiBase/game/versions" -Headers $headers -Method Get
    Write-Host "  Found $($versions.Count) game versions" -ForegroundColor Green
} catch {
    Write-Host "ERROR fetching versions: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host ""
    Write-Host "Token may be invalid. Get a new one at:" -ForegroundColor Yellow
    Write-Host "  https://www.curseforge.com/account/api-tokens" -ForegroundColor White
    exit 1
}

# Step 2: Find version IDs
# Look for: Minecraft 1.21.11, Fabric, Java 21
$mcVersion = $versions | Where-Object { $_.name -eq "1.21.11" } | Select-Object -First 1
$fabric    = $versions | Where-Object { $_.name -eq "Fabric" }   | Select-Object -First 1
$java21    = $versions | Where-Object { $_.name -eq "Java 21" }  | Select-Object -First 1

$gameVersionIds = @()

if ($mcVersion) {
    $gameVersionIds += $mcVersion.id
    Write-Host "  MC 1.21.11 -> ID $($mcVersion.id)" -ForegroundColor Green
} else {
    # Try broader match
    $mc121 = $versions | Where-Object { $_.name -like "1.21*" } | Sort-Object name -Descending
    if ($mc121) {
        Write-Host "  Available 1.21.x versions:" -ForegroundColor Yellow
        $mc121 | ForEach-Object { Write-Host "    $($_.name) (ID: $($_.id))" }
        $gameVersionIds += $mc121[0].id
        Write-Host "  Using: $($mc121[0].name) -> ID $($mc121[0].id)" -ForegroundColor Green
    } else {
        Write-Host "  WARNING: No 1.21.x version found!" -ForegroundColor Red
    }
}

if ($fabric) {
    $gameVersionIds += $fabric.id
    Write-Host "  Fabric -> ID $($fabric.id)" -ForegroundColor Green
} else {
    Write-Host "  WARNING: Fabric not found in versions!" -ForegroundColor Red
}

if ($java21) {
    $gameVersionIds += $java21.id
    Write-Host "  Java 21 -> ID $($java21.id)" -ForegroundColor Green
}

if ($gameVersionIds.Count -eq 0) {
    Write-Host "ERROR: Could not find any matching game versions!" -ForegroundColor Red
    Write-Host "Available versions:" -ForegroundColor Yellow
    $versions | Select-Object -First 30 | ForEach-Object { Write-Host "  $($_.name) (ID: $($_.id))" }
    exit 1
}

# Step 3: Read changelog
$changelog = "Initial release v1.0.0 - Colony Survival mechanics in Minecraft"
$changelogPath = "C:\ColonyCraft\CHANGELOG.md"
if (Test-Path $changelogPath) {
    $changelog = (Get-Content $changelogPath -Raw).Trim()
}

# Step 4: Build metadata as hashtable, let ConvertTo-Json handle escaping
$metadataObj = @{
    changelog     = [string]$changelog
    changelogType = "markdown"
    displayName   = "ColonyCraft v1.0.0"
    gameVersions  = $gameVersionIds
    releaseType   = "release"
}
$metadata = $metadataObj | ConvertTo-Json -Depth 5

Write-Host ""
Write-Host "Upload metadata:" -ForegroundColor Yellow
Write-Host "  Versions: $($gameVersionIds -join ', ')"
Write-Host "  File: $jarPath"
Write-Host ""

# Step 5: Upload via .NET HttpClient (proper multipart)
Write-Host "Uploading to CurseForge (project $projectId)..." -ForegroundColor Yellow

Add-Type -AssemblyName System.Net.Http

$httpClient = New-Object System.Net.Http.HttpClient
$httpClient.DefaultRequestHeaders.Add("X-Api-Token", $token)

$form = New-Object System.Net.Http.MultipartFormDataContent

# Metadata part — must be sent as plain string (CurseForge expects string containing JSON)
$metadataContent = New-Object System.Net.Http.StringContent($metadata)
$metadataContent.Headers.ContentType = $null
$form.Add($metadataContent, "metadata")

# File part
$fileBytes = [System.IO.File]::ReadAllBytes($jarPath)
$fileName  = [System.IO.Path]::GetFileName($jarPath)
$fileContent = New-Object System.Net.Http.ByteArrayContent(,($fileBytes))
$fileContent.Headers.ContentType = New-Object System.Net.Http.Headers.MediaTypeHeaderValue("application/java-archive")
$form.Add($fileContent, "file", $fileName)

$uploadUrl = "$apiBase/projects/$projectId/upload-file"
Write-Host "  URL: $uploadUrl"
Write-Host "  File size: $([math]::Round($fileBytes.Length / 1024))KB"

try {
    $response = $httpClient.PostAsync($uploadUrl, $form).Result
    $responseBody = $response.Content.ReadAsStringAsync().Result

    if ($response.IsSuccessStatusCode) {
        Write-Host ""
        Write-Host "SUCCESS! File uploaded to CurseForge!" -ForegroundColor Green
        Write-Host "Response: $responseBody" -ForegroundColor Cyan
        Write-Host "View: https://www.curseforge.com/minecraft/mc-mods/colonycraft/files" -ForegroundColor Cyan
    } else {
        Write-Host ""
        Write-Host "UPLOAD FAILED!" -ForegroundColor Red
        Write-Host "Status: $($response.StatusCode) ($([int]$response.StatusCode))" -ForegroundColor Red
        Write-Host "Response: $responseBody" -ForegroundColor Red
    }
} catch {
    Write-Host "ERROR: $($_.Exception.Message)" -ForegroundColor Red
    if ($_.Exception.InnerException) {
        Write-Host "Inner: $($_.Exception.InnerException.Message)" -ForegroundColor Red
    }
} finally {
    $form.Dispose()
    $httpClient.Dispose()
}
