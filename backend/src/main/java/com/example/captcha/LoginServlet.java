package com.example.captcha;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

/**
 * 后端登录接口，只返回 JSON 判断结果。
 */
@WebServlet(name = "LoginServlet", urlPatterns = "/api/login")
public class LoginServlet extends HttpServlet {

    private static final Map<String, String> USERS = Map.of(
            "student", "123456"
    );

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");
        response.setHeader("Cache-Control", "no-store");

        String username = valueOf(request.getParameter("username"));
        String password = valueOf(request.getParameter("password"));
        String captchaInput = valueOf(request.getParameter("captcha"));
        CaptchaServlet.CaptchaChallenge savedCaptcha = takeCaptcha(request);

        if (username.isBlank() || password.isBlank()) {
            writeResult(response, false, "missing_credentials", "请输入用户名和密码。");
            return;
        }

        if (savedCaptcha == null) {
            writeResult(response, false, "captcha_missing", "请刷新验证码后重试。");
            return;
        }

        if (savedCaptcha.isExpired(System.currentTimeMillis())) {
            writeResult(response, false, "captcha_expired", "验证码已过期，请刷新后重新输入。");
            return;
        }

        if (captchaInput.isBlank() || !savedCaptcha.code().equalsIgnoreCase(captchaInput.trim())) {
            writeResult(response, false, "captcha_error", "验证码错误，请重新输入。");
            return;
        }

        String expectedPassword = USERS.get(username.trim());
        if (expectedPassword == null || !expectedPassword.equals(password)) {
            writeResult(response, false, "credentials_error", "用户名或密码错误，请检查后重试。");
            return;
        }

        writeResult(
                response,
                true,
                "login_success",
                "验证通过，登录成功。欢迎 " + username.trim() + "！");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        response.setHeader("Allow", "POST");
    }

    private CaptchaServlet.CaptchaChallenge takeCaptcha(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }

        Object value = session.getAttribute(CaptchaServlet.SESSION_ATTRIBUTE);
        session.removeAttribute(CaptchaServlet.SESSION_ATTRIBUTE);
        return value instanceof CaptchaServlet.CaptchaChallenge captcha ? captcha : null;
    }

    private String valueOf(String value) {
        return value == null ? "" : value;
    }

    private void writeResult(
            HttpServletResponse response,
            boolean success,
            String code,
            String message) throws IOException {
        PrintWriter writer = response.getWriter();
        writer.format(
                "{\"success\":%s,\"code\":\"%s\",\"message\":\"%s\"}",
                success,
                escapeJson(code),
                escapeJson(message));
    }

    private String escapeJson(String value) {
        StringBuilder escaped = new StringBuilder(value.length() + 16);
        for (int i = 0; i < value.length(); i++) {
            char character = value.charAt(i);
            switch (character) {
                case '"' -> escaped.append("\\\"");
                case '\\' -> escaped.append("\\\\");
                case '\b' -> escaped.append("\\b");
                case '\f' -> escaped.append("\\f");
                case '\n' -> escaped.append("\\n");
                case '\r' -> escaped.append("\\r");
                case '\t' -> escaped.append("\\t");
                default -> {
                    if (character < 0x20) {
                        escaped.append(String.format("\\u%04x", (int) character));
                    } else {
                        escaped.append(character);
                    }
                }
            }
        }
        return escaped.toString();
    }
}
