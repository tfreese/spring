// Created: 03.04.24
package de.freese.kubernetes.api;

import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.util.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Thomas Freese
 */
public final class KubernetesApi {
    private static final Logger LOGGER = LoggerFactory.getLogger(KubernetesApi.class);

    static void main() {
        try {
            final ApiClient apiClient = Config.defaultClient();
            final CoreV1Api v1Api = new CoreV1Api(apiClient);

            v1Api.listPodForAllNamespaces().execute().getItems().forEach(v1Pod -> LOGGER.info("pod: {}", v1Pod.toJson()));
        }
        catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
    }

    private KubernetesApi() {
        super();
    }
}
