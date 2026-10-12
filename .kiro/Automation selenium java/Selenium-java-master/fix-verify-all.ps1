# Script replace verify prompt - simple string replace

$edits = @(
    @{file="TC2.java"; name="Mua 2 tang 1 - Dau an Nep Cai Hoa Vang"},
    @{file="TC3.java"; name="Mua 2 tang 1 - Kem Danh Rang Colgate Muoi"},
    @{file="TC4.java"; name="Mua 2 tang 1 - Sua Rua Mat Simple Lam Sach Sau"},
    @{file="TC5.java"; name="Mua 1 tang 1 - Keo Chewies Raspberry"},
    @{file="TC6.java"; name="Mua 1 tang 1 - Keo Chewies Blueberry"},
    @{file="TC7.java"; name="Mua 1 tang 1 - Bong Tay Trang Simply Organic 160"},
    @{file="TC8.java"; name="Mua 1 tang 1 - Bong Tay Trang 2 lop Simply Organic"},
    @{file="TC9.java"; name="Mua 1 tang 1 - Bong Tay Trang 3 lop Simply Organic"},
    @{file="TC10.java"; name="Giam gia 30% - Keo deo Vitamin C Minamax"},
    @{file="TC11.java"; name="Giam gia 30% - Vien uong tang de khang Chela"},
    @{file="TC12.java"; name="Giam gia 30% - Siro Bo Phe Labebe"},
    @{file="TC13.java"; name="Giam gia 30% - Sua rua mat Simple Kind To Skin"},
    @{file="TC14.java"; name="Giam gia 30% - Vien uong Actiso Extra"},
    @{file="TC15.java"; name="Giam gia 30% - Vitamin tong hop Good Health"},
    @{file="TC16.java"; name="Giam gia 30% - Vien uong Ho Tro Giam Can"},
    @{file="TC17.java"; name="Giam gia 30% - Vien Uong Best Collagen"},
    @{file="TC18.java"; name="Giam gia 30% - Vien Uong DHA Ho Tro Nao Bo"},
    @{file="TC19.java"; name="Giam gia 30% - Vitamin e 1000 IU Blackmores"},
    @{file="TC20.java"; name="Giam gia 50% - Nuoc uong bo sung sat Phargold"},
    @{file="TC21.java"; name="Giam gia 50% - Dau an Nep Cai Hoa Vang"},
    @{file="TC22.java"; name="Giam gia 50% - Mat Na Simple Cho Da Nhay Cam"},
    @{file="TC23.java"; name="Giam gia 50% - Bong Tay Trang 2 lop Simply Organic"},
    @{file="TC24.java"; name="Giam gia 50% - Sua Tam Romano Deluxe"},
    @{file="TC25new.java"; name="Giam gia 50% - Dung dich ve sinh phu nu"},
    @{file="TC26.java"; name="Qua tang - Bong tay trang Simply Organic 80"},
    @{file="TC27.java"; name="Qua tang - Keo Chewies Strawberry"},
    @{file="TC28.java"; name="Qua tang - Nuoc suc mieng P/S Fresh Breath"},
    @{file="TC29.java"; name="Qua tang - Bong tay trang Simply Organic 160"},
    @{file="TC30.java"; name="Qua tang - Kem Danh Rang Colgate Muoi"}
)

$oldPattern = "promotionModal.findElement(By.xpath(`".//*[contains(normalize-space(.),'" + " + 'PROMOTION_CODE' + " + "')]`"));"
$folder = "c:\Users\khangld10\Documents\APP\AI-MACAI2\.kiro\Automation selenium java\Selenium-java-master\src\test\java\testcases\uat\Mudsku"

foreach ($item in $edits) {
    $filePath = Join-Path $folder $item.file
    if (!(Test-Path $filePath)) {
        Write-Host "Skip: $($item.file)"
        continue
    }

    $content = Get-Content $filePath -Raw
    $name = $item.name

    $newPattern = "// Verify ca ma KM va ten co cau trong danh sach khuyen mai`r`n" +
                  "            promotionModal.findElement(By.xpath(`".//*[contains(.,'`" + PROMOTION_CODE + `"') or contains(.,'$name')]`"));"

    if ($content -match [regex]::Escape($oldPattern)) {
        $content = $content.Replace($oldPattern, $newPattern)
        Set-Content $filePath -Value $content -NoNewline
        Write-Host "OK $($item.file)"
    } else {
        Write-Host "⚠️ Skip $($item.file) (pattern mismatch)"
    }
}

Write-Host "`nDone!"
