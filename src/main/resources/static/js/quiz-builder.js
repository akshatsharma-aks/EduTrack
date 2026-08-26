const token =
    localStorage.getItem("edutrack_token");

const role =
    localStorage.getItem("edutrack_role");

if (!token || role !== "TRAINER") {

    window.location.href =
        "/login.html";
}


let questionCounter = 0;


function authHeaders() {

    return {
        "Authorization":
            `Bearer ${token}`,

        "Content-Type":
            "application/json"
    };
}


/* =========================
   LOAD ASSIGNED BATCHES
========================= */

async function loadBatches() {

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

        alert(
            "Unable to load batches."
        );

        return;
    }

    const batches =
        await response.json();

    const select =
        document.getElementById(
            "quizBatch"
        );

    select.innerHTML =
        `<option value="">
            Select Batch
         </option>`;


    batches.forEach(batch => {

        const option =
            document.createElement(
                "option"
            );

        option.value =
            batch.id;

        option.textContent =
            `${batch.name} — ${batch.courseName}`;

        select.appendChild(
            option
        );
    });
}


/* =========================
   ADD QUESTION
========================= */

function addQuestion() {

    questionCounter++;

    const questionId =
        questionCounter;

    const container =
        document.getElementById(
            "questionsContainer"
        );


    const card =
        document.createElement(
            "div"
        );

    card.className =
        "quiz-question-card";

    card.dataset.questionId =
        questionId;


    card.innerHTML = `

        <div class="question-card-header">

            <h3>
                Question ${questionId}
            </h3>

            <button
                type="button"
                class="btn small danger"
                onclick="removeQuestion(${questionId})">

                Remove

            </button>

        </div>


        <div class="form-group">

            <label>
                Question
            </label>

            <textarea
                class="question-text"
                rows="3"
                placeholder="Enter your question"></textarea>

        </div>


        <div class="form-group">

            <label>
                Question Type
            </label>

            <select
                class="question-type"
                onchange="questionTypeChanged(${questionId})">

                <option value="SINGLE_CORRECT">
                    Single Correct
                </option>

                <option value="MULTIPLE_CORRECT">
                    Multiple Correct
                </option>

            </select>

        </div>


        <div class="options-container">

        </div>


        <button
            type="button"
            class="btn small secondary"
            onclick="addOption(${questionId})">

            + Add Option

        </button>

    `;


    container.appendChild(
        card
    );


    addOption(questionId);
    addOption(questionId);
    addOption(questionId);
}


/* =========================
   REMOVE QUESTION
========================= */

function removeQuestion(
    questionId
) {

    const card =
        document.querySelector(
            `[data-question-id="${questionId}"]`
        );

    if (card) {

        card.remove();
    }

    renumberQuestions();
}


/* =========================
   RENUMBER QUESTIONS
========================= */

function renumberQuestions() {

    const cards =
        document.querySelectorAll(
            ".quiz-question-card"
        );

    cards.forEach(
        (card, index) => {

            const heading =
                card.querySelector(
                    "h3"
                );

            heading.textContent =
                `Question ${index + 1}`;
        }
    );
}


/* =========================
   ADD OPTION
========================= */

function addOption(
    questionId
) {

    const card =
        document.querySelector(
            `[data-question-id="${questionId}"]`
        );

    if (!card) {

        return;
    }


    const container =
        card.querySelector(
            ".options-container"
        );


    const currentCount =
        container.children.length;


    if (currentCount >= 6) {

        alert(
            "A question can have maximum 6 options."
        );

        return;
    }


    const type =
        card.querySelector(
            ".question-type"
        ).value;


    const option =
        document.createElement(
            "div"
        );

    option.className =
        "quiz-option-row";


    const inputType =
        type === "MULTIPLE_CORRECT"
            ? "checkbox"
            : "radio";


    option.innerHTML = `

        <input
            type="${inputType}"
            name="question-${questionId}-correct"
            class="option-correct">


        <input
            type="text"
            class="option-text"
            placeholder="Option ${currentCount + 1}">


        <button
            type="button"
            class="btn small danger"
            onclick="removeOption(this)">

            ×

        </button>

    `;


    container.appendChild(
        option
    );
}


/* =========================
   REMOVE OPTION
========================= */

function removeOption(
    button
) {

    const container =
        button.closest(
            ".options-container"
        );

    if (
        container.children.length
        <= 3
    ) {

        alert(
            "A question must have at least 3 options."
        );

        return;
    }


    button.closest(
        ".quiz-option-row"
    ).remove();
}


/* =========================
   TYPE CHANGED
========================= */

