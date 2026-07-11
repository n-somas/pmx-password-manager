package model;

import org.dizitart.no2.objects.Id;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class VaultEntry {

    private static final int PASSWORD_WARNING_DAYS = 180;
    private static final int PASSWORD_CHECK_DAYS = 90;
    private static final DateTimeFormatter DISPLAY_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    @Id
    private String website;
    private String username;
    private String passwordEncrypted;

    private String createdAt;
    private String updatedAt;
    private String passwordChangedAt;

    public VaultEntry() {
    }

    public VaultEntry(String website, String username, String passwordEncrypted) {
        String now = LocalDateTime.now().toString();
        this.website = website;
        this.username = username;
        this.passwordEncrypted = passwordEncrypted;
        this.createdAt = now;
        this.updatedAt = now;
        this.passwordChangedAt = now;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordEncrypted() {
        return passwordEncrypted;
    }

    public void setPasswordEncrypted(String passwordEncrypted) {
        this.passwordEncrypted = passwordEncrypted;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getPasswordChangedAt() {
        return passwordChangedAt;
    }

    public void setPasswordChangedAt(String passwordChangedAt) {
        this.passwordChangedAt = passwordChangedAt;
    }

    public String getPasswordMasked() {
        return "••••••";
    }

    public void ensureMetadata() {
        String now = LocalDateTime.now().toString();

        if (isBlank(createdAt)) {
            createdAt = now;
        }

        if (isBlank(updatedAt)) {
            updatedAt = createdAt;
        }

        if (isBlank(passwordChangedAt)) {
            passwordChangedAt = updatedAt;
        }
    }

    public void markUpdated(boolean passwordChanged) {
        String now = LocalDateTime.now().toString();
        ensureMetadata();
        updatedAt = now;

        if (passwordChanged) {
            passwordChangedAt = now;
        }
    }

    public String getUpdatedAtDisplay() {
        ensureMetadata();
        return formatDate(updatedAt);
    }

    public String getPasswordChangedAtDisplay() {
        ensureMetadata();
        return formatDate(passwordChangedAt);
    }

    public String getPasswordAgeStatus() {
        long days = getPasswordAgeDays();

        if (days >= PASSWORD_WARNING_DAYS) {
            return "Alt";
        }

        if (days >= PASSWORD_CHECK_DAYS) {
            return "Prüfen";
        }

        return "Aktuell";
    }

    public String getPasswordAgeTooltip() {
        long days = getPasswordAgeDays();
        return "Passwort seit " + days + " Tagen unverändert.";
    }

    public boolean isPasswordOld() {
        return getPasswordAgeDays() >= PASSWORD_WARNING_DAYS;
    }

    public boolean shouldCheckPassword() {
        long days = getPasswordAgeDays();
        return days >= PASSWORD_CHECK_DAYS && days < PASSWORD_WARNING_DAYS;
    }

    private long getPasswordAgeDays() {
        ensureMetadata();

        try {
            LocalDateTime changed = LocalDateTime.parse(passwordChangedAt);
            return Math.max(0, ChronoUnit.DAYS.between(changed, LocalDateTime.now()));
        } catch (Exception ex) {
            return 0;
        }
    }

    private String formatDate(String isoDateTime) {
        if (isBlank(isoDateTime)) {
            return "-";
        }

        try {
            return LocalDateTime.parse(isoDateTime).format(DISPLAY_FORMATTER);
        } catch (Exception ex) {
            return "-";
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
