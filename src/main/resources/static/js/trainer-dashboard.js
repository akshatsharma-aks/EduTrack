const token =
    localStorage.getItem("edutrack_token");

const role =
    localStorage.getItem("edutrack_role");

const name =
    localStorage.getItem("edutrack_name");


/* =========================
   AUTHENTICATION CHECK
========================= */

if (!token || role !== "TRAINER") {

    window.location.href =
        "/login.html";
}


document.getElementById("userName")
    .textContent = name;


/* =========================
   LOAD ASSIGNED BATCHES
========================= */

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


    /*
     * for...of is used because
     * lectures are loaded asynchronously.
     */

    for (const batch of batches) {

        const row =
            document.createElement("div");


        row.className =
            "data-row trainer-batch-card";


        /* =========================
           LOAD LECTURES
        ========================= */

        const lectures =
            await loadTrainerLectures(
                batch.id
            );


        let lectureHtml = "";


        if (lectures.length === 0) {

            lectureHtml = `
                <div class="empty-state lecture-empty">

                    No lectures uploaded yet.

                </div>
            `;

        } else {

            lectureHtml =
                lectures.map(
                    lecture => `

                    <div class="lecture-item">

                        <div class="lecture-info">

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


                        <div class="lecture-actions">

                            <!-- VIEW PROGRESS -->

                            <button
                                class="btn small primary"
                                onclick="viewLectureProgress(
                                    ${lecture.id},
                                    '${escapeForAttribute(
                        lecture.title
                    )}'
                                )">

                                View Progress

                            </button>


                            <!-- DELETE -->

                            <button
                                class="btn small danger"
                                onclick="deleteLecture(
                                    ${lecture.id}
                                )">

                                Delete

                            </button>

                        </div>

                    </div>

                `
                ).join("");
        }


        /* =========================
           BATCH CARD
        ========================= */

        row.innerHTML = `

            <div class="batch-card-header">

                <div class="data-main">

                    <strong>
                        ${escapeHtml(
            batch.name
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

            </div>


            <div class="lecture-management">

                <div class="lecture-heading">

                    <div>

                        <h3>
                            Lectures
                        </h3>

                        <p>
                            Manage lectures and
                            monitor trainee progress.
                        </p>

                    </div>


                    <button
                        class="btn small primary"
                        onclick="openLectureUploadModal(
                            ${batch.id},
                            '${escapeForAttribute(
            batch.name
        )}'
                        )">

                        + Upload Lecture

                    </button>

                </div>


                <div class="lecture-list">

                    ${lectureHtml}

                </div>

            </div>
        `;


        container.appendChild(row);
    }
}


/* =========================
   LOAD TRAINER LECTURES
========================= */

async function loadTrainerLectures(
    batchId
) {

    const response =
        await fetch(
            `/api/trainer/batches/${batchId}/lectures`,
            {
                headers: {
                    "Authorization":
                        `Bearer ${token}`
                }
            }
        );


    if (!response.ok) {

        return [];
    }


    return response.json();
}


/* =========================
   OPEN UPLOAD MODAL
========================= */

function openLectureUploadModal(
    batchId,
    batchName
) {

    console.log(
        "Opening lecture upload modal:",
        batchId,
        batchName
    );


    const modal =
        document.getElementById(
            "lectureUploadModal"
        );


    const batchIdInput =
        document.getElementById(
            "uploadBatchId"
        );


    const batchNameElement =
        document.getElementById(
            "uploadBatchName"
        );


    if (!modal) {

        console.error(
            "lectureUploadModal element not found"
        );

        return;
    }


    if (!batchIdInput) {

        console.error(
            "uploadBatchId element not found"
        );

        return;
    }


    if (!batchNameElement) {

        console.error(
            "uploadBatchName element not found"
        );

        return;
    }


    batchIdInput.value =
        batchId;


    batchNameElement.textContent =
        `Batch: ${batchName}`;


    modal.classList.add(
        "active"
    );
}


