/* ============================================
   QSpark - A production-grade smart parking management system
   Copyright (C) 2026 PawanSimha

   This program is free software: you can redistribute it and/or modify
   it under the terms of the GNU General Public License as published by
   the Free Software Foundation, either version 3 of the License, or
   (at your option) any later version.

   This program is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU General Public License for more details.

   You should have received a copy of the GNU General Public License
   along with this program.  If not, see <https://www.gnu.org/licenses/>.
   ============================================ */

// ============================================
// Toast Notification System
// ============================================
const Toast = {
  container: null,

  init: function () {
    this.container = document.getElementById('toastContainer');
    if (!this.container) {
      this.container = document.createElement('div');
      this.container.id = 'toastContainer';
      this.container.className = 'toast-container';
      document.body.appendChild(this.container);
    }
  },

  show: function (message, type, options) {
    if (!this.container) this.init();
    options = options || {};
    var title = options.title || '';
    var duration = options.duration || 5000;
    var undoCallback = options.undo || null;

    var icons = {
      success: '\u2713',
      error: '\u2717',
      info: '\u2139'
    };

    var toast = document.createElement('div');
    toast.className = 'toast ' + (type || 'info');

    toast.innerHTML =
      '<div class="toast-icon">' + (icons[type] || icons.info) + '</div>' +
      '<div class="toast-content">' +
        (title ? '<div class="toast-title">' + this._esc(title) + '</div>' : '') +
        '<div class="toast-message">' + this._esc(message) + '</div>' +
      '</div>' +
      '<button class="toast-close" onclick="Toast.dismiss(this.parentElement)" aria-label="Close">&times;</button>';

    if (undoCallback) {
      var undoBtn = document.createElement('button');
      undoBtn.className = 'toast-undo';
      undoBtn.textContent = '\u21A9 Undo';
      undoBtn.onclick = function () {
        undoCallback();
        Toast.dismiss(toast);
      };
      toast.querySelector('.toast-content').appendChild(undoBtn);
    }

    this.container.appendChild(toast);

    if (duration > 0) {
      toast._timeout = setTimeout(function () {
        Toast.dismiss(toast);
      }, duration);
    }

    return toast;
  },

  dismiss: function (toast) {
    if (toast._timeout) clearTimeout(toast._timeout);
    if (toast.classList.contains('removing')) return;
    toast.classList.add('removing');
    setTimeout(function () {
      if (toast.parentNode) toast.parentNode.removeChild(toast);
    }, 250);
  },

  success: function (message, options) {
    return this.show(message, 'success', options);
  },

  error: function (message, options) {
    return this.show(message, 'error', options);
  },

  info: function (message, options) {
    return this.show(message, 'info', options);
  },

  _esc: function (s) {
    var d = document.createElement('div');
    d.textContent = s;
    return d.innerHTML;
  }
};

