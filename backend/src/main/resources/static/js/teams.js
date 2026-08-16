document.getElementById('nav').innerHTML = renderHeader('teams');

async function loadTeams() {
  try {
    const teams = await API.teams.list();
    const tbody = document.querySelector('#team-table tbody');
    if (!teams.length) {
      tbody.innerHTML = '<tr><td colspan="7" class="empty">还没有球队,点击右上角新增</td></tr>';
      return;
    }
    tbody.innerHTML = teams.map(t => `
      <tr>
        <td>${t.id}</td>
        <td>${escapeHtml(t.name)}</td>
        <td>${escapeHtml(t.shortName || '')}</td>
        <td>${t.foundedYear || ''}</td>
        <td>${escapeHtml(t.stadium || '')}</td>
        <td>${escapeHtml(t.city || '')}</td>
        <td class="actions">
          <button class="btn small secondary" onclick='openEditModal(${JSON.stringify(t).replace(/'/g, "\\'")})'>编辑</button>
          <button class="btn small danger" onclick="deleteTeam(${t.id})">删除</button>
        </td>
      </tr>
    `).join('');
  } catch (e) {
    toast(e.message, 'error');
  }
}

function openCreateModal() {
  renderForm(null);
}

function openEditModal(team) {
  renderForm(team);
}

function renderForm(team) {
  const host = document.getElementById('modal-host');
  const isEdit = !!team;
  host.innerHTML = `
    <div class="modal-backdrop" onclick="if(event.target===this) closeModal()">
      <div class="modal">
        <h3>${isEdit ? '编辑球队' : '新增球队'}</h3>
        <div class="form-row">
          <div><label>队名 *</label><input id="f-name" value="${escapeAttr(team?.name || '')}" /></div>
          <div><label>简称</label><input id="f-short" value="${escapeAttr(team?.shortName || '')}" /></div>
        </div>
        <div class="form-row">
          <div><label>成立年份</label><input id="f-year" type="number" value="${team?.foundedYear || ''}" /></div>
          <div><label>城市</label><input id="f-city" value="${escapeAttr(team?.city || '')}" /></div>
        </div>
        <div class="form-row single">
          <div><label>球场</label><input id="f-stadium" value="${escapeAttr(team?.stadium || '')}" /></div>
        </div>
        <div style="display:flex;justify-content:flex-end;gap:8px;margin-top:12px">
          <button class="btn secondary" onclick="closeModal()">取消</button>
          <button class="btn" onclick="submitForm(${team?.id || 'null'})">保存</button>
        </div>
      </div>
    </div>`;
}

function closeModal() {
  document.getElementById('modal-host').innerHTML = '';
}

async function submitForm(id) {
  const data = {
    name: document.getElementById('f-name').value.trim(),
    shortName: document.getElementById('f-short').value.trim(),
    foundedYear: parseInt(document.getElementById('f-year').value) || null,
    city: document.getElementById('f-city').value.trim(),
    stadium: document.getElementById('f-stadium').value.trim(),
  };
  try {
    if (id) await API.teams.update(id, data);
    else await API.teams.create(data);
    toast('保存成功', 'success');
    closeModal();
    loadTeams();
  } catch (e) {
    toast(e.message, 'error');
  }
}

async function deleteTeam(id) {
  if (!confirm('确认删除该球队?')) return;
  try {
    await API.teams.remove(id);
    toast('已删除', 'success');
    loadTeams();
  } catch (e) {
    toast('删除失败: ' + e.message, 'error');
  }
}

function escapeHtml(s) {
  return String(s ?? '').replace(/[&<>"']/g, c => ({ '&':'&amp;', '<':'&lt;', '>':'&gt;', '"':'&quot;', "'":'&#39;' }[c]));
}
function escapeAttr(s) { return escapeHtml(s); }

loadTeams();