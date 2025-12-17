#!/bin/bash

echo "🐳 Ejecutando tests con Docker + Maven..."

docker run --rm \
  -v "$(pwd)":/app \
  -w /app \
  maven:3.9-eclipse-temurin-17 \
  mvn clean test
