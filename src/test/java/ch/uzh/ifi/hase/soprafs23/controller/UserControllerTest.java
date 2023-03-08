package ch.uzh.ifi.hase.soprafs23.controller;

import ch.uzh.ifi.hase.soprafs23.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs23.entity.User;
import ch.uzh.ifi.hase.soprafs23.rest.dto.UserPostDTO;
import ch.uzh.ifi.hase.soprafs23.rest.dto.UserPutDTO;
import ch.uzh.ifi.hase.soprafs23.service.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * UserControllerTest
 * This is a WebMvcTest which allows to test the UserController i.e. GET/POST
 * request without actually sending them over the network.
 * This tests if the UserController works.
 */
@WebMvcTest(UserController.class)
public class UserControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private UserService userService;

  // Test request to GET all users
  @Test
  public void givenUsers_whenGetUsers_thenReturnJsonArray() throws Exception {
    // given
    User user = new User();
    user.setPassword("password123");

    user.setId(1L);
    user.setUsername("firstname@lastname");
    user.setCreationDate();
    user.setStatus(UserStatus.OFFLINE);
    user.setBirthday(LocalDate.parse("2000-07-06"));

    List<User> allUsers = Collections.singletonList(user);

    // this mocks the UserService -> we define above what the userService should
    // return when getUsers() is called
    given(userService.getUserProfile(1)).willReturn(user);

    // when
    MockHttpServletRequestBuilder getRequest = get("/users/1").contentType(MediaType.APPLICATION_JSON);

    // then
    mockMvc.perform(getRequest)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id", is(user.getId().intValue())))
            .andExpect(jsonPath("$.username", is(user.getUsername())))
            .andExpect(jsonPath("$.creationDate", is(user.getCreationDate().toString())))
            .andExpect(jsonPath("$.status", is(user.getStatus().toString())))
            .andExpect((jsonPath("$.birthday", is(user.getBirthday().toString()))));
  }

    // Test request to GET all users
    @Test
    public void notExistingUser_whenGetUsers_notFoundRaised() throws Exception {

        // this mocks the UserService -> we define above what the userService should
        // return when getUsers() is called
        given(userService.getUserProfile(1)).willThrow(new ResponseStatusException(HttpStatus.NOT_FOUND));

        // when
        MockHttpServletRequestBuilder getRequest = get("/users/1").contentType(MediaType.APPLICATION_JSON);

        // then
        mockMvc.perform(getRequest)
                .andExpect(status().isNotFound());
    }

  // Test POST request where a user is created with a valid username and password
  @Test
  public void createUser_validInput_userCreated() throws Exception {
    // given
    User user = new User();
    user.setId(1L);
    user.setPassword("password123");
    user.setUsername("testUsername");
    user.setCreationDate();
    user.setToken("1");
    user.setStatus(UserStatus.ONLINE);
    user.setBirthday(null);

    UserPostDTO userPostDTO = new UserPostDTO();
    userPostDTO.setPassword("password123");
    userPostDTO.setUsername("testUsername");

    given(userService.createUser(Mockito.any())).willReturn(user);

    // when/then -> do the request + validate the result
    MockHttpServletRequestBuilder postRequest = post("/users")
        .contentType(MediaType.APPLICATION_JSON)
        .content(asJsonString(userPostDTO));

    // then
    mockMvc.perform(postRequest)
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id", is(user.getId().intValue())))
        // .andExpect(jsonPath("$.name", is(user.getPassword())))   // password doesn't get returned anymore
        .andExpect(jsonPath("$.username", is(user.getUsername())))
            .andExpect(jsonPath("$.creationDate", is(user.getCreationDate().toString())))
        .andExpect(jsonPath("$.status", is(user.getStatus().toString())))
            .andExpect(jsonPath("$.birthday", is(user.getBirthday())));
  }


    // Test POST request where conflict is raised
    @Test
    public void createUser_inValidInput_conflictRaised() throws Exception {

        UserPostDTO userPostDTO = new UserPostDTO();
        userPostDTO.setPassword("password123");
        userPostDTO.setUsername("testUsername");

        given(userService.createUser(Mockito.any())).willThrow(new ResponseStatusException(HttpStatus.CONFLICT));

        // when/then -> do the request + validate the result
        MockHttpServletRequestBuilder postRequest = post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(userPostDTO));

        // then
        mockMvc.perform(postRequest)
                .andExpect(status().isConflict());
    }


/*
    // Test valid PUT request
    @Test
    public void editUser_validInput_userEdited() throws Exception {
        // given
        User user = new User();
        user.setId(1L);
        user.setPassword("password123");
        user.setUsername("felixNew");
        user.setCreationDate();
        user.setToken("1");
        user.setStatus(UserStatus.ONLINE);
        user.setBirthday(LocalDate.parse("2000-07-06"));

        given(userService.createUser(Mockito.any())).willReturn(user);

        UserPutDTO userPutDTO = new UserPutDTO();
        userPutDTO.setUsername("felixNew");
        userPutDTO.setBirthday(LocalDate.parse("2000-07-06"));

        // when/then -> do the request + validate the result
        MockHttpServletRequestBuilder putRequest = put("/users/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(userPutDTO));

        // then
        mockMvc.perform(putRequest)
                .andExpect(status().isResetContent())
                .andExpect(jsonPath("$.id", is(user.getId().intValue())))
                .andExpect(jsonPath("$.username", is(user.getUsername())))
                .andExpect(jsonPath("$.creationDate", is(user.getCreationDate().toString())))
                .andExpect(jsonPath("$.status", is(user.getStatus().toString())))
                .andExpect(jsonPath("$.birthday", is(user.getBirthday())));
    }

 */



    // Test PUT for invalid input
    /*
    @Test
    public void editUser_inValidInput_notFoundRaised() throws Exception {

        // User user = new User();
        // user.setId(1L);
        // user.setUsername("newfirstname@lastname");
        // user.setBirthday(LocalDate.parse("2000-07-06"));

        UserPutDTO userPutDTO = new UserPutDTO();
        userPutDTO.setUsername("newfirstname@lastname");
        userPutDTO.setBirthday(LocalDate.parse("2000-07-06"));

        given(userService.putChanges(Mockito.anyLong(), Mockito.any())).willThrow(new ResponseStatusException(HttpStatus.NOT_FOUND));

        // when/then -> do the request + validate the result
        MockHttpServletRequestBuilder putRequest = put("/users/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(userPutDTO));

        // then
        mockMvc.perform(putRequest)
                .andExpect(status().isNotFound());
    }

     */

  /**
   * Helper Method to convert userPostDTO into a JSON string such that the input
   * can be processed
   * Input will look like this: {"name": "Test User", "username": "testUsername"}
   * 
   * @param object
   * @return string
   */
  private String asJsonString(final Object object) {
    try {
      return new ObjectMapper().writeValueAsString(object);
    } catch (JsonProcessingException e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          String.format("The request body could not be created.%s", e.toString()));
    }
  }
}