package com.primesys.adminserviceserver.dtos.issue;

import jakarta.validation.constraints.NotBlank;

public record CheckIssuePickableDTO(@NotBlank(message = "Note Id is required") String noteId
// @NotBlank(message = "User Id is required")
// String userId
) {
}
