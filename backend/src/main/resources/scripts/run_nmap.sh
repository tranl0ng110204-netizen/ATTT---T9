#!/bin/bash
TARGET=$1
# Cắt bỏ http:// hoặc https:// và các đường dẫn phía sau
HOST=$(echo "$TARGET" | sed -E 's|https?://||' | sed 's|/.*||')

nmap -T4 -F -n -Pn -oX - "$HOST"