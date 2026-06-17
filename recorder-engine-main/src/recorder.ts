/**
 * FRT Test Recorder — Recording Engine
 * Bật browser, inject listener, capture actions → export YAML
 */

import { chromium, Browser, Page, BrowserContext } from '@playwright/test';
import * as fs from 'fs';
import * as path from 'path';
import * as yaml from 'js-yaml';
import * as readline from 'readline';
import { FlowFile, FlowStep, RecordedAction, LocatorInfo, RelativeLocator } from './types';

export class FlowRecorder {
  private browser: Browser | null = null;
  private context: BrowserContext | null = null;
  private page: Page | null = null;
  private actions: RecordedAction[] = [];
  private outputDir: string;

  constructor(outputDir: string = path.join(process.cwd(), 'flows')) {
    this.outputDir = outputDir;
  }

  private locatorSummary(loc: LocatorInfo | undefined): string {
    if (!loc) return '?';
    switch (loc.type) {
      case 'role': return `role=${loc.role} "${loc.name}"`;
      case 'label': return `label="${loc.label}"`;
      case 'placeholder': return `placeholder="${loc.placeholder}"`;
      case 'text': return `text="${loc.text}"`;
      case 'testId': return `testId="${loc.testId}"`;
      case 'selector': return loc.selector || '?';
      default: return '?';
    }
  }

  /**
   * Start recording session
   * Opens browser, injects event listeners, captures user actions
   */
  async start(options: {
    url: string;
    flowName: string;
    description?: string;
    tags?: string[];
    author?: string;
    app?: string;
    env?: string;
  }): Promise<void> {
    console.log('\n🎬 FRT Test Recorder — Starting...');
    console.log(`   URL: ${options.url}`);
    console.log(`   Flow: ${options.flowName}`);
    console.log('');
    console.log('   📝 Thao tác trên browser, mọi action sẽ được ghi lại.');
    console.log('   🎯 Ctrl+Shift+A = Assert mode (click element để verify giá trị).');
    console.log('   ⏹️  Nhấn Enter trong terminal để dừng recording.');
    console.log('   💡 Tip: Thao tác chậm, rõ ràng để script chính xác hơn.');
    console.log('');

    this.actions = [];

    // Launch browser (always headed for recording)
    this.browser = await chromium.launch({
      headless: false,
      slowMo: 50,
      args: [
        '--disable-notifications',
        '--disable-popup-blocking',
        '--disable-infobars',
        '--start-maximized',
      ],
    });

    this.context = await this.browser.newContext({
      ignoreHTTPSErrors: true,
      viewport: null, // No fixed viewport — user can resize/maximize freely
      permissions: [], // Block all permission prompts
    });

    this.page = await this.context.newPage();

    // Auto-dismiss browser dialogs (except assert mode prompts)
    this.page.on('dialog', async (dialog) => {
      if (dialog.type() === 'prompt') {
        // Don't auto-dismiss prompts — let tester interact
        return;
      }
      console.log(`  [auto-dismiss] Dialog: ${dialog.type()} — "${dialog.message()}"`);
      await dialog.accept();
    });

    // Listen to page events for recording — MUST await before goto so init script injects in time
    await this.setupListeners(this.page);

    // Navigate to start URL
    await this.page.goto(options.url, { waitUntil: 'domcontentloaded' });

    // Record initial goto
    this.actions.push({
      timestamp: Date.now(),
      action: 'goto',
      url: options.url,
    });

    // Wait for user to finish (press Enter)
    await this.waitForStop();

    // Generate YAML
    const flow = this.buildFlow(options);

    // Add recorded_against metadata
    if (options.app && options.env) {
      flow.recorded_against = {
        app: options.app,
        env: options.env,
        url: options.url,
        date: new Date().toISOString().slice(0, 10),
      };
    }

    const outputPath = this.saveFlow(flow, options.flowName, options.app);

    console.log(`\n✅ Recording saved: ${outputPath}`);
    console.log(`   Total steps: ${flow.steps.length}`);
    console.log(`   Run with: frt-test play${options.app ? ` --app ${options.app}` : ''}${options.env ? ` --env ${options.env}` : ''} ${path.basename(outputPath)}`);

    // Cleanup
    if (this.browser) await this.browser.close();
  }

