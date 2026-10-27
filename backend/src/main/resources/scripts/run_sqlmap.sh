#!/bin/bash
TARGET=$1

if [ -z "$TARGET" ]; then
    echo "Loi: Khong co muc tieu (Target)!"
    exit 1
fi

echo "=== BAT DAU KIEM TRA SQLMAP CHO: $TARGET ==="
# --batch: TU DONG CHON YES cho moi prompt (BẮT BUỘC để không bị treo Java)
# --random-agent: gia lap user-agent trinh duyet
# --level 1 --risk 1: chay kiem thu an toan, khong pha hoai du lieu
# --flush-session: lam sach phien cu
sqlmap -u "$TARGET" --batch --random-agent --level=1 --risk=1 --flush-session
EXIT_CODE=$?

echo "=== SQLMAP HOAN TAT (Exit code: $EXIT_CODE) ==="
exit $EXIT_CODE