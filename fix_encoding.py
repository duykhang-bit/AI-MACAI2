import os

folder = r'c:\Users\khangld10\Documents\APP\AI-MACAI2\.kiro\Automation selenium java\Selenium-java-master\src\test\java\testcases\uat\Mudsku'

# Mapping sai → đúng (từ TC1)
replacements = {
    # Login & Modal
    'Chon dia chi dang nhap': 'Chọn địa chỉ đăng nhập',
    
    # Inside - QUAN TRỌNG
    'Tran Thi Thanh Thao': 'Trần Thị Thanh Thảo',
    '//input[contains(@placeholder,\'inside\') or contains(@placeholder,\'ma\')]': '//input[contains(@placeholder,\'inside\') or contains(@placeholder,\'mã\')]',
    'Chon Shop': 'Chọn Shop',
    'Login voi tai khoan': 'Login với tài khoản',
    'Login thanh cong': 'Login thành công',
    
    # Shop
    'Chon shop': 'Chọn shop',
    'Da chon shop': 'Đã chọn shop',
    'Hoan tat chon shop': 'Hoàn tất chọn shop',
    'Hoan tat': 'Hoàn tất',
    'Click Hoan tat thanh cong': 'Click Hoàn tất thành công',
    
    # Popup
    'Tat popup neu co': 'Tắt popup nếu có',
    'Da tat popup sai doi tuong': 'Đã tắt popup sai đối tượng',
    'Khong co popup sai doi tuong': 'Không có popup sai đối tượng',
    'Danh sach san pham sai doi tuong': 'Danh sách sản phẩm sai đối tượng',
    'De sau': 'Để sau',
    
    # Menu
    'Chon muc Ban hang': 'Chọn mục Bán hàng',
    'Da vao muc Ban hang': 'Đã vào mục Bán hàng',
    'Ban hang': 'Bán hàng',
    
    # Inside
    'Nhap ma inside': 'Nhập mã inside',
    'Da nhap ma inside': 'Đã nhập mã inside',
    
    # Phone
    'Nhap SDT': 'Nhập SĐT',
    'Nhap SĐT': 'Nhập SĐT',
    
    # Product
    'Them san pham': 'Thêm sản phẩm',
    'Nhap ma SP': 'Nhập mã SP',
    'Tim thay SP': 'Tìm thấy SP',
    'Da them SP': 'Đã thêm SP',
    'san pham': 'sản phẩm',
    
    # MUD
    'Nhap ma KM': 'Nhập mã KM',
    'Ap dung': 'Áp dụng',
    'Apply serial MUD thanh cong': 'Apply serial MUD thành công',
    'Apply MUD that bai': 'Apply MUD thất bại',
    'Xac nhan': 'Xác nhận',
    
    # CTKM
    'Khuyen mai khac': 'Khuyến mãi khác',
    'Danh sach khuyen mai': 'Danh sách khuyến mãi',
    'Tim thay': 'Tìm thấy',
    'trong danh sach khuyen mai': 'trong danh sách khuyến mãi',
    'Khong tim thay': 'Không tìm thấy',
    'verify qua man hinh ban hang': 'verify qua màn hình bán hàng',
    
    # Order
    'Tao don': 'Tạo đơn',
    'Click Tao don': 'Click Tạo đơn',
    'Tao don thanh cong': 'Tạo đơn thành công',
    'Don hang duoc tao': 'Đơn hàng được tạo',
}

files_fixed = 0

for tc_num in range(2, 31):
    if tc_num == 25:
        continue
    
    filename = f'TC{tc_num}.java'
    filepath = os.path.join(folder, filename)
    
    if not os.path.exists(filepath):
        continue
    
    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()
    
    new_content = content
    changes = 0
    
    for wrong, correct in replacements.items():
        if wrong in new_content:
            new_content = new_content.replace(wrong, correct)
            changes += new_content.count(correct) - content.count(correct)
    
    if new_content != content:
        with open(filepath, 'w', encoding='utf-8') as f:
            f.write(new_content)
        print(f'✅ Fixed {filename} - {changes} replacements')
        files_fixed += 1

print(f'\n✅ Done! Fixed encoding for {files_fixed} files.')
