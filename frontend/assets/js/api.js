'use strict';

const API_BASE = 'http://localhost:8080/api/v1';

// ========== Auth Helpers ==========
function getToken() { return localStorage.getItem('jwt'); }
function getUser() {
  try { return JSON.parse(localStorage.getItem('user') || '{}'); }
  catch { return {}; }
}
function setAuth(token, user) {
  localStorage.setItem('jwt', token);
  localStorage.setItem('user', JSON.stringify(user));
}
function logout() {
  localStorage.clear();
  window.location.href = 'index.html';
}
function requireAuth() {
  if (!getToken()) window.location.href = 'index.html';
}

// ========== API Fetch ==========
async function apiFetch(path, options = {}) {
  const token = getToken();
  const config = {
    ...options,
    headers: {
      'Content-Type': 'application/json',
      ...(token ? { 'Authorization': `Bearer ${token}` } : {}),
      ...options.headers,
    },
  };
  try {
    const res = await fetch(`${API_BASE}${path}`, config);
    if (res.status === 401 && !path.startsWith('/auth/')) {
      logout();
      return;
    }
    if (!res.ok) {
      const errBody = await res.json().catch(() => ({ message: res.statusText }));
      const msg = errBody.message || errBody.error || `API error ${res.status}`;
      showToast(msg, 'error');
      throw new Error(msg);
    }
    if (res.status === 204) return null;
    return await res.json();
  } catch (err) {
    if (!err.message.startsWith('API error')) console.error('Fetch error:', err);
    throw err;
  }
}

async function apiGet(path) { return apiFetch(path, { method: 'GET' }); }
async function apiPost(path, body) { return apiFetch(path, { method: 'POST', body: JSON.stringify(body) }); }
async function apiPut(path, body) { return apiFetch(path, { method: 'PUT', body: JSON.stringify(body) }); }
async function apiDelete(path) { return apiFetch(path, { method: 'DELETE' }); }

// ========== Toast Notifications ==========
function showToast(message, type = 'success', duration = 3500) {
  let container = document.getElementById('toast-container');
  if (!container) {
    container = document.createElement('div');
    container.id = 'toast-container';
    document.body.appendChild(container);
  }
  const toast = document.createElement('div');
  toast.className = `toast toast-${type}`;
  const icons = { success: '✓', error: '✕', info: 'ℹ', warning: '⚠' };
  toast.innerHTML = `<span style="margin-right:8px;font-weight:700">${icons[type] || '•'}</span>${message}`;
  container.appendChild(toast);
  setTimeout(() => {
    toast.style.animation = 'slideInToast 0.3s ease reverse';
    setTimeout(() => toast.remove(), 300);
  }, duration);
}

// ========== Format Helpers ==========
function formatCurrency(value) {
  if (value == null) return '—';
  return new Intl.NumberFormat('en-IN', { style: 'currency', currency: 'INR' }).format(value);
}
function formatDate(dateStr) {
  if (!dateStr) return '—';
  return new Date(dateStr).toLocaleDateString('en-GB', { day: '2-digit', month: 'short', year: 'numeric' });
}
function formatDateTime(dateStr) {
  if (!dateStr) return '—';
  return new Date(dateStr).toLocaleString('en-GB', { day: '2-digit', month: 'short', hour: '2-digit', minute: '2-digit' });
}
function formatPercent(val) {
  if (val == null) return '—';
  return val.toFixed(1) + '%';
}

// ========== Status Badge Helper ==========
function statusBadge(status) {
  if (!status) return '';
  const s = status.toLowerCase();
  return `<span class="badge badge-${s}"><span class="badge-dot"></span>${status.replace(/_/g,' ')}</span>`;
}

// ========== SoC Gauge SVG ==========
function createSoCGauge(soc, size = 90) {
  const radius = 38;
  const cx = size/2, cy = size/2;
  const circumference = 2 * Math.PI * radius;
  const pct = Math.max(0, Math.min(100, soc || 0));
  const dashoffset = circumference * (1 - pct/100);
  const color = pct > 50 ? 'var(--emerald)' : pct > 20 ? 'var(--amber)' : 'var(--crimson)';
  return `
  <div class="soc-gauge-container" style="width:${size}px;height:${size}px">
    <svg class="soc-gauge" width="${size}" height="${size}" viewBox="0 0 ${size} ${size}">
      <circle cx="${cx}" cy="${cy}" r="${radius}" fill="none" stroke="rgba(255,255,255,0.05)" stroke-width="7"/>
      <circle cx="${cx}" cy="${cy}" r="${radius}" fill="none" stroke="${color}" stroke-width="7"
        stroke-dasharray="${circumference}" stroke-dashoffset="${dashoffset}"
        stroke-linecap="round" style="transition:stroke-dashoffset 0.8s ease,stroke 0.5s ease;"/>
    </svg>
    <div class="soc-gauge-text">
      <div class="soc-gauge-percent" style="color:${color}">${pct.toFixed(0)}%</div>
      <div class="soc-gauge-label">SoC</div>
    </div>
  </div>`;
}

// ========== Sidebar Builder ==========
function buildSidebar(activePage) {
  const user = getUser();
  const navItems = [
    { href:'dashboard.html', icon:'⚡', label:'Dashboard', page:'dashboard' },
    { href:'dispatch.html', icon:'🚛', label:'Dispatch & Fleet', page:'dispatch' },
    { href:'masters.html', icon:'📦', label:'Master Data', page:'masters' },
    { href:'finance.html', icon:'💳', label:'Finance', page:'finance' },
    { href:'budgets.html', icon:'📊', label:'Budgets', page:'budgets' },
    { href:'reports.html', icon:'📈', label:'Reports', page:'reports' },
  ];
  const navHtml = navItems.map(item =>
    `<a href="${item.href}" class="nav-item ${activePage===item.page?'active':''}">
      <span class="nav-icon">${item.icon}</span>${item.label}
    </a>`
  ).join('');
  return `
  <div class="sidebar-brand">
    <div class="brand-title">MINEHAULOPS</div>
    <div class="brand-sub">Fleet Command &amp; ERP</div>
  </div>
  <nav class="sidebar-nav">
    <div class="nav-section-title">Operations</div>
    ${navHtml}
  </nav>
  <div class="sidebar-footer">
    <div class="sidebar-user">
      <div class="user-avatar">${(user.fullName||user.username||'?')[0].toUpperCase()}</div>
      <div class="user-info">
        <div class="user-name">${user.fullName||user.username||'User'}</div>
        <div class="user-role">${user.role||''}</div>
      </div>
    </div>
  </div>`;
}

function initSidebar(activePage) {
  const el = document.getElementById('sidebar');
  if (el) el.innerHTML = buildSidebar(activePage);
}

function initClock() {
  const el = document.getElementById('clock');
  if (!el) return;
  function update() { el.textContent = new Date().toLocaleTimeString('en-GB', {hour:'2-digit',minute:'2-digit',second:'2-digit'}); }
  update();
  setInterval(update, 1000);
}

function initLogout() {
  document.querySelectorAll('.btn-logout').forEach(btn => btn.addEventListener('click', logout));
}

// Mock data removed - all data fetched directly from PostgreSQL/SQL backend database

