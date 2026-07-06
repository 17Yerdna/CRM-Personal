package com.crm.personal.application.shared;

public interface HtmlSanitizerPort {

    String sanitize(String unsafeHtml);
}
