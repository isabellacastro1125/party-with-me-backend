package com.icas.party_with_me.data;

import org.springframework.boot.origin.SystemEnvironmentOrigin;
import org.springframework.stereotype.Service;

import com.icas.party_with_me.data.DAO.Invitation;
import com.icas.party_with_me.data.DAO.Item;
import com.icas.party_with_me.data.DAO.Party;
import com.icas.party_with_me.data.DAO.User;
import com.icas.party_with_me.exception.PartyNotFoundException;

import java.time.LocalDate;
import java.util.List;
import java.util.ArrayList;
import java.util.Optional;

@Service
public class PartiesService {

    private final PartiesRepository partiesRepository;
    private final ItemRepository itemRepository;

    public PartiesService(PartiesRepository partiesRepository, ItemRepository itemRepository) {
        this.partiesRepository = partiesRepository;
        this.itemRepository = itemRepository;
    }

    public List<Party> getPartiesByCreator(User createdBy) {
        return partiesRepository.findByCreatedBy(createdBy);
    }

    public List<Party> getPartiesByDate(LocalDate date) {
        return partiesRepository.findByDate(date);
    }

    public Party addParty(Party party) {
        System.out.println("HIT addParty");

        // Ensure all invitations are linked to the party
        party.getInvitations().forEach(invitation -> invitation.setParty(party));

        // Save the party and invitations first
        Party savedParty = partiesRepository.save(party);

        // Prepare and save items manually
        List<Item> itemsToSave = new ArrayList<>();
        party.getItems().forEach(item -> {
            item.setParty(savedParty);
            System.out.println("ITEM " + item.getItemName()+" "+item.getBroughtBy().getId() );

            // Resolve broughtBy (if applicable)
            if (item.getBroughtBy() != null) {
            	System.out.println("BroughtBy: "+item.getBroughtBy());
                String guestPhone = item.getBroughtBy().getGuestPhone();
                for(Invitation in: savedParty.getInvitations()) {
                	System.out.println(in.getGuestName()+ " "+in.getGuestPhone());
                }
                Optional<Invitation> matchingInvitation = savedParty.getInvitations().stream()
                    .filter(invitation -> invitation.getGuestPhone().equals(guestPhone))
                    .findFirst();

                if (matchingInvitation.isPresent()) {
                    item.setBroughtBy(matchingInvitation.get());
                } else {
                    throw new IllegalArgumentException(
                        "No invitation found for guest phone: " + guestPhone
                    );
                }
            }

            // Add item to the save list
            itemsToSave.add(item);
        });

        // Save items using ItemRepository
        itemRepository.saveAll(itemsToSave);

        // Update the party with saved items (if needed for return value consistency)
        savedParty.setItems(itemsToSave);

        return savedParty;
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
	
	public Party updateParty(Long id, Party updatedParty) throws PartyNotFoundException {
	    // Check if the party exists
	    Party existingParty = partiesRepository.findById(id)
	        .orElseThrow(() -> new PartyNotFoundException("Party not found for ID: " + id));
	    
	    // Update fields of the existing party with new data
	    existingParty.setTitle(updatedParty.getTitle());
	    existingParty.setLocation(updatedParty.getLocation());
	    existingParty.setDate(updatedParty.getDate());
	    existingParty.setTheme(updatedParty.getTheme());
	    existingParty.setCreatedBy(updatedParty.getCreatedBy());
	    // Add any other fields that need updating

	    // Save and return the updated party
	    return partiesRepository.save(existingParty);
	}

}