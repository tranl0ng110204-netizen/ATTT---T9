#!/bin/bash
TARGET=$1

if [ -z "$TARGET" ]; then
    echo "Loi: Khong co muc tieu (Target)!"
    exit 1
fi

echo "=== BAT DAU SCAN NUCLEI CHO: $TARGET ==="
# -silent: chi in cac phat hien
# -nh / -no-color: khong in ma mau ANSI de Java doc text sach
# -rate-limit 50: gioi han toc do gui request tranh qua tai server
nuclei -u "$TARGET" -silent -nh -rate-limit 50 -tags cve,misconfiguration,exposure
EXIT_CODE=$?

echo "=== NUCLEI HOAN TAT (Exit code: $EXIT_CODE) ==="
exit $EXIT_CODE