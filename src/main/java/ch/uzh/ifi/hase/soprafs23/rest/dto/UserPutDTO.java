package ch.uzh.ifi.hase.soprafs23.rest.dto;

import java.time.LocalDate;

public class UserPutDTO {

    private String username;
    private LocalDate birthday;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setBirthday(LocalDate birthday) {
        this.birthday = birthday;
    }

    public LocalDate getBirthday() {
        return birthday;
    }
}

