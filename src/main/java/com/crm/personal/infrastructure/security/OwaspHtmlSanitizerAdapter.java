package com.crm.personal.infrastructure.security;

import com.crm.personal.application.shared.HtmlSanitizerPort;
import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;
import org.springframework.stereotype.Component;

@Component
public class OwaspHtmlSanitizerAdapter implements HtmlSanitizerPort {

    private final PolicyFactory policy = new HtmlPolicyBuilder()
            .allowElements("p", "br", "b", "strong", "i", "em", "u", "ul", "ol", "li", "blockquote", "span", "a")
            .allowAttributes("href").onElements("a")
            .allowUrlProtocols("http", "https", "mailto")
            .allowAttributes("style").onElements("span")
            .toFactory();

    @Override
    public String sanitize(String unsafeHtml) {
        if (unsafeHtml == null || unsafeHtml.isBlank()) {
            return "";
        }
        return policy.sanitize(unsafeHtml);
    }
}
