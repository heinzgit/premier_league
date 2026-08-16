document.getElementById('nav').innerHTML = renderHeader('squads');

function defaultSeason() {
  const y = new Date().getFullYear();
  const m = new Date().getMonth() + 1;
  return m >= 8 ? `${y}-${y + 1}` : `${y - 1}-${y}`;
}

async function load() {
  const season = document.getElementById('season-input').value.trim() || defaultSeason();
  if (!document.getElementById('season-input').value.trim()) {
    document.getElementById('season-input').value = season;
  }
  const host = document.getElementById('squads');
  host.innerHTML = '<div class="card empty">加载中...</div>';
  try {
    const data = await API.seasonSquads(season);
    renderSquads(host, season, data);
  } catch (e) {
    host.innerHTML = `<div class="card empty" style="color:#dc2626">${escapeHtml(e.message)}</div>`;
  }
}

function renderSquads(host, season, data) {
  if (!data.length) {
    host.innerHTML = `<div class="card empty">${season} 赛季暂无阵容记录,请先在比赛录入中维护阵容</div>`;
    return;
  }

  // 按 teamId 分组,组内按球衣号升序,无球衣号的放末尾
  const grouped = new Map();
  data.forEach(p => {
    if (!grouped.has(p.teamId)) grouped.set(p.teamId, { teamName: p.teamName, players: [] });
    grouped.get(p.teamId).players.push(p);
  });

  const sortedGroups = Array.from(grouped.entries()).sort((a, b) => {
    const na = a[1].teamName || '';
    const nb = b[1].teamName || '';
    return na.localeCompare(nb, 'zh');
  });

  host.innerHTML = sortedGroups.map(([teamId, group]) => {
    const players = group.players.slice().sort((a, b) => {
      const na = parseInt(a.shirtNumber);
      const nb = parseInt(b.shirtNumber);
      const va = Number.isFinite(na) ? na : 9999;
      const vb = Number.isFinite(nb) ? nb : 9999;
      if (va !== vb) return va - vb;
      return a.playerName.localeCompare(b.playerName, 'zh');
    });
    return `
      <div class="card squad-team-card">
        <h3>${escapeHtml(group.teamName || '未知球队')} <span class="count">(${players.length} 人)</span></h3>
        <div class="squad-grid">
          ${players.map(p => `
            <div class="squad-player">
              <span class="num">${escapeHtml(p.shirtNumber || '-')}</span>
              <span class="name">${escapeHtml(p.playerName)}</span>
            </div>`).join('')}
        </div>
      </div>`;
  }).join('');
}

function escapeHtml(s) { return String(s ?? '').replace(/[&<>"']/g, c => ({ '&':'&amp;', '<':'&lt;', '>':'&gt;', '"':'&quot;', "'":'&#39;' }[c])); }

// 加载时填默认赛季
document.getElementById('season-input').value = defaultSeason();
load();