(() => {
    'use strict';

    // Same-origin when served by Spring Boot; falls back to localhost if index.html is opened from disk.
    const API_BASE = window.location.protocol === 'file:' ? 'http://localhost:8080/api' : '/api';
    const VIEWS = { dashboard: 'Dashboard', students: 'Students', schemes: 'Scholarship Schemes', applications: 'Applications', verification: 'Verification' };
    const POLL_MS = 10000;

    const state = { students: [], schemes: [], applications: [], verifications: [], stats: null, loaded: false };

    // ---------------------------------------------------------------- helpers
    const $ = (sel, root = document) => root.querySelector(sel);
    const $$ = (sel, root = document) => Array.from(root.querySelectorAll(sel));
    const esc = (v) => String(v ?? '').replace(/[&<>"']/g, (c) => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c]));
    const money = (n) => '₹' + Number(n).toLocaleString('en-IN', { maximumFractionDigits: 2 });
    const label = (s) => String(s || '').replace(/_/g, ' ');
    const num = (v) => (v === '' || v == null ? null : Number(v));
    const badge = (s) => (s ? `<span class="badge badge-${esc(s)}">${esc(label(s))}</span>` : '<span class="muted">—</span>');

    function fmtDate(d) {
        if (!d) return '—';
        const dt = String(d).length === 10 ? new Date(d + 'T00:00:00') : new Date(d);
        return dt.toLocaleDateString('en-IN', { day: '2-digit', month: 'short', year: 'numeric' });
    }

    async function api(path, options = {}) {
        let res;
        try {
            res = await fetch(API_BASE + path, { headers: { 'Content-Type': 'application/json' }, ...options });
        } catch (e) {
            const err = new Error('Cannot reach the server. Make sure the Spring Boot application is running on port 8080.');
            err.network = true;
            throw err;
        }
        const text = await res.text();
        let data = null;
        if (text) {
            try { data = JSON.parse(text); } catch (e) { data = text; }
        }
        if (!res.ok) {
            const err = new Error((data && data.message) || `Request failed (${res.status})`);
            err.status = res.status;
            err.details = data && data.errors;
            throw err;
        }
        return data;
    }

    function toast(message, type = 'info') {
        const el = document.createElement('div');
        el.className = `toast toast-${type}`;
        el.textContent = message;
        el.addEventListener('click', () => el.remove());
        $('#toasts').appendChild(el);
        setTimeout(() => el.remove(), type === 'error' ? 7000 : 5000);
    }

    function showFormError(form, err) {
        const box = $('.form-error', form);
        if (err.details && Object.keys(err.details).length) {
            box.innerHTML = 'Please fix the following:<ul>' + Object.values(err.details).map((m) => `<li>${esc(m)}</li>`).join('') + '</ul>';
        } else {
            box.textContent = err.message;
        }
        box.hidden = false;
    }

    const hideFormError = (form) => { $('.form-error', form).hidden = true; };

    async function guarded(btn, fn) {
        if (btn.disabled) return;
        const original = btn.textContent;
        btn.disabled = true;
        btn.textContent = 'Please wait…';
        try {
            await fn();
        } catch (err) {
            toast(err.message, 'error');
        } finally {
            btn.disabled = false;
            btn.textContent = original;
        }
    }

    // ---------------------------------------------------------------- rendering
    const emptyRow = (cols, msg) => `<tr><td colspan="${cols}" class="empty">${esc(msg)}</td></tr>`;

    function renderStats() {
        const s = state.stats;
        if (!s) return;
        const cards = [
            ['Total Students', s.totalStudents, ''],
            ['Total Scholarships', s.totalSchemes, ''],
            ['Total Applications', s.totalApplications, ''],
            ['Eligible Applications', s.eligibleApplications, 'green'],
            ['Ineligible Applications', s.ineligibleApplications, 'red'],
            ['Under Review', s.underReview, 'amber'],
            ['Approved', s.approved, 'green'],
            ['Disbursed', s.disbursed, 'purple']
        ];
        $('#statsGrid').innerHTML = cards.map(([l, v, c]) =>
            `<div class="stat ${c}"><div class="label">${esc(l)}</div><div class="value">${esc(v)}</div></div>`).join('');
    }

    function actionButtons(a) {
        const b = [];
        const btn = (text, action, cls = 'btn-primary') =>
            `<button type="button" class="btn ${cls} btn-sm" data-action="${action}" data-id="${a.id}">${text}</button>`;
        if (a.applicationStatus === 'SUBMITTED') b.push(btn('Start Review', 'start-review'));
        if (a.applicationStatus === 'UNDER_REVIEW') {
            b.push(btn('Verify', 'verify'));
            b.push(btn('Disburse', 'disburse', 'btn-ghost'));
        }
        if (a.applicationStatus === 'DISBURSEMENT_PENDING') b.push(btn('Disburse', 'disburse'));
        return b.length ? `<div class="actions">${b.join('')}</div>` : '<span class="muted">—</span>';
    }

    function applicationRow(a) {
        const remark = a.eligibilityStatus === 'INELIGIBLE' ? `<span class="remark">${esc(a.eligibilityRemarks)}</span>` : '';
        const vRemark = a.verificationRemarks ? `<span class="remark">${esc(a.verificationRemarks)}</span>` : '';
        return `<tr>
            <td class="nowrap"><strong>#${esc(a.id)}</strong></td>
            <td>${esc(a.studentName)}</td>
            <td>${esc(a.schemeName)}</td>
            <td>${badge(a.eligibilityStatus)}${remark}</td>
            <td>${badge(a.applicationStatus)}</td>
            <td>${badge(a.verificationStatus)}${vRemark}</td>
            <td class="nowrap">${fmtDate(a.applicationDate)}</td>
            <td>${actionButtons(a)}</td>
        </tr>`;
    }

    function renderApplications() {
        const fs = $('#filterStudent').value;
        const fst = $('#filterStatus').value;
        const fe = $('#filterEligibility').value;
        const list = state.applications.filter((a) =>
            (!fs || String(a.studentId) === fs) && (!fst || a.applicationStatus === fst) && (!fe || a.eligibilityStatus === fe));
        $('#applicationsBody').innerHTML = list.length ? list.map(applicationRow).join('')
            : emptyRow(8, state.applications.length ? 'No applications match the selected filters.' : 'No applications yet. Click “Apply for Scholarship” to create one.');
        const recent = state.applications.slice(0, 6);
        $('#recentBody').innerHTML = recent.length ? recent.map(applicationRow).join('') : emptyRow(8, 'No applications yet.');
    }

    function renderStudents() {
        $('#studentsBody').innerHTML = state.students.length ? state.students.map((s) => `<tr>
            <td>#${esc(s.id)}</td>
            <td><strong>${esc(s.name)}</strong></td>
            <td>${esc(s.email)}</td>
            <td>${esc(s.phone)}</td>
            <td class="nowrap">${money(s.annualIncome)}</td>
            <td>${esc(s.marks)}</td>
            <td>${esc(s.course)} <span class="muted">(Year ${esc(s.year)})</span></td>
            <td><button type="button" class="btn btn-danger btn-sm" data-action="delete-student" data-id="${s.id}">Delete</button></td>
        </tr>`).join('') : emptyRow(8, 'No students yet. Click “Add Student” to register one.');
    }

    function renderSchemes() {
        $('#schemesBody').innerHTML = state.schemes.length ? state.schemes.map((s) => `<tr>
            <td>#${esc(s.id)}</td>
            <td><strong>${esc(s.name)}</strong><span class="remark">${esc(s.description || '')}</span></td>
            <td class="nowrap">${money(s.incomeLimit)}</td>
            <td>${esc(s.minimumMarks)}</td>
            <td>${badge(s.status)}</td>
            <td><div class="actions">
                <button type="button" class="btn btn-ghost btn-sm" data-action="toggle-scheme" data-id="${s.id}">${s.status === 'ACTIVE' ? 'Deactivate' : 'Activate'}</button>
                <button type="button" class="btn btn-danger btn-sm" data-action="delete-scheme" data-id="${s.id}">Delete</button>
            </div></td>
        </tr>`).join('') : emptyRow(6, 'No scholarship schemes yet. Click “Add Scheme” to define one.');
    }

    function renderVerifications() {
        $('#verificationsBody').innerHTML = state.verifications.length ? state.verifications.map((v) => `<tr>
            <td>#${esc(v.id)}</td>
            <td>#${esc(v.applicationId)}</td>
            <td>${esc(v.studentName)}</td>
            <td>${esc(v.schemeName)}</td>
            <td>${esc(v.verifiedBy || '—')}</td>
            <td>${badge(v.status)}</td>
            <td class="nowrap">${fmtDate(v.verificationDate)}</td>
            <td><span class="remark" style="margin:0">${esc(v.remarks || '—')}</span></td>
            <td>${v.applicationStatus === 'DISBURSED' ? '<span class="muted">Locked</span>'
                : `<button type="button" class="btn btn-ghost btn-sm" data-action="edit-verification" data-id="${v.id}">${v.status === 'PENDING' ? 'Verify' : 'Edit'}</button>`}</td>
        </tr>`).join('') : emptyRow(9, 'No verification records yet. Start a review on an eligible application.');
    }

    function fillSelect(sel, items, valueFn, textFn, placeholder) {
        const previous = sel.value;
        sel.innerHTML = (placeholder ? `<option value="">${esc(placeholder)}</option>` : '') +
            items.map((i) => `<option value="${esc(valueFn(i))}">${esc(textFn(i))}</option>`).join('');
        if (previous && items.some((i) => String(valueFn(i)) === previous)) sel.value = previous;
    }

    function renderAll() {
        renderStats();
        fillSelect($('#filterStudent'), state.students, (s) => s.id, (s) => s.name, 'All students');
        renderApplications();
        renderStudents();
        renderSchemes();
        renderVerifications();
    }

    function renderLoadError(message) {
        const html = (cols) => `<tr><td colspan="${cols}" class="error-state">Unable to load data: ${esc(message)}<br>
            <button type="button" class="btn btn-ghost btn-sm" data-action="retry" style="margin-top:10px">Retry</button></td></tr>`;
        $('#recentBody').innerHTML = html(8);
        $('#applicationsBody').innerHTML = html(8);
        $('#studentsBody').innerHTML = html(8);
        $('#schemesBody').innerHTML = html(6);
        $('#verificationsBody').innerHTML = html(9);
    }

    function setLive(online) {
        const el = $('#liveIndicator');
        el.classList.toggle('online', online);
        el.classList.toggle('offline', !online);
        $('#liveText').textContent = online ? 'Live · updated ' + new Date().toLocaleTimeString('en-IN', { hour: '2-digit', minute: '2-digit', second: '2-digit' }) : 'Offline';
    }

    // ---------------------------------------------------------------- data loading
    async function refreshData(silent = false) {
        try {
            const [students, schemes, applications, verifications, stats] = await Promise.all([
                api('/students'), api('/schemes'), api('/applications'), api('/verifications'), api('/dashboard/stats')
            ]);
            Object.assign(state, { students, schemes, applications, verifications, stats, loaded: true });
            renderAll();
            setLive(true);
        } catch (err) {
            setLive(false);
            if (!state.loaded) renderLoadError(err.message);
            if (!silent) toast(err.message, 'error');
        }
    }

    // ---------------------------------------------------------------- navigation
    function route() {
        const name = (window.location.hash || '#dashboard').slice(1);
        const view = VIEWS[name] ? name : 'dashboard';
        $$('.view').forEach((v) => v.classList.toggle('active', v.id === 'view-' + view));
        $$('.nav-link').forEach((l) => l.classList.toggle('active', l.dataset.view === view));
        $('#pageTitle').textContent = VIEWS[view];
        $('#sidebar').classList.remove('open');
    }

    // ---------------------------------------------------------------- dialogs
    function openDialog(id) {
        const dialog = $('#' + id);
        const form = $('form', dialog);
        form.reset();
        hideFormError(form);
        dialog.showModal();
        return dialog;
    }

    function openApplyDialog() {
        if (!state.students.length) return toast('Add a student first.', 'warning');
        const active = state.schemes.filter((s) => s.status === 'ACTIVE');
        if (!active.length) return toast('There are no active scholarship schemes. Add or activate one first.', 'warning');
        openDialog('applyDialog');
        fillSelect($('#applyStudent'), state.students, (s) => s.id, (s) => `${s.name} (income ${money(s.annualIncome)}, marks ${s.marks})`);
        fillSelect($('#applyScheme'), active, (s) => s.id, (s) => `${s.name} (limit ${money(s.incomeLimit)}, min ${s.minimumMarks})`);
    }

    function openReviewDialog({ applicationId = null, verification = null } = {}) {
        const dialog = openDialog('reviewDialog');
        const form = $('#reviewForm');
        const select = $('#reviewApplication');
        if (verification) {
            $('#reviewTitle').textContent = 'Update Verification';
            form.elements.verificationId.value = verification.id;
            select.innerHTML = `<option value="${esc(verification.applicationId)}">#${esc(verification.applicationId)} — ${esc(verification.studentName)} / ${esc(verification.schemeName)}</option>`;
            select.disabled = true;
            form.elements.verifiedBy.value = verification.verifiedBy || '';
            if (verification.status !== 'PENDING') form.elements.status.value = verification.status;
            form.elements.remarks.value = verification.remarks || '';
        } else {
            $('#reviewTitle').textContent = 'Review Application';
            select.disabled = false;
            const candidates = state.applications.filter((a) => a.applicationStatus === 'UNDER_REVIEW');
            if (!candidates.length) {
                select.innerHTML = '<option value="">No applications are under review</option>';
            } else {
                fillSelect(select, candidates, (a) => a.id, (a) => `#${a.id} — ${a.studentName} / ${a.schemeName}`);
                if (applicationId) select.value = String(applicationId);
            }
        }
        return dialog;
    }

    // ---------------------------------------------------------------- form submissions
    async function submitForm(form, work) {
        const button = $('button[type=submit]', form);
        hideFormError(form);
        const original = button.textContent;
        button.disabled = true;
        button.textContent = 'Saving…';
        try {
            await work();
            form.closest('dialog').close();
            await refreshData(true);
        } catch (err) {
            showFormError(form, err);
        } finally {
            button.disabled = false;
            button.textContent = original;
        }
    }

    $('#studentForm').addEventListener('submit', (e) => {
        e.preventDefault();
        const fd = Object.fromEntries(new FormData(e.target));
        submitForm(e.target, async () => {
            const s = await api('/students', {
                method: 'POST',
                body: JSON.stringify({
                    name: fd.name.trim(), email: fd.email.trim(), phone: fd.phone.trim(),
                    annualIncome: num(fd.annualIncome), marks: num(fd.marks), course: fd.course.trim(), year: num(fd.year)
                })
            });
            toast(`Student “${s.name}” added.`, 'success');
        });
    });

    $('#schemeForm').addEventListener('submit', (e) => {
        e.preventDefault();
        const fd = Object.fromEntries(new FormData(e.target));
        submitForm(e.target, async () => {
            const s = await api('/schemes', {
                method: 'POST',
                body: JSON.stringify({
                    name: fd.name.trim(), description: fd.description.trim(),
                    incomeLimit: num(fd.incomeLimit), minimumMarks: num(fd.minimumMarks), status: fd.status
                })
            });
            toast(`Scheme “${s.name}” created.`, 'success');
        });
    });

    $('#applyForm').addEventListener('submit', (e) => {
        e.preventDefault();
        const fd = Object.fromEntries(new FormData(e.target));
        submitForm(e.target, async () => {
            const a = await api('/applications', {
                method: 'POST',
                body: JSON.stringify({ studentId: num(fd.studentId), schemeId: num(fd.schemeId) })
            });
            if (a.eligibilityStatus === 'ELIGIBLE') {
                toast(`Application #${a.id} submitted — ELIGIBLE. ${a.eligibilityRemarks}`, 'success');
            } else {
                toast(`Application #${a.id} flagged INELIGIBLE. ${a.eligibilityRemarks}`, 'warning');
            }
        });
    });

    $('#reviewForm').addEventListener('submit', (e) => {
        e.preventDefault();
        const form = e.target;
        const fd = Object.fromEntries(new FormData(form));
        submitForm(form, async () => {
            const payload = { verifiedBy: fd.verifiedBy.trim(), status: fd.status, remarks: fd.remarks.trim() };
            let v;
            if (fd.verificationId) {
                v = await api('/verifications/' + fd.verificationId, { method: 'PUT', body: JSON.stringify(payload) });
            } else {
                if (!fd.applicationId) throw new Error('Select an application that is under review.');
                v = await api('/verifications', { method: 'POST', body: JSON.stringify({ applicationId: num(fd.applicationId), ...payload }) });
            }
            toast(`Application #${v.applicationId} ${v.status === 'APPROVED' ? 'approved — disbursement pending' : 'rejected'}.`, v.status === 'APPROVED' ? 'success' : 'warning');
        });
    });

    // ---------------------------------------------------------------- click actions
    document.addEventListener('click', (e) => {
        const closer = e.target.closest('[data-close]');
        if (closer) { closer.closest('dialog').close(); return; }

        const t = e.target.closest('[data-action]');
        if (!t) return;
        const id = Number(t.dataset.id);

        switch (t.dataset.action) {
            case 'add-student': openDialog('studentDialog'); break;
            case 'add-scheme': openDialog('schemeDialog'); break;
            case 'apply': openApplyDialog(); break;
            case 'new-review': openReviewDialog(); break;
            case 'verify': openReviewDialog({ applicationId: id }); break;
            case 'edit-verification': openReviewDialog({ verification: state.verifications.find((v) => v.id === id) }); break;
            case 'retry': refreshData(); break;
            case 'start-review':
                guarded(t, async () => {
                    await api(`/applications/${id}/review`, { method: 'PUT' });
                    toast(`Application #${id} moved to UNDER REVIEW.`, 'success');
                    await refreshData(true);
                });
                break;
            case 'disburse':
                if (!window.confirm(`Mark application #${id} as disbursed?`)) break;
                guarded(t, async () => {
                    await api(`/applications/${id}/disburse`, { method: 'PUT' });
                    toast(`Application #${id} disbursed.`, 'success');
                    await refreshData(true);
                });
                break;
            case 'delete-student':
                if (!window.confirm('Delete this student?')) break;
                guarded(t, async () => {
                    await api('/students/' + id, { method: 'DELETE' });
                    toast('Student deleted.', 'success');
                    await refreshData(true);
                });
                break;
            case 'delete-scheme':
                if (!window.confirm('Delete this scholarship scheme?')) break;
                guarded(t, async () => {
                    await api('/schemes/' + id, { method: 'DELETE' });
                    toast('Scheme deleted.', 'success');
                    await refreshData(true);
                });
                break;
            case 'toggle-scheme': {
                const s = state.schemes.find((x) => x.id === id);
                guarded(t, async () => {
                    await api('/schemes/' + id, {
                        method: 'PUT',
                        body: JSON.stringify({
                            name: s.name, description: s.description, incomeLimit: s.incomeLimit,
                            minimumMarks: s.minimumMarks, status: s.status === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE'
                        })
                    });
                    toast(`Scheme “${s.name}” is now ${s.status === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE'}.`, 'success');
                    await refreshData(true);
                });
                break;
            }
            default: break;
        }
    });

    ['filterStudent', 'filterStatus', 'filterEligibility'].forEach((id) => $('#' + id).addEventListener('change', renderApplications));
    $('#refreshBtn').addEventListener('click', () => refreshData());
    $('#menuBtn').addEventListener('click', () => $('#sidebar').classList.toggle('open'));
    window.addEventListener('hashchange', route);

    // ---------------------------------------------------------------- start
    route();
    refreshData();
    // Real-time status: poll the API so students/verifiers always see the latest application status.
    setInterval(() => {
        const dialogOpen = $$('dialog').some((d) => d.open);
        if (!document.hidden && !dialogOpen) refreshData(true);
    }, POLL_MS);
})();
