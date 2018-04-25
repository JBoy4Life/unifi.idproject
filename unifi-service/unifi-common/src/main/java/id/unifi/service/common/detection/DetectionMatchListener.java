package id.unifi.service.common.detection;

import java.util.List;
import java.util.function.Consumer;

// FIXME: Break away unifi-attendance and move this class to unifi-core
public interface DetectionMatchListener extends Consumer<List<DetectionMatch>> {}
