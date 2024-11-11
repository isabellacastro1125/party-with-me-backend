package com.icas.party_with_me.data;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.icas.party_with_me.data.DAO.Party;

@Repository
public interface PartiesRepository extends JpaRepository<Party, Long> {
	
	boolean existsById(Long id);
	
    void deleteById(Long id);
    
	 // Custom method to find parties by creator's identifier
    List<Party> findByCreatedBy(Long createdBy);
    
    Optional<Party> findById(Long id);

	List<Party> findByDate(LocalDate date);
	
	
    
}