// ============================================
// Modal System
// ============================================
const Modal = {
  open: function (config) {
    this.close();
    var overlay = document.createElement('div');
    overlay.className = 'modal-overlay';
    overlay.id = 'activeModal';

    var modal = document.createElement('div');
    modal.className = 'modal';
    modal.setAttribute('role', 'dialog');
    modal.setAttribute('aria-modal', 'true');
    if (config.title) modal.setAttribute('aria-label', config.title);

    var html = '';

    if (config.title) {
      html += '<div class="modal-header">' +
        '<h2>' + config.title + '</h2>' +
        '<button class="btn btn-ghost btn-icon" onclick="Modal.close()" aria-label="Close">&times;</button>' +
        '</div>';
    }

    html += '<div class="modal-body">' + (config.body || '') + '</div>';

    if (config.buttons && config.buttons.length) {
      html += '<div class="modal-footer">';
      for (var i = 0; i < config.buttons.length; i++) {
        var b = config.buttons[i];
        html += '<button class="btn ' + (b.className || '') + '" data-action="' + i + '">' +
          (b.label || 'Button') + '</button>';
      }
      html += '</div>';
    }

    modal.innerHTML = html;
    overlay.appendChild(modal);
    document.body.appendChild(overlay);

    var btns = modal.querySelectorAll('[data-action]');
    for (var j = 0; j < btns.length; j++) {
      btns[j].onclick = function () {
        var idx = parseInt(this.getAttribute('data-action'));
        if (config.buttons && config.buttons[idx] && config.buttons[idx].onClick) {
          config.buttons[idx].onClick();
        }
      };
    }

    overlay.addEventListener('click', function (e) {
      if (e.target === overlay) {
        if (config.closeOnOverlay !== false) Modal.close();
      }
    });

    document.addEventListener('keydown', this._keyHandler);

    var focusable = modal.querySelectorAll('button, [href], input, select, textarea, [tabindex]:not([tabindex="-1"])');
    if (focusable.length) setTimeout(function () { focusable[0].focus(); }, 100);

    this._currentConfig = config;
    return overlay;
  },

  close: function () {
    var overlay = document.getElementById('activeModal');
    if (overlay) {
      document.removeEventListener('keydown', this._keyHandler);
      overlay.parentNode.removeChild(overlay);
    }
    this._currentConfig = null;
  },

  confirm: function (options) {
    var title = options.title || 'Confirm';
    var message = options.message || 'Are you sure?';
    var confirmText = options.confirmText || 'Confirm';
    var cancelText = options.cancelText || 'Cancel';
    var confirmClass = options.confirmClass || 'btn-primary';
    var onConfirm = options.onConfirm || function () {};
    var onCancel = options.onCancel || function () {};

    return this.open({
      title: title,
      body: '<p>' + message + '</p>',
      closeOnOverlay: false,
      buttons: [
        {
          label: cancelText,
          className: 'btn-secondary',
          onClick: function () {
            Modal.close();
            if (onCancel) onCancel();
          }
        },
        {
          label: confirmText,
          className: 'btn ' + confirmClass,
          onClick: function () {
            Modal.close();
            if (onConfirm) onConfirm();
          }
        }
      ]
    });
  },

  _keyHandler: function (e) {
    if (e.key === 'Escape') Modal.close();
    if (e.key === 'Tab') {
      var modal = document.querySelector('.modal');
      if (!modal) return;
      var focusable = modal.querySelectorAll('button, [href], input, select, textarea, [tabindex]:not([tabindex="-1"])');
      if (!focusable.length) return;
      var first = focusable[0];
      var last = focusable[focusable.length - 1];
      if (e.shiftKey && document.activeElement === first) {
        e.preventDefault();
        last.focus();
      } else if (!e.shiftKey && document.activeElement === last) {
        e.preventDefault();
        first.focus();
      }
    }
  }
};

// ============================================
// Dark Mode
// ============================================
function toggleDarkMode() {
  document.body.classList.toggle('dark');
  var isDark = document.body.classList.contains('dark');
  localStorage.setItem('qspark-dark-mode', isDark ? 'true' : 'false');
  var btn = document.getElementById('darkModeToggle');
  if (btn) btn.innerHTML = isDark ? sunIcon() : moonIcon();
}

function applyDarkMode() {
  var isDark = localStorage.getItem('qspark-dark-mode') === 'true';
  if (isDark) {
    document.body.classList.add('dark');
    var btn = document.getElementById('darkModeToggle');
    if (btn) btn.innerHTML = sunIcon();
  }
}

function moonIcon() {
  return '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 12.79A9 9 0 1 1 11.21 3 7 7 0 0 0 21 12.79z"/></svg>';
}

function sunIcon() {
  return '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="5"/><path d="M12 1v2M12 21v2M4.22 4.22l1.42 1.42M18.36 18.36l1.42 1.42M1 12h2M21 12h2M4.22 19.78l1.42-1.42M18.36 5.64l1.42-1.42"/></svg>';
}

