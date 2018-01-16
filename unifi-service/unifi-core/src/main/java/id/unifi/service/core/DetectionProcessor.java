package id.unifi.service.core;

import id.unifi.service.common.api.MessageListener;
import id.unifi.service.common.detection.Detection;
import id.unifi.service.common.detection.RawDetectionReport;
import id.unifi.service.core.site.ResolvedDetection;
import org.eclipse.jetty.websocket.api.Session;

import java.util.List;

public interface DetectionProcessor {
    void process(Detection detection);
    void addListener(String clientId, String siteId, Session session, MessageListener<List<ResolvedDetection>> listener);
}
