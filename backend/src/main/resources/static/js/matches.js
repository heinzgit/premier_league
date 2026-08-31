document.getElementById('nav').innerHTML = renderHeader('matches');

let cachedTeams = [];
let allMatches = [];
let currentPage = 1;
const PAGE_SIZE = 20;

async function loadTeams() {
  cachedTeams = await API.teams.list();
  cachedTeams.sort((a, b) => a.name.localeCompare(b.name, 'zh'));
}

async function loadMatches() {
  try {
    allMatches = await API.matches.list();
    // 按比赛日期降序排 (字符串 yyyy-MM-dd 可直接比较)
    allMatches.sort((a, b) => b.matchDate.localeCompare(a.matchDate));
    currentPage = 1;
    renderPage();
  } catch (e) {
    toast(e.message, 'error');
  }
}

function renderPage() {
  const tbody = document.querySelector('#match-table tbody');
  if (!allMatches.length) {
    tbody.innerHTML = '<tr><td colspan="8" class="empty">还没有比赛,点击新增</td></tr>';
    renderPager(0);
    return;
  }
  const totalPages = Math.max(1, Math.ceil(allMatches.length / PAGE_SIZE));
  if (currentPage > totalPages) currentPage = totalPages;
  const start = (currentPage - 1) * PAGE_SIZE;
  const pageRows = allMatches.slice(start, start + PAGE_SIZE);

  tbody.innerHTML = pageRows.map(m => {
    const goalsHtml = m.goals.length
      ? m.goals.map(g => `<div>${g.minute != null ? g.minute + "' " : ''}${escapeHtml(g.scorerName)}${g.goalType === 'PENALTY' ? ' (点)' : ''}${g.goalType === 'OWN_GOAL' ? ' (乌)' : ''}</div>`).join('')
      : '<span style="color:#9ca3af">无进球详情</span>';
    return `
      <tr>
        <td>${m.matchDate}</td>
        <td>${escapeHtml(m.season)}</td>
        <td>${escapeHtml(m.homeTeamName)}</td>
        <td><b>${m.homeScore} - ${m.awayScore}</b></td>
        <td>${escapeHtml(m.awayTeamName)}</td>
        <td style="font-size:12px">${goalsHtml}</td>
        <td><button class="btn small secondary" onclick="openLineupModal(${m.id})">阵容</button></td>
        <td class="actions">
          <button class="btn small secondary" onclick='openEditModal(${JSON.stringify(m).replace(/'/g, "\\'")})'>编辑</button>
          <button class="btn small danger" onclick="deleteMatch(${m.id})">删除</button>
        </td>
      </tr>`;
  }).join('');
  renderPager(totalPages);
}

function renderPager(totalPages) {
  let pager = document.getElementById('match-pager');
  if (!pager) {
    pager = document.createElement('div');
    pager.id = 'match-pager';
    pager.style.cssText = 'display:flex;justify-content:flex-end;gap:8px;align-items:center;margin-top:12px;font-size:13px;color:#6b7280';
    document.querySelector('#match-table').after(pager);
  }
  if (totalPages === 0) { pager.innerHTML = ''; return; }
  pager.innerHTML = `
    <span>共 ${allMatches.length} 条 · 第 ${currentPage} / ${totalPages} 页</span>
    <button class="btn small secondary" ${currentPage <= 1 ? 'disabled' : ''} onclick="goPage(${currentPage - 1})">上一页</button>
    <button class="btn small secondary" ${currentPage >= totalPages ? 'disabled' : ''} onclick="goPage(${currentPage + 1})">下一页</button>
  `;
}

function goPage(p) {
  currentPage = p;
  renderPage();
}

function openCreateModal() { renderForm(null); }
function openEditModal(m) { renderForm(m); }

