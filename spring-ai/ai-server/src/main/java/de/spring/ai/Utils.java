package de.spring.ai;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.function.Consumer;

import org.jspecify.annotations.Nullable;
import org.slf4j.MDC;
import org.springframework.ai.chat.metadata.Usage;

/**
 * @author Thomas Freese
 */
public final class Utils {
    public static String toHtml(final String prompt, final LocalDateTime start, @Nullable final Usage usage, final Consumer<StringBuilder> appendableConsumer) {
        final Duration duration = Duration.between(start, LocalDateTime.now());
        final String durationString = "%02d:%02d.%03d [mm:ss:SSS]".formatted(duration.toMinutes(), duration.toSecondsPart(), duration.toMillisPart());

        final StringBuilder sb = new StringBuilder();
        sb.append("<html>");
        sb.append("<head><title>Ai-Demo</title></head>");
        sb.append("<body style=\"background-color: lightgray\">");
        sb.append("<b>Question: ").append(prompt).append("</b><br>");
        sb.append("<b>Duration: ").append(durationString).append("</b><br>");
        sb.append("<br>");

        final BigDecimal maxBudget = Optional.ofNullable(MDC.get("x-litellm-key-max-budget")).map(BigDecimal::new).orElse(BigDecimal.ZERO);
        final BigDecimal usedBudget = Optional.ofNullable(MDC.get("x-litellm-key-spend")).map(BigDecimal::new).orElse(BigDecimal.ZERO).setScale(6, RoundingMode.HALF_UP);
        final BigDecimal responseBudget = Optional.ofNullable(MDC.get("x-litellm-response-cost")).map(BigDecimal::new).orElse(BigDecimal.ZERO).setScale(6, RoundingMode.HALF_UP);

        sb.append("<b>Max. Budget: ").append(maxBudget).append(" $</b><br>");
        sb.append("<b>Used Budget: ").append(usedBudget).append(" $</b><br>");
        sb.append("<b>Response Cost: ").append(responseBudget).append(" $</b><br>");
        sb.append("<br>");

        if (usage != null) {
            sb.append("<b>Prompt Tokens: ").append(usage.getPromptTokens()).append("</b><br>");
            sb.append("<b>Completion Tokens: ").append(usage.getCompletionTokens()).append("</b><br>");
            sb.append("<b>Total Tokens: ").append(usage.getTotalTokens()).append("</b><br>");
            sb.append("<br>");
        }

        sb.append("<b>Answer:</b><br>");

        appendableConsumer.accept(sb);

        sb.append("<br>");
        sb.append("</body>");
        sb.append("</html>");

        MDC.remove("x-litellm-key-max-budget");
        MDC.remove("x-litellm-key-spend");
        MDC.remove("x-litellm-response-cost");

        return sb.toString();
    }

    private Utils() {
        super();
    }
}
