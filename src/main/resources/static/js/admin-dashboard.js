const token =
    localStorage.getItem("edutrack_token");

const role =
    localStorage.getItem("edutrack_role");

const name =
    localStorage.getItem("edutrack_name");

if (!token || role !== "ADMIN") {

    window.location.href = "/login.html";
}

document.getElementById("userName")
    .textContent = name;

function authHeaders() {

    return {
        "Content-Type": "application/json",
        "Authorization": `Bearer ${token}`
    };
}

async function loadDashboard() {

    const response =
        await fetch(
            "/api/admin/dashboard",
            {
                headers: authHeaders()
            }
        );

    if (!response.ok) {

        logout();
        return;
    }

    const data = await response.json();

    document.getElementById("totalCourses")
        .textContent = data.totalCourses;

    document.getElementById("activeBatches")
        .textContent = data.activeBatches;

    document.getElementById("trainers")
        .textContent = data.trainers;

    document.getElementById("trainees")
        .textContent = data.trainees;
}

async function loadTrainerRequests() {

    const response =
        await fetch(
            "/api/admin/trainers",
            {
                headers: authHeaders()
            }
        );

    const trainers =
        await response.json();

    const container =
        document.getElementById(
            "trainerRequests"
        );

    container.innerHTML = "";

    if (trainers.length === 0) {

        container.innerHTML =
            `<div class="empty-state">
                No trainer registrations yet.
             </div>`;

        return;
    }

    trainers.forEach(trainer => {

        const item =
            document.createElement("div");

        item.className = "data-row";

        item.innerHTML = `

            <div class="data-main">

                <strong>
                    ${trainer.name}
                </strong>

                <span>
                    ${trainer.email}
                </span>

            </div>

            <span class="status ${trainer.status.toLowerCase()}">
                ${trainer.status}
            </span>

            <div class="row-actions">

                ${
            trainer.status === "PENDING"

                ? `
                        <button
                            class="btn small primary"
                            onclick="approveTrainer(${trainer.id})">
                            Approve
                        </button>

                        <button
                            class="btn small danger"
                            onclick="declineTrainer(${trainer.id})">
                            Decline
                        </button>
                      `

                : ""
        }

            </div>
        `;

        container.appendChild(item);
    });
}

async function approveTrainer(id) {

    const response =
        await fetch(
            `/api/admin/trainers/${id}/approve`,
            {
                method: "PUT",
                headers: authHeaders()
            }
        );

    if (!response.ok) {

        alert("Unable to approve trainer.");
        return;
    }

    await loadTrainerRequests();
    await loadDashboard();
}

async function declineTrainer(id) {

    const response =
        await fetch(
            `/api/admin/trainers/${id}/decline`,
            {
                method: "PUT",
                headers: authHeaders()
            }
        );

    if (!response.ok) {

        alert("Unable to decline trainer.");
        return;
    }

    await loadTrainerRequests();
    await loadDashboard();
}

async function loadCourses() {

    const response =
        await fetch(
            "/api/admin/courses",
            {
                headers: authHeaders()
            }
        );

    const courses =
        await response.json();

    const container =
        document.getElementById(
            "courseList"
        );

    container.innerHTML = "";

    if (courses.length === 0) {

        container.innerHTML =
            `<div class="empty-state">
                No courses created yet.
             </div>`;

        return;
    }

    courses.forEach(course => {

        const item =
            document.createElement("div");

        item.className = "data-row";

        item.innerHTML = `

            <div class="data-main">

                <strong>
                    ${course.name}
                </strong>

                <span>
                    ${course.description}
                </span>

            </div>

            <span class="status active">
                ${course.status}
            </span>
        `;

        container.appendChild(item);
    });

    const select =
        document.getElementById(
            "batchCourse"
        );

    select.innerHTML =
        `<option value="">
            Select course
         </option>`;

    courses.forEach(course => {

        if (course.status === "ACTIVE") {

            select.innerHTML += `
                <option value="${course.id}">
                    ${course.name}
                </option>
            `;
        }
    });
}

