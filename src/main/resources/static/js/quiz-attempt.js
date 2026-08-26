const token =
    localStorage.getItem("edutrack_token");

const role =
    localStorage.getItem("edutrack_role");


/* ==================================================
   AUTHENTICATION
================================================== */

if (!token || role !== "TRAINEE") {

    window.location.href =
        "/login.html";
}


/* ==================================================
   QUIZ ID
================================================== */

const urlParams =
    new URLSearchParams(
        window.location.search
    );


const quizId =
    urlParams.get("quizId");


if (!quizId) {

    alert(
        "Quiz ID is missing."
    );

    window.location.href =
        "/quizzes.html";
}


/* ==================================================
   STATE
================================================== */

let attemptId = null;

let quiz = null;

let questions = [];

let currentQuestionIndex = 0;

let answers = {};

let timerInterval = null;

let remainingSeconds = 0;

let submitting = false;


/* ==================================================
   ANTI-TAB-SWITCHING STATE
================================================== */

/*
 * Maximum allowed violations:
 *
 * 1st switch → Warning
 * 2nd switch → Warning
 * 3rd switch → Automatic submission
 */

const MAX_VIOLATIONS_BEFORE_SUBMIT = 3;


/*
 * Number of times the trainee has
 * left the quiz page.
 */

let violationCount = 0;


/*
 * Prevent duplicate detection when
 * visibilitychange and blur happen
 * for the same event.
 */

let lastViolationTime = 0;


/*
 * Prevent anti-tab logic from running
 * before the quiz has actually started.
 */

let quizStarted = false;


/*
 * Prevent further interaction after
 * automatic submission begins.
 */

let quizLocked = false;


/*
 * Used to determine whether blur
 * has already been handled by
 * visibilitychange.
 */

let hiddenByVisibilityChange = false;


/* ==================================================
   AUTH HEADERS
================================================== */

function authHeaders() {

    return {
        "Authorization":
            `Bearer ${token}`,

        "Content-Type":
            "application/json"
    };
}


/* ==================================================
   START QUIZ
================================================== */

async function initializeQuiz() {

    try {

        const response =
            await fetch(
                `/api/trainee/quizzes/${quizId}`,
                {
                    method: "GET",

                    headers:
                        authHeaders()
                }
            );


        const text =
            await response.text();


        if (!response.ok) {

            console.error(
                "Start quiz failed:",
                response.status,
                text
            );


            alert(
                text ||
                "Unable to start quiz."
            );


            window.location.href =
                "/quizzes.html";

            return;
        }


        const data =
            JSON.parse(text);


        console.log(
            "Quiz attempt started:",
            data
        );


        /*
         * Expected backend structure:
         *
         * {
         *   attemptId,
         *   quizId,
         *   title,
         *   timeLimitMinutes,
         *   startedAt,
         *   quiz: {...}
         * }
         */


        attemptId =
            data.attemptId
            ?? data.id;


        quiz =
            data.quiz;


        /*
         * Defensive fallback in case
         * backend returns quiz directly.
         */

        if (!quiz) {

            quiz = data;
        }


        questions =
            quiz.questions || [];


        if (!attemptId) {

            throw new Error(
                "Attempt ID was not returned by server."
            );
        }


        if (
            !questions ||
            questions.length === 0
        ) {

            throw new Error(
                "This quiz contains no questions."
            );
        }


        document.getElementById(
            "quizTitle"
        ).textContent =
            quiz.title;


        document.getElementById(
            "quizInfo"
        ).textContent =
            `${questions.length} Questions • `
            +
            `${quiz.timeLimitMinutes} Minutes`;


        /*
         * Initialize empty answers.
         */

        questions.forEach(
            question => {

                answers[
                    question.id
                    ] = [];

            }
        );


        /*
         * Quiz is now officially active.
         */

        quizStarted = true;


        /*
         * Start timer.
         */

        startTimer(
            data.startedAt
        );


        renderQuestion();

        renderNavigation();


        updateViolationCounter();


        /*
         * Install anti-tab detection
         * only after quiz starts.
         */

        initializeAntiTabSwitching();


    } catch (error) {

        console.error(
            "Quiz initialization error:",
            error
        );


        alert(
            error.message ||
            "Unable to start quiz."
        );


        window.location.href =
            "/quizzes.html";
    }
}


/* ==================================================
   ANTI-TAB-SWITCHING
================================================== */

