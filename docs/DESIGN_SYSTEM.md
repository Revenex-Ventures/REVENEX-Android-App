# Compose design system

Revenex uses ivory/white light surfaces, charcoal text, orange actions and restrained semantic lavender, sage, blue and yellow accents. Dark mode uses a coordinated charcoal palette. Role portals share one identity while prioritizing different tasks. Preserve approved workflow layouts when maintaining shared components.

Use the existing color, typography, spacing, shape and motion tokens under `ui/theme/`. Reusable presentation lives under `ui/components/`; role pages live under `ui/screens/`. Do not scatter arbitrary colors, oversized hero cards or nested decorative containers through screens.

Branding uses the official RV mark and company-card wordmark. Header sizing adapts to available width while retaining action controls. The opening reveal is brief, silent by default and does not replay on resume. Reduced motion avoids waiting for the reveal. Asset attribution and source references are recorded in [ASSETS.md](ASSETS.md).

Light is the default on a new installation. Settings exposes one persisted Light/Dark/System selector. System follows Android appearance. Check every surface, dialog, field, navigation item and system bar in both palettes.

Keep readable hierarchy, accessible touch targets, meaningful selected/disabled/error states, scrolling at large font sizes and keyboard/system insets. Inspect actual renders on narrow phones and larger screens; compilation and a contrast unit test do not certify all accessibility or visual behavior.
