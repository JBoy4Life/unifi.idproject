#!/bin/sh

printf "%s\\n" "Ending any existing \`unifi-core\` and \`unifi-mock-agent\` processes..."
for process in $(ps -xo "pid,args" | grep -E '(\bunifi-core\b|\bunifi-mock-agent\b)' | awk '{print $1}'); do
	kill -9 "$process"
done

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

