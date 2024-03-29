package ch.uzh.ifi.hase.soprafs23.service;

import ch.uzh.ifi.hase.soprafs23.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs23.entity.User;
import ch.uzh.ifi.hase.soprafs23.repository.UserRepository;
import javassist.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

/**
 * User Service
 * This class is the "worker" and responsible for all functionality related to
 * the user
 * (e.g., it creates, modifies, deletes, finds). The result will be passed back
 * to the caller.
 */
@Service
@Transactional
public class UserService {

  private final Logger log = LoggerFactory.getLogger(UserService.class);

  private final UserRepository userRepository;

  @Autowired
  public UserService(@Qualifier("userRepository") UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  public List<User> getUsers() {
    return this.userRepository.findAll();
  }

  public User createUser(User newUser) {
    newUser.setToken(UUID.randomUUID().toString());
    newUser.setStatus(UserStatus.ONLINE);
    newUser.setCreationDate();
    checkIfUserExists(newUser);
    // saves the given entity but data is only persisted in the database once
    // flush() is called
    newUser = userRepository.save(newUser);
    userRepository.flush();

    log.debug("Created Information for User: {}", newUser);
    return newUser;
  }

  /**
   * This is a helper method that will check the uniqueness criteria of the
   * username and the password
   * defined in the User entity. The method will do nothing if the input is unique
   * and throw an error otherwise.
   *
   * @param userToBeCreated
   * @throws org.springframework.web.server.ResponseStatusException
   * @see User
   */
  private void checkIfUserExists(User userToBeCreated) {
    User userByUsername = userRepository.findByUsername(userToBeCreated.getUsername());

    String errorMessage = "add User failed because username already exists";
    if (userByUsername != null) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, errorMessage);
    }
  }

  /* If the entered username does not exist or the password does not match, reject the login attempt. */
    public User checkIfUserExistsReverse(User userToBeLoggedIn) {
        User userByUsername = userRepository.findByUsername(userToBeLoggedIn.getUsername());

        String baseErrorMessage = "Sorry, your username or password was incorrect. Please double-check your credentials";
        if (userByUsername == null || !userByUsername.getPassword().equals(userToBeLoggedIn.getPassword()) ) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    String.format(baseErrorMessage));
        }else{
            userByUsername.setStatus(UserStatus.ONLINE);
            userByUsername = userRepository.save(userByUsername);
            userRepository.flush();

        }
        return userByUsername;
    }

    public User getUserProfile(long id) {
        Optional<User> outUser = userRepository.findById(id);

        String errorMessage = "user with " + id + " was not found";
        if(outUser.isEmpty()){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND , errorMessage);
        }
        // outUser is of type Optional. To get the actual User object I use the get() method.
        return outUser.get();
    }

    public User putChanges(Long userId, User userInput) {
        Optional<User> optionalUserInDatabase = userRepository.findById(userId);
        User userInDatabase;
        if(optionalUserInDatabase.isPresent()){
            userInDatabase = optionalUserInDatabase.get();
        }else{
            String errorMessage = "user with userId " + userId + " was not found";
            throw new ResponseStatusException(HttpStatus.NOT_FOUND , errorMessage);
        }
        // check if username was edited and is unique
        String newUsername = userInput.getUsername();
        if(newUsername != null && !userInDatabase.getUsername().equals(newUsername)){
            // Check if the new username is unique
            checkIfUsernameUnique(newUsername);
            userInDatabase.setUsername(newUsername);
        }
        // check if birthday was edited
        LocalDate newBirthday = userInput.getBirthday();
        if(newBirthday != null && userInDatabase.getBirthday() != newBirthday){
            userInDatabase.setBirthday(newBirthday);
        }
        userRepository.save(userInDatabase);
        userRepository.flush();
        return userInDatabase;
    }

    // checks if the new username is unique
    private void checkIfUsernameUnique(String newUsername) {
        User userByUsername = userRepository.findByUsername(newUsername);

        String errorMessage = "The username provided is not unique. Therefore, the username could not be changed!";
        if (userByUsername != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, errorMessage);
        }
    }

    public void setOffline(Long userId) {
        Optional<User> userByIdOptional = userRepository.findById(userId);
        if(userByIdOptional.isPresent()){
            User userById = userByIdOptional.get();
            userById.setStatus(UserStatus.OFFLINE);
            userRepository.save(userById);
            userRepository.flush();
        }
    }
}
