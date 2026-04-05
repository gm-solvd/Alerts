# Prefer Improving Existing Code Over Creating New Files

When adding functionality to existing code:

1. **Search first** — look for existing files in the same package/layer that handle similar concerns
2. **Extend before creating** — check if an existing function, class, or file can be extended with a small change
3. **Justify new files** — only create a new file when the functionality is genuinely distinct

Legitimate reasons for a new file:
- New domain model/entity representing a distinct concept
- New use case with a single responsibility (mobile pattern)
- New controller for a new REST resource
- New integration client for a new external service
- Test files for new production code

Not legitimate reasons:
- "Organization" when the existing file is under 200 lines
- Helper/util classes that could be private functions or extensions in the calling file
- Abstractions that have only one implementation
- Splitting a file just because you're adding a function to it
