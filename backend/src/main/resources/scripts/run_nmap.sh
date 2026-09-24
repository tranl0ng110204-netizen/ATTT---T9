#!/bin/bash
TARGET=$1
nmap -T4 -F -n -Pn -oX - "$TARGET"