# adv

Current game project, in active development.

# Setup and running
You'll need the following things to run the game:
- JDK 8+
- Gradle
- A clone of the cypai/adv-binassets repository

For development:
- An IDE that can handle Kotlin (eclipse, IntelliJ, etc.)
- Checkstyle and sevntu-checkstyle plugins for static analysis (I will not merge if there are issues found)

You will need to create a symbolic link from core/assets to binassets. Assuming that your repositories are in ~/repos:
```
ln -s ~/repos/adv-binassets/ ~/repos/adv/core/assets/binassets
```

You can begin running the game by simply using the following command:
```
gradle desktop:run
```

Other useful commands:
```
gradle clean test # For running unit tests
gradle check      # For running checkstyle
```
