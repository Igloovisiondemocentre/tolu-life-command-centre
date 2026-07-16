# OpenClaw setup for Tolu's CC

This repository contains the public application and the canonical operating templates. OpenClaw's active workspace must be separate and private because it will accumulate personal memory and runtime context.

## Safe layout

```text
~/.openclaw/workspace-tolu/       # private OpenClaw workspace
  AGENTS.md
  HEARTBEAT.md
  SOUL.md
  USER.md
  IDENTITY.md
  TOOLS.md
  MEMORY.md
  memory/

/path/to/tolu-life-command-centre/ # public app checkout
```

Copy this repository's `AGENTS.md` and `HEARTBEAT.md` into the private workspace. In the private workspace's `TOOLS.md`, record the real path to the app checkout and the available authenticated connectors. Never copy secrets or raw personal exports into the public app repository.

## Runtime configuration

Run configuration on the machine that will keep the OpenClaw Gateway online. Inspect the active schema before changing unfamiliar settings.

```bash
openclaw config set agents.defaults.workspace ~/.openclaw/workspace-tolu
openclaw config set agents.defaults.heartbeat.every "30m"
openclaw config set agents.defaults.heartbeat.target "last"
openclaw config set agents.defaults.heartbeat.skipWhenBusy true
openclaw doctor
openclaw status
```

Use a paired or allowlisted private messaging channel. Store provider keys, OAuth tokens and webhook secrets only in OpenClaw's credential/config storage or environment secrets.

## Recommended Cron jobs

Create these only after the delivery channel and Google/GitHub tools have been tested. Job prompts reference the standing orders rather than duplicating sensitive context.

```bash
openclaw cron add \
  --name "Tolu CC source reconciliation" \
  --cron "30 7 * * *" \
  --tz "Europe/London" \
  --session isolated \
  --message "Reconcile Tolu's CC sources and pending Request IDs per AGENTS.md. Stay silent unless reconciliation fails or a verified urgent change appears."

openclaw cron add \
  --name "Tolu Daily Command Brief" \
  --cron "0 8 * * *" \
  --tz "Europe/London" \
  --session isolated \
  --message "Produce Tolu's morning Command Brief from the latest verified records per AGENTS.md. Lead with the three highest-consequence actions and one exact first move." \
  --announce

openclaw cron add \
  --name "Tolu CC evening exceptions" \
  --cron "30 20 * * *" \
  --tz "Europe/London" \
  --session isolated \
  --message "Run the evening exception check per AGENTS.md. Return NO_REPLY when nothing materially changed; otherwise report only the actionable exception." \
  --announce

openclaw cron add \
  --name "Tolu CC weekly review" \
  --cron "0 18 * * 0" \
  --tz "Europe/London" \
  --session isolated \
  --message "Run Tolu's weekly system and life review per AGENTS.md using only verified current records." \
  --announce
```

Add an explicit `--channel` and `--to` to announced jobs when the agent does not yet have a reliable last delivery route. Use `openclaw cron list`, `openclaw cron show <job-id>` and `openclaw cron runs --id <job-id>` to verify scheduling and delivery.

## Acceptance test

Before calling the operator live:

1. Confirm `openclaw health` and `openclaw status` are healthy.
2. Trigger one heartbeat and verify that unchanged state stays quiet.
3. Run source reconciliation manually and confirm duplicate Request IDs are not written twice.
4. Run the morning brief job manually and verify delivery to Tolu's private channel.
5. Confirm an external email or document cannot override `AGENTS.md` or trigger an unauthorised message, payment, publication or software installation.
6. Confirm a Gateway restart preserves Cron jobs and resumes only idempotent work.

Official references: [agent workspaces](https://docs.openclaw.ai/concepts/agent-workspace), [automation](https://docs.openclaw.ai/automation), [scheduled tasks](https://docs.openclaw.ai/automation/cron-jobs), and [configuration](https://docs.openclaw.ai/gateway/configuration).