async function loadTrainersForBatch() {

    const response =
        await fetch(
            "/api/admin/trainers",
            {
                headers: authHeaders()
            }
        );

    const trainers =
        await response.json();

    const select =
        document.getElementById(
            "batchTrainer"
        );

    select.innerHTML =
        `<option value="">
            Select trainer
         </option>`;

    trainers
        .filter(
            trainer =>
                trainer.status === "APPROVED"
        )
        .forEach(trainer => {

            select.innerHTML += `
                <option value="${trainer.id}">
                    ${trainer.name}
                </option>
            `;
        });
}

async function loadBatches() {

    const response =
        await fetch(
            "/api/admin/batches",
            {
                headers: authHeaders()
            }
        );

    const batches =
        await response.json();

    const container =
        document.getElementById(
            "batchList"
        );

    container.innerHTML = "";

    if (batches.length === 0) {

        container.innerHTML =
            `<div class="empty-state">
                No batches created yet.
             </div>`;

        return;
    }

    batches.forEach(batch => {

        const trainerName =
            batch.trainerName || "Not allocated";

        const item =
            document.createElement("div");

        item.className = "data-row";

        item.innerHTML = `

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

            <span>
                Trainer:
                <strong>
                    ${trainerName}
                </strong>
            </span>
        `;

        container.appendChild(item);
    });
}

document
    .getElementById("courseForm")
    .addEventListener(
        "submit",
        async function(event) {

            event.preventDefault();

            const response =
                await fetch(
                    "/api/admin/courses",
                    {
                        method: "POST",
                        headers: authHeaders(),

                        body: JSON.stringify({

                            name:
                            document
                                .getElementById(
                                    "courseName"
                                )
                                .value,

                            description:
                            document
                                .getElementById(
                                    "courseDescription"
                                )
                                .value,

                            status:
                            document
                                .getElementById(
                                    "courseStatus"
                                )
                                .value
                        })
                    }
                );

            if (!response.ok) {

                alert("Unable to create course.");
                return;
            }

            closeCourseModal();

            document
                .getElementById("courseForm")
                .reset();

            await loadCourses();
            await loadDashboard();
        }
    );

document
    .getElementById("batchForm")
    .addEventListener(
        "submit",
        async function(event) {

            event.preventDefault();

            const trainerValue =
                document
                    .getElementById(
                        "batchTrainer"
                    )
                    .value;

            const response =
                await fetch(
                    "/api/admin/batches",
                    {
                        method: "POST",
                        headers: authHeaders(),

                        body: JSON.stringify({

                            name:
                            document
                                .getElementById(
                                    "batchName"
                                )
                                .value,

                            courseId:
                                Number(
                                    document
                                        .getElementById(
                                            "batchCourse"
                                        )
                                        .value
                                ),

                            startDate:
                            document
                                .getElementById(
                                    "batchStartDate"
                                )
                                .value,

                            endDate:
                            document
                                .getElementById(
                                    "batchEndDate"
                                )
                                .value,

                            trainerId:
                                trainerValue
                                    ? Number(trainerValue)
                                    : null
                        })
                    }
                );

            if (!response.ok) {

                const error =
                    await response.text();

                alert(error);
                return;
            }

            closeBatchModal();

            document
                .getElementById("batchForm")
                .reset();

            await loadBatches();
            await loadDashboard();
        }
    );

function openCourseModal() {

    document
        .getElementById("courseModal")
        .classList.add("show");
}

function closeCourseModal() {

    document
        .getElementById("courseModal")
        .classList.remove("show");
}

async function openBatchModal() {

    await loadCourses();
    await loadTrainersForBatch();

    document
        .getElementById("batchModal")
        .classList.add("show");
}

function closeBatchModal() {

    document
        .getElementById("batchModal")
        .classList.remove("show");
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

loadDashboard();
loadTrainerRequests();
loadCourses();
loadBatches();