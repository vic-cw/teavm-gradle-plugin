Gradle plugin for compiling Java bytecode to JavaScript using [TeaVM](http://teavm.org/).

Quick Start
===========

## Add build script dependency:

```
buildscript {
    repositories {
        mavenCentral()
        maven { url "https://jitpack.io" }
    }

    dependencies {
        classpath 'com.github.vic-cw:teavm-gradle-plugin:COMMIT_HASH'
    }
}
```
Replace `COMMIT_HASH` with actual short commit hash of desired version. See [Jitpack](https://jitpack.io/) for explanations.

Example: `classpath 'com.github.vic-cw:teavm-gradle-plugin:51ba430'`

> Note: It is discouraged to use <strike>`master-SNAPSHOT`</strike>, since this tool and TeaVM are still in 0.x versions, which means any commit can introduce a breaking change.

## Apply plugin to project:
```
apply plugin: 'com.edibleday.teavm'
```

Add repositories for TeaVM dependencies:
```
repositories {
    mavenCentral()
}
```

Set compilation options:
```
teavmc {
    // Required
    // Class with `public static void main(String[] args)` method:
    mainClass = "my.package.Main"

    // Optional configuration block

    /* Where to put final web app */
    installDirectory "${project.buildDir}/teavm"

    /* Main JavaScript file name */
    targetFileName 'app.js'

    /* Copy sources to install directory for simpler debugging */
    copySources false

    /* Generate JavaScript to Java mapping for debugging */
    generateSourceMap false

    /* Minify JavaScript */
    minified true

    /* Runtime JavaScript inclusion
     *  NONE: don't include runtime
     *  MERGED: merge runtime into main JavaScript file
     *  SEPARATE: include runtime as separated file
     */
    runtime org.teavm.tooling.RuntimeCopyOperation.SEPARATE
}
```

Usage
=====
To compile project, run `teavmc` task:
```
./gradlew teavmc
```

Resulting application will be put into `installDirectory` (by default `build/teavm`).

To run app, open `main.html` from `installDirectory`.

It's possible to add source code for source code copying tasks by using dependency with `teavmsources` configuration that points to source artifact. Example:

```
dependencies {
    teavmsources "com.test:test:1.0.0:sources"
}
```