function questionTypeChanged(
    questionId
) {

    const card =
        document.querySelector(
            `[data-question-id="${questionId}"]`
        );

    const type =
        card.querySelector(
            ".question-type"
        ).value;


    const correctInputs =
        card.querySelectorAll(
            ".option-correct"
        );


    correctInputs.forEach(
        input => {

            input.type =
                type === "MULTIPLE_CORRECT"
                    ? "checkbox"
                    : "radio";
        }
    );


    if (
        type === "SINGLE_CORRECT"
    ) {

        const checked =
            card.querySelectorAll(
                ".option-correct:checked"
            );

        if (checked.length > 1) {

            checked.forEach(
                (input, index) => {

                    if (index > 0) {

                        input.checked =
                            false;
                    }
                }
            );
        }
    }
}


/* =========================
   BUILD REQUEST
========================= */

function buildRequest() {

    const title =
        document.getElementById(
            "quizTitle"
        ).value.trim();


    const batchId =
        document.getElementById(
            "quizBatch"
        ).value;


    const timeLimit =
        Number(
            document.getElementById(
                "timeLimit"
            ).value
        );


    const questionCards =
        document.querySelectorAll(
            ".quiz-question-card"
        );


    if (!title) {

        throw new Error(
            "Quiz title is required."
        );
    }


    if (!batchId) {

        throw new Error(
            "Please select a batch."
        );
    }


    if (
        !timeLimit
        || timeLimit <= 0
    ) {

        throw new Error(
            "Time limit must be greater than zero."
        );
    }


    if (
        questionCards.length === 0
    ) {

        throw new Error(
            "Add at least one question."
        );
    }


    const questions =
        [];


    questionCards.forEach(
        card => {

            const questionText =
                card.querySelector(
                    ".question-text"
                ).value.trim();


            const type =
                card.querySelector(
                    ".question-type"
                ).value;


            if (!questionText) {

                throw new Error(
                    "Every question must have text."
                );
            }


            const optionRows =
                card.querySelectorAll(
                    ".quiz-option-row"
                );


            if (
                optionRows.length < 3
            ) {

                throw new Error(
                    "Every question must have at least 3 options."
                );
            }


            if (
                optionRows.length > 6
            ) {

                throw new Error(
                    "Every question can have maximum 6 options."
                );
            }


            const options =
                [];


            let correctCount =
                0;


            optionRows.forEach(
                row => {

                    const text =
                        row.querySelector(
                            ".option-text"
                        ).value.trim();


                    const correct =
                        row.querySelector(
                            ".option-correct"
                        ).checked;


                    if (!text) {

                        throw new Error(
                            "Option text cannot be empty."
                        );
                    }


                    if (correct) {

                        correctCount++;
                    }


                    options.push({
                        optionText: text,
                        correct: correct
                    });
                }
            );


            if (
                correctCount === 0
            ) {

                throw new Error(
                    "Every question needs at least one correct answer."
                );
            }


            if (
                type === "SINGLE_CORRECT"
                && correctCount !== 1
            ) {

                throw new Error(
                    "Single Correct questions must have exactly one correct answer."
                );
            }


            questions.push({
                questionText: questionText,
                type: type,
                options: options
            });
        }
    );


    return {
        title: title,
        timeLimitMinutes: timeLimit,
        batchId: Number(batchId),
        questions: questions
    };
}


/* =========================
   SAVE / PUBLISH
========================= */

async function saveQuiz(
    publish
) {

    try {

        const request =
            buildRequest();


        const response =
            await fetch(
                "/api/trainer/quizzes",
                {
                    method: "POST",

                    headers:
                        authHeaders(),

                    body:
                        JSON.stringify(
                            request
                        )
                }
            );


        const text =
            await response.text();


        if (!response.ok) {

            alert(
                text ||
                "Unable to create quiz."
            );

            return;
        }


        const quiz =
            JSON.parse(text);


        if (publish) {

            const publishResponse =
                await fetch(
                    `/api/trainer/quizzes/${quiz.id}/publish`,
                    {
                        method: "PUT",

                        headers: {
                            "Authorization":
                                `Bearer ${token}`
                        }
                    }
                );


            const publishText =
                await publishResponse.text();


            if (!publishResponse.ok) {

                alert(
                    publishText ||
                    "Quiz created but could not be published."
                );

                return;
            }


            alert(
                "Quiz created and published successfully."
            );

        } else {

            alert(
                "Quiz saved as draft successfully."
            );
        }


        window.location.href =
            "/trainer-dashboard.html";

    } catch (error) {

        alert(
            error.message
        );
    }
}


/* =========================
   BACK
========================= */

function goBack() {

    window.location.href =
        "/trainer-dashboard.html";
}


/* =========================
   INITIALIZE
========================= */

loadBatches();

addQuestion();