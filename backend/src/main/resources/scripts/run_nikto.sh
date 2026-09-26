#!/bin/bash
TARGET=$1
# Thêm -maxtime 14m để Nikto tự kết thúc sau 14 phút và trả về kết quả
# Tránh việc Java chờ quá 15 phút (900s) rồi kill ép buộc làm mất data
nikto -h "$TARGET" -maxtime 14m -Format csv -output - 2>/dev/null