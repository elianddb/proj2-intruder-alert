# Commence Project 2!!!
## Run the program
Download the .jar file from the releases on the right hand side under the About section. Then run the following command once you traverse to the directory containing the file:

```sh
java -cp proj2-intruder-alert-1.0-jar-with-dependencies.jar org.cs440.Driver

```
## If you want to compile the project yourself ...
### VSCode
Make sure you have the following extensions:
- Language Support for Java(TM) by Red Hat
- Maven for Java
- Project Manager for Java
- Test Runner for Java

Then once you've cloned the repo press the Run button at the top right while on the `App.java` file.

### General
Download and setup [Maven](https://maven.apache.org/install.html) with the necessary environment variables.

Navigate to root directory and run the following commands to setup the maven project and run the program.
```sh
mvn clean compile assembly:single
java -cp proj2-intruder-alert-1.0-jar-with-dependencies.jar org.cs440.Driver
```

