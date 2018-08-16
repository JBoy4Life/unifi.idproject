#!/bin/sh

for process in $(ps aux | grep "[j]ava" | awk '{print $2}'); do
	kill -9 "$process"
done
printf "%s\\n" "WARNING: This has ended any existing Java processes. Sorry."

mkdir -p "${HOME:?}/logs"

# Start core services.
cd "${HOME:?}/unifi.id/unifi-service" || exit
mvn exec:java -pl unifi-core > "${HOME:?}/logs/unifi-core.log" &
sleep 10
mvn exec:java -pl unifi-mock-agent -Dunifi.client.id=test-club > "${HOME:?}/logs/unifi-mock-agent.log" &
sleep 10

# Start web service.
cd "${HOME:?}/unifi.id/unifi-web" || exit
SOCKET_URI="localhost:8000" yarn start

