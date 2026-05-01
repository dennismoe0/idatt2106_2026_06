package no.ntnu.idatt2106.nettdetektivene.service;

import lombok.RequiredArgsConstructor;
import no.ntnu.idatt2106.nettdetektivene.dto.game.EarnedMedalDto;
import no.ntnu.idatt2106.nettdetektivene.entity.StudentMedal;
import no.ntnu.idatt2106.nettdetektivene.repository.MedalRepository;
import no.ntnu.idatt2106.nettdetektivene.repository.StudentMedalRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Provides medal retrieval for students, including both earned and all available medals.
 */
@Service
@RequiredArgsConstructor
public class MedalService {

    private static final Logger log = LoggerFactory.getLogger(MedalService.class);

    private final StudentMedalRepository studentMedalRepository;
    private final MedalRepository medalRepository;

    /**
     * Returns all medals with their earned status for the given student.
     *
     * @param studentId the student's user ID
     * @return list of {@link EarnedMedalDto} ordered by medal ID, with {@code earnedAt} null for unearned medals
     */
    @Transactional(readOnly = true)
    public List<EarnedMedalDto> getAllMedals(Long studentId) {
        log.info("[MedalService] getAllMedals studentId={}", studentId);
        Map<Long, StudentMedal> earned = studentMedalRepository
            .findByStudent_Id(studentId)
            .stream()
            .collect(Collectors.toMap(sm -> sm.getMedal().getId(), sm -> sm));

        return medalRepository.findAll(Sort.by(Sort.Direction.ASC, "id")).stream()
            .map(medal -> {
                StudentMedal sm = earned.get(medal.getId());
                return new EarnedMedalDto(
                    medal.getId(),
                    medal.getName(),
                    medal.getDescription(),
                    medal.getImageUrl(),
                    sm != null ? sm.getEarnedAt() : null
                );
            })
            .toList();
    }

    /**
     * Returns only the medals that the student has already earned.
     *
     * @param studentId the student's user ID
     * @return list of earned {@link EarnedMedalDto}
     */
    @Transactional(readOnly = true)
    public List<EarnedMedalDto> getEarnedMedals(Long studentId) {
        log.info("[MedalService] getEarnedMedals studentId={}", studentId);
        return studentMedalRepository.findByStudent_Id(studentId)
                .stream()
                .map(sm -> new EarnedMedalDto(
                        sm.getMedal().getId(),
                        sm.getMedal().getName(),
                        sm.getMedal().getDescription(),
                        sm.getMedal().getImageUrl(),
                        sm.getEarnedAt()
                ))
                .toList();
    }
}
