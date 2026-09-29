// ============================================================
// Enterprise AI Search Platform
// Authentication
// ============================================================

const AUTH_BASE_URL = "http://localhost:8080/api/v1/auth";


// ============================================================
// LOGIN API
// ============================================================

async function loginUser(email, password) {

    const response = await fetch(
        `${AUTH_BASE_URL}/login`,
        {
            method: "POST",

            headers: {
                "Content-Type": "application/json",
                "Accept": "application/json"
            },

            body: JSON.stringify({
                email: email,
                password: password
            })
        }
    );


    let data = null;

    try {
        data = await response.json();
    } catch (error) {
        data = null;
    }


    if (!response.ok) {

        let message =
            "Login failed. Please check your email and password.";

        if (data) {

            if (typeof data === "string") {
                message = data;
            }

            else if (data.message) {
                message = data.message;
            }

            else if (data.error) {
                message = data.error;
            }
        }

        throw new Error(message);
    }


    return data;
}


// ============================================================
// LOGIN FORM
// ============================================================

document.addEventListener(
    "DOMContentLoaded",
    function () {

        const loginForm =
            document.getElementById("loginForm");


        if (!loginForm) {
            return;
        }


        console.log(
            "Login form initialized"
        );


        loginForm.addEventListener(
            "submit",
            async function (event) {

                event.preventDefault();


                const emailInput =
                    document.getElementById("email");


                const passwordInput =
                    document.getElementById("password");


                const errorElement =
                    document.getElementById("loginError");


                const button =
                    loginForm.querySelector(
                        "button[type='submit']"
                    );


                const email =
                    emailInput.value.trim();


                const password =
                    passwordInput.value;


                errorElement.textContent = "";


                if (!email) {

                    errorElement.textContent =
                        "Please enter your email.";

                    return;
                }


                if (!password) {

                    errorElement.textContent =
                        "Please enter your password.";

                    return;
                }


                try {

                    button.disabled = true;

                    button.textContent =
                        "Signing in...";


                    console.log(
                        "Sending login request..."
                    );


                    const data =
                        await loginUser(
                            email,
                            password
                        );


                    console.log(
                        "Login response:",
                        data
                    );


                    // ----------------------------------------
                    // JWT
                    // ----------------------------------------

                    const token =
                        data.token ||
                        data.jwt ||
                        data.accessToken;


                    if (!token) {

                        throw new Error(
                            "Login succeeded but no JWT token was returned."
                        );
                    }


                    // ----------------------------------------
                    // Store authentication data
                    // ----------------------------------------

                    localStorage.setItem(
                        "jwtToken",
                        token
                    );


                    localStorage.setItem(
                        "userEmail",
                        email
                    );


                    // ----------------------------------------
                    // Redirect to MAIN APPLICATION
                    // ----------------------------------------

                    window.location.replace(
                        "dashboard.html"
                    );

                }


                catch (error) {

                    console.error(
                        "Login error:",
                        error
                    );


                    errorElement.textContent =
                        error.message ||
                        "Unable to login. Please try again.";
                }


                finally {

                    button.disabled =
                        false;

                    button.textContent =
                        "Sign In";
                }

            }
        );

    }
);