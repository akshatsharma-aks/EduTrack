const token =
    localStorage.getItem("edutrack_token");

const role =
    localStorage.getItem("edutrack_role");

const name =
    localStorage.getItem("edutrack_name");

if (!token || role !== "TRAINER") {

    window.location.href =
        "/login.html";
}

document.getElementById("userName")
    .textContent = name;

async function loadAssignedBatches() {

    const response =
        await fetch(
            "/api/trainer/batches",
            {
                headers: {
                    "Authorization":
                        `Bearer ${token}`
                }
            }
        );

    if (!response.ok) {

        logout();
        return;
    }

    const batches =
        await response.json();

    const container =
        document.getElementById(
            "trainerBatches"
        );

    container.innerHTML = "";

    if (batches.length === 0) {

        container.innerHTML =
            `<div class="empty-state">
                No batches have been assigned to you yet.
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
                    ${batch.name}
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

async function loadEnrollmentRequests() {

    const response =
        await fetch(
            "/api/trainer/enrollment-requests",
            {
                headers: {
                    "Authorization":
                        `Bearer ${token}`
                }
            }
        );

    if (!response.ok) {

        alert(
            "Unable to load enrollment requests."
        );

        return;
    }

    const requests =
        await response.json();

    const container =
        document.getElementById(
            "enrollmentRequests"
        );

    container.innerHTML = "";

    if (requests.length === 0) {

        container.innerHTML =
            `<div class="empty-state">
                No pending enrollment requests.
             </div>`;

        return;
    }

    requests.forEach(request => {

        const row =
            document.createElement("div");

        row.className = "data-row";

        row.innerHTML = `

            <div class="data-main">

                <strong>
                    ${request.traineeName}
                </strong>

                <span>
                    ${request.traineeEmail}
                </span>

                <span>
                    Batch:
                    ${request.batchName}
                </span>

                <span>
                    Course:
                    ${request.courseName}
                </span>

            </div>

            <div class="row-actions">

                <button
                    class="btn small primary"
                    onclick="approveEnrollment(
                        ${request.enrollmentId}
                    )">

                    Approve

                </button>

                <button
                    class="btn small danger"
                    onclick="rejectEnrollment(
                        ${request.enrollmentId}
                    )">

                    Reject

                </button>

            </div>
        `;

        container.appendChild(row);
    });
}

async function approveEnrollment(
    enrollmentId
) {

    const response =
        await fetch(
            `/api/trainer/enrollment-requests/${enrollmentId}/approve`,
            {
                method: "PUT",

                headers: {
                    "Authorization":
                        `Bearer ${token}`
                }
            }
        );

    if (!response.ok) {

        alert(
            "Unable to approve enrollment."
        );

        return;
    }

    alert(
        "Trainee approved successfully."
    );

    await loadEnrollmentRequests();
}

async function rejectEnrollment(
    enrollmentId
) {

    const response =
        await fetch(
            `/api/trainer/enrollment-requests/${enrollmentId}/reject`,
            {
                method: "PUT",

                headers: {
                    "Authorization":
                        `Bearer ${token}`
                }
            }
        );

    if (!response.ok) {

        alert(
            "Unable to reject enrollment."
        );

        return;
    }

    alert(
        "Enrollment request rejected."
    );

    await loadEnrollmentRequests();
}

loadAssignedBatches();
loadEnrollmentRequests();
