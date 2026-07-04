package com.example.demo.service.auth;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

public class UserDetailsPrinciple implements UserDetails {

    private final String email;
    private final Collection<? extends GrantedAuthority> authorities;

    // ---- Builder -----
    public static class Builder{
        private  String email;
        private  Collection<? extends GrantedAuthority> authorities;

        public Builder email(String email){
            this.email = email;
            return this;
        }

        public Builder authorities(Collection<? extends GrantedAuthority> authorities){
            this.authorities= authorities;
            return this;
        }

        public UserDetailsPrinciple build(){
            return new UserDetailsPrinciple(email,authorities);
        }

    }


   // Builder constructor or Public constructor
    public UserDetailsPrinciple(String email, Collection<? extends GrantedAuthority> authorities) {
        this.email = email;
        this.authorities = authorities;
    }

    public String getEmail() {
        return email;
    }

    // Spring Security Method
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return email;
    }

    @Override
    public String getUsername() {
        return null;
    }

}
