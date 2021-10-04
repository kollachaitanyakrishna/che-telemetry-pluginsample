package org.my.group;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.eclipse.che.api.core.rest.HttpJsonRequestFactory;
import org.eclipse.che.incubator.workspace.telemetry.base.AbstractAnalyticsManager;
import org.eclipse.che.incubator.workspace.telemetry.base.AnalyticsEvent;

import io.vertx.core.json.JsonObject;

public class AnalyticsManager extends AbstractAnalyticsManager {

    private String _apiEndpoint;
    private String _workspaceId;
    private String _machineToken;

    public AnalyticsManager(String apiEndpoint, String workspaceId, String machineToken,
            HttpJsonRequestFactory requestFactory) {
        super(apiEndpoint, workspaceId, machineToken, requestFactory);
        _apiEndpoint = apiEndpoint;
        _workspaceId = workspaceId;
        _machineToken = machineToken;
    }

    private long inactiveTimeLimt = 60000 * 3;

    @Override
    public boolean isEnabled() {
        System.out.println("Telemetry backend - isEnabled");
        return true;
    }

    @Override
    public void destroy() {
        System.out.println("Telemetry backend - destroy");
        HashMap<String, Object> commonProperties = new HashMap<String, Object>();
        commonProperties.put("onDestroy", "kolla");
        onEvent(AnalyticsEvent.WORKSPACE_STOPPED , "lastOwnerId", "localip", "kollamac", "lastResolution", commonProperties);
    }

    @Override
    public void onEvent(AnalyticsEvent event, String ownerId, String ip, String userAgent, String resolution, Map<String, Object> properties) {
        System.out.println("Telemetry backend - onEvent");
        HttpClient httpClient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost("http://<DeployedServer>/event");
        HashMap<String, Object> eventPayload = new HashMap<String, Object>(properties);
        eventPayload.put("event", event);
        StringEntity requestEntity = new StringEntity(new JsonObject(eventPayload).toString(),
                ContentType.APPLICATION_JSON);
        httpPost.setEntity(requestEntity);
        try {
            HttpResponse response = httpClient.execute(httpPost);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    
    @Override
    public void onActivity() {
        System.out.println("Telemetry backend - onActivity");
        HashMap<String, Object> commonProperties = new HashMap<String, Object>();
        commonProperties.put("onActivity", "kolla");
        onEvent(AnalyticsEvent.WORKSPACE_INACTIVE , "lastOwnerId", "localip", "kollamac", "lastResolution", commonProperties);
        
    }
}