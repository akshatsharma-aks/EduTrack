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


    titleElement.textContent =
        title;

    descriptionElement.textContent =
        description || "";


    /*
     * Show modal immediately.
     */

    modal.classList.add("active");


    /*
     * Show loading state.
     */

    video.removeAttribute("src");

    video.load();


    /*
     * Get JWT from localStorage.
     */

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
         * Request the video with the JWT.
         */

        const response =
            await fetch(
                `/api/trainee/lectures/${lectureId}/video`,
                {
                    method: "GET",

                    headers: {
                        "Authorization":
                            `Bearer ${token}`
                    }
                }
            );


        /*
         * Check authentication /
         * authorization / server errors.
         */

        if (!response.ok) {

            console.error(
                "Video request failed:",
                response.status
            );


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


        /*
         * Convert server response
         * into a browser Blob.
         */

        const videoBlob =
            await response.blob();


        /*
         * Create temporary URL.
         */

        const videoUrl =
            URL.createObjectURL(
                videoBlob
            );


        /*
         * Remove previous object URL
         * if one exists.
         */

        if (video.dataset.objectUrl) {

            URL.revokeObjectURL(
                video.dataset.objectUrl
            );
        }


        video.dataset.objectUrl =
            videoUrl;


        /*
         * Give Blob URL to HTML5 video.
         */

        video.src =
            videoUrl;


        video.load();


        /*
         * Start playback after
         * browser has loaded enough data.
         */

        video.play().catch(
            error => {

                /*
                 * Autoplay can be blocked
                 * by the browser.
                 *
                 * That's okay.
                 * User can press Play.
                 */

                console.log(
                    "Autoplay prevented:",
                    error
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


function closeLecture() {

    const video =
        document.getElementById(
            "lectureVideo"
        );


    /*
     * Stop playback.
     */

    video.pause();


    /*
     * Revoke temporary Blob URL.
     */

    if (video.dataset.objectUrl) {

        URL.revokeObjectURL(
            video.dataset.objectUrl
        );

        delete video.dataset.objectUrl;
    }


    /*
     * Remove video source.
     */

    video.removeAttribute(
        "src"
    );

    video.load();


    /*
     * Hide modal.
     */

    document.getElementById(
        "lectureModal"
    ).classList.remove("active");
}