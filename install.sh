#!/bin/bash
# AI Native Client — Cài đặt MCP config + Steering files (global)
# Chạy: bash install.sh

set -e

echo "🚀 Cài đặt AI Context Engine cho Kiro + Cursor..."
echo ""

# --- Kiro MCP config ---
KIRO_MCP_DIR="$HOME/.kiro/settings"
KIRO_MCP_FILE="$KIRO_MCP_DIR/mcp.json"

mkdir -p "$KIRO_MCP_DIR"

if [ -f "$KIRO_MCP_FILE" ]; then
  echo "⚠️  Kiro MCP config đã tồn tại: $KIRO_MCP_FILE"
  echo "   → Bạn cần tự thêm block 'ai-context-engine-company' vào file này."
  echo "   → Xem mẫu: mcp.kiro.example.json"
else
  cp mcp.kiro.example.json "$KIRO_MCP_FILE"
  echo "✅ Kiro MCP config đã tạo: $KIRO_MCP_FILE"
  echo "   → Mở file và điền tokens của bạn."
fi

# --- Cursor MCP config ---
CURSOR_MCP_DIR="$HOME/.cursor"
CURSOR_MCP_FILE="$CURSOR_MCP_DIR/mcp.json"

mkdir -p "$CURSOR_MCP_DIR"

if [ -f "$CURSOR_MCP_FILE" ]; then
  echo "⚠️  Cursor MCP config đã tồn tại: $CURSOR_MCP_FILE"
  echo "   → Bạn cần tự thêm block 'ai-context-engine-company' vào file này."
  echo "   → Xem mẫu: mcp.cursor.example.json"
else
  cp mcp.cursor.example.json "$CURSOR_MCP_FILE"
  echo "✅ Cursor MCP config đã tạo: $CURSOR_MCP_FILE"
  echo "   → Mở file và điền tokens của bạn."
fi

# --- Kiro Steering files ---
STEERING_DIR="$HOME/.kiro/steering"
mkdir -p "$STEERING_DIR"

cp .kiro/steering/*.md "$STEERING_DIR/"
echo "✅ Kiro steering files đã copy vào: $STEERING_DIR/"

# --- Cursor Rules files ---
CURSOR_RULES_DIR="$HOME/.cursor/rules"
mkdir -p "$CURSOR_RULES_DIR"

cp .cursor/rules/*.mdc "$CURSOR_RULES_DIR/"
echo "✅ Cursor rules đã copy vào: $CURSOR_RULES_DIR/"

echo ""
echo "🛰️  Antigravity: steering nằm trong repo tại .agents/rules/ (workspace rules)."
echo "   → Mở project chứa repo này hoặc copy .agents/rules vào git root project khác."
echo "   → Trong Antigravity: Customizations → Rules — bật Always On cho từng file (các file *-part*.md là một bộ)."

echo ""
echo "📋 Việc cần làm tiếp:"
echo "   1. Mở ~/.kiro/settings/mcp.json (hoặc ~/.cursor/mcp.json)"
echo "   2. Điền tokens: X-API-Key, X-Member-Id, X-GitLab-Token, X-Confluence-Token, X-Jira-Token"
echo "   3. Restart IDE"
echo "   4. Thử: 'Tìm code liên quan đến payment flow trong chain VAC'"
echo ""
echo "🔄 Khi team cập nhật steering files mới:"
echo "   cd ai-native-client && git pull && bash install.sh"
echo ""
echo "Done! 🎉"
