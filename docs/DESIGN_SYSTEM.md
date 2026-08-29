# REVENEX SCHOOL ERP — DESIGN SYSTEM & TOKENS

## 1. Palette & Theming (Material Design 3)
The Revenex visual identity uses an authoritative, high-trust navy, royal blue, and warm gold palette tailored for educational leadership:

| Token | Hex Value | Semantic Usage |
| :--- | :--- | :--- |
| **RevenexNavy** | `#1E3A8A` | Primary headers, executive brand cards, navigation active state |
| **RevenexBlue** | `#2563EB` | Interactive buttons, active tab indicators, links |
| **RevenexGold** | `#D97706` | Badges, student ranking indicators, highlights |
| **StatusSuccess** | `#16A34A` | Present attendance, paid fees, approved leaves |
| **StatusError** | `#DC2626` | Absent attendance, overdue fees, rejected leaves |
| **StatusWarning** | `#D97706` | Pending fees, pending approvals |

## 2. Typography Hierarchy
- **Display / Headers**: Bold sans-serif with comfortable tracking and high legibility.
- **Body & Captions**: Clean neutral tones with strict contrast ratios (WCAG AAA compliant).

## 3. Component Library
- **`StatCard`**: Metric visualizer with trend percentage, icon avatar, and container tint.
- **`QuickActionButton`**: Square touch-target component (>= 48dp) with surface background and colored glyph.
- **`StatusBadge`**: Pill badge with dynamic color schemes based on entity state.
- **`SimpleBarChart`**: High-performance pure Compose bar chart for attendance and academic trends.
- **`SimpleProgressRing`**: Radial progress indicator for CBSE board compliance and attendance health.
