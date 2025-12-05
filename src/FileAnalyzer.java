import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.io.File;

/**
 * Класс для анализа файлов и определения их типа
 */
public class FileAnalyzer {
    private List<FileType> fileTypes;

    public FileAnalyzer() {
        this.fileTypes = new ArrayList<>();
        initializeFileTypes();
    }

    /**
     * Инициализация базы данных типов файлов с магическими числами
     */
    private void initializeFileTypes() {

        //  DATABASE - Microsoft Access форматы
        addType("mdb", "application/x-msaccess", "Microsoft Access Database", "00 01 00 00 53 74 61 6E 64 61 72 64 20 4A 65 74");
        addType("accdb", "application/x-msaccess", "Microsoft Access 2007+ Database", "00 01 00 00 53 74 61 6E 64 61 72 64 20 41 43 45");

        //  Изображения
        addType("jpg", "image/jpeg", "JPEG Image", "FF D8 FF");
        addType("png", "image/png", "PNG Image", "89 50 4E 47 0D 0A 1A 0A");
        addType("gif", "image/gif", "GIF Image", "47 49 46 38");
        addType("bmp", "image/bmp", "Bitmap Image", "42 4D");

        //  Документы
        addType("pdf", "application/pdf", "PDF Document", "25 50 44 46");

        //  Office - новые форматы
        addType("docx", "application/docx", "Word Document", "50 4B 03 04");
        addType("xlsx", "application/xlsx", "Excel Spreadsheet", "50 4B 03 04");
        addType("pptx", "application/pptx", "PowerPoint Presentation", "50 4B 03 04");

        //  Office - старые форматы
        addType("doc", "application/msword", "Word Document", "D0 CF 11 E0 A1 B1 1A E1");
        addType("xls", "application/vnd.ms-excel", "Excel Spreadsheet", "D0 CF 11 E0 A1 B1 1A E1");
        addType("ppt", "application/vnd.ms-powerpoint", "PowerPoint Presentation", "D0 CF 11 E0 A1 B1 1A E1");

        //  Архивы
        addType("zip", "application/zip", "ZIP Archive", "50 4B 03 04");
        addType("rar", "application/vnd.rar", "RAR Archive", "52 61 72 21 1A 07 00"); // RAR4
        addType("rar", "application/vnd.rar", "RAR Archive v5", "52 61 72 21 1A 07 01 00"); // RAR5
        addType("7z", "application/x-7z-compressed", "7-Zip Archive", "37 7A BC AF 27 1C");

        //  Исполняемые файлы
        addType("exe", "application/x-msdownload", "Windows Executable", "4D 5A");

        // Видео
        addType("mp4", "video/mp4", "MPEG-4 Video", "66 74 79 70", 4);
        addType("avi", "video/x-msvideo", "AVI Video", "52 49 46 46");

        //  Аудио
        addType("mp3", "audio/mpeg", "MP3 Audio", "FF FB"); // MPEG Layer 3
        addType("mp3", "audio/mpeg", "MP3 Audio", "FF F3"); // MPEG Layer 3
        addType("mp3", "audio/mpeg", "MP3 Audio", "FF F2"); // MPEG Layer 3
        addType("mp3", "audio/mpeg", "MP3 Audio", "49 44 33"); // ID3v2 tag
        addType("wav", "audio/wav", "WAV Audio", "52 49 46 46"); // RIFF
        addType("wav", "audio/wav", "WAV Audio", "57 41 56 45", 8); // WAVE at offset 8

        //Текстовые файлы
        addType("txt", "text/plain", "Plain Text", null);
    }

    private void addType(String ext, String mime, String desc, String hex) {
        addType(ext, mime, desc, hex, 0);
    }

    private void addType(String ext, String mime, String desc, String hex, int offset) {
        byte[] magic = (hex != null) ? hexToBytes(hex) : null;
        fileTypes.add(new FileType(ext, mime, desc, magic, offset));
    }

    private byte[] hexToBytes(String hex) {
        try {
            String[] parts = hex.split(" ");
            byte[] bytes = new byte[parts.length];
            for (int i = 0; i < parts.length; i++) {
                bytes[i] = (byte) Integer.parseInt(parts[i], 16);
            }
            return bytes;
        } catch (Exception e) {
            System.err.println("Ошибка в hex строке: " + hex);
            return null;
        }
    }

