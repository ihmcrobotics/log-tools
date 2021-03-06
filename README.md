# Log Tools

This library is aimed at the IHMC Robotics lab.

### Features

- Zero-configuration required API. Use `LogTools.info("msg")` right out of the box, from anywhere.
- Realtime safe by default as long as info and higher level messages are non-recurring.
- Provides Java and Gradle properties for setting log levels.
- Granular non-realtime mode for setting log levels of packages and classes.
- Adds class name and line number to all log entries.
- Colors `ERROR`, `WARN`, `DEBUG`, and `TRACE` levels red, yellow, cyan, and green.

### Download

Unfortunately, the name "log-tools" has already been taken on JCenter so we currently require adding the Maven repository:

`maven { url = uri("https://dl.bintray.com/ihmcrobotics/maven-release") }`

`plugins { id "us.ihmc.log-tools" version `
[ ![Download](https://api.bintray.com/packages/ihmcrobotics/maven-release/log-tools/images/download.svg) ](https://bintray.com/ihmcrobotics/maven-release/log-tools/_latestVersion)
` }   // Gradle plugin`


`compile group: "us.ihmc", name: "log-tools", version: `
[ ![Download](https://api.bintray.com/packages/ihmcrobotics/maven-release/log-tools/images/download.svg) ](https://bintray.com/ihmcrobotics/maven-release/log-tools/_latestVersion)`   // API`

### API

To log messages, use the static `LogTools` methods.

#### Setting the log levels

Log tools provides the following log levels: `off`, `fatal`, `error`, `warn`, `info`, `debug`, `trace`, and `all`.

Each levels has no special behavior other than it's importance to the end user.
`fatal` is most important and `trace` is least important. Find conventional
meanings for the levels here: [https://en.wikipedia.org/wiki/Log4j#Log4j_log_levels](https://en.wikipedia.org/wiki/Log4j#Log4j_log_levels).

To set log levels you can pass JVM arguments:
- `-Dlog.level=<level>`: Set root log level, including all software on the classpath that uses SLF4J compatible log APIs.
- `-Dlog.granular=<true|false>`: Enable granular mode, allowing setting levels for arbitrary classpaths. (NOT realtime safe)
- `-Dlog.level.us.ihmc=<level>`: Set the log level of all IHMC software. In granular mode, this instead refers to all code in the `us.ihmc` package.
- `-Dlog.level.<classpathPrefix>=<level>`: If granular mode is `true` or unset: Set the log level of a package or class.

Note: For convenience, granular mode is automatically activates when at least one `-Dlog.level.<classpathPrefix>=<level>` is present. Setting `-Dlog.level.us.ihmc=<level>` or `-Dlog.level=<level>` will NOT enable granular mode and are always safe to use.
  
Examples:

```
<pass nothing>    // all log levels set to info, realtime safe

-Dlog.level=info   // same as default, realtime safe

-Dlog.level=off   // disable all SLF4J compatable logging, realtime safe

-Dlog.level.us.ihmc=debug   // debug IHMC software only, realtime safe

-Dlog.level.us.ihmc.avatar.behaviors=debug   // debug behaviors package only, NOT realtime safe

-Dlog.level.us.ihmc.variables.YoDouble=trace   // Enables trace level on YoDouble class, NOT realtime safe

-Dlog.granular=true -Dlog.level.us.ihmc.communication=trace   // Enables trace level on YoDouble class, NOT realtime safe

-Dlog.granular=false -Dlog.level.us.ihmc.communication=trace   // Does NOT enable trace level on YoDouble class, realtime safe

```

#### More options

Pass the `-Dlog4j2.configurationFile=` system property to tell Log4J2 to use a different configuration file.

There a few built in configuration options:

Pass `-Dlog4j2.configurationFile=log4j2TeeToFile.yml` to tee log output to a timestamped file in `~/.ihmc/logs`.

Pass `-Dlog4j2.configurationFile=log4j2NoColor.yml` to disable the ANSI colors. This is useful for viewers that don't support it.

### Realtime safety

To ensure your code is realtime safe:

1. Never build strings in a `LogTools` call.
2. Use the `LogTools.xxxx("var1: {}, var2: {}", var1, var2)` methods to build strings from variables.
3. Use a message `Supplier` to build complex messages, but do not inline it.
4. Do not log `info`, `warn`, `error`, or `fatal` levels more than a predetermined maximum amount.
    - If an error message is printed on some math failure, have a counter such that it becomes silent after 5 or so occurances.
5. Do not set `-Dlog.granular=true` or set any levels by package or class.
    - You may safely use `-Dlog.level=level` to set the global level (including non-ihmc messages)
    - You may safely use `-Dlog.level.us.ihmc` to set the log level of everything going though the `LogTools` API
    - You may NOT safely set any other log level i.e. `-Dlog.level.*`, unless you explicity set `-Dlog.granular=false`, which sets to ignore all `-Dlog.level.*` property. This is to make it convenient to quickly switch between modes without deleting complex level settings.

Some examples:

```
LogTools.info("str1 {}", someStrVariable)    // SAFE
LogTools.info("str1" + someStrVariable)      // NOT realtime safe

Supplier<String> supplier = () -> "string" + "builder";  // as field or existing method, created once
[...]
LogTools.info(supplier) // SAFE

LogTools.info(() -> { "string" + "builder"})   // NOT realtime safe, allocates new Supplier<String>()
```

### Gradle plugin

The Gradle plugin exists solely to provide a wrapper for the JVM properties. All properties are the same
as above. Just replace `-D` with `-P`.

The plugin applies the log settings to all JVMs spawned by Gradle to run your applications. This includes:
- `test` task JVMs
- Custom, user defined, `JavaExec` tasks
- `run` task provided by the `application` plugin
- Scripts generated by the `installDist` task provided by the `application` plugin

To run tests, only logging errors:

```
> gradle test -Plog.level=error
```

### Misc

Warnings about WindowsAnsiOutputStream: https://github.com/apache/logging-log4j2/commit/c8a7e559fa3cfd16f0e8a7be945a7682bebd11ab
