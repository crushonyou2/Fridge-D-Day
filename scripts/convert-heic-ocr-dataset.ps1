param(
    [Parameter(Mandatory = $true)]
    [string]$SourceDir,

    [Parameter(Mandatory = $true)]
    [string]$OutputDir,

    [ValidateRange(512, 8192)]
    [int]$MaxLongEdge = 2048,

    [ValidateRange(0, 31)]
    [int]$JpegQuality = 2,

    [ValidateSet("baseline-v1-width", "max-long-edge-v2")]
    [string]$ConversionProfile = "max-long-edge-v2"
)

$ErrorActionPreference = "Stop"

$sourceRoot = [IO.Path]::GetFullPath($SourceDir)
$outputRoot = [IO.Path]::GetFullPath($OutputDir)
$metadataRoot = Split-Path -Parent $outputRoot
if ($sourceRoot -eq $outputRoot) {
    throw "SourceDir and OutputDir must be different so the HEIC originals are preserved."
}

$heifConvert = (Get-Command heif-convert -ErrorAction Stop).Source
$ffmpeg = (Get-Command ffmpeg -ErrorAction Stop).Source
$sourceFiles = @(Get-ChildItem -LiteralPath $sourceRoot -File | Where-Object Extension -Match '^\.heic$' | Sort-Object Name)
if ($sourceFiles.Count -eq 0) { throw "No HEIC files found in $sourceRoot" }

$plans = @(
    foreach ($source in $sourceFiles) {
        $safeBaseName = [regex]::Replace($source.BaseName, '[^A-Za-z0-9_-]', '_')
        [pscustomobject]@{
            source = $source
            safe_base_name = $safeBaseName
            output_name = $safeBaseName + ".jpg"
        }
    }
)
$outputNameCollisions = @($plans | Group-Object output_name | Where-Object Count -gt 1)
if ($outputNameCollisions) {
    $details = $outputNameCollisions | ForEach-Object {
        "$($_.Name) <= $(($_.Group.source.Name | Sort-Object) -join ', ')"
    }
    throw "Normalized output filename collision: $($details -join '; ')"
}

$scaleFilter = switch ($ConversionProfile) {
    "baseline-v1-width" {
        "scale=${MaxLongEdge}:-2:force_original_aspect_ratio=decrease"
    }
    "max-long-edge-v2" {
        "scale=w='min(${MaxLongEdge},iw)':h='min(${MaxLongEdge},ih)':force_original_aspect_ratio=decrease:force_divisible_by=2"
    }
}

New-Item -ItemType Directory -Force -Path $outputRoot | Out-Null
$tempRoot = Join-Path ([IO.Path]::GetTempPath()) ("fridge-ocr-heic-" + [guid]::NewGuid().ToString("N"))
New-Item -ItemType Directory -Path $tempRoot | Out-Null

try {
    $rows = foreach ($plan in $plans) {
        $source = $plan.source
        $safeBaseName = $plan.safe_base_name
        $fullJpeg = Join-Path $tempRoot ($safeBaseName + ".jpg")
        $outputName = $plan.output_name
        $outputPath = Join-Path $outputRoot $outputName

        & $heifConvert --quiet -q 95 $source.FullName $fullJpeg
        if ($LASTEXITCODE -ne 0) { throw "heif-convert failed for $($source.Name)" }

        & $ffmpeg -hide_banner -loglevel error -y -i $fullJpeg `
            -vf $scaleFilter `
            -q:v $JpegQuality $outputPath
        if ($LASTEXITCODE -ne 0) { throw "ffmpeg failed for $($source.Name)" }

        [pscustomobject]@{
            source_file = $source.Name
            source_sha256 = (Get-FileHash -Algorithm SHA256 -LiteralPath $source.FullName).Hash.ToLowerInvariant()
            output_file = $outputName
            output_sha256 = (Get-FileHash -Algorithm SHA256 -LiteralPath $outputPath).Hash.ToLowerInvariant()
            max_long_edge = $MaxLongEdge
            heif_jpeg_quality = 95
            ffmpeg_jpeg_quality = $JpegQuality
            conversion_profile = $ConversionProfile
        }
    }

    $rows | Export-Csv -LiteralPath (Join-Path $metadataRoot "conversion-manifest.csv") -NoTypeInformation -Encoding utf8
    @(
        "converted_at=$([DateTimeOffset]::Now.ToString('o'))"
        "heif_convert=$(& $heifConvert --version 2>&1 | Select-Object -First 1)"
        "ffmpeg=$(& $ffmpeg -version 2>&1 | Select-Object -First 1)"
        "source_count=$($sourceFiles.Count)"
        "max_long_edge=$MaxLongEdge"
        "heif_jpeg_quality=95"
        "ffmpeg_jpeg_quality=$JpegQuality"
        "conversion_profile=$ConversionProfile"
    ) | Set-Content -LiteralPath (Join-Path $metadataRoot "conversion-metadata.txt") -Encoding utf8
} finally {
    if (Test-Path -LiteralPath $tempRoot) {
        Remove-Item -LiteralPath $tempRoot -Recurse -Force
    }
}

Write-Host "Converted $($sourceFiles.Count) HEIC files into $outputRoot"
