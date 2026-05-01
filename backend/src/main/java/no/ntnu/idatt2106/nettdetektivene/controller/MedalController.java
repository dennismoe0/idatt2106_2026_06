package no.ntnu.idatt2106.nettdetektivene.controller;

import lombok.RequiredArgsConstructor;
import no.ntnu.idatt2106.nettdetektivene.dto.game.EarnedMedalDto;
import no.ntnu.idatt2106.nettdetektivene.service.MedalService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Handles medal retrieval for students, including all defined medals and the student's earned medals.
 */
@RestController
@RequestMapping("/api/medals")
@RequiredArgsConstructor
public class MedalController {

    private static final Logger log = LoggerFactory.getLogger(MedalController.class);

    private final MedalService medalService;

    /**
     * Returns all medals in the system with their earned status for the authenticated student.
     *
     * @param userDetails the authenticated student
     * @return a list of {@link EarnedMedalDto} representing every medal and whether the student has earned each one
     */
    @GetMapping("/all")
    @PreAuthorize("hasRole('STUDENT')")
    public List<EarnedMedalDto> getAllMedals(@AuthenticationPrincipal UserDetails userDetails) {
        Long studentId = Long.parseLong(userDetails.getUsername());
        log.info("[MedalController] GET /medals/all studentId={}", studentId);
        return medalService.getAllMedals(studentId);
    }

    /**
     * Returns only the medals that the authenticated student has earned.
     *
     * @param userDetails the authenticated student
     * @return a list of {@link EarnedMedalDto} for medals the student has earned
     */
    @GetMapping("/mine")
    @PreAuthorize("hasRole('STUDENT')")
    public List<EarnedMedalDto> getMyMedals(@AuthenticationPrincipal UserDetails userDetails) {
        Long studentId = Long.parseLong(userDetails.getUsername());
        log.info("[MedalController] GET /medals/mine studentId={}", studentId);
        return medalService.getEarnedMedals(studentId);
    }
}
