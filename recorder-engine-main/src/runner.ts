/**
 * FRT Test Recorder — Flow Runner Engine
 * Đọc YAML flow file → execute bằng Playwright
 */

import { chromium, Browser, Page, BrowserContext, Locator } from '@playwright/test';
import * as fs from 'fs';
import * as path from 'path';
import * as yaml from 'js-yaml';
import { FlowFile, FlowStep, FlowVariable, RunResult, StepResult, SimpleLocator } from './types';
import { JUNK_SELECTORS, AGGRESSIVE_SELECTORS, resolvePopupMode, FUNCTIONAL_MODAL_SELECTOR } from './popup-dismiss';

export class FlowRunner {
  private browser: Browser | null = null;
  private context: BrowserContext | null = null;
  private page: Page | null = null;
  private flowsDir: string;
  private rootFlowsDir: string;
  private variables: FlowVariable = {};

  constructor(flowsDir: string = path.join(process.cwd(), 'flows'), rootFlowsDir?: string) {
    this.flowsDir = flowsDir;
    this.rootFlowsDir = rootFlowsDir || flowsDir;
  }

  /**
   * Load và parse YAML flow file
   */
  loadFlow(flowPath: string): FlowFile {
    const absolutePath = path.isAbsolute(flowPath)
      ? flowPath
      : path.resolve(this.flowsDir, flowPath);

    if (!fs.existsSync(absolutePath)) {
      throw new Error(`Flow file not found: ${absolutePath}`);
    }

    const content = fs.readFileSync(absolutePath, 'utf8');
    const flow = yaml.load(content) as FlowFile;

    if (!flow.name || !flow.steps) {
      throw new Error(`Invalid flow file: missing 'name' or 'steps' in ${absolutePath}`);
    }

    return flow;
  }

  /**
   * Resolve variable placeholders {{var_name}} in string
   */
  private interpolate(text: string | undefined, vars: FlowVariable): string {
    if (!text) return '';
    return text.replace(/\{\{(\w+)\}\}/g, (_, key) => {
      return String(vars[key] ?? `{{${key}}}`);
    });
  }

  /**
   * Cắt phần text TĨNH đứng trước token động (số ≥3 chữ số, mã đơn/giá/ngày…).
   * Dùng để click element có text kiểu "Mã đơn hàng: 8860325" → match prefix "Mã đơn hàng:"
   * (getByText substring) nên vẫn trúng "Mã đơn hàng: 1639978" ở lần chạy khác.
   * Trả '' nếu không có phần tĩnh đủ dài (≥3 ký tự) để tránh match quá rộng.
   */
  private staticTextPrefix(text: string): string {
    if (!text) return '';
    // Cắt tại run số ≥3 chữ số đầu tiên (kèm ký hiệu tiền/% nếu có liền trước)
    const m = text.match(/^(.*?)(?=\s*\S*\d{3,})/);
    const prefix = (m ? m[1] : text).replace(/[\s:–-]+$/, '').trim();
    return prefix.length >= 3 ? prefix : '';
  }

  /**
   * Fallback click cho element có text động: dùng prefix tĩnh + substring match.
   * Chỉ kích hoạt khi step dùng `text` locator và text có phần động (số ≥3 chữ số).
   * Trả true nếu click thành công.
   */
  private async tryClickByTextPrefix(page: Page, step: FlowStep, vars: FlowVariable, timeout: number): Promise<boolean> {
    if (!step.text) return false;
    const resolved = this.interpolate(step.text, vars);
    const prefix = this.staticTextPrefix(resolved);
    if (!prefix || prefix === resolved) return false; // không có phần động → bỏ qua
    try {
      console.log(`  [text-prefix] dynamic text fallback: click theo prefix "${prefix}"`);
      const el = page.getByText(prefix, { exact: false }).first();
      await el.waitFor({ state: 'visible', timeout: Math.min(timeout, 8000) });
      await el.click({ timeout });
      return true;
    } catch {
      return false;
    }
  }

  /**
   * Tách selectorFallback thành danh sách candidate theo thứ tự ưu tiên.
   * Recorder gộp nhiều selector vào MỘT field, ngăn bằng " || " — không sinh mảng fallbacks[].
   */
  private fallbackSelectors(step: FlowStep, vars: FlowVariable): string[] {
    if (!step.selectorFallback) return [];
    return this.interpolate(step.selectorFallback, vars)
      .split(' || ').map((s) => s.trim()).filter(Boolean);
  }

  /**
   * Resolve một SimpleLocator tương đối với `root` (Page hoặc một Locator đã filter theo scope).
   * Dùng cho `target` của relative locator (Form A).
   */
  private resolveSimpleLocator(root: Page | Locator, s: SimpleLocator): Locator {
    switch (s.type) {
      case 'role': return root.getByRole(s.role as any, s.name ? { name: s.name } : undefined);
      case 'label': return root.getByLabel(s.label!);
      case 'placeholder': return root.getByPlaceholder(s.placeholder!);
      case 'text': return root.getByText(s.text!);
      case 'testId': return root.getByTestId(s.testId!);
      case 'selector': return root.locator(s.selector!);
      default: throw new Error('Unknown SimpleLocator type: ' + (s as any).type);
    }
  }

