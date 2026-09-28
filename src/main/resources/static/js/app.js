// ScholarTrack Frontend Application Logic

document.addEventListener('DOMContentLoaded', () => {
    initNavigation();
    loadDashboardData();
    loadStudents();
    loadSchemes();
    loadApplications();
    loadVerifications();
    loadDisbursements();
});

// Helper: Show Toast Notifications
function showToast(message, type = 'info') {
    const container = document.getElementById('toast-container');
    const toast = document.createElement('div');
    toast.className = `toast ${type}`;
    toast.textContent = message;
    container.appendChild(toast);

    setTimeout(() => {
        toast.remove();
    }, 4000);
}

// Sidebar Navigation Handling
function initNavigation() {
    const navItems = document.querySelectorAll('.nav-item');
    navItems.forEach(item => {
        item.addEventListener('click', () => {
            navItems.forEach(i => i.classList.remove('active'));
            item.classList.add('active');

            const targetId = item.getAttribute('data-target');
            document.querySelectorAll('.content-section').forEach(sec => sec.classList.remove('active'));
            document.getElementById(targetId).classList.add('active');

            // Update header title
            const titles = {
                'dashboard-section': ['Dashboard Overview', 'Real-time statistics & verification tracker'],
                'students-section': ['Student Profiles', 'Manage student records and academic eligibility'],
                'schemes-section': ['Scholarship Schemes', 'Define income limits and minimum marks rules'],
                'apply-section': ['Apply for Scholarship', 'Automatic eligibility rule evaluation engine'],
                'verification-section': ['Verification Queue', 'Document review and manual application verification'],
                'disbursement-section': ['Disbursement Status', 'Manage fund payouts post-verification approval'],
                'tracking-section': ['Track Status', 'Lookup real-time status by Application ID']
            };

            if (titles[targetId]) {
                document.getElementById('current-section-title').textContent = titles[targetId][0];
                document.getElementById('current-section-subtitle').textContent = titles[targetId][1];
            }

            // Refresh target view data
            if (targetId === 'dashboard-section') loadDashboardData();
            if (targetId === 'students-section') loadStudents();
            if (targetId === 'schemes-section') loadSchemes();
            if (targetId === 'apply-section') populateApplyFormDropdowns();
            if (targetId === 'verification-section') loadVerifications();
            if (targetId === 'disbursement-section') loadDisbursements();
        });
    });
}

// Modal Toggle Utilities
function openModal(modalId) {
    document.getElementById(modalId).classList.add('active');
}

function closeModal(modalId) {
    document.getElementById(modalId).classList.remove('active');
}

// 1. Dashboard Overview
async function loadDashboardData() {
    try {
        const [stdRes, schRes, appRes] = await Promise.all([
            fetch('/api/students'),
            fetch('/api/schemes'),
            fetch('/api/applications')
        ]);

        const students = await stdRes.json();
        const schemes = await schRes.json();
        const applications = await appRes.json();

        document.getElementById('stat-total-students').textContent = students.length;
        document.getElementById('stat-total-schemes').textContent = schemes.length;
        document.getElementById('stat-total-apps').textContent = applications.length;

        const eligibleApps = applications.filter(a => a.eligibilityStatus === 'ELIGIBLE');
        const approvedApps = applications.filter(a => a.applicationStatus === 'APPROVED');
        const pendingVerifications = applications.filter(a => a.eligibilityStatus === 'ELIGIBLE' && a.applicationStatus === 'SUBMITTED');

        document.getElementById('stat-eligible-apps').textContent = eligibleApps.length;
        document.getElementById('stat-pending-verifications').textContent = pendingVerifications.length;
        document.getElementById('stat-approved-apps').textContent = approvedApps.length;

        // Render Recent Applications Table
        const tbody = document.getElementById('recent-applications-body');
        if (applications.length === 0) {
            tbody.innerHTML = `<tr><td colspan="7" class="text-center">No applications found in system.</td></tr>`;
            return;
        }

        tbody.innerHTML = applications.map(app => `
            <tr>
                <td><strong>#${app.id}</strong></td>
                <td>${app.student ? app.student.name : 'N/A'}</td>
                <td>${app.scheme ? app.scheme.name : 'N/A'}</td>
                <td>${app.applicationDate ? new Date(app.applicationDate).toLocaleDateString() : 'Today'}</td>
                <td><span class="badge ${app.eligibilityStatus === 'ELIGIBLE' ? 'badge-eligible' : 'badge-not-eligible'}">${app.eligibilityStatus}</span></td>
                <td><span class="badge ${getStatusBadgeClass(app.applicationStatus)}">${app.applicationStatus}</span></td>
                <td><span class="badge ${getDisbursementBadgeClass(app.disbursementStatus)}">${app.disbursementStatus}</span></td>
            </tr>
        `).join('');

    } catch (err) {
        showToast('Error loading dashboard data: ' + err.message, 'error');
    }
}

