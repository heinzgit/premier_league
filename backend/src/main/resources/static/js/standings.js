document.getElementById('nav').innerHTML = renderHeader('standings');

let currentSeason = '';

const seasonInput = document.getElementById('season-input');
seasonInput.value = defaultSeason();
seasonInput.placeholder = `如 ${defaultSeason()},留空看全部`;

async function load() {
  currentSeason = document.getElementById('season-input').value.trim();
  try {
    const data = await API.standings(currentSeason);
    const tbody = document.querySelector('#standing-table tbody');
    if (!data.length) {
      tbody.innerHTML = '<tr><td colspan="10" class="empty">暂无数据</td></tr>';
      await prefillRound();
      return;
    }
    tbody.innerHTML = data.map((s, i) => `
      <tr>
        <td>${i + 1}</td>
        <td><b>${escapeHtml(s.teamName)}</b></td>
        <td>${s.played}</td>
        <td>${s.won}</td>
        <td>${s.drawn}</td>
        <td>${s.lost}</td>
        <td>${s.goalsFor}</td>
        <td>${s.goalsAgainst}</td>
        <td>${s.goalDifference > 0 ? '+' + s.goalDifference : s.goalDifference}</td>
        <td><b>${s.points}</b></td>
      </tr>
    `).join('');
    await prefillRound();
  } catch (e) {
    toast(e.message, 'error');
  }
}

async function prefillRound() {
  if (!currentSeason) {
    document.getElementById('snap-round').value = '';
    return;
  }
  try {
    const snaps = await API.snapshots.list(currentSeason);
    const next = snaps.length ? Math.max(...snaps.map(s => s.roundNumber)) + 1 : 1;
    document.getElementById('snap-round').value = next;
  } catch (e) {
    // 忽略, 留空让用户手填
  }
}

async function saveSnapshot() {
  if (!currentSeason) {
    toast('请先选择赛季', 'error');
    return;
  }
  const round = parseInt(document.getElementById('snap-round').value);
  if (!round || round <= 0) {
    toast('请填写有效的轮次', 'error');
    return;
  }
  const note = document.getElementById('snap-note').value.trim();
  const btn = document.getElementById('snap-save-btn');
  btn.disabled = true;
  try {
    await API.snapshots.create({ season: currentSeason, roundNumber: round, note });
    toast(`第 ${round} 轮快照已保存`, 'success');
    document.getElementById('snap-note').value = '';
    await prefillRound();
  } catch (e) {
    toast('保存失败: ' + e.message, 'error');
  } finally {
    btn.disabled = false;
  }
}

function escapeHtml(s) { return String(s ?? '').replace(/[&<>"']/g, c => ({ '&':'&amp;', '<':'&lt;', '>':'&gt;', '"':'&quot;', "'":'&#39;' }[c])); }

load();