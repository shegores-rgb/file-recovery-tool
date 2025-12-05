import java.util.Arrays;
import java.util.List;

/**
 * Класс для хранения информации о типах файлов
 */
public class FileType {
    private String extension;
    private String mimeType;
    private String description;
    private byte[] magicNumbers;
    private int offset;

    public FileType(String extension, String mimeType, String description,
                    byte[] magicNumbers, int offset) {
        this.extension = extension;
        this.mimeType = mimeType;
        this.description = description;
        this.magicNumbers = magicNumbers;
        this.offset = offset;
    }

    // Геттеры
    public String getExtension() { return extension; }
    public String getMimeType() { return mimeType; }
    public String getDescription() { return description; }
    public byte[] getMagicNumbers() { return magicNumbers; }
    public int getOffset() { return offset; }

    @Override
    public String toString() {
        return String.format("%s (%s) - %s", extension, mimeType, description);
    }
}