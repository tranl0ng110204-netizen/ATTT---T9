#!/bin/bash
TARGET=$1
gobuster dir -u "$TARGET" -w /usr/share/wordlists/dirb/common.txt -s "200,301,302" --no-progress -q