const token =
    localStorage.getItem("edutrack_token");

const name =
    localStorage.getItem("edutrack_name");

const role =
    localStorage.getItem("edutrack_role");

const currentPage =
    window.location.pathname;

if (!token) {

    window.location.href =
        "/login.html";
}

const expectedPage = {

    ADMIN:
        "/admin-dashboard.html",

    TRAINER:
        "/trainer-dashboard.html",

    TRAINEE:
        "/trainee-dashboard.html"

}[role];

if (!expectedPage || currentPage !== expectedPage) {

    window.location.href =
        expectedPage || "/login.html";
}

document.getElementById("userName")
    .textContent = name;

document.getElementById("userRole")
    .textContent = role;

async function verifyServerAccess() {

    try {

        const response =
            await fetch("/api/user/me", {

                headers: {
                    "Authorization":
                        `Bearer ${token}`
                }

            });

        if (!response.ok) {

            logout();

        }

    } catch (error) {

        console.error(error);
    }
}

verifyServerAccess();

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