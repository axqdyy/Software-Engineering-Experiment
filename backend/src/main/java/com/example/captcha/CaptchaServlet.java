package com.example.captcha;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.InputStream;
import java.security.SecureRandom;

/**
 * 后端验证码图片接口。
 */
@WebServlet(name = "CaptchaServlet", urlPatterns = "/api/captcha/*")
public class CaptchaServlet extends HttpServlet {

    public static final String SESSION_ATTRIBUTE = "loginCaptcha";
    public static final long VALIDITY_MILLIS = 5 * 60 * 1000L;
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final CaptchaOption[] CAPTCHA_OPTIONS = {
            new CaptchaOption("A7K2", "/WEB-INF/captcha/01.png"),
            new CaptchaOption("M4QX", "/WEB-INF/captcha/02.png"),
            new CaptchaOption("R8ZN", "/WEB-INF/captcha/03.png"),
            new CaptchaOption("P3VT", "/WEB-INF/captcha/04.png"),
            new CaptchaOption("W6HC", "/WEB-INF/captcha/05.png"),
            new CaptchaOption("F9JB", "/WEB-INF/captcha/06.png"),
            new CaptchaOption("T2YL", "/WEB-INF/captcha/07.png"),
            new CaptchaOption("D5KS", "/WEB-INF/captcha/08.png"),
            new CaptchaOption("H7PU", "/WEB-INF/captcha/09.png"),
            new CaptchaOption("N4XR", "/WEB-INF/captcha/10.png")
    };

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        CaptchaOption option = CAPTCHA_OPTIONS[RANDOM.nextInt(CAPTCHA_OPTIONS.length)];
        request.getSession(true)
                .setAttribute(SESSION_ATTRIBUTE, new CaptchaChallenge(option.code(), System.currentTimeMillis()));

        response.setContentType("image/png");
        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0L);

        try (InputStream image =
                     getServletContext().getResourceAsStream(option.imagePath())) {
            if (image == null) {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                return;
            }
            image.transferTo(response.getOutputStream());
        }
    }

    private record CaptchaOption(String code, String imagePath) {
    }

    /**
     * 会话中保存的验证码及其签发时间。
     */
    public record CaptchaChallenge(String code, long issuedAtMillis) {

        public boolean isExpired(long nowMillis) {
            return nowMillis - issuedAtMillis > VALIDITY_MILLIS;
        }
    }
}
