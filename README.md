# bagoye-board

이 프로젝트는 Spring Boot와 React를 사용하여 개발된 게시판 애플리케이션입니다. 회원 인증, 게시물 관리, 좋아요 및 댓글 기능, 검색 및 추천 기능 등을 제공합니다.

## 목차

1. [개발환경](#개발환경)
2. [기능](#기능)
   - [인증 및 사용자 관리](#1-인증-및-사용자-관리)
   - [게시물 관리](#2-게시물-관리)
   - [좋아요 및 댓글 관리](#3-좋아요-및-댓글-관리)
   - [검색 및 추천](#4-검색-및-추천)
   - [파일 관리](#5-파일-관리)
3. [설치 및 실행](#설치-및-실행)

## 개발환경

### Backend

- **Spring Boot**
- Spring Security
- Bean Validation
- JSON Web Token (JWT)
- MySQL

### Frontend

- **NodeJS**
- React
- axios
- react-axios
- react-dom
- react-router
- react-router-dom

## 기능

### 1. 인증 및 사용자 관리

- 로그인 (signIn)
- 회원가입 (signUp)
- 유저 정보 조회 (getUser)
- 닉네임 수정 (patchNickname)
- 프로필 이미지 수정 (patchProfileImage)

### 2. 게시물 관리

- 주간 상위 3 게시물 조회 (weeklyTop3List)
- 최신 게시물 리스트 조회 (currentList)
- 검색 게시물 리스트 조회 (searchList)
- 게시물 상세 조회 (boardDetail)
- 게시물 작성 (boardWrite)
- 게시물 삭제 (boardDelete)
- 게시물 수정 (boardUpdate)
- 특정 유저 게시물 리스트 조회 (userBoardList)

### 3. 좋아요 및 댓글 관리

- 좋아요 리스트 조회 (favoriteList)
- 좋아요 기능 (putFavorite)
- 댓글 리스트 조회 (commentList)
- 댓글 쓰기 (postComment)

### 4. 검색 및 추천

- 인기 검색어 리스트 조회 (popularWordList)
- 관련 검색어 리스트 조회 (relativeWordList)

### 5. 파일 관리

- 파일 업로드 (fileUpload)
- 파일 불러오기 (getFile)

## 설치 및 실행

1. **백엔드**
    - `back` 디렉토리로 이동 후, 필요한 라이브러리 설치 및 애플리케이션 실행:
      ```bash
      ./mvnw clean install
      ./mvnw spring-boot:run
      ```
2. **프론트엔드**
    - `front` 디렉토리로 이동 후, 필요한 패키지 설치 및 애플리케이션 실행:
      ```bash
      npm install
      npm start
      ```
