package com.bagoye.board.exception;

import com.bagoye.board.audit.SecurityEventRecorder;
import com.bagoye.board.common.SecurityEventType;
import com.bagoye.board.dto.response.ResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@RestControllerAdvice
@RequiredArgsConstructor
public class BadRequestExceptionHandler {

    private final SecurityEventRecorder securityEventRecorder;

    @ExceptionHandler({
            MethodArgumentNotValidException.class,
            HttpMessageNotReadableException.class,
            MethodArgumentTypeMismatchException.class
    })
    public ResponseEntity<ResponseDto> validationExceptionHandler(Exception exception) {
        return ResponseDto.validationFailed();
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ResponseDto> maxUploadSizeExceededHandler(MaxUploadSizeExceededException exception) {
        securityEventRecorder.record(SecurityEventType.UPLOAD_BLOCKED, null, "size-limit-exceeded");
        return ResponseDto.validationFailed();
    }
}