  /**
   * Chụp ảnh viewport (JPEG nhẹ) → data URI base64 để nhúng thẳng vào HTML report.
   * Dùng cho step verify (assert) và step fail. Lỗi chụp → trả undefined, không làm hỏng run.
   */
  private async captureBase64(page: Page): Promise<string | undefined> {
    try {
      const buf = await page.screenshot({ type: 'jpeg', quality: 55 });
      return `data:image/jpeg;base64,${buf.toString('base64')}`;
    } catch {
      return undefined;
    }
  }

  /**
   * Execute a single step
   */
  private async executeStep(page: Page, step: FlowStep, vars: FlowVariable): Promise<void> {
    const timeout = step.timeout || 30000;

    // Handle shared flow reference
    if (step.use) {
      const sharedPath = step.use.endsWith('.yaml') ? step.use : `${step.use}.yaml`;
      const mergedVars = { ...vars, ...(step.with || {}) };
      // Resolve with variables
      const resolvedWith: Record<string, string> = {};
      if (step.with) {
        for (const [k, v] of Object.entries(step.with)) {
          resolvedWith[k] = this.interpolate(v, vars);
        }
      }
      await this.runSharedFlow(page, sharedPath, { ...vars, ...resolvedWith });
      return;
    }

    const selector = this.interpolate(step.selector, vars);
    const value = this.interpolate(step.value, vars);
    const key = this.interpolate(step.key, vars);

    // Resolve một locator từ tập field (relative > role > label > placeholder > text > testId > selector).
    // Dùng cho cả step thường và `step.trigger` của action selectOption.
    const locatorFromFields = (src: any): Locator => {
      if (src.relative) {
        const r = src.relative;
        // ⚠ RESIDUAL ĐÃ BIẾT: getByText KHÔNG lọc :visible → có thể trúng node a11y ẩn (rc-select nhân đôi
        // <div role=option ... hidden>). Recorder verify lúc record CÓ lọc visible nên YAML sinh ra né được
        // phần lớn; nhưng YAML cũ / DOM trôi vẫn có rủi ro. Combobox dùng action 'selectOption' (đã tự lọc
        // :visible / .ant-select-dropdown:visible) để tránh hẳn. Chưa nâng :visible ở đây theo quyết định giữ runtime.
        const anchor = page.getByText(this.interpolate(r.anchor.text, vars), { exact: true });
        console.log(`  [relative] form ${r.form}, anchor "${this.interpolate(r.anchor.text, vars)}"`);
        if (r.form === 'B') return anchor.locator(r.ancestor!.xpath).first();
        const scope = (r.scope!.by === 'role'
          ? page.getByRole(r.scope!.role as any)
          : page.locator(r.scope!.css!)).filter({ has: anchor });
        return this.resolveSimpleLocator(scope, r.target!).first();
      }
      if (src.role) return page.getByRole(src.role.type as any, { name: src.role.name });
      if (src.label) return page.getByLabel(this.interpolate(src.label, vars));
      if (src.placeholder) return page.getByPlaceholder(this.interpolate(src.placeholder, vars));
      if (src.text) return page.getByText(this.interpolate(src.text, vars));
      if (src.testId) return page.getByTestId(this.interpolate(src.testId, vars));
      const sel = this.interpolate(src.selector, vars);
      if (sel) return page.locator(sel);
      throw new Error(`Step "${step.name}": no locator specified (role/label/placeholder/text/testId/selector)`);
    };
    // Get locator based on priority: relative > role > label > placeholder > text > testId > selector
    const getLocator = () => locatorFromFields(step);

    switch (step.action) {
      case 'goto':
        await page.goto(value, { timeout, waitUntil: 'domcontentloaded' });
        break;

      case 'click': {
        const loc = getLocator();
        const forceClick = step.force === true;
        const isComboboxLike = step.role?.type === 'combobox' || step.role?.type === 'textbox';
        // Click "nhãn động": recorded text là NHÃN kết thúc bằng ":" (vd "Mã đơn hàng: ") nhưng
        // đích click thật là GIÁ TRỊ động bên cạnh (dãy số/link). Click vào value, tránh nút copy/icon.
        if (step.text && /:\s*$/.test(this.interpolate(step.text, vars))) {
          const label = this.interpolate(step.text, vars).replace(/[:\s]+$/, '').trim();
          if (label.length >= 2) {
            try {
              const container = page.getByText(label, { exact: false }).first();
              await container.waitFor({ state: 'visible', timeout });
              // Value = phần tử con có chứa số/chữ động, KHÔNG phải nút copy (ant-typography-copy / aria-label "Lưu")
              const value = container
                .locator('.ant-typography, a, [class*="link"], [class*="value"], span, div')
                .filter({ hasText: /\d{2,}|[A-Za-z]{2,}/ })
                .and(page.locator(':not([class*="copy"]):not([aria-label*="Lưu" i]):not([aria-label*="copy" i])'))
                .first();
              let clickTarget = value;
              if (!(await value.count().catch(() => 0))) clickTarget = container;
              // Click ở góc trái-trên của value → trúng ký tự đầu (dãy số), tránh icon copy ở cuối
              await clickTarget.click({ timeout, position: { x: 4, y: 6 } });
              console.log(`  [label-value] clicked value next to "${label}"`);
              if (step.waitAfter) {
                if (typeof step.waitAfter === 'number') await page.waitForTimeout(step.waitAfter);
                else if (step.waitAfter === 'networkIdle') await page.waitForLoadState('networkidle');
                else if (step.waitAfter === 'domContentLoaded') await page.waitForLoadState('domcontentloaded');
                else if (step.waitAfter === 'load') await page.waitForLoadState('load');
              }
              break; // Done
            } catch { /* fall through to normal click */ }
          }
        }
        // Safety net: Ant sidebar collapsed submenu — hover to reveal popup, then click TARGET (not anchor).
        if (step.relative?.scope?.css && /ant-menu-submenu/.test(step.relative.scope.css) && step.relative.anchor?.text) {
          const anchorText = this.interpolate(step.relative.anchor.text, vars);
          const targetSelector = step.relative.target?.selector;
          try {
            // Use anchor text as visibility signal that submenu popup is open
            const anchorEl = page.getByText(anchorText, { exact: true }).first();
            let visible = await anchorEl.isVisible().catch(() => false);
            if (!visible) {
              // Submenu not open → hover/click submenu-titles until anchor becomes visible
              const titles = page.locator('.ant-menu-submenu-title');
              const count = await titles.count();
              for (let i = 0; i < count && !visible; i++) {
                await titles.nth(i).hover({ timeout: 3000 }).catch(() => {});
                await page.waitForTimeout(350);
                visible = await anchorEl.isVisible().catch(() => false);
                if (!visible) {
                  await titles.nth(i).click({ timeout: 3000 }).catch(() => {});
                  await page.waitForTimeout(350);
                  visible = await anchorEl.isVisible().catch(() => false);
                }
              }
            }
            if (visible) {
              // Submenu is open — click the actual TARGET within the popup, not anchor text
              let clicked = false;
              if (targetSelector) {
                // Find target within visible submenu popup containers
                const popups = page.locator('.ant-menu-submenu-popup:visible, [class*="ant-menu-sub"]:visible');
                const popupCount = await popups.count();
                for (let p = 0; p < popupCount && !clicked; p++) {
                  const targetEl = popups.nth(p).locator(targetSelector).first();
                  if (await targetEl.isVisible().catch(() => false)) {
                    await targetEl.click({ timeout });
                    const targetText = await targetEl.textContent().catch(() => targetSelector);
                    console.log(`  [submenu-hover] clicked target "${targetText?.trim()}" (via ${targetSelector})`);
                    clicked = true;
                  }
                }
                // Fallback: try target selector on full page (popup might not match container selectors)
                if (!clicked) {
                  const pageTarget = page.locator(targetSelector).filter({ has: page.locator(':visible') }).first();
                  if (await pageTarget.isVisible().catch(() => false)) {
                    await pageTarget.click({ timeout });
                    const targetText = await pageTarget.textContent().catch(() => targetSelector);
                    console.log(`  [submenu-hover] clicked target "${targetText?.trim()}" (page-level ${targetSelector})`);
                    clicked = true;
                  }
                }
              }
              // Last resort: no target selector or target not found → click anchor directly
              if (!clicked) {
                await anchorEl.click({ timeout });
                console.log(`  [submenu-hover] clicked "${anchorText}" (anchor fallback)`);
              }
              if (step.waitAfter) {
                if (typeof step.waitAfter === 'number') await page.waitForTimeout(step.waitAfter);
                else if (step.waitAfter === 'networkIdle') await page.waitForLoadState('networkidle');
                else if (step.waitAfter === 'domContentLoaded') await page.waitForLoadState('domcontentloaded');
                else if (step.waitAfter === 'load') await page.waitForLoadState('load');
              }
              break; // Done — exit case 'click'
            }
          } catch { /* fall through to normal click path */ }
        }
        try {
          if (!forceClick && !isComboboxLike) {
            await loc.first().waitFor({ state: 'visible', timeout });
          } else if (isComboboxLike) {
            // Combobox inputs (Ant Select) are often opacity:0/width:0 — wait for attached, click forced.
            await loc.first().waitFor({ state: 'attached', timeout });
          }
          await loc.first().click({ timeout, force: forceClick || isComboboxLike });
        } catch (e: any) {
          if (e.message?.includes('intercept') || e.message?.includes('overlay')) {
            // Tier 3: reactive dismiss BEFORE force-retry
            const dismissed = await this.tryReactiveDismiss(page, step, JUNK_SELECTORS);
            if (dismissed) {
              try { await loc.first().click({ timeout }); break; } catch (e2: any) { e = e2; }
            }
            // Fall through to force-retry (needed for combobox opacity:0)
            console.log(`  [retry] Click intercepted, retrying with force: "${step.name}"`);
            try { await loc.first().click({ timeout, force: true }); break; } catch (e2: any) { e = e2; }
          }
          // Last-resort fallback: thử lần lượt các candidate trong selectorFallback (ngăn bằng " || ").
          const cands = this.fallbackSelectors(step, vars);
          if (cands.length) {
            let ok = false; let lastErr: any = e;
            for (const css of cands) {
              try {
                console.log(`  [fallback] primary failed, trying selector "${css}"`);
                const fLoc = page.locator(css).first();
                await fLoc.waitFor({ state: 'attached', timeout: Math.min(timeout, 8000) });
                await fLoc.click({ timeout, force: true });
                ok = true; break;
              } catch (e2: any) { lastErr = e2; }
            }
            if (ok) break;
            // Dynamic-text fallback: click bằng prefix tĩnh (vd "Mã đơn hàng:" thay vì "Mã đơn hàng: 8860325")
            if (await this.tryClickByTextPrefix(page, step, vars, timeout)) break;
            throw lastErr;
          } else {
            // Dynamic-text fallback trước khi bỏ cuộc
            if (await this.tryClickByTextPrefix(page, step, vars, timeout)) break;
            throw e;
          }
        }
        break;
      }

      case 'fill': {
        const loc = getLocator();
        const forceFill = step.force === true;
        try {
          await loc.first().click({ timeout, force: forceFill });
          await loc.first().fill(value);
        } catch (e: any) {
          if (e.message?.includes('intercept') || e.message?.includes('overlay')) {
            // Tier 3: reactive dismiss BEFORE force-retry
            const dismissed = await this.tryReactiveDismiss(page, step, JUNK_SELECTORS);
            if (dismissed) {
              try {
                await loc.first().click({ timeout });
                await loc.first().fill(value);
                if (step.pressAfter) await page.keyboard.press(step.pressAfter);
                break;
              } catch (e2: any) { e = e2; }
            }
            // Fall through to force-retry (needed for combobox opacity:0)
            console.log(`  [retry] Fill intercepted, retrying with force: "${step.name}"`);
            try {
              await loc.first().click({ timeout, force: true });
              await loc.first().fill(value);
              if (step.pressAfter) await page.keyboard.press(step.pressAfter);
              break;
            } catch (e2: any) { e = e2; }
          }
          const cands = this.fallbackSelectors(step, vars);
          if (cands.length) {
            let ok = false; let lastErr: any = e;
            for (const css of cands) {
              try {
                console.log(`  [fallback] primary failed, trying selector "${css}"`);
                const fLoc = page.locator(css).first();
                await fLoc.waitFor({ state: 'attached', timeout: Math.min(timeout, 8000) });
                await fLoc.click({ timeout, force: true });
                await fLoc.fill(value);
                ok = true; break;
              } catch (e2: any) { lastErr = e2; }
            }
            if (!ok) throw lastErr;
          } else {
            throw e;
          }
        }
        if (step.pressAfter) {
          await page.keyboard.press(step.pressAfter);
        }
        break;
      }

      case 'keyboard':
        await page.keyboard.press(key);
        break;

      case 'select': {
        const loc = getLocator();
        await loc.selectOption(value);
        break;
      }

      // Ant Select / combobox semantic: mở trigger → (search) gõ lọc → chọn option theo TÊN từ page ROOT
      // (xuyên portal — dropdown Ant render ngoài body). Né flaky của 2-click thô.
      case 'selectOption': {
        if (!step.trigger) throw new Error(`Step "${step.name}": selectOption thiếu 'trigger'`);
        const trigger = locatorFromFields(step.trigger);
        // 1. Mở dropdown. Input Ant thường opacity:0/width:0 → force.
        await trigger.first().waitFor({ state: 'attached', timeout });
        await trigger.first().click({ timeout, force: true });
        // 2. Gõ lọc nếu combobox có search. Gõ `searchText` (text user thực gõ) — KHÁC `value` (text option
        //    chọn): option label có thể dài hơn cái user gõ. Fallback `value` nếu chưa có searchText.
        const filterText = this.interpolate(step.searchText, vars) || value;
        if ((step.search || step.searchText) && filterText) {
          await page.keyboard.type(filterText);
          await page.waitForTimeout(400);
        }
        // 3. Chọn option theo TÊN từ page root. Ưu tiên role=option; nếu Ant đặt role=option ở node ẩn
        //    thì fallback sang option HIỂN THỊ trong dropdown đang mở (lọc theo text + :visible).
        const byRole = page.getByRole('option', { name: value, exact: false })
          .locator('visible=true').first();
        const byVisible = page.locator('.ant-select-dropdown:visible .ant-select-item-option, .ant-cascader-menu:visible .ant-cascader-menu-item')
          .filter({ hasText: value }).first();
        let optionLoc = byRole;
        try {
          await byRole.waitFor({ state: 'visible', timeout: Math.min(timeout, 5000) });
        } catch {
          optionLoc = byVisible;
          await optionLoc.waitFor({ state: 'visible', timeout });
        }
        await optionLoc.scrollIntoViewIfNeeded().catch(() => { /* virtual list */ });
        await optionLoc.click({ timeout });
        break;
      }

      case 'check': {
        const loc = getLocator();
        await loc.check({ timeout });
        break;
      }

      case 'uncheck': {
        const loc = getLocator();
        await loc.uncheck({ timeout });
        break;
      }

      case 'hover': {
        const loc = getLocator();
        await loc.first().waitFor({ state: 'visible', timeout });
        await loc.first().hover({ timeout });
        // Brief wait for hover-revealed elements to appear (CSS transition)
        await page.waitForTimeout(300);
        break;
      }

      case 'wait':
        if (selector) {
          await page.locator(selector).first().waitFor({ state: 'visible', timeout });
        } else if (value) {
          await page.waitForURL(value, { timeout });
        } else if (step.delay) {
          await page.waitForTimeout(step.delay);
        }
        break;

      case 'screenshot': {
        const screenshotPath = value || `screenshots/${Date.now()}.png`;
        const dir = path.dirname(path.resolve(this.flowsDir, '..', screenshotPath));
        if (!fs.existsSync(dir)) fs.mkdirSync(dir, { recursive: true });
        await page.screenshot({
          path: path.resolve(this.flowsDir, '..', screenshotPath),
          fullPage: true
        });
        break;
      }

      case 'upload': {
        const loc = getLocator();
        await loc.setInputFiles(value);
        break;
      }

      case 'scroll': {
        const scrollPos = step.scrollTo || { x: 0, y: 0 };
        if (selector) {
          await page.locator(selector).first().evaluate((el, pos) => {
            el.scrollTo(pos.x || 0, pos.y || 0);
          }, scrollPos);
        } else {
          await page.evaluate((pos) => window.scrollTo(pos.x || 0, pos.y || 0), scrollPos);
        }
        break;
      }

      case 'assert': {
        // Use smart locator (role > label > placeholder > text > testId > selector)
        // Some assert types (url, title) don't need an element locator
        let loc: ReturnType<typeof getLocator> | null = null;
        try {
          loc = getLocator();
        } catch {
          loc = null; // OK for assertType 'url', 'title' — no element needed
        }
        // If primary locator can't even attach, fall back to selectorFallback candidates (theo thứ tự).
        if (loc && step.selectorFallback) {
          try {
            await loc.first().waitFor({ state: 'attached', timeout });
          } catch {
            for (const css of this.fallbackSelectors(step, vars)) {
              const fl = page.locator(css);
              try { await fl.first().waitFor({ state: 'attached', timeout: Math.min(timeout, 8000) }); loc = fl; break; } catch { /* next */ }
            }
          }
        }

        switch (step.assertType) {
          case 'visible':
            if (!loc) throw new Error(`Assert visible failed: no locator specified for step "${step.name}"`);
            await loc.waitFor({ state: 'visible', timeout });
            break;
          case 'hidden':
            if (!loc) throw new Error(`Assert hidden failed: no locator specified for step "${step.name}"`);
            await loc.waitFor({ state: 'hidden', timeout });
            break;
          case 'text': {
            if (!loc) throw new Error(`Assert text failed: no locator specified for step "${step.name}"`);
            // For form fields, textContent returns empty; we need inputValue/.value.
            const tagName = await loc.first().evaluate((el: any) => el.tagName).catch(() => '');
            let assertText: string | null;
            if (['INPUT','TEXTAREA','SELECT'].includes(tagName)) {
              assertText = await loc.first().inputValue({ timeout });
            } else {
              assertText = await loc.first().textContent({ timeout });
            }
            if (!assertText?.includes(String(step.expected))) {
              throw new Error(`Assert text failed: expected "${step.expected}", got "${assertText}"`);
            }
            break;
          }
          case 'url': {
            const url = page.url();
            if (!url.includes(String(step.expected))) {
              throw new Error(`Assert URL failed: expected "${step.expected}" in "${url}"`);
            }
            break;
          }
          case 'title': {
            const title = await page.title();
            if (!title.includes(String(step.expected))) {
              throw new Error(`Assert title failed: expected "${step.expected}", got "${title}"`);
            }
            break;
          }
          case 'count':
            if (!loc) throw new Error(`Assert count failed: no locator specified for step "${step.name}"`);
            const assertCount = await loc.count();
            if (assertCount !== Number(step.expected)) {
              throw new Error(`Assert count failed: expected ${step.expected}, got ${assertCount}`);
            }
            break;
          case 'containsAll':
            if (!loc) throw new Error(`Assert containsAll failed: no locator specified for step "${step.name}"`);
            const raw = step.expected;
            const expectedValues: string[] = Array.isArray(raw) ? raw.map(String) : [String(raw || '')];
            const allElements = await loc.all();
            let found = false;
            for (const el of allElements) {
              const content = await el.textContent() || '';
              if (expectedValues.every((v: string) => content.includes(String(v)))) {
                found = true;
                break;
              }
            }
            if (!found) {
              throw new Error(`Assert containsAll failed: no element contains all of [${expectedValues.join(', ')}]`);
            }
            break;
        }
        break;
      }

      default:
        throw new Error(`Unknown action: ${step.action}`);
    }

    // Post-action waits
    if (step.waitAfter) {
      if (typeof step.waitAfter === 'number') {
        await page.waitForTimeout(step.waitAfter);
      } else {
        switch (step.waitAfter) {
          case 'networkIdle':
            await page.waitForLoadState('networkidle');
            break;
          case 'domContentLoaded':
            await page.waitForLoadState('domcontentloaded');
            break;
          case 'load':
            await page.waitForLoadState('load');
            break;
        }
      }
    }

    if (step.waitFor) {
      await page.locator(this.interpolate(step.waitFor, vars)).waitFor({ timeout });
    }

    if (step.delay) {
      await page.waitForTimeout(step.delay);
    }
  }

