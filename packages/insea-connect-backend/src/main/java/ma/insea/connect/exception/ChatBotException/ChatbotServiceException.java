package ma.insea.connect.exception.ChatException;

public class ChatbotServiceException extends RuntimeException {
    public ChatbotServiceException(String message) {
        super(message);
    }
}