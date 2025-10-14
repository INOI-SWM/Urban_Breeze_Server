package com.ridingmate.api_server.domain.activity.service;

import com.ridingmate.api_server.domain.auth.security.AuthUser;
import com.ridingmate.api_server.domain.user.entity.TerraUser;
import com.ridingmate.api_server.domain.user.entity.User;
import com.ridingmate.api_server.domain.user.repository.TerraUserRepository;
import com.ridingmate.api_server.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cglib.core.Local;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TerraService {

    private final UserRepository userRepository;
    private final TerraUserRepository terraUserRepository;

    public LocalDate determineActivityStartDate(TerraUser terraUser){
        LocalDate thirtyDaysAgo =  LocalDate.now().minusDays(30);

        if (terraUser.getLastSyncDate() == null) {
            return thirtyDaysAgo;
        } else {
            LocalDate earliestSyncDate = terraUser.getLastSyncDate().toLocalDate();
            return earliestSyncDate.isBefore(thirtyDaysAgo) ? thirtyDaysAgo : earliestSyncDate;
        }
    }

    public List<TerraUser> getTerraUsers(User user){
        return terraUserRepository.findAllByUserAndIsActiveTrueAndDeletedAtIsNull(user);
    }

    @Transactional
    public void updateLastSyncDate(TerraUser terraUser){
        terraUser.updateLastSyncDates();
        terraUserRepository.save(terraUser);
    }
}
