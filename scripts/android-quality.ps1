param(
    [string[]]$Tasks = @("testDebugUnitTest", "lintDebug", "assembleDebug"),
    [string[]]$GradleArgs = @(),
    [ValidatePattern("^[A-Z]$")]
    [string]$DriveLetter = "Q"
)

$ErrorActionPreference = "Stop"
$repoRoot = Split-Path -Parent $PSScriptRoot
$drive = "${DriveLetter}:"
$createdMapping = $false
$existingMapping = subst.exe | Where-Object { $_ -like "$drive\:*" }

if ($existingMapping) {
    throw "$drive is already used by subst. Choose another -DriveLetter."
}

subst.exe $drive $repoRoot
if ($LASTEXITCODE -ne 0) { throw "Failed to map $repoRoot to $drive" }
$createdMapping = $true

try {
    Push-Location "$drive\"
    try {
        & .\gradlew.bat @Tasks @GradleArgs --no-daemon --console=plain
        if ($LASTEXITCODE -ne 0) { throw "Android quality gate failed." }
    } finally {
        Pop-Location
    }
} finally {
    if ($createdMapping) { subst.exe $drive /D }
}
