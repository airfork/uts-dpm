# UTS DPM

Backend for UTS DPM. Frontend can be found [here](https://github.com/airfork/uts-dpm-frontend).

This is a Spring Boot app built with Maven, and be run/built using the maven wrapper. 
Database scripts are in [db_scripts](/db_scripts) and database settings go in [src/main/resources](/src/main/resources). 
There are a few secrets that are placed in an applications.properties file in the [config](config) directory. 
There is an [application.properties.example](config/application.properties.example) file that holds the expected structure.

Application is hosted on Heroku

When running locally, swagger docs can be accessed by visiting: http://localhost:8080/swagger-ui/index.html
