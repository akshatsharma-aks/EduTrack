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


/* =========================
   AVAILABLE BATCHES
========================= */

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


/* =========================
   REQUEST TO JOIN
========================= */

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


/* =========================
   GET MY ENROLLMENTS
========================= */

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


/* =========================
   MY ENROLLMENTS
========================= */

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


/* =========================
   LOAD LECTURES
========================= */

async function loadLectures(batchId) {

    console.log(
        "Loading lectures for batch:",
        batchId
    );

    const response =
        await fetch(
            `/api/trainee/batches/${batchId}/lectures`,
            {
                headers: authHeaders()
            }
        );

    console.log(
        "Lecture API status:",
        response.status
    );


    if (!response.ok) {

        const errorText =
            await response.text();

        console.error(
            "Lecture API failed:",
            errorText
        );

        return [];
    }


    const lectures =
        await response.json();


    console.log(
        "Lectures returned:",
        lectures
    );


    return lectures;
}

/* =========================
   MY APPROVED BATCHES
========================= */

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

    /*
     * We use a normal for...of loop here
     * because we need await for each
     * batch's lecture request.
     */

    for (const batch of batches) {

        const row =
            document.createElement("div");

        row.className = "data-row";

        const lectures =
            await loadLectures(
                batch.batchId
            );

        let lectureHtml = "";

        if (lectures.length === 0) {

            lectureHtml = `
                <div class="empty-state">
                    No lectures uploaded yet.
                </div>
            `;

        } else {

            lectureHtml =
                lectures.map(
                    lecture => `

                    <div class="lecture-item">

                        <div>

                            <strong>
                                ${escapeHtml(
                        lecture.title
                    )}
                            </strong>

                            <p>
                                ${escapeHtml(
                        lecture.description || ""
                    )}
                            </p>

                        </div>

                        <button
                            class="btn small primary"
                            onclick="openLecture(
                                ${lecture.id},
                                '${escapeForAttribute(
                        lecture.title
                    )}',
                                '${escapeForAttribute(
                        lecture.description || ""
                    )}'
                            )">

                            Watch Lecture

                        </button>

                    </div>

                `
                ).join("");
        }

        row.innerHTML = `

            <div class="data-main">

                <strong>
                    ${escapeHtml(
            batch.batchName
        )}
                </strong>

                <span>
                    ${escapeHtml(
            batch.courseName
        )}
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

            <div class="lecture-section">

                <h3>
                    Lectures
                </h3>

                ${lectureHtml}

            </div>
        `;

        container.appendChild(row);
    }
}


/* =========================
   SAFE HTML HELPERS
========================= */

function escapeHtml(value) {

    if (value === null ||
        value === undefined) {

        return "";
    }

    return String(value)
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;")
        .replace(/'/g, "&#039;");
}


function escapeForAttribute(value) {

    if (value === null ||
        value === undefined) {

        return "";
    }

    return String(value)
        .replace(/\\/g, "\\\\")
        .replace(/'/g, "\\'")
        .replace(/\r?\n/g, "\\n");
}


/* =========================
   LOGOUT
========================= */

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


/* =========================
   INITIALIZE
========================= */

async function initialize() {

    await loadAvailableBatches();

    await loadMyEnrollments();

    await loadMyBatches();
}

initialize();