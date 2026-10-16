#!/usr/bin/env python3
"""Fail-fast checks that keep CardControl's regression-review policy wired into CI."""

from pathlib import Path
import re
import sys

ROOT = Path(__file__).resolve().parents[1]


def fail(message: str) -> None:
    print(f"::error::{message}")
    raise SystemExit(1)


def read(relative: str) -> str:
    path = ROOT / relative
    if not path.is_file():
        fail(f"Required review-contract file is missing: {relative}")
    return path.read_text(encoding="utf-8")


skill_path = ".agents/skills/regression-review/SKILL.md"
skill = read(skill_path)
if not re.search(r"(?m)^---\s*\nname:\s*regression-review\s*\ndescription:\s*.+\n---\s*$", skill):
    fail("regression-review SKILL.md needs valid name/description frontmatter")

required_topics = [
    "normalizacao", "cobranca mais recente", "sinalizadores manuais",
    "SQLite real", "Valores em centavos", "CI/CD", "auditor independente",
]
for topic in required_topics:
    if topic.casefold() not in skill.casefold():
        fail(f"regression-review skill is missing required topic: {topic}")

agents = read("AGENTS.md")
if skill_path not in agents:
    fail("AGENTS.md must register the mandatory regression-review skill")

state_checker = read(".agents/skills/agent-state-sync/scripts/check_state.ps1")
if "'regression-review'" not in state_checker:
    fail("agent-state-sync must treat regression-review as a required skill")

workflow = read(".github/workflows/ci.yml")
if "scripts/check_review_contract.py" not in workflow:
    fail("Android CI must execute scripts/check_review_contract.py")

tracker_tests = read("app/src/test/java/com/cardcontrol/app/SubscriptionTrackerTest.kt")
for marker in [
    "ultima cobranca irregular",
    "parcelamento confirmado manualmente",
    "ultrapassa 100 por cento",
    "limites de ciclo documentados entre 28 e 32 dias",
]:
    if marker.casefold() not in tracker_tests.casefold():
        fail(f"Missing subscription regression test marker: {marker}")

repository_tests = read("app/src/test/java/com/cardcontrol/app/CardRepositoryRecurringSyncTest.kt")
if "variacoes de caixa" not in repository_tests.casefold():
    fail("Repository sync must have a merchant-normalization duplicate regression test")

dao = read("app/src/main/java/com/cardcontrol/app/data/local/AppDao.kt")
if "(:isRecurring = 0 OR installmentsCount = 1)" not in dao:
    fail("DAO must reject setting recurrence on installment purchases")

ui = read("app/src/main/java/com/cardcontrol/app/ui/MainScreen.kt")
if "inst.totalInstallments == 1" not in ui or '"Parcelamento"' not in ui:
    fail("UI must hide recurring action and explain installment rows")

guard_test = read("app/src/test/java/com/cardcontrol/app/RecurringTransactionGuardTest.kt")
if "bloqueia recorrencia manual em compra parcelada" not in guard_test.casefold():
    fail("A real Room DAO regression test must enforce the installment guard")

migration_test = read("app/src/test/java/com/cardcontrol/app/RoomV4ToV5MigrationTest.kt")
if "java.lang.reflect.Proxy" in migration_test or "Proxy.newProxyInstance" in migration_test:
    fail("Room migration test must execute against SQLite, not a SQL-recording proxy")
if "FrameworkSQLiteOpenHelperFactory" not in migration_test or "MIGRATION_4_5.migrate" not in migration_test:
    fail("Room migration test must invoke MIGRATION_4_5 against an actual SQLite database")

print("Review contract passed: skill registration, regression cases, real-SQLite migration test, and CI wiring.")
