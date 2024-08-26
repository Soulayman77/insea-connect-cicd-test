package ma.insea.connect.keycloak;

import lombok.extern.slf4j.Slf4j;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
@Slf4j
public  class KeycloakConfig {

    private static Keycloak keycloak = null;

    private static String serverUrl;

    @Value("${keycloak.server.url}")
    private String serverUrlProperty;

    public final static String realm = "INSEA-CONNECT";
    public final static String clientId = "INSEA-CONNECT-API";
    public final static String clientSecret = "**********";
    private final static String userName = "admin";
    private final static String password = "admin";

    public KeycloakConfig() {
    }

    @PostConstruct
    public void init() {
        KeycloakConfig.setServerUrl(serverUrlProperty);
    }

    private static void setServerUrl(String url) {
        serverUrl = url;
    }

    public static Keycloak getInstance(){
        if(keycloak == null){
            log.info("Initializing Keycloak with server URL: {}", serverUrl);

            keycloak = KeycloakBuilder.builder()
                    .serverUrl(serverUrl)
                    .realm(realm)
                    .grantType(OAuth2Constants.PASSWORD)
                    .username(userName)
                    .password(password)
                    .clientId(clientId)
                    .clientSecret(clientSecret)
                    .resteasyClient(new ResteasyClientBuilder()
                            .connectionPoolSize(10)
                            .build()
                    )
                    .build();
        }
        return keycloak;
    }
}
