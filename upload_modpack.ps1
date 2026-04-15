# ColonyCraft Modpack CurseForge Upload Script
# Usage: .\upload_modpack.ps1
# Requires: $env:CURSEFORGE_TOKEN set

$ErrorActionPreference = "Stop"

$token     = $env:CURSEFORGE_TOKEN
$projectId = "1514661"
$zipPath   = "C:\ColonyCraft\ColonyCraft_Modpack.zip"
$apiBase   = "https://minecraft.curseforge.com/api"

if (-not $token) {
    Write-Host "ERROR: CURSEFORGE_TOKEN not set!" -ForegroundColor Red
    exit 1
}
if (-not (Test-Path $zipPath)) {
    Write-Host "ERROR: Modpack zip not found at $zipPath - run build_modpack.ps1 first!" -ForegroundColor Red
    exit 1
}

Write-Host "=== ColonyCraft Modpack Upload ===" -ForegroundColor Cyan

# Step 1: Fetch game versions
Write-Host "Fetching game versions..." -ForegroundColor Yellow
try {
    $headers = @{ "X-Api-Token" = $token }
    $versions = Invoke-RestMethod -Uri "$apiBase/game/versions" -Headers $headers -Method Get
    Write-Host "  Found $($versions.Count) game versions" -ForegroundColor Green
} catch {
    Write-Host "ERROR fetching versions: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

# Step 2: Find version IDs
$mcVersion = $versions | Where-Object { $_.name -eq "1.21.1" } | Select-Object -First 1
$fabric    = $versions | Where-Object { $_.name -eq "Fabric" }   | Select-Object -First 1
$java21    = $versions | Where-Object { $_.name -eq "Java 21" }  | Select-Object -First 1

$gameVersionIds = @()

if ($mcVersion) {
    $gameVersionIds += $mcVersion.id
    Write-Host "  MC 1.21.1 -> ID $($mcVersion.id)" -ForegroundColor Green
}
if ($fabric) {
    $gameVersionIds += $fabric.id
    Write-Host "  Fabric -> ID $($fabric.id)" -ForegroundColor Green
}
# Note: Java 21 version ID is not valid for modpacks, skip it

if ($gameVersionIds.Count -eq 0) {
    Write-Host "ERROR: Could not find any matching game versions!" -ForegroundColor Red
    exit 1
}

# Step 3: Build metadata
$metadataObj = @{
    changelog     = "Initial release v1.0.0 - Colony Survival mechanics in Minecraft! Includes ColonyCraft mod, Fabric API, Sodium, Lithium, Iris Shaders, Mod Menu, and custom shader pack."
    changelogType = "text"
    displayName   = "ColonyCraft Modpack v1.0.0"
    gameVersions  = $gameVersionIds
    releaseType   = "release"
}
$metadata = $metadataObj | ConvertTo-Json -Depth 5

Write-Host ""
Write-Host "Upload metadata:" -ForegroundColor Yellow
Write-Host "  Project: $projectId (Modpack)"
Write-Host "  Versions: $($gameVersionIds -join ', ')"
Write-Host "  File: $zipPath"
Write-Host ""

# Step 4: Upload via .NET HttpClient (multipart)
Write-Host "Uploading modpack to CurseForge (project $projectId)..." -ForegroundColor Yellow

Add-Type -AssemblyName System.Net.Http

$httpClient = New-Object System.Net.Http.HttpClient
$httpClient.DefaultRequestHeaders.Add("X-Api-Token", $token)

$form = New-Object System.Net.Http.MultipartFormDataContent

# Metadata part
$metadataContent = New-Object System.Net.Http.StringContent($metadata)
$metadataContent.Headers.ContentType = $null
$form.Add($metadataContent, "metadata")

# File part
$fileBytes = [System.IO.File]::ReadAllBytes($zipPath)
$fileName  = [System.IO.Path]::GetFileName($zipPath)
$fileContent = New-Object System.Net.Http.ByteArrayContent(,($fileBytes))
$fileContent.Headers.ContentType = New-Object System.Net.Http.Headers.MediaTypeHeaderValue("application/zip")
$form.Add($fileContent, "file", $fileName)

$uploadUrl = "$apiBase/projects/$projectId/upload-file"
Write-Host "  URL: $uploadUrl"
Write-Host "  File size: $([math]::Round($fileBytes.Length / 1024))KB"

try {
    $response = $httpClient.PostAsync($uploadUrl, $form).Result
    $responseBody = $response.Content.ReadAsStringAsync().Result

    if ($response.IsSuccessStatusCode) {
        Write-Host ""
        Write-Host "SUCCESS! Modpack uploaded to CurseForge!" -ForegroundColor Green
        Write-Host "Response: $responseBody" -ForegroundColor Cyan
        Write-Host "View: https://www.curseforge.com/minecraft/modpacks/colonycraft-modpack/files" -ForegroundColor Cyan
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
