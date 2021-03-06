package com.t09.jibao.dao;

import com.t09.jibao.domain.Administrator;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdministratorDAO extends JpaRepository<Administrator, Long> {
    public Administrator findFirstByEmail(String email);
}
