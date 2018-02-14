package id.unifi.service.core.operator.email;

import com.samskivert.mustache.Mustache;
import com.samskivert.mustache.Template;
import com.statemachinesystems.envy.Default;
import id.unifi.service.common.operator.OperatorPK;
import id.unifi.service.common.provider.EmailSenderProvider;
import id.unifi.service.common.security.TimestampedToken;
import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Base64;
import java.util.Map;

public class OperatorEmailRenderer {
    private final String baseUrlFormat;
    private final Template htmlInvitationTemplate;
    private final Template htmlPasswordResetRequestedTemplate;
    private final Base64.Encoder base64;

    private interface Config {
        @Default("local.unifi.id:3000")
        String webServerTopDomain();

        @Default("http")
        String webServerScheme();
    }

    public OperatorEmailRenderer(Config config) {
        this.baseUrlFormat = String.format("%s://%%s.%s", config.webServerScheme(), config.webServerTopDomain());

        Mustache.Compiler htmlCompiler = Mustache.compiler().escapeHTML(true).defaultValue("???");
        this.htmlInvitationTemplate = compileTemplate(htmlCompiler, "invitation");
        this.htmlPasswordResetRequestedTemplate = compileTemplate(htmlCompiler, "password-reset-requested");
        this.base64 = Base64.getUrlEncoder();
    }

    public EmailSenderProvider.EmailMessage renderInvitation(String clientId,
                                                             String username,
                                                             TimestampedToken token,
                                                             OperatorPK onboarder) {
        Map<String, Object> context = Map.of(
                "clientId", clientId,
                "username", username,
                "onboarder", onboarder,
                "setPasswordUrl", String.format("%s/reset-password/%s/%s",
                        baseUrlForClient(clientId), username, base64.encodeToString(token.encoded()))
        );

        String htmlBody = htmlInvitationTemplate.execute(context);
        return new EmailSenderProvider.EmailMessage("Invitation to join ???", htmlBody);
    }

    public EmailSenderProvider.EmailMessage renderPasswordResetInstructions(String clientId,
                                                                            String username,
                                                                            TimestampedToken token) {
        String baseUrl = baseUrlForClient(clientId);
        String encodedToken = base64.encodeToString(token.encoded());
        Map<String, Object> context = Map.of(
                "setPasswordUrl", String.format("%s/reset-password/%s/%s", baseUrl, username, encodedToken),
                "cancelUrl", String.format("%s/password-reset-cancel/%s/%s", baseUrl, username, encodedToken)
        );

        String htmlBody = htmlPasswordResetRequestedTemplate.execute(context);
        return new EmailSenderProvider.EmailMessage("Password reset", htmlBody);
    }

    private static Template compileTemplate(Mustache.Compiler htmlCompiler, String templateName) {
        InputStream resourceStream =
                OperatorEmailRenderer.class.getResourceAsStream(templateName + ".mustache");
        try (Reader reader = new BufferedReader(new InputStreamReader(resourceStream, UTF_8))) {
            return htmlCompiler.compile(reader);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String baseUrlForClient(String clientId) {
        return String.format(baseUrlFormat, clientId);
    }
}
