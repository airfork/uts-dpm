# Email Template Redesign

Align MJML email templates with the frontend redesign (trusting-villani branch).

## Goals

1. **Visual parity** with the new frontend design
2. **Functional improvements** - better mobile rendering, clearer CTAs
3. **Maintain compatibility** - emails must render well across all major clients

## Design Decisions

- **Technology**: Stay with MJML (good email client compatibility, existing workflow)
- **Dark mode**: Light mode only (simpler, more predictable)
- **Key elements**: Gradient buttons, updated color scheme, card styling

## Color System

OKLCH colors converted to hex for email compatibility:

| Token | Hex | Usage |
|-------|-----|-------|
| primary-500 | `#4a5568` | Button gradient end |
| primary-600 | `#232d4b` | Button gradient start, links, header text |
| primary-700 | `#1a2138` | Reserved (hover states not applicable) |
| neutral-100 | `#f3f3f3` | Email body background |
| neutral-200 | `#e5e5e5` | Card border |
| base-100 | `#ffffff` | Card background |
| base-content | `#333333` | Body text |
| neutral-500 | `#6b7280` | Secondary text, footer |

## Layout Structure

```
┌─────────────────────────────────────────┐
│         Body: #f3f3f3 (neutral-100)     │
│  ┌───────────────────────────────────┐  │
│  │      Header: "UTS DPM" centered   │  │
│  │      Text: #232d4b (primary-600)  │  │
│  │      Font: 24px, semibold         │  │
│  └───────────────────────────────────┘  │
│  ┌───────────────────────────────────┐  │
│  │     Card: #ffffff (base-100)      │  │
│  │     Border: 1px solid #e5e5e5     │  │
│  │     Border-radius: 16px           │  │
│  │     Padding: 32px                 │  │
│  │                                   │  │
│  │     Greeting (bold, #333333)      │  │
│  │     Body text (#333333)           │  │
│  │     [  Gradient Button  ]         │  │
│  │     Optional footnote (#6b7280)   │  │
│  │                                   │  │
│  └───────────────────────────────────┘  │
│  ┌───────────────────────────────────┐  │
│  │     Footer: Copyright text        │  │
│  │     Text: #6b7280 (neutral-500)   │  │
│  │     Font: 12px                    │  │
│  └───────────────────────────────────┘  │
└─────────────────────────────────────────┘
```

## Button Design

**Specs:**
- Gradient: `#232d4b` → `#4a5568` (left to right)
- Text: `#ffffff`, semibold, 14px
- Padding: 14px vertical, 28px horizontal
- Border-radius: 8px

**Fallback strategy:**
1. Modern clients (Apple Mail, iOS, Gmail app): Full gradient
2. Outlook (Windows): VML fallback with solid `#232d4b`
3. Older clients: Solid `#232d4b` background

**Implementation:**
```xml
<mj-button
  background-color="#232d4b"
  color="#ffffff"
  border-radius="8px"
  font-weight="600"
  inner-padding="14px 28px"
  css-class="gradient-btn"
  href="${url}">
  Button Text
</mj-button>
```

With CSS override in `mj-style`:
```css
.gradient-btn a {
  background: linear-gradient(to right, #232d4b, #4a5568) !important;
}
```

## Template-Specific Details

### welcome.mjml
- Title: "Welcome to UTS DPM"
- Button: "Sign In"
- Temporary password displayed in monospace code block

### reset-password.mjml
- Title: "Password Reset"
- Button: "Sign In"
- Temporary password displayed in monospace code block

### dpm-received.mjml
- Title: "DPM Received"
- Show DPM type as subheading
- Button: "View DPMs"

### points-balance.mjml
- Title: "Points Balance"
- No button
- Display balance prominently (larger/bold text)

## Shared Components

### header.mjml
- Text: "UTS DPM"
- Color: `#232d4b`
- Font: Poppins, 24px, semibold
- Centered

### footer.mjml
- Text: Copyright notice
- Color: `#6b7280`
- Font: 12px
- Centered

## Password Display Block

For welcome and reset-password emails:

```xml
<mj-section background-color="#f3f3f3" border-radius="8px" padding="16px">
  <mj-column>
    <mj-text font-family="monospace" font-size="16px" font-weight="600">
      ${password}
    </mj-text>
  </mj-column>
</mj-section>
```

## Files to Modify

1. `mjml-templates/header.mjml` - Simplified branding
2. `mjml-templates/footer.mjml` - Updated styling
3. `mjml-templates/welcome.mjml` - Full redesign
4. `mjml-templates/reset-password.mjml` - Full redesign
5. `mjml-templates/dpm-received.mjml` - Full redesign
6. `mjml-templates/points-balance.mjml` - Full redesign

After MJML changes, regenerate the `.ftlh` files in `src/main/resources/templates/`.

## Out of Scope

- Dark mode support
- Bus icon/logo (keeping text-only branding)
- Shadow effects (poor email client support)
- New email types
