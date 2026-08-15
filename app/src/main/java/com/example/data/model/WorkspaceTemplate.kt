package com.example.data.model

data class WorkspaceTemplate(
  val id: String,
  val name: String,
  val icon: String,
  val description: String,
  val defaultFiles: Map<String, String>
)

object WorkspaceTemplateCatalog {
  val TEMPLATES = listOf(
    WorkspaceTemplate(
      id = "PYTHON_APP",
      name = "Python CLI & Scripts",
      icon = "🐍",
      description = "Full Python workspace with entry point, modular helpers, tests, and CLI runner.",
      defaultFiles = mapOf(
        "main.py" to """
# main.py - OpenCode Agent Python App
import sys
from utils import greet_user, compute_stats

def main():
    print("========================================")
    print("  Welcome to OpenCode CLI Python Workspace")
    print("========================================")
    user_name = "Developer"
    print(greet_user(user_name))
    
    data = [12, 45, 78, 23, 89, 56, 91, 34]
    stats = compute_stats(data)
    print(f"Sample data processing results: {stats}")

if __name__ == "__main__":
    main()
""".trimIndent(),
        "utils.py" to """
# utils.py - Helper utilities
def greet_user(name: str) -> str:
    return f"[OpenCode] Hello, {name}! AI Agent is ready in this workspace."

def compute_stats(numbers: list) -> dict:
    if not numbers:
        return {"count": 0, "sum": 0, "avg": 0}
    return {
        "count": len(numbers),
        "min": min(numbers),
        "max": max(numbers),
        "sum": sum(numbers),
        "avg": round(sum(numbers) / len(numbers), 2)
    }
""".trimIndent(),
        "requirements.txt" to """
# Project dependencies
pytest>=7.0.0
requests>=2.28.0
rich>=13.0.0
""".trimIndent(),
        "README.md" to """
# Python CLI Workspace
Created with **OpenCode CLI for Android**.
You can ask the AI agent to build APIs, algorithms, scrape data, or add unit tests!
""".trimIndent()
      )
    ),
    WorkspaceTemplate(
      id = "WEB_GAME",
      name = "HTML5 / JS Canvas Web App",
      icon = "🕹️",
      description = "Clean front-end project with HTML5, CSS animations, and JavaScript canvas loop.",
      defaultFiles = mapOf(
        "index.html" to """
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>OpenCode Web App</title>
    <link rel="stylesheet" href="style.css">
</head>
<body>
    <div class="container">
        <h1>🚀 OpenCode Web Project</h1>
        <p class="subtitle">Live Interactive Workspace</p>
        <div id="app-card">
            <canvas id="gameCanvas" width="400" height="300"></canvas>
            <div class="controls">
                <button id="actionBtn">Trigger Particle Burst</button>
                <button id="resetBtn">Reset</button>
            </div>
        </div>
    </div>
    <script src="app.js"></script>
</body>
</html>
""".trimIndent(),
        "style.css" to """
body {
    margin: 0;
    padding: 20px;
    background: #0B0F17;
    color: #F1F5F9;
    font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
    display: flex;
    justify-content: center;
}
.container {
    max-width: 600px;
    text-align: center;
}
h1 {
    color: #10B981;
    margin-bottom: 4px;
}
.subtitle {
    color: #94A3B8;
    margin-top: 0;
}
#gameCanvas {
    background: #111827;
    border: 1px solid #1E293B;
    border-radius: 8px;
}
.controls {
    margin-top: 15px;
    display: flex;
    gap: 10px;
    justify-content: center;
}
button {
    background: #06B6D4;
    color: #080C14;
    border: none;
    padding: 8px 16px;
    border-radius: 6px;
    font-weight: 600;
    cursor: pointer;
}
button:hover {
    background: #00F0FF;
}
""".trimIndent(),
        "app.js" to """
// app.js - Canvas & Animation script
const canvas = document.getElementById('gameCanvas');
const ctx = canvas.getContext('2d');

let particles = [];

function createBurst() {
    for (let i = 0; i < 30; i++) {
        particles.push({
            x: canvas.width / 2,
            y: canvas.height / 2,
            vx: (Math.random() - 0.5) * 8,
            vy: (Math.random() - 0.5) * 8,
            radius: Math.random() * 4 + 2,
            color: ['#10B981', '#06B6D4', '#F59E0B', '#A855F7'][Math.floor(Math.random() * 4)],
            life: 1.0
        });
    }
}

function loop() {
    ctx.fillStyle = 'rgba(17, 24, 39, 0.2)';
    ctx.fillRect(0, 0, canvas.width, canvas.height);

    for (let i = particles.length - 1; i >= 0; i--) {
        const p = particles[i];
        p.x += p.vx;
        p.y += p.vy;
        p.life -= 0.02;

        ctx.beginPath();
        ctx.arc(p.x, p.y, p.radius, 0, Math.PI * 2);
        ctx.fillStyle = p.color;
        ctx.globalAlpha = Math.max(0, p.life);
        ctx.fill();
        ctx.globalAlpha = 1.0;

        if (p.life <= 0) {
            particles.splice(i, 1);
        }
    }
    requestAnimationFrame(loop);
}

document.getElementById('actionBtn').addEventListener('click', createBurst);
document.getElementById('resetBtn').addEventListener('click', () => { particles = []; });

createBurst();
loop();
""".trimIndent(),
        "README.md" to "# HTML5 Canvas Project\nInteractive web app ready for agent enhancements."
      )
    ),
    WorkspaceTemplate(
      id = "KOTLIN_CLI",
      name = "Kotlin CLI / Microservice",
      icon = "⚡",
      description = "Kotlin / Java structured project with data classes, algorithms, and models.",
      defaultFiles = mapOf(
        "Main.kt" to """
// Main.kt - OpenCode CLI Kotlin Engine
package app

data class Task(val id: Int, val title: String, val completed: Boolean)

fun main() {
    println("🚀 [OpenCode Kotlin CLI]")
    val tasks = listOf(
        Task(1, "Initialize Workspace", true),
        Task(2, "Connect OpenCode Zen AI Provider", true),
        Task(3, "Execute Autonomous Agent Steps", false)
    )
    println("Pending Tasks:")
    tasks.filter { !it.completed }.forEach { println(" - ${'$'}{it.id}: ${'$'}{it.title}") }
}
""".trimIndent(),
        "README.md" to "# Kotlin CLI Project\nKotlin project managed by OpenCode CLI."
      )
    ),
    WorkspaceTemplate(
      id = "NODE_API",
      name = "Node.js / Express Service",
      icon = "🟢",
      description = "Modern Node.js REST API with routing, middleware, and controllers.",
      defaultFiles = mapOf(
        "package.json" to """
{
  "name": "opencode-node-service",
  "version": "1.0.0",
  "main": "server.js",
  "scripts": {
    "start": "node server.js"
  },
  "dependencies": {
    "express": "^4.18.2"
  }
}
""".trimIndent(),
        "server.js" to """
// server.js - Simple Express Mock/Server
const http = require('http');

const server = http.createServer((req, res) => {
    res.writeHead(200, { 'Content-Type': 'application/json' });
    res.end(JSON.stringify({
        status: "online",
        system: "OpenCode Agent Workspace",
        timestamp: new Date().toISOString()
    }));
});

const PORT = 3000;
console.log(`Server listening on port ${'$'}{PORT}...`);
""".trimIndent(),
        "README.md" to "# Node.js API Project"
      )
    ),
    WorkspaceTemplate(
      id = "EMPTY",
      name = "Empty Project Folder",
      icon = "📁",
      description = "Blank workspace folder. The AI agent will scaffold files from scratch.",
      defaultFiles = mapOf(
        "README.md" to "# Workspace\nInitialized clean root directory."
      )
    )
  )
}
