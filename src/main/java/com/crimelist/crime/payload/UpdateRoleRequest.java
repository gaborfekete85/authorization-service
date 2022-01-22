package com.crimelist.crime.payload;

import com.crimelist.crime.model.ERole;

import java.util.UUID;

/**
 * Created by rajeevkumarsingh on 02/08/17.
 */

public class UpdateRoleRequest {
    private UUID userId;
    private ERole role;

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public ERole getRole() {
        return role;
    }

    public void setRole(ERole role) {
        this.role = role;
    }
}
