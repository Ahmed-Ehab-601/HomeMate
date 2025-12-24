package com.homemate.analysis.service;

import com.homemate.analysis.dao.TaskerAnalysisDao;
import com.homemate.analysis.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TaskerAnalysisService {
    private final TaskerAnalysisDao dao;

    public GenderCountResponse getGenderCount() {
        return dao.fetchGenderCounts();
    }

    public AgeBucketsResponse getAgeBuckets() {
        return dao.fetchAgeBuckets();
    }

    public NewAccountsResponse getNewAccountsCounts(NewAccountsRequest request) {
        return dao.fetchNewAccountsCounts(request);
    }

    public RatingRangesResponse getRatingRanges() {
        return dao.fetchRatingRanges();
    }

    public HourRateRangesResponse getHourRateRanges() {
        return dao.fetchHourRateRanges();
    }

    public WorkedHoursRangesResponse getWorkedHoursRanges() {
        return dao.fetchWorkedHoursRanges();
    }

    public ServiceCountResponse getServiceCounts(ServiceCountRequest request) {
        return dao.fetchServiceCounts(request);
    }

    public CityCountResponse getCityCounts(CityCountRequest request) {
        return dao.fetchCityCounts(request);
    }

    public TaskerStatusCountsResponse getStatusCounts() {
        return dao.fetchStatusCounts();
    }
}
