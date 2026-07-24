---
name: code-review-java
description: Deep, on-demand code review for the ImageToPaint Java/Quarkus codebase. Use when the user asks to "review my code", "check this class", "review the image pipeline", or wants a review broader than the automatic post-turn diff check (e.green. reviewing a whole feature, a file not yet changed, or explaining *why* something is risky).
argument-hint: "[file, class, or feature to review]"
---

# /code-review-java

Review Java/Quarkus code in ImageToPaint for correctness, safety, and fit with this project's conventions.

## How this differs from the automatic reviewer

This repo already runs an automatic review after every turn (`.claude/agents/code-reviewer.md`, wired via the
`Stop` hook in `.claude/settings.json`): it looks at `git diff` and flags defects in whatever just changed.

**Read `.claude/agents/code-reviewer.md` first — it's the source of truth for the general review criteria**
(correctness, simplicity/reuse, Java 25 idiom fit, build/quality hygiene) and the output format. Apply those
criteria here too, rather than restating them, to avoid the two files drifting apart.

Use this skill instead of relying on the automatic pass when the ask is broader than "what did I just break":
- Reviewing a whole class, package, or feature — not just the diff.
- Reviewing code that isn't changed yet (a file the user points at directly).
- Wanting the reasoning spelled out (failure scenario, why it matters), not just a finding list.
- Reviewing against the project-specific domain risks below, which the generic agent criteria don't cover.

## Additional scope: image-domain risks

On top of `code-reviewer.md`'s general criteria, check for:
- Any new format/decoding path must sniff real format via magic bytes (see `ValidImageValidator`), never trust
  client `Content-Type` or filename extension.
- Any new endpoint or transform touching raw pixel data needs a pixel-count or dimension guard —
  `ValidImageValidator`'s 40,000,000-pixel cap is the existing precedent against decompression-bomb inputs.
- RGB/pixel matrix code should read/write with a single `getRGB`/`setRGB` call over the region, not per-pixel
  calls (see `adapter.BufferedImageConverter` — a deliberate performance choice already established in the repo).
- `ApplicationScoped` beans (`application.ImageFilteringFacade`, `adapter.BufferedImageConverter`,
  `adapter.ImageCodec`) must stay stateless — Quarkus reuses a single instance across requests.
- Only `adapter` classes may depend on external types/libraries (AWT, ImageIO); flag any such dependency
  creeping into `domain` or `application`.

## How to review

- Read the target file(s) fully, plus enough surrounding context (callers, tests) to judge correctness in
  context — don't limit yourself to a diff.
- Check for an existing test (`*Test.java`/`*IT.java`) covering the change; note if one is missing.
- You may run `./mvnw test -Dtest=<Class>` or the quality-tool goals (`checkstyle:checkstyle`, `pmd:pmd`,
  `spotbugs:spotbugs`) to sanity-check; avoid anything destructive.

## Output format

Same format as `code-reviewer.md`: a concise list of findings, each with file:line, a one-sentence description,
and a concrete failure scenario or reason it matters. Group image-domain findings separately from general
findings if both are present. Skip stylistic nitpicks. If nothing significant is wrong, say so plainly.
