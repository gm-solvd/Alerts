# AI Model Selection

Before sending a prompt, ask yourself:

---

## Decision Tree

```
Could a junior developer do this with clear instructions?
  → YES → Fast / cheap model  (or do it manually)

Does this need to understand complex context and make decisions?
  → YES → Standard coding model

Does this require deep analysis, multi-step reasoning, or architectural thinking?
  → YES → Reasoning / thinking model
```

---

## Examples by Tier

### Fast / cheap model (or manual)
- Rename a variable across a file
- Generate boilerplate (DTO, entity, simple Composable)
- Format or fix indentation
- Write a basic unit test for a pure function
- Translate a string resource

### Standard coding model
- Implement a feature given a clear spec
- Refactor a class to follow clean architecture
- Debug a specific failing test
- Write integration tests for a known endpoint
- Hook up a ViewModel to a screen

### Reasoning / thinking model
- Design the overall architecture or module structure
- Decide between two architectural approaches with trade-offs
- Debug a subtle concurrency or state bug with unclear root cause
- Plan a multi-step refactor that touches many layers
- Evaluate security implications of an auth design
