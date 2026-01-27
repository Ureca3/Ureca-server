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

    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),
    USER_ALREADY_DELETED(HttpStatus.BAD_REQUEST, "이미 탈퇴한 사용자입니다."),

    // 기타 공용 exception code
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "입력값이 잘못되었습니다."),
    INVALID_TYPE_VALUE(HttpStatus.BAD_REQUEST, "데이터 타입이 일치하지 않습니다."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "허용되지 않은 메소드입니다."),
    NOT_FOUND(HttpStatus.NOT_FOUND, "대상을 찾을 수 없습니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 로직 오류입니다."),

    OAUTH_TOKEN_NOT_FOUND(HttpStatus.BAD_REQUEST, "소셜 토큰이 없어 연결 해제를 진행할 수 없습니다. 다시 로그인 후 탈퇴해주세요."),
    SOCIAL_UNLINK_FAILED(HttpStatus.BAD_GATEWAY, "소셜 연결 해제에 실패했습니다. 잠시 후 다시 시도해주세요."),

    AUTH_STORAGE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "소셜 로그인 정보 저장에 실패했습니다. 잠시 후 다시 시도해주세요.");

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