  /**
   * Setup event listeners on page to capture actions
   * MUST be awaited before navigating, otherwise init script races with page load.
   */
  private async setupListeners(page: Page): Promise<void> {
    // Capture navigations
    page.on('framenavigated', (frame) => {
      if (frame === page.mainFrame()) {
        const url = frame.url();
        // Don't record about:blank or duplicate navigations
        if (url !== 'about:blank' && this.actions[this.actions.length - 1]?.url !== url) {
          this.actions.push({
            timestamp: Date.now(),
            action: 'navigation',
            url,
          });
        }
      }
    });

    // Expose a binding so browser code can call Node directly — more reliable than console.log channel
    await page.exposeBinding('__frtRecord', (_source, data: any) => {
      // Dedup: if console.log already captured this action (same locator within 200ms), skip
      const now = Date.now();
      if (this.actions.length > 0) {
        const last = this.actions[this.actions.length - 1];
        if (last.action === data.action && (now - last.timestamp) < 200) {
          if (data.action === 'click' && JSON.stringify(last.locator) === JSON.stringify(data.locator)) return;
          if (data.action === 'fill' && last.value === data.value && JSON.stringify(last.locator) === JSON.stringify(data.locator)) return;
        }
      }
      this.actions.push({
        timestamp: now,
        ...data,
      });
      const summary = data.action === 'click' ? `click ${this.locatorSummary(data.locator)}`
        : data.action === 'fill' ? `fill ${this.locatorSummary(data.locator)} = "${String(data.value).substring(0, 30)}"`
        : data.action === 'keyboard' ? `key ${data.key}`
        : data.action === 'assert' ? `assert ${data.assertType} "${data.expected}"`
        : data.action === 'scroll' ? `scroll (${data.scrollX}, ${data.scrollY})${data.selector ? ' on ' + data.selector : ''}`
        : data.action;
      console.log(`  [rec] ${summary}`);
    });

    // Console fallback — captures actions that survive page navigation (sync channel)
    page.on('console', (msg) => {
      if (msg.text().startsWith('__FRT_RECORD_DEBUG__:') || msg.text().startsWith('__FRT_RECORD_ERROR__:')) {
        console.log(`  [debug] ${msg.text()}`);
        return;
      }
      if (msg.text().startsWith('__FRT_RECORD__:')) {
        try {
          const data = JSON.parse(msg.text().replace('__FRT_RECORD__:', ''));
          const now = Date.now();
          // Dedup: if exposeBinding already captured this action, skip
          if (this.actions.length > 0) {
            const last = this.actions[this.actions.length - 1];
            if (last.action === data.action && (now - last.timestamp) < 200) {
              if (data.action === 'click' && JSON.stringify(last.locator) === JSON.stringify(data.locator)) return;
              if (data.action === 'fill' && last.value === data.value && JSON.stringify(last.locator) === JSON.stringify(data.locator)) return;
            }
          }
          this.actions.push({ timestamp: now, ...data });
          const summary = data.action === 'click' ? `click ${this.locatorSummary(data.locator)}`
            : data.action === 'fill' ? `fill ${this.locatorSummary(data.locator)} = "${String(data.value).substring(0, 30)}"`
            : data.action === 'keyboard' ? `key ${data.key}`
            : data.action === 'assert' ? `assert ${data.assertType} "${data.expected}"`
            : data.action === 'scroll' ? `scroll (${data.scrollX}, ${data.scrollY})${data.selector ? ' on ' + data.selector : ''}`
            : data.action;
          console.log(`  [rec] ${summary}`);
        } catch { /* ignore */ }
      }
    });

    // Inject recording script into page — await so it's registered before any navigation
    // @ts-ignore — this function runs in browser context, not Node
    await page.addInitScript(() => {
      // Polyfill esbuild/tsx helpers that get injected when transpiling this file.
      // Without this, nested `function` declarations crash with "__name is not defined".
      const g = globalThis as any;
      if (!g.__name) g.__name = (fn: any, _name?: string) => fn;
      if (!g.__publicField) g.__publicField = (obj: any, key: any, value: any) => { obj[key] = value; return value; };

      // One positional segment relative to siblings of the SAME tag — dùng cho relative target path.
      function nthSegment(node: any): string {
        const tag = node.tagName.toLowerCase();
        const parent = node.parentElement;
        if (!parent) return tag;
        const sameTag = Array.from(parent.children).filter((c: any) => c.tagName === node.tagName);
        if (sameTag.length > 1) return `${tag}:nth-of-type(${sameTag.indexOf(node) + 1})`;
        return tag;
      }

      const escAttr = (s: string) => String(s).replace(/\\/g, '\\\\').replace(/"/g, '\\"');
      // id "động" = chứa ':' (session) hoặc một chuỗi số ≥6 (timestamp/seq) → KHÔNG dùng trực tiếp.
      const isDynamicId = (id: string) => /[:]/.test(id) || /\d{6,}/.test(id);

      function tryStableSelector(node: any): string | null {
        if (!node || node === document.body) return null;
        if (node.id) {
          const id = node.id;
          if (!isDynamicId(id)) {
            // id tĩnh: #id nếu là token hợp lệ, còn lại [id="..."] cho CSS hợp lệ.
            return /^[A-Za-z][\w-]*$/.test(id) ? `#${id}` : `[id="${escAttr(id)}"]`;
          }
          // id động: thử lấy phần prefix ổn định (trước dấu ':' và trước chuỗi số dài).
          const prefix = id.split(':')[0].replace(/[_-]?\d{6,}.*$/, '');
          if (prefix && prefix.length >= 4 && /[A-Za-z]/.test(prefix)) {
            const sel = `[id^="${escAttr(prefix)}"]`;
            try { if (document.querySelectorAll(sel).length === 1) return sel; } catch { /* skip */ }
          }
          // không rút được prefix unique → bỏ qua id, để caller leo lên / chuyển sang relative.
        }
        const testId = node.getAttribute('data-testid') || node.getAttribute('data-test');
        if (testId) return `[data-testid="${testId}"]`;
        const name = node.getAttribute('name');
        if (name) return `[name="${name}"]`;
        if (node.className && typeof node.className === 'string') {
          const classes = node.className.split(' ').filter((c: string) => {
            if (!c) return false;
            if (c.startsWith('ng-') || c.startsWith('ant-') || c.startsWith('p-')) return false;
            if (/^css-[a-z0-9]+$/i.test(c)) return false;
            if (/^sc-[a-zA-Z]+-[a-zA-Z0-9]+$/.test(c)) return false;
            if (/^_[a-zA-Z0-9]{5,}$/.test(c)) return false;
            if (/^[a-zA-Z]{1,3}[0-9]{3,}$/.test(c)) return false;
            if (/^e[a-z0-9]{6,}$/i.test(c)) return false;
            if (/[[\]:&/!@#$%^*()+={}'"|\\<>,~`]/.test(c)) return false;
            return true;
          });
          if (classes.length > 0) {
            const selector = `.${classes[0]}`;
            try { if (document.querySelectorAll(selector).length === 1) return selector; } catch { /* skip */ }
          }
        }
        return null;
      }

      // Danh sách CSS candidate XẾP HẠNG (bền nhất trước): ant-title → own-stable → anchored-path → from-body.
      function getCSSCandidates(el: any): string[] {
        const out: string[] = [];
        const push = (s: string | null) => { if (s && out.indexOf(s) === -1) out.push(s); };

        try {
          const cls = ((el.className || '') + '');
          const title = el.getAttribute?.('title');
          if (title && /(?:ant-select-item-option|ant-cascader-menu-item)/.test(cls)) {
            push(`.ant-select-item-option[title="${escAttr(title)}"]`);
          }
        } catch { /* ignore */ }

        // ① chính element có neo ổn định.
        push(tryStableSelector(el));

        // ② neo tổ tiên ổn định gần nhất + ĐƯỜNG đi xuống đúng element (tránh trúng thẻ cha).
        {
          const segs: string[] = [nthSegment(el)];
          let cur: any = el.parentElement;
          for (let i = 0; i < 20 && cur && cur !== document.body; i++) {
            const anchor = tryStableSelector(cur);
            if (anchor) { push(`${anchor} > ${segs.join(' > ')}`); break; }
            segs.unshift(nthSegment(cur));
            cur = cur.parentElement;
          }
        }

        // ③ bí: chuỗi vị trí từ body.
        {
          const parts: string[] = [];
          let cur: any = el;
          while (cur && cur !== document.body) { parts.unshift(nthSegment(cur)); cur = cur.parentElement; }
          push(parts.join(' > '));
        }

        return out.length ? out : [el.tagName ? el.tagName.toLowerCase() : '*'];
      }

      // 1 selector tốt nhất (cho primary / target).
      function getCSSSelector(el: any): string { return getCSSCandidates(el)[0]; }

      // Chuỗi fallback GỘP nhiều candidate xếp hạng vào MỘT field, ngăn bằng " || " — runner thử lần lượt
      // theo đúng thứ tự (khác comma CSS vốn lấy theo DOM-order). Không sinh ra mảng fallbacks[].
      function getCSSFallback(el: any): string { return getCSSCandidates(el).slice(0, 3).join(' || '); }

      // Walk up to find the nearest interactive ancestor — Ant Select / rc-select / custom
      // wrappers route clicks through deep leaf nodes; the meaningful locator lives higher up.
      function findInteractiveTarget(el: any): any {
        // Non-interactive roles that should NOT stop the walk-up.
        // These are presentational/decorative roles inside buttons/links — we want the parent.
        const nonInteractiveRoles = new Set([
          'img', 'presentation', 'none', 'separator', 'group',
          'list', 'listitem', 'cell', 'row', 'rowgroup',
          'figure', 'math', 'note', 'definition', 'term',
        ]);

        // Inline/leaf elements that commonly inherit cursor:pointer from parent —
        // we should NOT stop walk-up at these even if they have pointer cursor.
        const inlineLeafTags = new Set([
          'SPAN', 'I', 'EM', 'STRONG', 'B', 'SMALL', 'LABEL',
          'SVG', 'svg', 'PATH', 'path', 'CIRCLE', 'circle', 'RECT', 'rect',
          'G', 'g', 'USE', 'use', 'IMG',
        ]);

        let cur = el;
        // Special case: if inside Ant Select virtual list, find the list item (direct child of container)
        // This ensures we capture the specific option text, not a random inner div.
        const virtualListParent = el.closest?.('.rc-virtual-list-holder-inner');
        if (virtualListParent) {
          // Walk up to find the direct child of .rc-virtual-list-holder-inner (the whole option row).
          let item = el;
          while (item && item.parentElement !== virtualListParent) {
            item = item.parentElement;
          }
          if (item && item.parentElement === virtualListParent) {
            // NHƯNG: nếu user bấm một NÚT CON riêng trong option (vd selector đơn vị "đ/Vỉ", "đ/Hộp"),
            // giữ đúng nút đó — KHÔNG gom thành "chọn cả option" (bug bắt nhầm → mất lựa chọn đơn vị).
            const itemText = (item.textContent || '').replace(/\s+/g, ' ').trim();
            let n = el;
            while (n && n !== item) {
              const isBtn = n.tagName === 'BUTTON' || n.tagName === 'A';
              const role = n.getAttribute && n.getAttribute('role');
              const roleInteractive = role && !nonInteractiveRoles.has(role);
              let ptr = false;
              if (!inlineLeafTags.has(n.tagName)) {
                try { ptr = getComputedStyle(n).cursor === 'pointer'; } catch { /* ignore */ }
              }
              const nText = (n.textContent || '').replace(/\s+/g, ' ').trim();
              // Là control con thật sự: nút/role tương tác, HOẶC block cursor:pointer có text RIÊNG
              // (ngắn ≤40 và khác text cả option) → đó là nút đơn vị, trả về chính nó.
              if ((isBtn || roleInteractive || ptr) && nText && nText.length <= 40 && nText !== itemText) {
                return n;
              }
              n = n.parentElement;
            }
            return item;
          }
        }

        for (let i = 0; i < 7 && cur && cur !== document.body && cur.tagName !== 'HTML'; i++) {
          // Check interactive tagNames first — these are always meaningful targets
          if (['BUTTON','A','INPUT','SELECT','TEXTAREA'].includes(cur.tagName)) return cur;

          // Check role — but only INTERACTIVE roles (button, link, combobox, etc.)
          // Skip non-interactive roles like "img" (Ant icon spans), "presentation", etc.
          if (cur.getAttribute && cur.getAttribute('role')) {
            const r = cur.getAttribute('role');
            if (!nonInteractiveRoles.has(r)) return cur;
          }

          if (cur.getAttribute && (cur.getAttribute('data-testid') || cur.getAttribute('data-test'))) return cur;
          const cls = ((cur.className || '') + '');
          if (/(ant-select|rc-select-selector|ant-picker|ant-cascader)/.test(cls)) return cur;

          // cursor:pointer check — but ONLY for block-level/div-like elements.
          // Inline/leaf elements (span, svg, i, img) inherit cursor from parent, so stopping
          // here would miss the actual interactive ancestor (button, link, etc.)
          if (!inlineLeafTags.has(cur.tagName)) {
            try {
              const style = getComputedStyle(cur);
              if (style.cursor === 'pointer' && cur.tagName !== 'BODY') {
                // Walk up further: pick the OUTERMOST ancestor that still has cursor:pointer.
                // Reason: cell elements (e.g. customer-info__additional) inherit cursor:pointer
                // from the actual clickable row. Stopping at the first match captures the cell,
                // whose text is often non-unique (a date, a phone digit). The outermost match is
                // the real clickable wrapper (the row with onclick handler).
                let outermost = cur;
                let p = cur.parentElement;
                let pDepth = 0;
                while (p && pDepth < 12 && p !== document.body && p.tagName !== 'HTML') {
                  try {
                    const pStyle = getComputedStyle(p);
                    if (pStyle.cursor === 'pointer') {
                      outermost = p;
                    } else {
                      break;
                    }
                  } catch { break; }
                  p = p.parentElement;
                  pDepth++;
                }
                return outermost;
              }
            } catch {}
          }

          cur = cur.parentElement;
        }
        return el;
      }

      // Get visible text from an element, filtering out icon/hidden content
      function getVisibleText(el: any): string {
        if (!el) return '';
        // Skip elements that are purely decorative
        if (el.getAttribute && el.getAttribute('aria-hidden') === 'true') return '';
        if (el.getAttribute && el.getAttribute('role') === 'img') return '';
        if (el.tagName === 'SVG' || el.tagName === 'svg') return '';
        if (el.tagName === 'IMG' || el.tagName === 'img') return '';
        // For icon spans (anticon, material-icons, etc.) — skip
        const cls = ((el.className || '') + '');
        if (/(anticon|material-icons|fa-|icon-|svg-inline)/.test(cls)) return '';

        // Collect text from child nodes
        let text = '';
        for (const child of Array.from(el.childNodes) as any[]) {
          if (child.nodeType === 3) { // Text node
            text += child.textContent || '';
          } else if (child.nodeType === 1) { // Element node
            text += ' ' + getVisibleText(child);
          }
        }
        // Normalize whitespace: collapse newlines / runs of spaces into single spaces.
        // Playwright's getByText also normalizes, so this keeps the YAML clean and matches reliably.
        return text.replace(/\s+/g, ' ').trim();
      }

      // Smart locator detection — returns typed object with 6-level priority
      function getBestLocatorFlat(el: any): any {
        // 0. Ant Design / rc-select recognition — capture the wrapper as combobox with placeholder text
        const cls0 = ((el.className || '') + '');
        if (/(ant-select|rc-select-selector)/.test(cls0)) {
          const ph = el.querySelector('.ant-select-selection-placeholder, .rc-select-selection-placeholder');
          const name = ph?.textContent?.trim();
          if (name) return { type: 'role', role: 'combobox', name };
        }

        // 1. Role + accessible name
        const implicitRoles: Record<string, string> = {
          BUTTON: 'button', A: 'link', SELECT: 'combobox',
          H1: 'heading', H2: 'heading', H3: 'heading', H4: 'heading', H5: 'heading', H6: 'heading',
        };
        const inputRoles: Record<string, string> = {
          text: 'textbox', email: 'textbox', search: 'textbox', tel: 'textbox',
          url: 'textbox', number: 'textbox', checkbox: 'checkbox', radio: 'radio',
        };

        // Respect explicit role attribute — Ant uses <input role="combobox">, we MUST NOT override to textbox.
        const explicitRole = el.getAttribute('role');
        let role = explicitRole || implicitRoles[el.tagName] || null;
        if (!explicitRole && el.tagName === 'INPUT') role = inputRoles[el.type] || null;
        if (!explicitRole && el.tagName === 'TEXTAREA') role = 'textbox';
        if (el.tagName === 'A' && !el.getAttribute('href')) role = null;

        if (role) {
          let name = el.getAttribute('aria-label') || '';
          if (!name && el.getAttribute('aria-labelledby')) {
            const ref = document.getElementById(el.getAttribute('aria-labelledby'));
            if (ref) name = ref.textContent?.trim() || '';
          }
          if (!name && (el.tagName === 'BUTTON' || el.tagName === 'A' || el.getAttribute('role') === 'button' || el.getAttribute('role') === 'link' || el.getAttribute('role') === 'menuitem' || el.getAttribute('role') === 'tab')) {
            // Get visible text — filter out icon/hidden content by collecting only text nodes
            // and visible child text (skip elements with role=img, aria-hidden, or zero-size icons)
            name = getVisibleText(el).substring(0, 50);
          }
          if (!name && el.getAttribute('title')) {
            name = el.getAttribute('title').trim().substring(0, 50);
          }
          if (!name && el.id) {
            const lbl = document.querySelector(`label[for="${el.id}"]`);
            if (lbl) name = lbl.textContent?.trim() || '';
          }
          if (name) return { type: 'role', role, name };
        }

        // 2. Label
        if (el.id) {
          const lbl = document.querySelector(`label[for="${el.id}"]`);
          if (lbl) {
            const labelText = lbl.textContent?.trim();
            if (labelText) return { type: 'label', label: labelText };
          }
        }

        // 3. Placeholder
        if ((el.tagName === 'INPUT' || el.tagName === 'TEXTAREA') && el.placeholder) {
          return { type: 'placeholder', placeholder: el.placeholder };
        }

        // 4. Text (short text on clickable elements — including list items, divs with cursor:pointer)
        {
          const txt = getVisibleText(el);
          if (txt && txt.length > 0) {
            // 4a. Dropdown / list options — accept up to 200 chars (substring match by getByText handles uniqueness).
            // Shop addresses, descriptive options, etc. routinely exceed 50 chars; falling back to CSS
            // walks up to the virtual-list container and clicks the wrong item.
            const cls4 = ((el.className || '') + '');
            const isDropdownOption =
              el.getAttribute?.('role') === 'option' ||
              el.closest?.('.rc-virtual-list-holder-inner') ||
              el.closest?.('.ant-select-dropdown') ||
              el.closest?.('.ant-cascader-menu') ||
              /(?:ant-select-item-option|ant-cascader-menu-item|ant-list-item|ant-menu-item|rc-select-item)/.test(cls4);
            if (isDropdownOption && txt.length <= 200) {
              // Ưu tiên nhất: option có thuộc tính label/title (số lô, mã) = ĐỊNH DANH ỔN ĐỊNH, unique
              // và KHÔNG dính text volatile (Tồn/giá). Tránh được lỗi rc-select nhân đôi node a11y ẩn
              // khiến mã text bị tính "ambiguous" → rớt sang relative → neo nhầm "Tồn 999956".
              const optEl = (el.matches && el.matches('.ant-select-item-option, .ant-cascader-menu-item'))
                ? el : (el.closest && el.closest('.ant-select-item-option, .ant-cascader-menu-item'));
              if (optEl) {
                const lbl = optEl.getAttribute && optEl.getAttribute('label');
                const ttl = optEl.getAttribute && optEl.getAttribute('title');
                if (lbl && lbl.trim()) return { type: 'selector', selector: `.ant-select-item-option[label="${escAttr(lbl)}"]` };
                if (ttl && ttl.trim()) return { type: 'selector', selector: `.ant-select-item-option[title="${escAttr(ttl)}"]` };
              }
              // Ưu tiên neo theo MÃ (token số ≥4 chữ số, có/không '#') — là định danh ổn định của option
              // (vd "#00030458"), tránh neo nhầm thuộc tính chung như "Việt Nam" hay phần text có tồn kho.
              const code = txt.match(/#?\d{4,}/);
              if (code) return { type: 'text', text: code[0] };
              // Nút đơn vị/giá kiểu "50,000 đ/Vỉ" → neo theo ĐƠN VỊ "đ/Vỉ" (bỏ giá volatile, vẫn phân biệt Viên/Vỉ/Hộp).
              const unit = txt.match(/[đ₫]\s*\/\s*\S+/);
              if (unit) return { type: 'text', text: unit[0].replace(/\s+/g, '') };
              return { type: 'text', text: txt };
            }

            // 4b. Standard short-text path — buttons/links/clickable divs with concise labels.
            if (txt.length <= 50) {
              if (el.tagName === 'BUTTON' || el.tagName === 'A' || el.getAttribute('role') === 'button' || el.getAttribute('role') === 'link' || el.getAttribute('role') === 'menuitem' || el.getAttribute('role') === 'tab') {
                return { type: 'text', text: txt };
              }
              try {
                const style = getComputedStyle(el);
                if (style.cursor === 'pointer' || el.onclick) {
                  return { type: 'text', text: txt };
                }
              } catch {}
            }
          }
        }

        // 5. TestId
        const testId = el.getAttribute('data-testid') || el.getAttribute('data-test');
        if (testId) return { type: 'testId', testId };

        // 6. CSS selector fallback
        return { type: 'selector', selector: getCSSSelector(el) };
      }

      // ================= Text-Anchor (relative) locator heuristics =================
      // Slug hóa chuỗi -> khóa biến (bỏ dấu tiếng Việt).
      function slug(s: any): string {
        return (s || '').toLowerCase().normalize('NFD').replace(/[̀-ͯ]/g, '')
          .replace(/đ/g, 'd').replace(/[^a-z0-9]+/g, '_').replace(/^_+|_+$/g, '').slice(0, 30);
      }

      // Đếm xấp xỉ số match của một locator phẳng trên trang (để phát hiện ambiguous).
      function countFlatMatches(flat: any): number {
        try {
          if (!flat) return 0;
          if (flat.type === 'selector') {
            try { return document.querySelectorAll(flat.selector).length; } catch { return 1; }
          }
          if (flat.type === 'text') {
            // Mô phỏng getByText: khớp theo SUBSTRING, đếm element NHỎ NHẤT chứa text (không con nào chứa)
            // — text gộp qua nhiều span vẫn tính đúng 1 (trước đây chỉ xét leaf-exact → ra 0 → coi nhầm yếu).
            const norm = (flat.text || '').replace(/\s+/g, ' ').trim();
            const nrm = (e: any) => (e.textContent || '').replace(/\s+/g, ' ').trim();
            let n = 0;
            for (const e of Array.from(document.querySelectorAll('*')) as any[]) {
              if (!nrm(e).includes(norm)) continue;
              let childHas = false;
              for (const c of Array.from(e.children) as any[]) { if (nrm(c).includes(norm)) { childHas = true; break; } }
              if (!childHas) { n++; if (n > 5) break; }
            }
            return n;
          }
          if (flat.type === 'role') {
            const name = (flat.name || '').replace(/\s+/g, ' ').trim();
            const map: Record<string, string> = {
              button: 'button,[role=button]', link: 'a[href],[role=link]', menuitem: '[role=menuitem]',
              tab: '[role=tab]', checkbox: 'input[type=checkbox],[role=checkbox]', radio: 'input[type=radio],[role=radio]',
              heading: 'h1,h2,h3,h4,h5,h6,[role=heading]',
            };
            const sel = map[flat.role] || ('[role="' + flat.role + '"]');
            let n = 0;
            for (const e of Array.from(document.querySelectorAll(sel)) as any[]) {
              const t = ((e.getAttribute && e.getAttribute('aria-label')) || getVisibleText(e) || '').replace(/\s+/g, ' ').trim();
              if (t === name || (name && t.includes(name))) { n++; if (n > 5) break; }
            }
            return n;
          }
          return 1;
        } catch { return 1; }
      }

      // Locator phẳng "yếu" = đã rớt nth-child/of-type, HOẶC text/role match >1 (ambiguous).
      function isWeakLocator(flat: any): boolean {
        if (!flat) return false;
        if (flat.type === 'selector' && /:nth-(?:of-type|child)\(/.test(flat.selector || '')) return true;
        // Hợp nhất: locator phẳng "yếu" khi KHÔNG bắt trúng đúng 1 element.
        //  - count > 1  → ambiguous (nút lặp ở nhiều dòng)
        //  - count = 0  → bắt không trúng (vd Ant combobox: getByRole(combobox,{name}) không khớp)
        // Cả hai đều chuyển sang neo-text (relative). Đây là cách combobox được lo CHUNG cơ chế.
        if ((flat.type === 'text' || flat.type === 'role') && countFlatMatches(flat) !== 1) return true;
        return false;
      }

      // Class "cấu trúc" (làm container/scope) — KHÁC luật định danh: cho phép ant-/ng-, chỉ loại hash.
      function isStructuralClass(c: string): boolean {
        if (!c) return false;
        if (/^css-[a-z0-9]+$/i.test(c)) return false;
        if (/^e[a-z0-9]{6,}$/i.test(c)) return false;
        if (/^sc-[a-zA-Z]+-/.test(c)) return false;
        if (/[[\]:&/!@#$%^*()+={}'"|\\<>,~`]/.test(c)) return false;
        // Class TRẠNG THÁI (active/selected/hover/focus/checked/open…) — CHỈ tồn tại lúc hover/record,
        // playback rỗng → target không trúng. (vd .ant-select-item-option-active). Không dùng làm locator.
        if (/(?:^|-)(?:active|selected|focus(?:ed)?|hover(?:ed)?|checked|open(?:ed)?|disabled|loading|highlight(?:ed)?|current|expanded|collapsed|pressed|dragging)$/i.test(c)) return false;
        return true;
      }

      // Text của các leaf trong root, bỏ phần nằm trong `exclude` (anchor ≠ target).
      function leafTextsExcluding(root: any, exclude: any): { text: string; node: any }[] {
        const out: { text: string; node: any }[] = [];
        const walk = (n: any, d: number) => {
          if (d > 6 || !n) return;
          if (n === exclude || (exclude && exclude.contains && exclude.contains(n))) return;
          if (!n.children || n.children.length === 0) {
            const t = getVisibleText(n); if (t) out.push({ text: t, node: n });
          } else for (const c of Array.from(n.children) as any[]) walk(c, d + 1);
        };
        walk(root, 0); return out;
      }

      // Anchor xấu: quá ngắn/dài, số/ngày/tiền thuần, hoặc thời gian tương đối.
      function isBadAnchor(t: string): boolean {
        return !t || t.length < 2 || t.length > 60
          || /^[\d\s.,:/%-]+$/.test(t)
          || /(trước|ago|vừa xong|\b\d{1,2}:\d{2}\b)/i.test(t);
      }

      // STATIC (label/header — bền) vs DATA (cell dữ liệu — đổi theo môi trường).
      function classifyAnchor(node: any): 'static' | 'data' {
        const inLabel = node.closest && node.closest('label, th, .ant-form-item-label, [class*="label"]');
        const t = getVisibleText(node);
        if (inLabel || /[:*]\s*$/.test(t)) return 'static';
        if (node.closest && node.closest('td, [role="cell"], [class*="value"], [class*="cell"]')) return 'data';
        return 'static';
      }

      function elIsClickable(el: any): boolean {
        if (!el) return false;
        if (el.tagName === 'A' || el.tagName === 'BUTTON') return true;
        const role = el.getAttribute && el.getAttribute('role');
        if (role && ['button', 'link', 'menuitem', 'tab', 'option', 'row'].includes(role)) return true;
        try { if (getComputedStyle(el).cursor === 'pointer') return true; } catch { /* ignore */ }
        return !!el.onclick;
      }

      // Các phần tử cùng vai trò "đơn vị lặp" (xấp xỉ getByRole bằng query DOM).
      function siblingsByRole(role: string): any[] {
        const extra = role === 'row' ? ',tr' : role === 'listitem' ? ',li' : '';
        try { return Array.from(document.querySelectorAll('[role="' + role + '"]' + extra)); } catch { return []; }
      }

      // Selector container tổng quát cho ancestor A (để filter có nghĩa) — Form A.
      function deriveContainerSelector(A: any): { by: 'role' | 'css'; role?: string; css?: string } | null {
        const roleByTag: Record<string, string> = { TR: 'row', LI: 'listitem' };
        const role = (A.getAttribute && A.getAttribute('role')) || roleByTag[A.tagName];
        if (role && ['row', 'listitem', 'option', 'article', 'treeitem'].includes(role)) return { by: 'role', role };
        for (const c of ((A.className || '') + '').split(' ').filter(isStructuralClass)) {
          try { if (document.querySelectorAll('.' + c).length >= 2) return { by: 'css', css: '.' + c }; } catch { /* ignore */ }
        }
        return null;
      }

      // Đếm leaf có đúng text này trên toàn trang (kiểm tra unique cho Form B).
      function countLeafTextOnPage(text: string): number {
        const norm = (text || '').replace(/\s+/g, ' ').trim();
        let n = 0;
        for (const e of Array.from(document.querySelectorAll('*')) as any[]) {
          if (e.children.length === 0 && (e.textContent || '').replace(/\s+/g, ' ').trim() === norm) { n++; if (n > 3) break; }
        }
        return n;
      }

      // Xpath leo từ anchor text lên đúng tổ tiên đích `el` (Form B).
      function deriveAncestorXPath(el: any, anchorNode: any): string {
        if (el.tagName === 'TR') return 'xpath=ancestor::tr[1]';
        if (el.tagName === 'LI') return 'xpath=ancestor::li[1]';
        for (const c of ((el.className || '') + '').split(' ').filter(isStructuralClass)) {
          try {
            if (document.querySelectorAll('.' + c).length >= 1)
              return 'xpath=ancestor::*[contains(concat(" ",normalize-space(@class)," ")," ' + c + ' ")][1]';
          } catch { /* ignore */ }
        }
        let depth = 0, cur = anchorNode;
        while (cur && cur !== el && depth < 15) { depth++; cur = cur.parentElement; }
        return 'xpath=ancestor::*[' + Math.max(depth, 1) + ']';
      }

      // Đặt tên biến cho anchor DATA: header cột (th cùng index) → label gần → null.
      function deriveVarName(node: any): string | null {
        try {
          const td = node.closest && node.closest('td');
          if (td) {
            const tr = td.closest('tr');
            const idx = tr ? Array.from(tr.children).indexOf(td) : -1;
            const table = td.closest('table');
            if (table && idx >= 0) {
              const ths = table.querySelectorAll('thead th, th');
              if (ths[idx]) { const h = slug(getVisibleText(ths[idx])); if (h) return h; }
            }
          }
        } catch { /* ignore */ }
        const fi = node.closest && node.closest('.ant-form-item');
        if (fi) { const l = fi.querySelector('.ant-form-item-label label, label'); if (l) { const s = slug(getVisibleText(l)); if (s) return s; } }
        return null;
      }

      function simpleFromFlat(flat: any): any {
        const s: any = { type: flat.type };
        for (const k of ['role', 'name', 'label', 'placeholder', 'text', 'testId', 'selector']) if (flat[k]) s[k] = flat[k];
        return s;
      }

      function relCssWithin(scopeEl: any, el: any): string {
        const parts: string[] = [];
        let cur = el;
        while (cur && cur !== scopeEl && cur !== document.body) { parts.unshift(nthSegment(cur)); cur = cur.parentElement; }
        return parts.join(' > ') || el.tagName.toLowerCase();
      }

      // Mô tả target TRONG scope: chỉ tin flat khi nó KHÔNG yếu (combobox role hỏng → bỏ qua),
      // sau đó thử role-attr / input-type / class cấu trúc unique trong scope (bắt được .ant-select); cuối là nth.
      function describeTargetWithinScope(el: any, scopeEl: any): any {
        const flat = getBestLocatorFlat(el);
        if (!isWeakLocator(flat) && ['role', 'placeholder', 'testId', 'label', 'text'].includes(flat.type)) {
          return simpleFromFlat(flat);
        }
        const role = el.getAttribute && el.getAttribute('role');
        const tag = el.tagName.toLowerCase();
        const tries: string[] = [];
        if (role) tries.push('[role="' + role + '"]');
        if (tag === 'input' && el.type) tries.push('input[type="' + el.type + '"]');
        // Class cấu trúc unique trong scope — bắt được .ant-select / .ant-picker… (combobox).
        for (const c of ((el.className || '') + '').split(' ').filter(isStructuralClass)) tries.push('.' + c);
        tries.push(tag);
        for (const sel of tries) {
          try { if (scopeEl.querySelectorAll(sel).length === 1) return { type: 'selector', selector: sel }; } catch { /* ignore */ }
        }
        return { type: 'selector', selector: relCssWithin(scopeEl, el) };
      }

      // Text "volatile" = giá trị hay đổi theo dữ liệu (tồn kho/số lượng/giá), KHÔNG nên làm anchor.
      // Nhận theo NGỮ NGHĨA, không chỉ đếm chữ số — vì "Tồn 21" chỉ có 2 số vẫn là tồn kho.
      function isVolatileText(t: string): boolean {
        return /\btồn\b/i.test(t)                                              // "Tồn N" — tồn kho
          || /\bsl\b|\bsố lượng\b/i.test(t)                          // số lượng
          || /\d[\d.,]*\s*(viên|vỉ|hộp|ống|chai|gói|lọ|tuýp|cái|sp|sản phẩm)\b/i.test(t) // số + đơn vị
          || /\d[\d.,]*\s*[đ₫]|[đ₫]\s*\d/.test(t)               // giá (đ/₫)
          || /\$\s*[\d.,]+|[\d.,]+\s*(USD|EUR|€)/.test(t)    // giá ($/€/USD/EUR)
          || /\d[\d.,]*\s*%/.test(t)                                        // phần trăm
          || /\d{1,2}[/\-.\\]\d{1,2}[/\-.\\]\d{2,4}/.test(t) // ngày tháng (dd/mm/yyyy)
          || /\d{3,}/.test(t);                                                       // chuỗi số dài (giữ luật cũ)
      }

      // Xếp hạng anchor: static < data; trong cùng nhóm, text volatile (tồn/SL/giá/số dài) bị phạt
      // để KHÔNG ưu tiên (vd ưu tiên "Hàng thường" hơn "Tồn 21"). Rồi tới text ngắn.
      function anchorRank(c: any): number {
        return (classifyAnchor(c.node) === 'static' ? 0 : 2) + (isVolatileText(c.text) ? 1 : 0);
      }

      // Tìm (container + text) leo lên: ưu tiên Form A (neo static); fallback Form B.
      function findTextAnchorScope(el: any): any {
        // Dropdown option row → Form A sẽ neo 1 option bằng class/nth (vd .ant-select-item-option-active —
        // class trạng thái, playback rỗng). Bỏ qua Form A, đi thẳng Form B: neo text option → ancestor option.
        const isOptionRow = !!(el && (
          (el.matches && el.matches('.ant-select-item-option, .ant-cascader-menu-item, [role="option"]')) ||
          (el.parentElement && el.parentElement.classList && el.parentElement.classList.contains('rc-virtual-list-holder-inner'))
        ));
        let A = el.parentElement;
        for (let d = 0; !isOptionRow && d < 12 && A && A !== document.body && A.tagName !== 'HTML'; d++) {
          const container = deriveContainerSelector(A);
          if (container) {
            const cands = leafTextsExcluding(A, el)
              .filter(c => !isBadAnchor(c.text))
              .sort((a, b) => (anchorRank(a) - anchorRank(b)) || (a.text.length - b.text.length));
            for (const cand of cands) {
              let all: any[];
              if (container.by === 'css') { try { all = Array.from(document.querySelectorAll(container.css!)); } catch { all = []; } }
              else all = siblingsByRole(container.role!);
              const hit = all.filter((c: any) => leafTextsExcluding(c, null).some(x => x.text === cand.text));
              if (hit.length === 1 && hit[0] === A) {
                return { form: 'A', scope: container, anchor: cand.text, anchorNode: cand.node, dynamic: classifyAnchor(cand.node) === 'data', scopeEl: A };
              }
            }
          }
          A = A.parentElement;
        }
        // Form B: chính `el` click được + chứa một leaf-text unique trên trang.
        if (elIsClickable(el)) {
          const inner = leafTextsExcluding(el, null)
            .filter(c => !isBadAnchor(c.text))
            .sort((a, b) => (anchorRank(a) - anchorRank(b)) || (a.text.length - b.text.length));
          for (const cand of inner) {
            if (countLeafTextOnPage(cand.text) === 1) {
              return { form: 'B', anchor: cand.text, anchorNode: cand.node, dynamic: classifyAnchor(cand.node) === 'data', scopeEl: el, ancestorXPath: deriveAncestorXPath(el, cand.node) };
            }
          }
        }
        return null;
      }

      // Dựng relative LocatorInfo; auto-variabilize anchor DATA (đẩy biến qua window.__frtPendingVars).
      function buildAnchorLocator(el: any): any {
        const f = findTextAnchorScope(el);
        if (!f) return null;
        if (f.scopeEl && f.scopeEl.closest && f.scopeEl.closest('.rc-virtual-list-holder-inner')) {
          try { console.log('__FRT_RECORD_DEBUG__:⚠ relative trong virtual-list — có thể fail khi dòng chưa render'); } catch { /* ignore */ }
        }
        let anchorText = f.anchor;
        if (f.dynamic) {
          let key = deriveVarName(f.anchorNode);
          if (!key) { (window as any).__frtVarCounter = ((window as any).__frtVarCounter || 0) + 1; key = 'dataAnchor' + (window as any).__frtVarCounter; }
          (window as any).__frtPendingVars = (window as any).__frtPendingVars || {};
          (window as any).__frtPendingVars[key] = f.anchor;
          anchorText = '{{' + key + '}}';
          try { console.log('__FRT_RECORD_DEBUG__:auto-var ' + key + '=' + f.anchor); } catch { /* ignore */ }
        }
        if (f.form === 'A') {
          return { type: 'relative', relative: { form: 'A', anchor: { text: anchorText, dynamic: !!f.dynamic }, scope: f.scope, target: describeTargetWithinScope(el, f.scopeEl) } };
        }
        return { type: 'relative', relative: { form: 'B', anchor: { text: anchorText, dynamic: !!f.dynamic }, ancestor: { xpath: f.ancestorXPath } } };
      }

      // ===== VERIFY → AUTO-PICK: resolve ngược locator để code TỰ chọn cái trúng đúng element click =====

      // Node "hiển thị" — loại node a11y ẩn (rc-select nhân đôi) + display:none. Dùng getClientRects làm
      // tiêu chí chính (giống Playwright); KHÔNG loại theo opacity/width (Ant combobox input opacity:0).
      function isVisibleNode(el: any): boolean {
        try {
          if (!el || !el.getClientRects) return true;
          if (el.getClientRects().length === 0) return false;
          const s = getComputedStyle(el);
          if (s.display === 'none' || s.visibility === 'hidden') return false;
          return true;
        } catch { return true; }
      }

      // Thay {{var}} bằng giá trị thật (buildAnchorLocator có thể đã biến hóa anchor DATA) để verify đúng.
      function litText(t: string): string {
        const m = /^\{\{(\w+)\}\}$/.exec(t || '');
        const pv = (window as any).__frtPendingVars;
        if (m && pv && pv[m[1]] != null) return String(pv[m[1]]);
        return t || '';
      }

      // Mô phỏng getByText: element NHỎ NHẤT chứa/khớp text (exact hoặc substring), theo DOM order.
      function findByText(root: any, text: string, exact: boolean, visibleOnly: boolean): any[] {
        const norm = (text || '').replace(/\s+/g, ' ').trim();
        const nrm = (e: any) => (e.textContent || '').replace(/\s+/g, ' ').trim();
        const out: any[] = [];
        let nodes: any[];
        try { nodes = Array.from(root.querySelectorAll('*')); } catch { return out; }
        for (const e of nodes) {
          const t = nrm(e);
          if (!(exact ? t === norm : t.includes(norm))) continue;
          let childHit = false;
          for (const c of Array.from(e.children) as any[]) { const ct = nrm(c); if (exact ? ct === norm : ct.includes(norm)) { childHit = true; break; } }
          if (childHit) continue;
          if (visibleOnly && !isVisibleNode(e)) continue;
          out.push(e);
        }
        return out;
      }

      // Mô phỏng getByRole(name) — reuse map; lọc theo accessible name (aria-label/visible text).
      function findByRole(root: any, role: string, name: string, visibleOnly: boolean): any[] {
        const map: Record<string, string> = {
          button: 'button,[role=button]', link: 'a[href],[role=link]', menuitem: '[role=menuitem]',
          tab: '[role=tab]', checkbox: 'input[type=checkbox],[role=checkbox]', radio: 'input[type=radio],[role=radio]',
          heading: 'h1,h2,h3,h4,h5,h6,[role=heading]', combobox: '[role=combobox]', option: '[role=option]',
        };
        const sel = map[role] || ('[role="' + role + '"]');
        const nm = (name || '').replace(/\s+/g, ' ').trim();
        const out: any[] = [];
        let nodes: any[];
        try { nodes = Array.from(root.querySelectorAll(sel)); } catch { return out; }
        for (const e of nodes) {
          if (visibleOnly && !isVisibleNode(e)) continue;
          if (!nm) { out.push(e); continue; }
          const t = ((e.getAttribute && e.getAttribute('aria-label')) || getVisibleText(e) || '').replace(/\s+/g, ' ').trim();
          if (t === nm || t.includes(nm)) out.push(e);
        }
        return out;
      }

      // Resolve một SimpleLocator (target của Form A) TRONG một root element.
      function resolveSimpleWithin(root: any, s: any, visibleOnly: boolean): any[] {
        try {
          switch (s.type) {
            case 'selector': { const r = Array.from(root.querySelectorAll(s.selector)) as any[]; return visibleOnly ? r.filter(isVisibleNode) : r; }
            case 'role': return findByRole(root, s.role, s.name, visibleOnly);
            case 'text': return findByText(root, s.text, false, visibleOnly);
            case 'placeholder': { const r = Array.from(root.querySelectorAll('[placeholder="' + escAttr(s.placeholder) + '"]')) as any[]; return visibleOnly ? r.filter(isVisibleNode) : r; }
            case 'testId': { const r = Array.from(root.querySelectorAll('[data-testid="' + escAttr(s.testId) + '"],[data-test="' + escAttr(s.testId) + '"]')) as any[]; return visibleOnly ? r.filter(isVisibleNode) : r; }
            case 'label': { const r = Array.from(root.querySelectorAll('[aria-label="' + escAttr(s.label) + '"]')) as any[]; return visibleOnly ? r.filter(isVisibleNode) : r; }
            default: return [];
          }
        } catch (e: any) { try { console.log('__FRT_RECORD_DEBUG__: resolveSimpleWithin err ' + (e && e.message)); } catch { /* ignore */ } return []; }
      }

      // Resolve một LocatorInfo NGƯỢC trên DOM — mirror đúng cách runner resolve (runner dùng .first()
      // → ta quan tâm phần tử [0]). Dùng để VERIFY locator có trúng đúng element click không.
      function resolveInPage(loc: any, visibleOnly: boolean): any[] {
        try {
          if (loc.type === 'relative') {
            const r = loc.relative;
            const anchorText = litText(r.anchor.text);
            if (r.form === 'B') {
              const anchors = findByText(document, anchorText, true, visibleOnly);
              const xp = (r.ancestor.xpath || '').replace(/^xpath=/, '');
              const out: any[] = [];
              for (const a of anchors) {
                try { const node = document.evaluate(xp, a, null, XPathResult.FIRST_ORDERED_NODE_TYPE, null).singleNodeValue; if (node && out.indexOf(node) === -1) out.push(node); } catch { /* ignore */ }
              }
              return out;
            }
            // Form A: scope.filter({has:anchor}).target
            const scopeNodes = r.scope.by === 'role'
              ? findByRole(document, r.scope.role, '', false)
              : (Array.from(document.querySelectorAll(r.scope.css)) as any[]);
            const scoped = scopeNodes.filter((s: any) => findByText(s, anchorText, true, false).length > 0);
            const out: any[] = [];
            for (const s of scoped) for (const n of resolveSimpleWithin(s, r.target, visibleOnly)) if (out.indexOf(n) === -1) out.push(n);
            return out;
          }
          if (loc.type === 'text') return findByText(document, loc.text, false, visibleOnly);
          if (loc.type === 'role') return findByRole(document, loc.role, loc.name, visibleOnly);
          return resolveSimpleWithin(document, loc, visibleOnly);
        } catch (e: any) { try { console.log('__FRT_RECORD_DEBUG__: resolveInPage err ' + (e && e.message)); } catch { /* ignore */ } return []; }
      }

      // el là ô MỞ một Ant Select/Picker/Cascader (trigger combobox) — KHÔNG phải option trong dropdown.
      function isAntComboboxTrigger(el: any): boolean {
        try { return !!(el && el.closest && el.closest('.ant-select, .ant-picker, .ant-cascader')); } catch { return false; }
      }

      // Độ bền của flat locator (nhỏ = bền hơn). Phạt volatile text.
      function rankFlat(f: any): number {
        let r = ({ testId: 0, role: 1, label: 1, placeholder: 1, text: 2, selector: 5 } as any)[f.type];
        if (r === undefined) r = 5;
        if (f.type === 'selector' && /^#|\[data-test/.test(f.selector || '')) r = 0;
        if (f.type === 'text' && isVolatileText(f.text || '')) r += 2;
        return r;
      }
      // Flat đủ "bền" để chốt LUÔN ở tầng 1 (khỏi sinh relative/css) — id/testid/role/label/placeholder/text-không-volatile.
      function flatStableEnough(f: any): boolean {
        if (f.type === 'role' || f.type === 'label' || f.type === 'placeholder' || f.type === 'testId') return true;
        if (f.type === 'selector' && /^#|\[data-test/.test(f.selector || '')) return true;
        if (f.type === 'text' && !isVolatileText(f.text || '')) return true;
        return false;
      }

      // VERIFY → AUTO-PICK (LAZY theo tầng độ bền: flat → relative → css). Sinh + resolve ngược + chọn
      // passer (trúng đúng element click) bền nhất, DỪNG sớm: click có flat ổn định + strong thì KHÔNG
      // chạy buildAnchorLocator/getCSSCandidates (vốn đắt). Trả { locator, fallback }.
      function getBestLocator(el: any): { locator: any; fallback: string } {
        (window as any).__frtPendingVars = null;
        let bestStrong: any = null; // {loc, rank} — any để TS khỏi narrow nhầm qua closure mutate
        let bestWeak: any = null;
        let relVars: any = null;
        const cssPassers: string[] = [];
        // verify 1 candidate: trả 'strong' | 'weak' | null; cập nhật best* theo rank.
        const consider = (loc: any, rank: number): 'strong' | 'weak' | null => {
          let nodes = resolveInPage(loc, true);
          if (!nodes.length) nodes = resolveInPage(loc, false); // visibility mis-detect → thử lại
          if (nodes[0] !== el) return null;                      // không trúng đúng element click → loại
          if (loc.type === 'selector') cssPassers.push(loc.selector);
          if (nodes.length === 1) { if (!bestStrong || rank < bestStrong.rank) bestStrong = { loc, rank }; return 'strong'; }
          if (!bestWeak || rank < bestWeak.rank) bestWeak = { loc, rank };
          return 'weak';
        };

        // Tầng 1 — flat (gọi getBestLocatorFlat 1 LẦN).
        const flat = getBestLocatorFlat(el);
        const flatRes = consider(flat, rankFlat(flat)); // flat là candidate đầu tiên → strong ⇒ bestStrong.loc===flat
        const tier1Done = flatRes === 'strong' && flatStableEnough(flat);

        // Tầng 2 — relative (chỉ khi flat chưa chốt chắc).
        if (!tier1Done) {
          try {
            const rel = buildAnchorLocator(el);
            relVars = (window as any).__frtPendingVars; // buildAnchorLocator có thể biến hóa anchor DATA
            if (rel) { const base = rel.relative.form === 'B' ? 3 : 4; const pen = isVolatileText(litText(rel.relative.anchor.text)) ? 1 : 0; consider(rel, base + pen); }
          } catch (e: any) { try { console.log('__FRT_RECORD_DEBUG__: buildAnchorLocator err ' + (e && e.message)); } catch { /* ignore */ } }
        }

        // Tầng 3 — css (chỉ khi CHƯA có strong passer nào; css chỉ bền bằng-hoặc-kém relative).
        if (!tier1Done && !bestStrong) {
          try {
            const css = getCSSCandidates(el);
            for (let i = 0; i < css.length; i++) {
              const sel = css[i];
              const rank = 5 + i + (/:nth-(?:of-type|child)\(/.test(sel) ? 2 : 0);
              if (consider({ type: 'selector', selector: sel }, rank) === 'strong') break; // css đã sort bền→kém
            }
          } catch (e: any) { try { console.log('__FRT_RECORD_DEBUG__: getCSSCandidates err ' + (e && e.message)); } catch { /* ignore */ } }
        }

        let verified = true;
        let primary: any = bestStrong ? bestStrong.loc : (bestWeak ? bestWeak.loc : null);
        if (!primary) { primary = flat; verified = false; } // lưới chót: không candidate nào verify được
        // Giữ biến anchor CHỈ khi primary là relative; ngược lại bỏ (tránh đính var thừa).
        (window as any).__frtPendingVars = (primary.type === 'relative') ? relVars : null;
        try {
          console.log('__FRT_RECORD_DEBUG__:picked ' + primary.type + (primary.type === 'relative' ? ' ' + primary.relative.form : '')
            + (verified ? ' (verified [0]===el)' : ' ⚠ no candidate verified — fallback flat'));
        } catch { /* ignore */ }
        const primarySel = primary.type === 'selector' ? primary.selector : null;
        let fallback = cssPassers.filter((s) => s !== primarySel).join(' || ');
        if (!fallback) { try { fallback = getCSSFallback(el); } catch { /* ignore */ } }
        return { locator: primary, fallback };
      }

      // Listen for clicks via pointerup (fires before 'click', less likely to be intercepted
      // by framework handlers or lost during navigation)
      let __lastRecordTime = 0;
      let __lastClickSelector = '';
      const RECORD_DEBOUNCE_MS = 1000;

      // Hover-to-reveal detection state
      let __lastHoverTarget: any = null;
      let __lastHoverTime = 0;
      let __hoverDebounceTimer: any = null;

      // Tìm element cần HOVER để lộ ra click target (menu/submenu/dropdown hover-to-reveal).
      // Bắt được cả popup PORTAL (Ant submenu render ra body) mà contains() KHÔNG bắt được —
      // dùng quan hệ cấu trúc/ARIA thay vì timing chuột.
      function findHoverTriggerForClick(clickEl: any): any {
        if (!clickEl || !clickEl.closest) return null;

        // 1. Ant Design submenu — inline (.ant-menu-sub) hoặc popup portal (.ant-menu-submenu-popup / #rc-menu-*-popup)
        let menuPopup = clickEl.closest('.ant-menu-submenu-popup, .ant-menu-sub');
        if (!menuPopup) {
          let p = clickEl;
          for (let i = 0; i < 12 && p && p !== document.body; i++) {
            if (p.id && /-popup$/.test(p.id) && /menu/i.test(p.id)) { menuPopup = p; break; }
            p = p.parentElement;
          }
        }
        if (menuPopup) {
          // a) title có aria-controls trỏ tới popup id (rc-menu chuẩn)
          if (menuPopup.id) {
            const byAria = document.querySelector('[aria-controls="' + menuPopup.id + '"]');
            if (byAria) return byAria;
            const sub = document.getElementById(menuPopup.id.replace(/-popup$/, ''));
            if (sub) { const t = sub.querySelector(':scope > .ant-menu-submenu-title'); if (t) return t; }
          }
          // b) submenu cha gần nhất của click target → title của nó
          const submenu = clickEl.closest('.ant-menu-submenu');
          if (submenu) { const t = submenu.querySelector(':scope > .ant-menu-submenu-title'); if (t) return t; }
          // c) submenu đang mở/active trên trang
          const openTitle = document.querySelector('.ant-menu-submenu-open > .ant-menu-submenu-title, .ant-menu-submenu-active > .ant-menu-submenu-title');
          if (openTitle) return openTitle;
        }

        // 2. ARIA chung: click trong [role=menu] mở bởi trigger [aria-haspopup]/[aria-controls]/[aria-owns]
        const ariaMenu = clickEl.closest('[role="menu"], [role="listbox"]');
        if (ariaMenu && ariaMenu.id) {
          const trig = document.querySelector('[aria-controls="' + ariaMenu.id + '"][aria-haspopup], [aria-owns="' + ariaMenu.id + '"]');
          if (trig) return trig;
        }

        // 3. Fallback timing: element vừa hover (trong cửa sổ thời gian) là TỔ TIÊN của click target.
        const HOVER_WINDOW_MS = 1500;
        if (__lastHoverTarget && (Date.now() - __lastHoverTime) < HOVER_WINDOW_MS) {
          const h = __lastHoverTarget;
          if (h.contains && h.contains(clickEl) && h !== clickEl) return h;
        }
        return null;
      }

      // Track pointerdown target to confirm it's a real click (not drag)
      let __pointerDownTarget: any = null;
      document.addEventListener('pointerdown', (e: any) => {
        __pointerDownTarget = e.target;
      }, true);

      document.addEventListener('pointerup', (e: any) => {
        try {
        const rawTarget = e.target;
        if (!rawTarget) return;
        // Only treat as click if pointerup is on same element (or close) as pointerdown
        // This filters out drag operations
        if (__pointerDownTarget !== rawTarget && __pointerDownTarget !== rawTarget.parentElement && rawTarget !== __pointerDownTarget?.parentElement) {
          console.log('__FRT_RECORD_DEBUG__:pointerup rejected (drag filter) target=' + (rawTarget.tagName || '?') + ' pointerdown=' + (__pointerDownTarget?.tagName || 'null'));
          __pointerDownTarget = null;
          return;
        }
        __pointerDownTarget = null;

        if (rawTarget.id === '__frt_assert_banner') return;
        const target = findInteractiveTarget(rawTarget);

        // Bỏ qua click vào backdrop/mask của popup — không ghi step thừa
        if (rawTarget.matches && (
          rawTarget.matches('.ant-modal-mask, .modal-backdrop, [class*="overlay"]') ||
          (rawTarget.classList && rawTarget.classList.contains('ant-modal-wrap') && rawTarget === target)
        )) {
          console.log('__FRT_RECORD_DEBUG__:[skip] popup backdrop click');
          return;
        }

        const text = target.textContent?.trim().substring(0, 50);

        if (__assertMode) {
          const locator = getBestLocatorFlat(target);
          const cssFallback = getCSSSelector(target);
          const currentText = text || '';

          const assertType = prompt(
            '🎯 Assert Type:\n1 = text (chứa text)\n2 = count (đếm số lượng)\n3 = containsAll (chứa tất cả giá trị)\n4 = visible\n\nChọn (1-4):',
            '1'
          );

          if (!assertType) {
            __assertMode = false;
            const banner = document.getElementById('__frt_assert_banner');
            if (banner) banner.remove();
            e.preventDefault();
            e.stopPropagation();
            return;
          }

          const typeMap: Record<string, string> = { '1': 'text', '2': 'count', '3': 'containsAll', '4': 'visible' };
          const chosenType = typeMap[assertType] || 'text';

          let expectedValue: string | null = currentText;
          if (chosenType === 'text') {
            expectedValue = prompt('Expected text (element phải chứa):', currentText);
          } else if (chosenType === 'count') {
            const count = document.querySelectorAll(cssFallback).length;
            expectedValue = prompt('Expected count (số lượng element):', String(count));
          } else if (chosenType === 'containsAll') {
            expectedValue = prompt('Expected values (phân cách bằng | ):\nVD: Phạm Hưng Thịnh|01/01/1997', currentText);
          }

          if (expectedValue === null) {
            __assertMode = false;
            const banner = document.getElementById('__frt_assert_banner');
            if (banner) banner.remove();
            e.preventDefault();
            e.stopPropagation();
            return;
          }

          // Assert mode bypasses debounce — always record
          __lastRecordTime = Date.now();

          // Auto-safe: text volatile (giá/ngày/%) → đổi sang visible để tránh flaky assert
          let effectiveType = chosenType;
          if (chosenType === 'text' && isVolatileText(expectedValue)) {
            console.log('__FRT_RECORD_DEBUG__: [auto-safe] volatile assert text → changed to visible: "' + expectedValue + '"');
            effectiveType = 'visible';
          }

          // Prefer text locator for text/visible asserts when expected is short and unambiguous.
          // Deep CSS chains rooted at table cells are fragile (40+ levels of div:nth-child).
          // getByText(expected) is semantic and survives DOM shuffles, and assertType=text
          // does textContent.includes(expected) anyway — finding any element with the text suffices.
          let finalLocator = locator;
          if ((effectiveType === 'text' || effectiveType === 'visible') &&
              typeof expectedValue === 'string' &&
              expectedValue.length >= 2 && expectedValue.length <= 100 &&
              locator.type === 'selector') {
            // Verify the expected text actually exists on page (sanity check)
            const norm = expectedValue.replace(/\s+/g, ' ').trim();
            try {
              const all = document.querySelectorAll('*');
              let matchCount = 0;
              for (const e2 of all as any) {
                if (e2.children.length === 0) {
                  const t2 = (e2.textContent || '').replace(/\s+/g, ' ').trim();
                  if (t2 === norm) { matchCount++; if (matchCount > 5) break; }
                }
              }
              if (matchCount >= 1) {
                finalLocator = { type: 'text', text: expectedValue };
              }
            } catch {}
          }

          (window as any).__frtRecord({
            action: 'assert',
            locator: finalLocator,
            selector: finalLocator.type === 'selector' ? finalLocator.selector : cssFallback,
            selectorFallback: getCSSFallback(target),
            text: currentText,
            assertType: effectiveType,
            expected: expectedValue,
          });

          __assertMode = false;
          const banner = document.getElementById('__frt_assert_banner');
          if (banner) banner.remove();
          e.preventDefault();
          e.stopPropagation();
          return;
        }

        // Debounce: ignore duplicate clicks on SAME element within 1s.
        // Clicks on DIFFERENT elements always pass (user intentionally clicking something else).
        const now = Date.now();
        const currentSelector = getCSSSelector(target);
        if (now - __lastRecordTime < RECORD_DEBOUNCE_MS && currentSelector === __lastClickSelector) return;
        __lastRecordTime = now;
        __lastClickSelector = currentSelector;

        // Detect hover-to-reveal: emit hover before click if click target was revealed by a hover.
        // Structural (Ant submenu / aria menu portal) + timing fallback. Bắt cả popup portal.
        const hoverTrigger = findHoverTriggerForClick(rawTarget);
        if (hoverTrigger && hoverTrigger !== target && hoverTrigger !== rawTarget) {
          const hoverLocator = getBestLocator(hoverTrigger);
          const hoverPayload = { action: 'hover', locator: hoverLocator.locator, selectorFallback: hoverLocator.fallback };
          console.log('__FRT_RECORD__:' + JSON.stringify(hoverPayload));
          (window as any).__frtRecord(hoverPayload);
          __lastHoverTarget = null;
        }

        const { locator, fallback } = getBestLocator(target);
        const payload: any = {
          action: 'click',
          locator,
          selectorFallback: fallback,
        };
        // Combobox semantic merge metadata (gộp open→[fill]→option thành 1 selectOption ở buildFlow).
        const inDropdown = rawTarget.closest && (rawTarget.closest('.ant-select-dropdown') || rawTarget.closest('.rc-virtual-list-holder-inner') || rawTarget.closest('.ant-cascader-menu'));
        if (inDropdown) {
          const optTxt = getVisibleText(target);
          if (optTxt) payload.optionText = optTxt.slice(0, 120);
        } else if (isAntComboboxTrigger(target)) {
          payload.comboboxOpen = true;
        }
        if ((window as any).__frtPendingVars) { payload.variables = (window as any).__frtPendingVars; (window as any).__frtPendingVars = null; }
        // Sync console.log FIRST — survives page navigation (unlike async exposeBinding)
        console.log('__FRT_RECORD__:' + JSON.stringify(payload));
        // Then async binding for faster processing when page doesn't navigate
        (window as any).__frtRecord(payload);
        } catch (err: any) {
          console.log('__FRT_RECORD_ERROR__:' + (err?.message || String(err)));
        }
      }, true);

      // Listen for input changes
      document.addEventListener('input', (e: any) => {
        const target = e.target;
        if (!target) return;

        // Skip checkbox/radio — they fire 'input' event but cannot be fill()'d in Playwright.
        // The click action already handles toggling them.
        if (target.tagName === 'INPUT' && (target.type === 'checkbox' || target.type === 'radio')) return;

        // No debounce for input — dedup logic in buildFlow handles multiple fills on same element.
        // But update lastRecordTime so clicks right after typing are debounced.
        __lastRecordTime = Date.now();

        const { locator, fallback } = getBestLocator(target);
        const fillPayload: any = {
          action: 'fill',
          locator,
          value: target.value,
          selectorFallback: fallback,
        };
        // Combobox search: fill gõ vào ô search của một Ant Select đang mở.
        if (target.closest && target.closest('.ant-select')) fillPayload.comboboxFill = true;
        if ((window as any).__frtPendingVars) { fillPayload.variables = (window as any).__frtPendingVars; (window as any).__frtPendingVars = null; }
        (window as any).__frtRecord(fillPayload);
      }, true);

      // Listen for scroll events (debounced — capture final position)
      let __scrollTimer: any = null;
      document.addEventListener('scroll', (e: any) => {
        const target = e.target;
        if (__scrollTimer) clearTimeout(__scrollTimer);
        __scrollTimer = setTimeout(() => {
          const isWindow = !target || target === document || target === document.documentElement;
          const x = isWindow ? window.scrollX : (target as Element).scrollLeft;
          const y = isWindow ? window.scrollY : (target as Element).scrollTop;
          // Only record meaningful scrolls
          if (x === 0 && y === 0) return;
          const payload: any = {
            action: 'scroll',
            scrollX: Math.round(x),
            scrollY: Math.round(y),
          };
          if (!isWindow) {
            payload.selector = getCSSSelector(target);
          }
          (window as any).__frtRecord(payload);
        }, 300);
      }, true);

      // Listen for mouseover — hover-to-reveal detection
      document.addEventListener('mouseover', (e: any) => {
        const t = e.target;
        if (!t) return;
        clearTimeout(__hoverDebounceTimer);
        __hoverDebounceTimer = setTimeout(() => {
          __lastHoverTarget = t;
          __lastHoverTime = Date.now();
        }, 150); // debounce 150ms
      }, true);

      // Listen for keyboard events (special keys only)
      let __assertMode = false;
      document.addEventListener('keydown', (e: any) => {
        // Ctrl+Shift+A = toggle assert mode
        if (e.ctrlKey && e.shiftKey && e.key === 'A') {
          __assertMode = !__assertMode;
          const banner = document.getElementById('__frt_assert_banner');
          if (__assertMode) {
            const div = document.createElement('div');
            div.id = '__frt_assert_banner';
            div.style.cssText = 'position:fixed;top:0;left:0;right:0;z-index:999999;background:#f59e0b;color:#000;padding:8px;text-align:center;font-weight:bold;font-size:14px;';
            div.textContent = '🎯 ASSERT MODE — Click element để verify. Ctrl+Shift+A để thoát.';
            document.body.appendChild(div);
          } else if (banner) {
            banner.remove();
          }
          e.preventDefault();
          return;
        }

        const specialKeys = ['Enter', 'Escape', 'Tab', 'F1', 'F2', 'F3', 'F4',
          'F5', 'F6', 'F7', 'F8', 'F9', 'F10', 'F11', 'F12'];

        let key = '';
        if (e.ctrlKey) key += 'Control+';
        if (e.shiftKey) key += 'Shift+';
        if (e.altKey) key += 'Alt+';

        if (specialKeys.includes(e.key) || key) {
          key += e.key;
          // Debounce keyboard too — but allow Enter/Tab immediately after fill (common pattern)
          const now = Date.now();
          if (key !== 'Enter' && key !== 'Tab' && now - __lastRecordTime < RECORD_DEBOUNCE_MS) return;
          __lastRecordTime = now;

          (window as any).__frtRecord({
            action: 'keyboard',
            key,
          });
        }
      }, true);
    });
  }

  /**
   * Wait for user to press Enter to stop recording
   */
  private waitForStop(): Promise<void> {
    return new Promise((resolve) => {
      const rl = readline.createInterface({
        input: process.stdin,
        output: process.stdout,
      });

      rl.question('\n⏹️  Nhấn Enter để dừng recording... ', () => {
        rl.close();
        resolve();
      });
    });
  }

  /**
   * Convert recorded actions to FlowFile structure
   */
  private buildFlow(options: {
    flowName: string;
    description?: string;
    tags?: string[];
    author?: string;
    url?: string;
  }): FlowFile {
    const steps: FlowStep[] = [];
    // Biến do auto-variabilize (anchor DATA) sinh ra — gộp vào flow.variables.
    const collectedVars: Record<string, string> = {};

    // Gộp chuỗi combobox (open → [fill search] → option-click) thành 1 action 'selectOption' semantic.
    const mergedActions = this.mergeComboboxActions(this.actions);

    // Deduplicate and clean actions
    let lastAction: RecordedAction | null = null;

    for (const action of mergedActions) {
      if (action.variables) Object.assign(collectedVars, action.variables);
      // Skip duplicate fills on the same element (keep last value).
      // Match by locator first; fall back to selector only when BOTH sides have a truthy selector.
      if (action.action === 'fill' && lastAction?.action === 'fill') {
        const sameLocator = action.locator && lastAction.locator
          && JSON.stringify(action.locator) === JSON.stringify(lastAction.locator);
        const sameSelector = !!action.selector && action.selector === lastAction.selector;
        if (sameLocator || sameSelector) {
          steps.pop();
        }
      }

      // Skip navigation events that are just redirects
      if (action.action === 'navigation') {
        // D1: Gắn waitAfter: networkIdle vào step click/fill cuối cùng
        // Navigation event = page thực sự navigate → step trước đó trigger page load
        if (steps.length > 0) {
          const lastStep = steps[steps.length - 1];
          if ((lastStep.action === 'click' || lastStep.action === 'fill') && !lastStep.waitAfter) {
            lastStep.waitAfter = 'networkIdle';
          }
        }
        lastAction = action;
        continue;
      }

      const step = this.actionToStep(action);
      if (step) {
        steps.push(step);
      }

      lastAction = action;
    }

    // Post-processing: merge Enter into fill, remove noise
    const cleanedSteps = this.postProcessSteps(steps);

    return {
      name: options.flowName,
      description: options.description || `Recorded flow: ${options.flowName}`,
      tags: options.tags || ['recorded'],
      author: options.author || 'recorder',
      created: new Date().toISOString().split('T')[0],
      config: {
        baseUrl: options.url || process.env.BASE_URL || 'http://localhost:3000',
        timeout: 30000,
      },
      variables: collectedVars,
      steps: cleanedSteps,
      assertions: [],
    };
  }

  /**
   * Chuyển LocatorInfo của bước MỞ combobox thành `trigger` cho step selectOption.
   * Giữ đủ field để runner resolve y như locator thường (role / relative / selector / placeholder…).
   */
  private locatorToTrigger(loc: LocatorInfo | undefined, selectorFallback?: string): FlowStep['trigger'] {
    const t: any = {};
    if (loc) {
      switch (loc.type) {
        case 'role': t.role = { type: loc.role, name: loc.name }; break;
        case 'relative': t.relative = loc.relative; break;
        case 'placeholder': t.placeholder = loc.placeholder; break;
        case 'text': t.text = loc.text; break;
        case 'testId': t.testId = loc.testId; break;
        case 'label': t.label = loc.label; break;
        case 'selector': t.selector = loc.selector; break;
      }
    }
    if (selectorFallback) t.selectorFallback = selectorFallback;
    return t;
  }

  /**
   * Gộp chuỗi combobox thành 1 action semantic 'selectOption':
   *   click(comboboxOpen) → [fill(comboboxFill)…] → click(optionText)
   * Player (runner) tự bung: mở trigger → (search) gõ value lọc → chọn option theo TÊN từ page root.
   * Bảo thủ: CHỈ gộp khi có cú click chọn option tường minh (optionText). "Gõ + Enter chọn" (không click
   * option) KHÔNG gộp — giữ nguyên click+fill+Enter như cũ.
   */
  private mergeComboboxActions(actions: RecordedAction[]): RecordedAction[] {
    const out: RecordedAction[] = [];
    for (let i = 0; i < actions.length; i++) {
      const a = actions[i];
      if (a.action === 'click' && a.comboboxOpen) {
        let j = i + 1;
        let search = false;
        let searchText: string | undefined;
        const vars: Record<string, string> = { ...(a.variables || {}) };
        // Nuốt các fill gõ-lọc trên cùng combobox (giữ text gõ cuối làm searchText).
        while (j < actions.length && actions[j].action === 'fill' && actions[j].comboboxFill) {
          search = true;
          if (actions[j].value) searchText = actions[j].value;
          if (actions[j].variables) Object.assign(vars, actions[j].variables);
          j++;
        }
        // Bỏ qua 1 phím Enter trung gian (gõ xong nhấn Enter rồi vẫn click option).
        if (j < actions.length && actions[j].action === 'keyboard' && actions[j].key === 'Enter') j++;
        // Phải có click chọn option tường minh.
        if (j < actions.length && actions[j].action === 'click' && actions[j].optionText) {
          const opt = actions[j];
          if (opt.variables) Object.assign(vars, opt.variables);
          out.push({
            timestamp: a.timestamp,
            action: 'selectOption',
            trigger: this.locatorToTrigger(a.locator, a.selectorFallback),
            value: opt.optionText,
            search,
            ...(searchText ? { searchText } : {}),
            ...(Object.keys(vars).length ? { variables: vars } : {}),
          } as RecordedAction);
          i = j; // tiêu thụ tới cú click option
          continue;
        }
      }
      out.push(a);
    }
    return out;
  }

  /**
   * Post-process steps:
   * 1. Merge "keyboard: Enter" into preceding "fill" as pressAfter: Enter
   * 2. Remove assert mode artifacts (Control+Control, Control+Shift+Shift)
   * 3. Remove click on form submit button if immediately after fill+Enter (redundant)
   */
  private postProcessSteps(steps: FlowStep[]): FlowStep[] {
    const result: FlowStep[] = [];

    for (let i = 0; i < steps.length; i++) {
      const step = steps[i];
      const nextStep = steps[i + 1];

      // Rule 1: Remove assert mode toggle artifacts
      if (step.action === 'keyboard') {
        const key = step.key || '';
        // Control+Control, Control+Shift+Shift, Shift+Shift are artifacts from Ctrl+Shift+A toggle
        if (key === 'Control+Control' || key === 'Control+Shift+Shift' || key === 'Shift+Shift' ||
            key === 'Control+Shift+A') {
          continue; // skip this step
        }
      }

      // Rule 2: Merge keyboard Enter into preceding fill step
      if (step.action === 'fill' && nextStep?.action === 'keyboard' && nextStep.key === 'Enter') {
        step.pressAfter = 'Enter';
        // Also check if the step AFTER Enter is a redundant form submit button click
        const stepAfterEnter = steps[i + 2];
        if (stepAfterEnter?.action === 'click') {
          const clickSelector = stepAfterEnter.selector || stepAfterEnter.selectorFallback || '';
          const isFormButton = /form.*button|button.*span.*svg|button:nth-child/i.test(clickSelector);
          if (isFormButton && !stepAfterEnter.role && !stepAfterEnter.text) {
            result.push(step);
            i += 2; // skip both Enter and redundant button click
            continue;
          }
        }
        result.push(step);
        i++; // skip the Enter step
        continue;
      }

      // Rule 3: If fill already has pressAfter: Enter, and next step is a click on a button
      // inside a form (CSS chain ending in "button" or "button > span"), skip the click
      // because Enter already submitted the form.
      if (step.action === 'fill' && step.pressAfter === 'Enter' && nextStep?.action === 'click') {
        const clickSelector = nextStep.selector || nextStep.selectorFallback || '';
        const isFormButton = /form.*button|button.*span.*svg|button:nth-child/i.test(clickSelector);
        if (isFormButton && !nextStep.role && !nextStep.text) {
          result.push(step);
          i++; // skip the redundant button click
          continue;
        }
      }

      // Rule 4: fill followed by click on form submit button (icon-only, no role/text)
      // User clicked search button instead of pressing Enter — same intent.
      if (step.action === 'fill' && nextStep?.action === 'click') {
        const clickSelector = nextStep.selector || nextStep.selectorFallback || '';
        const isFormSubmitButton = /form.*button|button.*span.*svg|button.*span.*span/i.test(clickSelector);
        if (isFormSubmitButton && !nextStep.role && !nextStep.text && !nextStep.label) {
          step.pressAfter = 'Enter';
          result.push(step);
          i++; // skip the button click step
          continue;
        }
      }

      result.push(step);
    }

    return result;
  }

  /**
   * Mô tả ngắn target của relative locator (cho tên step).
   */
  private describeTarget(rel?: RelativeLocator): string {
    if (!rel) return '?';
    if (rel.form === 'B') return 'mục';
    const t = rel.target;
    if (!t) return 'mục';
    if (t.type === 'role') return `${t.role} "${t.name || ''}"`;
    if (t.type === 'placeholder') return `ô "${t.placeholder}"`;
    if (t.type === 'text') return `"${t.text}"`;
    if (t.type === 'testId') return `testId "${t.testId}"`;
    return `"${t.selector}"`;
  }

  /**
   * Convert a single recorded action to a FlowStep
   */
  private actionToStep(action: RecordedAction): FlowStep | null {
    switch (action.action) {
      case 'goto':
        return {
          name: `Navigate to ${action.url}`,
          action: 'goto',
          value: action.url,
          waitAfter: 'networkIdle',
        };

      case 'click': {
        const step: FlowStep = { name: '', action: 'click' };
        if (action.selectorFallback) step.selectorFallback = action.selectorFallback;
        if (action.locator) {
          switch (action.locator.type) {
            case 'role': step.role = { type: action.locator.role!, name: action.locator.name! }; step.name = `Click ${action.locator.role} "${action.locator.name}"`; break;
            case 'label': step.label = action.locator.label; step.name = `Click label "${action.locator.label}"`; break;
            case 'placeholder': step.placeholder = action.locator.placeholder; step.name = `Click placeholder "${action.locator.placeholder}"`; break;
            case 'text': step.text = action.locator.text; step.name = `Click "${action.locator.text}"`; break;
            case 'testId': step.testId = action.locator.testId; step.name = `Click testId "${action.locator.testId}"`; break;
            case 'selector': step.selector = action.locator.selector; step.name = action.locator.name ? `Click combobox "${action.locator.name}"` : `Click ${action.locator.selector}`; break;
            case 'relative': step.relative = action.locator.relative; step.name = `Click ${this.describeTarget(action.locator.relative)} trong "${action.locator.relative!.anchor.text}"`; break;
          }
        } else if (action.useText && action.text) {
          step.text = action.text;
          step.name = `Click "${action.text}"`;
        } else {
          step.selector = action.selector;
          step.name = action.text ? `Click "${action.text}"` : `Click ${action.selector}`;
        }
        return step;
      }

      case 'fill': {
        const step: FlowStep = { name: '', action: 'fill', value: action.value };
        if (action.selectorFallback) step.selectorFallback = action.selectorFallback;
        if (action.locator) {
          switch (action.locator.type) {
            case 'role': step.role = { type: action.locator.role!, name: action.locator.name! }; step.name = `Fill ${action.locator.role} "${action.locator.name}"`; break;
            case 'label': step.label = action.locator.label; step.name = `Fill label "${action.locator.label}"`; break;
            case 'placeholder': step.placeholder = action.locator.placeholder; step.name = `Fill placeholder "${action.locator.placeholder}"`; break;
            case 'testId': step.testId = action.locator.testId; step.name = `Fill testId "${action.locator.testId}"`; break;
            case 'selector': step.selector = action.locator.selector; step.name = action.locator.name ? `Fill combobox "${action.locator.name}"` : `Fill ${action.locator.selector}`; break;
            case 'relative': step.relative = action.locator.relative; step.name = `Fill ${this.describeTarget(action.locator.relative)} trong "${action.locator.relative!.anchor.text}"`; break;
            default: step.selector = action.locator.selector || action.selector; step.name = `Fill ${action.selector}`; break;
          }
        } else {
          step.selector = action.selector;
          step.name = `Fill ${action.selector} with "${action.value}"`;
        }
        return step;
      }

      case 'selectOption': {
        const triggerName = action.trigger?.role?.name
          || action.trigger?.relative?.anchor.text
          || action.trigger?.placeholder
          || action.trigger?.label
          || '';
        return {
          name: triggerName ? `Chọn "${action.value}" ở "${triggerName}"` : `Chọn "${action.value}"`,
          action: 'selectOption',
          trigger: action.trigger,
          value: action.value,
          ...(action.search ? { search: true } : {}),
          ...(action.searchText ? { searchText: action.searchText } : {}),
        };
      }

      case 'keyboard':
        return {
          name: `Press ${action.key}`,
          action: 'keyboard',
          key: action.key,
        };

      case 'hover': {
        const step: FlowStep = { name: '', action: 'hover' };
        if (action.selectorFallback) step.selectorFallback = action.selectorFallback;
        if (action.locator) {
          switch (action.locator.type) {
            case 'role': step.role = { type: action.locator.role!, name: action.locator.name! }; step.name = `Hover ${action.locator.role} "${action.locator.name}"`; break;
            case 'label': step.label = action.locator.label; step.name = `Hover label "${action.locator.label}"`; break;
            case 'placeholder': step.placeholder = action.locator.placeholder; step.name = `Hover placeholder "${action.locator.placeholder}"`; break;
            case 'text': step.text = action.locator.text; step.name = `Hover "${action.locator.text}"`; break;
            case 'testId': step.testId = action.locator.testId; step.name = `Hover testId "${action.locator.testId}"`; break;
            case 'selector': step.selector = action.locator.selector; step.name = `Hover ${action.locator.selector}`; break;
            case 'relative': step.relative = action.locator.relative; step.name = `Hover ${this.describeTarget(action.locator.relative)} trong "${action.locator.relative!.anchor.text}"`; break;
            default: step.selector = action.locator.selector || action.selector; step.name = `Hover element`; break;
          }
        } else {
          step.selector = action.selector;
          step.name = `Hover ${action.selector}`;
        }
        return step;
      }

      case 'scroll': {
        const step: FlowStep = {
          name: action.selector ? `Scroll element ${action.selector}` : `Scroll page to (${action.scrollX || 0}, ${action.scrollY || 0})`,
          action: 'scroll',
          scrollTo: { x: (action as any).scrollX || 0, y: (action as any).scrollY || 0 },
        };
        if (action.selector) step.selector = action.selector;
        return step;
      }

      case 'assert': {
        const assertExpected = action.assertType === 'containsAll' && action.expected?.includes('|')
          ? action.expected.split('|').map((s: string) => s.trim())
          : action.assertType === 'count'
            ? Number(action.expected)
            : action.expected;
        const assertStep: FlowStep = {
          name: `Verify "${Array.isArray(assertExpected) ? assertExpected.join(', ') : assertExpected}"`,
          action: 'assert',
          assertType: (action.assertType || 'text') as any,
          expected: assertExpected,
        };
        if (action.selectorFallback) assertStep.selectorFallback = action.selectorFallback;
        // Map smart locator fields (same pattern as click/fill)
        if (action.locator) {
          switch (action.locator.type) {
            case 'role': assertStep.role = { type: action.locator.role!, name: action.locator.name! }; break;
            case 'label': assertStep.label = action.locator.label; break;
            case 'placeholder': assertStep.placeholder = action.locator.placeholder; break;
            case 'text': assertStep.text = action.locator.text; break;
            case 'testId': assertStep.testId = action.locator.testId; break;
            case 'selector': assertStep.selector = action.locator.selector; break;
          }
        } else if (action.selector) {
          // Backward-compat: old recordings without locator object
          assertStep.selector = action.selector;
        }
        return assertStep;
      }

      default:
        return null;
    }
  }

  /**
   * Save flow to YAML file
   */
  private saveFlow(flow: FlowFile, flowName: string, app?: string): string {
    if (!fs.existsSync(this.outputDir)) {
      fs.mkdirSync(this.outputDir, { recursive: true });
    }

    const filename = flowName
      .toLowerCase()
      .replace(/[^a-z0-9\u00C0-\u024F\u1E00-\u1EFF]+/gi, '-')
      .replace(/^-|-$/g, '') + '.yaml';

    const outputPath = path.join(this.outputDir, filename);

    // Custom YAML dump with comments
    const yamlContent = [
      `# Flow: ${flow.name}`,
      `# Author: ${flow.author}`,
      `# Created: ${flow.created}`,
      `# Run: frt-test play${app ? ` --app ${app}` : ''} ${filename}`,
      '',
      yaml.dump(flow, {
        lineWidth: 120,
        noRefs: true,
        quotingType: '"',
        forceQuotes: false,
      }),
    ].join('\n');

    fs.writeFileSync(outputPath, yamlContent, 'utf8');
    return outputPath;
  }
}
