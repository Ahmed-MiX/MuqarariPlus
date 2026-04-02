$file = "src\main\java\com\muqarariplus\platform\config\DatabaseSeeder.java"
$lines = Get-Content $file -Encoding UTF8
$trimmed = $lines[0..1065]
Set-Content $file -Value $trimmed -Encoding UTF8
Write-Host "Trimmed from $($lines.Count) to $($trimmed.Count) lines"
