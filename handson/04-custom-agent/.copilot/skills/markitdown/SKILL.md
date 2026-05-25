---
name: markitdown
description: "Convert PDF, Word, PowerPoint, Excel, HTML, CSV, images, and audio files to clean Markdown using Microsoft's open-source markitdown tool. Use when the user wants to convert binary office documents to markdown for downstream LLM / agent processing, batch-convert a folder of specs, or extract structured text from PDFs while preserving headings and tables."
---

# markitdown — Office / PDF → Markdown

## Overview

[microsoft/markitdown](https://github.com/microsoft/markitdown) is an MIT-licensed Python tool that converts a wide range of binary formats to clean Markdown. Use it whenever the user has a non-text document (PDF, Word, PowerPoint, Excel, images, audio) and wants the contents as Markdown — Markdown is the most LLM-friendly format (preserves headings, lists, tables; minimal token waste).

**Why prefer this over `pdftotext` / `marker` / raw `pypdf`:**
- One tool, many formats — no need to branch per file type
- Output preserves heading levels, lists, and tables (markdown tables)
- Built-in OCR fallback for image-based PDFs
- Speaker notes from PPT and per-sheet tables from Excel come out clean

## Installation

```bash
# Full install (recommended — pulls all optional format plugins)
pip install 'markitdown[all]'

# Or minimal + only what you need
pip install 'markitdown[pdf,docx,pptx,xlsx]'
```

Check it works:
```bash
markitdown --version
```

## Supported formats

| Extension                          | Notes                                   |
| ---------------------------------- | --------------------------------------- |
| `.pdf`                             | Text + OCR fallback for scanned pages   |
| `.docx`                            | Headings, lists, tables preserved       |
| `.pptx`                            | Each slide → section; speaker notes incl. |
| `.xlsx`, `.xls`                    | Each sheet → markdown table             |
| `.html`, `.htm`                    | Stripped to semantic markdown           |
| `.csv`, `.tsv`, `.json`, `.xml`    | Formatted tables / fenced blocks        |
| `.jpg`, `.png`                     | OCR via Tesseract / Azure DI            |
| `.mp3`, `.wav`, `.m4a`             | Transcribed via Whisper                 |
| `.zip`                             | Recursively converts archive contents   |
| URLs (http/https)                  | Fetches and converts                    |

## Quick start

### Single file
```bash
markitdown input.pdf -o output.md
```

### Read from stdin / pipe out
```bash
markitdown < input.docx > output.md
```

### Batch convert a folder
```bash
for f in specs/*.pdf; do
  markitdown "$f" -o "${f%.pdf}.md"
done
```

### From a URL
```bash
markitdown https://example.com/whitepaper.pdf -o whitepaper.md
```

### Use as a Python library
```python
from markitdown import MarkItDown

md = MarkItDown()
result = md.convert("spec.pdf")
print(result.text_content)
```

## Tips

- **Don't paste full output back into chat** — large docs blow up context. Save to `.md`, then read with `view` (with `view_range` if huge) or grep.
- **Tables sometimes drift columns** — sanity-check the first table in long PDFs before downstream processing.
- **Multi-column PDFs** (academic papers, brochures) may interleave text by visual order; warn the user.
- **Scanned / image PDFs** — OCR quality depends on language packs. For Chinese, install Tesseract `chi_tra` / `chi_sim`. For higher quality consider Azure Document Intelligence (out of scope here).
- **Pipelines** — markitdown plays well as a preprocessor before any agent that expects markdown input (e.g., spec analyzers, code-gen from requirements).

## When NOT to use markitdown

- If the file is already `.md` / `.txt` — just read it directly with `view`.
- If you only need structured field extraction from forms — use the `pdf` skill (`pypdf` + form-field APIs) instead.
- If the user wants to **edit** an Office file in place — use the `docx` / `pptx` / `xlsx` skills.

## Common workflows

### 1. Spec PDF → analyze
```bash
markitdown spec.pdf -o spec.md
# then: read spec.md, summarize, identify ambiguities
```

### 2. Folder of meeting notes (mixed .docx / .pdf) → searchable corpus
```bash
mkdir -p out
for f in notes/*.{pdf,docx}; do
  [ -e "$f" ] || continue
  markitdown "$f" -o "out/$(basename "${f%.*}").md"
done
```

### 3. Slide deck → speaker-notes extraction
```bash
markitdown deck.pptx -o deck.md   # speaker notes appear under each slide
```
