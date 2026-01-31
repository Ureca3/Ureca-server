# UNITY Backend Server

U+ 종합 상담 요약 서비스 **UNIFY**의 백엔드 서버입니다.  
OAuth 기반 인증, 상담 데이터 관리, AI 요약 연동을 담당합니다.

## 🔧 기술 스택

- **Language**: Java 17
- **Framework**: Spring Boot 3.x
- **Build Tool**: Gradle
- **DB**: MySQL 8.x
- **ORM / DB 접근**: MyBatis
- **Auth**: OAuth2 (Google / Kakao / Naver) + JWT
- **API 문서**: Swagger (springdoc-openapi)
- **Infra**: Docker, Docker Compose

<br>

## 🔐 인증 구조 (OAuth + JWT)
- 클라이언트 → OAuth Provider 로그인
- Authorization Code 발급
- 서버에서 Provider Access Token / Provider Refresh Token 요청 
- 사용자 정보 조회
- 서비스용 JWT Access / Refresh Token 발급
- Refresh Token은 DB에 저장 (Rotation 적용)

<br>

## 🔁 Refresh Token 전략 - (RTR: Refresh Token Rotation)
- Access Token 만료 시 /api/auth/refresh 호출
- Refresh Token 재발급 (탈취 방지를 위해 기존 토큰 무효화)

<br>

## 🛄 DataBase 
- MySQL 8.x (utf8mb4)
- 주요 테이블: users, counseling_result, summary, product, recommend
- 논리 삭제(`deleted_at`) 기반 데이터 관리
- Refresh Token Rotation 정책 적용

<br>

## ❗ 공통 에러 응답 정책
본 서버는 CustomException과 ErrorCode 기반의 공통 에러 처리 정책을 사용합니다.

- 모든 비즈니스 예외는 `CustomException`으로 처리됩니다.
- HTTP Status Code는 `ErrorCode`에 정의된 값을 사용합니다.
- 에러 응답은 아래 공통 포맷을 따릅니다.

```json
{
  "success": false,
  "errorCode": "TOKEN_EXPIRED",
  "message": "로그인이 만료되었습니다."
}
```

<br>

## 📁 폴더 계층 구조
```
src/main/java/com/ureca/unity
├─ domain
│  ├─ auth            # OAuth 로그인, JWT 발급/재발급, Refresh Token 관리
│  ├─ call            # 상담(통화) 메타데이터 및 상담 세션 관리
│  ├─ category        # 상담 카테고리 조회 및 분류 로직
│  ├─ gemini          # Gemini AI 연동 및 상담 요약 생성 처리
│  ├─ policy          # 서비스 이용약관, 개인정보 처리방침 관리
│  ├─ recommend       # 상담 요약 기반 추천 데이터 생성
│  ├─ stt             # STT(Speech-to-Text) 처리 및 음성 텍스트 변환
│  ├─ summary         # 상담 요약 도메인 (조회, 상태 관리)
│  └─ user            # 사용자 정보 관리 및 회원 관련 기능
│
├─ global
│  ├─ config          # 전역 설정 (DB, Swagger, CORS 등)
│  ├─ exception       # 공통 예외 및 전역 예외 처리
│  ├─ response        # 공통 API 응답 포맷
│  ├─ security        # Spring Security, 인증/인가 설정
│  └─ util            # 공통 유틸리티 클래스
│
└─ UnityApplication.java
```

<br>

## ▶️ 실행 방법 (Local)

### 1. 실행 프로파일
로컬 환경에서는 `local` 프로파일을 사용합니다.

```bash
spring.profiles.active=local
```

### 2. 데이터베이스 설정
로컬 MySQL이 실행 중이어야 합니다.

```text
Host: localhost
Port: 3306
Database: ureca (예시)
Username: ureca (예시)
Password: ureca (예시)
```

DB 설정은 `application-local.yml`에 정의되어 있습니다.

### 3. 서버 실행

```bash
./gradlew bootRun
```

서버는 기본적으로 아래 주소에서 실행됩니다.

```text
http://localhost:8080
```

<br>

## 📄 API 문서
- 스웨거 주소: http://localhost:8080/swagger-ui/index.html

