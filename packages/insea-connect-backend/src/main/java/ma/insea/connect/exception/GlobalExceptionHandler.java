package ma.insea.connect.exception;

import ma.insea.connect.exception.ChatBotException.*;
import ma.insea.connect.exception.ChatException.ChatMessageException;
import ma.insea.connect.exception.ChatException.GroupMessageException;
import ma.insea.connect.exception.ChatException.GroupNotFoundException;
import ma.insea.connect.exception.userExceptions.UnauthorizedAccessException;
import ma.insea.connect.exception.userExceptions.UserNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;


@ControllerAdvice
public class GlobalExceptionHandler {

    // Chatbot service related exceptions
    @ExceptionHandler(ChatbotServiceException.class)
    public ResponseEntity<String> handleChatbotServiceException(ChatbotServiceException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(ThreadCreationException.class)
    public ResponseEntity<String> handleThreadCreationException(ThreadCreationException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ChatbotApiException.class)
    public ResponseEntity<String> handleChatbotApiException(ChatbotApiException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.SERVICE_UNAVAILABLE);
    }

    @ExceptionHandler(MessageProcessingException.class)
    public ResponseEntity<String> handleMessageProcessingException(MessageProcessingException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(GroupMessageProcessingException.class)
    public ResponseEntity<String> handleGroupMessageProcessingException(GroupMessageProcessingException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // Chat message service related exceptions
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<String> handleUserNotFoundException(UserNotFoundException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(GroupNotFoundException.class)
    public ResponseEntity<String> handleGroupNotFoundException(GroupNotFoundException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(UnauthorizedAccessException.class)
    public ResponseEntity<String> handleUnauthorizedAccessException(UnauthorizedAccessException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(ChatMessageException.class)
    public ResponseEntity<String> handleChatMessageException(ChatMessageException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(GroupMessageException.class)
    public ResponseEntity<String> handleGroupMessageException(GroupMessageException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // Catch-all exception handler
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGenericException(Exception ex) {
        return new ResponseEntity<>("An unexpected error occurred: " + ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}