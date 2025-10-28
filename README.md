<h1 align="center">Hibernate Fabric</h1>

<p align="center">Let your Minecraft Fabric server snooze when idle, slashing CPU usage without missing a block!</p>

[![Issues][issues-shield]][issues-url]
[![Forks][forks-shield]][forks-url]
[![Stargazers][stars-shield]][stars-url]
[![LGPL v3.0 License][license-shield]][license-url]

## About The Project

**Hibernate Fabric** is a lightweight tool that automatically puts your Minecraft Fabric server to sleep when no players are online. By reducing CPU usage during idle times, it helps save server resources, lowers your electricity bill, and makes your server more eco-friendly. It's a simple way to keep your Minecraft world running smoothly without wasting power when it's not needed.

### Built With

This project is built with the following technologies:

* ![Java](https://img.shields.io/badge/Java-D5D5D5.svg?style=for-the-badge&logo=data:image/svg%2bxml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHdpZHRoPSI2NCIgaGVpZ2h0PSI2NCIgdmlld0JveD0iMCAwIDMyIDMyIj48cGF0aCBkPSJNMTEuNjIyIDI0Ljc0cy0xLjIzLjc0OC44NTUuOTYyYzIuNTEuMzIgMy44NDcuMjY3IDYuNjI1LS4yNjdhMTAuMDIgMTAuMDIgMCAwIDAgMS43NjMuODU1Yy02LjI1IDIuNjcyLTE0LjE2LS4xNi05LjI0NC0xLjU1em0tLjgtMy40NzNzLTEuMzM2IDEuMDE1Ljc0OCAxLjIzYzIuNzI1LjI2NyA0Ljg2Mi4zMiA4LjU1LS40MjdhMy4yNiAzLjI2IDAgMCAwIDEuMjgyLjgwMWMtNy41MzQgMi4yNDQtMTUuOTc2LjIxNC0xMC41OC0xLjYwM3ptMTQuNzQ3IDYuMDlzLjkwOC43NDgtMS4wMTUgMS4zMzZjLTMuNTggMS4wNy0xNS4wMTQgMS4zOS0xOC4yMiAwLTEuMTIyLS40OCAxLjAxNS0xLjE3NSAxLjctMS4yODIuNjk1LS4xNiAxLjA3LS4xNiAxLjA3LS4xNi0xLjIzLS44NTUtOC4xNzUgMS43NjMtMy41MjYgMi41MSAxMi43NyAyLjA4NCAyMy4yOTYtLjkwOCAxOS45ODMtMi40MDR6TTEyLjIgMTcuNjMzcy01LjgyNCAxLjM5LTIuMDg0IDEuODdjMS42MDMuMjE0IDQuNzU1LjE2IDcuNjk0LS4wNTMgMi40MDQtLjIxNCA0LjgxLS42NCA0LjgxLS42NHMtLjg1NS4zNzQtMS40NDMuNzQ4Yy01LjkzIDEuNTUtMTcuMzEyLjg1NS0xNC4wNTItLjc0OCAyLjc3OC0xLjMzNiA1LjA3Ni0xLjE3NSA1LjA3Ni0xLjE3NXptMTAuNDIgNS44MjRjNS45ODQtMy4xIDMuMjA2LTYuMDkgMS4yODItNS43MTctLjQ4LjEwNy0uNjk1LjIxNC0uNjk1LjIxNHMuMTYtLjMyLjUzNC0uNDI3YzMuNzk0LTEuMzM2IDYuNzg2IDQuMDA3LTEuMjMgNi4wOSAwIDAgLjA1My0uMDUzLjEwNy0uMTZ6bS05LjgzIDguNDQyYzUuNzcuMzc0IDE0LjU4Ny0uMjE0IDE0LjgtMi45NCAwIDAtLjQyNyAxLjA3LTQuNzU1IDEuODctNC45MTYuOTA4LTExLjAwNy44LTE0LjU4Ny4yMTQgMCAwIC43NDguNjQgNC41NDIuODU1eiIgZmlsbD0iIzRlNzg5NiIvPjxwYXRoIGQ9Ik0xOC45OTYuMDAxczMuMzEzIDMuMzY2LTMuMTUyIDguNDQyYy01LjE4MyA0LjExNC0xLjE3NSA2LjQ2NSAwIDkuMTM3LTMuMDQ2LTIuNzI1LTUuMjM2LTUuMTMtMy43NC03LjM3M0MxNC4yOTQgNi44OTMgMjAuMzMyIDUuMyAxOC45OTYuMDAxem0tMS43IDE1LjMzNWMxLjU1IDEuNzYzLS40MjcgMy4zNjYtLjQyNyAzLjM2NnMzLjk1NC0yLjAzIDIuMTM3LTQuNTQyYy0xLjY1Ni0yLjQwNC0yLjk0LTMuNTggNC4wMDctNy41ODcgMCAwLTEwLjk1MyAyLjcyNS01LjcxNyA4Ljc2M3oiIGZpbGw9IiNmNTgyMTkiLz48L3N2Zz4=)
* ![Gradle](https://img.shields.io/badge/gradle-02303A.svg?style=for-the-badge&logo=gradle&logoColor=white)
* ![Fabric](https://img.shields.io/badge/Fabric-DBD0B4.svg?style=for-the-badge&logo=data:image/svg%2bxml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHdpZHRoPSIxNiIgaGVpZ2h0PSIxNiIgdmlld0JveD0iMCAwIDE2IDE2Ij48cGF0aCBmaWxsPSIjMzgzNDJhIiBkPSJNOSAxaDF2MWgxdjFoMXYxaDF2MWgxdjFoMXYyaC0xdjFoLTJ2MWgtMXYxaC0xdjFIOXYySDh2MUg2di0xSDV2LTFINHYtMUgzdi0xSDJWOWgxVjhoMVY3aDFWNmgxVjVoMVY0aDFWMmgxeiIvPjxwYXRoIGZpbGw9IiNkYmQwYjQiIGQ9Ik00IDlWOGgxVjdoMVY2aDFsMS0xVjRoMVYyaDF2MWgxdjFoMXYxaDF2MWwtMSAxLTIgMy0zIDMtMy0zeiIvPjxwYXRoIGZpbGw9IiNiY2IyOWMiIGQ9Ik05IDNoMXYxaDF2MWgxdjFoMXYxaC0xTDkgNHpNMTAgMTBoMVY5aDFWN2gtMXYxaC0xekg4djJoMXYtMWgxek04IDEySDd2MWgxeiIvPjxwYXRoIGZpbGw9IiNjNmJjYTUiIGQ9Ik03IDVoMXYyaDN2MUg5VjZIN3pNNiA4aDF2MmgyVjlINnoiLz48cGF0aCBmaWxsPSIjYWVhNjk0IiBkPSJNMyA5djFsMyAzaDF2LTFINnYtMUg1di0xSDRWOXoiLz48cGF0aCBmaWxsPSIjOWE5MjdlIiBkPSJNMyAxMHYxaDJ2MmgydjFINnYtMkg0di0yeiIvPjxwYXRoIGZpbGw9IiM4MDdhNmQiIGQ9Ik0xMyA3aDF2MWgtMXoiLz48cGF0aCBmaWxsPSIjMzgzNDJhIiBkPSJNOSA0djFoMnYyaDFWNmgtMlY0eiIvPjwvc3ZnPgo=)

## Getting Started

To get a local copy up and running, follow these simple steps.

### Prerequisites

Ensure you have the following installed on your machine:

* **Java Development Kit (JDK)**: Version 21 or higher.
  * [Download JDK](https://adoptium.net/)
* **Gradle**: Version 9.1 or higher.
  * [Install Gradle](https://gradle.org/install/)
* **Minecraft**: Version 1.21.9/10

### Build

1. **Clone the repository**
```sh
git clone https://github.com/BJTMastermind/hibernate-fabric.git
```

2. Navigate to the project directory
```sh
cd hibernate-fabric
```

3. Build the project with Gradle
```sh
./gradlew clean build
```

## Run

You will need a Minecraft Fabric server.
Copy the `<mod>/build/libs/hibernate-fabric-<mod>-x.x.x.jar` file to the `mods` folder of your Minecraft Fabric server.

## Configuration

The mod automatically creates a configuration file at `config/hibernate-fabric.json` on first run. Here are the available settings:

### Basic Settings

```js
{
  "startEnabled": true,
  "ticksToSkip": 400,
  "permissionLevel": 2,
  "sleepTimeMs": 75
  ...
```

| Setting | Default | Description |
|---------|---------|-------------|
| `startEnabled` | `true` | Whether hibernation activates automatically when server starts with no players |
| `ticksToSkip` | `400` | Number of ticks to process before applying sleep during hibernation |
| `permissionLevel` | `2` | Required permission level to use hibernation commands (0=all, 4=owner) |
| `sleepTimeMs` | `75` | Milliseconds to sleep between tick processing cycles |

### Memory Optimization

```js
  ...
  "enableMemoryOptimization": true,
  "memoryCleanupIntervalSeconds": 30,
  "memoryThresholdPercent": 80.0,
  "forceGarbageCollection": true,
  "gcIntervalSeconds": 30,
  "saveBeforeHibernation": true,
  "removeOldDroppedItems": true,
  "droppedItemMaxAgeSeconds": 300,
  "removeProjectiles": true,
  "removeExperienceOrbs": true,
  "logMemoryUsage": true
  ...
```

| Setting | Default | Description |
|---------|---------|-------------|
| `enableMemoryOptimization` | `true` | Enable memory cleanup during hibernation |
| `memoryCleanupIntervalSeconds` | `30` | How often to run memory cleanup routines |
| `memoryThresholdPercent` | `80.0` | Memory usage percentage that triggers cleanup |
| `forceGarbageCollection` | `true` | Force Java garbage collection during hibernation |
| `gcIntervalSeconds` | `30` | Minimum time between garbage collection runs |
| `saveBeforeHibernation` | `true` | Save world data before entering hibernation |
| `removeOldDroppedItems` | `true` | Remove old dropped items during hibernation |
| `droppedItemMaxAgeSeconds` | `300` | Age in seconds after which items are removed (5 minutes) |
| `removeProjectiles` | `true` | Remove arrows and other projectiles |
| `removeExperienceOrbs` | `true` | Remove floating experience orbs |
| `logMemoryUsage` | `true` | Log memory usage information to console |

### CPU Optimization

```js
  ...
  "aggressiveCpuSaving": true,
  "minSleepInterval": 1500,
  "highLoadSleepMultiplier": 1.5,
  "yieldInterval": 8
}
```

| Setting | Default | Description |
|---------|---------|-------------|
| `aggressiveCpuSaving` | `true` | Enable more aggressive CPU saving measures |
| `minSleepInterval` | `1500` | Minimum time in milliseconds between sleep cycles |
| `highLoadSleepMultiplier` | `1.5` | Multiplier for sleep time when system load is high |
| `yieldInterval` | `8` | How often to yield CPU to other threads (every N ticks) |

## Contributing

Contributions are what make the open source community such an amazing place to learn, inspire, and create. Any contributions you make are **greatly appreciated**.
If you don't have any coding experience, testing other platforms and configurations is also very welcome!

If you have a suggestion that would make this better, please fork the repo and create a pull request. You can also simply open an issue with the tag "enhancement".
Don't forget to give the project a star! Thanks again!

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## License

Distributed under the **GNU Lesser General Public License v3.0 or later (LGPL-3.0-or-later)**.

This project is a fork of [Hibernateforge](https://github.com/Thadah/Hibernateforge) which was distributed under the **European Union Public License v1.2 (EUPL-1.2)**.
Relicensing has been done in accordance with the compatibility clause of the EUPL (Article 5).

Commits between `027f88d` (11/4/2024) and `e5cb169` (9/13/2025) remain under EUPL-1.2.

See `LICENSE` for more information.

## Acknowledgments

Thanks to these nice projects!

* [Img Shields](https://shields.io)
* [markdown-badges](https://github.com/Ileriayo/markdown-badges#table-of-contents)

[contributors-shield]: https://img.shields.io/github/contributors/BJTMastermind/hibernate-fabric.svg?style=for-the-badge
[contributors-url]: https://github.com/BJTMastermind/hibernate-fabric/graphs/contributors
[forks-shield]: https://img.shields.io/github/forks/BJTMastermind/hibernate-fabric.svg?style=for-the-badge
[forks-url]: https://github.com/BJTMastermind/hibernate-fabric/network/members
[stars-shield]: https://img.shields.io/github/stars/BJTMastermind/hibernate-fabric.svg?style=for-the-badge
[stars-url]: https://github.com/BJTMastermind/hibernate-fabric/stargazers
[issues-shield]: https://img.shields.io/github/issues/BJTMastermind/hibernate-fabric.svg?style=for-the-badge
[issues-url]: https://github.com/BJTMastermind/hibernate-fabric/issues
[license-shield]: https://img.shields.io/github/license/BJTMastermind/hibernate-fabric.svg?style=for-the-badge
[license-url]: https://github.com/BJTMastermind/hibernate-fabric/blob/master/LICENSE
