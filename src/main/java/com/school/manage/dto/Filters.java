package com.school.manage.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class Filters {
    private List<String> classes;
    private List<String> paymentModes;
}
