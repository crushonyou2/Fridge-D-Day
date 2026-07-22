param(
    [string]$RunName = "current",
    [string]$BaselineCsv = "",
    [string]$TargetFailureType = "",
    [int]$SampleLimit = 0,
    [ValidateRange(0, 1825)]
    [int]$EvaluationOffsetDays = 0,
    [string]$DatasetDir = "qa-private\ocr-benchmark",
    [switch]$ReleaseBaseline,
    [switch]$OverwriteResult,
    [switch]$ValidateOnly
)

$ErrorActionPreference = "Stop"
if ($RunName -notmatch "^[A-Za-z0-9_-]+$") { throw "RunName may contain only letters, numbers, '_' and '-': $RunName" }
if ($TargetFailureType -and -not $BaselineCsv) { throw "TargetFailureType requires BaselineCsv." }
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
$baselinePath = if ($BaselineCsv) {
    if ([IO.Path]::IsPathRooted($BaselineCsv)) {
        [IO.Path]::GetFullPath($BaselineCsv)
    } else {
        [IO.Path]::GetFullPath((Join-Path $repoRoot $BaselineCsv))
    }
} else {
    ""
}
if ($baselinePath -and $baselinePath.Equals([IO.Path]::GetFullPath($resultPath), [StringComparison]::OrdinalIgnoreCase)) {
    throw "BaselineCsv and the result path are the same file: $resultPath"
}
if ($baselinePath -and -not (Test-Path -LiteralPath $baselinePath -PathType Leaf)) {
    throw "Missing regression baseline: $baselinePath"
}

if (-not (Test-Path -LiteralPath $manifestPath)) {
    throw "Missing private manifest: $manifestPath. Copy docs/qa/manifest.example.csv and add local images."
}

$samples = @(Import-Csv -LiteralPath $manifestPath)
if ($samples.Count -eq 0) { throw "The private manifest has no samples." }
$requiredColumns = @(
    "sample_id",
    "image_file",
    "expected_date",
    "lighting",
    "orientation",
    "material",
    "date_format",
    "base_rotation",
    "evaluation_date"
)
$manifestColumns = @($samples[0].PSObject.Properties.Name)
$missingColumns = @($requiredColumns | Where-Object { $_ -notin $manifestColumns })
if ($missingColumns) { throw "Manifest is missing required columns: $($missingColumns -join ', ')" }

$duplicates = $samples | Group-Object sample_id | Where-Object Count -gt 1
if ($duplicates) { throw "Duplicate sample_id: $($duplicates.Name -join ', ')" }
$duplicateImageFiles = $samples | Group-Object image_file | Where-Object Count -gt 1
if ($duplicateImageFiles) { throw "Duplicate image_file: $($duplicateImageFiles.Name -join ', ')" }

