package com.vethd.backend.mapper;

import com.vethd.backend.DTOs.request.AvailabilityScheduleRequest;
import com.vethd.backend.DTOs.response.AvailabilityScheduleResponse;
import com.vethd.backend.model.AvailabilitySchedule;
import com.vethd.backend.model.Veterinarian;

public class AvailabilityScheduleMapper {

    private AvailabilityScheduleMapper() {

    }

    public static AvailabilitySchedule toEntity(AvailabilityScheduleRequest request,
                                                Veterinarian veterinarian) {
        AvailabilitySchedule schedule = new AvailabilitySchedule();
        schedule.setVeterinarian(veterinarian);
        schedule.setDayOfWeek(request.dayOfWeek());
        schedule.setStartTime(request.startTime());
        schedule.setEndTime(request.endTime());
        return schedule;
    }

    public static void updateEntity(AvailabilityScheduleRequest request,
                                    AvailabilitySchedule schedule) {
        schedule.setDayOfWeek(request.dayOfWeek());
        schedule.setStartTime(request.startTime());
        schedule.setEndTime(request.endTime());
    }

    public static AvailabilityScheduleResponse toResponse(AvailabilitySchedule schedule) {
        return AvailabilityScheduleResponse.builder()
                .id(schedule.getId())
                .veterinarianId(schedule.getVeterinarian().getId())
                .dayOfWeek(schedule.getDayOfWeek())
                .startTime(schedule.getStartTime())
                .endTime(schedule.getEndTime())
                .build();
    }
}
