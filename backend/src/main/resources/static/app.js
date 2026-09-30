async function j(res) { const t = await res.text(); try { return JSON.parse(t); } catch { return t; } }
function show(id, v) { document.getElementById(id).textContent = typeof v === 'string' ? v : JSON.stringify(v, null, 2); }

async function loadStatus() {
  const r = await fetch('/api/protection/status');
  const d = await j(r);
  const g = document.getElementById('statusGrid');
  g.innerHTML = '';
  const order = ['storageAccount','container','blobVersioning','blobSoftDelete','softDeleteWindowDays','changeFeed','pitRestore','pitRecoveryWindowDays','lifecycleManagement','tierCoolAfterDays','deleteVersionAfterDays','pitSoftDeleteRule'];
  for (const k of order) {
    if (!(k in d)) continue;
    const div = document.createElement('div');
    div.className = 'pill';
    div.textContent = k + ': ' + String(d[k]);
    g.appendChild(div);
  }
  document.getElementById('statusWarn').textContent = d.warning || '';
  document.getElementById('statusNote').textContent = d.statusNote || '';
}
async function listBlobs() {
  const r = await fetch('/api/blobs');
  const d = await j(r);
  const el = document.getElementById('blobList');
  if (!Array.isArray(d) || d.length === 0) { el.innerHTML = '<p>No blobs.</p>'; return; }
  let h = '<table><tr><th>Name</th><th>Version</th><th>Size</th><th>Modified</th><th>Actions</th></tr>';
  for (const b of d) {
    h += '<tr><td>' + b.blobName + '</td><td>' + (b.versionId || '').substring(0, 12) + '</td><td>' + b.contentLength + '</td><td>' + (b.lastModified || '') + '</td>'
      + '<td><button onclick="quickVersions(\'' + b.blobName + '\')">Versions</button> '
      + '<a href="/api/blobs/' + encodeURIComponent(b.blobName) + '/download"><button>Download</button></a></td></tr>';
  }
  el.innerHTML = h + '</table>';
}
async function upload() {
  const f = document.getElementById('fileInput').files[0];
  if (!f) { show('msg', 'Select a file first'); return; }
  const fd = new FormData(); fd.append('file', f);
  const r = await fetch('/api/blobs/upload', { method: 'POST', body: fd });
  show('msg', await j(r));
  listBlobs();
}
async function quickVersions(name) {
  document.getElementById('blobName').value = name;
  showVersions();
}
async function showVersions() {
  const n = document.getElementById('blobName').value.trim();
  if (!n) { show('msg', 'Enter blob name'); return; }
  const r = await fetch('/api/blobs/' + encodeURIComponent(n) + '/versions');
  const d = await j(r);
  const el = document.getElementById('versionList');
  if (!d.versions) { el.textContent = JSON.stringify(d); return; }
  let h = '<table><tr><th>VersionId</th><th>Current</th><th>Modified</th><th>Size</th><th>Actions</th></tr>';
  for (const v of d.versions) {
    h += '<tr><td>' + (v.versionId || '').substring(0, 20) + '</td><td>' + v.current + '</td><td>' + (v.lastModified || '') + '</td><td>' + v.contentLength + '</td>'
      + '<td><a href="/api/blobs/' + encodeURIComponent(n) + '/versions/' + encodeURIComponent(v.versionId) + '/download"><button>Download</button></a> '
      + '<button onclick="restoreVersion(\'' + n + '\',\'' + v.versionId + '\')">Restore this version</button></td></tr>';
  }
  el.innerHTML = h + '</table>';
}
async function restoreVersion(name, vid) {
  const r = await fetch('/api/blobs/' + encodeURIComponent(name) + '/restore/' + encodeURIComponent(vid), { method: 'POST' });
  show('msg', await j(r));
  showVersions();
}
async function requestPit() {
  const t = document.getElementById('pitTime').value.trim();
  const r = await fetch('/api/restore/point-in-time', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ restoreTime: t }) });
  const d = await j(r);
  if (d && d.restoreId) {
    show('pitOut', 'Admin action required (not executed by app user).\nRestoreId: ' + d.restoreId + '\nStatus: ' + d.status + '\nMessage: ' + d.message + '\nAdmin CLI: ' + d.cliCommand);
  } else {
    show('pitOut', d);
  }
}
async function validatePit() {
  const t = document.getElementById('pitTime').value.trim();
  if (!t) { show('pitOut', 'Enter UTC timestamp e.g. 2026-09-20T14:30:00Z'); return; }
  const d = new Date(t);
  if (isNaN(d)) { show('pitOut', 'Invalid ISO-8601 timestamp'); return; }
  if (d > new Date()) { show('pitOut', 'Must not be in the future'); return; }
  const earliest = new Date(Date.now() - 30 * 24 * 3600 * 1000);
  if (d < earliest) { show('pitOut', 'Outside 30-day window. Earliest: ' + earliest.toISOString()); return; }
  show('pitOut', 'Looks valid: ' + d.toISOString() + ' (server will re-validate)');
}
loadStatus();
