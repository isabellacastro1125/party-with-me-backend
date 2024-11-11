package com.icas.party_with_me.data;

import org.springframework.stereotype.Service;

import com.icas.party_with_me.data.DAO.Party;
import com.icas.party_with_me.exception.PartyNotFoundException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class PartiesService {

    private final PartiesRepository partiesRepository;

    public PartiesService(PartiesRepository partiesRepository) {
        this.partiesRepository = partiesRepository;
    }

    public List<Party> getPartiesByCreator(Long createdBy) {
        return partiesRepository.findByCreatedBy(createdBy);
    }

    public List<Party> getPartiesByDate(LocalDate date) {
        return partiesRepository.findByDate(date);
    }

    public Party addParty(Party party) {
        return partiesRepository.save(party);
    }
    
    public Optional<Party> getPartyById(Long id) throws PartyNotFoundException{
    	if (!partiesRepository.existsById(id)) {
            throw new PartyNotFoundException("Party not found for ID: " + id);
        }
    	return partiesRepository.findById(id);
    }

    
    public void deletePartyById(Long id) throws PartyNotFoundException {
        if (!partiesRepository.existsById(id)) {
            throw new PartyNotFoundException("Party not found for ID: " + id);
        }
        partiesRepository.deleteById(id);
    }

	public List<Party> addMultipleParties(List<Party> parties) {
		parties.forEach(partiesRepository::save);
		return parties;
	}
}