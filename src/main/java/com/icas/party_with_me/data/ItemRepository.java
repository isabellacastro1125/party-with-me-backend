package com.icas.party_with_me.data;

import org.springframework.data.jpa.repository.JpaRepository;
import com.icas.party_with_me.data.DAO.Item;

public interface ItemRepository extends JpaRepository<Item, Long> {
}

