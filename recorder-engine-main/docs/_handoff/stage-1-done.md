# Stage 1 Done — Auto-dismiss 4-tier refactor

## Files created/modified

- **Created**: `src/popup-dismiss.ts` — constants and utilities for popup dismiss logic
- **Modified**: `src/types.ts` — extended `autoPopupDismiss` type (D6)
- **Modified**: `src/runner.ts` — added import, replaced addInitScript block (D3), added D2 protection logic

## Config resolve-mode function location

`src/popup-dismiss.ts` — `resolvePopupMode()` at line 20

Mapping:
- `true` → `'aggressive'`
- `false` → `'off'`
- `undefined` → `'junk-only'`
- string value passed through as-is

## JUNK_SELECTORS and AGGRESSIVE_SELECTORS location

`src/popup-dismiss.ts`

- `JUNK_SELECTORS` (line 8): `.ant-notification-notice,.ant-message-notice,.onesignal-slidedown-container,[class*="onesignal"],[class*="cookie-"],[class*="ad-overlay"],[id*="ad-"]`
  - ⚠️ Does NOT contain `.ant-modal-*`
- `AGGRESSIVE_SELECTORS` (line 14): `.ant-modal-mask,.ant-modal-wrap,.modal-backdrop,.modal.fade.show,.modal.show,[class*="popup-overlay"],[class*="overlay-mask"],.ant-notification-notice,.ant-message-notice`

## addInitScript signature

```typescript
await this.page.addInitScript(
  ({ mode, junkSel, aggrSel }: { mode: string; junkSel: string; aggrSel: string }) => { ... },
  { mode: popupMode, junkSel: JUNK_SELECTORS, aggrSel: AGGRESSIVE_SELECTORS }
);
```

Single-arg object (Playwright `addInitScript<Arg>` accepts only 1 extra arg).
`mode`, `junkSel`, `aggrSel` are destructured from the arg inside the browser closure.

## D2 condition (aggressive only)

In `src/runner.ts`, inside the step execution loop, BEFORE `await this.executeStep(...)`:

```typescript
if (popupMode === 'aggressive' && this.page) {
  const sel = (step as any).selector || '';
  if (sel && (sel.includes('.ant-modal') || sel.includes('[class*="ant-modal"]'))) {
    await this.page.evaluate(() => {
      const modal = document.querySelector('.ant-modal-wrap:not([data-frt-protected])');
      if (modal) modal.setAttribute('data-frt-protected', 'true');
    }).catch(() => { /* ignore — best effort */ });
  }
}
```

Only fires when `popupMode === 'aggressive'`. Safe no-op for all other modes.

## TypeScript check result

```
npx tsc --noEmit → exit 0 (no errors)
```

## Test count/result

```
node --import tsx --test src/scaffold.test.ts

tests 10 | pass 10 | fail 0 | cancelled 0 | skipped 0
duration_ms ~172ms
```
