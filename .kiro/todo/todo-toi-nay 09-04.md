# TODO - Tối 9/4

## BE lên tối nay

### 1. Chặn sử dụng voucher ngang trong ngày issue
- BE voucher lên tối nay
- FE App lên sau
- **Lưu ý message lỗi:** Không chỉ ghi "24h kể từ lúc issue voucher"
  - Phải hiển thị **thời gian dùng được cụ thể** (làm tròn đến hàng chục phút)
  - Ví dụ: "Voucher có hiệu lực từ 22:40 ngày 09/04/2026"

### 2. OTP đến thành viên khi sử dụng voucher
- Bổ sung OTP flow khi member dùng voucher

---

## Phase 1 - 9/4 (HungTT24 + TúNT15)

### 3. Chặn giải tán gia đình RSA / RSA Ecom LC

### 4. Chặn không issue voucher đảo 1 khi:
- Thành viên rời gia đình
- Giải tán gia đình
- Thành viên tham gia gia đình mới

### 5. API check thành viên đã thuộc gia đình trước đó (HungTT24)
- Thêm API check lịch sử gia đình của thành viên

### 6. Chặn tạo gia đình với thành viên đã từng thuộc gia đình khác + pass đảo 1 (TúNT15)

---

## Đã xử lý hôm nay
- [x] Chặn đầu số ảo 0580 A Tú
- [x] Voucher: chỉnh ngày bắt đầu hiệu lực sau 24 tiếng a Nghĩa
- [x] Message lỗi SĐT đã tồn tại với gia đình đã tạo và pass đảo 1 trước đó chặn BE a TÚ
- [x] Veriy gửi otp rsa/ecom

luồng otp app làm sau

luồng chặn add tv đã tạo và chặn k hiện pop_up nhận vc  https://www.figma.com/design/M64juJO8tfHlk1AobsoSr4/Family-Package?node-id=14484-58747&t=FnYwx7wDSahnCCpG-0


Tàu 15/4
- PTT ĐÓNG SHOP https://reqs.frt.vn/browse/FSS-3097
- Tàu family  https://reqs.frt.vn/issues/?jql=project%20%3D%20FP%20AND%20fixVersion%20in%20(%22release%2F20260415%22%2C%20%22release%2F20260415-v8.17.4-lc%22)%20ORDER%20BY%20priority%20DESC%2C%20updated%20DESC 
- [Promotion Service] Loại trừ thuộc tính thuốc và sản phẩm 
https://reqs.frt.vn/browse/FSS-3102 


xấy dựng phòng khám tàu 20/4



 <!-- <test name="UAT Tests">
        <classes>
            <!-- Promotion RSA Tests -->
            <class name="testcases.uat.PromotionRsa.TC1"/>
            <class name="testcases.uat.PromotionRsa.TC2"/>
            <class name="testcases.uat.PromotionRsa.TC3"/>
            <class name="testcases.uat.PromotionRsa.TC4"/>
            <class name="testcases.uat.PromotionRsa.TC5"/>
            <class name="testcases.uat.PromotionRsa.TC6"/>
            <class name="testcases.uat.PromotionRsa.TC7"/>
            <class name="testcases.uat.PromotionRsa.TC8"/>
            <class name="testcases.uat.PromotionRsa.TC9"/>
            <class name="testcases.uat.PromotionRsa.TC10"/>
            <class name="testcases.uat.PromotionRsa.TC11"/>
            <class name="testcases.uat.PromotionRsa.TC12"/>
            <class name="testcases.uat.PromotionRsa.TC13"/>
     

            <!-- Family Tests -->
            <!--
            <class name="testcases.Family.WebRSAUAT"/>
            <class name="testcases.Family.WebRSAUAT1"/>
            <class name="testcases.Family.WebRSAUAT2"/>
            <class name="testcases.Family.WebRSAUAT3"/>
            <class name="testcases.Family.WebRSAUAT4"/>
            <class name="testcases.Family.WebRSAUAT5"/>
            <class name="testcases.Family.WebRSAUAT6"/>
            -->
        </classes>
    </test> -->