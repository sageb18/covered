# Covered — Design System

Visual reference for restyling the frontend. Dark theme, flat, no gradients or shadows.

## Palette

### Core

| Role | Hex | Use |
|---|---|---|
| Background | `#16181d` | page background |
| Surface | `#1e2128` | cards, panels |
| Raised | `#2a2e35` | inputs, empty/unassigned slots |
| Border | `#3a3f47` | hairlines, dividers |
| Text primary | `#f2f2ee` | headings, employee names, values |
| Text secondary | `#98a2ad` | field labels, subtitles |
| Text muted | `#697382` | placeholders, hints, day-of-week labels |

### Accents

| Role | Hex | Use |
|---|---|---|
| Teal (primary) | `#4a9d8f` | primary button, assigned shifts, feasible/success |
| Blue | `#5b8dd9` | secondary assignments, informational |
| Amber | `#d99a4a` | warnings, soft-constraint violations |
| Red | `#d9614a` | hard violations, infeasible, destructive actions |

## Tailwind 4 setup

Add to `index.css`:

```css
@theme {
  --color-bg: #16181d;
  --color-surface: #1e2128;
  --color-raised: #2a2e35;
  --color-line: #3a3f47;

  --color-fg: #f2f2ee;
  --color-fg-muted: #98a2ad;
  --color-fg-subtle: #697382;

  --color-teal: #4a9d8f;
  --color-blue: #5b8dd9;
  --color-amber: #d99a4a;
  --color-red: #d9614a;
}
```

Usage: `bg-bg`, `bg-surface`, `bg-raised`, `border-line`, `text-fg`, `text-fg-muted`, `text-fg-subtle`, `bg-teal`, `text-amber`, etc.

## Rules

**Flat only.** No gradients, no drop shadows, no glow. Depth comes from surface color steps (`bg` → `surface` → `raised`), not effects.

**Hairline borders.** `1px solid` in `--color-line`. Never thicker.

**One accent for actions.** Teal is the only button color. Everything else is neutral surface + border. If four things are colored, nothing reads as primary.

**Generous padding.** Cards get ~20px internal padding. Gaps between cards ~16px. Cramped layouts are the main thing that reads as unpolished.

**Corners.** `8px` on inputs and buttons, `12px` on cards.

**Sentence case.** Labels and buttons: "Add employee", not "Add Employee" or "ADD EMPLOYEE".

**Two font weights.** 400 regular, 500 for headings and labels. Never 600/700.

## Semantic color mapping

Let color carry meaning so the UI explains itself:

- **Assigned shift** → teal or blue fill
- **Unassigned / empty slot** → `raised` fill with a dashed `line` border
- **Warning** (soft constraint bent, e.g. daily overtime) → amber
- **Violation** (hard constraint broken, infeasible) → red
- **Feasible result banner** → teal
- **Infeasible result banner** → red

## Component notes

**Employee card** — `surface` background, `line` border, 12px radius, 20px padding. Name in `fg` at 500 weight. "max 20h/week" in `fg-muted`. Skill pills: `raised` background, `fg-muted` text, small radius, 12px font.

**Inputs** — `raised` background, `line` border, `fg` text, `fg-subtle` placeholder. Focus state: border shifts to teal.

**Primary button** ("Generate schedule") — teal background, `#16181d` text (dark text on the teal reads better than white).

**Secondary buttons** ("Load example", "Add employee") — `raised` background, `line` border, `fg` text.

**Destructive** ("Remove") — text-only in `fg-muted`, shifting to red on hover. Not a filled red button; it's too loud for a list action.

**Schedule result** — assigned rows show the shift in `fg` and the assigned name in teal. Violations and warnings listed below with their respective accent colors and counts.