function initializeAntiTabSwitching() {

    /*
     * Browser tab / window visibility.
     *
     * This is the primary mechanism.
     */

    document.addEventListener(
        "visibilitychange",
        handleVisibilityChange
    );


    /*
     * Browser window focus / blur.
     *
     * Used as a secondary browser-level
     * signal, particularly when the user
     * changes application/window.
     */

    window.addEventListener(
        "blur",
        handleWindowBlur
    );


    window.addEventListener(
        "focus",
        handleWindowFocus
    );


    console.log(
        "Quiz anti-tab-switching protection enabled."
    );
}


/* ==================================================
   VISIBILITY CHANGE
================================================== */

function handleVisibilityChange() {

    if (!quizStarted || submitting || quizLocked) {

        return;
    }


    /*
     * Page has become hidden.
     *
     * This is the strongest signal that
     * the trainee left the quiz tab.
     */

    if (
        document.visibilityState === "hidden"
    ) {

        hiddenByVisibilityChange = true;

        registerTabViolation(
            "visibilitychange"
        );

        return;
    }


    /*
     * Page has become visible again.
     */

    if (
        document.visibilityState === "visible"
    ) {

        hiddenByVisibilityChange = false;
    }
}


/* ==================================================
   WINDOW BLUR
================================================== */

function handleWindowBlur() {

    if (!quizStarted || submitting || quizLocked) {

        return;
    }


    /*
     * If visibilitychange already detected
     * the tab leaving the foreground,
     * do NOT count blur again.
     */

    if (
        document.visibilityState === "hidden"
    ) {

        return;
    }


    /*
     * The browser window may have lost
     * focus without changing visibility.
     *
     * Example:
     * switching to another application.
     */

    registerTabViolation(
        "blur"
    );
}


/* ==================================================
   WINDOW FOCUS
================================================== */

function handleWindowFocus() {

    /*
     * No violation is counted on focus.
     *
     * The violation was already counted
     * when the page lost visibility/focus.
     */

    console.log(
        "Quiz window focused."
    );
}


/* ==================================================
   REGISTER VIOLATION
================================================== */

function registerTabViolation(
    source
) {

    if (
        !quizStarted ||
        submitting ||
        quizLocked
    ) {

        return;
    }


    /*
     * Prevent duplicate events from
     * counting as separate violations.
     *
     * Example:
     *
     * visibilitychange
     * followed immediately by
     * blur
     */

    const now =
        Date.now();


    if (
        now - lastViolationTime
        < 1000
    ) {

        console.log(
            "Duplicate tab-switch event ignored:",
            source
        );

        return;
    }


    lastViolationTime =
        now;


    violationCount++;


    console.warn(
        "Quiz tab-switch violation:",
        violationCount,
        "source:",
        source
    );


    updateViolationCounter();


    /*
     * Third violation:
     *
     * Automatically submit.
     */

    if (
        violationCount
        >=
        MAX_VIOLATIONS_BEFORE_SUBMIT
    ) {

        autoSubmitForTabViolation();

        return;
    }


    /*
     * First and second violations:
     * show warning.
     *
     * The warning will be visible when
     * the trainee returns to the page.
     */

    showViolationWarning();
}


/* ==================================================
   UPDATE VIOLATION COUNTER
================================================== */

function updateViolationCounter() {

    const counter =
        document.getElementById(
            "violationCounter"
        );


    if (!counter) {

        return;
    }


    counter.textContent =
        `Tab switches: ${
            violationCount
        } / 2`;


    /*
     * Change appearance after violations.
     */

    if (
        violationCount === 0
    ) {

        counter.style.background =
            "#fff3cd";

        counter.style.color =
            "#856404";

        counter.style.borderColor =
            "#ffe69c";

    } else if (
        violationCount === 1
    ) {

        counter.style.background =
            "#fff3cd";

        counter.style.color =
            "#856404";

        counter.style.borderColor =
            "#ffe69c";

    } else {

        counter.style.background =
            "#f8d7da";

        counter.style.color =
            "#842029";

        counter.style.borderColor =
            "#f1aeb5";
    }
}


/* ==================================================
   SHOW VIOLATION WARNING
================================================== */

