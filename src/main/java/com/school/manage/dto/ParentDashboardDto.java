package com.school.manage.dto;

import lombok.Data;

import java.util.List;

@Data
public class ParentDashboardDto {

    private String parentName;
    private String parentEmail;
    private List<ChildOverviewDto> children;
}
