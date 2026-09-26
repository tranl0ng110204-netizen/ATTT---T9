#!/bin/bash
TARGET=$1
# Tách host từ URL nếu có http://
HOST=$(echo "$TARGET" | sed -E 's|https?://||' | sed 's|/.*||')
sslscan --no-colour "$HOST" 2>/dev/null