package com.vethd.backend.mapper;

import com.vethd.backend.DTOs.request.DiagnosisRequest;
import com.vethd.backend.DTOs.response.DiagnosisResponse;
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
        return diagnosis;
    }

    public static void updateEntity(DiagnosisRequest request, Diagnosis diagnosis) {
        diagnosis.setDiagnosis(request.diagnosis());
        diagnosis.setDetails(request.details());
        diagnosis.setTreatment(request.treatment());
        diagnosis.setNotes(request.notes());
    }

    public static DiagnosisResponse toResponse(Diagnosis diagnosis) {
        return DiagnosisResponse.builder()
                .id(diagnosis.getId())
                .diagnosis(diagnosis.getDiagnosis())
                .details(diagnosis.getDetails())
                .treatment(diagnosis.getTreatment())
                .notes(diagnosis.getNotes())
                .createdAt(diagnosis.getCreatedAt())
                .build();
    }
}
