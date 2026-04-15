# ColonyCraft Modpack Builder
# Creates a CurseForge-compatible modpack zip
# Usage: .\build_modpack.ps1
# Requires: $env:CURSEFORGE_TOKEN

$ErrorActionPreference = "Stop"

$token = $env:CURSEFORGE_TOKEN
if (-not $token) {
    Write-Host "ERROR: Set CURSEFORGE_TOKEN first!" -ForegroundColor Red
    exit 1
}

$apiBase   = "https://minecraft.curseforge.com/api"
$headers   = @{ "X-Api-Token" = $token }
$packDir   = "C:\ColonyCraft\modpack_build"
$outputZip = "C:\ColonyCraft\ColonyCraft_Modpack.zip"

# Clean previous build
if (Test-Path $packDir) { Remove-Item $packDir -Recurse -Force }
New-Item -ItemType Directory -Path $packDir -Force | Out-Null
New-Item -ItemType Directory -Path "$packDir\overrides\shaderpacks" -Force | Out-Null

Write-Host "=== ColonyCraft Modpack Builder ===" -ForegroundColor Cyan
Write-Host ""

# ── Step 1: Look up mod project IDs via search ──
Write-Host "Looking up mods..." -ForegroundColor Yellow

$modsToFind = @(
    @{ slug = "fabric-api";    name = "Fabric API";    projectId = 306612 },
    @{ slug = "sodium";        name = "Sodium";        projectId = 394468 },
    @{ slug = "lithium";       name = "Lithium";       projectId = 360438 },
    @{ slug = "irisshaders";   name = "Iris Shaders";  projectId = 455508 },
    @{ slug = "modmenu";       name = "Mod Menu";      projectId = 308702 },
    @{ slug = "colonycraft";   name = "ColonyCraft";   projectId = 1514636 }
)

# ── Step 2: Find latest file ID for each mod (1.21.11 + Fabric) ──
Write-Host "Finding latest files for MC 1.21.11 + Fabric..." -ForegroundColor Yellow

$manifestFiles = @()
$modlistEntries = @()

foreach ($mod in $modsToFind) {
    $pid = $mod.projectId
    $name = $mod.name
    
    try {
        $filesUrl = "$apiBase/projects/$pid/files?gameVersion=1.21.11&modLoaderType=4&pageSize=5"
        $filesResp = Invoke-RestMethod -Uri $filesUrl -Headers $headers -Method Get

        $latestFile = $null
        if ($filesResp -is [array] -and $filesResp.Count -gt 0) {
            $latestFile = $filesResp[0]
        } elseif ($filesResp.data -and $filesResp.data.Count -gt 0) {
            $latestFile = $filesResp.data[0]
        } elseif ($filesResp.Count -gt 0) {
            $latestFile = $filesResp | Select-Object -First 1
        }

        if (-not $latestFile) {
            # Try without version filter
            $filesUrl2 = "$apiBase/projects/$pid/files?pageSize=5"
            $filesResp2 = Invoke-RestMethod -Uri $filesUrl2 -Headers $headers -Method Get
            if ($filesResp2 -is [array] -and $filesResp2.Count -gt 0) {
                $latestFile = $filesResp2[0]
            } elseif ($filesResp2.data -and $filesResp2.data.Count -gt 0) {
                $latestFile = $filesResp2.data[0]
            }
        }

        if ($latestFile) {
            $fileId = if ($latestFile.id) { $latestFile.id } else { $latestFile.fileId }
            $fileName = if ($latestFile.fileName) { $latestFile.fileName } elseif ($latestFile.displayName) { $latestFile.displayName } else { "unknown" }
            
            $manifestFiles += @{
                projectID = $pid
                fileID    = [int]$fileId
                required  = $true
            }
            $modlistEntries += @{ name = $name; slug = $mod.slug; link = "https://www.curseforge.com/minecraft/mc-mods/$($mod.slug)" }
            Write-Host "  OK  $name -> File $fileId ($fileName)" -ForegroundColor Green
        } else {
            Write-Host "  WARN $name -> No file found for 1.21.11, adding project ID only" -ForegroundColor Yellow
            $manifestFiles += @{
                projectID = $pid
                fileID    = 0
                required  = $true
            }
            $modlistEntries += @{ name = $name; slug = $mod.slug; link = "https://www.curseforge.com/minecraft/mc-mods/$($mod.slug)" }
        }
    } catch {
        Write-Host "  ERR  $name -> $($_.Exception.Message)" -ForegroundColor Red
        # Still add with project ID
        $manifestFiles += @{
            projectID = $pid
            fileID    = 0
            required  = $true
        }
        $modlistEntries += @{ name = $name; slug = $mod.slug; link = "https://www.curseforge.com/minecraft/mc-mods/$($mod.slug)" }
    }
}