$imageHashes = @{}
foreach ($sample in $samples) {
    foreach ($column in $requiredColumns) {
        if ([string]::IsNullOrWhiteSpace($sample.$column)) {
            throw "Empty $column for $($sample.sample_id)."
        }
    }
    if ($sample.base_rotation -notin @("0", "90", "180", "270")) {
        throw "Invalid base_rotation for $($sample.sample_id): $($sample.base_rotation)"
    }
    try {
        [void][DateTime]::ParseExact($sample.expected_date, "yyyy-MM-dd", [Globalization.CultureInfo]::InvariantCulture)
        [void][DateTime]::ParseExact($sample.evaluation_date, "yyyy-MM-dd", [Globalization.CultureInfo]::InvariantCulture)
    } catch {
        throw "Dates must use YYYY-MM-DD for $($sample.sample_id): expected=$($sample.expected_date), evaluation=$($sample.evaluation_date)"
    }

    $imagePath = [IO.Path]::GetFullPath((Join-Path $privateRoot $sample.image_file))
    if (-not $imagePath.StartsWith(($privateRoot.TrimEnd('\') + '\'), [StringComparison]::OrdinalIgnoreCase)) {
        throw "image_file must stay inside DatasetDir for $($sample.sample_id): $($sample.image_file)"
    }
    if (-not (Test-Path -LiteralPath $imagePath)) { throw "Missing image for $($sample.sample_id): $imagePath" }
    $imageHashes[$sample.sample_id] = (Get-FileHash -LiteralPath $imagePath -Algorithm SHA256).Hash.ToLowerInvariant()
}

$duplicateHashes = @($imageHashes.GetEnumerator() | Group-Object Value | Where-Object Count -gt 1)
if ($duplicateHashes) {
    $duplicateIds = $duplicateHashes | ForEach-Object { ($_.Group.Name | Sort-Object) -join "/" }
    throw "Duplicate image content detected: $($duplicateIds -join ', ')"
}

$releaseColumns = @("print_quality", "independence_key", "cohort")
$missingReleaseColumns = @($releaseColumns | Where-Object { $_ -notin $manifestColumns })
if ($ReleaseBaseline -and $samples.Count -lt 50) {
    throw "A v1.1 release baseline requires at least 50 independent samples; found $($samples.Count)."
}
if ($ReleaseBaseline -and $missingReleaseColumns) {
    throw "A 50+ release baseline requires columns: $($missingReleaseColumns -join ', ')"
}
if (-not $missingReleaseColumns) {
    foreach ($sample in $samples) {
        foreach ($column in $releaseColumns) {
            if ([string]::IsNullOrWhiteSpace($sample.$column)) {
                if ($ReleaseBaseline) { throw "Empty $column for release sample $($sample.sample_id)." }
            }
        }
    }
    $duplicateIndependenceKeys = @($samples | Where-Object independence_key | Group-Object independence_key | Where-Object Count -gt 1)
    if ($duplicateIndependenceKeys) {
        throw "Non-independent samples share independence_key: $($duplicateIndependenceKeys.Name -join ', ')"
    }
}
if ($ReleaseBaseline) {
    $unspecifiedMaterial = @($samples | Where-Object { $_.material -match "unspecified|mixed_or" })
    if ($unspecifiedMaterial) {
        throw "A release baseline requires classified material; unresolved samples: $($unspecifiedMaterial.sample_id -join ', ')"
    }
    $existingCount = @($samples | Where-Object cohort -eq "existing_20").Count
    $newCount = @($samples | Where-Object cohort -eq "new_30_plus").Count
    $unsupportedCohorts = @($samples | Where-Object { $_.cohort -notin @("existing_20", "new_30_plus") })
    if ($existingCount -ne 20 -or $newCount -lt 30 -or $unsupportedCohorts) {
        throw "A release baseline requires cohort existing_20=20 and new_30_plus>=30; found existing_20=$existingCount new_30_plus=$newCount."
    }
}

$hasHangulDate = @($samples | Where-Object { $_.date_format -match "hangul|korean|년|월|일" }).Count -gt 0
$hasCompactDate = @($samples | Where-Object { $_.date_format -match "compact|yyyymmdd|yymmdd" }).Count -gt 0
Write-Host "[dataset] samples=$($samples.Count) unique_sample_ids=$($samples.Count) unique_images=$($imageHashes.Count) release_baseline=$ReleaseBaseline"
Write-Host "[coverage] hangul_date=$hasHangulDate compact_date=$hasCompactDate"
if ($samples.Count -lt 50) { Write-Warning "Fewer than 50 independent samples: validation is not a v1.1 release baseline." }
if (-not $ReleaseBaseline -and $samples.Count -ge 50 -and $missingReleaseColumns) {
    Write-Warning "This 50+ historical dataset lacks release columns ($($missingReleaseColumns -join ', ')); rerun with a classified Korean manifest and -ReleaseBaseline for v1.1."
}
if (-not $hasHangulDate) { Write-Warning "No Korean year/month/day date format is classified in the manifest." }
if (-not $hasCompactDate) { Write-Warning "No compact YYYYMMDD or YYMMDD date format is classified in the manifest." }
if ($ValidateOnly) {
    Write-Host "Dataset validation passed: $manifestPath"
    return
}
if ((Test-Path -LiteralPath $resultPath) -and -not $OverwriteResult) {
    throw "Result already exists; choose a new RunName or pass -OverwriteResult: $resultPath"
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
$evaluationScenario = if ($EvaluationOffsetDays -gt 0) { "expected_minus_${EvaluationOffsetDays}d" } else { "manifest" }
if ($EvaluationOffsetDays -gt 0) {
    $runnerArgs += "-Pandroid.testInstrumentationRunnerArguments.evaluationOffsetDays=$EvaluationOffsetDays"
}
$runStart = Get-Date
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
if ($generatedResult.LastWriteTime -le $runStart) {
    throw "The newest ocr-benchmark.csv predates this run ($runStart): $($generatedResult.FullName)"
}
Copy-Item -LiteralPath $generatedResult.FullName -Destination $resultPath -Force

function Show-Metrics([string]$CsvPath, [string]$Label) {
    $rows = @(Import-Csv -LiteralPath $CsvPath)
    $count = $rows.Count
    if ($count -eq 0) { throw "Benchmark result has no samples: $CsvPath" }
    $exact = @($rows | Where-Object exact_match -eq "true").Count
    $detected = @($rows | Where-Object detected -eq "true").Count
    $requiredResultColumns = @("expected_candidate_detected", "candidate_count", "failed_variant_count", "evaluation_scenario")
    $missingResultColumns = @($requiredResultColumns | Where-Object { $_ -notin $rows[0].PSObject.Properties.Name })
    if ($missingResultColumns) {
        throw "Benchmark CSV uses the legacy result schema; rerun it with the current instrumentation. Missing: $($missingResultColumns -join ', ')"
    }
    $expectedDetected = @($rows | Where-Object expected_candidate_detected -eq "true").Count
    $failedVariants = ($rows | Measure-Object failed_variant_count -Sum).Sum
    Write-Host "[$Label] samples=$count exact=$exact/$count ($([math]::Round(100 * $exact / $count, 2))%) any_candidate=$detected/$count ($([math]::Round(100 * $detected / $count, 2))%) expected_candidate=$expectedDetected/$count ($([math]::Round(100 * $expectedDetected / $count, 2))%) failed_variants=$failedVariants scenario=$($rows[0].evaluation_scenario)"
    if ($count -lt 50) { Write-Warning "Fewer than 50 samples: this run is not a v1.1 release baseline." }

    $topFailures = @(
        $rows |
            Where-Object failure_type |
            Group-Object failure_type |
            Sort-Object @{ Expression = "Count"; Descending = $true }, @{ Expression = "Name"; Ascending = $true } |
            Select-Object -First 3
    )
    Write-Host "  top_failure_types"
    if ($topFailures.Count -eq 0) {
        Write-Host "    none"
    } else {
        $topFailures | ForEach-Object { Write-Host "    $($_.Name): $($_.Count)" }
    }

    foreach ($dimension in @("failure_type", "cohort", "lighting", "orientation", "material", "date_format", "print_quality")) {
        Write-Host "  $dimension"
        $rows | Where-Object { $_.$dimension } | Group-Object -Property $dimension | Sort-Object Count -Descending | ForEach-Object {
            $failed = @($_.Group | Where-Object exact_match -ne "true").Count
            Write-Host "    $($_.Name): failures=$failed/$($_.Count) ($([math]::Round(100 * $failed / $_.Count, 2))%)"
        }
    }
    return [PSCustomObject]@{
        Count = $count
        Exact = $exact
        Detected = $detected
        ExpectedDetected = $expectedDetected
        SampleIds = @($rows.sample_id | Sort-Object)
        Rows = $rows
        TopFailureType = if ($topFailures.Count -gt 0) { $topFailures[0].Name } else { "" }
        EvaluationScenario = $rows[0].evaluation_scenario
    }
}

$current = Show-Metrics -CsvPath $resultPath -Label $RunName
if ($current.EvaluationScenario -ne $evaluationScenario) {
    throw "Instrumentation scenario mismatch: expected '$evaluationScenario', got '$($current.EvaluationScenario)'."
}
if ($BaselineCsv) {
    $baseline = Show-Metrics -CsvPath $baselinePath -Label "baseline"
    if ($baseline.Count -ne $current.Count) { throw "Regression comparison requires the same sample count." }
    if (Compare-Object $baseline.SampleIds $current.SampleIds) { throw "Regression comparison requires identical sample_id values." }
    $identityColumns = @(
        "image_sha256",
        "expected_date",
        "lighting",
        "orientation",
        "material",
        "date_format",
        "print_quality",
        "independence_key",
        "cohort",
        "evaluation_date",
        "evaluation_scenario",
        "base_rotation"
    )
    $currentById = @{}
    $current.Rows | ForEach-Object { $currentById[$_.sample_id] = $_ }
    foreach ($baselineRow in $baseline.Rows) {
        $currentRow = $currentById[$baselineRow.sample_id]
        foreach ($column in $identityColumns) {
            if ($column -notin $baselineRow.PSObject.Properties.Name -or $column -notin $currentRow.PSObject.Properties.Name) {
                throw "Regression CSV is missing identity column: $column"
            }
            if ($baselineRow.$column -ne $currentRow.$column) {
                throw "Regression input changed for $($baselineRow.sample_id): $column"
            }
        }
    }
    $exactRegressions = @(
        $baseline.Rows | Where-Object {
            $_.exact_match -eq "true" -and $currentById[$_.sample_id].exact_match -ne "true"
        } | Select-Object -ExpandProperty sample_id
    )
    if ($exactRegressions) {
        throw "Per-sample exact-match regression: $($exactRegressions -join ', ')"
    }
    $candidateRegressions = @(
        $baseline.Rows | Where-Object {
            $_.expected_candidate_detected -eq "true" -and
                $currentById[$_.sample_id].expected_candidate_detected -ne "true"
        } | Select-Object -ExpandProperty sample_id
    )
    if ($candidateRegressions) {
        throw "Per-sample expected-candidate regression: $($candidateRegressions -join ', ')"
    }
    if ($current.Exact -lt $baseline.Exact) { throw "Exact-match regression: $($baseline.Exact) -> $($current.Exact)" }
    if ($current.ExpectedDetected -lt $baseline.ExpectedDetected) {
        throw "Expected-candidate regression: $($baseline.ExpectedDetected) -> $($current.ExpectedDetected)"
    }
    if ($TargetFailureType) {
        if ($TargetFailureType -ne $baseline.TopFailureType) {
            throw "TargetFailureType must be the baseline's largest failure type: expected '$($baseline.TopFailureType)', got '$TargetFailureType'."
        }
        $baselineTargetCount = @($baseline.Rows | Where-Object failure_type -eq $TargetFailureType).Count
        $currentTargetCount = @($current.Rows | Where-Object failure_type -eq $TargetFailureType).Count
        if ($currentTargetCount -ge $baselineTargetCount) {
            throw "Target failure type did not improve: $TargetFailureType $baselineTargetCount -> $currentTargetCount"
        }
        Write-Host "[target-improvement] $TargetFailureType=$baselineTargetCount->$currentTargetCount"
    }
    Write-Host "[delta] exact=$($current.Exact - $baseline.Exact) any_candidate=$($current.Detected - $baseline.Detected) expected_candidate=$($current.ExpectedDetected - $baseline.ExpectedDetected)"
}

Write-Host "Private result saved: $resultPath"
