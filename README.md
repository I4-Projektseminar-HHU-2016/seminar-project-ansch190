# VideoAnalyzer

This is a Project for analyze and compare Videos.

This Software uses Perceptual-Hashing for creating a Database and Hamming-Distance for compare different Videos.

FFmpeg used as VideoEngine.

## Features ##

- [x] Multithreading
- [ ] SQL Connection

## Getting Started

Clone the Repository with an IDEA for Example:

- IntelliJ IDEA 2016

or Copy the Repository.

## Prerequisities

- You need ffmpeg installed on your Computer.

### Java

- Java 8 SDK

### OS

- Linux

### Gradle Dependency

Add this in your root `build.gradle` file (**not** your module `build.gradle` file):

```gradle
allprojects {
	repositories {
		...
		mavenCentral()
	}
}
```

Then, add the library to your module `build.gradle`
```gradle
dependencies {
    testCompile group: 'junit', name: 'junit', version: '4.11'

    // https://mvnrepository.com/artifact/org.slf4j/slf4j-api
    compile group: 'org.slf4j', name: 'slf4j-api', version: '1.7.21'

    // https://mvnrepository.com/artifact/org.slf4j/slf4j-log4j12
    compile group: 'org.slf4j', name: 'slf4j-log4j12', version: '1.7.21'

    //Database-Connection
    compile 'mysql:mysql-connector-java:6.0.3'
    compile 'org.mariadb.jdbc:mariadb-java-client:1.5.0-RC1'
}
```

### Installing

For creating a Database you need the Methods in the File

Tests.java

For Testing your Database or Analyze a Video you need the Methods in the File

Tests.java

## Versioning

v1.0.0

## License

This project is licensed under the MIT License - see the [LICENSE.md](LICENSE.md) file for details.

*Check http://choosealicense.com/ to choose a license for your code*

## Acknowledgments

This Project is not tested with more than 1 Video in a Database.
