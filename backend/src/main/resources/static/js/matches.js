document.getElementById('nav').innerHTML = renderHeader('matches');

let cachedTeams = [];

async function loadTeams() {
  cachedTeams = await API.teams.list();
  cachedTeams.sort((a, b) => a.name.localeCompare(b.name, 'zh'));
}

async function loadMatches() {
  try {
    const matches = await API.matches.list();
    const tbody = document.querySelector('#match-table tbody');
    if (!matches.length) {
      tbody.innerHTML = '<tr><td colspan="7" class="empty">还没有比赛,点击新增</td></tr>';
      return;
    }
    tbody.innerHTML = matches.map(m => {
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
          <td class="actions">
            <button class="btn small secondary" onclick='openEditModal(${JSON.stringify(m).replace(/'/g, "\\'")})'>编辑</button>
            <button class="btn small danger" onclick="deleteMatch(${m.id})">删除</button>
          </td>
        </tr>`;
    }).join('');
  } catch (e) {
    toast(e.message, 'error');
  }
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

function defaultSeason() {
  const y = new Date().getFullYear();
  const m = new Date().getMonth() + 1;
  return m >= 8 ? `${y}-${y+1}` : `${y-1}-${y}`;
}

function closeModal() { document.getElementById('modal-host').innerHTML = ''; }
function escapeHtml(s) { return String(s ?? '').replace(/[&<>"']/g, c => ({ '&':'&amp;', '<':'&lt;', '>':'&gt;', '"':'&quot;', "'":'&#39;' }[c])); }
function escapeAttr(s) { return escapeHtml(s); }

(async () => {
  await loadTeams();
  await loadMatches();
})();