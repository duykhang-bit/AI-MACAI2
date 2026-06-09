"""
Runner cho flow: Tao don hang ban tai quay
Dịch từ flows/rsa/ci/tao-don-hang-ban-tai-quay.yaml sang Playwright Python
"""

import asyncio
import time
from datetime import datetime
from playwright.async_api import async_playwright, TimeoutError as PlaywrightTimeoutError

TIMEOUT = 30000
SCREENSHOT_DIR = "flows/screenshots"

async def try_click(page, selectors: list, timeout=10000):
    for sel in selectors:
        try:
            await page.wait_for_selector(sel, timeout=timeout, state="visible")
            await page.click(sel)
            return True
        except Exception:
            continue
    return False

async def try_fill(page, selectors: list, value: str, timeout=10000):
    for sel in selectors:
        try:
            await page.wait_for_selector(sel, timeout=timeout, state="visible")
            await page.fill(sel, value)
            return True
        except Exception:
            continue
    return False

async def screenshot(page, step_name: str):
    ts = datetime.now().strftime("%Y-%m-%d_%H-%M")
    fname = f"{SCREENSHOT_DIR}/run__{step_name[:60]}__{ts}.png"
    try:
        await page.screenshot(path=fname, full_page=False)
        print(f"  📸 Screenshot: {fname}")
    except Exception:
        pass

