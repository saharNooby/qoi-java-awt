# qoi-java-awt

This is `ImageIO` support library for [qoi-java](https://github.com/saharNooby/qoi-java) that allows working with QOI images as `BufferedImage`s.

## How to Use

### Add as a dependency

This library is available in Maven Central.

#### Maven

```xml
<dependency>
    <groupId>me.saharnooby</groupId>
    <artifactId>qoi-java-awt</artifactId>
    <version>1.2.0</version>
</dependency>
```

#### Gradle

```groovy
dependencies {
	implementation 'me.saharnooby:qoi-java-awt:1.2.0'
}
```

#### Other build systems

You can download prebuilt JARs from [GitHub releases](https://github.com/saharNooby/qoi-java-awt/releases) or build them yourself.

### Usage

Documentation is in [the main repository](https://github.com/saharNooby/qoi-java).

## Building

You will need Git, Maven and JDK 8 or higher.

```shell
git clone https://github.com/saharNooby/qoi-java-awt.git
cd qoi-java-awt
mvn clean install
```
