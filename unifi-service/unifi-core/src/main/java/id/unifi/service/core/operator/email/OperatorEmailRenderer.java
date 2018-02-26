package id.unifi.service.core.operator.email;

import com.samskivert.mustache.Mustache;
import com.samskivert.mustache.Template;
import com.statemachinesystems.envy.Default;
import id.unifi.service.common.provider.EmailSenderProvider;
import id.unifi.service.common.security.TimestampedToken;
import id.unifi.service.core.operator.OperatorInfo;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Base64;
import java.util.Map;
import java.util.function.Function;

public class OperatorEmailRenderer {
    private final String baseUrlFormat;
    private final Template htmlInvitationTemplate;
    private final Template htmlPasswordResetRequestedTemplate;
    private final Base64.Encoder base64;
    private final Template textInvitationTemplate;
    private final Template textPasswordResetRequestedTemplate;

    private interface Config {
        @Default("local.unifi.id:3000")
        String webServerTopDomain();

        @Default("http")
        String webServerScheme();
    }

    public OperatorEmailRenderer(Config config) {
        this.baseUrlFormat = String.format("%s://%%s.%s", config.webServerScheme(), config.webServerTopDomain());
        Function<String, Mustache.TemplateLoader> loaderByFormat = format -> name -> {
            String filename = String.format("%s.%s.mustache", name, format);
            InputStream stream = OperatorEmailRenderer.class.getResourceAsStream(filename);
            if (stream == null) throw new RuntimeException("Mustache template " + filename + " not found");
            return new BufferedReader(new InputStreamReader(stream));
        };
        Mustache.Compiler htmlCompiler = Mustache.compiler().withLoader(loaderByFormat.apply("html")).escapeHTML(true);
        Mustache.Compiler textCompiler = Mustache.compiler().withLoader(loaderByFormat.apply("txt")).escapeHTML(false);
        this.htmlInvitationTemplate = compileTemplate(htmlCompiler, "invitation");
        this.textInvitationTemplate = compileTemplate(textCompiler, "invitation");
        this.htmlPasswordResetRequestedTemplate = compileTemplate(htmlCompiler, "password-reset-requested");
        this.textPasswordResetRequestedTemplate = compileTemplate(textCompiler, "password-reset-requested");
        this.base64 = Base64.getUrlEncoder();
    }

    public EmailSenderProvider.EmailMessage renderInvitation(OperatorInfo operator,
                                                             TimestampedToken token,
                                                             OperatorInfo onboarder) {
        Map<String, Object> context = Map.of(
                "operator", operator,
                "onboarder", onboarder,
                "setPasswordUrl", String.format("%s/accept-invitation/%s/%s",
                        baseUrlForClient(operator.clientId), operator.username, base64.encodeToString(token.encoded())),
                "expiryTime", "24 hours" // TODO: use actual expiry time
        );

        String htmlBody = htmlInvitationTemplate.execute(context);
        String textBody = textInvitationTemplate.execute(context);
        return new EmailSenderProvider.EmailMessage("Invitation to join unifi.id", htmlBody, textBody);
    }

    public EmailSenderProvider.EmailMessage renderPasswordResetInstructions(OperatorInfo operator,
                                                                            TimestampedToken token) {
        String baseUrl = baseUrlForClient(operator.clientId);
        String encodedToken = base64.encodeToString(token.encoded());
        Map<String, Object> context = Map.of(
                "operator", operator,
                "setPasswordUrl", String.format("%s/reset-password/%s/%s", baseUrl, operator.username, encodedToken),
                "cancelUrl", String.format("%s/cancel-password-reset/%s/%s", baseUrl, operator.username, encodedToken),
                "expiryTime", "24 hours" // TODO: use actual expiry time
        );

        String htmlBody = htmlPasswordResetRequestedTemplate.execute(context);
        String textBody = textPasswordResetRequestedTemplate.execute(context);
        return new EmailSenderProvider.EmailMessage("Password reset", htmlBody, textBody);
    }

    private static Template compileTemplate(Mustache.Compiler htmlCompiler, String templateName) {
        try (Reader reader = htmlCompiler.loader.getTemplate(templateName)) {
            return htmlCompiler.compile(reader);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String baseUrlForClient(String clientId) {
        return String.format(baseUrlFormat, clientId);
    }
}
