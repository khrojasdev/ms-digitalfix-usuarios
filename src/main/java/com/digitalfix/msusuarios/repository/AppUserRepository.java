package com.digitalfix.msusuarios.repository;

import com.digitalfix.msusuarios.domain.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AppUserRepository extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findByAzureOid(String azureOid);
    List<AppUser> findByCompanyId(Long companyId);
}