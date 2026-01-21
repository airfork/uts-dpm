#!/bin/bash

# Start the Spring Boot application

PROFILE="${1:-local}"

# Build JVM args from .env file
JVM_ARGS=""
if [ -f .env ]; then
    while IFS='=' read -r key value; do
        # Skip comments and empty lines
        [[ "$key" =~ ^#.*$ || -z "$key" ]] && continue
        JVM_ARGS="$JVM_ARGS -D$key=$value"
    done < .env
fi

echo "Starting UTS-DPM with profile: $PROFILE"
./mvnw spring-boot:run -Dspring-boot.run.profiles="$PROFILE" -Dspring-boot.run.jvmArguments="$JVM_ARGS"