// ============================================
// Live Clock
// ============================================
function updateClock() {
  var now = new Date();
  var days = ['Sunday', 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday'];
  var months = ['January', 'February', 'March', 'April', 'May', 'June', 'July', 'August', 'September', 'October', 'November', 'December'];
  var dateEl = document.getElementById('liveDate');
  var timeEl = document.getElementById('liveTime');
  if (dateEl) dateEl.textContent = days[now.getDay()] + ', ' + now.getDate() + ' ' + months[now.getMonth()];
  if (timeEl) timeEl.textContent = now.getHours().toString().padStart(2, '0') + ':' + now.getMinutes().toString().padStart(2, '0') + ':' + now.getSeconds().toString().padStart(2, '0');
}
updateClock();
setInterval(updateClock, 1000);

// ============================================
// Table Sorting
// ============================================
var tableSortState = {};

function makeSortable(tableId) {
  var table = document.getElementById(tableId);
  if (!table) return;
  var ths = table.querySelectorAll('thead th');
  for (var i = 0; i < ths.length; i++) {
    if (ths[i].getAttribute('data-sortable') !== 'false') {
      ths[i].classList.add('sortable');
      ths[i].setAttribute('data-col', i);
      ths[i].onclick = function () {
        sortTableByColumn(tableId, parseInt(this.getAttribute('data-col')));
      };
    }
  }
}

function sortTableByColumn(tableId, colIndex) {
  var table = document.getElementById(tableId);
  if (!table) return;
  var tbody = table.querySelector('tbody');
  var rows = Array.from(tbody.querySelectorAll('tr'));

  if (!tableSortState[tableId]) tableSortState[tableId] = {};
  var state = tableSortState[tableId];
  var asc = state.col === colIndex ? !state.asc : true;
  state.col = colIndex;
  state.asc = asc;

  var ths = table.querySelectorAll('thead th');
  for (var i = 0; i < ths.length; i++) {
    ths[i].classList.remove('sort-asc', 'sort-desc');
    if (i === colIndex) {
      ths[i].classList.add(asc ? 'sort-asc' : 'sort-desc');
    }
  }

  rows.sort(function (a, b) {
    var va = a.cells[colIndex] ? a.cells[colIndex].textContent.trim() : '';
    var vb = b.cells[colIndex] ? b.cells[colIndex].textContent.trim() : '';
    var cmp = va.localeCompare(vb, undefined, { numeric: true });
    return asc ? cmp : -cmp;
  });

  rows.forEach(function (r) { tbody.appendChild(r); });
}

// ============================================
// Table Filtering
// ============================================
function filterTable(tableId, inputId) {
  var q = document.getElementById(inputId);
  if (!q) return;
  var query = q.value.toLowerCase();
  var table = document.getElementById(tableId);
  if (!table) return;
  var rows = table.querySelectorAll('tbody tr');
  for (var i = 0; i < rows.length; i++) {
    var text = '';
    var cols = rows[i].querySelectorAll('td');
    for (var j = 0; j < cols.length; j++) {
      text += cols[j].textContent.toLowerCase() + ' ';
    }
    rows[i].style.display = text.indexOf(query) >= 0 ? '' : 'none';
  }
}

// ============================================
// Client-Side Pagination
// ============================================
function paginateTable(tableId, perPage) {
  var table = document.getElementById(tableId);
  if (!table) return;
  var tbody = table.querySelector('tbody');
  var rows = Array.from(tbody.querySelectorAll('tr'));
  var totalPages = Math.ceil(rows.length / perPage) || 1;

  var footer = table.parentElement.querySelector('.table-footer');
  if (!footer) {
    footer = document.createElement('div');
    footer.className = 'table-footer';
    table.parentElement.appendChild(footer);
  }

  function renderPage(page) {
    page = Math.max(1, Math.min(page, totalPages));
    var start = (page - 1) * perPage;
    var end = start + perPage;
    for (var i = 0; i < rows.length; i++) {
      rows[i].style.display = (i >= start && i < end) ? '' : 'none';
    }

    var html = '<div class="pagination">';
    html += '<button class="pagination-btn" onclick="paginateGo(\'' + tableId + '\', ' + (page - 1) + ')" ' + (page <= 1 ? 'disabled' : '') + '>&lsaquo; Prev</button>';
    var startPage = Math.max(1, page - 2);
    var endPage = Math.min(totalPages, page + 2);
    if (startPage > 1) {
      html += '<button class="pagination-btn" onclick="paginateGo(\'' + tableId + '\', 1)">1</button>';
      if (startPage > 2) html += '<span class="pagination-info" style="margin:0 4px;">...</span>';
    }
    for (var p = startPage; p <= endPage; p++) {
      html += '<button class="pagination-btn' + (p === page ? ' active' : '') + '" onclick="paginateGo(\'' + tableId + '\', ' + p + ')">' + p + '</button>';
    }
    if (endPage < totalPages) {
      if (endPage < totalPages - 1) html += '<span class="pagination-info" style="margin:0 4px;">...</span>';
      html += '<button class="pagination-btn" onclick="paginateGo(\'' + tableId + '\', ' + totalPages + ')">' + totalPages + '</button>';
    }
    html += '<button class="pagination-btn" onclick="paginateGo(\'' + tableId + '\', ' + (page + 1) + ')" ' + (page >= totalPages ? 'disabled' : '') + '>Next &rsaquo;</button>';
    html += '</div>';
    html += '<span class="pagination-info">' + (start + 1) + '&ndash;' + Math.min(end, rows.length) + ' of ' + rows.length + '</span>';

    footer.innerHTML = html;
    footer._page = page;
    footer._totalPages = totalPages;
  }

  renderPage(1);
  table._paginate = { render: renderPage, perPage: perPage };
}

function paginateGo(tableId, page) {
  var table = document.getElementById(tableId);
  if (!table || !table._paginate) return;
  table._paginate.render(page);
}

// ============================================
// CSV Export
// ============================================
function exportCSV(tableId, filename) {
  var table = document.getElementById(tableId);
  if (!table) return;
  var csv = [];
  var ths = table.querySelectorAll('thead th');
  var maxCols = ths.length;
  var headers = [];
  for (var i = 0; i < maxCols; i++) {
    headers.push('"' + ths[i].textContent.trim() + '"');
  }
  csv.push(headers.join(','));
  var rows = table.querySelectorAll('tbody tr');
  for (var r = 0; r < rows.length; r++) {
    if (rows[r].style.display === 'none') continue;
    var cols = rows[r].querySelectorAll('td');
    var row = [];
    for (var c = 0; c < Math.min(cols.length, maxCols); c++) {
      row.push('"' + cols[c].textContent.trim().replace(/"/g, '""') + '"');
    }
    csv.push(row.join(','));
  }
  var blob = new Blob([csv.join('\n')], { type: 'text/csv;charset=utf-8;' });
  var link = document.createElement('a');
  link.href = URL.createObjectURL(blob);
  link.download = filename || 'export.csv';
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
  URL.revokeObjectURL(link.href);
}

// ============================================
// Vehicle Number Format Helpers
// ============================================
var VEHICLE_FORMATS = {
  'std1': { label: 'Standard (1-letter)', pattern: '^[A-Z]{2} [0-9]{2} [A-Z]{1} [0-9]{4}$', example: 'KA 04 A 2256' },
  'std2': { label: 'Standard (2-letter)', pattern: '^[A-Z]{2} [0-9]{2} [A-Z]{2} [0-9]{4}$', example: 'DL 03 CA 1234' },
  'std3': { label: 'Standard (3-letter)', pattern: '^[A-Z]{2} [0-9]{2} [A-Z]{3} [0-9]{4}$', example: 'ML 04 ASD 5256' },
  'bhr':  { label: 'Bharat Series', pattern: '^[0-9]{2} BH [0-9]{4} [A-Z]{2}$', example: '26 BH 9876 AB' }
};

function populateFormatSelect(selectId) {
  var sel = document.getElementById(selectId);
  if (!sel) return;
  sel.innerHTML = '<option value="">Select Format</option>';
  for (var key in VEHICLE_FORMATS) {
    if (VEHICLE_FORMATS.hasOwnProperty(key)) {
      var f = VEHICLE_FORMATS[key];
      sel.innerHTML += '<option value="' + key + '">' + f.label + ' \u2014 ' + f.example + '</option>';
    }
  }
}

function setupVehicleFormat(formatSelectId, vehicleInputId) {
  var sel = document.getElementById(formatSelectId);
  var inp = document.getElementById(vehicleInputId);
  if (!sel || !inp) return;

  sel.addEventListener('change', function () {
    var fmt = VEHICLE_FORMATS[sel.value];
    if (fmt) {
      inp.placeholder = 'e.g. ' + fmt.example;
      inp.pattern = fmt.pattern;
      inp.title = 'Format: ' + fmt.example;
      inp.readOnly = false;
      inp.style.cursor = '';
      inp.value = '';
    } else {
      inp.placeholder = 'Select format above';
      inp.pattern = '';
      inp.title = '';
      inp.readOnly = true;
      inp.style.cursor = 'not-allowed';
      inp.value = '';
    }
  });

  inp.addEventListener('input', function () {
    this.value = this.value.toUpperCase().replace(/[^A-Z0-9 ]/g, '');
  });
}

// ============================================
// Slot Grid
// ============================================
function buildSlotGrid(basement, occupiedSlots, gridId, hiddenId, statusId) {
  var grid = document.getElementById(gridId);
  var hidden = document.getElementById(hiddenId);
  var status = document.getElementById(statusId);
  if (!grid) return;

  hidden.value = '';
  status.textContent = basement + ' — Select a slot';
  grid.innerHTML = '';

  var occupied = occupiedSlots.split(',').filter(Boolean);
  var total = 120;
  var cols = 15;
  var rows = Math.ceil(total / cols);

  function makeTileClickHandler(tile, slotVal) {
    tile.onclick = function () {
      document.querySelectorAll('.slot-tile.selected').forEach(function (t) {
        t.classList.remove('selected');
      });
      this.classList.add('selected');
      document.getElementById(hiddenId).value = slotVal;
      document.getElementById(statusId).textContent = 'Selected: ' + slotVal;
      var qj = document.getElementById('slotQuickJump');
      if (qj) qj.value = slotVal;
    };
  }

  for (var r = 0; r < rows; r++) {
    var start = r * cols + 1;
    var end = Math.min((r + 1) * cols, total);
    var label = document.createElement('div');
    label.className = 'slot-row-label';
    label.textContent = basement + '-' + start.toString().padStart(3, '0') + ' to ' + basement + '-' + end.toString().padStart(3, '0');
    grid.appendChild(label);

    for (var c = 0; c < cols; c++) {
      var i = r * cols + c + 1;
      if (i > total) break;
      var n = i.toString().padStart(3, '0');
      var slotVal = basement + '-' + n;
      var tile = document.createElement('div');
      tile.className = 'slot-tile';
      tile.textContent = n;
      tile.dataset.slot = slotVal;
      if (occupied.indexOf(slotVal) >= 0) {
        tile.classList.add('occupied');
      } else {
        tile.classList.add('available');
        makeTileClickHandler(tile, slotVal);
      }
      grid.appendChild(tile);
    }
  }
}

// ============================================
// Slot Quick-Jump
// ============================================
function quickJumpSlot() {
  var input = document.getElementById('slotQuickJump');
  if (!input) return;
  var val = input.value.trim().toUpperCase();
  var grid = document.getElementById('slotGrid');
  if (!grid) return;

  grid.querySelectorAll('.slot-tile.highlight').forEach(function (t) {
    t.classList.remove('highlight');
  });

  if (!val) return;

  var tiles = grid.querySelectorAll('.slot-tile');
  var matches = [];
  var exactMatch = null;

  for (var i = 0; i < tiles.length; i++) {
    var slot = tiles[i].dataset.slot;
    if (!slot) continue;
    var isOccupied = tiles[i].classList.contains('occupied');
    if (slot === val) {
      exactMatch = { tile: tiles[i], occupied: isOccupied };
      matches.push(tiles[i]);
    } else if (slot.indexOf(val) >= 0) {
      matches.push(tiles[i]);
    }
  }

  // If exactly one match and it's available, auto-select
  if (exactMatch && !exactMatch.occupied && matches.length === 1) {
    exactMatch.tile.click();
    return;
  }

  // If one non-occupied match via partial search
  if (matches.length === 1 && !matches[0].classList.contains('occupied')) {
    matches[0].click();
    return;
  }

  // Otherwise highlight all matches
  matches.forEach(function (t) {
    t.classList.add('highlight');
  });
  if (matches.length > 0) {
    matches[0].scrollIntoView({ behavior: 'smooth', block: 'nearest' });
  }
}

// Enter key on quick-jump triggers jump
document.addEventListener('DOMContentLoaded', function () {
  var qj = document.getElementById('slotQuickJump');
  if (qj) {
    qj.addEventListener('keydown', function (e) {
      if (e.key === 'Enter') {
        e.preventDefault();
        quickJumpSlot();
      }
    });
  }
});

// ============================================
// Initialize on DOM ready
// ============================================
document.addEventListener('DOMContentLoaded', function () {
  applyDarkMode();
});
