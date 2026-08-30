
# OBD2AI - Intelligent Car Diagnostics

[![Android CI](https://github.com/JaKuBisz/OBD2AI/actions/workflows/android-ci.yml/badge.svg)](https://github.com/JaKuBisz/OBD2AI/actions/workflows/android-ci.yml)

OBD2AI is an innovative Android application that connects to your car's OBD2 system, providing detailed diagnostics and state analysis using OpenAI's ChatGPT API. This application aims to simplify car maintenance and diagnostics, making it accessible for every car owner.

## Features

- **Live dashboard:** Streams RPM, speed, coolant temperature and fuel level into real-time gauges (with a demo mode that works without a car).
- **Diagnostics scan:** Reads current, pending and permanent trouble codes from the ECU.
- **AI-powered analysis:** OpenAI explains each trouble code in plain language — severity, implications and suggested actions.
- **Offline DTC database:** Bundled database of common trouble codes, so diagnostics work without network or an API key.
- **History:** Scan results and trip statistics (max RPM/speed, avg coolant temp) are stored locally with Room.

## Architecture

Single-activity Jetpack Compose app following MVVM with a repository layer and Hilt DI:

```
bluetooth/      BluetoothController - SPP socket handling, permissions
data/obd/       ObdRepository - connection state machine + live PID streaming as Flow
data/ai/        OpenAIService, AiRepository (LLM with offline fallback), DtcInfoProvider (bundled DTC db)
data/local/     Room: DTC scan records + trip sessions
ui/             Compose screens (home, connect, dashboard, dtc, history) + custom Canvas gauges
model/          Shared models (DtpCodeDTO, LiveReading, ErrorSeverity)
```

## Releases

- This project uses [Semantic versioning](https://semver.org/)
- See [current releases](https://github.com/JaKuBisz/OBD2AI/releases)

## Getting Started

### Prerequisites

- An Android device with Android 6.0 or newer.
- An OBD2 ELM 327 Bluetooth scanner.

### Installation

1. Clone the repo:
   ```bash
   git clone https://github.com/JaKuBisz/OBD2AI.git
   ```
2. Add your OpenAI API key to `Src/local.properties`:
   ```
   OPENAI_API_KEY=sk-...
   ```
   Alternatively, export it as the `OPENAI_API_KEY` environment variable (this is what CI uses).
3. Open the `Src` folder in Android Studio, or run `./gradlew assembleDebug` from `Src`.

## Usage

1. Connect the OBD2 scanner to your car's OBD2 port.
2. Open the OBD2AI app and pair it with the scanner.
3. Access real-time data and AI-powered insights from your car.

## Roadmap

- **Foreground tracking service** — keep recording live data with the screen off, with a persistent notification.
- **Charts over time** — graph RPM/temperature trends within a trip.
- **Fleet mode** — a lightweight backend (REST + WebSockets) where multiple devices report vehicle status for fleet tracking.
- **Release pipeline** — signed release builds and versioned artifacts from CI.

## Contributing

Contributions are what make the open-source community such an amazing place to learn, inspire, and create. Any contributions you make are **greatly appreciated**.

1. Clone the project repository to your local machine (`git clone https://github.com/JaKuBisz/OBD2AI.git`)
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## License

This project is licensed under the Apache License 2.0 - see the [LICENSE](https://github.com/JaKuBisz/OBD2AI/blob/main/LICENSE) file for details.

## Acknowledgements

- [OpenAI ChatGPT API](https://openai.com/api/)
- [OBD2 Communication - kotlin-obd-api by eltonvs](https://github.com/eltonvs/kotlin-obd-api)
- [OpenAI API - openai-kotlin by Aallam](https://github.com/Aallam/openai-kotlin)

*This readme was created with the help of ChatGPT.* 