function renderForm(match) {
  const isEdit = !!match;
  const teamOpts = cachedTeams.map(t => `<option value="${t.id}" ${(match && (t.id === match.homeTeamId || t.id === match.awayTeamId)) ? 'selected' : ''}>${escapeHtml(t.name)}</option>`).join('');
  const goals = match?.goals || [];
  const today = new Date().toISOString().slice(0, 10);

  document.getElementById('modal-host').innerHTML = `
    <div class="modal-backdrop" onclick="if(event.target===this) closeModal()">
      <div class="modal">
        <h3>${isEdit ? '编辑比赛' : '新增比赛'}</h3>
        <div class="form-row">
          <div><label>比赛日期 *</label><input id="f-date" type="date" value="${match?.matchDate || today}" /></div>
          <div><label>赛季 *</label><input id="f-season" value="${escapeAttr(match?.season || defaultSeason())}" placeholder="如 2025-2026" /></div>
        </div>
        <div class="form-row">
          <div><label>主队 *</label><select id="f-home">${teamOpts}</select></div>
          <div><label>客队 *</label><select id="f-away">${teamOpts}</select></div>
        </div>
        <div class="form-row">
          <div><label>主队进球</label><input id="f-home-score" type="number" min="0" value="${match?.homeScore ?? 0}" /></div>
          <div><label>客队进球</label><input id="f-away-score" type="number" min="0" value="${match?.awayScore ?? 0}" /></div>
        </div>

        <div style="margin-top:8px">
          <label>进球详情 (可选)</label>
          <div id="goal-list"></div>
          <button class="btn secondary small" onclick="addGoalRow()">+ 添加进球</button>
        </div>

        <div style="display:flex;justify-content:flex-end;gap:8px;margin-top:16px">
          <button class="btn secondary" onclick="closeModal()">取消</button>
          <button class="btn" onclick="submitForm(${match?.id || 'null'})">保存</button>
        </div>
      </div>
    </div>`;

  if (isEdit) {
    document.getElementById('f-home').value = match.homeTeamId;
    document.getElementById('f-away').value = match.awayTeamId;
    goals.forEach(g => addGoalRow(g));
  } else {
    addGoalRow();
  }
}

function addGoalRow(goal) {
  const list = document.getElementById('goal-list');
  const teamOpts = cachedTeams.map(t =>
    `<option value="${t.id}" ${goal && t.id === goal.teamId ? 'selected' : ''}>${escapeHtml(t.name)}</option>`
  ).join('');
  const row = document.createElement('div');
  row.className = 'goal-row';
  row.innerHTML = `
    <div><label>进球者</label><input class="g-scorer" value="${escapeAttr(goal?.scorerName || '')}" /></div>
    <div><label>球队</label><select class="g-team">${teamOpts}</select></div>
    <div><label>分钟</label>
      <div style="display:flex;gap:4px">
        <input class="g-minute" type="number" min="0" max="130" value="${goal?.minute ?? ''}" style="width:60px" />
        <select class="g-type">
          <option value="REGULAR" ${(!goal || goal.goalType === 'REGULAR') ? 'selected' : ''}>普通</option>
          <option value="PENALTY" ${goal?.goalType === 'PENALTY' ? 'selected' : ''}>点球</option>
          <option value="OWN_GOAL" ${goal?.goalType === 'OWN_GOAL' ? 'selected' : ''}>乌龙</option>
        </select>
      </div>
    </div>
    <div><label>&nbsp;</label><button class="btn small danger" onclick="this.parentNode.parentNode.remove()">×</button></div>`;
  list.appendChild(row);
}

async function submitForm(id) {
  const goalRows = document.querySelectorAll('#goal-list .goal-row');
  const goals = Array.from(goalRows).map(row => {
    const scorerName = row.querySelector('.g-scorer').value.trim();
    if (!scorerName) return null;
    return {
      scorerName,
      teamId: parseInt(row.querySelector('.g-team').value),
      minute: parseInt(row.querySelector('.g-minute').value) || null,
      goalType: row.querySelector('.g-type').value,
    };
  }).filter(Boolean);

  const data = {
    homeTeamId: parseInt(document.getElementById('f-home').value),
    awayTeamId: parseInt(document.getElementById('f-away').value),
    homeScore: parseInt(document.getElementById('f-home-score').value) || 0,
    awayScore: parseInt(document.getElementById('f-away-score').value) || 0,
    matchDate: document.getElementById('f-date').value,
    season: document.getElementById('f-season').value.trim(),
    goals,
  };
  try {
    if (id) await API.matches.update(id, data);
    else await API.matches.create(data);
    toast('保存成功', 'success');
    closeModal();
    loadMatches();
  } catch (e) {
    toast(e.message, 'error');
  }
}

async function deleteMatch(id) {
  if (!confirm('确认删除该比赛及其所有进球?')) return;
  try {
    await API.matches.remove(id);
    toast('已删除', 'success');
    loadMatches();
  } catch (e) {
    toast('删除失败: ' + e.message, 'error');
  }
}

function closeModal() { document.getElementById('modal-host').innerHTML = ''; }

// ===== 阵容 (球场可视化) =====