function showViolationWarning() {

    /*
     * If the page is still hidden,
     * don't attempt to force a modal.
     *
     * It will be shown when the trainee
     * returns.
     */

    if (
        document.visibilityState !==
        "visible"
    ) {

        return;
    }


    const modal =
        document.getElementById(
            "violationModal"
        );


    const title =
        document.getElementById(
            "violationTitle"
        );


    const message =
        document.getElementById(
            "violationMessage"
        );


    const countText =
        document.getElementById(
            "violationCountText"
        );


    const continueButton =
        document.getElementById(
            "violationContinueButton"
        );


    if (!modal) {

        return;
    }


    title.textContent =
        `Warning ${violationCount}`;


    message.textContent =
        "You left the quiz page. " +
        "Please remain on the quiz page " +
        "until your attempt is completed.";


    countText.textContent =
        `Violation ${violationCount} of 2`;


    continueButton.textContent =
        "Continue Quiz";


    modal.style.display =
        "flex";
}


/* ==================================================
   CLOSE VIOLATION MODAL
================================================== */

function closeViolationModal() {

    const modal =
        document.getElementById(
            "violationModal"
        );


    if (!modal) {

        return;
    }


    modal.style.display =
        "none";
}


/* ==================================================
   AUTOMATIC SUBMISSION
   CAUSED BY TAB VIOLATION
================================================== */

async function autoSubmitForTabViolation() {

    if (submitting) {

        return;
    }


    quizLocked = true;


    /*
     * Stop timer.
     */

    if (timerInterval) {

        clearInterval(
            timerInterval
        );
    }


    /*
     * Disable all quiz interaction.
     */

    disableQuizInteraction();


    /*
     * Show blocking overlay.
     */

    const overlay =
        document.getElementById(
            "autoSubmitOverlay"
        );


    if (overlay) {

        overlay.style.display =
            "flex";
    }


    const status =
        document.getElementById(
            "autoSubmitStatus"
        );


    if (status) {

        status.textContent =
            "Maximum tab-switch violations reached. Submitting...";
    }


    /*
     * Submit directly.
     *
     * IMPORTANT:
     * We do NOT call submitQuiz()
     * because submitQuiz() asks for
     * confirmation.
     */

    await performSubmit(
        true
    );
}


/* ==================================================
   DISABLE QUIZ INTERACTION
================================================== */

function disableQuizInteraction() {

    quizLocked = true;


    /*
     * Disable all buttons.
     */

    document.querySelectorAll(
        "button"
    ).forEach(
        button => {

            button.disabled =
                true;

        }
    );


    /*
     * Disable all answer inputs.
     */

    document.querySelectorAll(
        "input"
    ).forEach(
        input => {

            input.disabled =
                true;

        }
    );


    /*
     * Disable text interaction
     * with the question area.
     */

    const questionContainer =
        document.getElementById(
            "questionContainer"
        );


    if (questionContainer) {

        questionContainer.style.pointerEvents =
            "none";
    }
}


/* ==================================================
   TIMER
================================================== */

function startTimer(
    startedAt
) {

    const timeLimit =
        Number(
            quiz.timeLimitMinutes
        ) || 0;


    if (timeLimit <= 0) {

        return;
    }


    /*
     * Backend returns epoch milliseconds.
     */

    let startTime =
        Number(
            startedAt
        );


    /*
     * Defensive handling if backend
     * returns a date string.
     */

    if (
        Number.isNaN(
            startTime
        )
    ) {

        startTime =
            new Date(
                startedAt
            ).getTime();
    }


    const deadline =
        startTime
        +
        (
            timeLimit
            *
            60
            *
            1000
        );


    function updateTimer() {

        if (quizLocked) {

            return;
        }


        const now =
            Date.now();


        remainingSeconds =
            Math.max(
                0,
                Math.floor(
                    (
                        deadline
                        -
                        now
                    ) / 1000
                )
            );


        const minutes =
            Math.floor(
                remainingSeconds / 60
            );


        const seconds =
            remainingSeconds % 60;


        document.getElementById(
            "timer"
        ).textContent =
            `${String(minutes).padStart(2, "0")}:`
            +
            `${String(seconds).padStart(2, "0")}`;


        /*
         * Time expired.
         */

        if (
            remainingSeconds <= 0
        ) {

            clearInterval(
                timerInterval
            );


            document.getElementById(
                "timer"
            ).textContent =
                "00:00";


            autoSubmitQuiz();
        }
    }


    updateTimer();


    timerInterval =
        setInterval(
            updateTimer,
            1000
        );
}


/* ==================================================
   RENDER CURRENT QUESTION
================================================== */

