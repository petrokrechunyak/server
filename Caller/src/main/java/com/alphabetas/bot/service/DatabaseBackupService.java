package com.alphabetas.bot.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;

@Service
@Slf4j
public class DatabaseBackupService {

    @Value("${spring.datasource.url}")
    private String datasourceUrl;

    @Value("${spring.datasource.username}")
    private String datasourceUsername;

    @Value("${spring.datasource.password}")
    private String datasourcePassword;

    @Value("${db.host:localhost}")
    private String dbHost;

    @Value("${db.port:5432}")
    private String dbPort;

    private static final String BACKUP_DIR = "./backups";
    private static final String DB_NAME = "caller";

    public DatabaseBackupService() {
        createBackupDirectory();
    }

    private void createBackupDirectory() {
        File backupDir = new File(BACKUP_DIR);
        if (!backupDir.exists()) {
            if (backupDir.mkdirs()) {
                log.info("Backup directory created: {}", BACKUP_DIR);
            } else {
                log.warn("Failed to create backup directory: {}", BACKUP_DIR);
            }
        }
    }

    /**
     * Creates a database backup and returns the file path
     * @return Path to the backup file
     */
    public String createBackup() {
        try {
            String timestamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
            String backupFileName = String.format("%s/backup_%s.sql", BACKUP_DIR, timestamp);

            log.info("Starting database backup to: {}", backupFileName);

            // Extract database name from URL
            String dbName = extractDatabaseName();

            // Build pg_dump command (plain text format)
            ProcessBuilder pb = new ProcessBuilder(
                    "pg_dump",
                    "-U", datasourceUsername,
                    "-h", dbHost,
                    "-p", dbPort,
                    "-F", "p",
                    "-b",
                    dbName
            );

            // Set password via environment variable
            pb.environment().put("PGPASSWORD", datasourcePassword);
            // DO NOT redirect error stream - we only want stdout in the file

            Process process = pb.start();

            // Write ONLY stdout to file (not stderr/verbose messages)
            try (InputStream is = process.getInputStream();
                 FileOutputStream fos = new FileOutputStream(backupFileName)) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = is.read(buffer)) != -1) {
                    fos.write(buffer, 0, bytesRead);
                }
            }

            // Log stderr messages (verbose output from pg_dump)
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    log.debug("pg_dump: {}", line);
                }
            }

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                log.error("pg_dump failed with exit code: {}", exitCode);
                throw new RuntimeException("Database backup failed with exit code: " + exitCode);
            }

            log.info("Database backup completed successfully: {}", backupFileName);
            return backupFileName;

        } catch (Exception e) {
            log.error("Error creating backup: {}", ExceptionUtils.getStackTrace(e));
            throw new RuntimeException("Failed to create database backup", e);
        }
    }

    /**
     * Restores database from a backup file (plain text SQL format)
     * Filters out incompatible PostgreSQL configuration parameters for version compatibility
     * @param backupFilePath Path to the backup file
     */
    public void restoreBackup(String backupFilePath) {
        File tempFile = null;
        try {
            File backupFile = new File(backupFilePath);
            if (!backupFile.exists()) {
                throw new FileNotFoundException("Backup file not found: " + backupFilePath);
            }

            log.info("Starting database restore from: {}", backupFilePath);

            // Clear whole DB schema first to avoid conflicts (drop all objects)
            clearDatabaseSchema();

            // Create a temporary file with filtered SQL (also adds IF NOT EXISTS where applicable)
            tempFile = File.createTempFile("backup_", ".sql");
            filterBackupFile(backupFile, tempFile);

            String dbName = extractDatabaseName();

            // Use psql for plain text SQL files; stop on first error
            ProcessBuilder pb = new ProcessBuilder(
                    "psql",
                    "-U", datasourceUsername,
                    "-h", dbHost,
                    "-p", dbPort,
                    "-d", dbName,
                    "-v", "ON_ERROR_STOP=1",
                    "-f", tempFile.getAbsolutePath()
            );

            // Set password via environment variable
            pb.environment().put("PGPASSWORD", datasourcePassword);

            Process process = pb.start();

            // Capture stdout
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    log.debug("PSQL stdout: {}", line);
                }
            }

            // Capture stderr
            StringBuilder stderr = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    stderr.append(line).append("\n");
                    log.debug("PSQL stderr: {}", line);
                }
            }

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                String errorMsg = stderr.toString();
                log.error("psql restore failed with exit code: {}\nError output:\n{}", exitCode, errorMsg);
                throw new RuntimeException("Database restore failed with exit code: " + exitCode + "\n" + errorMsg);
            }

            log.info("Database restore completed successfully");

        } catch (Exception e) {
            log.error("Error restoring backup: {}", ExceptionUtils.getStackTrace(e));
            throw new RuntimeException("Failed to restore database", e);
        } finally {
            // Clean up temporary file
            if (tempFile != null && tempFile.exists()) {
                if (tempFile.delete()) {
                    log.debug("Temporary backup file deleted");
                }
            }
        }
    }

    /**
     * Executes a psql command to drop and recreate the public schema, fully clearing DB objects.
     * This is safer than relying on individual DROP statements in the dump and avoids ordering issues.
     */
    private void clearDatabaseSchema() throws IOException, InterruptedException {
        String dbName = extractDatabaseName();
        String cmd = "DROP SCHEMA public CASCADE; CREATE SCHEMA public; GRANT ALL ON SCHEMA public TO public;";

        ProcessBuilder pb = new ProcessBuilder(
                "psql",
                "-U", datasourceUsername,
                "-h", dbHost,
                "-p", dbPort,
                "-d", dbName,
                "-c", cmd
        );
        pb.environment().put("PGPASSWORD", datasourcePassword);
        pb.redirectErrorStream(true);

        Process process = pb.start();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                log.debug("psql-clear: {}", line);
            }
        }

        int exit = process.waitFor();
        if (exit != 0) {
            throw new IOException("Failed to clear database schema, psql exit code: " + exit);
        }
    }

    /**
     * Filters backup file to remove incompatible PostgreSQL configuration parameters
     * Handles version compatibility issues and adds IF NOT EXISTS where reasonable
     */
    private void filterBackupFile(File sourceFile, File targetFile) throws IOException {
        // List of incompatible configuration parameters that might cause issues on older versions
        String[] incompatibleParams = {
                "transaction_timeout",
                "idle_in_transaction_session_timeout",
                "power_cycle_time",
                "log_recovery_conflict_waits",
                "backend_flush_after"
        };

        try (BufferedReader reader = new BufferedReader(new FileReader(sourceFile));
             FileWriter writer = new FileWriter(targetFile)) {

            String line;
            while ((line = reader.readLine()) != null) {
                boolean shouldSkip = false;

                String trimmed = line.trim();

                // Skip SET statements with incompatible parameters
                if (trimmed.startsWith("SET ")) {
                    for (String param : incompatibleParams) {
                        if (line.contains(param)) {
                            log.debug("Skipping incompatible parameter: {}", line);
                            shouldSkip = true;
                            break;
                        }
                    }
                }

                if (shouldSkip) {
                    continue;
                }

                // Add IF NOT EXISTS checks for common CREATE statements to make restore idempotent
                // Avoid double-inserting IF NOT EXISTS
                if (trimmed.startsWith("CREATE TABLE ") && !trimmed.startsWith("CREATE TABLE IF NOT EXISTS")) {
                    line = line.replaceFirst("CREATE TABLE\\s+", "CREATE TABLE IF NOT EXISTS ");
                } else if (trimmed.startsWith("CREATE SEQUENCE ") && !trimmed.startsWith("CREATE SEQUENCE IF NOT EXISTS")) {
                    line = line.replaceFirst("CREATE SEQUENCE\\s+", "CREATE SEQUENCE IF NOT EXISTS ");
                } else if (trimmed.startsWith("CREATE EXTENSION ") && !trimmed.startsWith("CREATE EXTENSION IF NOT EXISTS")) {
                    line = line.replaceFirst("CREATE EXTENSION\\s+", "CREATE EXTENSION IF NOT EXISTS ");
                } else if (trimmed.startsWith("CREATE INDEX ") && !trimmed.startsWith("CREATE INDEX IF NOT EXISTS")) {
                    line = line.replaceFirst("CREATE INDEX\\s+", "CREATE INDEX IF NOT EXISTS ");
                } else if (trimmed.startsWith("CREATE VIEW ") && !trimmed.startsWith("CREATE VIEW IF NOT EXISTS")) {
                    line = line.replaceFirst("CREATE VIEW\\s+", "CREATE VIEW IF NOT EXISTS ");
                }

                writer.write(line);
                writer.write("\n");
            }
        }

        log.info("Backup file filtered for compatibility, size: {} bytes", targetFile.length());
    }

    /**
     * Gets the list of all available backup files
     */
    public File[] getAvailableBackups() {
        File backupDir = new File(BACKUP_DIR);
        File[] backups = backupDir.listFiles((dir, name) -> name.startsWith("backup_") && name.endsWith(".sql"));
        return backups != null ? backups : new File[0];
    }

    /**
     * Deletes an old backup file
     */
    public void deleteBackup(String backupFilePath) {
        try {
            Files.delete(Paths.get(backupFilePath));
            log.info("Backup file deleted: {}", backupFilePath);
        } catch (IOException e) {
            log.error("Error deleting backup file: {}", backupFilePath, e);
        }
    }

    /**
     * Extracts database name from datasource URL
     */
    private String extractDatabaseName() {
        // URL format: jdbc:postgresql://localhost:5432/caller
        try {
            String[] parts = datasourceUrl.split("/");
            return parts[parts.length - 1];
        } catch (Exception e) {
            log.warn("Could not extract database name from URL, using default: {}", DB_NAME);
            return DB_NAME;
        }
    }
}
