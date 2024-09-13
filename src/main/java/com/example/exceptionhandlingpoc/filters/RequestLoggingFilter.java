package com.example.exceptionhandlingpoc.filters;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Component
@Slf4j
public class RequestLoggingFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        request.setAttribute("request-id", UUID.randomUUID());
        var wrappedRequest = new ContentCachingRequestWrapper(request);
        var wrappedResponse = new ContentCachingResponseWrapper(response);
        filterChain.doFilter(wrappedRequest, wrappedResponse);

        // Getting request body
        byte[] requestBody = wrappedRequest.getContentAsByteArray();
        String requestBodyString = new String(requestBody, StandardCharsets.UTF_8);

        // Getting Response Body
        byte[] responseBody = wrappedResponse.getContentAsByteArray();
        String responseBodyString = new String(responseBody, StandardCharsets.UTF_8);
        wrappedResponse.copyBodyToResponse();
        log.info("""
                        \n
                        ####
                        
                        Request URI: {} {}
                        Request ID: {}
                        Request Body: {}
                        
                        --
                        
                        wResponse Status: {}
                        Response Body: {}
                        
                        ####
                        """,
                wrappedRequest.getMethod(),
                wrappedRequest.getRequestURI(),
                wrappedRequest.getAttribute("request-id"),
                requestBodyString,
                wrappedResponse.getStatus(),
                responseBodyString);
    }
}