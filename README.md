# Log Tools

This library is aimed at the IHMC Robotics lab.

### Features

- Uses a global `us.ihmc` logger for log entries by default, removing the need to instantiate loggers all over the place.
- Provides Java and Gradle command line properties for setting log levels.
- Adds class name and line number to log entries.
- Real time safety (no reallocation) when root logger is set to `OFF`.