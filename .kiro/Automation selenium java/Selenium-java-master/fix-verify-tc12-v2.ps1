# Script sua TC12 verify: them ten co cau vao verify xpath

$mapping = @{
    "TC1.java"     = @{ name="Mua 2 tang 1 - Nuoc mat nhan tao Easimist Eye Drops 5ml" }
    "TC2.java"     = @{ name="Mua 2 tang 1 - Dau an Nep Cai Hoa Vang 2L" }
    "TC3.java"     = @{ name="Mua 2 tang 1 - Kem Danh Rang Colgate Muoi 225g" }
    "TC4.java"     = @{ name="Mua 2 tang 1 - Sua Rua Mat Simple Lam Sach Sau 150ml" }
    "TC5.java"     = @{ name="Mua 1 tang 1 - Keo Chewies Raspberry 15g" }
    "TC6.java"     = @{ name="Mua 1 tang 1 - Keo Chewies Blueberry 15g" }
    "TC7.java"     = @{ name="Mua 1 tang 1 - Bong Tay Trang Simply Organic 160 mieng" }
    "TC8.java"     = @{ name="Mua 1 tang 1 - Bong Tay Trang 2 lop Simply Organic 300 mieng" }
    "TC9.java"     = @{ name="Mua 1 tang 1 - Bong Tay Trang 3 lop Simply Organic 300 mieng" }
    "TC10.java"    = @{ name="Giam gia 30% - Keo deo Vitamin C Minamax" }
    "TC11.java"    = @{ name="Giam gia 30% - Vien uong tang de khang Chela Vit C 550" }
    "TC12.java"    = @{ name="Giam gia 30% - Siro Bo Phe Labebe 120ml" }
    "TC13.java"    = @{ name="Giam gia 30% - Sua rua mat Simple Kind To Skin Refreshing Facial Wash Gel 150ml" }
    "TC14.java"    = @{ name="Giam gia 30% - Vien uong Actiso Extra" }
    "TC15.java"    = @{ name="Giam gia 30% - Vitamin tong hop Good Health Viralex Attack 60 vien" }
    "TC16.java"    = @{ name="Giam gia 30% - Vien uong Ho Tro Giam Can Natures Way Sculpt" }
    "TC17.java"    = @{ name="Giam gia 30% - Vien Uong Best Collagen Absolute Gold" }
    "TC18.java"    = @{ name="Giam gia 30% - Vien Uong DHA Ho Tro Nao Bo Blackmores" }
    "TC19.java"    = @{ name="Giam gia 30% - Vitamin e 1000 IU Blackmores 30 vien" }
    "TC20.java"    = @{ name="Giam gia 50% - Nuoc uong bo sung sat Phargold 125ml" }
    "TC21.java"    = @{ name="Giam gia 50% - Dau an Nep Cai Hoa Vang 2L" }
    "TC22.java"    = @{ name="Giam gia 50% - Mat Na Simple Cho Da Nhay Cam Sang Da Vitamin C Glow-Boost Sheet Mask 21ml" }
    "TC23.java"    = @{ name="Giam gia 50% - Bong Tay Trang 2 lop Simply Organic 300 mieng" }
    "TC24.java"    = @{ name="Giam gia 50% - Sua Tam Romano Deluxe 650g" }
    "TC25new.java" = @{ name="Giam gia 50% - Dung dich ve sinh phu nu Natural Feminine La Trau Khong 100ml" }
    "TC26.java"    = @{ name="Qua tang - Bong tay trang Simply Organic 80 mieng" }
    "TC27.java"    = @{ name="Qua tang - Keo Chewies Strawberry 15g" }
    "TC28.java"    = @{ name="Qua tang - Nuoc suc mieng P/S Fresh Breath 500ml" }
    "TC29.java"    = @{ name="Qua tang - Bong tay trang Simply Organic 160 mieng" }
    "TC30.java"    = @{ name="Qua tang - Kem Danh Rang Colgate Muoi 225g" }
}

$folder = "c:\Users\khangld10\Documents\APP\AI-MACAI2\.kiro\Automation selenium java\Selenium-java-master\src\test\java\testcases\uat\Mudsku"

foreach ($fileName in $mapping.Keys) {
    $filePath = Join-Path $folder $fileName
    if (!(Test-Path $filePath)) {
        Write-Host "⚠️ Khong tim thay: $fileName"
        continue
    }

    $name = $mapping[$fileName].name
    $content = Get-Content $filePath -Raw

    # Tim pattern: promotionModal.findElement(By.xpath(".//*[contains(normalize-space(.),'" + PROMOTION_CODE + "')]"));
    # Thay thanh: promotionModal.findElement(By.xpath(".//*[contains(.,'KM-...-...') or contains(.,'TEN CO CAU')]"));
    
    $oldPattern = "promotionModal.findElement(By.xpath(`".//*[contains(normalize-space(.),'" + " + 'PROMOTION_CODE' + " + "')]`"));"
    $newPattern = "// Verify ca ma KM va ten co cau`r`n" + 
                  "            promotionModal.findElement(By.xpath(`".//*[contains(.,'`" + PROMOTION_CODE + `"') or contains(.,'$name')]`"));"

    if ($content -match [regex]::Escape($oldPattern)) {
        $content = $content.Replace($oldPattern, $newPattern)
        Set-Content $filePath -Value $content -NoNewline
        Write-Host "OK Fixed: $fileName → '$name'"
    } else {
        Write-Host "⚠️ Pattern khong khop: $fileName"
    }
}

Write-Host "`nDone fix TC12 verify!"