// 2. Students Section
async function loadStudents() {
    try {
        const res = await fetch('/api/students');
        const students = await res.json();
        const tbody = document.getElementById('students-table-body');

        if (students.length === 0) {
            tbody.innerHTML = `<tr><td colspan="8" class="text-center">No student records registered yet.</td></tr>`;
            return;
        }

        tbody.innerHTML = students.map(s => `
            <tr>
                <td>#${s.id}</td>
                <td><strong>${s.name}</strong></td>
                <td>${s.email}</td>
                <td>${s.phone}</td>
                <td><strong>${s.marks}%</strong></td>
                <td>Rs. ${s.annualIncome.toLocaleString()}</td>
                <td>${s.course} (${s.college})</td>
                <td>
                    <button class="btn btn-secondary btn-sm" onclick="deleteStudent(${s.id})">Delete</button>
                </td>
            </tr>
        `).join('');

    } catch (err) {
        showToast('Error loading student records: ' + err.message, 'error');
    }
}

async function handleCreateStudent(event) {
    event.preventDefault();
    const payload = {
        name: document.getElementById('std-name').value,
        email: document.getElementById('std-email').value,
        phone: document.getElementById('std-phone').value,
        marks: parseFloat(document.getElementById('std-marks').value),
        annualIncome: parseFloat(document.getElementById('std-income').value),
        course: document.getElementById('std-course').value,
        college: document.getElementById('std-college').value
    };

    try {
        const res = await fetch('/api/students', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });

        if (!res.ok) {
            const errData = await res.json();
            throw new Error(errData.message || 'Failed to create student');
        }

        showToast('Student registered successfully!', 'success');
        closeModal('add-student-modal');
        document.getElementById('add-student-form').reset();
        loadStudents();
    } catch (err) {
        showToast(err.message, 'error');
    }
}

async function deleteStudent(id) {
    if (!confirm('Are you sure you want to delete student #' + id + '?')) return;

    try {
        const res = await fetch(`/api/students/${id}`, { method: 'DELETE' });
        if (!res.ok) throw new Error('Failed to delete student');

        showToast('Student record deleted successfully', 'info');
        loadStudents();
    } catch (err) {
        showToast(err.message, 'error');
    }
}

// 3. Scholarship Schemes Section
async function loadSchemes() {
    try {
        const res = await fetch('/api/schemes');
        const schemes = await res.json();
        const tbody = document.getElementById('schemes-table-body');

        if (schemes.length === 0) {
            tbody.innerHTML = `<tr><td colspan="5" class="text-center">No active scholarship schemes.</td></tr>`;
            return;
        }

        tbody.innerHTML = schemes.map(sch => `
            <tr>
                <td>#${sch.id}</td>
                <td><strong>${sch.name}</strong></td>
                <td>Min ${sch.minimumMarks}%</td>
                <td>Max Rs. ${sch.incomeLimit.toLocaleString()}</td>
                <td><span class="badge badge-eligible">${sch.status}</span></td>
            </tr>
        `).join('');

    } catch (err) {
        showToast('Error loading scholarship schemes: ' + err.message, 'error');
    }
}

async function handleCreateScheme(event) {
    event.preventDefault();
    const payload = {
        name: document.getElementById('scheme-name').value,
        description: document.getElementById('scheme-desc').value,
        minimumMarks: parseFloat(document.getElementById('scheme-min-marks').value),
        incomeLimit: parseFloat(document.getElementById('scheme-income-limit').value),
        status: 'ACTIVE'
    };

    try {
        const res = await fetch('/api/schemes', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });

        if (!res.ok) {
            const errData = await res.json();
            throw new Error(errData.message || 'Failed to create scheme');
        }

        showToast('Scholarship scheme created successfully!', 'success');
        document.getElementById('create-scheme-form').reset();
        loadSchemes();
    } catch (err) {
        showToast(err.message, 'error');
    }
}

// 4. Apply for Scholarship Section
async function populateApplyFormDropdowns() {
    try {
        const [stdRes, schRes] = await Promise.all([
            fetch('/api/students'),
            fetch('/api/schemes')
        ]);

        const students = await stdRes.json();
        const schemes = await schRes.json();

        const stdSelect = document.getElementById('apply-student-select');
        const schSelect = document.getElementById('apply-scheme-select');

        stdSelect.innerHTML = `<option value="">-- Choose Student --</option>` + 
            students.map(s => `<option value="${s.id}">${s.name} (Marks: ${s.marks}%, Income: Rs. ${s.annualIncome.toLocaleString()})</option>`).join('');

        schSelect.innerHTML = `<option value="">-- Choose Scheme --</option>` + 
            schemes.map(sch => `<option value="${sch.id}">${sch.name} (Min Marks: ${sch.minimumMarks}%, Income Limit: Rs. ${sch.incomeLimit.toLocaleString()})</option>`).join('');

    } catch (err) {
        showToast('Error populating application form: ' + err.message, 'error');
    }
}

