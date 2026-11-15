#!/bin/sh
set -eu
bucket="${S3_BUCKET:-valdifly-artifacts}"
code=$(curl -s -o /tmp/s3.out -w "%{http_code}" -X PUT "http://floci:4566/${bucket}")
if [ "$code" = "200" ] || [ "$code" = "409" ]; then
  echo "bucket ${bucket} ready (${code})"
  exit 0
fi
echo "bucket create failed: HTTP ${code}"
cat /tmp/s3.out
exit 1
