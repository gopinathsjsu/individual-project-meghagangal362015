# Getting a log file “from the internet” that fills `apm.json` / `application.json` / `request.json`

## Why most random downloads show empty JSON

This CLI only understands lines made of **space-separated `key=value` tokens** (with optional quoted values). Typical “sample logs” on the web are **Apache / Nginx combined** lines (IP, brackets, quoted HTTP request, etc.). Those lines **do not parse** as `key=value`, so all three JSON files stay **`{}`**.

Examples that usually give **empty** output here:

- Elastic example Apache access log (large file from GitHub raw)
- Common `access.log` / `error.log` samples

## Option A — Use a file in this repo (works offline)

These are **already** valid for your program:

| File | Purpose |
|------|--------|
| `src/test/resources/sample.log` | Short demo (APM + app + request) |
| `docs/synthetic-demo-sample.log` | Longer **original** synthetic demo (same format rules, not from any course handout) |

```bash
cd "/path/to/log-parser-cli"
java -jar target/log-parser-cli-1.0.0.jar --file docs/synthetic-demo-sample.log
```

## Option B — Download from the internet **after** you push to GitHub

Once this project is on GitHub, every file has a **raw** URL you can `curl`:

```text
https://raw.githubusercontent.com/<YOUR_USER>/<YOUR_REPO>/<branch>/docs/synthetic-demo-sample.log
```

Example (replace with your real user, repo, and branch):

```bash
curl -L -o downloaded.log \
  "https://raw.githubusercontent.com/YOUR_USER/YOUR_REPO/main/docs/synthetic-demo-sample.log"

java -jar target/log-parser-cli-1.0.0.jar --file downloaded.log
```

That **is** downloading from the internet, and it will produce **non-empty** JSON because the format matches the parser.

## Option C — GitHub Gist (no full repo required)

1. Go to [gist.github.com](https://gist.github.com) and create a new gist.
2. Paste the contents of `docs/synthetic-demo-sample.log` (or `sample.log`).
3. Click **Create public gist**.
4. Open the gist → **Raw** → copy the URL.
5. Download and run:

```bash
curl -L -o gist-sample.log "https://gist.githubusercontent.com/.../raw/..."
java -jar target/log-parser-cli-1.0.0.jar --file gist-sample.log
```

## Summary

- **Random** “log” URLs from tutorials are often **wrong format** → empty JSON.
- **Reliable** internet flow: host a **`key=value`** file (GitHub repo raw URL or Gist raw URL), then `curl` it and pass `--file` to the JAR.
