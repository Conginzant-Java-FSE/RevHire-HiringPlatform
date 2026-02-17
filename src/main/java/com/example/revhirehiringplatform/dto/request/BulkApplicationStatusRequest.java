package com.revhire.dto.request;

import com.revhire.model.Application.ApplicationStatus;
import lombok.Data;
import java.util.List;

@Data
public class BulkApplicationStatusRequest {
    private List<Long> applicationIds;
    private ApplicationStatus status;
    private String comment;
}