  /**
   * Tier 3 reactive dismiss: called when a step fails with intercept/overlay error.
   * - junk selector match → closes it → returns true (caller should retry)
   * - functional modal (has form inputs) → throws descriptive error
   * - protected modal → throws descriptive error
   * - unknown/unidentifiable → returns false (caller falls through to force-retry)
   *
   * MUST NOT remove the force-retry fallback in callers — comboboxes depend on it.
   */
  private async tryReactiveDismiss(
    page: Page,
    step: FlowStep,
    junkSelectors: string
  ): Promise<boolean> {
    try {
      const result = await page.evaluate(
        ({ junkSel, functionalSel }: { junkSel: string; functionalSel: string }) => {
          const viewW = window.innerWidth;
          const viewH = window.innerHeight;
          const candidates = [
            document.elementFromPoint(viewW / 2, viewH / 2),
            document.elementFromPoint(viewW / 2, viewH / 3),
          ].filter(Boolean) as Element[];

          function findOverlay(el: Element): Element | null {
            let cur: Element | null = el;
            while (cur && cur !== document.body) {
              const style = window.getComputedStyle(cur);
              if (
                cur.classList.contains('ant-modal-wrap') ||
                cur.classList.contains('ant-modal') ||
                cur.classList.contains('modal') ||
                style.position === 'fixed' ||
                style.zIndex !== 'auto'
              ) return cur;
              cur = cur.parentElement;
            }
            return null;
          }

          for (const candidate of candidates) {
            const overlay = findOverlay(candidate);
            if (!overlay) continue;

            if (overlay.hasAttribute('data-frt-protected')) {
              return { action: 'protected' };
            }

            const junkList = junkSel.split(',');
            for (const sel of junkList) {
              if (overlay.matches(sel.trim())) {
                const closeBtn = overlay.querySelector(
                  'button[aria-label*="close" i],[class*="close"]'
                ) as HTMLElement | null;
                if (closeBtn) { closeBtn.click(); return { action: 'closed' }; }
                (overlay as HTMLElement).style.display = 'none';
                return { action: 'closed' };
              }
            }

            if (!!overlay.querySelector(functionalSel)) {
              return { action: 'functional' };
            }

            const closeBtn = overlay.querySelector(
              'button[aria-label*="close" i],[class*="close"]'
            ) as HTMLElement | null;
            if (closeBtn) { closeBtn.click(); return { action: 'closed' }; }

            return { action: 'unknown' };
          }
          return { action: 'none' };
        },
        { junkSel: junkSelectors, functionalSel: FUNCTIONAL_MODAL_SELECTOR }
      );

      if (result.action === 'protected') {
        throw new Error(
          `Protected modal đang chặn step '${step.name || step.action}' — không tự đóng. Kiểm tra flow đã đóng modal trước step này chưa.`
        );
      }
      if (result.action === 'functional') {
        throw new Error(
          `Modal lạ có form đang chặn step '${step.name || step.action}' — không tự đóng (ngầu flow thay đổi). Hãy kiểm tra UI đã có modal mới.`
        );
      }
      if (result.action === 'closed') {
        console.log(`  [reactive-dismiss] Đóng overlay rác trước step '${step.name || step.action}'`);
        return true;
      }
      return false;
    } catch (err: any) {
      if (
        err.message?.includes('Protected modal') ||
        err.message?.includes('Modal lạ')
      ) throw err;
      return false;
    }
  }

