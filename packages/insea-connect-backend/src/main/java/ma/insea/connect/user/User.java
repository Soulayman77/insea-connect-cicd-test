package ma.insea.connect.user;

import jakarta.persistence.*;
import lombok.*;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

@Entity
@AllArgsConstructor
@Getter
@Setter
@Builder
@NoArgsConstructor
@Table(name="chat_user",schema = "testo")
@EntityListeners(AuditingEntityListener.class)
public class User implements UserDetails {
        @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String email;
    private String username;
    private String passwordHash;
    private String imagrUrl;
    private String firstname;
    private String lastname;
    private String dateOfBirth;
    private String bio;

    @Enumerated(value = EnumType.STRING)
    private Role role = Role.STUDENT;

    @CreatedDate
    @Column(updatable = false,nullable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(insertable = false)
    private LocalDateTime updatedAt;
    
    private Status status;
    private Date lastLogin;
    private List<Long> groups = new ArrayList<>();

    

    // @ManyToOne
    // private DegreePath degreePath;


    public void addGroup(Long group){
            List<Long> groups = this.getGroups();
            if (groups == null) 
            {
                groups = new ArrayList<Long>();
                
            }
            groups.add(group);

        }


    public void removeGroup(Long groupId) {
        List<Long> groups = this.getGroups();
        if (groups == null) {
            groups = new ArrayList<Long>();
        }
        groups.remove(groupId);
    }

    

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
       return List.of(new SimpleGrantedAuthority(role.name()));
    }

   

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }


    @Override
    public String getUsername() {
        return username;
    }


    @Override
    public String getPassword() {
        return passwordHash;
    }


  

     


}