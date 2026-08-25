const registerForm = document.getElementById("registerForm");
const message = document.getElementById("message");

registerForm.addEventListener("submit", async function (event) {

    event.preventDefault();

    message.className = "message";
    message.textContent = "";

    const name =
        document.getElementById("name").value.trim();

    const email =
        document.getElementById("email").value.trim();

    const password =
        document.getElementById("password").value;

    const role =
        document.getElementById("role").value;

    if (!name || !email || !password || !role) {

        showMessage(
            "Please fill in all fields.",
            "error"
        );

        return;
    }

    if (role === "ADMIN") {

        showMessage(
            "ADMIN registration is not allowed.",
            "error"
        );

        return;
    }

    try {

        const response = await fetch("/api/auth/register", {

            method: "POST",

            headers: {
                "Content-Type": "application/json"
            },

            body: JSON.stringify({
                name,
                email,
                password,
                role
            })

        });

        const result = await response.json();

        if (response.ok) {

            showMessage(
                `Account created successfully as ${result.role}.`,
                "success"
            );

            registerForm.reset();

        } else {

            const errorMessage =
                typeof result === "string"
                    ? result
                    : "Registration failed.";

            showMessage(
                errorMessage,
                "error"
            );
        }

    } catch (error) {

        console.error(error);

        showMessage(
            "Unable to connect to the server.",
            "error"
        );
    }

});

function showMessage(text, type) {

    message.textContent = text;

    message.className =
        `message ${type}`;
}