  /**
   * Run a shared flow (imported via 'use' keyword)
   * Resolution order: 1) flowsDir/{path} (app-specific), 2) rootFlowsDir/{path} (global)
   */
  private async runSharedFlow(page: Page, flowPath: string, vars: FlowVariable): Promise<void> {
    const appSpecific = path.resolve(this.flowsDir, flowPath);
    const globalPath = path.resolve(this.rootFlowsDir, flowPath);
    const resolvedPath = fs.existsSync(appSpecific) ? appSpecific :
                          fs.existsSync(globalPath) ? globalPath : null;
    if (!resolvedPath) {
      throw new Error(`Shared flow "${flowPath}" not found. Searched:\n  - ${appSpecific}\n  - ${globalPath}`);
    }

    const sharedFlow = this.loadFlow(resolvedPath);
    const mergedVars = { ...sharedFlow.variables, ...vars };

    for (const step of sharedFlow.steps) {
      await this.executeStep(page, step, mergedVars);
    }
  }

  /**
   * Run a complete flow
   */
  async run(flowPath: string, options?: {
    headless?: boolean;
    slowMo?: number;
    variables?: FlowVariable;
    /** Meta môi trường để gắn vào report (app/baseUrl/env). */
    meta?: { app?: string; baseUrl?: string; env?: string };
  }): Promise<RunResult> {
    const flow = this.loadFlow(flowPath);
    const startTime = Date.now();
    const stepResults: StepResult[] = [];

    // Merge variables: flow defaults < runtime overrides < env injection
    const vars: FlowVariable = {
      ...(flow.variables || {}),
      baseUrl: options?.meta?.baseUrl || flow.config?.baseUrl || '',
      env: options?.meta?.env || '',
      app: options?.meta?.app || '',
      ...(options?.variables || {}),
    };

    const config = flow.config || {};
    const headless = options?.headless ?? config.headless ?? false;
    const slowMo = options?.slowMo ?? config.slowMo ?? 0;

    // Meta môi trường — gắn vào mọi RunResult trả về để reporter render topbar/panel.
    const runMeta = {
      startedAt: startTime,
      app: options?.meta?.app,
      baseUrl: options?.meta?.baseUrl,
      env: options?.meta?.env,
      browser: 'Chromium',
      headless,
    };

    try {
      this.browser = await chromium.launch({
        headless,
        slowMo,
        args: [
          '--disable-notifications',
          '--disable-popup-blocking',
          '--disable-infobars',
          '--no-default-browser-check',
          '--disable-extensions',
        ],
      });
      this.context = await this.browser.newContext({
        ignoreHTTPSErrors: true,
        viewport: config.viewport || { width: 1440, height: 900 },
        permissions: [], // Block all permission prompts (notifications, geolocation, etc.)
      });

      // Auto-dismiss any browser dialogs (alert, confirm, prompt, beforeunload)
      this.page = await this.context.newPage();
      this.page.on('dialog', async (dialog) => {
        console.log(`  [auto-dismiss] Dialog: ${dialog.type()} — "${dialog.message()}"`);
        await dialog.accept();
      });

      // Inject script to auto-dismiss OneSignal/notification popups
      await this.page.addInitScript(() => {
        // Block OneSignal SDK from showing slidedown
        (window as any).OneSignal = (window as any).OneSignal || [];
        (window as any).OneSignal.push(() => {
          (window as any).OneSignal.setSubscription(false);
        });

        // Override Notification API to prevent permission requests
        Object.defineProperty(window, 'Notification', {
          value: class {
            static permission = 'denied';
            static requestPermission() { return Promise.resolve('denied'); }
            constructor() {}
          },
          writable: false,
        });

        // Auto-click common notification dismiss buttons after DOM loads
        const dismissSelectors = [
          '#onesignal-slidedown-cancel-button',
          '#onesignal-slidedown-allow-button',
          '[class*="onesignal"] button',
          '.onesignal-slidedown-dialog button:first-child',
        ];

        const observer = new MutationObserver(() => {
          for (const sel of dismissSelectors) {
            const btn = document.querySelector(sel) as HTMLElement;
            if (btn && btn.offsetParent !== null) {
              btn.click();
              return;
            }
          }
        });

        if (document.body) {
          observer.observe(document.body, { childList: true, subtree: true });
        } else {
          document.addEventListener('DOMContentLoaded', () => {
            observer.observe(document.body, { childList: true, subtree: true });
          });
        }
      });

      // Auto-dismiss popups/modals that appear mid-flow
      const popupMode = resolvePopupMode(config.autoPopupDismiss);
      if (popupMode !== 'off') {
        await this.page.addInitScript(
          ({ mode, junkSel, aggrSel }: { mode: string; junkSel: string; aggrSel: string }) => {
            const DANGER_WORDS = /xóa|delete|xác nhận xóa|confirm delete/i;
            const CLOSE_TEXTS = /^(×|✕|Đóng|Close|Cancel|Bỏ qua|Huỷ)$/i;
            const AUTO_DISAPPEAR = ['.ant-notification-notice', '.ant-message-notice'];

            function trySafeClose(popup: Element): void {
              for (const sel of AUTO_DISAPPEAR) {
                if (popup.matches(sel)) return;
              }
              const closeBtn = popup.querySelector(
                'button[aria-label*="close" i], [class*="close"]'
              ) as HTMLElement | null;
              if (closeBtn) {
                const txt = closeBtn.textContent?.trim() || closeBtn.getAttribute('aria-label') || '';
                if (DANGER_WORDS.test(txt)) return;
                closeBtn.click();
                return;
              }
              const buttons = Array.from(popup.querySelectorAll('button, span'));
              for (const btn of buttons) {
                const t = (btn as HTMLElement).textContent?.trim() || '';
                if (CLOSE_TEXTS.test(t)) {
                  if (DANGER_WORDS.test(t)) return;
                  (btn as HTMLElement).click();
                  return;
                }
              }
            }

            const selectorList = mode === 'aggressive' ? aggrSel : junkSel;
            let __frtPopupThrottle = 0;

            const __frtPopupObserver = new MutationObserver(() => {
              const now = Date.now();
              if (now - __frtPopupThrottle < 300) return;
              __frtPopupThrottle = now;
              const popups = Array.from(document.querySelectorAll(selectorList));
              for (const p of popups) {
                // In aggressive mode, skip protected modals
                if ((p as Element).hasAttribute('data-frt-protected')) continue;
                if ((p as HTMLElement).offsetParent !== null ||
                    (mode === 'aggressive' && p.matches('.ant-modal-wrap'))) {
                  trySafeClose(p);
                }
              }
            });

            if (document.body) {
              __frtPopupObserver.observe(document.body, { childList: true, subtree: true });
            } else {
              document.addEventListener('DOMContentLoaded', () => {
                __frtPopupObserver.observe(document.body, { childList: true, subtree: true });
              });
            }

            window.addEventListener('beforeunload', () => { __frtPopupObserver.disconnect(); });
          },
          { mode: popupMode, junkSel: JUNK_SELECTORS, aggrSel: AGGRESSIVE_SELECTORS }
        );
      }

      // Execute each step
      for (const step of flow.steps) {
        const stepStart = Date.now();
        try {
          // D2: aggressive mode only — mark functional modals as protected (best-effort safety net)
          if (popupMode === 'aggressive' && this.page) {
            const sel = (step as any).selector || '';
            if (sel && (sel.includes('.ant-modal') || sel.includes('[class*="ant-modal"]'))) {
              await this.page.evaluate(() => {
                const modal = document.querySelector('.ant-modal-wrap:not([data-frt-protected])');
                if (modal) modal.setAttribute('data-frt-protected', 'true');
              }).catch(() => { /* ignore — best effort */ });
            }
          }
          await this.executeStep(this.page, step, vars);
          const passedStep: StepResult = {
            name: step.name || step.use || 'unnamed',
            status: 'passed',
            duration: Date.now() - stepStart,
            action: step.action,
            startedAt: stepStart,
          };
          // Step verify (assert) → chụp ảnh base64 để nhúng vào report.
          if (step.action === 'assert') {
            passedStep.screenshot = await this.captureBase64(this.page);
          }
          stepResults.push(passedStep);
        } catch (err: any) {
          if (step.optional) {
            stepResults.push({
              name: step.name || step.use || 'unnamed',
              status: 'skipped',
              duration: Date.now() - stepStart,
              error: err.message,
              action: step.action,
              startedAt: stepStart,
            });
          } else {
            // \u1EA2nh base64 nh\u00FAng v\u00E0o report cho step fail.
            const failShot = await this.captureBase64(this.page);
            stepResults.push({
              name: step.name || step.use || 'unnamed',
              status: 'failed',
              duration: Date.now() - stepStart,
              error: err.message,
              action: step.action,
              startedAt: stepStart,
              screenshot: failShot,
            });

            // Screenshot on failure
            const screenshotDir = path.resolve(this.flowsDir, '../screenshots');
            if (!fs.existsSync(screenshotDir)) fs.mkdirSync(screenshotDir, { recursive: true });
            const slug = (s: string) => s.toLowerCase().replace(/[^a-z0-9\u00C0-\u024F\u1E00-\u1EFF]+/gi, '-').replace(/^-|-$/g, '').substring(0, 60);
            const now = new Date();
            const datetime = `${now.getFullYear()}-${String(now.getMonth()+1).padStart(2,'0')}-${String(now.getDate()).padStart(2,'0')}_${String(now.getHours()).padStart(2,'0')}-${String(now.getMinutes()).padStart(2,'0')}`;
            const screenshotPath = path.join(screenshotDir, `${slug(flow.name)}__${slug(step.name)}__${datetime}.png`);
            await this.page.screenshot({ path: screenshotPath, fullPage: true });

            return {
              flowName: flow.name,
              flowPath,
              status: 'failed',
              duration: Date.now() - startTime,
              steps: stepResults,
              error: `Step "${step.name}" failed: ${err.message}`,
              screenshotPath,
              ...runMeta,
            };
          }
        }
      }

      // Run assertions
      if (flow.assertions) {
        for (const assertion of flow.assertions) {
          const assertStep: FlowStep = {
            name: `Assert: ${assertion.message || assertion.type}`,
            action: 'assert',
            assertType: assertion.type as FlowStep['assertType'],
            selector: assertion.selector,
            expected: assertion.value,
          };
          const stepStart = Date.now();
          try {
            await this.executeStep(this.page, assertStep, vars);
            // Assertion luôn là verify → chụp ảnh base64.
            const shot = await this.captureBase64(this.page);
            stepResults.push({
              name: assertStep.name,
              status: 'passed',
              duration: Date.now() - stepStart,
              action: 'assert',
              startedAt: stepStart,
              screenshot: shot,
            });
          } catch (err: any) {
            const shot = await this.captureBase64(this.page);
            stepResults.push({
              name: assertStep.name,
              status: 'failed',
              duration: Date.now() - stepStart,
              error: err.message,
              action: 'assert',
              startedAt: stepStart,
              screenshot: shot,
            });
            return {
              flowName: flow.name,
              flowPath,
              status: 'failed',
              duration: Date.now() - startTime,
              steps: stepResults,
              error: err.message,
              ...runMeta,
            };
          }
        }
      }

      return {
        flowName: flow.name,
        flowPath,
        status: 'passed',
        duration: Date.now() - startTime,
        steps: stepResults,
        ...runMeta,
      };

    } finally {
      if (this.browser) await this.browser.close();
    }
  }

