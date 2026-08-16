document.getElementById('nav').innerHTML = renderHeader('history');

let cachedTeams = [];

async function init() {
  cachedTeams = await API.teams.list();
  cachedTeams.sort((a, b) => a.name.localeCompare(b.name, 'zh'));
  const sel = document.getElementById('team-select');
  sel.innerHTML = '<option value="">-- 请选择 --</option>' +
    cachedTeams.map(t => `<option value="${t.id}">${escapeHtml(t.name)}</option>`).join('');
  sel.onchange = load;
}

async function load() {
  const id = document.getElementById('team-select').value;
  const tbody = document.querySelector('#history-table tbody');
  const summary = document.getElementById('summary');
  summary.innerHTML = '';
  tbody.innerHTML = '';
  if (!id) return;
  try {
    const data = await API.teams.history(id);
    if (!data.length) {
      tbody.innerHTML = '<tr><td colspan="6" class="empty">该球队暂无比赛记录</td></tr>';
      return;
    }
    let win = 0, draw = 0, loss = 0;
    data.forEach(d => {
      if (d.result === 'WIN') win++;
      else if (d.result === 'DRAW') draw++;
      else loss++;
    });
    summary.innerHTML = `
      <div style="display:flex;gap:16px;margin-bottom:12px;font-size:14px">
        <span>总场次 <b>${data.length}</b></span>
        <span style="color:#166534">胜 <b>${win}</b></span>
        <span style="color:#92400e">平 <b>${draw}</b></span>
        <span style="color:#991b1b">负 <b>${loss}</b></span>
      </div>`;
    tbody.innerHTML = data.map(h => `
      <tr>
        <td>${h.matchDate}</td>
        <td>${escapeHtml(h.season)}</td>
        <td>${escapeHtml(h.opponentName || '')}</td>
        <td><span class="result-tag result-${h.homeAway}">${h.homeAway === 'HOME' ? '主场' : '客场'}</span></td>
        <td>${h.teamScore} - ${h.opponentScore}</td>
        <td><span class="result-tag result-${h.result}">${h.result === 'WIN' ? '胜' : h.result === 'DRAW' ? '平' : '负'}</span></td>
      </tr>
    `).join('');
  } catch (e) {
    toast(e.message, 'error');
  }
}

function escapeHtml(s) { return String(s ?? '').replace(/[&<>"']/g, c => ({ '&':'&amp;', '<':'&lt;', '>':'&gt;', '"':'&quot;', "'":'&#39;' }[c])); }

init();