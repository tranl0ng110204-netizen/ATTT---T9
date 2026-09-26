#!/bin/bash
TARGET=$1
gobuster dir -u "$TARGET" -w /usr/share/dirb/wordlists/common.txt -t 5 --timeout 60s --retry --np -q