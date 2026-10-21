package com.vethd.backend.mapper;

import com.vethd.backend.DTOs.request.DiagnosisRequest;
import com.vethd.backend.model.Appointment;
import com.vethd.backend.model.Diagnosis;

public class DiagnosisMapper {
    private DiagnosisMapper() {
    }

    public static Diagnosis toEntity(DiagnosisRequest request, Appointment appointment) {
        Diagnosis diagnosis = new Diagnosis();
        diagnosis.setAppointment(appointment);
        diagnosis.setDiagnosis(request.diagnosis());
        diagnosis.setDetails(request.details());
        diagnosis.setTreatment(request.treatment());
        diagnosis.setNotes(request.notes());
        diagnosis.setNextSuggestedAppointment(request.nextSuggestedAppointment());
        return diagnosis;
    }
}