param(
    [string]$RunName = "current",
    [string]$BaselineCsv = "",
    [int]$SampleLimit = 0,
    [string]$DatasetDir = "qa-private\ocr-benchmark"
)

$ErrorActionPreference = "Stop"
$repoRoot = Split-Path -Parent $PSScriptRoot
$privateRoot = if ([IO.Path]::IsPathRooted($DatasetDir)) {
    [IO.Path]::GetFullPath($DatasetDir)
} else {
    [IO.Path]::GetFullPath((Join-Path $repoRoot $DatasetDir))
}
$repoPrefix = [IO.Path]::GetFullPath($repoRoot).TrimEnd('\') + '\'
if (-not $privateRoot.StartsWith($repoPrefix, [StringComparison]::OrdinalIgnoreCase)) {
    throw "DatasetDir must stay inside the repository so Android test assets remain local: $privateRoot"
}
$datasetRelative = [IO.Path]::GetRelativePath($repoRoot, $privateRoot).Replace('\', '/')
$manifestPath = Join-Path $privateRoot "manifest.csv"
$resultDir = Join-Path $repoRoot "qa-private\results"
$resultPath = Join-Path $resultDir "$RunName.csv"

if (-not (Test-Path -LiteralPath $manifestPath)) {
    throw "Missing private manifest: $manifestPath. Copy docs/qa/manifest.example.csv and add local images."
}

$samples = @(Import-Csv -LiteralPath $manifestPath)
if ($samples.Count -eq 0) { throw "The private manifest has no samples." }
$duplicates = $samples | Group-Object sample_id | Where-Object Count -gt 1
if ($duplicates) { throw "Duplicate sample_id: $($duplicates.Name -join ', ')" }
foreach ($sample in $samples) {
    $imagePath = Join-Path $privateRoot $sample.image_file
    if (-not (Test-Path -LiteralPath $imagePath)) { throw "Missing image for $($sample.sample_id): $imagePath" }
}

$localProperties = Get-Content -LiteralPath (Join-Path $repoRoot "local.properties") -Raw
$sdkLine = ($localProperties -split "`r?`n" | Where-Object { $_ -like "sdk.dir=*" } | Select-Object -First 1)
if (-not $sdkLine) { throw "local.properties must define sdk.dir." }
$sdkDir = $sdkLine.Substring("sdk.dir=".Length).Replace("\:", ":").Replace("\\", "\")
$adb = Join-Path $sdkDir "platform-tools\adb.exe"
if (-not (Test-Path -LiteralPath $adb)) { throw "adb not found: $adb" }

$devices = & $adb devices
$connected = @($devices | Select-Object -Skip 1 | Where-Object { $_ -match "\sdevice$" })
if ($connected.Count -ne 1) { throw "Connect exactly one Android device or emulator. Found: $($connected.Count)." }

$runnerArgs = @(
    "-PocrBenchmarkDir=$datasetRelative",
    "-Pandroid.testInstrumentationRunnerArguments.class=app.fridgedday.util.ocr.OcrBenchmarkTest"
)
if ($SampleLimit -gt 0) {
    $runnerArgs += "-Pandroid.testInstrumentationRunnerArguments.sampleLimit=$SampleLimit"
}
& (Join-Path $PSScriptRoot "android-quality.ps1") `
    -Tasks @("connectedDebugAndroidTest") `
    -GradleArgs $runnerArgs

New-Item -ItemType Directory -Force -Path $resultDir | Out-Null
$androidTestOutput = Join-Path $repoRoot "app\build\outputs\connected_android_test_additional_output\debugAndroidTest\connected"
$generatedResult = Get-ChildItem -LiteralPath $androidTestOutput -Recurse -Filter "ocr-benchmark.csv" |
    Sort-Object LastWriteTime -Descending |
    Select-Object -First 1
if (-not $generatedResult) {
    throw "Could not find ocr-benchmark.csv in Android test additional output: $androidTestOutput"
}
Copy-Item -LiteralPath $generatedResult.FullName -Destination $resultPath -Force

function Show-Metrics([string]$CsvPath, [string]$Label) {
    $rows = @(Import-Csv -LiteralPath $CsvPath)
    $count = $rows.Count
    if ($count -eq 0) { throw "Benchmark result has no samples: $CsvPath" }
    $exact = @($rows | Where-Object exact_match -eq "true").Count
    $detected = @($rows | Where-Object detected -eq "true").Count
    Write-Host "[$Label] samples=$count exact=$exact/$count ($([math]::Round(100 * $exact / $count, 2))%) detected=$detected/$count ($([math]::Round(100 * $detected / $count, 2))%)"
    if ($count -lt 50) { Write-Warning "Fewer than 50 samples: this run is not a v1.1 release baseline." }

    foreach ($dimension in @("failure_type", "lighting", "orientation", "material", "date_format")) {
        Write-Host "  $dimension"
        $rows | Where-Object { $_.$dimension } | Group-Object -Property $dimension | Sort-Object Count -Descending | ForEach-Object {
            $failed = @($_.Group | Where-Object exact_match -ne "true").Count
            Write-Host "    $($_.Name): failures=$failed/$($_.Count) ($([math]::Round(100 * $failed / $_.Count, 2))%)"
        }
    }
    return [PSCustomObject]@{ Count = $count; Exact = $exact; Detected = $detected; SampleIds = @($rows.sample_id | Sort-Object) }
}

$current = Show-Metrics -CsvPath $resultPath -Label $RunName
if ($BaselineCsv) {
    $baselinePath = if ([IO.Path]::IsPathRooted($BaselineCsv)) { $BaselineCsv } else { Join-Path $repoRoot $BaselineCsv }
    $baseline = Show-Metrics -CsvPath $baselinePath -Label "baseline"
    if ($baseline.Count -ne $current.Count) { throw "Regression comparison requires the same sample count." }
    if (Compare-Object $baseline.SampleIds $current.SampleIds) { throw "Regression comparison requires identical sample_id values." }
    Write-Host "[delta] exact=$($current.Exact - $baseline.Exact) detected=$($current.Detected - $baseline.Detected)"
}

Write-Host "Private result saved: $resultPath"
