package ma.insea.connect.chat.group;


import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GroupDTO3 {
    private Long id;
    private String imagrUrl;
    private String name;
    private String description;
    private Boolean isOfficial;
    private Date createdDate;
}