async function handleApplyScholarship(event) {
    event.preventDefault();
    const studentId = document.getElementById('apply-student-select').value;
    const schemeId = document.getElementById('apply-scheme-select').value;

    if (!studentId || !schemeId) {
        showToast('Please select both a student and a scholarship scheme.', 'error');
        return;
    }

    try {
        const res = await fetch('/api/applications', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ studentId: parseInt(studentId), schemeId: parseInt(schemeId) })
        });

        const app = await res.json();
        const resultCard = document.getElementById('apply-result-card');
        resultCard.style.display = 'block';

        if (app.eligibilityStatus === 'ELIGIBLE') {
            resultCard.className = 'result-banner eligible';
            resultCard.innerHTML = `<strong>ELIGIBILITY CHECK PASSED!</strong><br>Application #${app.id} submitted successfully and is eligible for verification review.`;
            showToast('Application submitted and evaluated as ELIGIBLE!', 'success');
        } else {
            resultCard.className = 'result-banner not-eligible';
            resultCard.innerHTML = `<strong>ELIGIBILITY CRITERIA NOT MET.</strong><br>Application #${app.id} submitted but student does not satisfy minimum marks or income limit rules. Marked as NOT_ELIGIBLE.`;
            showToast('Application submitted but student is NOT ELIGIBLE.', 'error');
        }

    } catch (err) {
        showToast(err.message, 'error');
    }
}

// 5. Verification Queue Section
async function loadVerifications() {
    try {
        const res = await fetch('/api/applications');
        const applications = await res.json();
        const tbody = document.getElementById('verification-table-body');

        if (applications.length === 0) {
            tbody.innerHTML = `<tr><td colspan="6" class="text-center">No applications queued for verification.</td></tr>`;
            return;
        }

        tbody.innerHTML = applications.map(app => `
            <tr>
                <td><strong>#${app.id}</strong></td>
                <td>
                    <strong>${app.student ? app.student.name : 'N/A'}</strong><br>
                    <small>Marks: ${app.student ? app.student.marks : 0}% | Income: Rs.${app.student ? app.student.annualIncome.toLocaleString() : 0}</small>
                </td>
                <td>
                    <strong>${app.scheme ? app.scheme.name : 'N/A'}</strong><br>
                    <small>Min Marks: ${app.scheme ? app.scheme.minimumMarks : 0}% | Limit: Rs.${app.scheme ? app.scheme.incomeLimit.toLocaleString() : 0}</small>
                </td>
                <td><span class="badge ${app.eligibilityStatus === 'ELIGIBLE' ? 'badge-eligible' : 'badge-not-eligible'}">${app.eligibilityStatus}</span></td>
                <td><span class="badge ${getStatusBadgeClass(app.applicationStatus)}">${app.applicationStatus}</span></td>
                <td>
                    ${app.eligibilityStatus === 'ELIGIBLE' ? 
                        `<button class="btn btn-primary btn-sm" onclick="openVerificationModal(${app.id}, '${app.student ? app.student.name : ''}', '${app.scheme ? app.scheme.name : ''}')">Review & Verify</button>` :
                        `<button class="btn btn-secondary btn-sm" disabled title="Ineligible applications cannot undergo verifier review">Ineligible</button>`
                    }
                </td>
            </tr>
        `).join('');

    } catch (err) {
        showToast('Error loading verification queue: ' + err.message, 'error');
    }
}

function openVerificationModal(appId, studentName, schemeName) {
    document.getElementById('verify-app-id').value = appId;
    document.getElementById('verify-modal-info').innerHTML = `<strong>Reviewing Application #${appId}</strong><br>Student: ${studentName} | Scheme: ${schemeName}`;
    document.getElementById('verify-remarks').value = '';
    openModal('verify-modal');
}

async function handleVerifySubmit(event) {
    event.preventDefault();
    const appId = parseInt(document.getElementById('verify-app-id').value);
    const status = document.getElementById('verify-status-select').value;
    const remarks = document.getElementById('verify-remarks').value;

    try {
        const res = await fetch('/api/verifications', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ applicationId: appId, verificationStatus: status, remarks: remarks })
        });

        if (!res.ok) {
            const errData = await res.json();
            throw new Error(errData.message || 'Verification submission failed');
        }

        showToast(`Application #${appId} verification updated to ${status}!`, 'success');
        closeModal('verify-modal');
        loadVerifications();
    } catch (err) {
        showToast(err.message, 'error');
    }
}

