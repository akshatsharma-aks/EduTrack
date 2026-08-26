const token =
    localStorage.getItem("edutrack_token");

const role =
    localStorage.getItem("edutrack_role");


/* =========================
   AUTHENTICATION
========================= */

if (!token || role !== "TRAINEE") {

    window.location.href =
        "/login.html";
}


/* =========================
   LOAD RESULT
========================= */

function loadResult() {

    const resultCard =
        document.getElementById(
            "resultCard"
        );


    const stored =
        sessionStorage.getItem(
            "edutrack_last_quiz_result"
        );


    if (!stored) {

        resultCard.innerHTML = `
            <div class="empty-state">

                Quiz result is not available.

            </div>
        `;

        return;
    }


    let result;


    try {

        result =
            JSON.parse(stored);

    } catch (error) {

        console.error(
            "Invalid stored result:",
            error
        );


        resultCard.innerHTML = `
            <div class="empty-state">

                Unable to display quiz result.

            </div>
        `;

        return;
    }


    const percentage =
        Number(
            result.percentage
            || 0);


    document.getElementById(
        "resultTitle"
    ).textContent =
        result.quizTitle
        || "Quiz Completed";


    resultCard.innerHTML = `

        <div
            class="data-row"
            style="
                display:block;
                text-align:center;
            ">

            <h2>

                ${escapeHtml(
        result.quizTitle
        || "Quiz"
    )}

            </h2>


            <div
                style="
                    font-size:48px;
                    font-weight:700;
                    margin:25px 0;
                ">

                ${percentage.toFixed(2)}%

            </div>


            <p
                class="admin-subtitle">

                Score:
                <strong>

                    ${result.score}

                    /

                    ${result.totalQuestions}

                </strong>

            </p>


            <p>

                Attempt #${result.attemptNumber}

            </p>


            <span
                class="
                    status
                    ${
        percentage >= 50
            ? "approved"
            : "declined"
    }
                ">

                ${
        percentage >= 50
            ? "PASSED"
            : "NEEDS IMPROVEMENT"
    }

            </span>

        </div>

    `;
}


/* =========================
   NAVIGATION
========================= */

function goToQuizzes() {

    sessionStorage.removeItem(
        "edutrack_last_quiz_result"
    );


    window.location.href =
        "/quizzes.html";
}


function goToDashboard() {

    sessionStorage.removeItem(
        "edutrack_last_quiz_result"
    );


    window.location.href =
        "/trainee-dashboard.html";
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
   HTML SAFETY
========================= */

function escapeHtml(value) {

    if (
        value === null ||
        value === undefined
    ) {

        return "";
    }


    return String(value)
        .replace(
            /&/g,
            "&amp;"
        )
        .replace(
            /</g,
            "&lt;"
        )
        .replace(
            />/g,
            "&gt;"
        )
        .replace(
            /"/g,
            "&quot;"
        )
        .replace(
            /'/g,
            "&#039;"
        );
}


/* =========================
   INITIALIZE
========================= */

loadResult();