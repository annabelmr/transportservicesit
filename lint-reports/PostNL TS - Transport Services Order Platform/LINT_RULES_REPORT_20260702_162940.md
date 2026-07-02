# Lint Rules Report
**Project:** PostNL TS - Transport Services Order Platform  
**Date:** 2026-07-02  
**mxcli Version:** v0.7.0  
**Mendix Version:** 11.6.5

---

## Executive Summary

| Metric | Count |
|--------|-------|
| **Total Issues** | 296 |
| **Critical (✗)** | 118 |
| **Warnings (⚠)** | 108 |
| **Info (ℹ)** | 70 |

---

## Issues by Rule

### 🔴 Top Issues (by frequency)

| Rank | Rule | Count | Severity | Category |
|------|------|-------|----------|----------|
| 1 | **PNL016** | 60 | ℹ | Orphaned elements - unreferenced microflows/pages |
| 2 | **PNL003** | 58 | ⚠ | Documentation missing (standard) |
| 3 | **PNL002** | 56 | ✗ | Documentation missing (critical) |
| 4 | **SEC001** | 31 | ⚠ | Persistent entity without access rules |
| 5 | **PNL001** | 29 | ✗ | Microflow naming prefix convention (ADR-005) |
| 6 | **CONV013** | 25 | ⚠ | Custom rule violation |
| 7 | **MPR008** | 9 | ⚠ | Quality issue |
| 8 | **PNL011** | 8 | ⚠ | Boolean attribute naming |
| 9 | **PNL012** | 6 | ⚠ | Page naming suffix convention |
| 10 | **PNL014** | 5 | ⚠ | Resource naming prefixes |

---

## Lint Rules Reference

### Documentation Rules

#### **PNL002** - Documentation Critical (ADR-014)
- **Severity:** ✗ CRITICAL
- **Count:** 56 occurrences
- **Description:** Entities, microflows, and key elements must have documentation
- **References:** Architecture Decision Record ADR-014
- **Fix:** Add descriptions explaining purpose, role, and domain context
- **Example:** `/** Manages customer orders and fulfillment workflow */`

#### **PNL003** - Documentation Standard (ADR-014)
- **Severity:** ⚠ WARNING
- **Count:** 58 occurrences
- **Description:** Supporting microflows should have documentation
- **References:** Architecture Decision Record ADR-014
- **Fix:** Add inline documentation for microflow purpose

### Naming & Convention Rules

#### **PNL001** - Microflow Naming Prefix (ADR-005)
- **Severity:** ✗ CRITICAL
- **Count:** 29 occurrences
- **Description:** Microflows must follow naming convention with ADR-005 prefixes
- **Valid Prefixes:**
  - `DS_` - Data Service
  - `BCO_` - Business Core Operation
  - `BCR_` - Business Core Retrieve
  - `ACR_` - Application Core Retrieve
  - `OCH_` - Operation Change Handler
  - `ACT_` - Activity/Action
  - `PRI_` - Process, Report, Integration
  - `PUB_` - Public/Published
  - `BGT_` - Background Task
  - `MAP_` - Mapper/Transformer
- **Fix:** Rename microflows to include appropriate prefix

#### **PNL011** - Boolean Attribute Naming
- **Severity:** ⚠ WARNING
- **Count:** 8 occurrences
- **Description:** Boolean attributes must start with Is/Has/Can/Should/Was/Will
- **Fix:** Rename to follow convention (e.g., `IsActive`, `HasError`, `CanDelete`)

#### **PNL012** - Page Naming Suffix
- **Severity:** ⚠ WARNING
- **Count:** 6 occurrences
- **Description:** Pages should follow Entity_Type naming pattern
- **Valid Suffixes:** `_New`, `_Edit`, `_View`, `_Overview`, `_Detail`
- **Fix:** Rename pages to match pattern (e.g., `Customer_Overview`, `Order_Edit`)

#### **PNL014** - Resource Naming Prefixes
- **Severity:** ⚠ WARNING
- **Count:** 5 occurrences
- **Description:** Resources should use consistent naming prefixes

### Refactoring & Architecture Rules

