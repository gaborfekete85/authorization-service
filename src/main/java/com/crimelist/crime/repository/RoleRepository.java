package com.crimelist.crime.repository;

import java.util.Optional;

import com.crimelist.crime.model.ERole;
import com.crimelist.crime.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface RoleRepository extends JpaRepository<Role, Integer> {
    Role findByName(ERole name);
}