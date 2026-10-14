# IndustrialCraft2-CE

> [!IMPORTANT]
> **Unofficial community fan project**
>
> IndustrialCraft2-CE is a community fan-made reimplementation. It is **not an official IndustrialCraft 2 (IC2) release or an official continuation of IC2**.
>
> **本项目为社区粉丝作品，并非 IC2 官方项目或官方正式延续。**

## About the project

IndustrialCraft2-CE reintroduces **IndustrialCraft 2 to modern Minecraft**, using the **IC2 implementation for Minecraft 1.19.2** as its reference.

The goal is to preserve the familiar industrial progression: start with power generation and basic resource processing, connect machines into automated production chains, and work toward advanced equipment and nuclear energy.

Gameplay is independently rewritten for **NeoForge**, with the reference implementation guiding behavior and compatibility. Original IC2 textures, visual assets and sound effects are retained to preserve the recognizable look and feel of its machines, equipment and materials.

- **Energy:** Generators, cables, transformers and energy storage for powering an industrial base.
- **Industry:** Ore processing, metal forming, fluid processing and automated machine chains.
- **Nuclear:** Reactors, fuel, cooling components and heat management.
- **Tools and farming:** Powered tools, wearable equipment, crops and farming machines.

Maintained by **SISE-Team**, with project authorship credited to **Viking602**.

## Build from source

Install **JDK 25** and **Git**. Set `JAVA_HOME` to your JDK installation and make sure its `bin` directory is on your `PATH`. The included Gradle Wrapper downloads Gradle and the required dependencies on the first build, so an internet connection is needed.

Clone the repository and enter its directory:

```sh
git clone https://github.com/SISE-Team/IndustrialCraft2-CE.git
cd IndustrialCraft2-CE
```

**Windows (PowerShell)**

```powershell
.\gradlew.bat build
```

**Linux / macOS**

```sh
./gradlew build
```

The build compiles the mod, runs the standalone engine checks and creates the installable mod JAR and sources JAR in `build/libs/`. Install the mod JAR in the `mods/` directory of a compatible NeoForge instance; the sources JAR is for development.

The target Minecraft and NeoForge dependencies are defined in [gradle.properties](gradle.properties). The original IC2 reference JAR and decompiled reference sources are not needed to build the project.

## Licensing

**Project code — MIT.** All code written by IndustrialCraft2-CE contributors is licensed under the [MIT License](LICENSE).

**IC2 materials — original IC2 rights.** Reused textures, visual assets and sound effects belong to the official IndustrialCraft 2 (IC2) project and their original copyright holders. Their original copyrights, author credits and licensing terms are retained. The project's MIT license does not apply to original IC2 code or materials.

**Third-party materials — their respective licenses.** Attribution and license details are preserved in [NOTICE](NOTICE) and [licenses/](licenses/).