/* =========================
   CLOSE UPLOAD MODAL
========================= */

function closeLectureUploadModal() {

    const modal =
        document.getElementById(
            "lectureUploadModal"
        );


    if (modal) {

        modal.classList.remove(
            "active"
        );
    }


    const form =
        document.getElementById(
            "lectureUploadForm"
        );


    if (form) {

        form.reset();
    }


    const batchId =
        document.getElementById(
            "uploadBatchId"
        );


    if (batchId) {

        batchId.value = "";
    }
}


/* =========================
   UPLOAD LECTURE
========================= */

document.getElementById(
    "lectureUploadForm"
).addEventListener(
    "submit",
    async function (event) {

        event.preventDefault();


        const batchId =
            document.getElementById(
                "uploadBatchId"
            ).value;


        const form =
            document.getElementById(
                "lectureUploadForm"
            );


        const file =
            document.getElementById(
                "lectureFile"
            ).files[0];


        /* =========================
           FILE VALIDATION
        ========================= */

        if (!file) {

            alert(
                "Please select an MP4 video."
            );

            return;
        }


        if (file.type !== "video/mp4") {

            alert(
                "Only MP4 video files are allowed."
            );

            return;
        }


        if (
            file.size >
            100 * 1024 * 1024
        ) {

            alert(
                "Video must not exceed 100 MB."
            );

            return;
        }


        const formData =
            new FormData(form);


        const button =
            document.getElementById(
                "uploadLectureButton"
            );


        const originalText =
            button.textContent;


        button.disabled = true;

        button.textContent =
            "Uploading...";


        try {

            const response =
                await fetch(
                    `/api/trainer/batches/${batchId}/lectures`,
                    {
                        method: "POST",

                        headers: {
                            "Authorization":
                                `Bearer ${token}`
                        },

                        /*
                         * Do NOT set Content-Type.
                         *
                         * Browser automatically
                         * creates multipart/form-data.
                         */

                        body: formData
                    }
                );


            const text =
                await response.text();


            if (!response.ok) {

                alert(
                    text ||
                    "Lecture upload failed."
                );

                return;
            }


            alert(
                "Lecture uploaded successfully."
            );


            closeLectureUploadModal();


            await loadAssignedBatches();


        } catch (error) {

            console.error(
                "Lecture upload error:",
                error
            );


            alert(
                "An error occurred while uploading the lecture."
            );


        } finally {

            button.disabled = false;

            button.textContent =
                originalText;
        }
    }
);


/* =========================
   DELETE LECTURE
========================= */

async function deleteLecture(
    lectureId
) {

    const confirmed =
        confirm(
            "Are you sure you want to delete this lecture?"
        );


    if (!confirmed) {

        return;
    }


    const response =
        await fetch(
            `/api/trainer/lectures/${lectureId}`,
            {
                method: "DELETE",

                headers: {
                    "Authorization":
                        `Bearer ${token}`
                }
            }
        );


    if (!response.ok) {

        const text =
            await response.text();


        alert(
            text ||
            "Unable to delete lecture."
        );

        return;
    }


    alert(
        "Lecture deleted successfully."
    );


    await loadAssignedBatches();
}


/* =========================
   VIEW LECTURE PROGRESS
========================= */

