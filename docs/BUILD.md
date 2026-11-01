# Build

## Requirements
- JDK 25 (Minecraft 26.2 targets Java 25).
- Gradle via the included wrapper (`./gradlew`), Gradle 9.8.2.

## Core (no Minecraft needed)
```
./gradlew build     # compile + run all tests
./gradlew test      # tests only
```
The core references Minecraft only through interfaces, so it builds and tests with
zero Minecraft on the classpath.

## Minecraft 26.2 adapters
The `src/mc` source set compiles only when you provide the deobfuscated client jar:
```
./gradlew compileMc -PmcJar=C:/path/to/minecraft-26.2.jar
```
This is normally done inside your client's own build during integration; the adapters
are compile-verified there, not here.

## Notes
- SQLite/native warnings: none — the module has no runtime dependencies (findings are
  in-memory, persistence is delegated to the host).
- Deprecation warning from Gradle about future Gradle 10 is harmless.
