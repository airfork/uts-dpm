# Email Template Redesign - Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Update MJML email templates to match the frontend redesign with gradient buttons, updated colors, and card styling.

**Architecture:** MJML templates compile to FreeMarker (.ftlh) files. Shared header/footer components are included via `mj-include`. Each template follows a consistent structure: header → card content → footer.

**Tech Stack:** MJML 4.x, FreeMarker templates, Mailgun for delivery

**Prerequisites:**
- Install MJML CLI: `npm install -g mjml`
- Or use npx: `npx mjml <file>`

---

## Task 1: Update Header Component

**Files:**
- Modify: `mjml-templates/header.mjml`

**Step 1: Update header.mjml**

Replace the entire contents with:

```xml
<mj-section padding="24px 0 16px 0">
  <mj-column>
    <mj-text
      align="center"
      color="#232d4b"
      font-size="24px"
      font-weight="600"
      font-family="Poppins, Arial, 'Helvetica Neue', Helvetica, sans-serif">
      UTS DPM
    </mj-text>
  </mj-column>
</mj-section>
```

**Step 2: Commit**

```bash
git add mjml-templates/header.mjml
git commit -m "style(email): update header to match redesign

- Change text from 'University Transit Service' to 'UTS DPM'
- Update color to primary-600 (#232d4b)
- Increase font size to 24px with semibold weight"
```

---

## Task 2: Update Footer Component

**Files:**
- Modify: `mjml-templates/footer.mjml`

**Step 1: Update footer.mjml**

Replace the entire contents with:

```xml
<mj-section padding="16px 0 24px 0">
  <mj-column>
    <mj-text
      align="center"
      color="#6b7280"
      font-size="12px"
      font-family="Poppins, Arial, 'Helvetica Neue', Helvetica, sans-serif">
      &copy; ${year} University Transit Service. All rights reserved.
    </mj-text>
  </mj-column>
</mj-section>
```

**Step 2: Commit**

```bash
git add mjml-templates/footer.mjml
git commit -m "style(email): update footer styling

- Update color to neutral-500 (#6b7280)
- Reduce font size to 12px
- Adjust padding for better spacing"
```

---

## Task 3: Redesign Welcome Email

**Files:**
- Modify: `mjml-templates/welcome.mjml`

**Step 1: Update welcome.mjml**

Replace the entire contents with:

```xml
<mjml>
  <mj-head>
    <mj-title>Welcome to UTS DPM</mj-title>
    <mj-font name="Poppins" href="https://fonts.googleapis.com/css?family=Poppins:400,600" />
    <mj-attributes>
      <mj-all font-family="Poppins, Arial, 'Helvetica Neue', Helvetica, sans-serif" />
      <mj-text color="#333333" line-height="1.5" />
    </mj-attributes>
    <mj-style>
      .gradient-btn a {
        background: linear-gradient(to right, #232d4b, #4a5568) !important;
      }
    </mj-style>
  </mj-head>
  <mj-body background-color="#f3f3f3">
    <mj-include path="./header.mjml" />

    <mj-section padding="0 16px 24px 16px">
      <mj-column>
        <mj-wrapper
          background-color="#ffffff"
          border="1px solid #e5e5e5"
          border-radius="16px"
          padding="32px">

          <mj-section padding="0">
            <mj-column>
              <mj-text font-size="18px" font-weight="600" padding-bottom="16px">
                ${name},
              </mj-text>

              <mj-text font-size="14px" padding-bottom="20px">
                Welcome to UTS! You've been added to our Driver Performance Management system. To get started, use your temporary password below to sign in.
              </mj-text>

              <mj-text font-size="12px" color="#6b7280" padding-bottom="8px" text-transform="uppercase" letter-spacing="0.5px">
                Temporary Password
              </mj-text>
            </mj-column>
          </mj-section>

          <mj-section background-color="#f3f3f3" border-radius="8px" padding="16px">
            <mj-column>
              <mj-text font-family="'Courier New', monospace" font-size="16px" font-weight="600" align="center">
                ${password}
              </mj-text>
            </mj-column>
          </mj-section>

          <mj-section padding="24px 0 0 0">
            <mj-column>
              <mj-button
                background-color="#232d4b"
                color="#ffffff"
                border-radius="8px"
                font-weight="600"
                font-size="14px"
                inner-padding="14px 28px"
                css-class="gradient-btn"
                href="${url}">
                Sign In
              </mj-button>

              <mj-text font-size="13px" color="#6b7280" padding-top="20px">
                P.S. This is optional. You'll still receive relevant emails if you don't sign in.
              </mj-text>
            </mj-column>
          </mj-section>

        </mj-wrapper>
      </mj-column>
    </mj-section>

    <mj-include path="./footer.mjml" />
  </mj-body>
</mjml>
```

**Step 2: Compile and verify**

Run: `npx mjml mjml-templates/welcome.mjml -o src/main/resources/templates/welcome.ftlh`

Open the generated file and verify:
- Card has white background with border
- Button has gradient (in supported email preview)
- Password block has gray background

