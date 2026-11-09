package com.bagoye.board.audit;

import com.bagoye.board.common.SecurityEventType;
import com.bagoye.board.entity.SecurityEventEntity;
import com.bagoye.board.repository.SecurityEventRepository;
import jakarta.annotation.PreDestroy;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Slf4j
@Component
public class SecurityEventRecorder {

    private final SecurityEventRepository securityEventRepository;
    private final ThreadPoolTaskExecutor executor;

    public SecurityEventRecorder(SecurityEventRepository securityEventRepository) {
        this.securityEventRepository = securityEventRepository;

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(1);
        executor.setMaxPoolSize(2);
        executor.setQueueCapacity(1000);
        executor.setThreadNamePrefix("security-event-");
        executor.setRejectedExecutionHandler((task, pool) ->
                log.warn("[SECURITY_EVENT] Queue full. Event dropped."));
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(5);
        executor.initialize();
        this.executor = executor;
    }

    @PreDestroy
    public void shutdown() {
        executor.shutdown();
    }

    public void record(SecurityEventType eventType, String email, String detail) {
        try {
            HttpServletRequest request = currentRequest();
            String ipAddress = request == null ? null : request.getRemoteAddr();
            String requestUri = request == null ? null : request.getRequestURI();
            String resolvedEmail = email != null ? email : currentUserEmail();

            SecurityEventEntity securityEventEntity = new SecurityEventEntity(
                    eventType,
                    clean(resolvedEmail, 100),
                    clean(ipAddress, 45),
                    clean(requestUri, 255),
                    clean(detail, 255)
            );

            log.warn("[SECURITY_EVENT] type={}, email={}, ip={}, uri={}, detail={}",
                    eventType, securityEventEntity.getEmail(), securityEventEntity.getIpAddress(),
                    securityEventEntity.getRequestUri(), securityEventEntity.getDetail());

            executor.execute(() -> save(securityEventEntity));

        } catch (Exception exception) {
            log.error("[SECURITY_EVENT] Failed to record event. type={}", eventType, exception);
        }
    }

    private void save(SecurityEventEntity securityEventEntity) {
        try {
            securityEventRepository.save(securityEventEntity);
        } catch (Exception exception) {
            log.error("[SECURITY_EVENT] Failed to save event. type={}", securityEventEntity.getEventType(), exception);
        }
    }

    private HttpServletRequest currentRequest() {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (attributes instanceof ServletRequestAttributes servletRequestAttributes) {
            return servletRequestAttributes.getRequest();
        }
        return null;
    }

    private String currentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) return null;
        return authentication.getName();
    }

    private static String clean(String value, int maxLength) {
        if (value == null) return null;
        String cleaned = value.replaceAll("[\\p{Cntrl}]", "_");
        return cleaned.length() > maxLength ? cleaned.substring(0, maxLength) : cleaned;
    }
}
