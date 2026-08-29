# REVENEX SCHOOL ERP — ENVIRONMENT & SECRETS GUIDE

## Overview

Revenex School ERP separates configuration into runtime modes (Demo vs Production) and uses standard Android Secrets Gradle Plugin for key management.

---

## Configuration Files

| File | Purpose | Version Controlled |
|:---|:---|:---:|
| `.env.example` | Environment variable template | Yes |
| `.env` | Local secrets file | **No** (`.gitignore`) |
| `local.properties` | Local SDK path | **No** (`.gitignore`) |
| `google-services.json` | Firebase configuration | **No** (`.gitignore`) |
| `debug.keystore` | Debug signing key | **No** (`.gitignore`) |

---

## Environment Variables

### 1. `GEMINI_API_KEY`
- **Purpose**: Serverless / Client AI Assistant queries for school guidelines, policy lookups, and report summaries.
- **Where to obtain**: [Google AI Studio](https://aistudio.google.com/app/apikey)
- **Configuration**:
  ```properties
  GEMINI_API_KEY=AIzaSy...
  ```
- **Dependent features**: `AiAssistantScreen` natural language responses.

### 2. `RAZORPAY_KEY_ID` (Optional commercial integration)
- **Purpose**: Online UPI, NetBanking, and Card payments for student fee dues.
- **Where to obtain**: [Razorpay Dashboard](https://dashboard.razorpay.com)
- **Configuration**:
  ```properties
  RAZORPAY_KEY_ID=rzp_live_...
  ```
- **Dependent features**: Production 1-tap fee gateway checkout.

---

## External Configuration Blockers Report

If any of the following external credentials are missing in Production Mode, the system behaves as follows:

| Credential | Status | Fallback Behavior |
|:---|:---:|:---|
| `google-services.json` | **REQUIRED FOR PROD** | Production Mode authentication will fail with a clear, user-friendly message asking to provide `google-services.json` or use Demo Mode. |
| `GEMINI_API_KEY` | **OPTIONAL** | AI Assistant displays a configuration notice explaining key setup instructions. |
| `RAZORPAY_KEY_ID` | **OPTIONAL** | Fee payments fall back to instant internal ledger receipt mode with status `SUCCESS`. |