function renderQuestion() {

    if (quizLocked) {

        return;
    }


    const question =
        questions[
            currentQuestionIndex
            ];


    if (!question) {

        return;
    }


    const container =
        document.getElementById(
            "questionContainer"
        );


    const selectedOptions =
        answers[
            question.id
            ] || [];


    const inputType =
        question.type
        === "SINGLE_CORRECT"
            ? "radio"
            : "checkbox";


    let optionsHtml = "";


    question.options.forEach(
        option => {

            const checked =
                selectedOptions
                    .includes(
                        option.id
                    );


            optionsHtml += `

                <label
                    style="
                        display:block;
                        padding:16px;
                        margin:10px 0;
                        border:1px solid #ddd;
                        border-radius:10px;
                        cursor:pointer;
                    ">

                    <input
                        type="${inputType}"
                        name="questionOption"
                        value="${option.id}"
                        ${checked ? "checked" : ""}
                        onchange="selectOption(
                            ${question.id},
                            ${option.id},
                            '${question.type}'
                        )">

                    <span style="margin-left:10px;">

                        ${escapeHtml(
                option.optionText
            )}

                    </span>

                </label>

            `;
        }
    );


    container.innerHTML = `

        <div>

            <h2>

                ${escapeHtml(
        question.questionText
    )}

            </h2>


            <p class="admin-subtitle">

                ${
        question.type
        === "MULTIPLE_CORRECT"
            ? "Select all correct answers."
            : "Select one answer."
    }

            </p>


            <div>

                ${optionsHtml}

            </div>

        </div>

    `;


    document.getElementById(
        "questionCounter"
    ).textContent =
        `Question ${
            currentQuestionIndex + 1
        } of ${
            questions.length
        }`;


    updateNavigationButtons();

    updateNavigationHighlight();
}


/* ==================================================
   SELECT OPTION
================================================== */

function selectOption(
    questionId,
    optionId,
    questionType
) {

    if (quizLocked) {

        return;
    }


    if (
        questionType
        === "SINGLE_CORRECT"
    ) {

        answers[
            questionId
            ] = [
            optionId
        ];

    } else {

        let selected =
            answers[
                questionId
                ] || [];


        if (
            selected.includes(
                optionId
            )
        ) {

            selected =
                selected.filter(
                    id =>
                        id !== optionId
                );

        } else {

            selected.push(
                optionId
            );
        }


        answers[
            questionId
            ] = selected;
    }


    updateNavigationHighlight();
}


/* ==================================================
   NEXT QUESTION
================================================== */

function nextQuestion() {

    if (quizLocked) {

        return;
    }


    if (
        currentQuestionIndex
        <
        questions.length - 1
    ) {

        currentQuestionIndex++;

        renderQuestion();

        return;
    }


    /*
     * Last question.
     */

    const confirmed =
        confirm(
            "You are on the last question. Submit the quiz?"
        );


    if (confirmed) {

        submitQuiz();
    }
}


/* ==================================================
   PREVIOUS QUESTION
================================================== */

function previousQuestion() {

    if (quizLocked) {

        return;
    }


    if (
        currentQuestionIndex
        > 0
    ) {

        currentQuestionIndex--;

        renderQuestion();
    }
}


/* ==================================================
   NAVIGATION
================================================== */

function renderNavigation() {

    const container =
        document.getElementById(
            "questionNavigation"
        );


    container.innerHTML = "";


    questions.forEach(
        (question, index) => {

            const button =
                document.createElement(
                    "button"
                );


            button.className =
                "btn small secondary";


            button.textContent =
                index + 1;


            button.onclick =
                function () {

                    if (quizLocked) {

                        return;
                    }


                    currentQuestionIndex =
                        index;


                    renderQuestion();
                };


            container.appendChild(
                button
            );
        }
    );


    updateNavigationHighlight();
}


/* ==================================================
   NAVIGATION HIGHLIGHT
================================================== */

function updateNavigationHighlight() {

    const buttons =
        document.querySelectorAll(
            "#questionNavigation button"
        );


    buttons.forEach(
        (button, index) => {

            const question =
                questions[index];


            const answered =
                answers[
                    question.id
                    ] &&
                answers[
                    question.id
                    ].length > 0;


            if (
                index
                ===
                currentQuestionIndex
            ) {

                button.className =
                    "btn small primary";

            } else if (answered) {

                button.className =
                    "btn small primary";

            } else {

                button.className =
                    "btn small secondary";
            }
        }
    );
}


