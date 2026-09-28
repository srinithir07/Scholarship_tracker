package com.scholartrack.dto;

import com.scholartrack.entity.DisbursementStatus;
import jakarta.validation.constraints.NotNull;

public class DisbursementUpdateRequest {

    @NotNull(message = "Disbursement status is required")
    private DisbursementStatus disbursementStatus;

    public DisbursementUpdateRequest() {
    }

    public DisbursementUpdateRequest(DisbursementStatus disbursementStatus) {
        this.disbursementStatus = disbursementStatus;
    }

    public DisbursementStatus getDisbursementStatus() {
        return disbursementStatus;
    }

    public void setDisbursementStatus(DisbursementStatus disbursementStatus) {
        this.disbursementStatus = disbursementStatus;
    }
}
