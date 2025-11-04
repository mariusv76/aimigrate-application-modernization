# ASR to Architecture Summary Conversion

The extension includes an **ASR Converter** that uses **GitHub Copilot's intelligent parsing** to transform Application Summary Report (ASR) markdown files into the Architecture Summary JSON format required by the extension.

## Overview

The ASR Converter (`asrConverter.ts`) leverages **GitHub Copilot's language model** to intelligently analyze ASR markdown documents and extract structured architecture information. Unlike rigid pattern-matching approaches, this method is resilient to varying ASR formats and structures.

### How It Works

1. **Content Reading**: The converter reads the entire ASR markdown file
2. **Copilot Analysis**: Sends the ASR content to GitHub Copilot with detailed extraction instructions
3. **Structured Extraction**: Copilot analyzes the document and returns a structured JSON object
4. **Validation**: The converter validates and ensures all required fields are present

### Benefits of Copilot-Based Parsing

- ✅ **Format Flexibility**: Works with varying ASR structures and section numbering
- ✅ **Intelligent Inference**: Can infer missing information from context
- ✅ **Semantic Understanding**: Understands relationships between technologies and migration patterns
- ✅ **Automatic Mapping**: Maps source technologies to appropriate Azure target services
- ✅ **Adaptive Extraction**: Handles different table formats and content organizations

## Usage

### Via Command Palette

1. Open Command Palette: `Ctrl+Shift+P` (Windows/Linux) or `Cmd+Shift+P` (Mac)
2. Type: `Architecture Transform: Convert ASR Markdown to Architecture Summary JSON`
3. Select your ASR markdown file (e.g., `responses-50000.md`)
4. Choose where to save the output JSON file
5. The JSON file will be created and opened automatically

### What Gets Converted

GitHub Copilot analyzes the ASR and extracts:

| Information | Mapped To | Description |
|-------------|-----------|-------------|
| Application name & purpose | `projectName`, `description` | Project identity and business drivers |
| Current technology stack | `sourceArchitecture.technologies` | Technologies, frameworks, databases in use |
| Current components | `sourceArchitecture.components` | Services, layers, modules |
| Architectural patterns | `sourceArchitecture.patterns` | Design patterns and architecture styles |
| Migration strategies | `transformationRules` | Transformation approach per component |
| Target Azure services | `targetArchitecture.technologies` | Recommended Azure services |
| Migration complexity | `migrationComplexity` | Assessment of effort required |
| Time estimates | `estimatedEffort` | Timeline projections |

### Capabilities & Limitations

**Capabilities:**

- ✅ **Adaptive Parsing**: Handles varying section structures and numbering
- ✅ **Context Understanding**: Infers relationships between components
- ✅ **Technology Mapping**: Automatically maps to Azure services
- ✅ **Intelligent Defaults**: Provides reasonable defaults for missing information

**Limitations:**

- ⚠️ **Requires GitHub Copilot**: Extension needs active GitHub Copilot subscription
- ⚠️ **API Availability**: Conversion requires connection to Copilot services
- ⚠️ **Review Recommended**: Generated JSON should still be reviewed for accuracy
- ⚠️ **Large Documents**: Very large ASRs may exceed context limits

## Manual Enhancement

After conversion, review and enhance the JSON file:

### 1. Add Transformation Examples

```json
{
  "transformationRules": [
    {
      "id": "rule-001",
      "examples": [
        {
          "before": "// Your old code pattern",
          "after": "// Your new code pattern",
          "description": "Explanation of the transformation"
        }
      ]
    }
  ]
}
```

### 2. Add Component Paths

```json
{
  "sourceArchitecture": {
    "components": [
      {
        "name": "Backend Services",
        "path": "src/services",  // ← Add actual path
        "dependencies": ["DatabaseService", "CacheService"]  // ← Add dependencies
      }
    ]
  }
}
```

### 3. Add Dependencies

```json
{
  "dependencies": [
    {
      "name": "express",
      "version": "4.17.x",
      "type": "direct",
      "replacementTarget": "express:4.18.x",
      "migrationNotes": "Update to latest version"
    }
  ]
}
```

## Sample Files

The extension includes sample files for reference:

- **Input**: `samples/responses-50000.md` - ASR markdown from assessment
- **Output**: `samples/crms-architecture-summary.json` - Converted and enhanced JSON

## Best Practices

### For ASR Authors

1. **Clear Structure**: Use logical section organization with headers
2. **Explicit Details**: Include specific technology names and versions
3. **Migration Intent**: Clearly state migration strategies and rationale
4. **Complete Information**: Provide business drivers, current state, and target state

### For Extension Users

1. **Verify Copilot**: Ensure GitHub Copilot is active before conversion
2. **Review Output**: Always review and validate the generated JSON
3. **Enhance Details**: Add specific examples for complex transformations
4. **Validate Paths**: Confirm component paths match your project structure
5. **Test Prompts**: Generate and test prompts to ensure quality

## Workflow

```mermaid
ASR Markdown (responses-50000.md)
         ↓
   [Convert ASR Command]
         ↓
   [GitHub Copilot Analysis]
         ↓
Architecture Summary JSON (auto-generated)
         ↓
   [Manual Enhancement]
         ↓
Complete Architecture Summary JSON
         ↓
   [Generate Prompts Command]
         ↓
Prompt Instruction Files
```

## Tips

- **Copilot-Powered**: The converter uses AI to understand ASR structure - no rigid format required
- **Starting Point**: The generated JSON is comprehensive but should be reviewed
- **Enhance Examples**: Add code examples for better transformation guidance
- **Sample Reference**: Use included sample files as quality reference
- **Phased Migrations**: Create multiple summaries for complex multi-phase migrations

## Advanced: Custom Conversion

The converter is designed to be flexible:

1. **Varying Formats**: Copilot adapts to different ASR structures automatically
2. **Manual Creation**: You can still manually create JSON following `example-arch-summary.json` schema
3. **Hybrid Approach**: Convert with Copilot, then refine manually for precision
4. **Extension**: Modify `src/asrConverter.ts` to customize the extraction prompt for your organization's ASR format

The key is ensuring the final JSON matches the expected schema for optimal prompt generation.
