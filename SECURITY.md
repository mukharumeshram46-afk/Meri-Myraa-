# Security Policy

## Supported Versions

| Version | Supported          |
| ------- | ------------------ |
| 1.0.x   | :white_check_mark: |

## Security Principles

1. **No Hardcoded Secrets**: All API keys must be injected securely through the AI Studio Secrets panel or environment variables (`.env`).
2. **Least Privilege**: MYRAA only requests permissions strictly required for explicit user-requested features.
3. **Transparent Hardware Access**: Zero secret background audio or camera capture. Visual indicators are always shown.
4. **Action Confirmation**: High-risk operations (e.g. data deletion, messaging) require user confirmation before execution.

## Reporting a Vulnerability

To report a vulnerability, please open an issue in this repository or contact the maintainer directly.