const FORMATIONS = {
  '4-3-3': [
    { idx: 1,  label: 'GK', x: 50, y: 8  },
    { idx: 2,  label: 'LB', x: 12, y: 28 },
    { idx: 3,  label: 'CB', x: 37, y: 25 },
    { idx: 4,  label: 'CB', x: 63, y: 25 },
    { idx: 5,  label: 'RB', x: 88, y: 28 },
    { idx: 6,  label: 'CM', x: 25, y: 55 },
    { idx: 7,  label: 'CM', x: 50, y: 55 },
    { idx: 8,  label: 'CM', x: 75, y: 55 },
    { idx: 9,  label: 'LW', x: 15, y: 82 },
    { idx: 10, label: 'ST', x: 50, y: 85 },
    { idx: 11, label: 'RW', x: 85, y: 82 },
  ],
  '4-4-2': [
    { idx: 1,  label: 'GK', x: 50, y: 8  },
    { idx: 2,  label: 'LB', x: 12, y: 30 },
    { idx: 3,  label: 'CB', x: 37, y: 27 },
    { idx: 4,  label: 'CB', x: 63, y: 27 },
    { idx: 5,  label: 'RB', x: 88, y: 30 },
    { idx: 6,  label: 'LM', x: 12, y: 58 },
    { idx: 7,  label: 'CM', x: 37, y: 58 },
    { idx: 8,  label: 'CM', x: 63, y: 58 },
    { idx: 9,  label: 'RM', x: 88, y: 58 },
    { idx: 10, label: 'ST', x: 37, y: 85 },
    { idx: 11, label: 'ST', x: 63, y: 85 },
  ],
  '3-5-2': [
    { idx: 1,  label: 'GK',  x: 50, y: 8  },
    { idx: 2,  label: 'CB',  x: 25, y: 27 },
    { idx: 3,  label: 'CB',  x: 50, y: 27 },
    { idx: 4,  label: 'CB',  x: 75, y: 27 },
    { idx: 5,  label: 'LWB', x: 10, y: 55 },
    { idx: 6,  label: 'CM',  x: 30, y: 55 },
    { idx: 7,  label: 'CM',  x: 50, y: 55 },
    { idx: 8,  label: 'CM',  x: 70, y: 55 },
    { idx: 9,  label: 'RWB', x: 90, y: 55 },
    { idx: 10, label: 'ST',  x: 38, y: 85 },
    { idx: 11, label: 'ST',  x: 62, y: 85 },
  ],
  '5-3-2': [
    { idx: 1,  label: 'GK',  x: 50, y: 8  },
    { idx: 2,  label: 'LWB', x: 8,  y: 30 },
    { idx: 3,  label: 'CB',  x: 28, y: 27 },
    { idx: 4,  label: 'CB',  x: 50, y: 27 },
    { idx: 5,  label: 'CB',  x: 72, y: 27 },
    { idx: 6,  label: 'RWB', x: 92, y: 30 },
    { idx: 7,  label: 'CM',  x: 30, y: 58 },
    { idx: 8,  label: 'CM',  x: 50, y: 58 },
    { idx: 9,  label: 'CM',  x: 70, y: 58 },
    { idx: 10, label: 'ST',  x: 38, y: 85 },
    { idx: 11, label: 'ST',  x: 62, y: 85 },
  ],
  '3-4-3': [
    { idx: 1,  label: 'GK', x: 50, y: 8  },
    { idx: 2,  label: 'CB', x: 25, y: 27 },
    { idx: 3,  label: 'CB', x: 50, y: 27 },
    { idx: 4,  label: 'CB', x: 75, y: 27 },
    { idx: 5,  label: 'LM', x: 12, y: 55 },
    { idx: 6,  label: 'CM', x: 37, y: 55 },
    { idx: 7,  label: 'CM', x: 63, y: 55 },
    { idx: 8,  label: 'RM', x: 88, y: 55 },
    { idx: 9,  label: 'LW', x: 15, y: 82 },
    { idx: 10, label: 'ST', x: 50, y: 85 },
    { idx: 11, label: 'RW', x: 85, y: 82 },
  ],
  '4-2-3-1': [
    { idx: 1,  label: 'GK',  x: 50, y: 8  },
    { idx: 2,  label: 'LB',  x: 12, y: 28 },
    { idx: 3,  label: 'CB',  x: 37, y: 25 },
    { idx: 4,  label: 'CB',  x: 63, y: 25 },
    { idx: 5,  label: 'RB',  x: 88, y: 28 },
    { idx: 6,  label: 'CDM', x: 35, y: 48 },
    { idx: 7,  label: 'CDM', x: 65, y: 48 },
    { idx: 8,  label: 'LAM', x: 20, y: 65 },
    { idx: 9,  label: 'CAM', x: 50, y: 65 },
    { idx: 10, label: 'RAM', x: 80, y: 65 },
    { idx: 11, label: 'ST',  x: 50, y: 85 },
  ],
  '自定义': Array.from({ length: 11 }, (_, i) => ({
    idx: i + 1, label: `P${i + 1}`, x: 6, y: 6 + i * 8,
  })),
};

