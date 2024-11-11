package com.icas.party_with_me.controllers;

import com.icas.party_with_me.data.PartiesService;
import com.icas.party_with_me.data.UserRepository;
import com.icas.party_with_me.data.DAO.LoginRequest;
import com.icas.party_with_me.data.DAO.LoginResponse;
import com.icas.party_with_me.data.DAO.Party;
import com.icas.party_with_me.data.DAO.User;
import com.icas.party_with_me.exception.PartyNotFoundException;
import com.icas.party_with_me.security.JWTUtil;

import jakarta.validation.Valid;

import java.util.List;
import java.util.ArrayList;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@Validated
public class AdminController {

    private final UserRepository userRepository;
    private final PartiesService partiesService;
    private final PasswordEncoder passwordEncoder;
    private final JWTUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    public AdminController(UserRepository userRepository, 
                          PasswordEncoder passwordEncoder, 
                          JWTUtil jwtUtil, 
                          AuthenticationManager authenticationManager, PartiesService partiesService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.authenticationManager = authenticationManager;
        this.partiesService = partiesService;
    }

  
    /**
     * Endpoint for user dashboard.
     * Requires a valid JWT token to access.
     */
    @GetMapping("/dashboard")
    public ResponseEntity<?> getDashboard() {
        // Retrieve the authenticated user details from the SecurityContext
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserDetails)) {
            return ResponseEntity.status(403).body("Unauthorized access");
        }

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String username = userDetails.getUsername();

        // Placeholder for actual dashboard logic
        return ResponseEntity.ok("Welcome to your dashboard, " + username);
    }

    /**
     * Endpoint for user parties.
     * Requires a valid JWT token to access.
     */
    @GetMapping("/parties")
    public ResponseEntity<?> getParties() {
        // Retrieve the authenticated user details from the SecurityContext
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserDetails)) {
            return ResponseEntity.status(403).body("Unauthorized access");
        }

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String username = userDetails.getUsername();
        
        Optional<User> user = userRepository.findByEmail(username);
        List<Party> parties = partiesService.getPartiesByCreator(user.get().getId());
        
        // Placeholder for fetching and returning user-specific parties
        return ResponseEntity.ok("Here are your parties, " + username+"\n"+parties);
    }
    
    
    // Endpoint to add a new party
    @PostMapping("/parties/add")
    public ResponseEntity<?> addParty(@RequestBody Party party) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Check if authentication is valid
        if (authentication == null || !(authentication.getPrincipal() instanceof UserDetails)) {
            return ResponseEntity.status(403).body("Unauthorized access");
        }

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String username = userDetails.getUsername();

        Optional<User> user = userRepository.findByEmail(username);
        if(!user.isEmpty()) {
        	System.out.println("FOUND USER: "+user.get().getId());
        	System.out.println(" createdBy: "+party.getCreatedBy());

        }

        if (user.isPresent() && user.get().getId() == party.getCreatedBy()) {
            Party createdParty = partiesService.addParty(party);
            return ResponseEntity.ok(createdParty);
        } else {
            return ResponseEntity.status(403).body("Error adding party: User ID doesn't match created_by");
        }
    }



    
 // New endpoint to add multiple parties
    @PostMapping("/parties/add/bulk")
    public ResponseEntity<?> addMultipleParties(@RequestBody List<Party> parties) {
    	 Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
         if (authentication == null || !(authentication.getPrincipal() instanceof UserDetails)) {
             return ResponseEntity.status(403).body("Unauthorized access");
         }

         UserDetails userDetails = (UserDetails) authentication.getPrincipal();
         String username = userDetails.getUsername();
         
         Optional<User> user = userRepository.findByEmail(username);
         
         List<Party> userParties = user.map(u -> 
         parties.stream()
                .filter(i -> i.getCreatedBy() == u.getId())
                .collect(Collectors.toList()))
        		.orElse(new ArrayList<>());
        List<Party> createdParties = partiesService.addMultipleParties(userParties);
        return ResponseEntity.ok(createdParties);
    }
    
    @DeleteMapping("/parties/delete/{id}")
    public ResponseEntity<String> deleteParty(@PathVariable Long id) {
    	Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserDetails)) {
            return ResponseEntity.status(403).body("Unauthorized access");
        }

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String username = userDetails.getUsername();
        
        Optional<User> user = userRepository.findByEmail(username);
        try {
            Optional<Party> party = partiesService.getPartyById(id);
            if(party.isPresent()) {
            	if(user.isPresent() && user.get().getId() == party.get().getCreatedBy()) {
                    partiesService.deletePartyById(id);
                    return ResponseEntity.ok("Party with ID " + id + " has been deleted successfully.");
            	}else {
            		return ResponseEntity.status(HttpStatus.PRECONDITION_FAILED).body("User Id doesnt match Partys created user");
            	}
            }else {
            	throw new PartyNotFoundException(id.toString());
            }
        } catch (PartyNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Party with ID " + id + " not found.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while deleting the party.");
        }
    }
    
   
    @GetMapping("/parties/find/{id}")
    public ResponseEntity<?> findParty(@PathVariable Long id) {
    	Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserDetails)) {
            return ResponseEntity.status(403).body("Unauthorized access");
        }

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String username = userDetails.getUsername();
        
        Optional<User> user = userRepository.findByEmail(username);
        try {
            Optional<Party> party = partiesService.getPartyById(id);
            if(party.isPresent()) {
            	if(user.isPresent() && user.get().getId() == party.get().getId()) {
                    return ResponseEntity.ok(party);
            	}else {
            		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not allowed to view party "+party.get().getId());
            	}
            }else {
            	throw new PartyNotFoundException(id.toString());
            }
        } catch (PartyNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Party with ID " + id + " not found.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while finding the party.\n"+ e.getMessage()+"\n"+e.getStackTrace());
        }
    }
    
}