**Step 3: Commit**

```bash
git add mjml-templates/welcome.mjml src/main/resources/templates/welcome.ftlh
git commit -m "style(email): redesign welcome email template

- Add card container with border and rounded corners
- Implement gradient button with fallback
- Style password in monospace code block
- Update colors to match frontend design system"
```

---

## Task 4: Redesign Reset Password Email

**Files:**
- Modify: `mjml-templates/reset-password.mjml`

**Step 1: Update reset-password.mjml**

Replace the entire contents with:

```xml
<mjml>
  <mj-head>
    <mj-title>Password Reset</mj-title>
    <mj-font name="Poppins" href="https://fonts.googleapis.com/css?family=Poppins:400,600" />
    <mj-attributes>
      <mj-all font-family="Poppins, Arial, 'Helvetica Neue', Helvetica, sans-serif" />
      <mj-text color="#333333" line-height="1.5" />
    </mj-attributes>
    <mj-style>
      .gradient-btn a {
        background: linear-gradient(to right, #232d4b, #4a5568) !important;
      }
    </mj-style>
  </mj-head>
  <mj-body background-color="#f3f3f3">
    <mj-include path="./header.mjml" />

    <mj-section padding="0 16px 24px 16px">
      <mj-column>
        <mj-wrapper
          background-color="#ffffff"
          border="1px solid #e5e5e5"
          border-radius="16px"
          padding="32px">

          <mj-section padding="0">
            <mj-column>
              <mj-text font-size="18px" font-weight="600" padding-bottom="16px">
                ${name},
              </mj-text>

              <mj-text font-size="14px" padding-bottom="20px">
                Your password has been reset. Use the temporary password below to sign in and set a new password.
              </mj-text>

              <mj-text font-size="12px" color="#6b7280" padding-bottom="8px" text-transform="uppercase" letter-spacing="0.5px">
                Temporary Password
              </mj-text>
            </mj-column>
          </mj-section>

          <mj-section background-color="#f3f3f3" border-radius="8px" padding="16px">
            <mj-column>
              <mj-text font-family="'Courier New', monospace" font-size="16px" font-weight="600" align="center">
                ${password}
              </mj-text>
            </mj-column>
          </mj-section>

          <mj-section padding="24px 0 0 0">
            <mj-column>
              <mj-button
                background-color="#232d4b"
                color="#ffffff"
                border-radius="8px"
                font-weight="600"
                font-size="14px"
                inner-padding="14px 28px"
                css-class="gradient-btn"
                href="${url}">
                Sign In
              </mj-button>

              <mj-text font-size="13px" color="#6b7280" padding-top="20px">
                If you didn't request this reset, please contact an administrator.
              </mj-text>
            </mj-column>
          </mj-section>

        </mj-wrapper>
      </mj-column>
    </mj-section>

    <mj-include path="./footer.mjml" />
  </mj-body>
</mjml>
```

**Step 2: Compile and verify**

Run: `npx mjml mjml-templates/reset-password.mjml -o src/main/resources/templates/reset-password.ftlh`

**Step 3: Commit**

```bash
git add mjml-templates/reset-password.mjml src/main/resources/templates/reset-password.ftlh
git commit -m "style(email): redesign reset password email template

- Add card container with border and rounded corners
- Implement gradient button with fallback
- Style password in monospace code block
- Update colors to match frontend design system"
```

---

## Task 5: Redesign DPM Received Email

**Files:**
- Modify: `mjml-templates/dpm-received.mjml`

**Step 1: Update dpm-received.mjml**

Replace the entire contents with:

```xml
<mjml>
  <mj-head>
    <mj-title>DPM Received - ${dpmType}</mj-title>
    <mj-font name="Poppins" href="https://fonts.googleapis.com/css?family=Poppins:400,600" />
    <mj-attributes>
      <mj-all font-family="Poppins, Arial, 'Helvetica Neue', Helvetica, sans-serif" />
      <mj-text color="#333333" line-height="1.5" />
    </mj-attributes>
    <mj-style>
      .gradient-btn a {
        background: linear-gradient(to right, #232d4b, #4a5568) !important;
      }
    </mj-style>
  </mj-head>
  <mj-body background-color="#f3f3f3">
    <mj-include path="./header.mjml" />

    <mj-section padding="0 16px 24px 16px">
      <mj-column>
        <mj-wrapper
          background-color="#ffffff"
          border="1px solid #e5e5e5"
          border-radius="16px"
          padding="32px">

          <mj-section padding="0">
            <mj-column>
              <mj-text font-size="18px" font-weight="600" padding-bottom="8px">
                ${name},
              </mj-text>

              <mj-text font-size="14px" color="#6b7280" padding-bottom="16px">
                DPM Received on ${receivedDate}
              </mj-text>

              <mj-text font-size="14px" padding-bottom="24px">
                You received a <strong>${dpmType}</strong> DPM. If you have any questions about this, please contact ${manager} directly.
              </mj-text>

              <mj-button
                background-color="#232d4b"
                color="#ffffff"
                border-radius="8px"
                font-weight="600"
                font-size="14px"
                inner-padding="14px 28px"
                css-class="gradient-btn"
                href="${url}">
                View DPMs
              </mj-button>
            </mj-column>
          </mj-section>

        </mj-wrapper>
      </mj-column>
    </mj-section>

    <mj-include path="./footer.mjml" />
  </mj-body>
</mjml>
```

