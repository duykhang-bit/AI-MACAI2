# AI Native Client — Cài đặt MCP config + Steering files (global)
# Chạy: powershell -ExecutionPolicy Bypass -File install.ps1
# Hoặc: Right-click → Run with PowerShell

$ErrorActionPreference = "Stop"

Write-Host "`n Cai dat AI Context Engine cho Kiro + Cursor...`n" -ForegroundColor Cyan

# --- Kiro MCP config ---
$KiroMcpDir = Join-Path $env:USERPROFILE ".kiro\settings"
$KiroMcpFile = Join-Path $KiroMcpDir "mcp.json"

if (-not (Test-Path $KiroMcpDir)) {
    New-Item -ItemType Directory -Path $KiroMcpDir -Force | Out-Null
}

if (Test-Path $KiroMcpFile) {
    Write-Host "[!] Kiro MCP config da ton tai: $KiroMcpFile" -ForegroundColor Yellow
    Write-Host "    -> Ban can tu them block 'ai-context-engine-company' vao file nay."
    Write-Host "    -> Xem mau: mcp.kiro.example.json"
} else {
    Copy-Item "mcp.kiro.example.json" $KiroMcpFile
    Write-Host "[OK] Kiro MCP config da tao: $KiroMcpFile" -ForegroundColor Green
    Write-Host "     -> Mo file va dien tokens cua ban."
}

# --- Cursor MCP config ---
$CursorMcpDir = Join-Path $env:USERPROFILE ".cursor"
$CursorMcpFile = Join-Path $CursorMcpDir "mcp.json"

if (-not (Test-Path $CursorMcpDir)) {
    New-Item -ItemType Directory -Path $CursorMcpDir -Force | Out-Null
}

if (Test-Path $CursorMcpFile) {
    Write-Host "[!] Cursor MCP config da ton tai: $CursorMcpFile" -ForegroundColor Yellow
    Write-Host "    -> Ban can tu them block 'ai-context-engine-company' vao file nay."
    Write-Host "    -> Xem mau: mcp.cursor.example.json"
} else {
    Copy-Item "mcp.cursor.example.json" $CursorMcpFile
    Write-Host "[OK] Cursor MCP config da tao: $CursorMcpFile" -ForegroundColor Green
    Write-Host "     -> Mo file va dien tokens cua ban."
}

# --- Kiro Steering files ---
$SteeringDir = Join-Path $env:USERPROFILE ".kiro\steering"

if (-not (Test-Path $SteeringDir)) {
    New-Item -ItemType Directory -Path $SteeringDir -Force | Out-Null
}

Copy-Item ".kiro\steering\*.md" $SteeringDir -Force
Write-Host "[OK] Kiro steering files da copy vao: $SteeringDir" -ForegroundColor Green

# --- Cursor Rules files ---
$CursorRulesDir = Join-Path $env:USERPROFILE ".cursor\rules"

if (-not (Test-Path $CursorRulesDir)) {
    New-Item -ItemType Directory -Path $CursorRulesDir -Force | Out-Null
}

Copy-Item ".cursor\rules\*.mdc" $CursorRulesDir -Force
Write-Host "[OK] Cursor rules da copy vao: $CursorRulesDir" -ForegroundColor Green

Write-Host ""
Write-Host "Antigravity: steering trong repo tai .agents/rules/ (workspace rules)." -ForegroundColor Cyan
Write-Host "  -> Mo project chua repo nay hoac copy .agents/rules vao git root project khac."
Write-Host "  -> Antigravity: Customizations -> Rules — bat Always On cho tung file (*-part*.md la mot bo)."

Write-Host ""
Write-Host "Viec can lam tiep:" -ForegroundColor Cyan
Write-Host "   1. Mo %USERPROFILE%\.kiro\settings\mcp.json (hoac %USERPROFILE%\.cursor\mcp.json)"
Write-Host "   2. Dien tokens: X-API-Key, X-Member-Id, X-GitLab-Token, X-Confluence-Token, X-Jira-Token"
Write-Host "   3. Restart IDE"
Write-Host "   4. Thu: 'Tim code lien quan den payment flow trong chain VAC'"
Write-Host ""
Write-Host "Khi team cap nhat steering files moi:" -ForegroundColor Cyan
Write-Host "   cd ai-native-client; git pull; powershell -ExecutionPolicy Bypass -File install.ps1"
Write-Host ""
Write-Host "Done!" -ForegroundColor Green
