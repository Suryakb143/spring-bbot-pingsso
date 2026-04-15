# How to Convert Diagrams to PNG

All 6 diagrams have been saved as `.mmd` (Mermaid) files in the `diagrams/` folder:

```
diagrams/
├── 01-component-architecture.mmd
├── 02-login-flow.mmd
├── 03-authenticated-request-flow.mmd
├── 04-api-endpoints-security.mmd
├── 05-startup-initialization.mmd
└── 06-file-structure-dependencies.mmd
```

## Method 1: Online Mermaid Converter (Easiest)

1. Go to: **https://mermaid.live/**
2. Copy contents of a `.mmd` file
3. Paste into the left panel
4. Click **Download** in the top-right
5. Select **PNG** format
6. Save the file

Repeat for each diagram.

## Method 2: Mermaid CLI (Local)

Install globally:
```bash
npm install -g @mermaid-js/mermaid-cli
```

Convert all diagrams:
```bash
cd diagrams
mmdc -i 01-component-architecture.mmd -o 01-component-architecture.png
mmdc -i 02-login-flow.mmd -o 02-login-flow.png
mmdc -i 03-authenticated-request-flow.mmd -o 03-authenticated-request-flow.png
mmdc -i 04-api-endpoints-security.mmd -o 04-api-endpoints-security.png
mmdc -i 05-startup-initialization.mmd -o 05-startup-initialization.png
mmdc -i 06-file-structure-dependencies.mmd -o 06-file-structure-dependencies.png
```

Or create a batch script `convert-diagrams.bat`:
```batch
@echo off
cd diagrams
for %%f in (*.mmd) do (
    mmdc -i %%f -o %%~nf.png
)
echo All diagrams converted to PNG!
pause
```

Run it:
```bash
convert-diagrams.bat
```

## Method 3: VS Code Extension

1. Install **Mermaid Markdown Syntax Highlighting** extension
2. Install **Markdown Preview Mermaid Support** extension
3. Open `.mmd` file in VS Code
4. Right-click → **Open Preview to the Side** (Ctrl+Shift+V)
5. Right-click diagram → **Save as PNG**

## Method 4: Docker

If you have Docker installed:
```bash
docker run --rm -v $(pwd)/diagrams:/data ghcr.io/mermaid-js/mermaid-cli/mermaid-cli:latest -i /data/01-component-architecture.mmd -o /data/01-component-architecture.png
```

---

## Diagram Files Reference

| # | File | Description |
|---|------|-------------|
| 1 | 01-component-architecture.mmd | All layers: Client, API, Security, Services, Entities, Repos, DB, Config |
| 2 | 02-login-flow.mmd | Detailed login sequence: user creation → session → token → response |
| 3 | 03-authenticated-request-flow.mmd | Secured API flow: bearer token validation → session checks → queries |
| 4 | 04-api-endpoints-security.mmd | All 9 endpoints, filters, database ops, response codes |
| 5 | 05-startup-initialization.mmd | Application startup: 7-step initialization, filter setup, routes ready |
| 6 | 06-file-structure-dependencies.mmd | Complete file tree, dependencies, Spring Boot + Angular structure |

---

## Recommended: Method 1 (Online Converter)

For quick PNG exports without installing software:

1. Open **https://mermaid.live/**
2. One at a time, paste each `.mmd` file content
3. Click **Download** → **PNG**
4. That's it!

All diagrams will be saved to your Downloads folder.

---

## Need Help?

- **Mermaid Docs:** https://mermaid.js.org/
- **Live Editor:** https://mermaid.live/
- **CLI Installation:** https://github.com/mermaid-js/mermaid-cli
