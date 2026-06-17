/**
 * FRT Test Recorder — Core Types
 * Định nghĩa schema cho YAML flow files
 */

export interface EnvironmentMeta {
  label?: string;
  description?: string;
}

export interface FlowConfig {
  baseUrl?: string;
  timeout?: number;
  retries?: number;
  headless?: boolean;
  slowMo?: number;
  viewport?: { width: number; height: number };
  /** Auto-dismiss mode.
   * - 'junk-only' (default): only close known junk (toast/notifications/ads). Ant modals are never proactively closed.
   * - 'off': disable all auto-dismiss.
   * - 'aggressive': legacy behavior — closes all ant-modal-wrap (use for old flows that relied on this).
   * Backward-compat: true maps to 'aggressive', false maps to 'off'.
   */
  autoPopupDismiss?: boolean | 'junk-only' | 'off' | 'aggressive';
}

export interface FlowVariable {
  [key: string]: string | number | boolean;
}

export interface FlowStep {
  /** Tên step hiển thị (tiếng Việt) */
  name: string;

  /** Loại action */
  action: 'goto' | 'click' | 'fill' | 'keyboard' | 'select' | 'selectOption' | 'check' | 'uncheck'
    | 'hover' | 'wait' | 'screenshot' | 'assert' | 'upload' | 'scroll';

  /** Scroll position target */
  scrollTo?: { x?: number; y?: number };

  /** CSS/XPath/Role selector */
  selector?: string;

  /** Role-based selector (Playwright getByRole) */
  role?: { type: string; name: string };

  /** Text-based selector (Playwright getByText) */
  text?: string;

  /** Label-based selector (Playwright getByLabel) */
  label?: string;

  /** Placeholder-based selector (Playwright getByPlaceholder) */
  placeholder?: string;

  /** TestId-based selector (Playwright getByTestId) */
  testId?: string;

  /** Value to fill / URL to goto / file path to upload */
  value?: string;

  /** Keyboard key to press */
  key?: string;

  /** Press key after fill */
  pressAfter?: string;

  /** Wait condition after action */
  waitAfter?: 'networkIdle' | 'domContentLoaded' | 'load' | number;

  /** Wait for selector to appear before continuing */
  waitFor?: string;

  /** If true, step won't fail the test if it errors */
  optional?: boolean;

  /** Force click even if element is covered by overlay */
  force?: boolean;

  /** Override timeout for this step (ms) */
  timeout?: number;

  /** Fixed delay after action (ms) — use sparingly */
  delay?: number;

  /** Use a shared flow (import reference) */
  use?: string;

  /** Variables to pass to shared flow */
  with?: Record<string, string>;

  /** Assertion type (for action: 'assert') */
  assertType?: 'visible' | 'hidden' | 'text' | 'value' | 'url' | 'title' | 'count' | 'containsAll' | 'screenshot';

  /** Expected value for assertion */
  expected?: string | number | string[];

  /** Last-resort CSS selector captured alongside the primary smart locator. Runner uses it on timeout. */
  selectorFallback?: string;

  /**
   * Text-anchor (relative) locator — neo theo một text ổn định gần element rồi đi ra để tới đích.
   * Recorder chỉ emit khi locator phẳng "yếu" (ambiguous hoặc nth-child). Xem RelativeLocator.
   */
  relative?: RelativeLocator;

  /**
   * action='selectOption' (Ant Select / combobox semantic): ô combobox cần MỞ.
   * Player tự bung: click trigger → (search?) gõ value lọc → chọn option theo TÊN (`value`) từ page root (xuyên portal).
   * `trigger` mang tập field locator của 1 step (role/relative/selector/placeholder/text/testId/label) để runner
   * resolve y như locator thường (combobox neo role=combobox+name hoặc relative scope `.ant-select-selector`).
   */
  trigger?: Pick<FlowStep, 'role' | 'relative' | 'selector' | 'placeholder' | 'text' | 'testId' | 'label' | 'selectorFallback'>;

  /** action='selectOption': trigger là combobox CÓ ô search → gõ để lọc trước khi chọn. */
  search?: boolean;
  /** action='selectOption': text user GÕ để lọc (tách khỏi `value`=text option chọn). Player gõ cái này; fallback `value`. */
  searchText?: string;
}

/** Locator "đơn" (6 bậc cũ) — dùng làm `target` lồng trong RelativeLocator. KHÔNG cho relative-trong-relative. */
export interface SimpleLocator {
  type: 'role' | 'label' | 'placeholder' | 'text' | 'testId' | 'selector';
  role?: string;
  name?: string;
  label?: string;
  placeholder?: string;
  text?: string;
  testId?: string;
  selector?: string;
}

