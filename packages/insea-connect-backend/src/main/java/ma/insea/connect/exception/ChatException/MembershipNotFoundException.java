package ma.insea.connect.exception.ChatException;

public class MembershipNotFoundException extends RuntimeException {
    public MembershipNotFoundException(Long userId, Long groupId) {
        super("Membership for user with ID " + userId + " in group with ID " + groupId + " not found.");
    }
}