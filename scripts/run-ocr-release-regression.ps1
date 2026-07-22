param(
    [Parameter(Mandatory = $true)]
    [string]$RunNamePrefix,

    [string]$DatasetDir = "qa-private\korean-labels-55",

    [string]$D30BaselineCsv = "qa-private\results\korean-labels-55-v2-d30.csv",

    [string]$D180BaselineCsv = "qa-private\results\korean-labels-55-v2-d180.csv",

    [string]$TargetFailureType = "",

    [switch]$OverwriteResults
)

$ErrorActionPreference = "Stop"
if ($RunNamePrefix -notmatch "^[A-Za-z0-9_-]+$") {
    throw "RunNamePrefix may contain only letters, numbers, '_' and '-': $RunNamePrefix"
}

$benchmarkScript = Join-Path $PSScriptRoot "run-ocr-benchmark.ps1"
$commonArguments = @{
    DatasetDir = $DatasetDir
    ReleaseBaseline = $true
}
if ($OverwriteResults) {
    $commonArguments.OverwriteResult = $true
}

$d30Arguments = $commonArguments.Clone()
$d30Arguments["RunName"] = "$RunNamePrefix-d30"
$d30Arguments["EvaluationOffsetDays"] = 30
$d30Arguments["BaselineCsv"] = $D30BaselineCsv
if ($TargetFailureType) {
    $d30Arguments["TargetFailureType"] = $TargetFailureType
}
& $benchmarkScript @d30Arguments

$d180Arguments = $commonArguments.Clone()
$d180Arguments["RunName"] = "$RunNamePrefix-d180"
$d180Arguments["EvaluationOffsetDays"] = 180
$d180Arguments["BaselineCsv"] = $D180BaselineCsv
& $benchmarkScript @d180Arguments

Write-Host "Release regression passed for D-30 and D-180: $RunNamePrefix"