  /**
   * Run multiple flows by tag
   */
  async runByTag(tag: string, options?: { headless?: boolean; slowMo?: number; variables?: FlowVariable; meta?: { app?: string; baseUrl?: string; env?: string } }): Promise<RunResult[]> {
    const results: RunResult[] = [];
    const flowFiles = this.listFlows();

    for (const file of flowFiles) {
      const flow = this.loadFlow(file);
      if (flow.tags?.includes(tag)) {
        const result = await this.run(file, options);
        results.push(result);
      }
    }

    return results;
  }

  /**
   * Run all flows in directory
   */
  async runAll(options?: { headless?: boolean; slowMo?: number; variables?: FlowVariable; meta?: { app?: string; baseUrl?: string; env?: string } }): Promise<RunResult[]> {
    const results: RunResult[] = [];
    const flowFiles = this.listFlows();

    for (const file of flowFiles) {
      // Skip shared flows (in _shared/ folder)
      if (file.includes('_shared')) continue;
      const result = await this.run(file, options);
      results.push(result);
    }

    return results;
  }

  /**
   * List all flow files
   */
  listFlows(): string[] {
    if (!fs.existsSync(this.flowsDir)) return [];

    const files: string[] = [];
    const walk = (dir: string) => {
      const entries = fs.readdirSync(dir, { withFileTypes: true });
      for (const entry of entries) {
        const fullPath = path.join(dir, entry.name);
        if (entry.isDirectory()) {
          walk(fullPath);
        } else if (entry.name.endsWith('.yaml') || entry.name.endsWith('.yml')) {
          files.push(fullPath);
        }
      }
    };
    walk(this.flowsDir);
    return files;
  }
}
