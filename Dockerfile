## Maven 构建并打包
#FROM maven:3.8-jdk-21 as builder
#
#WORKDIR /app
#
#COPY . .
#
## 打包
#RUN mvn clean package -DskipTests

# 构建镜像
FROM openjdk:21-jdk-slim

# 设置时区为东八区
ENV TZ=Asia/Shanghai
#RUN apt-get update && apt-get install -y curl bash tree tzdata \
#    && ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# copy 构建产物
#COPY --from=builder /app/target/*.jar /app.jar
COPY target/*.jar /app.jar

EXPOSE 8080

CMD ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "/app.jar"]
