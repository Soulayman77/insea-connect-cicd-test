package ma.insea.connect.exception.ChatException;

public class UnauthorizedConversationAccessException extends RuntimeException {
    public UnauthorizedConversationAccessException(String message) {
        super(message);
    }
}