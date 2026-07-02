# basic-auth-eyes-ufg-github-actions-java-selenium

## Run locally

Requires Java 17+, Maven, and Chrome installed.

```bash
git clone https://github.com/ssharma007-dev/basic-auth-eyes-ufg-github-actions-java-selenium.git
cd basic-auth-eyes-ufg-github-actions-java-selenium

export APPLITOOLS_API_KEY=your-api-key
export URL="https://your-basic-auth-protected-page"
export BASIC_AUTH_USERNAME=your-username
export BASIC_AUTH_PASSWORD=your-password

mvn test
```

## Run via GitHub Actions

After cloning/forking, go to **Settings → Secrets and variables →
Actions** on your copy of the repo and add these repository secrets:

- `APPLITOOLS_API_KEY`
- `URL`
- `BASIC_AUTH_USERNAME`
- `BASIC_AUTH_PASSWORD`

Then push to `main`, open a PR, or trigger the workflow manually from the
**Actions** tab.
