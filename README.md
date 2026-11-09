# bagoye-board

Spring Boot와 React로 만든 게시판 애플리케이션입니다. 회원 인증, 게시물·댓글·좋아요, 검색어 추천, 이미지 업로드 기능을 제공합니다.

기본 기능 구현 이후 **OWASP Top 10 관점에서 백엔드 보안을 점검하고 강화**했으며, 그 내역을 [보안 강화 내역](#보안-강화-내역)에 정리했습니다.

## 목차

1. [개발 환경](#개발-환경)
2. [기능](#기능)
3. [설치 및 실행](#설치-및-실행)
4. [보안 강화 내역](#보안-강화-내역)
5. [보안 이벤트 로그 API](#보안-이벤트-로그-api)
6. [테스트](#테스트)
7. [알려진 한계](#알려진-한계)
8. [비밀값 노출 대응 현황](#비밀값-노출-대응-현황)

## 개발 환경

### Backend

- Java 17, **Spring Boot 3.3**, Gradle
- Spring Security, Bean Validation
- Spring Data JPA (Hibernate 6), MySQL 8
- JWT (jjwt 0.11)
- 테스트: JUnit 5, MockMvc, spring-security-test, H2

### Frontend

- React 18, TypeScript
- axios, react-router-dom

## 기능

### 1. 인증 및 사용자 관리

- 회원가입, 로그인 (Access Token + Refresh Token 발급)
- Access Token 재발급 (`POST /api/v1/auth/refresh`)
- 유저 정보 조회, 닉네임 수정, 프로필 이미지 수정

### 2. 게시물 관리

- 주간 상위 3 게시물, 최신 게시물, 검색 게시물, 특정 유저 게시물 목록 조회
- 게시물 상세 조회, 작성, 수정, 삭제 (작성자 본인만 수정·삭제 가능)

### 3. 좋아요 및 댓글

- 좋아요 목록 조회, 좋아요 토글
- 댓글 목록 조회, 댓글 작성

### 4. 검색 및 추천

- 인기 검색어, 관련 검색어 조회

### 5. 파일 관리

- 이미지 업로드 (jpg, jpeg, png, gif, webp / 최대 10MB)
- 이미지 조회

### 6. 관리자 (ADMIN 전용)

- 보안 이벤트 목록 조회 (기간·유형 필터, 페이징)
- 보안 이벤트 통계 (일별·유형별 건수, IP별 상위 10개)

## 설치 및 실행

### 사전 준비

- JDK 17
- MySQL 8
- Node.js (프론트엔드)

### 1. 데이터베이스 준비

```sql
CREATE DATABASE db_bagoye DEFAULT CHARACTER SET utf8mb4;
CREATE USER 'bagoye'@'localhost' IDENTIFIED BY '<비밀번호>';
GRANT ALL PRIVILEGES ON db_bagoye.* TO 'bagoye'@'localhost';
```

테이블은 `spring.jpa.hibernate.ddl-auto: update` 설정으로 실행 시 자동 생성됩니다.
기존 DB를 업그레이드하는 경우에는 [기존 DB 마이그레이션](#기존-db-마이그레이션)을 먼저 실행하세요.

### 2. 환경변수 설정

비밀값은 코드에 두지 않고 환경변수로 주입합니다. **필수 값이 없으면 애플리케이션이 시작되지 않습니다.**

| 변수 | 필수 | 설명 | 기본값 |
|---|---|---|---|
| `DB_URL` | | MySQL 접속 URL | `jdbc:mysql://localhost:3306/db_bagoye?...` |
| `DB_USERNAME` | ✅ | DB 계정 | |
| `DB_PASSWORD` | ✅ | DB 비밀번호 | |
| `JWT_SECRET_KEY` | ✅ | JWT 서명 키 (**32바이트 이상**, 미만이면 기동 실패) | |
| `FILE_PATH` | ✅ | 업로드 파일 저장 디렉토리 (끝에 `/` 포함) | |
| `FILE_URL` | | 업로드 파일 접근 URL 접두어 | `http://localhost:8080/file/` |
| `CORS_ALLOWED_ORIGINS` | | 허용할 프론트엔드 출처 (쉼표 구분, `*` 불가) | `http://localhost:3000` |

변수 목록은 루트의 `.env.example`에도 정리되어 있습니다. Spring Boot는 `.env` 파일을 자동으로 읽지 않으므로 셸 환경변수나 IDE 실행 설정으로 주입하세요. 값을 채운 `.env` 파일은 `.gitignore`로 커밋에서 제외됩니다.

JWT 키 생성 예시:

```bash
openssl rand -base64 48
```

macOS / Linux / Git Bash:

```bash
export DB_USERNAME=bagoye
export DB_PASSWORD='<비밀번호>'
export JWT_SECRET_KEY='<생성한 키>'
export FILE_PATH=/path/to/upload/
```

Windows PowerShell:

```powershell
$env:DB_USERNAME = "bagoye"
$env:DB_PASSWORD = "<비밀번호>"
$env:JWT_SECRET_KEY = "<생성한 키>"
$env:FILE_PATH = "C:/bagoye/upload/"
```

### 3. 백엔드 실행

```bash
cd back
./gradlew bootRun        # Windows: gradlew.bat bootRun
```

서버는 `http://localhost:8080`에서 실행됩니다.

### 4. 프론트엔드 실행

```bash
cd front
npm install
npm start
```

`http://localhost:3000`에서 실행됩니다.

### 5. 관리자 계정 지정

관리자 API를 사용하려면 가입한 계정의 역할을 DB에서 변경합니다. (회원가입 API로는 항상 `USER`로만 가입됩니다.)

```sql
UPDATE user SET role = 'ADMIN' WHERE email = 'admin@example.com';
```

### 기존 DB 마이그레이션

보안 강화 이전 버전의 DB를 사용 중이라면 아래 SQL을 실행합니다.

```sql
ALTER TABLE user
    ADD COLUMN failed_login_count INT         NOT NULL DEFAULT 0,
    ADD COLUMN locked_until       DATETIME    NULL,
    ADD COLUMN role               VARCHAR(10) NOT NULL DEFAULT 'USER';

CREATE TABLE refresh_token (
    token_hash       CHAR(64)     NOT NULL PRIMARY KEY,
    user_email       VARCHAR(255) NOT NULL,
    expired_datetime DATETIME     NOT NULL,
    INDEX idx_refresh_token_user_email (user_email)
);

CREATE TABLE security_event (
    id          BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    event_type  VARCHAR(30)  NOT NULL,
    email       VARCHAR(100) NULL,
    ip_address  VARCHAR(45)  NULL,
    request_uri VARCHAR(255) NULL,
    detail      VARCHAR(255) NULL,
    created_at  DATETIME     NOT NULL,
    INDEX idx_security_event_created_at (created_at),
    INDEX idx_security_event_type_created_at (event_type, created_at),
    INDEX idx_security_event_ip_created_at (ip_address, created_at)
);
```

## 보안 강화 내역

코드 위치는 `back/src/main/java/com/bagoye/board/` 기준입니다.

| 위협 | 기존 상태 | 대응 | 관련 코드 위치 |
|---|---|---|---|
| **비밀값 노출** | `application.yml`에 DB 비밀번호와 JWT 키가 평문으로 커밋됨 | 환경변수로 분리하고 기본값을 두지 않아 미설정 시 기동 실패. 기존 값의 교체·히스토리 정리 상태는 [비밀값 노출 대응 현황](#비밀값-노출-대응-현황) 참고 | `resources/application.yml` |
| **JWT 위조** (약한 서명 키) | 키 길이 검증 없음. 검증 실패를 `printStackTrace()`로만 출력 | 키가 32바이트(HS256 권장 길이) 미만이면 기동 시 예외. 실패를 만료/서명 오류/형식 오류로 구분해 기록 | `provider/JwtProvider.java` |
| **탈취 토큰 장기 악용** | 유효기간 1시간인 단일 토큰 | Access Token 30분 + Refresh Token 7일. Refresh Token은 난수 값의 SHA-256 해시만 DB에 저장하고, 재발급 시마다 폐기·재발급(1회용 회전). 삭제된 행 수를 확인해 동시 재사용 차단 | `provider/RefreshTokenProvider.java`, `service/implement/AuthServiceImplement.java` (`refresh`) |
| **CORS 오용** | 모든 출처 허용 (`*`) | 환경변수에 지정한 출처만 허용. `*` 설정 시 기동 실패. 허용 메서드·헤더를 필요한 것으로 제한 | `config/WebSecurityConfig.java` |
| **무차별 대입 / 크리덴셜 스터핑** | 로그인 시도 횟수 제한 없음 | 5회 연속 실패 시 15분 잠금 (응답 코드 `AL`). 잠긴 동안에는 비밀번호를 대조하지 않음. `SELECT ... FOR UPDATE`로 병렬 요청을 이용한 횟수 우회 방지 | `entity/UserEntity.java`, `repository/UserRepository.java`, `service/implement/AuthServiceImplement.java` (`signIn`) |
| **계정 열거** | 이메일 없음과 비밀번호 틀림을 같은 응답(`SF`)으로 처리 (기존부터 적용) | 응답 통일 유지. 존재하지 않는 이메일에도 더미 해시로 BCrypt 비교를 수행해 응답 시간 차이를 줄임 | `service/implement/AuthServiceImplement.java` (`signIn`) |
| **악성 파일 업로드** (웹셸, 위장 파일) | 확장자 검사 없음, 최대 100MB | 확장자 화이트리스트(jpg/jpeg/png/gif/webp, 대소문자 무시). 파일 앞부분의 매직 바이트가 확장자 형식과 일치하는지 검사. 저장 파일명은 UUID와 서버가 정한 확장자. 최대 10MB | `common/ImageFileType.java`, `service/implement/FileServiceImplement.java` (`upload`), `resources/application.yml` |
| **Path Traversal** | 요청 파일명을 저장 경로에 그대로 연결 | `..`, `/`, `\`, NULL 문자가 포함된 파일명 거부. `normalize()`한 경로가 업로드 폴더 밖이면 거부. 이미지 확장자만 제공. 응답 Content-Type을 확장자에 맞게 고정 | `service/implement/FileServiceImplement.java` (`getImage`), `controller/FileController.java` |
| **권한 상승** | 인증된 사용자 모두 동일 권한 (`NO_AUTHORITIES`) | `USER`/`ADMIN` 역할 도입. 역할은 토큰이 아닌 DB에서 매 요청 조회(권한 회수 즉시 반영). `/api/v1/admin/**`은 ADMIN만 허용. 권한 부족 시 403과 `NP` 응답 | `common/Role.java`, `filter/JwtAuthenticationFilter.java`, `config/WebSecurityConfig.java`, `config/NoPermissionAccessDeniedHandler.java` |
| **공격 탐지 불가** | 차단·실패 시 콘솔 출력만 있음 | 로그인 실패, 계정 잠금, 토큰 위조, 업로드 차단, 경로 조작, 권한 거부(남의 게시글 수정·삭제 시도 포함)를 DB에 보안 이벤트로 저장. 관리자용 조회·통계 API 제공 | `audit/SecurityEventRecorder.java`, `controller/AdminController.java`, `service/implement/AdminServiceImplement.java` |
| **로그 인젝션** | 해당 기능 없음 | 이벤트에 기록하는 값의 제어문자(개행 등)를 치환하고 길이를 제한 | `audit/SecurityEventRecorder.java` (`clean`) |
| **로그 기록으로 인한 장애** | 해당 기능 없음 | 이벤트 저장은 별도 스레드에서 비동기 처리(큐 1000건, 초과 시 폐기). 기록 실패가 로그인 등 본 기능에 영향을 주지 않음 | `audit/SecurityEventRecorder.java` |
| **SQL Injection** | **이미 방어됨.** 모든 DB 접근이 Spring Data JPA 파생 쿼리 또는 `@Query`의 파라미터 바인딩(`?1`, `:param`)으로 이루어지고, 사용자 입력을 SQL 문자열에 이어 붙이는 코드가 없음 | 변경 없음. 새로 추가한 쿼리(보안 이벤트 조회·통계)도 같은 원칙으로 작성하고, 정렬 조건은 사용자 입력이 아닌 서버 고정값 사용 | `repository/*.java` |

### 응답 코드 추가

기존 `ResponseDto`(`code`, `message`) 형식을 유지하며 다음 코드를 추가했습니다.

| 코드 | HTTP | 의미 |
|---|---|---|
| `AL` | 401 | Account Locked (로그인 5회 실패로 잠금) |

권한 부족(403)은 기존 `NP` 코드를 사용합니다.

## 보안 이벤트 로그 API

모두 **ADMIN 역할**의 Access Token이 필요합니다. USER 토큰이면 `403 NP`, 토큰이 없으면 `401`로 거부됩니다.

### 이벤트 유형

| 유형 | 기록 시점 |
|---|---|
| `LOGIN_FAILED` | 로그인 실패 (없는 이메일, 비밀번호 불일치) |
| `ACCOUNT_LOCKED` | 계정 잠금 발생, 잠금 중 로그인 시도 |
| `INVALID_TOKEN` | JWT 서명·형식 오류, 사용됐거나 알 수 없는 Refresh Token |
| `UPLOAD_BLOCKED` | 허용되지 않은 확장자, 시그니처 불일치, 크기 초과 |
| `FILE_ACCESS_BLOCKED` | 경로 조작 패턴, 업로드 폴더 밖 접근 |
| `ACCESS_DENIED` | 역할 부족(403), 남의 게시글 수정·삭제 시도 |

토큰 없는 요청(401)과 Access Token 만료는 정상 사용 중에도 자주 발생하므로 기록하지 않습니다.

### 1. 이벤트 목록 조회

```
GET /api/v1/admin/security-events?from=2026-11-01&to=2026-11-09&type=LOGIN_FAILED&page=0&size=20
Authorization: Bearer <ADMIN_ACCESS_TOKEN>
```

| 파라미터 | 필수 | 설명 |
|---|---|---|
| `from`, `to` | | 조회 기간 (`yyyy-MM-dd`, `to` 당일 포함). 생략 시 최근 7일, 최대 90일 |
| `type` | | 이벤트 유형. 생략 시 전체 |
| `page` | | 페이지 번호 (기본 0) |
| `size` | | 페이지 크기 (기본 20, 최대 100) |

응답 예시:

```json
{
  "code": "SU",
  "message": "Success.",
  "securityEventList": [
    {
      "id": 42,
      "eventType": "LOGIN_FAILED",
      "email": "user@example.com",
      "ipAddress": "203.0.113.10",
      "requestUri": "/api/v1/auth/sign-in",
      "detail": "bad-credentials, failedCount=3",
      "createdAt": "2026-11-09 14:21:07"
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 1,
  "totalPages": 1
}
```

기간이나 페이지 범위를 벗어나거나 잘못된 `type`·날짜 형식이면 `400 VF`로 응답합니다.

### 2. 이벤트 통계

```
GET /api/v1/admin/security-events/stats?from=2026-11-03&to=2026-11-09
Authorization: Bearer <ADMIN_ACCESS_TOKEN>
```

응답 예시:

```json
{
  "code": "SU",
  "message": "Success.",
  "from": "2026-11-03",
  "to": "2026-11-09",
  "dailyCountList": [
    { "date": "2026-11-08", "eventType": "LOGIN_FAILED", "count": 12 },
    { "date": "2026-11-08", "eventType": "ACCOUNT_LOCKED", "count": 2 },
    { "date": "2026-11-09", "eventType": "UPLOAD_BLOCKED", "count": 1 }
  ],
  "typeCountList": [
    { "name": "LOGIN_FAILED", "count": 12 },
    { "name": "ACCOUNT_LOCKED", "count": 2 },
    { "name": "UPLOAD_BLOCKED", "count": 1 }
  ],
  "topIpList": [
    { "name": "203.0.113.10", "count": 13 },
    { "name": "198.51.100.7", "count": 2 }
  ]
}
```

`topIpList`는 이벤트 발생 건수 기준 상위 10개 IP입니다.

## 테스트

```bash
cd back
./gradlew test
```

테스트는 `test` 프로파일의 H2 인메모리 DB(MySQL 호환 모드)로 실행되므로 MySQL 없이 동작합니다.

| 테스트 | 검증 내용 |
|---|---|
| `LoginLockTest` | 5회 실패 시 잠금(`AL`), 잠금 중 올바른 비밀번호도 거부, 성공 시 실패 횟수 초기화 |
| `FileSecurityTest` | `.jsp` 업로드 거부, 확장자만 `.png`인 텍스트 파일 거부, 실제 PNG는 허용. 서비스와 HTTP 양쪽에서 `../` 경로 조작 거부 |
| `AdminAccessTest` | USER 토큰으로 관리자 API 접근 시 `403 NP`, 토큰 없으면 401, ADMIN이면 200 |

각 차단 테스트에는 정상 요청이 통과하는지 확인하는 대조군을 함께 두었습니다.

## 알려진 한계

구현하지 않았거나 의도적으로 남겨 둔 부분입니다.

- **업로드 인증 없음:** `/file/upload`는 로그인 없이 호출할 수 있습니다. 업로드 파일 자체는 검증하지만 업로드 남용은 막지 못합니다.
- **프론트엔드는 Refresh Token 미사용:** 프론트엔드가 재발급 API를 아직 호출하지 않아 30분마다 다시 로그인해야 합니다.
- **계정 잠금의 부작용:**
  - 타인의 이메일로 일부러 실패를 반복해 해당 계정을 잠글 수 있습니다.
  - 존재하지 않는 이메일은 잠기지 않으므로, 반복 시도로 `AL` 응답이 나오는지 보면 가입 여부를 추정할 수 있습니다.
  - IP 기반 요청 제한(rate limiting)은 구현하지 않았습니다.
- **프록시 환경의 IP:** 보안 이벤트의 IP는 `request.getRemoteAddr()` 값입니다. 리버스 프록시 뒤에서는 프록시 IP가 기록됩니다. 위조 가능한 `X-Forwarded-For` 헤더는 의도적으로 신뢰하지 않습니다.
- **이벤트 보관 정책 없음:** 오래된 보안 이벤트를 자동 삭제하는 기능이 없습니다.
- **비동기 기록의 유실 가능성:** 이벤트가 폭주해 큐가 가득 차면 초과 이벤트는 버려집니다(가용성 우선 설계).
- **통계 API는 MySQL 전용:** 통계 쿼리가 MySQL 함수(`DATE_FORMAT`)를 사용해 H2 기반 자동 테스트로는 검증하지 않았습니다.
- **관리자 화면 미구현:** 보안 이벤트는 API로만 조회할 수 있습니다.

## 비밀값 노출 대응 현황

과거 커밋의 `back/src/main/resources/application.yml`과, 실수로 커밋되었던 빌드 산출물 `back/build/resources/main/application.yml`에 MySQL 비밀번호와 JWT 서명 키(이전 키 포함)가 평문으로 남아 있었습니다. 최초 커밋부터 환경변수 분리 직전 커밋까지 해당됩니다.

### 완료한 조치

- DB 비밀번호와 JWT 키를 환경변수(`DB_PASSWORD`, `JWT_SECRET_KEY`)로 분리하고 기본값을 제거
- 현재 코드에 기존 비밀값이 남아 있지 않음을 확인 (테스트 프로파일의 JWT 키는 H2 테스트 전용 더미 값)
- `.gitignore`에 `.env`, `.env.*`, `application-local.yml`, 인증서·키스토어 파일 추가
- 비밀값 없는 환경변수 템플릿 `.env.example` 추가
- **로컬 개발 DB 비밀번호 교체** (2026-11-09): 교체 전 기존 비밀번호가 여전히 유효했음을 확인. 교체 후 새 값으로 앱 기동 확인
- **JWT 서명 키 교체** (2026-11-09): 새 키로 앱 기동 확인. 기존 키로 서명된 토큰은 무효
- **Git 히스토리 정리** (2026-11-09): `git-filter-repo --replace-text`로 모든 과거 커밋의 비밀값을 `${DB_PASSWORD}`, `${JWT_SECRET_KEY}` 자리표시자로 치환하고 `main`, `security-hardening`에 강제 푸시. 원격 저장소를 새로 클론해 기존 비밀값이 0건임을 확인. 커밋 메시지·작성자·날짜와 최신 코드는 변경되지 않음(커밋 해시는 변경됨)

### 남아 있는 위험

- **운영·배포 환경:** 배포 서버나 별도 DB가 있었다면 해당 환경의 비밀번호와 키는 이 조치로 교체되지 않았습니다.
- **외부 사본:** 재작성 전에 만들어진 클론·포크와, GitHub이 캐시한 옛 커밋(해시를 알면 조회 가능)에는 원본이 남아 있을 수 있습니다. 옛 값은 이미 교체되어 무효이지만, 완전 삭제가 필요하면 GitHub Support에 캐시 삭제를 요청해야 합니다.
- **DB 사용자명:** 과거 커밋에 DB 계정 이름은 남아 있습니다(비밀값은 아님).