// 6. Disbursement Section
async function loadDisbursements() {
    try {
        const res = await fetch('/api/applications');
        const applications = await res.json();
        const tbody = document.getElementById('disbursement-table-body');

        if (applications.length === 0) {
            tbody.innerHTML = `<tr><td colspan="6" class="text-center">No applications available for disbursement.</td></tr>`;
            return;
        }

        tbody.innerHTML = applications.map(app => `
            <tr>
                <td><strong>#${app.id}</strong></td>
                <td>${app.student ? app.student.name : 'N/A'}</td>
                <td>${app.scheme ? app.scheme.name : 'N/A'}</td>
                <td><span class="badge ${getStatusBadgeClass(app.applicationStatus)}">${app.applicationStatus}</span></td>
                <td><span class="badge ${getDisbursementBadgeClass(app.disbursementStatus)}">${app.disbursementStatus}</span></td>
                <td>
                    <select onchange="handleDisbursementUpdate(${app.id}, this.value)" class="form-select-sm">
                        <option value="NOT_STARTED" ${app.disbursementStatus === 'NOT_STARTED' ? 'selected' : ''}>NOT_STARTED</option>
                        <option value="PROCESSING" ${app.disbursementStatus === 'PROCESSING' ? 'selected' : ''}>PROCESSING</option>
                        <option value="COMPLETED" ${app.disbursementStatus === 'COMPLETED' ? 'selected' : ''}>COMPLETED</option>
                    </select>
                </td>
            </tr>
        `).join('');

    } catch (err) {
        showToast('Error loading disbursements: ' + err.message, 'error');
    }
}

async function handleDisbursementUpdate(appId, newStatus) {
    try {
        const res = await fetch(`/api/applications/${appId}/disbursement`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ disbursementStatus: newStatus })
        });

        if (!res.ok) {
            const errData = await res.json();
            throw new Error(errData.message || 'Disbursement update failed');
        }

        showToast(`Disbursement status for Application #${appId} updated to ${newStatus}`, 'success');
        loadDisbursements();
    } catch (err) {
        showToast(err.message, 'error');
        loadDisbursements(); // Refresh table to reset invalid selection
    }
}

// 7. Tracking Section
async function trackApplication() {
    const appId = document.getElementById('track-app-id').value;
    if (!appId) {
        showToast('Please enter a valid Application ID.', 'error');
        return;
    }

    try {
        const res = await fetch(`/api/applications/${appId}/status`);
        if (!res.ok) {
            const errData = await res.json();
            throw new Error(errData.message || 'Application not found');
        }

        const data = await res.json();
        const box = document.getElementById('tracking-result-box');
        box.style.display = 'block';

        box.innerHTML = `
            <div class="card-header">
                <h4>Application #${data.applicationId} Summary</h4>
                <span class="badge ${getStatusBadgeClass(data.applicationStatus)}">${data.applicationStatus}</span>
            </div>
            <div class="tracking-grid">
                <div class="tracking-field">
                    <label>Student Name</label>
                    <p>${data.studentName}</p>
                </div>
                <div class="tracking-field">
                    <label>Scholarship Scheme</label>
                    <p>${data.schemeName}</p>
                </div>
                <div class="tracking-field">
                    <label>Eligibility Status</label>
                    <p><span class="badge ${data.eligibilityStatus === 'ELIGIBLE' ? 'badge-eligible' : 'badge-not-eligible'}">${data.eligibilityStatus}</span></p>
                </div>
                <div class="tracking-field">
                    <label>Verification Status</label>
                    <p><span class="badge ${getStatusBadgeClass(data.verificationStatus)}">${data.verificationStatus}</span></p>
                </div>
                <div class="tracking-field">
                    <label>Disbursement Status</label>
                    <p><span class="badge ${getDisbursementBadgeClass(data.disbursementStatus)}">${data.disbursementStatus}</span></p>
                </div>
                <div class="tracking-field">
                    <label>Verification Remarks</label>
                    <p>${data.verificationRemarks || 'None'}</p>
                </div>
            </div>
        `;

    } catch (err) {
        showToast(err.message, 'error');
        document.getElementById('tracking-result-box').style.display = 'none';
    }
}

// Helpers for Badge Classes
function getStatusBadgeClass(status) {
    switch (status) {
        case 'SUBMITTED': return 'badge-submitted';
        case 'UNDER_REVIEW': return 'badge-under-review';
        case 'APPROVED': return 'badge-approved';
        case 'REJECTED': return 'badge-rejected';
        case 'PENDING': return 'badge-under-review';
        default: return 'badge-submitted';
    }
}

function getDisbursementBadgeClass(status) {
    switch (status) {
        case 'NOT_STARTED': return 'badge-not-started';
        case 'PROCESSING': return 'badge-processing';
        case 'COMPLETED': return 'badge-completed';
        default: return 'badge-not-started';
    }
}
