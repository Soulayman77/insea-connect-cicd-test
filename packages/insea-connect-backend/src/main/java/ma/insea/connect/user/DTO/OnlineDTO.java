package ma.insea.connect.user.DTO;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Data;
import ma.insea.connect.user.model.Status;

@AllArgsConstructor
@Data
public class OnlineDTO {
    private Status status;
    private Date lastLogin;

}
