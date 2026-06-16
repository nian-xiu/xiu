package com.example.ssmshop.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
public class ProductImageStorageService {
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp", "gif");

    private final Path uploadDirectory;

    public ProductImageStorageService(@Value("${app.upload.product-image-dir:uploads/products}") String uploadDirectory) {
        this.uploadDirectory = Paths.get(uploadDirectory).toAbsolutePath().normalize();
    }

    public String store(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null;
        }

        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename() == null ? "" : file.getOriginalFilename());
        String extension = StringUtils.getFilenameExtension(originalFilename);
        if (extension == null || !ALLOWED_EXTENSIONS.contains(extension.toLowerCase(Locale.ROOT))) {
            throw new IllegalArgumentException("封面图仅支持 jpg、jpeg、png、webp、gif 格式");
        }
        String normalizedExtension = extension.toLowerCase(Locale.ROOT);
        validateReadableImage(file, normalizedExtension);

        try {
            Files.createDirectories(uploadDirectory);
            String filename = UUID.randomUUID() + "." + normalizedExtension;
            Path target = uploadDirectory.resolve(filename).normalize();
            if (!target.startsWith(uploadDirectory)) {
                throw new IllegalArgumentException("封面图文件名不合法");
            }
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, target, StandardCopyOption.REPLACE_EXISTING);
            }
            return "/uploads/products/" + filename;
        } catch (IOException ex) {
            throw new IllegalStateException("封面图保存失败，请稍后重试", ex);
        }
    }

    private void validateReadableImage(MultipartFile file, String extension) {
        try (InputStream inputStream = file.getInputStream()) {
            if ("webp".equals(extension)) {
                validateWebpHeader(inputStream);
                return;
            }
            BufferedImage image = ImageIO.read(inputStream);
            if (image == null || image.getWidth() <= 0 || image.getHeight() <= 0) {
                throw new IllegalArgumentException("封面图无法识别为有效图片");
            }
        } catch (IOException ex) {
            throw new IllegalArgumentException("封面图内容读取失败", ex);
        }
    }

    private void validateWebpHeader(InputStream inputStream) throws IOException {
        byte[] header = inputStream.readNBytes(12);
        boolean isWebp = header.length == 12
                && header[0] == 'R' && header[1] == 'I' && header[2] == 'F' && header[3] == 'F'
                && header[8] == 'W' && header[9] == 'E' && header[10] == 'B' && header[11] == 'P';
        if (!isWebp) {
            throw new IllegalArgumentException("Invalid WebP image");
        }
    }
}
