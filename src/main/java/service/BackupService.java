package service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import model.VaultEntry;
import util.EncryptionUtil;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public final class BackupService {

    private static final String BACKUP_HEADER = "PMX-BACKUP-1";
    private static final String BACKUP_FORMAT = "pmx.encrypted.v1";

    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .create();

    private BackupService() {
    }

    public static int exportBackup(File targetFile, Iterable<VaultEntry> entries, byte[] dataKey) {
        validateDataKey(dataKey);

        try {
            BackupData data = new BackupData();
            data.format = BACKUP_FORMAT;
            data.exportedAt = LocalDateTime.now().toString();
            data.entries = new ArrayList<>();

            for (VaultEntry entry : entries) {
                entry.ensureMetadata();
                data.entries.add(entry);
            }

            String json = GSON.toJson(data);
            String encryptedBackup = EncryptionUtil.encrypt(json, dataKey);
            String fileContent = BACKUP_HEADER + System.lineSeparator() + encryptedBackup;

            Files.writeString(targetFile.toPath(), fileContent, StandardCharsets.UTF_8);
            return data.entries.size();
        } catch (Exception ex) {
            throw new RuntimeException("Backup konnte nicht exportiert werden.", ex);
        }
    }

    public static List<VaultEntry> importBackup(File sourceFile, byte[] dataKey) {
        validateDataKey(dataKey);

        try {
            String fileContent = Files.readString(sourceFile.toPath(), StandardCharsets.UTF_8);
            String[] parts = fileContent.split("\\R", 2);

            if (parts.length != 2 || !BACKUP_HEADER.equals(parts[0].trim())) {
                throw new IllegalArgumentException("Ungueltiges PMX Backup Format.");
            }

            String encryptedBackup = parts[1].trim();
            String json = EncryptionUtil.decrypt(encryptedBackup, dataKey);
            BackupData data = GSON.fromJson(json, BackupData.class);

            if (data == null || !BACKUP_FORMAT.equals(data.format) || data.entries == null) {
                throw new IllegalArgumentException("Backup Daten konnten nicht gelesen werden.");
            }

            for (VaultEntry entry : data.entries) {
                entry.ensureMetadata();
            }

            return data.entries;
        } catch (Exception ex) {
            throw new RuntimeException("Backup konnte nicht importiert werden.", ex);
        }
    }

    private static void validateDataKey(byte[] dataKey) {
        if (dataKey == null || dataKey.length == 0) {
            throw new IllegalStateException("Kein Daten Schluessel vorhanden.");
        }
    }

    private static class BackupData {
        private String format;
        private String exportedAt;
        private List<VaultEntry> entries;
    }
}
