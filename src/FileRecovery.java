import java.io.*;
import java.nio.file.*;

public class FileRecovery {
    private FileAnalyzer analyzer;
    private String outputFolder;

    public FileRecovery() {
        this.analyzer = new FileAnalyzer();
        this.outputFolder = "recovered_files";
        createOutputFolder();
    }

    /**
     * Создаем папку для восстановленных файлов
     */
    private void createOutputFolder() {
        File folder = new File(outputFolder);
        if (!folder.exists()) {
            folder.mkdir();
            System.out.println(" Создана папка для восстановленных файлов: " + outputFolder);
        }
    }

    /**
     * Восстановление одного файла
     */
    public boolean recoverFile(String filePath) {
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                System.out.println(" Файл не существует: " + filePath);
                return false;
            }

            FileType fileType = analyzer.identifyFileType(filePath);
            if (fileType == null) {
                System.out.println(" Не удалось определить тип файла");
                return false;
            }

            // Сохраняем в папку recovered_files
            String newPath = getNewFilePath(file, fileType.getExtension());
            Files.copy(file.toPath(), Paths.get(newPath), StandardCopyOption.REPLACE_EXISTING);

            System.out.println(" Восстановлен: " + new File(newPath).getName());
            System.out.println(" Тип: " + fileType.getDescription());
            System.out.println(" Сохранен в: " + outputFolder);

            return true;

        } catch (Exception e) {
            System.out.println(" Ошибка: " + e.getMessage());
            return false;
        }
    }

    /**
     * Показать файлы в папке и предложить выбор
     */
    public File[] showFilesInFolder(String folderPath) {
        File folder = new File(folderPath);
        if (!folder.exists() || !folder.isDirectory()) {
            System.out.println(" Папка не существует: " + folderPath);
            return null;
        }

        File[] files = folder.listFiles(File::isFile);
        if (files == null || files.length == 0) {
            System.out.println(" В папке нет файлов");
            return null;
        }

        System.out.println("\n Файлы в папке '" + folderPath + "':");
        for (int i = 0; i < files.length; i++) {
            System.out.println((i + 1) + ". " + files[i].getName() +
                    " (" + files[i].length() + " байт)");
        }

        return files;
    }

    /**
     * Восстановить выбранный файл из папки
     */
    public boolean recoverSelectedFile(File[] files, int choice) {
        if (files == null || choice < 1 || choice > files.length) {
            System.out.println(" Неверный выбор");
            return false;
        }

        File selectedFile = files[choice - 1];
        System.out.println("\n Восстановление файла: " + selectedFile.getName());
        return recoverFile(selectedFile.getAbsolutePath());
    }

    /**
     * Восстановить ВСЕ файлы в папке
     */
    public void recoverAllFiles(String folderPath) {
        File folder = new File(folderPath);
        File[] files = folder.listFiles(File::isFile);

        if (files == null || files.length == 0) {
            System.out.println(" В папке нет файлов");
            return;
        }

        System.out.println(" Восстановление " + files.length + " файлов...");
        int successCount = 0;

        for (File file : files) {
            System.out.println("\n--- " + file.getName() + " ---");
            if (recoverFile(file.getAbsolutePath())) {
                successCount++;
            }
        }

        System.out.println("\n Итог: успешно восстановлено " + successCount + " из " + files.length);
        System.out.println(" Все файлы сохранены в папку: " + outputFolder);
    }

    /**
     * Получить путь для сохранения восстановленного файла
     */
    private String getNewFilePath(File originalFile, String extension) {
        String name = originalFile.getName();
        int dotIndex = name.lastIndexOf('.');
        String baseName = (dotIndex > 0) ? name.substring(0, dotIndex) : name;

        // Сохраняем в папку recovered_files
        return outputFolder + File.separator + baseName + "." + extension;
    }

    /**
     * Показать содержимое папки с восстановленными файлами
     */
    public void showRecoveredFiles() {
        File folder = new File(outputFolder);
        File[] files = folder.listFiles(File::isFile);

        if (files == null || files.length == 0) {
            System.out.println(" В папке восстановленных файлов пусто");
            return;
        }

        System.out.println("\n Восстановленные файлы в '" + outputFolder + "':");
        for (int i = 0; i < files.length; i++) {
            System.out.println((i + 1) + ". " + files[i].getName() +
                    " (" + files[i].length() + " байт)");
        }
    }
}