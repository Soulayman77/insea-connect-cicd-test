package ma.insea.connect.exception.ChatException;

public class GroupNotFoundException extends RuntimeException {
    public GroupNotFoundException(Long groupId) {
        super("Group with ID " + groupId + " not found.");
    }
}