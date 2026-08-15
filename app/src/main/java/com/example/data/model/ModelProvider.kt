package com.example.data.model

enum class ProviderType {
  OPENCODE_ZEN,
  GEMINI_FREE,
  OPENROUTER_FREE,
  CUSTOM_OPENAI
}

data class ModelProvider(
  val id: String,
  val name: String,
  val type: ProviderType,
  val description: String,
  val defaultBaseUrl: String,
  val isFree: Boolean = true,
  val requiresApiKey: Boolean = false,
  val docsUrl: String = ""
)

data class AiModel(
  val id: String,
  val name: String,
  val providerId: String,
  val providerType: ProviderType,
  val contextWindow: String,
  val isFree: Boolean = true,
  val description: String,
  val recommendedFor: String,
  val badge: String = "FREE"
)

object ProviderDefaults {
  val PROVIDERS = listOf(
    ModelProvider(
      id = "opencode_zen",
      name = "OpenCode Zen (Free)",
      type = ProviderType.OPENCODE_ZEN,
      description = "Free high-performance coding models curated by OpenCode Zen with zero setup required.",
      defaultBaseUrl = "https://api.opencode.ai/v1",
      isFree = true,
      requiresApiKey = false,
      docsUrl = "https://opencode.ai/zen"
    ),
    ModelProvider(
      id = "gemini_free",
      name = "Google Gemini (Free Tier)",
      type = ProviderType.GEMINI_FREE,
      description = "Google DeepMind's Gemini 2.5 Flash & Pro models with high context window and reasoning.",
      defaultBaseUrl = "https://generativelanguage.googleapis.com/v1beta",
      isFree = true,
      requiresApiKey = false,
      docsUrl = "https://ai.google.dev"
    ),
    ModelProvider(
      id = "openrouter_free",
      name = "OpenRouter (Free Models)",
      type = ProviderType.OPENROUTER_FREE,
      description = "Access top open-source coding models hosted on OpenRouter with free credits.",
      defaultBaseUrl = "https://openrouter.ai/api/v1",
      isFree = true,
      requiresApiKey = false,
      docsUrl = "https://openrouter.ai"
    ),
    ModelProvider(
      id = "custom_openai",
      name = "Custom / Local Ollama",
      type = ProviderType.CUSTOM_OPENAI,
      description = "Connect to any OpenAI-compatible server (Ollama, LM Studio, vLLM, DeepSeek, or Groq).",
      defaultBaseUrl = "http://localhost:11434/v1",
      isFree = true,
      requiresApiKey = false,
      docsUrl = ""
    )
  )

  val MODELS = listOf(
    // OpenCode Zen Free Models
    AiModel(
      id = "zen/free-deepseek-r1",
      name = "DeepSeek R1 (OpenCode Zen)",
      providerId = "opencode_zen",
      providerType = ProviderType.OPENCODE_ZEN,
      contextWindow = "128k",
      isFree = true,
      description = "Premier reasoning model with Chain-of-Thought planning for complex code generation.",
      recommendedFor = "Full project generation, debugging, refactoring",
      badge = "ZEN FREE"
    ),
    AiModel(
      id = "zen/free-deepseek-v3",
      name = "DeepSeek V3 (OpenCode Zen)",
      providerId = "opencode_zen",
      providerType = ProviderType.OPENCODE_ZEN,
      contextWindow = "128k",
      isFree = true,
      description = "Lightning-fast 671B MoE model optimized for code editing, terminal commands, and scripting.",
      recommendedFor = "Fast CLI edits, shell commands, unit tests",
      badge = "ZEN FREE"
    ),
    AiModel(
      id = "zen/free-qwen-2.5-coder-32b",
      name = "Qwen 2.5 Coder 32B (OpenCode Zen)",
      providerId = "opencode_zen",
      providerType = ProviderType.OPENCODE_ZEN,
      contextWindow = "128k",
      isFree = true,
      description = "Specialized coding powerhouse with high accuracy across Kotlin, Python, TypeScript, and Rust.",
      recommendedFor = "Multi-file architecture, code reviews",
      badge = "ZEN FREE"
    ),
    AiModel(
      id = "zen/free-llama-3.3-70b",
      name = "Llama 3.3 70B (OpenCode Zen)",
      providerId = "opencode_zen",
      providerType = ProviderType.OPENCODE_ZEN,
      contextWindow = "128k",
      isFree = true,
      description = "Meta's flagship open model for instruction following and clean software documentation.",
      recommendedFor = "Documentation, explanations, scripting",
      badge = "ZEN FREE"
    ),
    
    // Gemini Models
    AiModel(
      id = "gemini-2.5-flash",
      name = "Gemini 2.5 Flash",
      providerId = "gemini_free",
      providerType = ProviderType.GEMINI_FREE,
      contextWindow = "1M",
      isFree = true,
      description = "Ultra-fast multimodal model with 1 million token context and rapid tool calling.",
      recommendedFor = "Fast response, massive codebases, tool calls",
      badge = "FREE TIER"
    ),
    AiModel(
      id = "gemini-2.5-pro",
      name = "Gemini 2.5 Pro",
      providerId = "gemini_free",
      providerType = ProviderType.GEMINI_FREE,
      contextWindow = "2M",
      isFree = true,
      description = "Advanced reasoning model with deep problem solving and 2M token context.",
      recommendedFor = "Complex algorithms, full-stack migrations",
      badge = "FREE TIER"
    ),

    // OpenRouter Free Models
    AiModel(
      id = "deepseek/deepseek-r1:free",
      name = "DeepSeek R1 (OpenRouter Free)",
      providerId = "openrouter_free",
      providerType = ProviderType.OPENROUTER_FREE,
      contextWindow = "64k",
      isFree = true,
      description = "OpenRouter hosted DeepSeek R1 reasoning endpoint.",
      recommendedFor = "Reasoning, step-by-step logic",
      badge = "FREE"
    ),
    AiModel(
      id = "meta-llama/llama-3.3-70b-instruct:free",
      name = "Llama 3.3 70B (OpenRouter Free)",
      providerId = "openrouter_free",
      providerType = ProviderType.OPENROUTER_FREE,
      contextWindow = "128k",
      isFree = true,
      description = "OpenRouter hosted Llama 3.3 70B instruct model.",
      recommendedFor = "General coding and CLI workflows",
      badge = "FREE"
    ),
    AiModel(
      id = "qwen/qwen-2.5-coder-32b-instruct:free",
      name = "Qwen 2.5 Coder (OpenRouter Free)",
      providerId = "openrouter_free",
      providerType = ProviderType.OPENROUTER_FREE,
      contextWindow = "32k",
      isFree = true,
      description = "OpenRouter free tier for Qwen 2.5 Coder.",
      recommendedFor = "Code snippets, fast edits",
      badge = "FREE"
    ),

    // Custom
    AiModel(
      id = "custom-openai-model",
      name = "Custom Local / Endpoint Model",
      providerId = "custom_openai",
      providerType = ProviderType.CUSTOM_OPENAI,
      contextWindow = "Custom",
      isFree = true,
      description = "User-configured model name connecting to local Ollama or private OpenAI server.",
      recommendedFor = "Local offline LLMs, custom endpoints",
      badge = "CUSTOM"
    )
  )

  val DEFAULT_MODEL = MODELS[0] // DeepSeek R1 (OpenCode Zen)
}
