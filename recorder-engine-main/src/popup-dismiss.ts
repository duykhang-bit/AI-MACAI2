/**
 * FRT popup-dismiss constants and utilities.
 * NODE-SIDE only (canonical). For browser-side usage, constants are passed as
 * addInitScript args; functions are inlined in the script string.
 */

/** Selectors for known junk popups to proactively close. Does NOT include .ant-modal-* */
export const JUNK_SELECTORS =
  '.ant-notification-notice,.ant-message-notice,' +
  '.onesignal-slidedown-container,[class*="onesignal"],' +
  '[class*="cookie-"],[class*="ad-overlay"],[id*="ad-"]';

/** Legacy selectors for aggressive mode (original behavior). */
export const AGGRESSIVE_SELECTORS =
  '.ant-modal-mask,.ant-modal-wrap,.modal-backdrop,.modal.fade.show,.modal.show,' +
  '[class*="popup-overlay"],[class*="overlay-mask"],.ant-notification-notice,.ant-message-notice';

/** Resolve autoPopupDismiss config value to a canonical mode string. */
export function resolvePopupMode(
  raw: boolean | 'junk-only' | 'off' | 'aggressive' | undefined
): 'junk-only' | 'off' | 'aggressive' {
  if (raw === true) return 'aggressive';
  if (raw === false) return 'off';
  if (raw === undefined) return 'junk-only';
  return raw;
}

/** Canonical CSS selector for form-like elements inside a modal. Single source of truth. */
export const FUNCTIONAL_MODAL_SELECTOR = 'input,select,textarea,[role="combobox"],table,.ant-form';

/**
 * Returns true if the element looks like a functional modal (has form inputs).
 * CANONICAL Node-side version — also inlined in addInitScript for browser use.
 * Keep both in sync.
 */
export function isLikelyFunctionalModal(el: Element): boolean {
  return !!el.querySelector(FUNCTIONAL_MODAL_SELECTOR);
}

/**
 * INLINE version of isLikelyFunctionalModal as a string for browser injection.
 * Must match the logic of isLikelyFunctionalModal above.
 */
export const IS_FUNCTIONAL_MODAL_INLINE =
  'function isLikelyFunctionalModal(el){' +
  'return !!el.querySelector("input,select,textarea,[role=\\"combobox\\"],table,.ant-form");' +
  '}';