#### **PNL016** - Orphaned Elements (Unreferenced)
- **Severity:** ℹ INFO
- **Count:** 60 occurrences
- **Description:** Microflows, pages, or elements not referenced anywhere
- **Recommendations:**
  1. Remove if truly unused
  2. If valid entry point (REST, scheduled), add to `EXCLUDED_ELEMENTS`
  3. Add dated justification if kept for backward compatibility
- **Action:** Review and clean up unused assets

#### **PNL004** - Module Encapsulation
- **Severity:** Architecture
- **Description:** Data should not cross module boundaries in pages
- **Fix:** Use view entities or service microflows for cross-module data access

#### **PNL005** - Gravity Rule
- **Severity:** Architecture
- **Description:** Data should flow towards the center; UI logic stays at edges
- **Fix:** Move business logic to service layers, keep UI logic isolated

### Security Rules

#### **SEC001** - Persistent Entity Without Access Rules
- **Severity:** ⚠ WARNING
- **Count:** 31 occurrences
- **Description:** All persistent entities need access control rules
- **Fix:** `GRANT <Role> ON Entity (READ *, WRITE *, DELETE)`

#### **PNL018** - Strict Mode on Production
- **Severity:** ✗ CRITICAL
- **Count:** 1 occurrence
- **Status:** Project-level security setting
- **Issue:** Strict mode is OFF at Production security level
- **CVE:** CVE-2023-23835
- **Fix:** Enable strict mode in Project Settings → Security

#### **SEC003** - Demo Users at Production Level
- **Severity:** ⚠ WARNING
- **Count:** 1 occurrence
- **Status:** Project-level setting
- **Fix:** Disable demo users in Project Settings → Security

---

## Priority Action Items

### 🔴 CRITICAL (Must fix before release)
1. **[PNL002]** Add documentation to 56 critical elements (entities, key microflows)
2. **[PNL001]** Rename 29 microflows to follow ADR-005 naming convention
3. **[PNL018]** Enable strict mode in Project Security settings (CVE-2023-23835)

### 🟠 HIGH (Should fix for quality)
1. **[SEC001]** Add access rules to 31 persistent entities
2. **[PNL003]** Add documentation to 58 supporting microflows
3. **[CONV013]** Fix 25 custom rule violations

### 🟡 MEDIUM (Nice to have)
1. **[PNL016]** Review and remove/document 60 unreferenced elements
2. **[MPR008]** Fix 9 quality issues
3. **[PNL011]** Rename 8 boolean attributes to follow convention
4. **[PNL012]** Rename 6 pages to follow naming pattern

---

## Lint Rules Configuration

### Active Rules (18 total)

**PostNL Custom Rules (PNL001–PNL018):**
- Located in: `.claude/lint-rules/`
- Enforces PostNL standards and architectural decisions
- Based on Architecture Decision Records (ADRs)

**Built-in Rules:**
- MPR (Mendix Platform Rules) - Quality & correctness
- SEC (Security Rules) - Access control & compliance
- CONV (Convention Rules) - Naming & patterns

---

## Next Steps

1. **Fix Critical Issues First**
   ```bash
   ./mxcli lint -p "PostNL TS - Transport Services Order Platform.mpr" --color
   ```

2. **Fix by Category**
   - Start with documentation ([PNL002] - 56 issues)
   - Then naming conventions ([PNL001] - 29 issues)
   - Then access rules ([SEC001] - 31 issues)

3. **Re-run Lint**
   ```bash
   ./mxcli lint -p "PostNL TS - Transport Services Order Platform.mpr"
   ```

4. **Track Progress**
   - Use version control to track fixes per commit
   - Re-run lint after each batch of fixes

---

## Resources

- **AGENTS.md** - Full mxcli reference and commands
- **CLAUDE_AI_TOOLING.md** - AI assistant workflow documentation
- **.ai-context/adrs/** - Architecture Decision Records (ADR-001 through ADR-017)
- **.ai-context/skills/** - Mendix development guides and patterns

---

## References

- **ADR-005:** Naming Conventions & Standard Terminology
- **ADR-014:** Technical Documentation Standards
- **CVE-2023-23835:** Mendix Strict Mode Security Advisory