/* ==================================================
   NAVIGATION BUTTON STATE
================================================== */

function updateNavigationButtons() {

    const previous =
        document.getElementById(
            "previousButton"
        );


    const next =
        document.getElementById(
            "nextButton"
        );


    if (!previous || !next) {

        return;
    }


    previous.disabled =
        currentQuestionIndex === 0
        ||
        quizLocked;


    if (
        currentQuestionIndex
        ===
        questions.length - 1
    ) {

        next.textContent =
            "Submit Quiz";

    } else {

        next.textContent =
            "Next";
    }


    next.disabled =
        quizLocked;
}


/* ==================================================
   NORMAL SUBMIT
================================================== */

async function submitQuiz() {

    if (
        submitting ||
        quizLocked
    ) {

        return;
    }


    const unanswered =
        questions.filter(
            question => {

                const selected =
                    answers[
                        question.id
                        ] || [];


                return selected.length === 0;
            }
        );


    if (
        unanswered.length > 0
    ) {

        const confirmed =
            confirm(
                `You have ${
                    unanswered.length
                } unanswered question(s). Submit anyway?`
            );


        if (!confirmed) {

            return;
        }
    }


    const confirmed =
        confirm(
            "Are you sure you want to submit the quiz?"
        );


    if (!confirmed) {

        return;
    }


    await performSubmit(
        false
    );
}


/* ==================================================
   AUTO SUBMIT — TIME
================================================== */

async function autoSubmitQuiz() {

    if (
        submitting
    ) {

        return;
    }


    /*
     * Lock quiz immediately.
     */

    quizLocked = true;


    disableQuizInteraction();


    alert(
        "Time is up. Your quiz will be submitted automatically."
    );


    await performSubmit(
        true
    );
}


/* ==================================================
   ACTUAL SUBMISSION
================================================== */

async function performSubmit(
    automaticSubmission = false
) {

    if (submitting) {

        return;
    }


    submitting = true;


    quizLocked = true;


    /*
     * Stop timer.
     */

    if (timerInterval) {

        clearInterval(
            timerInterval
        );
    }


    /*
     * Disable interaction.
     */

    disableQuizInteraction();


    const submitButton =
        document.getElementById(
            "submitButton"
        );


    if (submitButton) {

        submitButton.disabled =
            true;

        submitButton.textContent =
            automaticSubmission
                ? "Auto-submitting..."
                : "Submitting...";
    }


    /*
     * Convert answers object into
     * backend expected format.
     */

    const answerList =
        questions.map(
            question => {

                return {

                    questionId:
                    question.id,

                    selectedOptionIds:
                        answers[
                            question.id
                            ] || []
                };
            }
        );


    const requestBody = {

        attemptId:
        attemptId,

        answers:
        answerList
    };


    console.log(
        "Submitting quiz:",
        requestBody
    );


    try {

        const response =
            await fetch(
                "/api/trainee/quizzes/submit",
                {
                    method: "POST",

                    headers:
                        authHeaders(),

                    body:
                        JSON.stringify(
                            requestBody
                        )
                }
            );


        const text =
            await response.text();


        if (!response.ok) {

            console.error(
                "Quiz submission failed:",
                response.status,
                text
            );


            alert(
                text ||
                "Unable to submit quiz."
            );


            submitting = false;

            quizLocked = false;


            if (submitButton) {

                submitButton.disabled =
                    false;

                submitButton.textContent =
                    "Submit Quiz";
            }


            return;
        }


        const result =
            JSON.parse(text);


        console.log(
            "Quiz result:",
            result
        );


        /*
         * Store result temporarily so
         * result page can display it.
         */

        sessionStorage.setItem(
            "edutrack_last_quiz_result",
            JSON.stringify(result)
        );


        window.location.href =
            "/quiz-result.html";


    } catch (error) {

        console.error(
            "Quiz submission error:",
            error
        );


        alert(
            "An error occurred while submitting the quiz."
        );


        submitting = false;

        quizLocked = false;


        if (submitButton) {

            submitButton.disabled =
                false;

            submitButton.textContent =
                "Submit Quiz";
        }
    }
}


/* ==================================================
   HTML SAFETY
================================================== */

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


/* ==================================================
   INITIALIZE
================================================== */

initializeQuiz();