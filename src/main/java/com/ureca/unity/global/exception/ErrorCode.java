package com.ureca.unity.global.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {

    TOKEN_MISSING(HttpStatus.UNAUTHORIZED, "토큰이 없습니다."),
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "로그인이 만료되었습니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),

    REFRESH_TOKEN_MISSING(HttpStatus.UNAUTHORIZED, "리프레시 토큰이 없습니다."),
    REFRESH_TOKEN_INVALID(HttpStatus.UNAUTHORIZED, "유효하지 않은 리프레시 토큰입니다."),
    REFRESH_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "리프레시 토큰이 만료되었습니다."),

    INVALID_OAUTH_PROVIDER(HttpStatus.BAD_REQUEST,"지원하지 않는 OAuth 제공자입니다."),

    // 기타 공용 exception code
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST,"입력값이 잘못되었습니다."),
    INVALID_TYPE_VALUE(HttpStatus.BAD_REQUEST,"데이터 타입이 일치하지 않습니다."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED,"허용되지 않은 메소드입니다."),
    NOT_FOUND(HttpStatus.NOT_FOUND,"대상을 찾을 수 없습니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR,"서버 내부 로직 오류입니다.");



    private final HttpStatus status;
    private final String message;

    ErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }
}
