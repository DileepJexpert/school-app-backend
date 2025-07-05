# Use an official OpenJDK runtime as a parent image
FROM openjdk:17-jdk-slim

# Set the working directory in the container
WORKDIR /app

# Copy the fat jar into the container at /app
# The 'target/*.jar' path assumes you are using Maven. Adjust if using Gradle ('build/libs/*.jar').
COPY target/*.jar app.jar

# Make port 8080 available to the world outside this container
# Render's free plan uses port 10000, but it will map it correctly.
EXPOSE 8080

# Run the jar file
ENTRYPOINT ["java","-jar","app.jar"]