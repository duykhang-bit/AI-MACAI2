/**
 * FRT Test Recorder — Reporter
 * Format và hiển thị kết quả chạy test
 */

import { RunResult } from './types';

export class FlowReporter {
  /**
   * Escape HTML special characters to prevent XSS
   */
  private escapeHtml(str: string): string {
    return str.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/"/g, '&quot;');
  }

  /**
   * Print results to console with colors
   */
  printResults(results: RunResult[]): void {
    const total = results.length;
    const passed = results.filter(r => r.status === 'passed').length;
    const failed = results.filter(r => r.status === 'failed').length;
    const totalDuration = results.reduce((sum, r) => sum + r.duration, 0);

    console.log('\n' + '═'.repeat(60));
    console.log('  FRT Test Recorder — Results');
    if (results.length > 0 && results[0].env) {
      const r0 = results[0];
      console.log(`  App: ${r0.app || '-'} | Env: ${r0.env} | URL: ${r0.baseUrl || '-'}`);
    }
    console.log('═'.repeat(60));

    for (const result of results) {
      const icon = result.status === 'passed' ? '✅' : '❌';
      const duration = (result.duration / 1000).toFixed(1);
      console.log(`\n  ${icon} ${result.flowName} (${duration}s)`);

      if (result.status === 'failed') {
        console.log(`     ❌ Error: ${result.error}`);
        if (result.screenshotPath) {
          console.log(`     📸 Screenshot: ${result.screenshotPath}`);
        }
      }

      // Print step details
      for (const step of result.steps) {
        const stepIcon = step.status === 'passed' ? '  ✓' :
          step.status === 'skipped' ? '  ⊘' : '  ✗';
        const stepDuration = step.duration > 1000 ? ` (${(step.duration / 1000).toFixed(1)}s)` : '';
        console.log(`     ${stepIcon} ${step.name}${stepDuration}`);
        if (step.status === 'failed' && step.error) {
          console.log(`       → ${step.error}`);
        }
      }
    }

    console.log('\n' + '─'.repeat(60));
    console.log(`  Total: ${total} | Passed: ${passed} | Failed: ${failed} | Duration: ${(totalDuration / 1000).toFixed(1)}s`);
    console.log('─'.repeat(60) + '\n');
  }

  /** Format epoch ms → "HH:MM:SS" (vi). Trả '' nếu không có. */
  private fmtTime(epoch?: number): string {
    if (!epoch) return '';
    const d = new Date(epoch);
    const p = (n: number) => String(n).padStart(2, '0');
    return `${p(d.getHours())}:${p(d.getMinutes())}:${p(d.getSeconds())}`;
  }

  /** Format ms → "x.xs". */
  private fmtDur(ms: number): string {
    return `${(ms / 1000).toFixed(1)}s`;
  }

  /** Format epoch ms → "dd/mm/yyyy HH:MM". */
  private fmtDateTime(epoch?: number): string {
    if (!epoch) return '';
    const d = new Date(epoch);
    const p = (n: number) => String(n).padStart(2, '0');
    return `${p(d.getDate())}/${p(d.getMonth() + 1)}/${d.getFullYear()} ${p(d.getHours())}:${p(d.getMinutes())}`;
  }

  /** Render timeline các step cho 1 test (dùng trong tab Tests). */
  private renderSteps(r: RunResult): string {
    return r.steps.map(s => {
      const cls = s.status === 'passed' ? 'pass' : s.status === 'skipped' ? 'skip' : 'fail';
      const icon = s.status === 'passed' ? '✔' : s.status === 'skipped' ? '⊘' : '✘';
      const verify = s.action === 'assert' ? ' verify' : '';
      const time = s.startedAt ? `${this.fmtTime(s.startedAt)} · ${this.fmtDur(s.duration)}` : this.fmtDur(s.duration);
      const err = s.error ? `<div class="err">✘ ${this.escapeHtml(s.error)}</div>` : '';
      const shot = s.screenshot
        ? `<div class="shot"><div class="cap">📸 ${s.status === 'failed' ? 'Ảnh lúc lỗi' : 'Ảnh tại bước verify'}</div><img class="ph" src="${s.screenshot}" loading="lazy" onclick="zoom(this)"></div>`
        : '';
      const dataFilter = s.status === 'failed' ? 'err' : (s.action === 'assert' ? 'verify' : 'other');
      return `<div class="step ${cls}${verify}" data-filter="${dataFilter}"><div class="ic">${icon}</div><div class="body">
        <div class="nm">${this.escapeHtml(s.name)}</div><div class="tm">${time}</div>${err}${shot}</div></div>`;
    }).join('\n');
  }

