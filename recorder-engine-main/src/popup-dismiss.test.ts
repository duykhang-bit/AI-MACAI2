/**
 * popup-dismiss.test.ts — Unit tests for popup-dismiss constants and utilities
 * Run: node --import tsx --test src/popup-dismiss.test.ts
 */

import { describe, it } from 'node:test';
import assert from 'node:assert/strict';
import {
  resolvePopupMode,
  JUNK_SELECTORS,
  AGGRESSIVE_SELECTORS,
  isLikelyFunctionalModal,
  IS_FUNCTIONAL_MODAL_INLINE,
  FUNCTIONAL_MODAL_SELECTOR,
} from './popup-dismiss';

// =============================================================================
// Group 1: resolvePopupMode
// =============================================================================
describe('resolvePopupMode', () => {
  it('true → aggressive', () => assert.equal(resolvePopupMode(true), 'aggressive'));
  it('false → off', () => assert.equal(resolvePopupMode(false), 'off'));
  it('undefined → junk-only', () => assert.equal(resolvePopupMode(undefined), 'junk-only'));
  it('"junk-only" → junk-only', () => assert.equal(resolvePopupMode('junk-only'), 'junk-only'));
  it('"off" → off', () => assert.equal(resolvePopupMode('off'), 'off'));
  it('"aggressive" → aggressive', () => assert.equal(resolvePopupMode('aggressive'), 'aggressive'));
});

// =============================================================================
// Group 2: JUNK_SELECTORS — must NOT contain modal selectors
// =============================================================================
describe('JUNK_SELECTORS', () => {
  it('does NOT contain .ant-modal-wrap', () => assert.ok(!JUNK_SELECTORS.includes('.ant-modal-wrap')));
  it('does NOT contain .ant-modal-mask', () => assert.ok(!JUNK_SELECTORS.includes('.ant-modal-mask')));
  it('does NOT contain .modal-backdrop', () => assert.ok(!JUNK_SELECTORS.includes('.modal-backdrop')));
  it('contains .ant-notification-notice', () => assert.ok(JUNK_SELECTORS.includes('.ant-notification-notice')));
  it('contains .ant-message-notice', () => assert.ok(JUNK_SELECTORS.includes('.ant-message-notice')));
});

// =============================================================================
// Group 3: AGGRESSIVE_SELECTORS — must include modal selectors for backward compat
// =============================================================================
describe('AGGRESSIVE_SELECTORS', () => {
  it('contains .ant-modal-wrap (backward compat)', () => assert.ok(AGGRESSIVE_SELECTORS.includes('.ant-modal-wrap')));
  it('contains .ant-modal-mask', () => assert.ok(AGGRESSIVE_SELECTORS.includes('.ant-modal-mask')));
});

// =============================================================================
// Group 4: isLikelyFunctionalModal — checks via FUNCTIONAL_MODAL_SELECTOR
// =============================================================================
describe('isLikelyFunctionalModal (source inspection)', () => {
  it('heuristic checks form inputs (via FUNCTIONAL_MODAL_SELECTOR)', () =>
    assert.ok(FUNCTIONAL_MODAL_SELECTOR.includes('input,select,textarea')));
  it('heuristic checks combobox role (via FUNCTIONAL_MODAL_SELECTOR)', () =>
    assert.ok(FUNCTIONAL_MODAL_SELECTOR.includes('combobox')));
  it('heuristic checks table (via FUNCTIONAL_MODAL_SELECTOR)', () =>
    assert.ok(FUNCTIONAL_MODAL_SELECTOR.includes('table')));
  it('heuristic checks .ant-form (via FUNCTIONAL_MODAL_SELECTOR)', () =>
    assert.ok(FUNCTIONAL_MODAL_SELECTOR.includes('.ant-form')));
  it('function body references FUNCTIONAL_MODAL_SELECTOR', () =>
    assert.ok(isLikelyFunctionalModal.toString().includes('FUNCTIONAL_MODAL_SELECTOR'),
      'function must use the constant, not a hardcoded string'));
});

// =============================================================================
// Group 5: IS_FUNCTIONAL_MODAL_INLINE consistency
// =============================================================================
describe('IS_FUNCTIONAL_MODAL_INLINE consistency', () => {
  it('contains same form selectors as isLikelyFunctionalModal', () => {
    assert.ok(IS_FUNCTIONAL_MODAL_INLINE.includes('input,select,textarea'), 'inline must have input,select,textarea');
    assert.ok(IS_FUNCTIONAL_MODAL_INLINE.includes('combobox'), 'inline must have combobox');
    assert.ok(IS_FUNCTIONAL_MODAL_INLINE.includes('table'), 'inline must have table');
    assert.ok(IS_FUNCTIONAL_MODAL_INLINE.includes('.ant-form'), 'inline must have .ant-form');
  });

  it('is a valid function string (contains function keyword)', () => {
    assert.ok(IS_FUNCTIONAL_MODAL_INLINE.includes('function isLikelyFunctionalModal'), 'must start with function declaration');
  });
});

// =============================================================================
// Group 6: FUNCTIONAL_MODAL_SELECTOR — single source of truth
// =============================================================================
describe('FUNCTIONAL_MODAL_SELECTOR', () => {
  it('contains input,select,textarea', () => assert.ok(FUNCTIONAL_MODAL_SELECTOR.includes('input,select,textarea')));
  it('contains combobox', () => assert.ok(FUNCTIONAL_MODAL_SELECTOR.includes('combobox')));
  it('IS_FUNCTIONAL_MODAL_INLINE embeds same selectors', () => {
    const normalized = IS_FUNCTIONAL_MODAL_INLINE.replace(/\\"/g, '"');
    assert.ok(normalized.includes(FUNCTIONAL_MODAL_SELECTOR), 'inline must embed FUNCTIONAL_MODAL_SELECTOR');
  });
});
