This is a Maven/Eclipse Java project for demonstrating a command line Java calculator with JUnit test cases and logging. Continuous integration is managed by CodeShip and Travis-CI.

Build Status:

CodeShip: <img src="https://codeship.com/projects/51ba43d0-d7e8-0133-0c37-1eae90b9310e/status?branch=master" alt="CodeShip Status 51ba43d0-d7e8-0133-0c37-1eae90b9310e"><BR>
Travis-CI: <img src="https://travis-ci.org/justincranford/demo.svg?branch=master" alt="Travis-CI Demo"><BR>


Project Examples:

- mvn compile
- mvn test
- mvn package

Command line examples:

- org.justin.demo.calculator.Main "add(1,2)"
- org.justin.demo.calculator.Main "let(a, let(b, 10, add(b, b)), let(b, 20, add(a, b)))" DEBUG


Assumptions:

- JAVA_HOME set to a Java 7 x32/x64 JDK
- Eclipse Installed JRE points to JDK, and adds JDK lib/tools.jar   
- Eclipse 4.5.2 (Mars SR2 x64)
- Tab spacing is 4.


Test