package com.sjmt.SJMT.Controller;

import com.sjmt.SJMT.DTO.RequestDTO.ClientErrorRequest;
import com.sjmt.SJMT.DTO.ResponseDTO.ApiResponse;
import com.sjmt.SJMT.Service.ErrorNotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Receives unhandled error reports from the React frontend.
 *
 * <p>The endpoint is intentionally unauthenticated so that errors occurring
 * before or during login can still be captured. Each report is written to
 * the application log (and therefore to {@code logs/sjmt-errors.log}) so
 * developers have a single place to look for both backend and frontend
 * failures.
 */
@RestController
@RequestMapping("/api/client-errors")
public class ClientErrorController {

    private static final Logger logger = LoggerFactory.getLogger(ClientErrorController.class);

    @Autowired(required = false)
    private ErrorNotificationService errorNotificationService;

    /**
     * POST /api/client-errors
     *
     * @param request error details captured by the frontend
     * @return 200 OK always — the frontend should never retry on failure
     */
    @PostMapping
    public ResponseEntity<ApiResponse<Void>> receiveClientError(
            @RequestBody ClientErrorRequest request) {

        logger.error(
            "[FRONTEND-ERROR] source={} | url={} | timestamp={} | context={} | message={}\nStack:\n{}",
            request.getSource(),
            request.getUrl(),
            request.getTimestamp(),
            request.getContext(),
            request.getMessage(),
            request.getStack()
        );

        if (errorNotificationService != null) {
            errorNotificationService.sendErrorAlert(
                "Frontend – " + request.getSource(),
                request.getMessage(),
                request.getStack(),
                request.getUrl()
            );
        }

        return ResponseEntity.ok(ApiResponse.success("Error logged", null));
    }
}