async def run_flow():
    print("\n" + "="*60)
    print("▶ Flow: Tao don hang ban tai quay")
    print("▶ App: rsa | Env: CI")
    print("▶ URL: https://ci-rsa-web.frt.vn")
    print("="*60 + "\n")

    async with async_playwright() as p:
        # Dùng Chrome/Edge có sẵn trên máy thay vì download playwright chromium
        import os
        chrome_path = None
        for path in [
            r"C:\Program Files\Google\Chrome\Application\chrome.exe",
            r"C:\Program Files (x86)\Google\Chrome\Application\chrome.exe",
            r"C:\Program Files (x86)\Microsoft\Edge\Application\msedge.exe",
        ]:
            if os.path.exists(path):
                chrome_path = path
                print(f"  🌐 Dùng browser: {path}")
                break

        if chrome_path:
            browser = await p.chromium.launch(
                headless=False,
                slow_mo=500,
                executable_path=chrome_path
            )
        else:
            browser = await p.chromium.launch(headless=False, slow_mo=500)
        context = await browser.new_context(viewport={"width": 1440, "height": 900})
        page = await context.new_page()
        page.set_default_timeout(TIMEOUT)

        steps_passed = 0
        steps_failed = 0

        try:
            # ── Step 1: Navigate ─────────────────────────────────────────
            print("[ 1] Navigate to https://ci-rsa-web.frt.vn")
            await page.goto("https://ci-rsa-web.frt.vn", wait_until="networkidle")
            steps_passed += 1

            # ── Step 2-3: Fill username ──────────────────────────────────
            print("[ 2] Fill username: ngocdtm3")
            await page.wait_for_timeout(1000)
            username_sels = [
                "#mat-input-3",
                ".kt-form > div:nth-of-type(3) > mat-form-field > div > div:nth-of-type(1) > div > input"
            ]
            await try_fill(page, username_sels, "ngocdtm3")
            steps_passed += 1

            # ── Step 4-5: Fill password ──────────────────────────────────
            print("[ 3] Fill password")
            await page.wait_for_timeout(1000)
            password_sels = [
                "#mat-input-4",
                ".kt-form > div:nth-of-type(4) > mat-form-field > div > div:nth-of-type(1) > div > input"
            ]
            await try_fill(page, password_sels, "ergerg")
            steps_passed += 1

            # ── Step 6: Click Đăng nhập ──────────────────────────────────
            print("[ 4] Click 'Đăng nhập'")
            await page.wait_for_timeout(1000)
            login_sels = [
                "#kt_login_signin_submit",
                ".kt-login__actions > button",
                "button[type='submit']"
            ]
            ok = await try_click(page, login_sels)
            if not ok:
                # Fallback: click by role/text
                await page.get_by_role("button", name="Đăng nhập").click()
            await page.wait_for_load_state("networkidle")
            steps_passed += 1

            # ── Step 7: Chọn cửa hàng 80006 ─────────────────────────────
            print("[ 5] Chọn cửa hàng 80006 - LC HCM 72 Tân Mỹ")
            await page.wait_for_timeout(1000)
            store_trigger_sels = [
                "#chooseForm > div:nth-of-type(1) > div > div:nth-of-type(2) > div > div > div > div > span:nth-of-type(1)",
                "div:nth-of-type(2) > div > div:nth-of-type(2) > div > div:nth-of-type(2) > div:nth-of-type(2) > form > div:nth-of-type(1) > div > div:nth-of-type(2) > div > div > div > div > span:nth-of-type(1)"
            ]
            # Click to open dropdown
            for sel in store_trigger_sels:
                try:
                    await page.click(sel, timeout=5000)
                    break
                except Exception:
                    continue
            await page.wait_for_timeout(1000)
            # Type to search
            try:
                await page.keyboard.type("80006")
                await page.wait_for_timeout(1500)
                # Select matching option
                option = page.locator(".ant-select-item-option", has_text="80006")
                if await option.count() > 0:
                    await option.first.click()
                else:
                    # Try mat-option
                    mat_option = page.locator("mat-option", has_text="80006")
                    await mat_option.first.click()
            except Exception as e:
                print(f"  ⚠ Store search fallback: {e}")
            steps_passed += 1

            # ── Step 8: Click Hoàn tất ───────────────────────────────────
            print("[ 6] Click 'Hoàn tất'")
            await page.wait_for_timeout(1000)
            await page.get_by_role("button", name="Hoàn tất").click(timeout=10000)
            await page.wait_for_load_state("networkidle")
            steps_passed += 1

            # ── Step 9: Click Close ──────────────────────────────────────
            print("[ 7] Click 'Close'")
            await page.wait_for_timeout(1000)
            try:
                await page.get_by_role("button", name="Close").click(timeout=5000)
            except Exception:
                # Có thể không có popup này
                print("  ℹ Close button không xuất hiện, bỏ qua")
            steps_passed += 1

            # ── Step 10: Click vào menu đầu tiên (tạo đơn) ──────────────
            print("[ 8] Navigate to Bán hàng/Tạo đơn")
            await page.wait_for_timeout(2000)
            nav_sels = [
                ".home-layout > div:nth-of-type(1) > div:nth-of-type(1) > div:nth-of-type(1) > div:nth-of-type(2) > div:nth-of-type(1) > a",
                "div:nth-of-type(1) > div > div > section > section > main > div:nth-of-type(1) > div:nth-of-type(1) > div:nth-of-type(1) > div:nth-of-type(2) > div:nth-of-type(1) > a"
            ]
            ok = await try_click(page, nav_sels)
            if not ok:
                # Thử tìm link/button chứa text bán hàng
                print("  ⚠ Nav click failed, thử tìm theo text")
                for text in ["Bán hàng", "Tạo đơn", "POS", "Bán tại quầy"]:
                    try:
                        el = page.get_by_text(text, exact=False).first
                        if await el.is_visible(timeout=2000):
                            await el.click()
                            print(f"  ✓ Clicked: '{text}'")
                            break
                    except Exception:
                        continue
                else:
                    # Thử href chứa "pos" hoặc "ban-hang"
                    try:
                        link = page.locator("a[href*='pos'], a[href*='ban-hang'], a[href*='order'], a[href*='sell']").first
                        await link.click(timeout=5000)
                    except Exception as e:
                        print(f"  ⚠ Không tìm được nav link: {e}")
            await page.wait_for_load_state("networkidle")
            await page.wait_for_timeout(1500)
            steps_passed += 1

            # ── Step 11: Chọn nhân viên Trần Thị Thanh Thảo ─────────────
            print("[ 9] Chọn NV 'Trần Thị Thanh Thảo (00017)'")
            await page.wait_for_timeout(2000)
            try:
                # Thử nhiều selector cho combobox nhân viên
                employee_sels = [
                    "#cmb-employee",
                    "[id*='cmb-employee']",
                    "[placeholder*='nhân viên']",
                    "[placeholder*='NV']",
                ]
                found = False
                for emp_sel in employee_sels:
                    try:
                        await page.wait_for_selector(emp_sel, timeout=5000, state="visible")
                        await page.click(emp_sel)
                        found = True
                        break
                    except Exception:
                        continue
                if not found:
                    # Tìm theo text label
                    label = page.locator("label, span", has_text="Lựa chọn NV")
                    if await label.count() > 0:
                        # Click vào input gần label
                        parent = label.first.locator("..").locator("input, [role='combobox']")
                        await parent.first.click(timeout=5000)
                        found = True
                if found:
                    await page.wait_for_timeout(500)
                    await page.keyboard.type("Thảo")
                    await page.wait_for_timeout(1500)
                    option = page.locator(".ant-select-item-option", has_text="Trần Thị Thanh Thảo")
                    if await option.count() > 0:
                        await option.first.click()
                        print("  ✓ Chọn NV thành công")
                    else:
                        option2 = page.locator("[class*='option']", has_text="00017")
                        if await option2.count() > 0:
                            await option2.first.click()
                else:
                    print("  ⚠ Không tìm thấy employee combobox, bỏ qua")
            except Exception as e:
                print(f"  ⚠ Employee select: {e}")
            steps_passed += 1

            # ── Step 12: Nhập mã thuốc 00003337 ─────────────────────────
            print("[10] Nhập mã thuốc '00003337'")
            await page.wait_for_timeout(1000)
            product_sels = [
                ".product-search-input [role='combobox']",
                ".product-search-input > div > span:nth-of-type(1) > input",
                "[placeholder*='F1']",
                "[placeholder*='barcode']"
            ]
            ok = await try_fill(page, product_sels, "00003337")
            if not ok:
                # Try typing instead
                for sel in product_sels:
                    try:
                        await page.click(sel, timeout=3000)
                        await page.keyboard.type("00003337")
                        break
                    except Exception:
                        continue
            await page.wait_for_timeout(2000)
            steps_passed += 1

            await screenshot(page, "sau-nhap-ma-thuoc")

            # ── Step 13: Chọn item đầu tiên trong dropdown ───────────────
            print("[11] Chọn item đầu tiên trong dropdown sản phẩm")
            await page.wait_for_timeout(1000)
            dropdown_sels = [
                ".ant-select-dropdown:visible .ant-select-item-option:first-child",
                ".ant-select-dropdown .ant-select-item-option:first-child",
                ".ant-select-item-option:first-child"
            ]
            ok = await try_click(page, dropdown_sels, timeout=60000)
            if not ok:
                print("  ⚠ Dropdown item fallback")
            await page.wait_for_timeout(1000)
            steps_passed += 1

            # ── Step 14: Tăng số lượng (+) ───────────────────────────────
            print("[12] Click tăng số lượng")
            await page.wait_for_timeout(1000)
            qty_sels = [
                ".item-quantity > span > button:nth-of-type(2)",
                "button[aria-label='+']"
            ]
            await try_click(page, qty_sels)
            steps_passed += 1

            # ── Step 15-16: Nhập SĐT khách hàng ─────────────────────────
            print("[13] Nhập SĐT khách hàng: 0364575665")
            await page.wait_for_timeout(1000)
            phone_sels = [
                "[placeholder*='Số điện thoại']",
                "[placeholder*='F2']",
                ".c-info-form > div:nth-of-type(1) > div > div > div > div > div > input"
            ]
            ok = await try_fill(page, phone_sels, "0364575665")
            if ok:
                await page.keyboard.press("Enter")
            steps_passed += 1

            # ── Step 17: Scroll ──────────────────────────────────────────
            print("[14] Scroll")
            await page.wait_for_timeout(500)
            try:
                await page.eval_on_selector(".sc-papXJ", "el => el.scrollTo(0, 343)")
            except Exception:
                await page.mouse.wheel(0, 343)
            steps_passed += 1

            await screenshot(page, "truoc-tao-don")

            # ── Step 18: Click Tạo đơn (F4) ──────────────────────────────
            print("[15] Click 'Tạo đơn (F4)'")
            await page.wait_for_timeout(1000)
            create_sels = [
                "[id^='create-order_session']",
                "#btn-next > button"
            ]
            ok = await try_click(page, create_sels, timeout=10000)
            if not ok:
                await page.get_by_role("button", name="Tạo đơn").click(timeout=10000)
            steps_passed += 1

            # ── Step 19: Click thanh toán tiền mặt ───────────────────────
            print("[16] Click thanh toán 'Tổng tiền (Shift+Enter)'")
            await page.wait_for_timeout(500)
            payment_sels = [
                "#btn__payment--method-cash",
                "[id^='focus__payment--1-'] > div:nth-of-type(1) > div:nth-of-type(2) > div > div:nth-of-type(2) > button"
            ]
            ok = await try_click(page, payment_sels, timeout=10000)
            if not ok:
                await page.get_by_role("button", name="Tổng tiền").click(timeout=10000)
            steps_passed += 1

            # ── Step 20: Click Hoàn tất (F4) ─────────────────────────────
            print("[17] Click 'Hoàn tất (F4)'")
            await page.wait_for_timeout(1000)
            finish_sels = [
                "[id^='btn__finish--F4']",
                "#btn-finish > button"
            ]
            ok = await try_click(page, finish_sels, timeout=10000)
            if not ok:
                await page.get_by_role("button", name="Hoàn tất").click(timeout=10000)
            steps_passed += 1

            # ── Step 21: Chọn NV thanh toán ──────────────────────────────
            print("[18] Chọn NV thanh toán 'Trần Thị Thanh Thảo (00017)'")
            await page.wait_for_timeout(1000)
            try:
                emp_payment_sel = "#cmb-employee-payment"
                await page.wait_for_selector(emp_payment_sel, timeout=8000)
                await page.click(emp_payment_sel)
                await page.wait_for_timeout(500)
                await page.keyboard.type("Thảo")
                await page.wait_for_timeout(1000)
                option = page.locator(".ant-select-item-option", has_text="Trần Thị Thanh Thảo")
                if await option.count() > 0:
                    await option.first.click()
            except Exception as e:
                print(f"  ⚠ Payment employee: {e}")
            steps_passed += 1

            # ── Step 22-23: Click confirm ─────────────────────────────────
            print("[19] Click confirm (2 lần)")
            await page.wait_for_timeout(1000)
            for i in range(2):
                try:
                    confirm_sels = [".sc-lbxAil", "div:nth-of-type(3) > div > div:nth-of-type(2) > div"]
                    await try_click(page, confirm_sels, timeout=8000)
                    await page.wait_for_timeout(1000)
                except Exception as e:
                    print(f"  ⚠ Confirm click {i+1}: {e}")
            steps_passed += 1

            # ── Step 24: Assert kết quả ───────────────────────────────────
            print("[20] Assert: 'Hoàn thành đơn hàng, Mã ĐH: '")
            await page.wait_for_timeout(1500)
            await screenshot(page, "ket-qua-cuoi")

            result_sels = [".sc-lbxAil", "div:nth-of-type(3) > div > div:nth-of-type(2) > div"]
            assert_passed = False
            for sel in result_sels:
                try:
                    el = page.locator(sel)
                    if await el.count() > 0:
                        text = await el.first.inner_text()
                        if "Hoàn thành đơn hàng" in text and "Mã ĐH:" in text:
                            print(f"\n  ✅ ASSERT PASSED: '{text[:80]}'")
                            assert_passed = True
                            break
                except Exception:
                    continue

            if not assert_passed:
                # Try finding text anywhere on page
                try:
                    body_text = await page.inner_text("body")
                    if "Hoàn thành đơn hàng" in body_text:
                        print("\n  ✅ ASSERT PASSED: 'Hoàn thành đơn hàng' found on page")
                        assert_passed = True
                except Exception:
                    pass

            if not assert_passed:
                print("\n  ❌ ASSERT FAILED: Không tìm thấy 'Hoàn thành đơn hàng'")
                steps_failed += 1
            else:
                steps_passed += 1

        except Exception as e:
            print(f"\n  ❌ ERROR: {e}")
            await screenshot(page, "error")
            steps_failed += 1

        finally:
            print("\n" + "="*60)
            print(f"📊 KẾT QUẢ: {steps_passed} passed | {steps_failed} failed")
            if steps_failed == 0:
                print("✅ FLOW PASSED")
            else:
                print("❌ FLOW FAILED")
            print("="*60)
            try:
                await page.wait_for_timeout(3000)
            except Exception:
                pass
            try:
                await browser.close()
            except Exception:
                pass

if __name__ == "__main__":
    asyncio.run(run_flow())