    /**
     * УМНАЯ ПРОВЕРКА OFFICE ФАЙЛОВ
     */
    private FileType checkOfficeFormat(byte[] buffer, int bytesRead) {
        if (bytesRead < 100) return null;

        // Проверяем новые форматы Office (ZIP-based)
        if (buffer[0] == 0x50 && buffer[1] == 0x4B && buffer[2] == 0x03 && buffer[3] == 0x04) {
            try {
                String content = new String(buffer, 0, Math.min(2000, bytesRead));

                if (content.contains("word/") || content.contains("document.xml")) {
                    System.out.println(" Определен: Word Document");
                    return getFileTypeByExtension("docx");
                }
                else if (content.contains("xl/") || content.contains("worksheets/")) {
                    System.out.println(" Определен: Excel Spreadsheet");
                    return getFileTypeByExtension("xlsx");
                }
                else if (content.contains("ppt/") || content.contains("presentation.xml")) {
                    System.out.println(" Определен: PowerPoint Presentation");
                    return getFileTypeByExtension("pptx");
                }
            } catch (Exception e) {
                // Продолжаем проверку другими методами
            }
        }

        // Проверяем старые форматы Office (OLE-based)
        if (buffer[0] == (byte)0xD0 && buffer[1] == (byte)0xCF &&
                buffer[2] == 0x11 && buffer[3] == (byte)0xE0) {

            try {
                String content = new String(buffer, 0, Math.min(5000, bytesRead));

                if (content.contains("WordDocument")) {
                    System.out.println(" Определен: Word Document (old)");
                    return getFileTypeByExtension("doc");
                }
                else if (content.contains("Workbook") || content.contains("Excel")) {
                    System.out.println(" Определен: Excel Spreadsheet (old)");
                    return getFileTypeByExtension("xls");
                }
                else if (content.contains("PowerPoint") || content.contains("Presentation")) {
                    System.out.println(" Определен: PowerPoint Presentation (old)");
                    return getFileTypeByExtension("ppt");
                }
            } catch (Exception e) {
                // Продолжаем проверку
            }
        }

        // Проверяем форматы Microsoft Access
        // MDB формат (Jet Database Engine)
        if (bytesRead >= 16) {
            // Проверка сигнатуры MDB: "Standard Jet"
            byte[] mdbSignature = {
                    0x00, 0x01, 0x00, 0x00,
                    0x53, 0x74, 0x61, 0x6E,  // S t a n
                    0x64, 0x61, 0x72, 0x64,  // d a r d
                    0x20, 0x4A, 0x65, 0x74   //   J e t
            };

            boolean isMdb = true;
            for (int i = 0; i < mdbSignature.length; i++) {
                if (buffer[i] != mdbSignature[i]) {
                    isMdb = false;
                    break;
                }
            }

            if (isMdb) {
                System.out.println(" Определен: Microsoft Access Database (MDB)");
                return getFileTypeByExtension("mdb");
            }

            // Проверка сигнатуры ACCDB: "Standard ACE"
            if (bytesRead >= 16) {
                byte[] accdbSignature = {
                        0x00, 0x01, 0x00, 0x00,
                        0x53, 0x74, 0x61, 0x6E,  // S t a n
                        0x64, 0x61, 0x72, 0x64,  // d a r d
                        0x20, 0x41, 0x43, 0x45   //   A C E
                };

                boolean isAccdb = true;
                for (int i = 0; i < accdbSignature.length; i++) {
                    if (buffer[i] != accdbSignature[i]) {
                        isAccdb = false;
                        break;
                    }
                }

                if (isAccdb) {
                    System.out.println(" Определен: Microsoft Access Database (ACCDB)");
                    return getFileTypeByExtension("accdb");
                }
            }
        }

        return null;
    }

    /**
     * Определяет тип файла по его содержимому
     */
    public FileType identifyFileType(String filePath) {
        try (FileInputStream fis = new FileInputStream(filePath)) {
            byte[] buffer = new byte[8192];
            int bytesRead = fis.read(buffer);

            if (bytesRead == -1) {
                System.err.println(" Файл пустой");
                return null;
            }

            System.out.println("\n Анализ файла: " + new File(filePath).getName());
            System.out.print(" Сигнатура: ");
            for (int i = 0; i < Math.min(16, bytesRead); i++) {
                System.out.printf("%02X ", buffer[i]);
            }
            System.out.println();

            // 1. Проверяем Office и Access форматы
            FileType officeType = checkOfficeFormat(buffer, bytesRead);
            if (officeType != null) return officeType;

            // 3. Проверяем по магическим числам
            for (FileType type : fileTypes) {
                byte[] magic = type.getMagicNumbers();
                if (magic != null && bytesRead >= magic.length + type.getOffset()) {
                    boolean match = true;
                    for (int i = 0; i < magic.length; i++) {
                        if (buffer[i + type.getOffset()] != magic[i]) {
                            match = false;
                            break;
                        }
                    }
                    if (match) {
                        System.out.println(" Определен: " + type.getDescription());
                        return type;
                    }
                }
            }

            // 4. Проверяем текстовые файлы
            if (isTextFile(buffer, bytesRead)) {
                System.out.println(" Определен: Text File");
                return getFileTypeByExtension("txt");
            }

            System.out.println(" Тип не определен");
            return null;

        } catch (IOException e) {
            System.err.println(" Ошибка чтения: " + e.getMessage());
            return null;
        }
    }

    /**
     * Проверяет, является ли файл текстовым
     */
    private boolean isTextFile(byte[] buffer, int length) {
        if (length == 0) return false;

        int textChars = 0;
        int totalChecked = Math.min(length, 1000);

        for (int i = 0; i < totalChecked; i++) {
            byte b = buffer[i];
            if ((b >= 0x20 && b <= 0x7E) ||  // ASCII
                    b == 0x09 || b == 0x0A || b == 0x0D ||  // Tab, LF, CR
                    (b & 0xE0) == 0xC0 ||  // UTF-8 начало
                    (b & 0xC0) == 0x80) {  // UTF-8 продолжение
                textChars++;
            } else if (b == 0x00 && i > 0) {
                return false; // Бинарный файл
            }
        }

        double ratio = (double) textChars / totalChecked;
        System.out.printf(" Текстовых символов: %.1f%%\n", ratio * 100);
        return ratio > 0.80;
    }

    /**
     * Получает FileType по расширению
     */
    public FileType getFileTypeByExtension(String extension) {
        for (FileType fileType : fileTypes) {
            if (fileType.getExtension().equalsIgnoreCase(extension)) {
                return fileType;
            }
        }
        return null;
    }

    /**
     * Возвращает список всех поддерживаемых типов файлов
     */
    public List<FileType> getSupportedFileTypes() {
        return new ArrayList<>(fileTypes);
    }
}