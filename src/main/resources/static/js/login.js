document.getElementById("togglePassword").addEventListener("click", function () {
    const passwordField = document.getElementById("passwordField");
    const type = passwordField.getAttribute("type") === "password" ? "text" : "password";
    passwordField.setAttribute("type", type);
});

