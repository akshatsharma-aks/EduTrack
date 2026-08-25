const token =
    localStorage.getItem("edutrack_token");

const role =
    localStorage.getItem("edutrack_role");

const name =
    localStorage.getItem("edutrack_name");

if (!token || role !== "TRAINEE") {

    window.location.href =
        "/login.html";
}

document.getElementById("userName")
    .textContent = name;

function authHeaders() {

    return {
        "Content-Type": "application/json",
        "Authorization":
            `Bearer ${token}`
    };
}

async function loadAvailableBatches() {

    const response =
        await fetch(
            "/api/trainee/batches",
            {
                headers: authHeaders()
            }
        );

    if (!response.ok) {

        alert("Unable to load batches.");
        return;
    }

    const batches =
        await response.json();

    const enrollments =
        await getMyEnrollments();

    const enrollmentMap =
        new Map();

    enrollments.forEach(enrollment => {

        enrollmentMap.set(
            enrollment.batchId,
            enrollment.status
        );
    });

    const container =
        document.getElementById(
            "availableBatches"
        );

    container.innerHTML = "";

    if (batches.length === 0) {

        container.innerHTML =
            `<div class="empty-state">
                No batches are currently available.
             </div>`;

        return;
    }

    batches.forEach(batch => {

        const status =
            enrollmentMap.get(batch.id);

        const row =
            document.createElement("div");

        row.className = "data-row";

        let action = "";

        if (status === "PENDING") {

            action = `
                <span class="status pending">
                    PENDING
                </span>
            `;

        } else if (status === "APPROVED") {

            action = `
                <span class="status approved">
                    APPROVED
                </span>
            `;

        } else if (status === "REJECTED") {

            action = `
                <span class="status declined">
                    REJECTED
                </span>
            `;

        } else {

            action = `
                <button
                    class="btn small primary"
                    onclick="requestToJoin(${batch.id})">
                    Request to Join
                </button>
            `;
        }

        row.innerHTML = `

            <div class="data-main">

                <strong>
                    ${batch.name}
                </strong>

                <span>
                    ${batch.courseName}
                </span>

                <span>
                    Trainer:
                    ${batch.trainerName}
                </span>

            </div>

            <div class="batch-dates">

                ${batch.startDate}
                →
                ${batch.endDate}

            </div>

            <div class="row-actions">

                ${action}

            </div>
        `;

        container.appendChild(row);
    });
}

async function requestToJoin(batchId) {

    const response =
        await fetch(
            `/api/trainee/batches/${batchId}/request`,
            {
                method: "POST",
                headers: authHeaders()
            }
        );

    const text =
        await response.text();

    if (!response.ok) {

        alert(
            text ||
            "Unable to submit enrollment request."
        );

        return;
    }

    alert(
        "Enrollment request submitted successfully."
    );

    await loadAvailableBatches();
    await loadMyEnrollments();
    await loadMyBatches();
}

async function getMyEnrollments() {

    const response =
        await fetch(
            "/api/trainee/enrollments",
            {
                headers: authHeaders()
            }
        );

    if (!response.ok) {

        return [];
    }

    return response.json();
}

async function loadMyEnrollments() {

    const enrollments =
        await getMyEnrollments();

    const container =
        document.getElementById(
            "myEnrollments"
        );

    container.innerHTML = "";

    if (enrollments.length === 0) {

        container.innerHTML =
            `<div class="empty-state">
                You have not requested to join any batch yet.
             </div>`;

        return;
    }

    enrollments.forEach(enrollment => {

        const row =
            document.createElement("div");

        row.className = "data-row";

        row.innerHTML = `

            <div class="data-main">

                <strong>
                    ${enrollment.batchName}
                </strong>

                <span>
                    ${enrollment.courseName}
                </span>

            </div>

            <div class="batch-dates">

                ${enrollment.startDate}
                →
                ${enrollment.endDate}

            </div>

            <span class="status
                ${enrollment.status.toLowerCase()}">

                ${enrollment.status}

            </span>
        `;

        container.appendChild(row);
    });
}

async function loadMyBatches() {

    const response =
        await fetch(
            "/api/trainee/my-batches",
            {
                headers: authHeaders()
            }
        );

    if (!response.ok) {

        return;
    }

    const batches =
        await response.json();

    const container =
        document.getElementById(
            "myBatches"
        );

    container.innerHTML = "";

    if (batches.length === 0) {

        container.innerHTML =
            `<div class="empty-state">
                No approved batches yet.
             </div>`;

        return;
    }

    batches.forEach(batch => {

        const row =
            document.createElement("div");

        row.className = "data-row";

        row.innerHTML = `

            <div class="data-main">

                <strong>
                    ${batch.batchName}
                </strong>

                <span>
                    ${batch.courseName}
                </span>

            </div>

            <div class="batch-dates">

                ${batch.startDate}
                →
                ${batch.endDate}

            </div>

            <span class="status approved">
                APPROVED
            </span>
        `;

        container.appendChild(row);
    });
}

function logout() {

    localStorage.removeItem(
        "edutrack_token"
    );

    localStorage.removeItem(
        "edutrack_name"
    );

    localStorage.removeItem(
        "edutrack_role"
    );

    window.location.href =
        "/login.html";
}

async function initialize() {

    await loadAvailableBatches();
    await loadMyEnrollments();
    await loadMyBatches();
}

initialize();