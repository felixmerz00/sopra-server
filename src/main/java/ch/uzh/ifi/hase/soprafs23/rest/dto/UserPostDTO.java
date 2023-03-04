package ch.uzh.ifi.hase.soprafs23.rest.dto;

public class UserPostDTO {

  private String password;

  private String username;

  public String getPassword() {
    return password;
  }

  public void setPassword(String name) {
    this.password = name;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }
}
