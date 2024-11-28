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
     * Get authenticated user from the SecurityContext.
     */
    private Optional<User> getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserDetails)) {
            return Optional.empty();
        }
        String username = ((UserDetails) authentication.getPrincipal()).getUsername();
        return userRepository.findByEmail(username);
    }

    /**
     * Endpoint for user parties.
     * Requires a valid JWT token to access.
     */
    @GetMapping("/parties")
    public ResponseEntity<?> getParties() {
        
        Optional<User> user = getAuthenticatedUser();
        if(user.isPresent()) {
        	List<Party> parties = partiesService.getPartiesByCreator(user.get());
            
            // Placeholder for fetching and returning user-specific parties
            return ResponseEntity.ok("Here are your parties\n"+parties);
        }else {
        	return ResponseEntity.status(403).body("Unauthorized access");
        }
        
    }
    
    
    // Endpoint to add a new party
    @PostMapping("/parties/add")
    public ResponseEntity<?> addParty(@RequestBody Party party) {
    	System.out.println("HIT /parties/add");
    	  Optional<User> user = getAuthenticatedUser();
          
        if (user.isPresent() ) {
        	System.out.println("USER: "+user.get().getName());
        	System.out.println("PARTY:\n"+party);
        	party.setCreatedBy(user.get());
            Party createdParty = partiesService.addParty(party);
            return ResponseEntity.ok(createdParty);
        } else {
            return ResponseEntity.status(403).body("Error adding party");
        }
    }

    
 // New endpoint to add multiple parties
    @PostMapping("/parties/add/bulk")
    public ResponseEntity<?> addMultipleParties(@RequestBody List<Party> parties) {
  	  	Optional<User> user = getAuthenticatedUser();

         
         List<Party> userParties = user.map(u -> 
         parties.stream()
                .filter(i -> i.getCreatedBy().getId() == u.getId())
                .collect(Collectors.toList()))
        		.orElse(new ArrayList<>());
        List<Party> createdParties = partiesService.addMultipleParties(userParties);
        return ResponseEntity.ok(createdParties);
    }
    
    
    @DeleteMapping("/parties/delete/{id}")
    public ResponseEntity<String> deleteParty(@PathVariable Long id) {
    	Optional<User> user = getAuthenticatedUser();

        try {
            Optional<Party> party = partiesService.getPartyById(id);
            if(party.isPresent()) {
            	if(user.isPresent() && user.get().getId() == party.get().getCreatedBy().getId()) {
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
  	  	Optional<User> user = getAuthenticatedUser();
  	  
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
    
    @GetMapping("/parties/edit/{id}")
    public ResponseEntity<?> editParty(@PathVariable Long id, @RequestBody Party newParty){
  	  	Optional<User> user = getAuthenticatedUser();
  	  	Optional<Party> foundParty = partiesService.getPartyById(id);
        try {
        	if(user.isPresent() && foundParty.isPresent() && user.get().getId() == foundParty.get().getCreatedBy().getId()) {
            	Party party = partiesService.updateParty(id,  newParty);
            	return ResponseEntity.ok(party);
        	}else {
        		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not allowed to edit party "+newParty.getId());
        	}
        	
        } catch (PartyNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Party with ID " + id + " not found. So could not be updated");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while trying to update the party.\n"+ e.getMessage()+"\n"+e.getStackTrace());
        }
    	
    }
    
}