async function viewLectureProgress(
    lectureId,
    lectureTitle
) {

    const modal =
        document.getElementById(
            "progressModal"
        );


    const titleElement =
        document.getElementById(
            "progressLectureTitle"
        );


    const container =
        document.getElementById(
            "progressReport"
        );


    if (!modal || !titleElement || !container) {

        console.error(
            "Progress modal elements not found."
        );

        return;
    }


    titleElement.textContent =
        lectureTitle;


    /*
     * Show modal immediately.
     */

    modal.classList.add(
        "active"
    );


    /*
     * Loading state.
     */

    container.innerHTML = `
        <div class="empty-state">
            Loading trainee progress...
        </div>
    `;


    try {

        const response =
            await fetch(
                `/api/trainer/lectures/${lectureId}/progress`,
                {
                    headers: {
                        "Authorization":
                            `Bearer ${token}`
                    }
                }
            );


        const text =
            await response.text();


        if (!response.ok) {

            console.error(
                "Progress API failed:",
                response.status,
                text
            );


            container.innerHTML = `
                <div class="empty-state">

                    Unable to load trainee progress.

                </div>
            `;

            return;
        }


        const progress =
            text
                ? JSON.parse(text)
                : [];


        if (!Array.isArray(progress)
            || progress.length === 0) {

            container.innerHTML = `
                <div class="empty-state">

                    No trainee progress recorded yet.

                </div>
            `;

            return;
        }


        container.innerHTML = `

            <div class="progress-table-wrapper">

                <table class="progress-table">

                    <thead>

                        <tr>

                            <th>
                                Student
                            </th>

                            <th>
                                Email
                            </th>

                            <th>
                                Progress
                            </th>

                            <th>
                                Status
                            </th>

                        </tr>

                    </thead>


                    <tbody>

                        ${progress.map(
            student => {

                const percentage =
                    Math.round(
                        student.percentageWatched || 0
                    );


                let statusClass =
                    "not-started";


                if (
                    student.completed
                ) {

                    statusClass =
                        "completed";

                } else if (
                    percentage > 0
                ) {

                    statusClass =
                        "in-progress";
                }


                return `

                                    <tr>

                                        <td>
                                            <strong>
                                                ${escapeHtml(
                    student.traineeName
                )}
                                            </strong>
                                        </td>


                                        <td>
                                            ${escapeHtml(
                    student.traineeEmail
                )}
                                        </td>


                                        <td>

                                            <div class="report-progress">

                                                <div
                                                    class="progress-bar">

                                                    <div
                                                        class="progress-fill"
                                                        style="width: ${percentage}%">
                                                    </div>

                                                </div>

                                                <span>
                                                    ${percentage}%
                                                </span>

                                            </div>

                                        </td>


                                        <td>

                                            <span
                                                class="progress-status ${statusClass}">

                                                ${
                    student.completed
                        ? "Completed"
                        : percentage > 0
                            ? "In Progress"
                            : "Not Started"
                }

                                            </span>

                                        </td>

                                    </tr>
                                `;
            }
        ).join("")}

                    </tbody>

                </table>

            </div>
        `;


    } catch (error) {

        console.error(
            "Progress loading error:",
            error
        );


        container.innerHTML = `
            <div class="empty-state">

                Unable to load trainee progress.

            </div>
        `;
    }
}


/* =========================
   CLOSE PROGRESS MODAL
========================= */

function closeProgressModal() {

    const modal =
        document.getElementById(
            "progressModal"
        );


    if (modal) {

        modal.classList.remove(
            "active"
        );
    }
}


/* =========================
   ENROLLMENT REQUESTS
========================= */

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


        row.className =
            "data-row";


        row.innerHTML = `

            <div class="data-main">

                <strong>
                    ${escapeHtml(
            request.traineeName
        )}
                </strong>

                <span>
                    ${escapeHtml(
            request.traineeEmail
        )}
                </span>

                <span>
                    Batch:
                    ${escapeHtml(
            request.batchName
        )}
                </span>

                <span>
                    Course:
                    ${escapeHtml(
            request.courseName
        )}
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


/* =========================
   APPROVE ENROLLMENT
========================= */

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


/* =========================
   REJECT ENROLLMENT
========================= */

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
   HTML SAFETY HELPERS
========================= */

function escapeHtml(value) {

    if (
        value === null ||
        value === undefined
    ) {

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

    if (
        value === null ||
        value === undefined
    ) {

        return "";
    }


    return String(value)
        .replace(/\\/g, "\\\\")
        .replace(/'/g, "\\'")
        .replace(/\r?\n/g, "\\n");
}


/* =========================
   INITIALIZE DASHBOARD
========================= */

loadAssignedBatches();

loadEnrollmentRequests();