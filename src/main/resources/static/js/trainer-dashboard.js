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
     * Use for...of instead of forEach
     * because we need await while loading
     * lectures for each batch.
     */

    for (const batch of batches) {

        const row =
            document.createElement("div");

        row.className =
            "data-row trainer-batch-card";


        /* =========================
           LOAD LECTURES FOR BATCH
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
                            class="btn small danger"
                            onclick="deleteLecture(
                                ${lecture.id}
                            )">

                            Delete

                        </button>

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
                            Manage lectures for this batch.
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


    modal.classList.add("active");


    console.log(
        "Lecture upload modal opened"
    );
}



/* =========================
   CLOSE UPLOAD MODAL
========================= */

function closeLectureUploadModal() {

    document.getElementById(
        "lectureUploadModal"
    ).classList.remove("active");


    document.getElementById(
        "lectureUploadForm"
    ).reset();


    document.getElementById(
        "uploadBatchId"
    ).value = "";
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
                         * IMPORTANT:
                         * Do NOT set Content-Type here.
                         *
                         * Browser automatically creates:
                         * multipart/form-data
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


            /*
             * Reload batches so the newly
             * uploaded lecture immediately
             * appears.
             */

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