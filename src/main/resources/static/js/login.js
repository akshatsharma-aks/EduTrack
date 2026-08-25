const loginForm =
    document.getElementById("loginForm");

const message =
    document.getElementById("message");

loginForm.addEventListener(
    "submit",
    async function (event) {

        event.preventDefault();

        const email =
            document.getElementById("email")
                .value
                .trim();

        const password =
            document.getElementById("password")
                .value;

        message.className = "message";
        message.textContent = "";

        try {

            const response =
                await fetch("/api/auth/login", {

                    method: "POST",

                    headers: {
                        "Content-Type":
                            "application/json"
                    },

                    body: JSON.stringify({
                        email,
                        password
                    })
                });

            const result =
                await response.json();

            if (!response.ok) {

                showMessage(
                    typeof result === "string"
                        ? result
                        : "Login failed.",
                    "error"
                );

                return;
            }

            localStorage.setItem(
                "edutrack_token",
                result.token
            );

            localStorage.setItem(
                "edutrack_name",
                result.name
            );

            localStorage.setItem(
                "edutrack_role",
                result.role
            );

            redirectByRole(result.role);

        } catch (error) {

            console.error(error);

            showMessage(
                "Unable to connect to the server.",
                "error"
            );
        }
    }
);

function redirectByRole(role) {

    if (role === "ADMIN") {

        window.location.href =
            "/admin-dashboard.html";

    } else if (role === "TRAINER") {

        window.location.href =
            "/trainer-dashboard.html";

    } else if (role === "TRAINEE") {

        window.location.href =
            "/trainee-dashboard.html";

    } else {

        showMessage(
            "Unknown user role.",
            "error"
        );
    }
}

function showMessage(text, type) {

    message.textContent = text;

    message.className =
        `message ${type}`;
}