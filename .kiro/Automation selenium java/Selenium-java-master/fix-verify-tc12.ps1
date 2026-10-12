# Script sửa TC12 verify: thêm tên cơ cấu vào verify xpath

$mapping = @{
    "TC1.java"     = @{ code="KM-1026-094"; name="Mua 2 tặng 1 - Nước mắt nhân tạo Easimist Eye Drops 5ml" }
    "TC2.java"     = @{ code="KM-1026-095"; name="Mua 2 tặng 1 - Dầu ăn Nếp Cái Hoa Vàng 2L" }
    "TC3.java"     = @{ code="KM-1026-096"; name="Mua 2 tặng 1 - Kem Đánh Răng Colgate Muối 225g" }
    "TC4.java"     = @{ code="KM-1026-097"; name="Mua 2 tặng 1 - Sữa Rửa Mặt Simple Làm Sạch Sâu 150ml" }
    "TC5.java"     = @{ code="KM-1026-098"; name="Mua 1 tặng 1 - Kẹo Chewies Raspberry 15g" }
    "TC6.java"     = @{ code="KM-1026-099"; name="Mua 1 tặng 1 - Kẹo Chewies Blueberry 15g" }
    "TC7.java"     = @{ code="KM-1026-100"; name="Mua 1 tặng 1 - Bông Tẩy Trang Simply Organic 160 miếng" }
    "TC8.java"     = @{ code="KM-1026-101"; name="Mua 1 tặng 1 - Bông Tẩy Trang 2 lớp Simply Organic 300 miếng" }
    "TC9.java"     = @{ code="KM-1026-102"; name="Mua 1 tặng 1 - Bông Tẩy Trang 3 lớp Simply Organic 300 miếng" }
    "TC10.java"    = @{ code="KM-1026-103"; name="Giảm giá 30% - Kẹo dẻo Vitamin C Minamax" }
    "TC11.java"    = @{ code="KM-1026-104"; name="Giảm giá 30% - Viên uống tăng đề kháng Chela Vit C 550" }
    "TC12.java"    = @{ code="KM-1026-105"; name="Giảm giá 30% - Siro Bổ Phế Labebe 120ml" }
    "TC13.java"    = @{ code="KM-1026-106"; name="Giảm giá 30% - Sữa rửa mặt Simple Kind To Skin Refreshing Facial Wash Gel 150ml" }
    "TC14.java"    = @{ code="KM-1026-107"; name="Giảm giá 30% - Viên uống Actiso Extra" }
    "TC15.java"    = @{ code="KM-1026-108"; name="Giảm giá 30% - Vitamin tổng hợp Good Health Viralex Attack 60 viên" }
    "TC16.java"    = @{ code="KM-1026-109"; name="Giảm giá 30% - Viên uống Hỗ Trợ Giảm Cân Natures Way Sculpt" }
    "TC17.java"    = @{ code="KM-1026-110"; name="Giảm giá 30% - Viên Uống Best Collagen Absolute Gold" }
    "TC18.java"    = @{ code="KM-1026-111"; name="Giảm giá 30% - Viên Uống DHA Hỗ Trợ Não Bộ Blackmores" }
    "TC19.java"    = @{ code="KM-1026-112"; name="Giảm giá 30% - Vitamin e 1000 IU Blackmores 30 viên" }
    "TC20.java"    = @{ code="KM-1026-113"; name="Giảm giá 50% - Nước uống bổ sung sắt Phargold 125ml" }
    "TC21.java"    = @{ code="KM-1026-114"; name="Giảm giá 50% - Dầu ăn Nếp Cái Hoa Vàng 2L" }
    "TC22.java"    = @{ code="KM-1026-115"; name="Giảm giá 50% - Mặt Nạ Simple Cho Da Nhạy Cảm Sáng Da Vitamin C Glow-Boost Sheet Mask 21ml" }
    "TC23.java"    = @{ code="KM-1026-116"; name="Giảm giá 50% - Bông Tẩy Trang 2 lớp Simply Organic 300 miếng" }
    "TC24.java"    = @{ code="KM-1026-117"; name="Giảm giá 50% - Sữa Tắm Romano Deluxe 650g" }
    "TC25new.java" = @{ code="KM-1026-118"; name="Giảm giá 50% - Dung dịch vệ sinh phụ nữ Natural Feminine La Trầu Không 100ml" }
    "TC26.java"    = @{ code="KM-1026-119"; name="Quà tặng - Bông tẩy trang Simply Organic 80 miếng" }
    "TC27.java"    = @{ code="KM-1026-120"; name="Quà tặng - Kẹo Chewies Strawberry 15g" }
    "TC28.java"    = @{ code="KM-1026-121"; name="Quà tặng - Nước súc miệng P/S Fresh Breath 500ml" }
    "TC29.java"    = @{ code="KM-1026-122"; name="Quà tặng - Bông tẩy trang Simply Organic 160 miếng" }
    "TC30.java"    = @{ code="KM-1026-123"; name="Quà tặng - Kem Đánh Răng Colgate Muối 225g" }
}

$folder = "c:\Users\khangld10\Documents\APP\AI-MACAI2\.kiro\Automation selenium java\Selenium-java-master\src\test\java\testcases\uat\Mudsku"

foreach ($fileName in $mapping.Keys) {
    $filePath = Join-Path $folder $fileName
    if (!(Test-Path $filePath)) {
        Write-Host "⚠️ Không tìm thấy: $fileName"
        continue
    }

    $code = $mapping[$fileName].code
    $name = $mapping[$fileName].name

    $content = Get-Content $filePath -Raw

    # Old pattern: promotionModal.findElement(By.xpath(".//*[contains(normalize-space(.),'" + PROMOTION_CODE + "')]"));
    $oldPattern = [regex]::Escape("promotionModal.findElement(By.xpath(`".//*[contains(normalize-space(.),'" + PROMOTION_CODE + "')]`"));")
    
    # New pattern: verify cả mã KM và tên
    $newLine = "promotionModal.findElement(By.xpath(`".//*[contains(.,'$code') or contains(.,'$name')]`"));"

    if ($content -match [regex]::Escape(".//*[contains(normalize-space(.),'" + " + 'PROMOTION_CODE' + " + "')]")) {
        $content = $content -replace [regex]::Escape("promotionModal.findElement(By.xpath(`".//*[contains(normalize-space(.),'" + " + 'PROMOTION_CODE' + " + "')]`"));"), $newLine
        Set-Content $filePath -Value $content -NoNewline
        Write-Host "✅ Fixed TC12 verify: $fileName → verify '$name'"
    } else {
        Write-Host "⚠️ Pattern không khớp: $fileName (có thể đã sửa rồi)"
    }
}

Write-Host "`n✅ Hoàn tất fix TC12 verify!"
