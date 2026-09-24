#!/bin/bash
TARGET=$1

# Thêm --timeout 60s và --retry để chống chịu mạng lag
# Thêm -t 5 (5 luồng) để giảm tải, tránh bị WAF chặn
gobuster dir -u "$TARGET" -w /usr/share/dirb/wordlists/common.txt -t 5 --timeout 60s --retry --np -q