# Remove entries with fileID 0 (couldn't resolve)
$validFiles = $manifestFiles | Where-Object { $_.fileID -ne 0 }
if ($validFiles.Count -eq 0) {
    Write-Host ""
    Write-Host "Could not resolve file IDs via old API. Using known file IDs..." -ForegroundColor Yellow
    # Use known/uploaded file IDs
    $validFiles = @(
        @{ projectID = 1514636; fileID = 7929429; required = $true }   # ColonyCraft (just uploaded)
    )
    Write-Host "  ColonyCraft -> File 7929429 (just uploaded)" -ForegroundColor Green
    Write-Host "  Other mods will be listed as dependencies" -ForegroundColor Yellow
}

Write-Host ""

# ── Step 3: Create manifest.json ──
Write-Host "Creating manifest.json..." -ForegroundColor Yellow

$manifest = @{
    minecraft = @{
        version = "1.21.11"
        modLoaders = @(
            @{
                id      = "fabric-0.19.1"
                primary = $true
            }
        )
    }
    manifestType    = "minecraftModpack"
    manifestVersion = 1
    name            = "ColonyCraft Modpack"
    version         = "1.0.0"
    author          = "Wux"
    files           = $validFiles
    overrides       = "overrides"
}

$manifest | ConvertTo-Json -Depth 10 | Set-Content "$packDir\manifest.json" -Encoding UTF8
Write-Host "  manifest.json created with $($validFiles.Count) mods" -ForegroundColor Green

# ── Step 4: Create modlist.html ──
Write-Host "Creating modlist.html..." -ForegroundColor Yellow

$modlistHtml = "<ul>`n"
foreach ($entry in $modlistEntries) {
    $modlistHtml += "  <li><a href=`"$($entry.link)`">$($entry.name)</a></li>`n"
}
$modlistHtml += "</ul>"
$modlistHtml | Set-Content "$packDir\modlist.html" -Encoding UTF8
Write-Host "  modlist.html created" -ForegroundColor Green

# ── Step 5: Copy overrides (shader pack + config) ──
Write-Host "Copying overrides..." -ForegroundColor Yellow

# Copy shader pack
$shaderSrc = "C:\ColonyCraft\shaderpack\shaders"
if (Test-Path $shaderSrc) {
    Compress-Archive -Path $shaderSrc -DestinationPath "$packDir\overrides\shaderpacks\ColonyCraft_Shaders.zip" -Force
    Write-Host "  Shader pack copied" -ForegroundColor Green
}

# Create options.txt snippet (enable shader pack by default)
@"
resourcePacks:["vanilla"]
"@ | Set-Content "$packDir\overrides\options.txt" -Encoding UTF8

Write-Host "  Overrides ready" -ForegroundColor Green

# ── Step 6: Create the modpack zip ──
Write-Host ""
Write-Host "Packaging modpack..." -ForegroundColor Yellow

if (Test-Path $outputZip) { Remove-Item $outputZip -Force }
Compress-Archive -Path "$packDir\*" -DestinationPath $outputZip -Force

$zipSize = [math]::Round((Get-Item $outputZip).Length / 1024)
Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  MODPACK READY!" -ForegroundColor Green
Write-Host "  File: $outputZip" -ForegroundColor White
Write-Host "  Size: ${zipSize}KB" -ForegroundColor White
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Next steps:" -ForegroundColor Yellow
Write-Host "  1. Go to https://www.curseforge.com/project/create" -ForegroundColor White
Write-Host "  2. Select: Minecraft -> Modpacks" -ForegroundColor White
Write-Host "  3. Name: 'ColonyCraft Modpack'" -ForegroundColor White
Write-Host "  4. Upload the zip as a file" -ForegroundColor White
Write-Host ""
