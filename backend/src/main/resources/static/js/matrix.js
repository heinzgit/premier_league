document.getElementById('nav').innerHTML = renderHeader('matrix');

async function load() {
  const season = document.getElementById('season-input').value.trim();
  try {
    const data = await API.matrix(season);
    render(data);
  } catch (e) {
    toast(e.message, 'error');
  }
}

function render(data) {
  const teams = data.teams || [];
  const cells = data.cells || [];
  const table = document.getElementById('matrix');

  if (!teams.length) {
    table.innerHTML = '<tr><td class="empty">暂无球队</td></tr>';
    return;
  }

  // 表头
  let html = '<thead><tr><th class="row-header corner"></th>';
  teams.forEach(t => {
    html += `<th>${escapeHtml(t)}</th>`;
  });
  html += '</tr></thead><tbody>';

  // 数据行
  for (let i = 0; i < teams.length; i++) {
    html += '<tr>';
    html += `<td class="team-name">${escapeHtml(teams[i])}</td>`;
    for (let j = 0; j < teams.length; j++) {
      if (i === j) {
        html += `<td class="self">—</td>`;
        continue;
      }
      const c = cells[i] && cells[i][j];
      html += renderCell(c);
    }
    html += '</tr>';
  }
  html += '</tbody>';
  table.innerHTML = html;
}

function renderCell(c) {
  if (!c || (!c.homePlayed && !c.awayPlayed)) {
    return `<td class="match-cell cell-empty">—</td>`;
  }

  // 从行球队视角累计战绩
  let w = 0, d = 0, l = 0;
  const lines = [];

  if (c.homePlayed) {
    // 行主 vs 列客
    lines.push(`<span class="line home">主 ${c.homeScore}-${c.awayScore}</span>`);
    if (c.homeScore > c.awayScore) w++;
    else if (c.homeScore < c.awayScore) l++;
    else d++;
  }
  if (c.awayPlayed) {
    // 列主 vs 行客
    lines.push(`<span class="line away">客 ${c.awayScore2}-${c.homeScore2}</span>`);
    // 行球队分数是 awayScore2
    if (c.awayScore2 > c.homeScore2) w++;
    else if (c.awayScore2 < c.homeScore2) l++;
    else d++;
  }

  let cls = '';
  if (w > 0 && l === 0 && d === 0) cls = 'cell-win';
  else if (l > 0 && w === 0 && d === 0) cls = 'cell-loss';
  else if (d > 0 && w === 0 && l === 0) cls = 'cell-draw';
  // 平胜/平负混合:按主队视角
  else if (w > l) cls = 'cell-win';
  else if (l > w) cls = 'cell-loss';
  else cls = 'cell-draw';

  return `<td class="match-cell ${cls}">${lines.join('')}</td>`;
}

function escapeHtml(s) { return String(s ?? '').replace(/[&<>"']/g, c => ({ '&':'&amp;', '<':'&lt;', '>':'&gt;', '"':'&quot;', "'":'&#39;' }[c])); }

load();