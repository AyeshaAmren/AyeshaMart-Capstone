#!/usr/bin/env bash
# AyeshaMart container entry point.
#  1. Reads Railway's PORT (or defaults to 8080) and rebinds Tomcat's HTTP
#     connector in conf/server.xml so the proxy can reach the app.
#  2. Starts Tomcat in the foreground (PID 1 -> clean shutdown signals).
set -e

PORT="${PORT:-8080}"
CONF=/usr/local/tomcat/conf/server.xml

sed -i.bak "s/port=\"8080\"/port=\"${PORT}\"/" "$CONF"

echo "AyeshaMart starting on HTTP port ${PORT} (DB: ${AYESHAMART_DB_URL:-default})"

exec /usr/local/tomcat/bin/catalina.sh run