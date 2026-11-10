const form = document.getElementById("login-form");
const captchaImage = document.getElementById("captcha-image");
const captchaInput = document.getElementById("captcha");
const submitButton = document.getElementById("submit-button");
const resultBox = document.getElementById("result");
const apiBase = window.CAPTCHA_API_BASE;
let loginCompleted = false;

function randomAddressToken() {
    if (window.crypto && typeof window.crypto.randomUUID === "function") {
        return window.crypto.randomUUID();
    }
    return Date.now().toString(36) + Math.random().toString(36).slice(2);
}

function refreshCaptcha() {
    if (loginCompleted) {
        return;
    }
    captchaImage.src = apiBase + "/captcha/" + randomAddressToken();
}

function showResult(message, type) {
    resultBox.textContent = message;
    resultBox.className = "result " + (type === "success" ? "is-success" : "is-error");
}

captchaImage.addEventListener("click", refreshCaptcha);
captchaImage.addEventListener("keydown", (event) => {
    if (event.key === "Enter" || event.key === " ") {
        event.preventDefault();
        refreshCaptcha();
    }
});
captchaImage.addEventListener("error", () => {
    showResult("验证码加载失败，请确认后端服务已启动。", "error");
});

refreshCaptcha();

form.addEventListener("submit", async (event) => {
    event.preventDefault();
    if (loginCompleted) {
        return;
    }
    submitButton.disabled = true;
    submitButton.textContent = "正在验证...";

    let loginSucceeded = false;
    try {
        const response = await fetch(apiBase + "/login", {
            method: "POST",
            headers: {
                "Content-Type": "application/x-www-form-urlencoded;charset=UTF-8"
            },
            body: new URLSearchParams(new FormData(form)),
            credentials: "same-origin"
        });

        if (!response.ok) {
            throw new Error("服务器返回了异常状态：" + response.status);
        }

        const result = await response.json();
        showResult(result.message, result.success ? "success" : "error");
        loginSucceeded = result.success;
    } catch (error) {
        showResult("暂时无法完成验证，请确认前后端服务已启动。", "error");
    } finally {
        captchaInput.value = "";
        if (loginSucceeded) {
            loginCompleted = true;
            form.querySelectorAll("input, button").forEach((control) => {
                control.disabled = true;
            });
            captchaImage.setAttribute("aria-disabled", "true");
            submitButton.textContent = "登录成功";
        } else {
            submitButton.disabled = false;
            submitButton.textContent = "提交验证";
            refreshCaptcha();
        }
    }
});