**Step 2: Compile and verify**

Run: `npx mjml mjml-templates/dpm-received.mjml -o src/main/resources/templates/dpm-received.ftlh`

**Step 3: Commit**

```bash
git add mjml-templates/dpm-received.mjml src/main/resources/templates/dpm-received.ftlh
git commit -m "style(email): redesign DPM received email template

- Add card container with border and rounded corners
- Implement gradient button with fallback
- Show DPM type prominently with date subheading
- Update colors to match frontend design system"
```

---

## Task 6: Redesign Points Balance Email

**Files:**
- Modify: `mjml-templates/points-balance.mjml`

**Step 1: Update points-balance.mjml**

Replace the entire contents with:

```xml
<mjml>
  <mj-head>
    <mj-title>DPM Points Balance</mj-title>
    <mj-font name="Poppins" href="https://fonts.googleapis.com/css?family=Poppins:400,600" />
    <mj-attributes>
      <mj-all font-family="Poppins, Arial, 'Helvetica Neue', Helvetica, sans-serif" />
      <mj-text color="#333333" line-height="1.5" />
    </mj-attributes>
  </mj-head>
  <mj-body background-color="#f3f3f3">
    <mj-include path="./header.mjml" />

    <mj-section padding="0 16px 24px 16px">
      <mj-column>
        <mj-wrapper
          background-color="#ffffff"
          border="1px solid #e5e5e5"
          border-radius="16px"
          padding="32px">

          <mj-section padding="0">
            <mj-column>
              <mj-text font-size="18px" font-weight="600" padding-bottom="16px">
                ${name},
              </mj-text>

              <mj-text font-size="14px" padding-bottom="24px">
                Below is your current points balance. For more details about this value, please contact ${manager}.
              </mj-text>

              <mj-text font-size="12px" color="#6b7280" padding-bottom="8px" text-transform="uppercase" letter-spacing="0.5px">
                Current Balance
              </mj-text>
            </mj-column>
          </mj-section>

          <mj-section background-color="#f3f3f3" border-radius="8px" padding="20px">
            <mj-column>
              <mj-text font-size="28px" font-weight="600" align="center" color="#232d4b">
                ${points} points
              </mj-text>
            </mj-column>
          </mj-section>

        </mj-wrapper>
      </mj-column>
    </mj-section>

    <mj-include path="./footer.mjml" />
  </mj-body>
</mjml>
```

**Step 2: Compile and verify**

Run: `npx mjml mjml-templates/points-balance.mjml -o src/main/resources/templates/points-balance.ftlh`

**Step 3: Commit**

```bash
git add mjml-templates/points-balance.mjml src/main/resources/templates/points-balance.ftlh
git commit -m "style(email): redesign points balance email template

- Add card container with border and rounded corners
- Display balance prominently with large text
- Add 'Current Balance' label
- Update colors to match frontend design system"
```

---

## Task 7: Final Verification

**Step 1: Compile all templates**

Run all compilations to ensure everything works:

```bash
npx mjml mjml-templates/welcome.mjml -o src/main/resources/templates/welcome.ftlh
npx mjml mjml-templates/reset-password.mjml -o src/main/resources/templates/reset-password.ftlh
npx mjml mjml-templates/dpm-received.mjml -o src/main/resources/templates/dpm-received.ftlh
npx mjml mjml-templates/points-balance.mjml -o src/main/resources/templates/points-balance.ftlh
```

**Step 2: Visual verification**

Open each `.ftlh` file in a browser to verify:
- [ ] Header shows "UTS DPM" in navy
- [ ] Card has white background with subtle border
- [ ] Buttons have gradient (in supporting clients)
- [ ] Password/balance blocks have gray background
- [ ] Footer shows copyright in gray
- [ ] All template variables (${name}, ${url}, etc.) are preserved

**Step 3: Test with local profile (optional)**

If you want to test email delivery:
1. Start the app with local profile
2. Trigger each email type
3. Check the override email address

**Step 4: Final commit if any fixes needed**

```bash
git add -A
git commit -m "fix(email): address verification feedback"
```

---

## Summary

| Task | File | Description |
|------|------|-------------|
| 1 | header.mjml | Simplified "UTS DPM" branding |
| 2 | footer.mjml | Updated styling (gray, smaller) |
| 3 | welcome.mjml | Full redesign with card + gradient button |
| 4 | reset-password.mjml | Full redesign with card + gradient button |
| 5 | dpm-received.mjml | Full redesign with card + gradient button |
| 6 | points-balance.mjml | Full redesign with card + prominent balance |
| 7 | Verification | Compile all and verify |

**Total: 7 tasks, ~6 commits**
