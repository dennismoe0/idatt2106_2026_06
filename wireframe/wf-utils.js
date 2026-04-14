/**
 * wf-utils.js — Wireframe theme picker (DOM-safe, no innerHTML)
 * Floating button lets you change colors across all wireframe screens.
 * Changes persist to localStorage and apply on every page load.
 */
(function () {
  'use strict';

  // ── Color helpers ─────────────────────────────────────────────────────────
  function hexToRgb(hex) {
    const h = hex.replace('#', '');
    return { r: parseInt(h.slice(0, 2), 16), g: parseInt(h.slice(2, 4), 16), b: parseInt(h.slice(4, 6), 16) };
  }
  function toHex(v) { return Math.max(0, Math.min(255, Math.round(v))).toString(16).padStart(2, '0'); }
  function blend(hex, target, ratio) {
    const c = hexToRgb(hex), t = hexToRgb(target);
    return '#' + toHex(c.r + (t.r - c.r) * ratio) + toHex(c.g + (t.g - c.g) * ratio) + toHex(c.b + (t.b - c.b) * ratio);
  }

  // ── Apply theme ───────────────────────────────────────────────────────────
  function applyColors(p, a) {
    const r = document.documentElement;
    r.style.setProperty('--p', p);
    r.style.setProperty('--pl', blend(p, '#ffffff', 0.88));
    r.style.setProperty('--pd', blend(p, '#000000', 0.25));
    r.style.setProperty('--a', a);
    r.style.setProperty('--ad', blend(a, '#000000', 0.15));
  }

  // Restore saved theme on load
  try {
    const s = JSON.parse(localStorage.getItem('wf_theme'));
    if (s && s.primary && s.accent) applyColors(s.primary, s.accent);
  } catch (_) {}

  // ── DOM builder helper ────────────────────────────────────────────────────
  function mk(tag, attrs) {
    const e = document.createElement(tag);
    if (!attrs) return e;
    for (const [k, v] of Object.entries(attrs)) {
      if (k === 'cls') { e.className = v; }
      else if (k === 'txt') { e.textContent = v; }
      else if (k === 'css') { e.style.cssText = v; }
      else { e.setAttribute(k, v); }
    }
    return e;
  }
  function app(parent, ...children) { children.forEach(c => parent.appendChild(c)); return parent; }

  // ── Preset schemes ────────────────────────────────────────────────────────
  const SCHEMES = [
    { name: 'Blå (standard)', p: '#2B6CB0', a: '#F6AD55', ep: '🔵' },
    { name: 'Grønn',          p: '#276749', a: '#F6AD55', ep: '🟢' },
    { name: 'Lilla',          p: '#6B46C1', a: '#68D391', ep: '🟣' },
    { name: 'Rød',            p: '#9B2C2C', a: '#4299E1', ep: '🔴' },
    { name: 'Teal',           p: '#2C7A7B', a: '#FC8181', ep: '🩵' },
    { name: 'Mørk',           p: '#1A202C', a: '#F6AD55', ep: '🌑' },
  ];

  // ── Inject CSS ────────────────────────────────────────────────────────────
  const css = mk('style');
  css.textContent = [
    '#wf-fab{position:fixed;bottom:50px;right:16px;z-index:10000;width:44px;height:44px;border-radius:50%;',
    'background:#1A202C;color:#fff;border:2px solid #4A5568;font-size:20px;cursor:pointer;',
    'box-shadow:0 4px 16px rgba(0,0,0,.4);display:flex;align-items:center;justify-content:center;',
    'transition:transform .2s;}',
    '#wf-fab:hover{transform:scale(1.12);}',
    '#wf-panel{position:fixed;bottom:102px;right:16px;z-index:10001;background:#1A202C;',
    'border-radius:16px;padding:16px;box-shadow:0 8px 32px rgba(0,0,0,.5);width:230px;',
    'display:none;font-family:system-ui,sans-serif;}',
    '#wf-panel.open{display:block;}',
    '.wf-ph{color:#fff;font-size:11px;font-weight:800;text-transform:uppercase;letter-spacing:.6px;margin-bottom:10px;}',
    '.wf-row{display:flex;align-items:center;gap:8px;padding:6px 8px;border-radius:8px;cursor:pointer;transition:background .15s;margin-bottom:2px;}',
    '.wf-row:hover{background:rgba(255,255,255,.1);}',
    '.wf-sw{display:flex;gap:4px;}',
    '.wf-dot{width:14px;height:14px;border-radius:50%;border:2px solid rgba(255,255,255,.15);flex-shrink:0;}',
    '.wf-lbl{color:rgba(255,255,255,.85);font-size:12px;font-weight:600;}',
    '.wf-hr{border:none;border-top:1px solid rgba(255,255,255,.1);margin:10px 0;}',
    '.wf-custom{display:flex;align-items:center;justify-content:space-between;margin-bottom:8px;}',
    '.wf-custom label{color:rgba(255,255,255,.7);font-size:12px;font-weight:600;}',
    '.wf-custom input[type=color]{width:40px;height:28px;border:none;border-radius:6px;cursor:pointer;background:none;padding:0;}',
    '.wf-btn{width:100%;padding:7px;border:1px solid rgba(255,255,255,.2);border-radius:8px;',
    'background:rgba(255,255,255,.05);color:rgba(255,255,255,.7);font-size:12px;font-weight:700;',
    'cursor:pointer;font-family:inherit;transition:background .15s;}',
    '.wf-btn:hover{background:rgba(255,255,255,.12);}',
  ].join('');
  document.head.appendChild(css);

  // ── Build FAB ─────────────────────────────────────────────────────────────
  const fab = mk('button', { id: 'wf-fab', title: 'Endre fargetema', txt: '🎨' });
  document.body.appendChild(fab);

  // ── Build panel ───────────────────────────────────────────────────────────
  const panel = mk('div', { id: 'wf-panel' });

  const heading = mk('div', { cls: 'wf-ph', txt: '🎨 Fargetema' });
  panel.appendChild(heading);

  // Scheme rows
  SCHEMES.forEach(s => {
    const row = mk('div', { cls: 'wf-row' });
    const sw = mk('div', { cls: 'wf-sw' });
    const d1 = mk('div', { cls: 'wf-dot', css: 'background:' + s.p });
    const d2 = mk('div', { cls: 'wf-dot', css: 'background:' + s.a });
    const lbl = mk('span', { cls: 'wf-lbl', txt: s.ep + ' ' + s.name });
    app(sw, d1, d2);
    app(row, sw, lbl);
    row.dataset.p = s.p;
    row.dataset.a = s.a;
    row.addEventListener('click', () => saveApply(s.p, s.a));
    panel.appendChild(row);
  });

  // Divider
  panel.appendChild(mk('hr', { cls: 'wf-hr' }));

  // Custom primary picker
  const cpRow = mk('div', { cls: 'wf-custom' });
  const cpLabel = mk('label', { txt: 'Primærfarge' });
  const cpInput = mk('input', { type: 'color', id: 'wf-cp', value: '#2B6CB0' });
  app(cpRow, cpLabel, cpInput);
  panel.appendChild(cpRow);

  // Custom accent picker
  const caRow = mk('div', { cls: 'wf-custom' });
  const caLabel = mk('label', { txt: 'Aksentfarge' });
  const caInput = mk('input', { type: 'color', id: 'wf-ca', value: '#F6AD55' });
  app(caRow, caLabel, caInput);
  panel.appendChild(caRow);

  // Reset button
  const resetBtn = mk('button', { cls: 'wf-btn', txt: '↺ Tilbakestill standard' });
  resetBtn.addEventListener('click', () => saveApply('#2B6CB0', '#F6AD55'));
  panel.appendChild(resetBtn);

  document.body.appendChild(panel);

  // ── Interactions ──────────────────────────────────────────────────────────
  function saveApply(p, a) {
    applyColors(p, a);
    localStorage.setItem('wf_theme', JSON.stringify({ primary: p, accent: a }));
    cpInput.value = p;
    caInput.value = a;
  }

  fab.addEventListener('click', e => { e.stopPropagation(); panel.classList.toggle('open'); });
  cpInput.addEventListener('input', () => saveApply(cpInput.value, caInput.value));
  caInput.addEventListener('input', () => saveApply(cpInput.value, caInput.value));
  document.addEventListener('click', e => {
    if (!panel.contains(e.target) && e.target !== fab) panel.classList.remove('open');
  });

  // Sync pickers to current saved values
  try {
    const s = JSON.parse(localStorage.getItem('wf_theme'));
    if (s && s.primary) { cpInput.value = s.primary; caInput.value = s.accent; }
  } catch (_) {}
})();