const FORMATION_NAMES = Object.keys(FORMATIONS);

function resolveFormation(name) {
  return FORMATIONS[name] ? name : '4-3-3';
}

async function openLineupModal(matchId) {
  try {
    const data = await API.lineups.get(matchId);
    renderLineupModal(matchId, data);
  } catch (e) {
    toast(e.message, 'error');
  }
}

function renderLineupModal(matchId, data) {
  const home = data.home || { teamId: null, teamName: '主队', formation: '', players: [] };
  const away = data.away || { teamId: null, teamName: '客队', formation: '', players: [] };
  document.getElementById('modal-host').innerHTML = `
    <div class="modal-backdrop" onclick="if(event.target===this) closeModal()">
      <div class="modal" style="max-width:880px">
        <h3>出场阵容</h3>
        <div class="lineup-sides">
          ${renderLineupSide('home', home)}
          ${renderLineupSide('away', away)}
        </div>
        <div style="display:flex;justify-content:flex-end;gap:8px;margin-top:16px">
          <button class="btn secondary" onclick="closeModal()">取消</button>
          <button class="btn" onclick="submitLineup(${matchId})">保存</button>
        </div>
      </div>
    </div>`;
  // 绑定阵型变更
  document.querySelectorAll('.ln-formation').forEach(sel => {
    sel.addEventListener('change', e => {
      const side = e.target.closest('.lineup-side').dataset.side;
      rerenderPitch(side);
    });
  });
  setupDrag('home');
  setupDrag('away');
}

function renderLineupSide(side, lineup) {
  const teamId = lineup.teamId || 'null';
  const formationKey = resolveFormation(lineup.formation);
  // 把已有球员按 displayOrder 索引
  const byOrder = {};
  const posByOrder = {};
  const numberByOrder = {};
  (lineup.players || []).forEach(p => {
    if (p.displayOrder) {
      byOrder[p.displayOrder] = p.playerName || '';
      if (p.posX != null && p.posY != null) {
        posByOrder[p.displayOrder] = { x: p.posX, y: p.posY };
      }
      if (p.shirtNumber) numberByOrder[p.displayOrder] = p.shirtNumber;
    }
  });
  const formationOpts = FORMATION_NAMES.map(n =>
    `<option value="${n}" ${n === formationKey ? 'selected' : ''}>${n}</option>`
  ).join('');
  const slots = FORMATIONS[formationKey];
  const slotsHtml = slots.map(s => {
    const pos = posByOrder[s.idx] || { x: s.x, y: s.y };
    const num = numberByOrder[s.idx] || '';
    return `
    <div class="pitch-slot" data-idx="${s.idx}" data-label="${s.label}" style="left:${pos.x}%;top:${pos.y}%">
      <input class="badge" type="text" maxlength="3" placeholder="#" value="${escapeAttr(num)}" title="球衣号码" />
      <input class="slot-name" type="text" placeholder="${s.label}" value="${escapeAttr(byOrder[s.idx] || '')}" />
    </div>`;
  }).join('');
  return `
    <div class="lineup-side" data-side="${side}">
      <div style="font-weight:600;margin-bottom:6px">${escapeHtml(lineup.teamName || (side === 'home' ? '主队' : '客队'))}</div>
      <input type="hidden" class="ln-team-id" value="${teamId}" />
      <div style="display:flex;align-items:center;gap:8px;margin-bottom:8px">
        <label style="margin:0">阵型</label>
        <select class="ln-formation" style="width:auto">${formationOpts}</select>
        <span style="font-size:11px;color:#6b7280">点击号码修改 · 按住空白处拖动</span>
      </div>
      <div class="pitch">
        <div class="pitch-goal"></div>
        <div class="pitch-center-circle"></div>
        <div class="ln-slots">${slotsHtml}</div>
      </div>
    </div>`;
}

