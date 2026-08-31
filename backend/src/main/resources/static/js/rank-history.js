document.getElementById('nav').innerHTML = renderHeader('rank-history');

let cachedTeams = [];
let chart = null;

document.getElementById('season-input').value = defaultSeason();

async function init() {
  cachedTeams = await API.teams.list();
  cachedTeams.sort((a, b) => a.name.localeCompare(b.name, 'zh'));
  const sel = document.getElementById('team-select');
  sel.innerHTML = '<option value="">-- 请选择 --</option>' +
    cachedTeams.map(t => `<option value="${t.id}">${escapeHtml(t.name)}</option>`).join('');
}

async function load() {
  const season = document.getElementById('season-input').value.trim();
  const teamId = document.getElementById('team-select').value;
  const emptyHint = document.getElementById('empty-hint');
  const chartWrap = document.getElementById('chart-wrap');
  const table = document.getElementById('history-table');
  const tbody = table.querySelector('tbody');

  emptyHint.style.display = '';
  chartWrap.style.display = 'none';
  table.style.display = 'none';
  tbody.innerHTML = '';

  if (!season) { emptyHint.textContent = '请填写赛季'; return; }
  if (!teamId) { emptyHint.textContent = '请选择球队'; return; }

  try {
    const data = await API.snapshots.progression(teamId, season);
    if (!data.points.length) {
      emptyHint.textContent = '该赛季还没有任何快照,请先在积分榜页面保存';
      return;
    }
    emptyHint.style.display = 'none';
    chartWrap.style.display = '';
    table.style.display = '';

    const labels = data.points.map(p => `第${p.roundNumber}轮`);
    const ranks = data.points.map(p => p.rank);  // null = 未参赛
    const labelsWithData = labels.map((l, i) => ranks[i] != null ? l : null);
    const ranksForChart = ranks.map(r => r == null ? null : r);

    drawChart(labels, ranksForChart, data.teamName);
    renderTable(tbody, data);
  } catch (e) {
    emptyHint.textContent = '加载失败: ' + e.message;
  }
}

function drawChart(labels, ranks, teamName) {
  const ctx = document.getElementById('rank-chart').getContext('2d');
  if (chart) chart.destroy();
  chart = new Chart(ctx, {
    type: 'line',
    data: {
      labels,
      datasets: [{
        label: teamName + ' 排名',
        data: ranks,
        borderColor: '#38003c',
        backgroundColor: 'rgba(56,0,60,0.1)',
        tension: 0.2,
        spanGaps: true,
        pointRadius: 5,
        pointBackgroundColor: '#38003c',
      }],
    },
    options: {
      responsive: true,
      scales: {
        y: {
          reverse: true,           // 排名 1 在最上面
          min: 1,
          ticks: { stepSize: 1 },
          title: { display: true, text: '排名' },
        },
        x: {
          title: { display: true, text: '轮次' },
        },
      },
      plugins: {
        legend: { display: false },
        tooltip: {
          callbacks: {
            label: ctx => ctx.parsed.y == null ? '该轮未参赛' : `第 ${ctx.parsed.y} 名`,
          },
        },
      },
    },
  });
}

function renderTable(tbody, data) {
  tbody.innerHTML = data.points.map(p => {
    const change = p.rankChange;
    let badge = '<span style="color:#9ca3af">—</span>';
    if (change != null) {
      if (change > 0) badge = `<span style="color:#166534">↑${change}</span>`;
      else if (change < 0) badge = `<span style="color:#991b1b">↓${-change}</span>`;
      else badge = '<span style="color:#6b7280">—</span>';
    }
    return `<tr>
      <td>第 ${p.roundNumber} 轮</td>
      <td>${p.snapshotDate}</td>
      <td><b>${p.rank == null ? '<span style="color:#9ca3af">未参赛</span>' : p.rank}</b></td>
      <td>${badge}</td>
    </tr>`;
  }).join('');
}

function escapeHtml(s) { return String(s ?? '').replace(/[&<>"']/g, c => ({ '&':'&amp;', '<':'&lt;', '>':'&gt;', '"':'&quot;', "'":'&#39;' }[c])); }

init();
