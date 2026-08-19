<!--
Sync Impact Report
Version: 1.0.0 → 1.0.0
Modified principles:
- I. Code Quality by Default → I. Code Quality by Default (expanded architecture scope)
- II. Test-First Validation → II. Test-First Validation (clarified coverage scope)
- III. Consistent User Experience → III. Developer Experience & API Contracts
- IV. Performance Budgets → IV. Performance & Efficiency Budgets
Added sections:
- Quality & Validation Standards
- Delivery & Change Control
Removed sections:
- none
Templates updated:
- ✅ .specify/templates/plan-template.md
- ✅ .specify/templates/spec-template.md
- ✅ .specify/templates/tasks-template.md
- ✅ README.md
Follow-up TODOs: none
-->

# Financas Constitution

## Core Principles

### I. Code Quality by Default
Production code MUST be small, explicit, and easy to review. Public APIs,
domain rules, architectural boundaries, and data transformations MUST have one
clear owner and a narrow purpose. Duplication, dead code, and unexplained
abstraction are defects, not style choices. New complexity MUST be justified by
a concrete domain need and kept local to the change.

### II. Test-First Validation
Behavior changes MUST be protected by automated tests that fail before
implementation and pass after it. Logic-heavy changes require focused unit
tests; persistence, API, security, and workflow changes require integration or
contract coverage; bug fixes MUST add a regression test. Flaky or skipped tests
are unacceptable, and a feature is not complete until the relevant test suite
is green.

### III. Developer Experience & API Contracts
The development workflow MUST stay fast, predictable, and reproducible. Build,
lint, and test commands MUST run with documented entry points and clear failure
signals. Public API contracts MUST be explicit, version-aware, and backward
compatible by default; any breaking change MUST be documented, justified, and
accompanied by migration guidance and contract validation.

### IV. Performance & Efficiency Budgets
Features MUST define a measurable performance expectation before implementation
when they affect runtime cost, render time, or data volume. New work MUST avoid
unnecessary round-trips, repeated computations, and unbounded work on the hot
path unless the plan records a justified exception. Regressions against the
agreed budget block merge and require remediation or a revised budget with
explicit approval.

## Quality & Validation Standards

- Every feature plan MUST state the test strategy, API contract validation
  method, DX impact, and any measurable performance constraint.
- Changes that alter module boundaries or cross-layer flows MUST include a short
  architecture note describing ownership and dependency impact.
- Changes that touch persistence, APIs, or security MUST include the narrowest
  practical automated coverage for the affected boundary.
- Performance-sensitive work MUST include a measurable check, such as response
  latency, query count, render time, or payload size.
- Developer-facing changes (tooling, scripts, CI commands) MUST preserve or
  improve setup and feedback-loop efficiency.

## Delivery & Change Control

- Pull requests and implementation plans MUST include a Constitution Check
  section or equivalent validation against this document.
- A change is not eligible for merge until the declared tests pass and any
  DX/API/performance criteria are met or explicitly waived in writing.
- When a principle needs to change, the change MUST be made in a separate
  constitution update with a semantic version bump and propagated templates.
- Repository guidance in README and related workflow docs MUST remain
  consistent with this constitution.

## Governance

This constitution supersedes informal conventions, ad hoc preferences, and
contradictory guidance in lower-priority docs.

Amendments MUST be made through an explicit constitution update that includes:

- A rationale for the change.
- A semantic version bump.
- Propagation to plan, spec, and tasks templates when their guidance is
  affected.
- A Sync Impact Report at the top of this file.

Versioning policy:

- MAJOR for removals or incompatible redefinitions of a principle.
- MINOR for adding a principle or materially expanding governance.
- PATCH for clarifications, wording fixes, or non-semantic refinements.

Compliance review expectations:

- Plans MUST fail fast when they violate this constitution.
- Specs MUST surface measurable requirements for tests, API contracts, DX, and
  performance whenever relevant.
- Tasks MUST reflect the validation work needed to prove the requirements.
- Reviews MUST call out unresolved constitution gaps before implementation is
  considered complete.

**Version**: 1.0.0 | **Ratified**: 2026-07-27 | **Last Amended**: 2026-07-27
