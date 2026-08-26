let currentLectureId = null;

let progressSaveTimer = null;

let currentObjectUrl = null;


/* =========================
   OPEN LECTURE
========================= */

async function openLecture(
    lectureId,
    title,
    description
) {

    const modal =
        document.getElementById(
            "lectureModal"
        );

    const titleElement =
        document.getElementById(
            "lectureTitle"
        );

    const descriptionElement =
        document.getElementById(
            "lectureDescription"
        );

    const video =
        document.getElementById(
            "lectureVideo"
        );


    currentLectureId =
        lectureId;


    titleElement.textContent =
        title;


    descriptionElement.textContent =
        description || "";


    modal.classList.add(
        "active"
    );


    video.removeAttribute("src");

    video.load();


    const token =
        localStorage.getItem(
            "edutrack_token"
        );


    if (!token) {

        alert(
            "Your session has expired. Please login again."
        );

        window.location.href =
            "/login.html";

        return;
    }


    try {

        /*
         * Load previous progress
         */

        const progressResponse =
            await fetch(
                `/api/trainee/lectures/${lectureId}/progress`,
                {
                    headers: {
                        "Authorization":
                            `Bearer ${token}`
                    }
                }
            );


        let savedPosition = 0;


        if (progressResponse.ok) {

            const progress =
                await progressResponse.json();


            savedPosition =
                progress.currentPosition || 0;
        }


        /*
         * Load authenticated video
         */

        const response =
            await fetch(
                `/api/trainee/lectures/${lectureId}/video`,
                {
                    headers: {
                        "Authorization":
                            `Bearer ${token}`
                    }
                }
            );


        if (!response.ok) {

            if (
                response.status === 401 ||
                response.status === 403
            ) {

                alert(
                    "You are not authorized to watch this lecture."
                );

            } else {

                alert(
                    "Unable to load the lecture video."
                );
            }

            return;
        }


        const videoBlob =
            await response.blob();


        currentObjectUrl =
            URL.createObjectURL(
                videoBlob
            );


        video.src =
            currentObjectUrl;


        video.load();


        /*
         * Restore saved position
         * after metadata is available.
         */

        video.addEventListener(
            "loadedmetadata",
            function restorePosition() {

                if (
                    savedPosition > 0 &&
                    savedPosition < video.duration
                ) {

                    video.currentTime =
                        savedPosition;
                }


                video.removeEventListener(
                    "loadedmetadata",
                    restorePosition
                );
            }
        );


    } catch (error) {

        console.error(
            "Video loading error:",
            error
        );


        alert(
            "An error occurred while loading the lecture video."
        );
    }
}


/* =========================
   VIDEO TIME UPDATE
========================= */

document.addEventListener(
    "DOMContentLoaded",
    function () {

        const video =
            document.getElementById(
                "lectureVideo"
            );


        if (!video) {

            return;
        }


        video.addEventListener(
            "timeupdate",
            function () {

                if (
                    !video.duration ||
                    !currentLectureId
                ) {

                    return;
                }


                /*
                 * Save approximately every
                 * 5 seconds.
                 */

                if (!progressSaveTimer) {

                    progressSaveTimer =
                        setTimeout(
                            function () {

                                saveVideoProgress();

                                progressSaveTimer =
                                    null;

                            },
                            5000
                        );
                }
            }
        );


        video.addEventListener(
            "pause",
            function () {

                saveVideoProgress();
            }
        );


        video.addEventListener(
            "ended",
            function () {

                saveVideoProgress();
            }
        );
    }
);


/* =========================
   SAVE VIDEO PROGRESS
========================= */

async function saveVideoProgress() {

    const video =
        document.getElementById(
            "lectureVideo"
        );


    if (
        !video ||
        !currentLectureId ||
        !video.duration ||
        video.duration <= 0
    ) {

        return;
    }


    const token =
        localStorage.getItem(
            "edutrack_token"
        );


    if (!token) {

        return;
    }


    try {

        await fetch(
            `/api/trainee/lectures/${currentLectureId}/progress`,
            {
                method: "PUT",

                headers: {
                    "Content-Type":
                        "application/json",

                    "Authorization":
                        `Bearer ${token}`
                },

                body: JSON.stringify({

                    currentPosition:
                    video.currentTime,

                    duration:
                    video.duration
                })
            }
        );


    } catch (error) {

        console.error(
            "Unable to save video progress:",
            error
        );
    }
}


/* =========================
   CLOSE LECTURE
========================= */

function closeLecture() {

    /*
     * Save one final progress value
     * before closing.
     */

    saveVideoProgress();


    const video =
        document.getElementById(
            "lectureVideo"
        );


    video.pause();


    if (progressSaveTimer) {

        clearTimeout(
            progressSaveTimer
        );

        progressSaveTimer = null;
    }


    if (currentObjectUrl) {

        URL.revokeObjectURL(
            currentObjectUrl
        );

        currentObjectUrl = null;
    }


    video.removeAttribute(
        "src"
    );

    video.load();


    document.getElementById(
        "lectureModal"
    ).classList.remove(
        "active"
    );


    currentLectureId =
        null;
}