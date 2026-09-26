# Security Policy

## Supported Versions

| Version | Supported          |
| ------- | ------------------ |
| 1.x     | :white_check_mark: |

## Reporting a Vulnerability

If you discover a security vulnerability or privacy concern within this repository, please do **NOT** open a public issue.

Instead, please report the vulnerability privately by opening a [Security Advisory](https://github.com) or reaching out to the maintainer directly.

### Security Principles of SilentPixel
- **Zero Telemetry**: SilentPixel does not collect, log, or transmit any user data, network traffic, or analytics.
- **Minimal Permissions**: The app only requests the bare minimum permissions needed to communicate with the local audio HAL and Shizuku.
- **Non-Invasive**: No system files or partitions (`/system`, `/vendor`, `/boot`) are altered or written to. All changes operate strictly within Android's runtime Audio Policy in RAM.
