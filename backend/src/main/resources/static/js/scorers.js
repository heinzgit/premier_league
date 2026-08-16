document.getElementById('nav').innerHTML = renderHeader('scorers');

async function load() {
  const season = document.getElementById('season-input').value.trim();
  try {
    const data = await API.scorers(season);
    const tbody = document.querySelector('#scorer-table tbody');
    if (!data.length) {
      tbody.innerHTML = '<tr><td colspan="4" class="empty">暂无进球记录</td></tr>';
      return;
    }
    tbody.innerHTML = data.map((s, i) => `
      <tr>
        <td>${i + 1}</td>
        <td><b>${escapeHtml(s.scorerName)}</b></td>
        <td>${escapeHtml(s.teamName || '')}</td>
        <td><b>${s.goals}</b></td>
      </tr>
    `).join('');
  } catch (e) {
    toast(e.message, 'error');
  }
}

function escapeHtml(s) { return String(s ?? '').replace(/[&<>"']/g, c => ({ '&':'&amp;', '<':'&lt;', '>':'&gt;', '"':'&quot;', "'":'&#39;' }[c])); }

load();