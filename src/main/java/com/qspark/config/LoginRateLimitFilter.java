/*
 * QSpark - A production-grade smart parking management system
 * Copyright (C) 2026 Pawan Simha R
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.qspark.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Order(1)
public class LoginRateLimitFilter implements Filter {

    static final int MAX_ATTEMPTS = 5;
    static final long COOLDOWN_SECONDS = 300;

    private final Map<String, Attempt> attempts = new ConcurrentHashMap<>();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        if (!isLoginRequest(req)) {
            chain.doFilter(request, response);
            return;
        }

        String ip = req.getRemoteAddr();
        Attempt attempt = attempts.get(ip);

        if (attempt != null && attempt.isBlocked()) {
            res.setStatus(429);
            res.setContentType("text/plain");
            res.getWriter().write("Too many login attempts. Try again in 5 minutes.");
            return;
        }

        chain.doFilter(request, response);

        String param = req.getParameter("password");
        if (res.getStatus() == 302 || res.getStatus() == 200) {
            String redirect = res.getHeader("Location");
            if (redirect != null && redirect.contains("error=true")) {
                recordFailure(ip);
            } else {
                attempts.remove(ip);
            }
        }
    }

    private boolean isLoginRequest(HttpServletRequest req) {
        return "/login".equals(req.getRequestURI()) && "POST".equalsIgnoreCase(req.getMethod());
    }

    private void recordFailure(String ip) {
        Attempt a = attempts.get(ip);
        if (a == null) {
            attempts.put(ip, new Attempt(1, Instant.now()));
        } else {
            attempts.put(ip, new Attempt(a.count + 1, a.firstAttempt));
        }
    }

    static class Attempt {
        final int count;
        final Instant firstAttempt;

        Attempt(int count, Instant firstAttempt) {
            this.count = count;
            this.firstAttempt = firstAttempt;
        }

        boolean isBlocked() {
            if (count < MAX_ATTEMPTS) return false;
            return Instant.now().isBefore(firstAttempt.plusSeconds(COOLDOWN_SECONDS));
        }
    }
}
