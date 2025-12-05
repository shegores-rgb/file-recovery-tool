import java.util.Scanner;
import java.io.File;

public class Main {
    // Папка для файлов без расширений
    private static final String FILES_FOLDER = "files_to_recover";

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        FileRecovery recovery = new FileRecovery();


        createFilesFolder();

        while (true) {
            System.out.println("\n=== ВОССТАНОВЛЕНИЕ РАСШИРЕНИЙ ФАЙЛОВ ===");
            System.out.println(" Исходные файлы: " + FILES_FOLDER);
            System.out.println(" Восстановленные: recovered_files");
            System.out.println("1.  Показать файлы для восстановления");
            System.out.println("2.  Восстановить ОДИН файл");
            System.out.println("3.  Восстановить ВСЕ файлы");
            System.out.println("4.  Показать восстановленные файлы");
            System.out.println("5.  Поддерживаемые форматы");
            System.out.println("6.  Выход");
            System.out.print("Выберите действие: ");

            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    showFilesInFolder(recovery);
                    break;
                case "2":
                    recoverSingleFile(scanner, recovery);
                    break;
                case "3":
                    recoverAllFiles(recovery);
                    break;
                case "4":
                    recovery.showRecoveredFiles();
                    break;
                case "5":
                    showSupportedFormats(recovery);
                    break;
                case "6":
                    System.out.println("До свидания!");
                    return;
                default:
                    System.out.println("Неверный выбор");
            }
        }
    }

    private static void createFilesFolder() {
        File folder = new File(FILES_FOLDER);
        if (!folder.exists()) {
            folder.mkdir();
            System.out.println("Создана папка: " + FILES_FOLDER);
            System.out.println("Киньте файлы БЕЗ расширений в эту папку");
        }
    }

    private static void showFilesInFolder(FileRecovery recovery) {
        recovery.showFilesInFolder(FILES_FOLDER);
    }

    private static void recoverSingleFile(Scanner scanner, FileRecovery recovery) {
        File[] files = recovery.showFilesInFolder(FILES_FOLDER);
        if (files == null) return;

        System.out.print("\nВыберите файл для восстановления (номер): ");
        try {
            int choice = Integer.parseInt(scanner.nextLine());
            recovery.recoverSelectedFile(files, choice);
        } catch (NumberFormatException e) {
            System.out.println("Введите число");
        }
    }

    private static void recoverAllFiles(FileRecovery recovery) {
        System.out.println(" Запуск восстановления всех файлов");
        recovery.recoverAllFiles(FILES_FOLDER);
    }

    private static void showSupportedFormats(FileRecovery recovery) {
        System.out.println("\n Поддерживаемые форматы:");


        FileAnalyzer analyzer = new FileAnalyzer();
        for (FileType type : analyzer.getSupportedFileTypes()) {
            System.out.println(" • " + type.getDescription() + " (." + type.getExtension() + ")");
        }
    }
}