/**
 * Text-anchor locator. Hai hình:
 *  - Form A: tìm container chung chứa anchor text, rồi đi xuống target trong container.
 *    `page.locator(scope).filter({ has: getByText(anchor) }).<target>`
 *  - Form B: đích CHÍNH LÀ tổ tiên của text (cả dòng/card click được).
 *    `page.getByText(anchor).locator(ancestor.xpath)`
 */
export interface RelativeLocator {
  form: 'A' | 'B';
  /** Text neo. `dynamic:true` = neo dữ liệu (đã tham số hóa thành {{var}}); false/undefined = neo static. */
  anchor: { text: string; dynamic?: boolean };
  /** Form A: đơn vị lặp (dòng/list-item) để filter. */
  scope?: { by: 'role' | 'css'; role?: string; css?: string };
  /** Form A: element cần tác động, tìm TRONG scope. */
  target?: SimpleLocator;
  /** Form B: leo từ anchor lên tổ tiên đích bằng xpath relative (vd 'xpath=ancestor::tr[1]'). */
  ancestor?: { xpath: string };
}

export interface FlowAssertion {
  type: 'visible' | 'hidden' | 'text' | 'url' | 'title' | 'count' | 'screenshot';
  selector?: string;
  value?: string | number;
  message?: string;
}

export interface FlowFile {
  /** Tên flow */
  name: string;

  /** Mô tả ngắn */
  description?: string;

  /** Tags để filter khi chạy */
  tags?: string[];

  /** Author (tester name) */
  author?: string;

  /** Ngày tạo */
  created?: string;

  /** Import shared flows */
  imports?: string[];

  /** Config override cho flow này */
  config?: FlowConfig;

  /** Variables — thay đổi data không cần sửa steps */
  variables?: FlowVariable;

  /** Danh sách steps */
  steps: FlowStep[];

  /** Assertions cuối flow */
  assertions?: FlowAssertion[];

  /** Metadata: recorded against which app/env/url */
  recorded_against?: { app: string; env: string; url: string; date: string };
}

export interface LocatorInfo {
  type: 'role' | 'label' | 'placeholder' | 'text' | 'testId' | 'selector' | 'relative';
  role?: string;
  name?: string;
  label?: string;
  placeholder?: string;
  text?: string;
  testId?: string;
  selector?: string;
  relative?: RelativeLocator;
}

export interface RecordedAction {
  timestamp: number;
  action: string;
  selector?: string;
  locator?: LocatorInfo;
  value?: string;
  key?: string;
  url?: string;
  role?: { type: string; name: string };
  text?: string;
  assertType?: string;
  expected?: string;
  useText?: boolean;
  selectorFallback?: string;
  scrollX?: number;
  scrollY?: number;
  /** Cặp biến cần đẩy vào flow.variables (do auto-variabilize anchor DATA sinh ra). */
  variables?: Record<string, string>;

  /** Combobox semantic merge (set lúc record): click MỞ một Ant Select / combobox. */
  comboboxOpen?: boolean;
  /** Combobox semantic merge: fill này gõ vào ô search của combobox đang mở. */
  comboboxFill?: boolean;
  /** Combobox semantic merge: click này CHỌN một option trong dropdown (text option hiển thị). */
  optionText?: string;
  /** action='selectOption' (sau khi gộp): locator ô combobox cần mở. */
  trigger?: FlowStep['trigger'];
  /** action='selectOption' (sau khi gộp): combobox có gõ-lọc. */
  search?: boolean;
  /** action='selectOption' (sau khi gộp): text user gõ để lọc (tách khỏi value=option chọn). */
  searchText?: string;
}

export interface RunResult {
  flowName: string;
  flowPath: string;
  status: 'passed' | 'failed' | 'skipped';
  duration: number;
  steps: StepResult[];
  error?: string;
  screenshotPath?: string;

  /** Epoch ms lúc flow bắt đầu — render giờ bắt đầu trong report. */
  startedAt?: number;
  /** App đang chạy (vd "rsa-web"). */
  app?: string;
  /** Base URL của app. */
  baseUrl?: string;
  /** Environment đang chạy (vd "ci", "uat", "prod"). */
  env?: string;
  /** Tên trình duyệt (vd "Chromium"). */
  browser?: string;
  /** Chạy headless hay không. */
  headless?: boolean;
}

export interface StepResult {
  name: string;
  status: 'passed' | 'failed' | 'skipped';
  duration: number;
  error?: string;

  /** Loại action của step (vd 'assert') — reporter dùng để nhận diện step verify. */
  action?: string;
  /** Epoch ms lúc step bắt đầu — render timestamp HH:MM:SS. */
  startedAt?: number;
  /** Ảnh chụp dạng data URI base64 (verify steps + step fail) để nhúng thẳng vào report. */
  screenshot?: string;
}
