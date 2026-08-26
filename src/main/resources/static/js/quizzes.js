const token =
    localStorage.getItem("edutrack_token");

const role =
    localStorage.getItem("edutrack_role");

const name =
    localStorage.getItem("edutrack_name");


/* =========================
   AUTHENTICATION
========================= */

if (!token || role !== "TRAINEE") {

    window.location.href =
        "/login.html";
}


/* =========================
   AUTH HEADERS
========================= */

function authHeaders() {

    return {
        "Authorization":
            `Bearer ${token}`
    };
}


/* =========================
   LOAD PUBLISHED QUIZZES
========================= */

async function loadQuizzes() {

    const container =
        document.getElementById(
            "quizList"
        );

    try {

        const response =
            await fetch(
                "/api/trainee/quizzes",
                {
                    headers:
                        authHeaders()
                }
            );


        if (!response.ok) {

            const text =
                await response.text();

            console.error(
                "Quiz API failed:",
                response.status,
                text
            );

            container.innerHTML = `
                <div class="empty-state">
                    Unable to load quizzes.
                </div>
            `;

            return;
        }


        const quizzes =
            await response.json();


        container.innerHTML = "";


        if (
            !quizzes ||
            quizzes.length === 0
        ) {

            container.innerHTML = `
                <div class="empty-state">

                    No published quizzes are
                    available for your batches.

                </div>
            `;

            return;
        }


        quizzes.forEach(
            quiz => {

                const row =
                    document.createElement(
                        "div"
                    );


                row.className =
                    "data-row";


                row.innerHTML = `

                    <div class="data-main">

                        <strong>
                            ${escapeHtml(
                    quiz.title
                )}
                        </strong>

                        <span>
                            Batch:
                            ${escapeHtml(
                    quiz.batchName
                )}
                        </span>

                        <span>
                            Course:
                            ${escapeHtml(
                    quiz.courseName
                )}
                        </span>

                    </div>


                    <div class="batch-dates">

                        ${quiz.questionCount}
                        Questions

                        <br>

                        ${quiz.timeLimitMinutes}
                        Minutes

                    </div>


                    <div class="row-actions">

                        <button
                            class="btn small primary"
                            onclick="startQuiz(
                                ${quiz.id}
                            )">

                            Start Quiz

                        </button>

                    </div>

                `;


                container.appendChild(
                    row
                );
            }
        );


    } catch (error) {

        console.error(
            "Quiz loading error:",
            error
        );


        container.innerHTML = `
            <div class="empty-state">

                Unable to connect to the server.

            </div>
        `;
    }
}


/* =========================
   START QUIZ
========================= */

function startQuiz(
    quizId
) {

    window.location.href =
        `/quiz-attempt.html?quizId=${quizId}`;
}


/* =========================
   LOAD PREVIOUS RESULTS
========================= */

async function loadResults() {

    const container =
        document.getElementById(
            "resultList"
        );


    try {

        const response =
            await fetch(
                "/api/trainee/quizzes/results",
                {
                    headers:
                        authHeaders()
                }
            );


        if (!response.ok) {

            const text =
                await response.text();

            console.error(
                "Results API failed:",
                response.status,
                text
            );

            container.innerHTML = `
                <div class="empty-state">

                    Unable to load quiz results.

                </div>
            `;

            return;
        }


        const results =
            await response.json();


        container.innerHTML = "";


        if (
            !results ||
            results.length === 0
        ) {

            container.innerHTML = `
                <div class="empty-state">

                    You have not completed
                    any quizzes yet.

                </div>
            `;

            return;
        }


        results.forEach(
            result => {

                const row =
                    document.createElement(
                        "div"
                    );


                row.className =
                    "data-row";


                const percentage =
                    Number(
                        result.percentage
                        || 0);


                const submittedDate =
                    result.submittedAt
                        ? new Date(
                            result.submittedAt
                        ).toLocaleString()
                        : "-";


                row.innerHTML = `

                    <div class="data-main">

                        <strong>

                            ${escapeHtml(
                    result.quizTitle
                )}

                        </strong>

                        <span>

                            Attempt
                            #${result.attemptNumber}

                        </span>

                        <span>

                            ${submittedDate}

                        </span>

                    </div>


                    <div class="batch-dates">

                        ${result.score}
                        /
                        ${result.totalQuestions}

                        <br>

                        ${percentage.toFixed(2)}%

                    </div>


                    <span
                        class="
                            status
                            ${
                    percentage >= 50
                        ? "approved"
                        : "declined"
                }
                        ">

                        ${percentage.toFixed(2)}%

                    </span>

                `;


                container.appendChild(
                    row
                );
            }
        );


    } catch (error) {

        console.error(
            "Results loading error:",
            error
        );


        container.innerHTML = `
            <div class="empty-state">

                Unable to load quiz results.

            </div>
        `;
    }
}


/* =========================
   NAVIGATION
========================= */

function goBack() {

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

async function initialize() {

    await loadQuizzes();

    await loadResults();
}


initialize();