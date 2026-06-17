# Stage 2 Done — D4 reactive dismiss helper

## tryReactiveDismiss location

`src/runner.ts` — private method `tryReactiveDismiss` at line 616 (inside `FlowRunner` class).

## How click + fill handlers call it

Both handlers call `tryReactiveDismiss` BEFORE the force-retry fallback:

**Click handler** (~line 299):
```
const dismissed = await this.tryReactiveDismiss(page, step, JUNK_SELECTORS);
if (dismissed) {
  try { await loc.first().click({ timeout }); break; } catch (e2: any) { e = e2; }
}
// Fall through to force-retry (needed for combobox opacity:0)
console.log(`  [retry] Click intercepted, retrying with force: "${step.name}"`);
try { await loc.first().click({ timeout, force: true }); break; } catch (e2: any) { e = e2; }
```

**Fill handler** (~line 342):
```
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
...force: true blocks follow...
```

## Error message format

- **Protected modal**: `Protected modal đang chặn step '${step.name || step.action}' — không tự đóng. Kiểm tra flow đã đóng modal trước step này chưa.`
- **Functional modal**: `Modal lạ có form đang chặn step '${step.name || step.action}' — không tự đóng (ngầu flow thay đổi). Hãy kiểm tra UI đã có modal mới.`

## Force-retry combobox path

✅ CONFIRMED — force-retry blocks still exist in both click and fill handlers immediately after the reactive-dismiss attempt. The `// Fall through to force-retry (needed for combobox opacity:0)` comment marks each path.

## JUNK_SELECTORS usage

✅ `JUNK_SELECTORS` from `./popup-dismiss` is passed as argument to `tryReactiveDismiss` — not hardcoded inside the method.

## D5 status

`isLikelyFunctionalModal` — already canonical in `src/popup-dismiss.ts`. No changes needed. The reactive helper uses its inline equivalent (`functionalSel` string) for browser-side evaluation.

## TypeScript check

```
npx tsc --noEmit → exit 0 (no errors)
```

## Test count

```
tests 10 | pass 10 | fail 0 | cancelled 0 | skipped 0
duration_ms ~171ms
```
