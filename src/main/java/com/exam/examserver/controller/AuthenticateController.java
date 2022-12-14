package com.exam.examserver.controller;

import com.exam.examserver.config.JwtUtils;
import com.exam.examserver.model.authentication.JwtRequest;
import com.exam.examserver.model.authentication.JwtResponse;
import com.exam.examserver.model.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@CrossOrigin("*")
public class AuthenticateController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    @Qualifier("UserDetailsSpringSecurityServiceImpl")
    private UserDetailsService userDetailsService;

    @Autowired
    private JwtUtils jwtUtils;

    //Generate Token
    @PostMapping("/generate-token")
    public ResponseEntity<?> generateToken(@RequestBody JwtRequest jwtRequest) throws Exception {
        try {
            this.authenticate(jwtRequest.getUsername(), jwtRequest.getPassword());
        } catch (UsernameNotFoundException usernameNotFoundException) {
            usernameNotFoundException.printStackTrace();
            throw new UsernameNotFoundException("Username: " + jwtRequest.getUsername() + " not Found in Database");
        } catch (Exception exception) {
            exception.printStackTrace();
            throw new Exception("Exception while Authenticating User");
        }

        UserDetails user = this.userDetailsService.loadUserByUsername(jwtRequest.getUsername());
        String jwtToken = this.jwtUtils.generateToken(user);
        return ResponseEntity.ok(new JwtResponse(jwtToken));
    }

    //Returns the details of the current user
    @GetMapping("/current-user")
    public User getCurrentUser(Principal principal) {
        return (User) this.userDetailsService.loadUserByUsername(principal.getName());
    }

    private void authenticate(String username, String password) throws Exception {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
        } catch (DisabledException disabledUserException) {
            throw new Exception("User: " + username + " " + disabledUserException.getMessage());
        } catch (BadCredentialsException badCredentialsException) {
            throw new Exception("Invalid Credentials " + badCredentialsException.getMessage());
        }
    }
}
