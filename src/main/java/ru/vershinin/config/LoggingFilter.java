package ru.vershinin.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;


import java.io.BufferedReader;
import java.io.IOException;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class LoggingFilter extends OncePerRequestFilter {

    private final Logger logger = LoggerFactory.getLogger(LoggingFilter.class);

    private static final String ALREADY_FILTERED_FLAG = LoggingFilter.class.getName() + ".FILTERED";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // Проверка, был ли фильтр уже выполнен
        if (request.getAttribute(ALREADY_FILTERED_FLAG) == null) {
            // Установка флага, что фильтр был выполнен
            request.setAttribute(ALREADY_FILTERED_FLAG, true);

            // Логирование информации о запросе
            logger.info("Request received. Method: {}, Path: {}", request.getMethod(), request.getRequestURI());

            // Перехват данных из тела запроса (если это POST с данными JSON)
            if ("POST".equalsIgnoreCase(request.getMethod()) && request.getContentType() != null && request.getContentType().contains("application/json")) {
                String requestData = extractPostData(request);
                logger.info("Request data: {}", requestData);
            }

            // Перехват параметров GET-запроса
            if ("GET".equalsIgnoreCase(request.getMethod())) {
                Map<String, String[]> parameters = request.getParameterMap();
                if (!parameters.isEmpty()) {
                    String parametersInfo = parameters.entrySet().stream()
                            .map(entry -> entry.getKey() + "=" + String.join(",", entry.getValue()))
                            .collect(Collectors.joining(" "));
                    logger.info("Request parameters: {}", parametersInfo);
                }
            }

            // Продолжаем цепочку фильтров
            filterChain.doFilter(request, response);

            // Логирование информации о ответе
            logger.info("Response sent. Status: {}", response.getStatus());
        } else {
            // Просто продолжаем цепочку фильтров, так как логирование уже выполнено
            filterChain.doFilter(request, response);
        }
    }

    private String extractPostData(HttpServletRequest request) throws IOException {
        try (BufferedReader bufferedReader = request.getReader()) {
            return bufferedReader.lines().collect(Collectors.joining(System.lineSeparator()));
        }
    }
}

