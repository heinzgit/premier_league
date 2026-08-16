// API 封装
const API = (() => {
  const BASE = '/api';

  async function request(path, options = {}) {
    const opts = {
      headers: { 'Content-Type': 'application/json' },
      ...options,
    };
    if (opts.body && typeof opts.body !== 'string') {
      opts.body = JSON.stringify(opts.body);
    }
    const res = await fetch(BASE + path, opts);
    if (!res.ok) {
      let msg = `请求失败: ${res.status}`;
      try {
        const body = await res.json();
        if (body && body.error) msg = body.error;
      } catch (_) {}
      throw new Error(msg);
    }
    if (res.status === 204) return null;
    return res.json();
  }

  return {
    teams: {
      list: () => request('/teams'),
      get: (id) => request(`/teams/${id}`),
      create: (data) => request('/teams', { method: 'POST', body: data }),
      update: (id, data) => request(`/teams/${id}`, { method: 'PUT', body: data }),
      remove: (id) => request(`/teams/${id}`, { method: 'DELETE' }),
      history: (id) => request(`/teams/${id}/history`),
    },
    matches: {
      list: (season) => request('/matches' + (season ? `?season=${encodeURIComponent(season)}` : '')),
      get: (id) => request(`/matches/${id}`),
      create: (data) => request('/matches', { method: 'POST', body: data }),
      update: (id, data) => request(`/matches/${id}`, { method: 'PUT', body: data }),
      remove: (id) => request(`/matches/${id}`, { method: 'DELETE' }),
    },
    standings: (season) => request('/standings' + (season ? `?season=${encodeURIComponent(season)}` : '')),
    scorers: (season) => request('/scorers' + (season ? `?season=${encodeURIComponent(season)}` : '')),
    matrix: (season) => request('/matrix' + (season ? `?season=${encodeURIComponent(season)}` : '')),
  };
})();

// 简易 toast
function toast(msg, type = '') {
  const el = document.createElement('div');
  el.className = 'toast' + (type ? ' ' + type : '');
  el.textContent = msg;
  document.body.appendChild(el);
  setTimeout(() => el.remove(), 2500);
}

// 渲染 header (导航)
function renderHeader(active) {
  const links = [
    { href: '/index.html', label: '首页', key: 'home' },
    { href: '/teams.html', label: '球队管理', key: 'teams' },
    { href: '/matches.html', label: '比赛录入', key: 'matches' },
    { href: '/standings.html', label: '积分榜', key: 'standings' },
    { href: '/scorers.html', label: '射手榜', key: 'scorers' },
    { href: '/matrix.html', label: '对阵矩阵', key: 'matrix' },
    { href: '/team-history.html', label: '球队历史', key: 'history' },
  ];
  const nav = links.map(l =>
    `<a href="${l.href}" class="${active === l.key ? 'active' : ''}">${l.label}</a>`
  ).join('');
  return `<header><h1>⚽ 英超数据录入</h1><nav>${nav}</nav></header>`;
}