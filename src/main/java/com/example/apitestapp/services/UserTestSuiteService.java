package com.example.apitestapp.services;

import com.example.apitestapp.config.AppSession;
import com.example.apitestapp.models.dto.ApiCleanupRequest;
import com.example.apitestapp.models.entity.User;
import com.example.apitestapp.models.entity.UserTestSuite;
import com.example.apitestapp.repository.UserTestSuiteRepository;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class UserTestSuiteService {
    private final UserTestSuiteRepository repository;

    public UserTestSuiteService() {
        this.repository = new UserTestSuiteRepository();
    }

    public UserTestSuite create(String name, String method, String endpoint, String description) throws SQLException {
        validateRequired(name, "Tên testsuit");
        validateRequired(method, "Method");
        validateRequired(endpoint, "Endpoint");

        User currentUser = AppSession.getInstance().getCurrentUser();
        UserTestSuite suite = new UserTestSuite();
        suite.setUserId(currentUser == null ? null : currentUser.getId());
        suite.setOwnerName(resolveOwnerName());
        suite.setName(name.trim());
        suite.setMethod(normalizeMethod(method));
        suite.setEndpoint(endpoint.trim());
        suite.setDescription(description);
        return repository.save(suite);
    }

    public UserTestSuite update(String id, String name, String method, String endpoint, String description) throws SQLException {
        validateRequired(id, "Testsuit");
        validateRequired(name, "Tên testsuit");
        validateRequired(method, "Method");
        validateRequired(endpoint, "Endpoint");

        UserTestSuite suite = new UserTestSuite();
        suite.setId(id);
        suite.setName(name.trim());
        suite.setMethod(normalizeMethod(method));
        suite.setEndpoint(endpoint.trim());
        suite.setDescription(description);
        return repository.update(suite);
    }

    public void delete(String id) throws SQLException {
        validateRequired(id, "Testsuit");
        repository.softDelete(id);
    }

    public UserTestSuite updateCleanupRequests(String id, List<ApiCleanupRequest> cleanupRequests) throws SQLException {
        validateRequired(id, "Testsuit");
        return repository.updateCleanupRequests(id, cleanupRequests);
    }

    public Optional<UserTestSuite> findById(String id) throws SQLException {
        return repository.findById(id);
    }

    public List<UserTestSuite> findForCurrentUser() throws SQLException {
        User currentUser = AppSession.getInstance().getCurrentUser();
        String userId = currentUser == null ? null : currentUser.getId();
        return repository.findActiveByOwner(userId, resolveOwnerName());
    }

    private String resolveOwnerName() {
        String ownerName = AppSession.getUsername();
        return ownerName == null || ownerName.isBlank() ? "User" : ownerName.trim();
    }

    private void validateRequired(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " không được để trống.");
        }
    }

    private String normalizeMethod(String method) {
        return method == null || method.isBlank() ? "POST" : method.trim().toUpperCase();
    }
}
