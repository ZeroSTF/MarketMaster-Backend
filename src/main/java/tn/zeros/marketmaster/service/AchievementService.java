package tn.zeros.marketmaster.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.zeros.marketmaster.repository.AchievementRepository;
import tn.zeros.marketmaster.repository.UserAchievementRepository;

@Service
@RequiredArgsConstructor
public class AchievementService {
    private final AchievementRepository achievementRepository;
    private final UserAchievementRepository userAchievementRepository;

}
