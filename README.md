
# OBD2AI - Intelligent Car Diagnostics

[![Android CI](https://github.com/JaKuBisz/OBD2AI/actions/workflows/android-ci.yml/badge.svg)](https://github.com/JaKuBisz/OBD2AI/actions/workflows/android-ci.yml)

OBD2AI is an innovative Android application that connects to your car's OBD2 system, providing detailed diagnostics and state analysis using OpenAI's ChatGPT API. This application aims to simplify car maintenance and diagnostics, making it accessible for every car owner.

## Features

- **Real-Time Diagnostics:** Connects to your car's OBD2 port for real-time data retrieval.
- **AI-Powered Analysis:** Uses OpenAI's ChatGPT API for advanced error analysis and state-of-the-car reporting.
- **User-Friendly Interface:** Easy to navigate interface for a hassle-free user experience.

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

- **Live data dashboard** — stream RPM, speed, coolant temp and other PIDs into real-time charts instead of one-shot reads.
- **Trip history** — persist scans and live sessions locally (Room) so users can compare vehicle health over time.
- **DTC lookup without AI** — bundle an offline DTC database, use the LLM only for explanations to cut latency and API cost.
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
