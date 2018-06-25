@echo off
:run
java -Dunifi.service.api.client.id=deloitte -Dunifi.service.api.username=test -Dunifi.service.api.password=test -Dunifi.service.api.uri=ws://10.0.99.4:8000/service/msgpack -Dunifi.ftc.api.server=10.0.99.3 -Dunifi.ftc.api.domain=localhost -Dunifi.ftc.api.username=Administrator -Dunifi.ftc.api.password=TestPass123 -Dunifi.ftc.api.facility.code=12345 -Dunifi.mq.endpoint=10.0.99.4:5672 -jar unifi-gallagher-detection-logger-0.1-SNAPSHOT-jar-with-dependencies.jar
goto :run
