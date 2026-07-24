package com.example.urlshortener.api;

import com.example.urlshortener.exception.DuplicateAliasException;
import com.example.urlshortener.exception.InvalidUrlException;
import com.example.urlshortener.exception.ReservedAliasException;
import com.example.urlshortener.exception.UrlExpiredException;
import com.example.urlshortener.exception.UrlNotFoundException;
import com.example.urlshortener.metrics.UrlMetrics;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private final UrlMetrics metrics;

    public GlobalExceptionHandler(UrlMetrics metrics) {
        this.metrics = metrics;
    }

    @ExceptionHandler(UrlNotFoundException.class)
    public ProblemDetail handleNotFound(UrlNotFoundException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problem.setProperty("code", "URL_NOT_FOUND");
        return problem;
    }

    @ExceptionHandler(UrlExpiredException.class)
    public ProblemDetail handleExpired(UrlExpiredException ex) {
        metrics.incrementRedirectError("EXPIRED");
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.GONE, ex.getMessage());
        problem.setProperty("code", "URL_EXPIRED");
        return problem;
    }

    @ExceptionHandler(DuplicateAliasException.class)
    public ProblemDetail handleDuplicateAlias(DuplicateAliasException ex) {
        metrics.incrementUrlCreationError("DUPLICATE_ALIAS");
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        problem.setProperty("code", "DUPLICATE_ALIAS");
        return problem;
    }

    @ExceptionHandler({InvalidUrlException.class, ReservedAliasException.class})
    public ProblemDetail handleBadRequest(RuntimeException ex) {
        metrics.incrementUrlCreationError("VALIDATION_ERROR");
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        problem.setProperty("code", "VALIDATION_ERROR");
        return problem;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        metrics.incrementUrlCreationError("VALIDATION_ERROR");
        List<Map<String, String>> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> Map.of("field", fe.getField(), "message", fe.getDefaultMessage()))
                .collect(Collectors.toList());
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Validation failed");
        problem.setProperty("code", "VALIDATION_ERROR");
        problem.setProperty("fieldErrors", fieldErrors);
        return problem;
    }
}
