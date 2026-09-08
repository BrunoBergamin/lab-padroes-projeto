package br.com.bergamin.checkout.config;

import br.com.bergamin.checkout.checkout.OrderNotFoundException;
import br.com.bergamin.checkout.checkout.PaymentDeclinedException;
import br.com.bergamin.checkout.order.InvalidTransitionException;
import br.com.bergamin.checkout.rules.CheckoutRejectedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.List;

/** Traduz as excecoes de dominio para status HTTP, sem try/catch espalhado nos controllers. */
@RestControllerAdvice
public class ApiExceptionHandler {

    public record ApiError(int status, String erro, String detalhe, String origem, Instant momento) {

        static ApiError of(HttpStatus status, String detail, String source) {
            return new ApiError(status.value(), status.getReasonPhrase(), detail, source, Instant.now());
        }
    }

    @ExceptionHandler(CheckoutRejectedException.class)
    public ResponseEntity<ApiError> rejected(CheckoutRejectedException e) {
        return ResponseEntity.unprocessableEntity()
                .body(ApiError.of(HttpStatus.UNPROCESSABLE_ENTITY, e.getMessage(), e.rule()));
    }

    @ExceptionHandler(PaymentDeclinedException.class)
    public ResponseEntity<ApiError> declined(PaymentDeclinedException e) {
        return ResponseEntity.status(HttpStatus.PAYMENT_REQUIRED)
                .body(ApiError.of(HttpStatus.PAYMENT_REQUIRED, e.getMessage(), "PaymentGateway"));
    }

    @ExceptionHandler(InvalidTransitionException.class)
    public ResponseEntity<ApiError> transition(InvalidTransitionException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiError.of(HttpStatus.CONFLICT, e.getMessage(), "OrderStatus"));
    }

    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<ApiError> notFound(OrderNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiError.of(HttpStatus.NOT_FOUND, e.getMessage(), "OrderRepository"));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> invalid(MethodArgumentNotValidException e) {
        List<String> fields = e.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + " " + error.getDefaultMessage())
                .toList();
        return ResponseEntity.badRequest()
                .body(ApiError.of(HttpStatus.BAD_REQUEST, String.join("; ", fields), "Validacao"));
    }
}