function rerenderPitch(side) {
  const root = document.querySelector(`.lineup-side[data-side="${side}"]`);
  if (!root) return;
  const formation = root.querySelector('.ln-formation').value;
  const slots = FORMATIONS[formation];
  const host = root.querySelector('.ln-slots');
  const existing = {};
  host.querySelectorAll('.pitch-slot').forEach(el => {
    const idx = parseInt(el.dataset.idx);
    const name = el.querySelector('.slot-name')?.value || '';
    const num = el.querySelector('.badge')?.value || '';
    const left = parseFloat(el.style.left);
    const top = parseFloat(el.style.top);
    existing[idx] = {
      name,
      number: num,
      pos: Number.isFinite(left) && Number.isFinite(top) ? { x: left, y: top } : null,
    };
  });
  host.innerHTML = slots.map(s => {
    const ex = existing[s.idx];
    const name = ex?.name != null ? ex.name : '';
    const number = ex?.number != null ? ex.number : '';
    const pos = ex?.pos || { x: s.x, y: s.y };
    return `
      <div class="pitch-slot" data-idx="${s.idx}" data-label="${s.label}" style="left:${pos.x}%;top:${pos.y}%">
        <input class="badge" type="text" maxlength="3" placeholder="#" value="${escapeAttr(number)}" title="球衣号码" />
        <input class="slot-name" type="text" placeholder="${s.label}" value="${escapeAttr(name)}" />
      </div>`;
  }).join('');
  setupDrag(side);
}

function setupDrag(side) {
  const root = document.querySelector(`.lineup-side[data-side="${side}"]`);
  if (!root) return;
  const pitch = root.querySelector('.pitch');
  root.querySelectorAll('.pitch-slot').forEach(slot => {
    if (slot.dataset.dragReady === '1') return;
    slot.dataset.dragReady = '1';
    slot.addEventListener('mousedown', e => {
      // 点在 input 上不拖动 (号码/姓名输入框需要焦点)
      if (e.target.tagName === 'INPUT') return;
      e.preventDefault();
      const slotRect = slot.getBoundingClientRect();
      const pitchRect = pitch.getBoundingClientRect();
      const offsetX = e.clientX - (slotRect.left + slotRect.width / 2);
      const offsetY = e.clientY - (slotRect.top + slotRect.height / 2);
      const onMove = ev => {
        const left = ((ev.clientX - offsetX - pitchRect.left) / pitchRect.width) * 100;
        const top = ((ev.clientY - offsetY - pitchRect.top) / pitchRect.height) * 100;
        slot.style.left = `${Math.max(0, Math.min(100, left))}%`;
        slot.style.top = `${Math.max(0, Math.min(100, top))}%`;
      };
      const onUp = () => {
        document.removeEventListener('mousemove', onMove);
        document.removeEventListener('mouseup', onUp);
      };
      document.addEventListener('mousemove', onMove);
      document.addEventListener('mouseup', onUp);
    });
  });
}

async function submitLineup(matchId) {
  const build = side => {
    const root = document.querySelector(`.lineup-side[data-side="${side}"]`);
    if (!root) return null;
    const teamId = root.querySelector('.ln-team-id').value;
    const formation = root.querySelector('.ln-formation').value;
    const slots = root.querySelectorAll('.pitch-slot');
    const players = Array.from(slots).map(el => {
      const left = parseFloat(el.style.left);
      const top = parseFloat(el.style.top);
      return {
        playerName: el.querySelector('.slot-name').value.trim(),
        position: el.dataset.label,
        displayOrder: parseInt(el.dataset.idx),
        posX: Number.isFinite(left) ? Math.round(left) : null,
        posY: Number.isFinite(top) ? Math.round(top) : null,
        shirtNumber: el.querySelector('.badge').value.trim() || null,
      };
    }).filter(p => p.playerName);
    return {
      teamId: teamId === 'null' ? null : parseInt(teamId),
      formation,
      players,
    };
  };
  const payload = { home: build('home'), away: build('away') };
  try {
    await API.lineups.save(matchId, payload);
    toast('阵容已保存', 'success');
    closeModal();
  } catch (e) {
    toast(e.message, 'error');
  }
}
function escapeHtml(s) { return String(s ?? '').replace(/[&<>"']/g, c => ({ '&':'&amp;', '<':'&lt;', '>':'&gt;', '"':'&quot;', "'":'&#39;' }[c])); }
function escapeAttr(s) { return escapeHtml(s); }

(async () => {
  await loadTeams();
  await loadMatches();
})();