package ma.insea.connect.user.service;


import lombok.AllArgsConstructor;
import ma.insea.connect.exception.userExceptions.InvalidUserDataException;
import ma.insea.connect.exception.userExceptions.UserNotFoundException;
import ma.insea.connect.user.DTO.OnlineDTO;
import ma.insea.connect.user.DTO.User;
import ma.insea.connect.user.DTO.UserDTO;
import ma.insea.connect.user.model.Status;
import ma.insea.connect.user.repository.UserRepository;
import ma.insea.connect.utils.Functions;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final Functions functions;

    public User saveUser(User user) {
        if (user == null || user.getEmail() == null || user.getUsername() == null) {
            throw new InvalidUserDataException("User data is incomplete or invalid.");
        }
        user.setStatus(Status.ONLINE);
        userRepository.save(user);
        return user;
    }

    public void disconnect(User user) {
        if (user == null || user.getEmail() == null) {
            throw new InvalidUserDataException("User data is incomplete or invalid.");
        }
        var storedUser = userRepository.findByEmail(user.getEmail());
        if (storedUser == null) {
            throw new UserNotFoundException("User with email " + user.getEmail() + " not found.");
        }
        storedUser.setStatus(Status.OFFLINE);
        storedUser.setLastLogin(new java.util.Date(System.currentTimeMillis()));
        userRepository.save(storedUser);
    }

    public List<UserDTO> findAllUsers() {
        List<User> users = userRepository.findAll();
        if (users.isEmpty()) {
            throw new UserNotFoundException("No users found.");
        }
        List<UserDTO> userDTOs = new ArrayList<>();
        for (User user : users) {
            UserDTO userDTO = new UserDTO(user.getId(), user.getUsername(), user.getEmail(), user.getStatus(), user.getLastLogin());
            userDTOs.add(userDTO);
        }
        return userDTOs;
    }

    public OnlineDTO getUserStatus(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User with ID " + userId + " not found."));
        return new OnlineDTO(user.getStatus(), user.getLastLogin());
    }

    public void updateUserLastSeen(Status status) {
        User connectedUser = functions.getConnectedUser();
        if (connectedUser == null) {
            throw new UserNotFoundException("Connected user not found.");
        }
        connectedUser.setLastLogin(new java.util.Date(System.currentTimeMillis()));
        connectedUser.setStatus(status);
        userRepository.save(connectedUser);
    }

    @Scheduled(fixedRate = 60000)
    public void checkUserStatuses() {
        List<User> users = userRepository.findAll();
        for (User user : users) {
            LocalDateTime lastLogin = LocalDateTime.ofInstant(user.getLastLogin().toInstant(), ZoneId.systemDefault());
            if (lastLogin.isBefore(lastLogin.now().minusSeconds(30))) { // Offline if no heartbeat for 2 minutes
                user.setStatus(Status.OFFLINE);
                userRepository.save(user);
            }
        }
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User with username " + username + " not found."));
    }

    public User findByEmail(String email) {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new UserNotFoundException("User with email " + email + " not found.");
        }
        return user;
    }
}