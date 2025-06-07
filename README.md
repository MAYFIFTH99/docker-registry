
# 🐳 사설 도커 이미지 관리 서비스 (Proxy 기반)


---

## 📌 프로젝트 개요

Docker Registry를 기반으로 한 사설 이미지 저장소에 프록시 서버(Spring Boot)를 구성하고, RESTful API 및 Docker CLI를 통해 다음 기능을 제공합니다:

- 이미지 등록(push) / 다운로드(pull)
- 이미지 및 태그 목록 조회
- 특정 태그 또는 이미지 전체 삭제
- 사용자 인증 (Basic Auth 기반)
- 감사 로그 기록 및 사용자/이미지별 조회

---

## 🏗️ 시스템 아키텍처

![image](https://github.com/user-attachments/assets/fc80c735-4e4a-42c9-ba4a-b65c6a198326)


- 프론트엔드 없이 REST API 기반
- 사용자 요청 → Spring Proxy Server → Docker Registry
- 이미지 메타/레이어 프록시 요청 + DB 감사 로그 기록

---

## ⚙️ 개발 환경 및 실행 방법

### 개발 환경

- OS: Ubuntu 22.04
- Java: 21
- Spring Boot: 3.2+
- Docker: 28.0.4
- Docker Compose: v2.x

### 실행 방법 (Clean Ubuntu 기준)

```bash
sudo apt update
sudo apt install -y docker.io docker-compose openjdk-21-jdk
sudo vim /etc/docker/daemon.json
````

```json
{
  "insecure-registries": ["localhost:5000", "localhost:8080"]
}
```

```bash
sudo systemctl restart docker
sudo usermod -aG docker $USER
newgrp docker

git clone https://github.com/MAYFIFTH99/docker-registry.git
cd docker-registry
chmod +x ./gradlew
./gradlew build
docker-compose up -d

docker login localhost:8080
# 아이디: test / 비밀번호: 1234
```

---

## ✅ 주요 기능

### 🔐 사용자 관리 API

![image](https://github.com/user-attachments/assets/ec2089ff-23b6-4ed1-bc27-285f8181d589)


* `POST /api/users` : 사용자 생성
* `GET /api/users` : 전체 사용자 조회
* `DELETE /api/users/{username}` : 사용자 삭제
* 인증은 `htpasswd` 기반 Basic Auth

### 📦 이미지 및 태그 관리

![image](https://github.com/user-attachments/assets/957f46e8-516b-4eae-ad7b-e115dcd33cab)
![image](https://github.com/user-attachments/assets/56480856-8151-4616-8405-9e94d8538e27)
 ![image](https://github.com/user-attachments/assets/b62d8e8e-5b64-4d0a-9634-eefd4112b4e8)


* `GET /api/images` : 전체 이미지 목록 조회 (`filter` 쿼리 지원)
* `GET /api/images/{name}/tags` : 특정 이미지의 태그 조회
* `DELETE /api/images/{name}` : 이미지 전체 삭제 (모든 태그 삭제)
* `DELETE /api/images/{name}/tags/{tag}` : 특정 태그 삭제

#### 🧩 태그 삭제 방식 설명

* `HEAD /v2/{name}/manifests/{tag}` → digest 조회
* `DELETE /v2/{name}/manifests/{digest}` → manifest 삭제 → tag 제거 효과

### 📝 감사 로그 API

* `GET /api/audit/user?username=test` : 특정 사용자 활동 조회
* `GET /api/audit/image?image=busybox` : 특정 이미지 활동 조회

**로깅 항목 (총 6개):**

* `VIEW_ALL_IMAGES`
* `VIEW_TAGS`
* `DELETE_IMAGE_TAG`
* `DELETE_IMAGE_ALL_TAGS`
* `PUSH_IMAGE`
* `PULL_IMAGE`

> 스프링 AOP 기반으로 서비스 계층 메서드 실행 후 자동 기록
> 사용자명은 Basic Auth에서 추출하거나 없을 경우 anonymous

---

## 🧪 테스트

### 🔐 사용자 API

```bash
curl -X GET 'http://localhost:8080/api/users'
curl -X POST 'http://localhost:8080/api/users?username=minseok&password=minseok' \
  -H 'Authorization: Basic dGVzdDoxMjM0'
curl -X DELETE 'http://localhost:8080/api/users/minseok' \
  -H 'Authorization: Basic dGVzdDoxMjM0'
```

### 📦 이미지 API

```bash
curl -X GET 'http://localhost:8080/api/images' \
  -H 'Authorization: Basic dGVzdDoxMjM0'
curl -X GET 'http://localhost:8080/api/images?filter=alpine' \
  -H 'Authorization: Basic dGVzdDoxMjM0'
curl -X GET 'http://localhost:8080/api/images/busybox/tags' \
  -H 'Authorization: Basic dGVzdDoxMjM0'
curl -X DELETE 'http://localhost:8080/api/images/busybox' \
  -H 'Authorization: Basic dGVzdDoxMjM0'
curl -X DELETE 'http://localhost:8080/api/images/busybox/tags/latest' \
  -H 'Authorization: Basic dGVzdDoxMjM0'
```

### 📝 감사 로그 API

```bash
curl -X GET 'http://localhost:8080/api/audit/user?username=test' \
  -H 'Authorization: Basic dGVzdDoxMjM0'
curl -X GET 'http://localhost:8080/api/audit/image?image=busybox' \
  -H 'Authorization: Basic dGVzdDoxMjM0'
```

### 🐳 Docker CLI

```bash
docker login localhost:8080
docker push localhost:8080/alpine:latest
docker pull localhost:8080/alpine:latest
```

---

## 📂 프로젝트 구조

```
docker-registry/
├── docker-compose.yml
├── Dockerfile
├── src/
│   └── main/
│       └── java/
│           └── opensource/
│               └── dockerregistry/
│                   └── backend/
│                       ├── aop/
│                       │   └── AuditLogAspect.java
│                       ├── config/
│                       │   ├── RestTemplateConfig.java
│                       │   ├── SecurityConfig.java
│                       │   └── SwaggerConfig.java
│                       ├── controller/
│                       │   ├── AuditLogController.java
│                       │   ├── ImageController.java
│                       │   ├── RegistryProxyController.java
│                       │   └── UserController.java
│                       ├── dto/
│                       │   ├── AuditLogRequestDto.java
│                       │   ├── DeleteTagsResponse.java
│                       │   ├── ImageActivityResponse.java
│                       │   ├── TagListResponse.java
│                       │   └── UserActivityResponse.java
│                       ├── entity/
│                       │   └── AuditLogEntity.java
│                       ├── repository/
│                       │   └── AuditLogRepository.java
│                       ├── service/
│                       │   ├── AuditLogService.java
│                       │   ├── HtpasswdUserDetailsService.java
│                       │   ├── ImageService.java
│                       │   └── UserService.java
│                       ├── util/
│                       │   └── UserUtils.java
│                       └── DockerRegistryApplication.java


```

---

