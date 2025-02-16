# Build stage
FROM maven:3.8-openjdk-11 AS builder

# Install git with signature verification disabled
RUN apt-get update --allow-insecure-repositories && \
    apt-get install -y --allow-unauthenticated git && \
    rm -rf /var/lib/apt/lists/*

# Set working directory
WORKDIR /build

# Clone repository with submodules
# RUN git clone --recurse-submodules https://github.com/faa-swim/fns-client .
ADD . .

# Install submodule dependencies
RUN mvn clean install -f ./aixm-5.1/pom.xml && \
    mvn clean install -f ./jms-client/pom.xml && \
    mvn clean install -f ./swim-utilities/pom.xml

# Build main application
RUN mvn clean package

# Runtime stage
FROM openjdk:11-jre-slim

# Set working directory
WORKDIR /app

# Create necessary directories

# Copy default config file

# Copy built application from builder stage
COPY --from=builder /build/target /app

COPY --from=builder /build/templates/fnsClient.conf /app/fns-client/fnsClient.conf
COPY --from=builder /build/CLIENT_CERT /app/fns-client/CLIENT_CERT

RUN mkdir -p /app/fil

# Expose REST API port
EXPOSE 8080

# Change to fns-client directory before running
WORKDIR /app/fns-client

# Set entrypoint
ENTRYPOINT ["java", "-jar", "FnsClient.jar"]
