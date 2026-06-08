package com.sidwik.logquery.logs;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
@RequestMapping("/api/logs")
public class LogController {
    private final LogService logService;

    public LogController(LogService logService) {
        this.logService = logService;
    }

    @PostMapping("/publish")
    public PublishResponse publish(@Valid @RequestBody IngestLogRequest request) {
        logService.publish(request);
        return new PublishResponse("ACCEPTED");
    }

    @PostMapping("/ingest")
    public LogResponse ingest(@Valid @RequestBody IngestLogRequest request) {
        return logService.ingest(request);
    }

    @GetMapping("/search")
    public Page<LogResponse> search(
            @RequestParam(required = false) String serviceName,
            @RequestParam(required = false) Severity severity,
            @RequestParam(required = false) Instant from,
            @RequestParam(required = false) Instant to,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size
    ) {
        return logService.search(new SearchLogsRequest(serviceName, severity, from, to, keyword, page, size));
    }

    @PostMapping("/archive")
    public ArchiveResponse archive(@Valid @RequestBody ArchiveRequest request) {
        return logService.archiveBefore(request);
    }

    public record PublishResponse(String status) {
    }
}
