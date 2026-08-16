document.getElementById('nav').innerHTML = renderHeader('standings');

async function load() {
  const season = document.getElementById('season-input').value.trim();
  try {
    const data = await API.standings(season);
    const tbody = document.querySelector('#standing-table tbody');
    if (!data.length) {
      tbody.innerHTML = '<tr><td colspan="10" class="empty">暂无数据</td></tr>';
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
  } catch (e) {
    toast(e.message, 'error');
  }
}

function escapeHtml(s) { return String(s ?? '').replace(/[&<>"']/g, c => ({ '&':'&amp;', '<':'&lt;', '>':'&gt;', '"':'&quot;', "'":'&#39;' }[c])); }

load();