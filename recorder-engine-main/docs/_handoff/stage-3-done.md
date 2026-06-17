# Stage 3 Done — D7 + D8 + Senior Review

## Test file created

`src/popup-dismiss.test.ts` — 23 tests across 6 groups:
- Group 1: resolvePopupMode (6 tests)
- Group 2: JUNK_SELECTORS (5 tests)
- Group 3: AGGRESSIVE_SELECTORS (2 tests)
- Group 4: isLikelyFunctionalModal via FUNCTIONAL_MODAL_SELECTOR (5 tests)
- Group 5: IS_FUNCTIONAL_MODAL_INLINE consistency (2 tests)
- Group 6: FUNCTIONAL_MODAL_SELECTOR single source of truth (3 tests)

**Result:** 23/23 pass, 0 fail

## D8 Logic-trace (E2E unverified — sandbox only)

| Check | Result |
|-------|--------|
| JUNK_SELECTORS does NOT contain .ant-modal | ✅ Only in AGGRESSIVE_SELECTORS |
| addInitScript receives mode arg (not hardcoded) | ✅ `{ mode: popupMode, junkSel: JUNK_SELECTORS, aggrSel: AGGRESSIVE_SELECTORS }` |
| tryReactiveDismiss throws on functional modal | ✅ "Modal lạ có form" error at line 690 |
| selectorList correct for non-aggressive | ✅ `selectorList = mode === 'aggressive' ? aggrSel : junkSel` |
| popupMode in scope at D2 block | ✅ Declared line 836, used line 905, same function scope |
| document.body null guard | ✅ DOMContentLoaded fallback at lines 886–890 |
| observer disconnects on beforeunload | ✅ line 894 |

## Senior review findings

### [P1] FIXED — Hardcoded functionalSel in tryReactiveDismiss
`runner.ts` line 680 duplicated `'input,select,textarea,[role="combobox"],table,.ant-form'` inline instead of referencing the canonical constant from `popup-dismiss.ts`. A future change to one would silently diverge from the other.

**Fix:** Exported `FUNCTIONAL_MODAL_SELECTOR` from `popup-dismiss.ts`. `isLikelyFunctionalModal` now uses the constant. `runner.ts` imports and passes it. `IS_FUNCTIONAL_MODAL_INLINE` still embeds the literal string (required for browser serialization) but Group 6 tests verify they match.

### [P2] ACKNOWLEDGED — `(step as any).selector` at line 906
`FlowStep` type doesn't declare a `selector` field, so it's cast to `any`. Non-critical (best-effort D2 protection, surrounded by `.catch(() => {})`). Requires a FlowStep type audit outside this scope.

## Security scan verdict

CLEAN — no secrets, credentials, tokens, PII, or internal infra in changed files. Only public Ant Design class names and Vietnamese UI error strings.

## tsc result

```
npx tsc --noEmit → exit 0
```

## All tests

```
popup-dismiss.test.ts: 23/23 pass
scaffold.test.ts:      10/10 pass
```

## SDD location

`docs/design/fix-auto-dismiss-4tier.html`

## Files changed in Stage 3

- `src/popup-dismiss.ts` — added `FUNCTIONAL_MODAL_SELECTOR` export; `isLikelyFunctionalModal` uses it
- `src/runner.ts` — import updated to include `FUNCTIONAL_MODAL_SELECTOR`; hardcoded functionalSel replaced
- `src/popup-dismiss.test.ts` — created (23 tests)
- `docs/design/fix-auto-dismiss-4tier.html` — created (SDD)
- `docs/_handoff/stage-3-done.md` — this file

## Overall verdict

**APPROVED** — 0 P0/P1 findings remaining. All deliverables D1–D8 complete. Tests pass. tsc clean.
