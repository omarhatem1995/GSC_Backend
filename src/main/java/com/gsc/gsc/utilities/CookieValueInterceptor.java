package com.gsc.gsc.utilities;

import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class CookieValueInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        // Check if the handler is a method handler
        if (handler instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) handler;

            // Check if the method has the CookieValue annotation
            if (handlerMethod.getMethod().getParameterCount() > 0
                    && handlerMethod.getMethodParameters()[0].hasParameterAnnotation(CookieValue.class)) {
                // Check if the cookie is present in the request
                if (request.getCookies() == null || request.getCookies().length == 0) {
                    response.sendError(HttpServletResponse.SC_FORBIDDEN, "CookieValue is required");
                    return false;
                }
            }
        }

        // Continue with the request
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
                           ModelAndView modelAndView) throws Exception {
        // Not needed for this interceptor
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
            throws Exception {
        // Not needed for this interceptor
}
}