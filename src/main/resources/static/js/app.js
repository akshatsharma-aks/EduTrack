const backendStatus = document.getElementById("backendStatus");
const connectionBadge = document.getElementById("connectionBadge");
const connectionTitle = document.getElementById("connectionTitle");
const connectionMessage = document.getElementById("connectionMessage");
const toast = document.getElementById("toast");


async function checkBackendConnection() {

    try {

        const response = await fetch("/api/health");

        if (!response.ok) {
            throw new Error("Backend returned an error");
        }

        const message = await response.text();

        backendStatus.textContent = "Connected";

        connectionBadge.textContent = "Connected";
        connectionBadge.className = "badge connected";

        connectionTitle.textContent =
            "Backend connection successful";

        connectionMessage.textContent =
            message;

        showToast("EduTrack backend connected successfully.");

    } catch (error) {

        backendStatus.textContent = "Offline";

        connectionBadge.textContent = "Offline";
        connectionBadge.className = "badge error";

        connectionTitle.textContent =
            "Backend connection failed";

        connectionMessage.textContent =
            "Make sure Spring Boot is running and try refreshing the page.";

        showToast("Unable to connect to backend.");

        console.error("Backend connection error:", error);
    }
}


function showToast(message) {

    toast.textContent = message;

    toast.classList.add("show");

    setTimeout(() => {
        toast.classList.remove("show");
    }, 3000);
}


document.addEventListener("DOMContentLoaded", () => {

    checkBackendConnection();

});