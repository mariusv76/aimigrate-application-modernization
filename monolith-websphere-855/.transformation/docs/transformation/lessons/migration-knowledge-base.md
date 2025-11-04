# Migration Knowledge Base

> **Purpose:** Curated collection of migration lessons, known issues, and proven solutions to inform future transformations and AI agent prompts.

---

## How to Use This Knowledge Base


### For Developers

- Search this document before starting migration work

- Add new lessons immediately after resolving migration issues

- Link to official documentation whenever possible


### For GitHub Copilot / AI Agents

When prompting Copilot for migration assistance, include relevant sections from this knowledge base:

```text
I'm migrating from .NET 8 to .NET 9. Before suggesting solutions, review these known migration patterns from our knowledge base:
[paste relevant section]

Now help me with: [your specific issue]
```n

---

## Knowledge Base Structure

Each entry follows this template:

```markdown

### [Package/Technology Name] - [Issue Summary]

**Migration Path:** [Source Version] → [Target Version]  
**Issue:** [Brief description]  
**Root Cause:** [Why this breaks]  
**Solution:** [Step-by-step fix]  
**Official Docs:** [Link]  
**Related Issues:** [Links to internal tickets/PRs if applicable]  
**Discovered:** [Date] by [Person/Team]
```n

---

## .NET 8 → .NET 9 Migration Lessons


### Semantic Kernel - Text Embedding Service Obsolescence

**Migration Path:** Semantic Kernel 1.x → 1.x (with updated API surface)  
**Issue:** `ITextEmbeddingGeneration` and related embedding services marked obsolete; existing code using text embedding fails to compile or produces warnings.  
**Root Cause:** Semantic Kernel refactored embedding APIs to align with modern AI service patterns and improve consistency with chat completion services.  
**Solution:**

1. Replace `ITextEmbeddingGeneration` with `ITextEmbeddingGenerationService`
2. Update service registration in DI container:

   ```csharp
   // OLD (obsolete)
   services.AddSingleton<ITextEmbeddingGeneration>(
       new AzureOpenAITextEmbeddingGeneration(modelId, endpoint, apiKey));
   
   // NEW (recommended)
   services.AddSingleton<ITextEmbeddingGenerationService>(
       new AzureOpenAITextEmbeddingGenerationService(modelId, endpoint, apiKey));
   ```

3. Update method calls from `GenerateEmbeddingsAsync` to align with new interface contract
4. Review constructor parameter changes (endpoint format, credential handling)

**Official Docs:** <https://learn.microsoft.com/en-us/semantic-kernel/support/migration/text-embedding-obsolete-migration-guide?pivots=programming-language-csharp>  
**Related Issues:** [Add internal tracking link if applicable]  
**Discovered:** 2025-09-30 by [Team]

**Copilot Prompt Enhancement:**

```text
I'm upgrading Semantic Kernel. The ITextEmbeddingGeneration interface is obsolete. 
Refer to https://learn.microsoft.com/en-us/semantic-kernel/support/migration/text-embedding-obsolete-migration-guide 
and refactor my code to use ITextEmbeddingGenerationService following Microsoft's recommended patterns.
```n

---

## Azure SDK Migration Lessons


### Azure SDK - [Add specific issue as encountered]

**Migration Path:** Azure.* SDK vX → vY  
**Issue:** [TBD]  
**Solution:** [TBD]  
**Official Docs:** [TBD]  
**Discovered:** [Date]

---

## Entity Framework Core Migration Lessons


### [Placeholder - Add as encountered]

**Migration Path:** EF Core 8 → EF Core 9  
**Issue:** [TBD]  
**Solution:** [TBD]  
**Official Docs:** [TBD]  
**Discovered:** [Date]

---

## ASP.NET Core Migration Lessons


### [Placeholder - Add as encountered]

**Migration Path:** ASP.NET Core 8 → ASP.NET Core 9  
**Issue:** [TBD]  
**Solution:** [TBD]  
**Official Docs:** [TBD]  
**Discovered:** [Date]

---

## System.Text.Json Migration Lessons


### [Placeholder - Add as encountered]

