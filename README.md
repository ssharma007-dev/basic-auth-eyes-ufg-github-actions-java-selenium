# basic-auth-eyes-ufg-github-actions-java-selenium

Minimal reproducer: opens a basic-auth-protected URL in a real, headless
Chrome browser and takes a single Applitools Eyes checkpoint. No login flow,
no extra steps — just enough to show what actually renders at that URL.
Runnable locally or via the included GitHub Actions workflow.

The whole test is `src/test/java/com/applitools/repro/BasicAuthUrlEyesTest.java`.
No credentials or URLs are hardcoded anywhere in this repo — everything
comes from environment variables you set yourself.

## How it works

`BasicAuthUrlEyesTest` (`src/test/java/com/applitools/repro/BasicAuthUrlEyesTest.java`)
does exactly five things:

1. **Reads config from environment variables** — `URL`,
   `BASIC_AUTH_USERNAME`, `BASIC_AUTH_PASSWORD` (`APPLITOOLS_API_KEY` is
   read automatically by the Eyes SDK). Nothing is hardcoded, so the same
   jar/repo works for anyone's URL and credentials.

2. **Launches headless Chrome** via Selenium (`WebDriverManager` fetches a
   matching `chromedriver` automatically — no manual driver setup). A 45s
   page-load timeout keeps the test from hanging if a page never fires its
   `load` event.

3. **Handles basic auth by embedding credentials in the URL** —
   `https://user:pass@host/...` — rather than intercepting auth via Chrome
   DevTools Protocol (CDP). This is deliberate: CDP's auth interception API
   depends on Selenium's DevTools implementation matching the exact Chrome
   version, which breaks easily across Chrome updates. URL-embedded
   credentials work regardless of Chrome/Selenium version.

4. **Navigates to the page and lets Chrome fully render it** — including
   any content loaded or populated by JavaScript after the initial HTML
   response, which a non-browser HTTP check (e.g. `curl`) would never see.

5. **Takes one Applitools Eyes checkpoint** (`eyes.check(Target.window())`)
   of what's actually on screen, uploads it to Applitools' Ultrafast Grid,
   and prints a dashboard link where you can see exactly what was rendered.

## Prerequisites

- Java 17+
- Maven
- Google Chrome installed
- An Applitools API key ([sign up free](https://applitools.com/) if you
  don't have one)

## Setup

Set these environment variables:

```bash
export APPLITOOLS_API_KEY=your-api-key
export URL="https://your-basic-auth-protected-page"
export BASIC_AUTH_USERNAME=your-username
export BASIC_AUTH_PASSWORD=your-password
```

## Run

```bash
mvn test
```

That's it. The test:

1. Opens `URL` in headless Chrome (embedding `BASIC_AUTH_USERNAME` /
   `BASIC_AUTH_PASSWORD` into the URL for basic auth)
2. Waits for the page to load
3. Takes one Applitools Eyes checkpoint of what's rendered
4. Prints a result summary with a link to view the checkpoint on the
   Applitools dashboard

## Output

```
result summary {
    all results=
        TestResultContainer{
 testResults=New test [ steps: 1, test name: URL check, matches: 0, mismatches:0, missing: 0] , URL: https://eyes.applitools.com/app/batches/...
 ...
    passed=1
    failed=0
}
```

Open the printed URL to see exactly what the browser rendered at that page.

## Running in GitHub Actions

The workflow at `.github/workflows/eyes-repro.yml` runs on push/PR to
`main`, or manually via "Run workflow". To enable it:

1. Push this repo to GitHub.
2. Go to **Settings → Secrets and variables → Actions** and add these
   repository secrets:
   - `APPLITOOLS_API_KEY`
   - `URL`
   - `BASIC_AUTH_USERNAME`
   - `BASIC_AUTH_PASSWORD`
3. Push to `main`, open a PR, or trigger it manually from the Actions tab.

**Since this repo is public:** repository secrets are never exposed in
logs or to outside contributors, but GitHub does not pass secrets to
workflow runs triggered by pull requests from forks — only to runs on
branches/PRs within this repo, or manual `workflow_dispatch` runs. That's
a GitHub security default, not something this project configures.

## Why this matters

If a plain HTTP request (e.g. `curl`) to the same URL looks empty or
different from what a real browser shows, that's usually because the page
renders some or all of its content client-side via JavaScript after the
initial HTML loads. This test uses an actual browser (Selenium + Chrome),
so it waits for and captures the fully rendered page — including anything
populated by JavaScript after page load — not just the raw HTML response.
