package no.nibio.vips.logic.messaging;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class FcmClient {

    public enum SendResult {
        SENT,
        INVALID_TOKEN,
        SKIPPED,
        FAILED
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(FcmClient.class);
    private static final String SCOPE = "https://www.googleapis.com/auth/firebase.messaging";
    private static final String TOKEN_URL = "https://oauth2.googleapis.com/token";
    private static final String ANDROID_CHANNEL_ID = "vips_alerts";

    private final String serviceAccountPath;
    private final ObjectMapper mapper = new ObjectMapper();
    private final HttpClient http = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(15))
        .build();

    private boolean enabled;
    private String projectId;
    private String clientEmail;
    private PrivateKey privateKey;
    private String accessToken;
    private Instant accessTokenExpiresAt = Instant.EPOCH;

    @Inject
    public FcmClient(
            @ConfigProperty(name = "no.nibio.vips.logic.FCM_SERVICE_ACCOUNT_PATH", defaultValue = "")
            String serviceAccountPath) {
        this.serviceAccountPath = serviceAccountPath == null ? "" : serviceAccountPath.trim();
    }

    @PostConstruct
    void init() {
        if (serviceAccountPath.isEmpty()) {
            LOGGER.info("FCM disabled: no.nibio.vips.logic.FCM_SERVICE_ACCOUNT_PATH is not set");
            return;
        }
        Path path = Path.of(serviceAccountPath);
        if (!Files.isRegularFile(path)) {
            LOGGER.warn("FCM disabled: service account file not found at {}", path);
            return;
        }
        try {
            JsonNode json = mapper.readTree(Files.readString(path));
            projectId = text(json, "project_id");
            clientEmail = text(json, "client_email");
            privateKey = parsePkcs8(text(json, "private_key"));
            if (projectId.isEmpty() || clientEmail.isEmpty()) {
                LOGGER.warn("FCM disabled: service account JSON is missing project_id or client_email");
                return;
            }
            enabled = true;
            LOGGER.info("FCM enabled for project {}", projectId);
        } catch (Exception ex) {
            LOGGER.warn("FCM disabled: could not read service account", ex);
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    public SendResult send(String token, String title, String body, String type, String id) {
        if (!enabled) {
            return SendResult.SKIPPED;
        }
        if (token == null || token.isBlank()) {
            return SendResult.INVALID_TOKEN;
        }
        try {
            String bearer = accessToken();
            String payload = mapper.writeValueAsString(messageBody(token, title, body, type, id));
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://fcm.googleapis.com/v1/projects/" + projectId + "/messages:send"))
                .timeout(Duration.ofSeconds(20))
                .header("Authorization", "Bearer " + bearer)
                .header("Content-Type", "application/json; charset=UTF-8")
                .POST(HttpRequest.BodyPublishers.ofString(payload, StandardCharsets.UTF_8))
                .build();
            HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            int status = response.statusCode();
            if (status >= 200 && status < 300) {
                return SendResult.SENT;
            }
            if (isInvalidToken(status, response.body())) {
                LOGGER.info("FCM token no longer valid, will drop it");
                return SendResult.INVALID_TOKEN;
            }
            LOGGER.warn("FCM send failed HTTP {}: {}", status, response.body());
            return SendResult.FAILED;
        } catch (Exception ex) {
            LOGGER.warn("FCM send failed", ex);
            return SendResult.FAILED;
        }
    }

    private Map<String, Object> messageBody(String token, String title, String body, String type, String id) {
        Map<String, String> notification = new LinkedHashMap<>();
        notification.put("title", nullToEmpty(title));
        notification.put("body", nullToEmpty(body));

        Map<String, String> data = new LinkedHashMap<>();
        data.put("type", nullToEmpty(type));
        data.put("id", nullToEmpty(id));

        Map<String, Object> androidNotification = new LinkedHashMap<>();
        androidNotification.put("channel_id", ANDROID_CHANNEL_ID);
        Map<String, Object> android = new LinkedHashMap<>();
        android.put("priority", "HIGH");
        android.put("notification", androidNotification);

        Map<String, Object> alert = new LinkedHashMap<>();
        alert.put("title", nullToEmpty(title));
        alert.put("body", nullToEmpty(body));
        Map<String, Object> aps = new LinkedHashMap<>();
        aps.put("alert", alert);
        aps.put("sound", "default");
        Map<String, Object> apnsPayload = new LinkedHashMap<>();
        apnsPayload.put("aps", aps);
        Map<String, Object> apns = new LinkedHashMap<>();
        apns.put("payload", apnsPayload);

        Map<String, Object> message = new LinkedHashMap<>();
        message.put("token", token);
        message.put("notification", notification);
        message.put("data", data);
        message.put("android", android);
        message.put("apns", apns);

        Map<String, Object> root = new LinkedHashMap<>();
        root.put("message", message);
        return root;
    }

    private synchronized String accessToken() throws IOException, InterruptedException, GeneralSecurityException {
        if (accessToken != null && Instant.now().isBefore(accessTokenExpiresAt.minusSeconds(60))) {
            return accessToken;
        }
        String jwt = signedJwt();
        String form = "grant_type=" + url("urn:ietf:params:oauth:grant-type:jwt-bearer")
            + "&assertion=" + url(jwt);
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(TOKEN_URL))
            .timeout(Duration.ofSeconds(20))
            .header("Content-Type", "application/x-www-form-urlencoded")
            .POST(HttpRequest.BodyPublishers.ofString(form, StandardCharsets.UTF_8))
            .build();
        HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IOException("FCM token HTTP " + response.statusCode() + ": " + response.body());
        }
        JsonNode json = mapper.readTree(response.body());
        accessToken = text(json, "access_token");
        long expiresIn = json.path("expires_in").asLong(3600);
        accessTokenExpiresAt = Instant.now().plusSeconds(expiresIn);
        if (accessToken.isEmpty()) {
            throw new IOException("FCM token response missing access_token");
        }
        return accessToken;
    }

    private String signedJwt() throws GeneralSecurityException {
        long iat = Instant.now().getEpochSecond();
        String header = b64Url("{\"alg\":\"RS256\",\"typ\":\"JWT\"}");
        String payload = b64Url("{"
            + "\"iss\":\"" + clientEmail + "\","
            + "\"scope\":\"" + SCOPE + "\","
            + "\"aud\":\"" + TOKEN_URL + "\","
            + "\"iat\":" + iat + ","
            + "\"exp\":" + (iat + 3600)
            + "}");
        String unsigned = header + "." + payload;
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initSign(privateKey);
        signature.update(unsigned.getBytes(StandardCharsets.UTF_8));
        return unsigned + "." + b64Url(signature.sign());
    }

    private static boolean isInvalidToken(int status, String body) {
        if (status == 404) {
            return true;
        }
        if (body == null) {
            return false;
        }
        String upper = body.toUpperCase();
        return upper.contains("UNREGISTERED")
            || upper.contains("NOT_FOUND")
            || (status == 400 && upper.contains("INVALID_ARGUMENT"));
    }

    private static PrivateKey parsePkcs8(String pem) throws GeneralSecurityException {
        String stripped = pem
            .replace("-----BEGIN PRIVATE KEY-----", "")
            .replace("-----END PRIVATE KEY-----", "")
            .replaceAll("\\s", "");
        byte[] der = Base64.getDecoder().decode(stripped);
        return KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(der));
    }

    private static String text(JsonNode json, String field) {
        JsonNode node = json.path(field);
        return node.isMissingNode() || node.isNull() ? "" : node.asText("").trim();
    }

    private static String b64Url(String value) {
        return b64Url(value.getBytes(StandardCharsets.UTF_8));
    }

    private static String b64Url(byte[] value) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(value);
    }

    private static String url(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