**Migration Path:** .NET 8 → .NET 9  
**Issue:** [TBD - e.g., serialization behavior changes]  

**Solution:** [TBD]  
**Official Docs:** [TBD]  
**Discovered:** [Date]

---

## OpenTelemetry / Observability Migration Lessons


### [Placeholder - Add as encountered]

**Migration Path:** [Version details]  
**Issue:** [TBD - e.g., exporter API changes]  

**Solution:** [TBD]  
**Official Docs:** [TBD]  
**Discovered:** [Date]

---

## Authentication / Identity Migration Lessons


### [Placeholder - Add as encountered]

**Migration Path:** [Version details]  
**Issue:** [TBD - e.g., MSAL.NET breaking changes]  

**Solution:** [TBD]  
**Official Docs:** [TBD]  
**Discovered:** [Date]

---

## Dependency Injection / Hosting Migration Lessons


### [Placeholder - Add as encountered]

**Migration Path:** .NET 8 → .NET 9  
**Issue:** [TBD - e.g., service lifetime changes]  

**Solution:** [TBD]  
**Official Docs:** [TBD]  
**Discovered:** [Date]

---

## gRPC Migration Lessons


### [Placeholder - Add as encountered]

**Migration Path:** [Version details]  
**Issue:** [TBD]  
**Solution:** [TBD]  
**Official Docs:** [TBD]  
**Discovered:** [Date]

---

## Performance & Optimization Patterns


### [Placeholder - Add as encountered]

**Context:** [Performance regression or optimization opportunity]  
**Issue:** [TBD]  
**Solution:** [TBD]  
**Benchmarks:** [Include BenchmarkDotNet results if available]  
**Discovered:** [Date]

---

## Anti-Patterns & Gotchas


### [Placeholder - Add as encountered]

**Anti-Pattern:** [Description of what NOT to do]  
**Why It Fails:** [Explanation]  
**Correct Approach:** [Recommended solution]  
**Discovered:** [Date]

---

## Contributing to This Knowledge Base


### When to Add an Entry

- You encounter a migration issue that required non-obvious research

- GitHub Copilot or AI tools suggested incorrect solutions

- Official documentation exists but is hard to discover

- A breaking change impacts multiple projects/teams


### Entry Quality Standards

- ✅ Link to official Microsoft documentation (primary source)

- ✅ Include working code samples (before/after)

- ✅ Specify exact package versions where relevant

- ✅ Test the solution before documenting

- ✅ Use consistent formatting (follow template above)

- ❌ Don't include workarounds that violate best practices

- ❌ Don't document issues that are already in official breaking changes lists without adding context


### How to Add

1. Copy the template above
2. Fill in all required fields
3. Add code samples with syntax highlighting
4. Test your solution
5. Commit with descriptive message: `docs: add migration lesson for [topic]`

---

## Integration with Transformation Agent

This knowledge base should be:

1. **Indexed by the Transformation Agent** - loaded into context before migration operations

2. **Referenced in prompts** - included in system prompts for AI-assisted migration

3. **Updated continuously** - treat as living documentation, not one-time artifact

4. **Versioned** - track when lessons apply (by .NET version, package version)


### Example Agent Prompt Integration

```text
SYSTEM: You are a .NET migration assistant. Before suggesting solutions, consult the migration knowledge base at docs/transformation/migration-knowledge-base.md. Prioritize known patterns and official documentation links from the knowledge base over generic solutions.

USER: Help me upgrade this Semantic Kernel code from .NET 8 to .NET 9.

ASSISTANT: [Searches knowledge base for "Semantic Kernel"] I found a documented pattern for the ITextEmbeddingGeneration obsolescence issue. Let me apply the recommended solution from our knowledge base...
```n

---

## Related Documents

- **Prerequisites:** `transform-prereqs.md` - What to check BEFORE migration

- **Test Generation:** `transform-test-generation-prompts.md` - How to establish test baselines

- **Validation:** `transform-prereqs-validation-prompt-copilot.md` - Automated validation prompts

---

## Change Log

| Date | Change | Author |
|------|--------|--------|
| 2025-09-30 | Initial knowledge base created with Semantic Kernel embedding lesson | Transformation Agent Docs |

---

End of document.