  /**
   * Generate HTML report — layout Extent-style (topbar + sidebar + dashboard + tab chi tiết test).
   * Báo cáo là 1 file HTML tự chứa (ảnh nhúng base64).
   */
  generateHtmlReport(results: RunResult[], outputPath: string): void {
    const fs = require('fs');
    const path = require('path');

    const total = results.length;
    const passed = results.filter(r => r.status === 'passed').length;
    const failed = results.filter(r => r.status === 'failed').length;
    const skipped = results.filter(r => r.status === 'skipped').length;
    const totalDuration = results.reduce((sum, r) => sum + r.duration, 0);
    const passPct = total ? (passed / total * 100) : 0;

    // Meta lấy từ test đầu tiên (cùng 1 lần chạy).
    const meta = results[0] || ({} as RunResult);
    const app = meta.app || '—';
    const env = meta.env || '';
    const baseUrl = meta.baseUrl || '—';
    const browser = `${meta.browser || 'Chromium'}${meta.headless ? ' (headless)' : ''}`;
    const startedAt = meta.startedAt;

    const html = `<!DOCTYPE html>
<html lang="vi">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>FRT Test Report — ${this.fmtDateTime(startedAt) || new Date().toLocaleDateString('vi-VN')}</title>
<style>
  * { box-sizing: border-box; margin: 0; padding: 0; }
  :root{
    --bg:#eef1f6; --panel:#ffffff; --ink:#2b2f3a; --muted:#8a92a6;
    --pass:#27ae60; --fail:#e74c3c; --skip:#f39c12; --brand:#34495e; --accent:#2d8cf0;
    --line:#e9edf3;
  }
  body { font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,sans-serif; background:var(--bg); color:var(--ink); }
  .topbar { background:var(--brand); color:#fff; height:56px; display:flex; align-items:center; padding:0 20px; gap:14px; position:sticky; top:0; z-index:10; }
  .topbar .logo { font-weight:700; font-size:16px; letter-spacing:.3px; }
  .topbar .tag { background:rgba(255,255,255,.15); padding:3px 10px; border-radius:20px; font-size:12px; }
  .topbar .spacer { flex:1; }
  .topbar .meta { font-size:12px; opacity:.85; }
  .wrap { display:flex; min-height:calc(100vh - 56px); }
  .nav { width:64px; background:#3d4a5c; display:flex; flex-direction:column; align-items:center; padding-top:14px; gap:6px; }
  .nav button { width:46px; height:46px; border:0; background:transparent; color:#aeb8c8; border-radius:10px; cursor:pointer; font-size:18px; }
  .nav button.active { background:rgba(255,255,255,.12); color:#fff; }
  .main { flex:1; padding:22px 26px; overflow:auto; }
  .view { display:none; } .view.show { display:block; }
  h2.section { font-size:15px; color:var(--muted); text-transform:uppercase; letter-spacing:.5px; margin:4px 0 14px; }
  .cards { display:grid; grid-template-columns:repeat(3,1fr); gap:16px; margin-bottom:20px; }
  .card { background:var(--panel); border-radius:12px; padding:18px; box-shadow:0 1px 3px rgba(0,0,0,.06); }
  .card .n { font-size:30px; font-weight:700; }
  .card .l { color:var(--muted); font-size:13px; margin-top:4px; }
  .card.pass .n{color:var(--pass)} .card.fail .n{color:var(--fail)} .card.skip .n{color:var(--skip)}
  .row2 { display:grid; grid-template-columns:320px 1fr; gap:16px; margin-bottom:20px; }
  .panel { background:var(--panel); border-radius:12px; padding:20px; box-shadow:0 1px 3px rgba(0,0,0,.06); }
  .panel h3 { font-size:14px; margin-bottom:16px; }
  .donut-wrap { display:flex; align-items:center; gap:20px; }
  .donut { width:150px; height:150px; border-radius:50%;
    background:conic-gradient(var(--pass) 0 ${passPct.toFixed(1)}%, var(--fail) ${passPct.toFixed(1)}% 100%);
    display:flex; align-items:center; justify-content:center; position:relative; }
  .donut::after { content:''; width:104px; height:104px; background:var(--panel); border-radius:50%; }
  .donut .c { position:absolute; text-align:center; }
  .donut .c b { font-size:26px; } .donut .c span{ font-size:12px; color:var(--muted); }
  .legend { font-size:13px; }
  .legend div { display:flex; align-items:center; gap:8px; margin:8px 0; }
  .dot { width:11px; height:11px; border-radius:3px; }
  .env table { width:100%; border-collapse:collapse; font-size:13px; }
  .env td { padding:8px 6px; border-bottom:1px solid var(--line); word-break:break-all; }
  .env td:first-child { color:var(--muted); width:42%; }
  .tests { display:grid; grid-template-columns:300px 1fr; gap:16px; }
  .list { background:var(--panel); border-radius:12px; overflow:hidden; box-shadow:0 1px 3px rgba(0,0,0,.06); height:fit-content; }
  .list .li { padding:13px 16px; border-bottom:1px solid var(--line); cursor:pointer; display:flex; align-items:center; gap:10px; }
  .list .li:hover{ background:#f6f8fb; } .list .li.active { background:#eaf3ff; border-left:3px solid var(--accent); }
  .badge { width:9px; height:9px; border-radius:50%; flex:none; }
  .b-pass{background:var(--pass)} .b-fail{background:var(--fail)} .b-skip{background:var(--skip)}
  .li .nm { font-size:13px; font-weight:600; } .li .du { font-size:11px; color:var(--muted); }
  .detail { background:var(--panel); border-radius:12px; padding:22px; box-shadow:0 1px 3px rgba(0,0,0,.06); }
  .detail.hide { display:none; }
  .detail .dh { display:flex; align-items:center; gap:12px; margin-bottom:6px; }
  .pill { font-size:11px; font-weight:700; padding:3px 10px; border-radius:20px; color:#fff; }
  .pill.pass{background:var(--pass)} .pill.fail{background:var(--fail)} .pill.skip{background:var(--skip)}
  .detail h2 { font-size:18px; } .detail .sub{ color:var(--muted); font-size:12px; margin-bottom:18px; }
  .step { display:flex; gap:12px; padding:10px 0; border-bottom:1px solid var(--line); }
  .step .ic { width:22px; text-align:center; font-weight:700; }
  .step.pass .ic{color:var(--pass)} .step.skip .ic{color:var(--skip)} .step.fail .ic{color:var(--fail)}
  .step .body { flex:1; }
  .step .nm { font-size:14px; }
  .step .tm { font-size:11px; color:var(--muted); }
  .step .err { color:var(--fail); font-size:12px; margin-top:4px; background:#fdecea; padding:6px 10px; border-radius:6px; white-space:pre-wrap; }
  .step.verify .nm::before { content:'VERIFY'; font-size:9px; font-weight:700; color:#fff; background:var(--accent); padding:1px 6px; border-radius:4px; margin-right:8px; vertical-align:middle; }
  .shot { margin-top:8px; }
  .shot .ph { width:300px; max-width:100%; border:1px solid var(--line); border-radius:8px; box-shadow:0 1px 4px rgba(0,0,0,.08); cursor:zoom-in; display:block; }
  .shot .cap { font-size:11px; color:var(--muted); margin-bottom:4px; }
  .filterbar { margin-bottom:14px; display:flex; gap:8px; }
  .filterbar button { border:1px solid var(--line); background:#fff; padding:6px 14px; border-radius:20px; font-size:12px; cursor:pointer; }
  .filterbar button.on { background:var(--brand); color:#fff; border-color:var(--brand); }
  .lightbox { display:none; position:fixed; inset:0; background:rgba(0,0,0,.85); z-index:99; align-items:center; justify-content:center; padding:30px; cursor:zoom-out; }
  .lightbox.show { display:flex; } .lightbox img { max-width:100%; max-height:100%; border-radius:8px; }
</style>
</head>
<body>
  <div class="topbar">
    <span class="logo">🧪 FRT Test Report</span>
    <span class="tag">${this.escapeHtml(app)}${env ? ' @ ' + this.escapeHtml(env) : ''}</span>
    <span class="spacer"></span>
    <span class="meta">${this.fmtDateTime(startedAt)} &nbsp;·&nbsp; ⏱ ${this.fmtDur(totalDuration)} &nbsp;·&nbsp; ${this.escapeHtml(browser)}</span>
  </div>

  <div class="wrap">
    <div class="nav">
      <button class="active" onclick="showView('dash',this)" title="Dashboard">📊</button>
      <button onclick="showView('tests',this)" title="Tests">📋</button>
    </div>

    <div class="main">
      <!-- ===== DASHBOARD ===== -->
      <div class="view show" id="dash">
        <h2 class="section">Tổng quan</h2>
        <div class="cards">
          <div class="card"><div class="n">${total}</div><div class="l">Tổng test</div></div>
          <div class="card pass"><div class="n">${passed}</div><div class="l">Passed</div></div>
          <div class="card fail"><div class="n">${failed}</div><div class="l">Failed</div></div>
        </div>

        <div class="row2">
          <div class="panel">
            <h3>Tỉ lệ Pass/Fail</h3>
            <div class="donut-wrap">
              <div class="donut"><div class="c"><b>${passPct.toFixed(0)}%</b><br><span>pass</span></div></div>
              <div class="legend">
                <div><span class="dot" style="background:var(--pass)"></span> Passed — ${passed}</div>
                <div><span class="dot" style="background:var(--fail)"></span> Failed — ${failed}</div>
                <div><span class="dot" style="background:var(--skip)"></span> Skipped — ${skipped}</div>
              </div>
            </div>
          </div>
          <div class="panel env">
            <h3>Môi trường chạy</h3>
            <table>
              <tr><td>App</td><td>${this.escapeHtml(app)}</td></tr>
              <tr><td>Base URL</td><td>${this.escapeHtml(baseUrl)}</td></tr>
              <tr><td>Trình duyệt</td><td>${this.escapeHtml(browser)}</td></tr>
              <tr><td>Bắt đầu</td><td>${this.fmtDateTime(startedAt) || '—'}</td></tr>
              <tr><td>Thời lượng</td><td>${this.fmtDur(totalDuration)}</td></tr>
            </table>
          </div>
        </div>

        <div class="panel">
          <h3>Danh sách test</h3>
          ${results.map(r => {
            const cls = r.status === 'passed' ? 'pass' : r.status === 'skipped' ? 'skip' : 'fail';
            const icon = r.status === 'passed' ? '✔' : r.status === 'skipped' ? '⊘' : '✘';
            return `<div class="step ${cls}"><div class="ic">${icon}</div><div class="body">
            <div class="nm">${this.escapeHtml(r.flowName)}</div>
            <div class="tm">${r.steps.length} bước · ${this.fmtDur(r.duration)} · ${r.status === 'passed' ? 'Passed' : r.status === 'skipped' ? 'Skipped' : 'Failed'}</div>
          </div></div>`;
          }).join('\n')}
        </div>
      </div>

      <!-- ===== TESTS ===== -->
      <div class="view" id="tests">
        <h2 class="section">Chi tiết test</h2>
        <div class="tests">
          <div class="list">
            ${results.map((r, i) => {
              const b = r.status === 'passed' ? 'b-pass' : r.status === 'skipped' ? 'b-skip' : 'b-fail';
              return `<div class="li${i === 0 ? ' active' : ''}" onclick="selectTest(${i},this)"><span class="badge ${b}"></span><div><div class="nm">${this.escapeHtml(r.flowName)}</div><div class="du">${this.fmtDur(r.duration)}</div></div></div>`;
            }).join('\n')}
          </div>

          <div class="detail-wrap">
            ${results.map((r, i) => {
              const pillCls = r.status === 'passed' ? 'pass' : r.status === 'skipped' ? 'skip' : 'fail';
              const pillTxt = r.status === 'passed' ? 'PASS' : r.status === 'skipped' ? 'SKIP' : 'FAIL';
              return `<div class="detail${i === 0 ? '' : ' hide'}" data-test="${i}">
            <div class="dh"><span class="pill ${pillCls}">${pillTxt}</span><h2>${this.escapeHtml(r.flowName)}</h2></div>
            <div class="sub">${this.escapeHtml(app)} · ${this.fmtDateTime(startedAt)} · ${this.fmtDur(r.duration)} · ${r.steps.length} bước</div>
            <div class="filterbar">
              <button class="on" onclick="filterSteps(this,'all')">Tất cả</button>
              <button onclick="filterSteps(this,'verify')">Chỉ verify</button>
              <button onclick="filterSteps(this,'err')">Chỉ lỗi</button>
            </div>
            ${this.renderSteps(r)}
          </div>`;
            }).join('\n')}
          </div>
        </div>
      </div>
    </div>
  </div>

  <div class="lightbox" id="lightbox" onclick="this.classList.remove('show')"><img id="lightbox-img"></div>

<script>
  function showView(id, btn){
    document.querySelectorAll('.view').forEach(v=>v.classList.remove('show'));
    document.getElementById(id).classList.add('show');
    document.querySelectorAll('.nav button').forEach(b=>b.classList.remove('active'));
    btn.classList.add('active');
  }
  function selectTest(i, el){
    document.querySelectorAll('.list .li').forEach(li=>li.classList.remove('active'));
    el.classList.add('active');
    document.querySelectorAll('.detail').forEach(d=>d.classList.add('hide'));
    document.querySelector('.detail[data-test="'+i+'"]').classList.remove('hide');
  }
  function filterSteps(btn, mode){
    const detail = btn.closest('.detail');
    detail.querySelectorAll('.filterbar button').forEach(b=>b.classList.remove('on'));
    btn.classList.add('on');
    detail.querySelectorAll('.step').forEach(s=>{
      const f = s.getAttribute('data-filter');
      s.style.display = (mode==='all' || f===mode) ? '' : 'none';
    });
  }
  function zoom(img){
    document.getElementById('lightbox-img').src = img.src;
    document.getElementById('lightbox').classList.add('show');
  }
</script>
</body>
</html>`;

    const dir = path.dirname(outputPath);
    if (!fs.existsSync(dir)) fs.mkdirSync(dir, { recursive: true });
    fs.writeFileSync(outputPath, html, 'utf8');
    console.log(`📊 HTML Report: ${outputPath}`);
  }
}
