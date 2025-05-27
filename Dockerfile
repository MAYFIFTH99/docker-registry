# 1. Java 베이스 이미지 선택 (JDK 17 기준)
FROM eclipse-temurin:21-jdk-alpine

# 2. 작업 디렉토리 설정
WORKDIR /app

# 3. 로컬의 JAR 파일을 컨테이너 내부로 복사
COPY build/libs/docker-registry-0.0.1-SNAPSHOT.jar app.jar

# 4. 실행 포트 (선택적으로 열어줄 수 있음)
EXPOSE 8080

# 5. JAR 실행 명령
ENTRYPOINT ["java", "-jar", "app.jar"]
