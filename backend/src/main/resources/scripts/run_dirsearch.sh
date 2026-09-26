bash


#!/bin/bash
TARGET=$1
dirsearch -u "$TARGET" -e php,html,js,txt -q --format=plain 2>/dev/null