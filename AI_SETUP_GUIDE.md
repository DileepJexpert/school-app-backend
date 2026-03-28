# AI Homework Helper — Setup & Configuration Guide

## Quick Start (Free with Ollama — 5 minutes)

### Step 1: Install Ollama

```bash
# Linux / WSL
curl -fsSL https://ollama.ai/install.sh | sh

# macOS
brew install ollama

# Windows
# Download from https://ollama.com/download
```

### Step 2: Pull a Model

```bash
# Best for homework help (8B params, fast, good quality)
ollama pull llama3

# Verify it's downloaded
ollama list
```

### Step 3: Start Ollama

```bash
ollama serve
# Ollama is now running at http://localhost:11434
# Verify: curl http://localhost:11434/api/tags
```

### Step 4: Enable AI in Your School

1. Login as **SCHOOL_ADMIN** or **SUPER_ADMIN**
2. Go to **AI Settings** in the admin sidebar
3. Toggle **Enable AI Homework Helper** → ON
4. Primary Provider → **Ollama (Free Local)**
5. Enabled Modes → check TUTOR, SOLVE, PRACTICE (all 3)
6. Click **Save Settings**

### Step 5: Test It!

1. Login as **STUDENT**
2. Go to **AI Helper** tab
3. Pick a mode → Type a question → See the AI respond!

---

## Switching Models (Super Easy!)

### Change Ollama Model

You can switch models **instantly** from the admin UI — no code changes needed.

**From the UI:**
Admin → AI Settings → Ollama Model Name → type the new model → Save

**Available Ollama models (all free):**

| Model | Size | Best For | Pull Command |
|-------|------|----------|--------------|
| `llama3` | 4.7 GB | General homework help (recommended) | `ollama pull llama3` |
| `llama3:8b` | 4.7 GB | Same as above (explicit size) | `ollama pull llama3:8b` |
| `llama3:70b` | 40 GB | Better quality, needs 48GB+ RAM | `ollama pull llama3:70b` |
| `mistral` | 4.1 GB | Fast, good for math | `ollama pull mistral` |
| `phi3` | 2.2 GB | Smallest, fastest, Microsoft | `ollama pull phi3` |
| `gemma2` | 5.4 GB | Google's model, good reasoning | `ollama pull gemma2` |
| `qwen2` | 4.4 GB | Good for multilingual (Hindi) | `ollama pull qwen2` |
| `codellama` | 3.8 GB | Best for coding/CS homework | `ollama pull codellama` |
| `llama3.1` | 4.7 GB | Newer Llama, slightly better | `ollama pull llama3.1` |
| `deepseek-r1` | 4.7 GB | Great reasoning, math | `ollama pull deepseek-r1` |

**To switch models:**
```bash
# 1. Pull the new model
ollama pull mistral

# 2. Change in Admin UI: AI Settings → Ollama Model → "mistral" → Save
# That's it! Next student question will use the new model.
```

**Try different models side-by-side:**
```bash
# Pull multiple models
ollama pull llama3
ollama pull mistral
ollama pull phi3

# Switch between them from the Admin UI whenever you want
# No restart needed — just change the model name and save
```

### Switch to Gemini (Paid, Cheapest Cloud)

1. Get API key from https://aistudio.google.com/apikey (free tier: 15 req/min)
2. Admin → AI Settings → Primary Provider → **Google Gemini**
3. Paste your Gemini API Key
4. Model: `gemini-2.0-flash` (cheapest) or `gemini-1.5-pro` (better)
5. Save

**Gemini Models:**
| Model | Cost per 1M tokens | Quality |
|-------|-------------------|---------|
| `gemini-2.0-flash` | $0.10 input / $0.40 output | Good, fastest |
| `gemini-1.5-flash` | $0.075 input / $0.30 output | Good |
| `gemini-1.5-pro` | $3.50 input / $10.50 output | Best |

### Switch to Claude (Paid, Best Quality)

1. Get API key from https://console.anthropic.com/settings/keys
2. Admin → AI Settings → Primary Provider → **Claude**
3. Paste your Claude API Key
4. Model: `claude-sonnet-4-20250514` (recommended) or `claude-haiku-4-5-20251001` (cheaper)
5. Save

**Claude Models:**
| Model | Cost per 1M tokens | Quality |
|-------|-------------------|---------|
| `claude-haiku-4-5-20251001` | $1.00 input / $5.00 output | Good, cheapest |
| `claude-sonnet-4-20250514` | $3.00 input / $15.00 output | Great (recommended) |
| `claude-opus-4-20250514` | $15.00 input / $75.00 output | Best, expensive |

---

## Understanding the Logs

When a student asks a question, you'll see logs like this in your Spring Boot console:

```
========== AI CHAT REQUEST START ==========
  Student     : Rahul Kumar (id=abc123)
  Tenant      : springfield
  Mode        : TUTOR
  Message     : What is photosynthesis? (24chars)
  HomeworkId  : hw456
  ConvId      : new conversation
  Language    : en
  [CONFIG] enabled=true, provider=OLLAMA, fallback=GEMINI, model=llama3, dailyLimit=20
  [CONFIG] enabledModes=[TUTOR, SOLVE, PRACTICE]
  [LIMITS] Questions today: 3/20
  [CONV] Linked to homework: 'Science Chapter 5' (Science / Class 7 - A)
  [CONV] New conversation created — mode=TUTOR
  [PROMPT] Mode=TUTOR, Subject=Science, Class=Class 7 - A, Language=en
  [PROMPT] System prompt length: 287 chars
  [TURNS] Current messages: 2/60
  [AI CALL] Calling PRIMARY provider: OLLAMA (model=llama3)
  [AI CALL] Sending 2 messages to AI...
    ┌─── OLLAMA REQUEST ───────────────────────
    │ URL   : http://localhost:11434/api/chat
    │ Model : llama3
    │ Messages being sent to Ollama:
    │   [0] SYSTEM → "You are a Socratic tutor for school students. Subject: Science. ..."
    │   [1] USER → "What is photosynthesis?"
    │ Calling Ollama API (stream=false)...
    │
    │ ✓ OLLAMA RESPONSE
    │ Time         : 3200ms (3.2s)
    │ Input tokens : 156 (prompt)
    │ Output tokens: 89 (generated)
    │ Total tokens : 245
    │ Response len : 412 chars
    │ Cost         : $0.00 (FREE - local model)
    │ AI says: "Great question! Let's think about this together. Have you ever ..."
    │ Model used: llama3
    │ Ollama eval_duration: 2800ms (31.8 tokens/sec)
    └───────────────────────────────────────
  [AI CALL] SUCCESS in 3200ms
  [AI RESPONSE] Provider    : OLLAMA
  [AI RESPONSE] Input tokens : 156
  [AI RESPONSE] Output tokens: 89
  [AI RESPONSE] Response length: 412 chars
  [AI RESPONSE] Preview: Great question! Let's think about this together. Have you ever noticed...
  [CONV] Saved — total messages now: 3
  [COST] Estimated: $0.000000 (FREE - local model)
  [USAGE] Student daily usage: 4/20
  [USAGE] Conversation total tokens: in=156 out=89
========== AI CHAT REQUEST COMPLETE ==========
```

### What Each Log Section Means

| Log Section | What It Tells You |
|-------------|-------------------|
| `AI CHAT REQUEST START` | Who asked, what mode, which homework |
| `[CONFIG]` | Which AI provider and model will be used |
| `[LIMITS]` | How many questions the student has used today |
| `[CONV]` | New conversation or continuing existing one |
| `[PROMPT]` | The system prompt (instructions) sent to AI |
| `[AI CALL]` | Which provider is being called, with what config |
| `OLLAMA/GEMINI/CLAUDE REQUEST` | Detailed API call: URL, model, messages sent |
| `✓ RESPONSE` | Response time, tokens, cost, what AI said |
| `[COST]` | How much this request costs ($0 for Ollama) |
| `[USAGE]` | Running totals for the day/conversation |

### Error Logs

If Ollama is not running:
```
    │ OLLAMA ERROR after 500ms: Connection refused
    │ Is Ollama running? Check: curl http://localhost:11434/api/tags
    │ Is model 'llama3' pulled? Run: ollama pull llama3
```

If a model isn't pulled:
```
    │ OLLAMA ERROR after 200ms: 404 Not Found
    │ Is model 'mistral' pulled? Run: ollama pull mistral
```

If primary fails and fallback works:
```
  [AI CALL] PRIMARY provider OLLAMA FAILED after 500ms: Connection refused
  [AI CALL] Trying FALLBACK provider: GEMINI
    ┌─── GEMINI REQUEST ──────────────────────
    ...
  [AI CALL] FALLBACK SUCCESS in 1200ms
```

---

## Configuration Options (Admin UI → AI Settings)

| Setting | Default | What It Does |
|---------|---------|-------------|
| **Enable AI** | OFF | Master switch. Must be ON for students to use AI |
| **Enabled Modes** | TUTOR only | Which modes students can use |
| **Primary Provider** | OLLAMA | Which AI to call first |
| **Fallback Provider** | GEMINI | Backup if primary fails (optional) |
| **Ollama URL** | http://localhost:11434 | Where Ollama is running |
| **Ollama Model** | llama3 | Which Ollama model to use |
| **Gemini API Key** | (empty) | Your Google AI key |
| **Claude API Key** | (empty) | Your Anthropic key |
| **Daily Limit** | 20 | Max questions per student per day |
| **Max Turns** | 30 | Max back-and-forth per conversation |

---

## Recommended Setup by Budget

### Free (Testing/Small School)
- Provider: **Ollama**
- Model: **llama3** or **phi3** (if low RAM)
- Daily Limit: 50
- Needs: Any computer with 8GB+ RAM

### Low Budget ($5-10/month)
- Provider: **Gemini** (gemini-2.0-flash)
- Fallback: **Ollama**
- Daily Limit: 20
- Cost: ~$0.01 per 100 questions

### Best Quality
- Provider: **Claude** (claude-sonnet-4-20250514)
- Fallback: **Gemini**
- Daily Limit: 10
- Cost: ~$0.50 per 100 questions

---

## Troubleshooting

| Problem | Solution |
|---------|----------|
| "AI is not enabled" | Admin → AI Settings → toggle Enable ON → Save |
| "Ollama connection refused" | Run `ollama serve` in terminal |
| "Model not found" | Run `ollama pull llama3` |
| "Gemini API key not configured" | Add your key in AI Settings |
| "Daily limit reached" | Wait for next day, or admin increases the limit |
| AI responds slowly | Try a smaller model: `phi3` or `mistral` |
| AI gives bad answers | Try a bigger model: `llama3:70b` or switch to Gemini/Claude |
| AI responds in wrong language | Student can set language in chat, or admin restricts it |

## Hardware Requirements for Ollama

| Model | RAM Needed | GPU Needed | Speed |
|-------|-----------|------------|-------|
| phi3 (3B) | 4 GB | No | Very Fast |
| llama3 (8B) | 8 GB | No (but faster with GPU) | Fast |
| mistral (7B) | 8 GB | No | Fast |
| llama3 (70B) | 48 GB | Recommended | Slow |
