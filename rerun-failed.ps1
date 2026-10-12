# Re-run 9 failed TCs
$BaseDir = "c:\Users\khangld10\Documents\APP\AI-MACAI2\.kiro\Automation selenium java\Selenium-java-master"

# 9 TCs fail
$FailedTCs = @("TC13", "TC14", "TC15", "TC16", "TC17", "TC18", "TC19", "TC20", "TC21")

Write-Host "=" * 80
Write-Host "RE-RUNNING $($FailedTCs.Count) FAILED TCs"
Write-Host "=" * 80
Write-Host ""

# Create testng-rerun-failed.xml
$TestngContent = @"
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE suite SYSTEM "https://testng.org/testng-1.0.dtd">
<suite name="Rerun Failed TCs" verbose="1">
    <listeners>
        <listener class-name="listeners.TestListener"/>
        <listener class-name="utils.DashboardReportGenerator"/>
    </listeners>
    <test name="Failed TCs Rerun">
        <classes>
"@

foreach ($tc in $FailedTCs) {
    $TestngContent += "            <class name=`"testcases.uat.Mudsku.$tc`"/>`n"
}

$TestngContent += @"
        </classes>
    </test>
</suite>
"@

$TestngPath = Join-Path $BaseDir "testng-rerun-failed.xml"
$TestngContent | Out-File -FilePath $TestngPath -Encoding utf8 -NoNewline

Write-Host "✅ Created: $TestngPath"
Write-Host ""
Write-Host "Running tests..."
Write-Host ""

# Run Maven test
Set-Location $BaseDir
mvn clean test "-Dsurefire.suiteXmlFiles=testng-rerun-failed.xml"

Write-Host ""
Write-Host "=" * 80
Write-Host "REPORT LOCATIONS"
Write-Host "=" * 80
Write-Host ""
Write-Host "📊 Extent Report: $BaseDir\test-output\ExtentReport.html"
Write-Host "📈 Dashboard:     $BaseDir\test-output\dashboard.html"
Write-Host ""
Write-Host "Open in browser to view updated results!"
Write-Host "=" * 80
