package com.unimas.library.service;

import com.unimas.library.entity.Setting;
import com.unimas.library.repository.SettingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Single-row settings access with lazy initialisation of defaults. */
@Service
@Transactional
public class SettingsService {

    private final SettingRepository settingRepository;

    public SettingsService(SettingRepository settingRepository) {
        this.settingRepository = settingRepository;
    }

    public Setting get() {
        return settingRepository.findTopByOrderByIdAsc()
                .orElseGet(() -> settingRepository.save(new Setting()));
    }

    public Setting update(Setting form) {
        Setting s = get();
        s.setLibraryName(form.getLibraryName());
        s.setLibraryAddress(form.getLibraryAddress());
        s.setContactNumber(form.getContactNumber());
        s.setAdminName(form.getAdminName());
        s.setAdminEmail(form.getAdminEmail());
        s.setEmailFrom(form.getEmailFrom());
        s.setEmailNotifications(form.getEmailNotifications() != null && form.getEmailNotifications());
        s.setMaxLoanDays(form.getMaxLoanDays());
        s.setMaxBooksPerMember(form.getMaxBooksPerMember());
        s.setFinePerDay(form.getFinePerDay());
        s.setThemeMode(form.getThemeMode());
        Setting saved = settingRepository.save(s);
        return saved;
    }
}
