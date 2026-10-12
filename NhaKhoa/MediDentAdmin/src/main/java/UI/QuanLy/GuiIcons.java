package UI.QuanLy;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;

final class GuiIcons {
    static final String HOME = "home";
    static final String STAFF = "staff";
    static final String DENTAL_SERVICE = "dental-service";
    static final String PATIENT = "patient";
    static final String CALENDAR = "calendar";
    static final String INVOICE = "hoadon";
    static final String REPORT = "chart";
    static final String REVENUE = "revenue";
    static final String PENDING_PAYMENT = "pending";
    static final String CANCEL_REQUEST = "cancel-request";
    static final String TOOTH_LOGO = "tooth-logo";
    static final String SHIELD = "shield";
    static final String BELL = "bell";
    static final String SEARCH = "search";
    static final String ADD = "add";
    static final String CHECK = "check";
    static final String DOWNLOAD = "download";
    static final String LOGOUT = "logout";
    static final String FILTER = "filter";

    private static final Map<String, BufferedImage> SOURCE_CACHE = new HashMap<>();
    private static final Map<String, Icon> ICON_CACHE = new HashMap<>();

    private GuiIcons() { }

    static Icon icon(String name, Color color, int size) {
        String key = name + ":" + color.getRGB() + ":" + size;
        return ICON_CACHE.computeIfAbsent(key, ignored -> {
            BufferedImage source = loadSource(name);
            BufferedImage processed = TOOTH_LOGO.equals(name)
                    ? extractLightGlyph(source)
                    : isPaymentStatusIcon(name) ? extractColoredGlyph(source)
                    : removeTileAndTint(source, color);
            BufferedImage glyph = trimGlyph(processed);
            BufferedImage scaled = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
            Graphics2D graphics = scaled.createGraphics();
            graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            graphics.drawImage(glyph, 0, 0, size, size, null);
            graphics.dispose();
            return new ImageIcon(scaled);
        });
    }

    private static boolean isPaymentStatusIcon(String name) {
        return REVENUE.equals(name) || PENDING_PAYMENT.equals(name) || CANCEL_REQUEST.equals(name);
    }

    private static BufferedImage extractColoredGlyph(BufferedImage source) {
        BufferedImage glyph = new BufferedImage(
                source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < source.getHeight(); y++) {
            for (int x = 0; x < source.getWidth(); x++) {
                int pixel = source.getRGB(x, y);
                int red = (pixel >>> 16) & 0xff;
                int green = (pixel >>> 8) & 0xff;
                int blue = pixel & 0xff;
                int saturation = Math.max(red, Math.max(green, blue))
                        - Math.min(red, Math.min(green, blue));
                int alpha = Math.min(255, saturation * 2);
                glyph.setRGB(x, y, (alpha << 24) | (pixel & 0x00ffffff));
            }
        }
        return glyph;
    }

    private static BufferedImage loadSource(String name) {
        return SOURCE_CACHE.computeIfAbsent(name, key -> {
            String filename = TOOTH_LOGO.equals(key) ? "tooth-logo.jpg" : key + ".png";
            try (InputStream resource = GuiIcons.class.getResourceAsStream("/Image/" + filename)) {
                if (resource != null) {
                    BufferedImage image = ImageIO.read(resource);
                    if (image == null) {
                        throw new IllegalStateException("Tệp icon không phải ảnh hợp lệ: " + filename);
                    }
                    return image;
                }
                Path sourceFolder = Path.of("img", filename);
                if (!Files.isRegularFile(sourceFolder)) {
                    sourceFolder = Path.of("SourceCode", "NhaKhoa", "nha_khoa", "img", filename);
                }
                if (!Files.isRegularFile(sourceFolder)) {
                    throw new IllegalStateException("Không tìm thấy icon giao diện: " + filename);
                }
                BufferedImage image = ImageIO.read(sourceFolder.toFile());
                if (image == null) {
                    throw new IllegalStateException("Tệp icon không phải ảnh hợp lệ: " + filename);
                }
                return image;
            } catch (IOException exception) {
                throw new IllegalStateException("Không thể đọc icon giao diện: " + filename, exception);
            }
        });
    }

    private static BufferedImage extractLightGlyph(BufferedImage source) {
        BufferedImage glyph = new BufferedImage(
                source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < source.getHeight(); y++) {
            for (int x = 0; x < source.getWidth(); x++) {
                int pixel = source.getRGB(x, y);
                int red = (pixel >>> 16) & 0xff;
                int green = (pixel >>> 8) & 0xff;
                int blue = pixel & 0xff;
                int luminance = (red * 299 + green * 587 + blue * 114) / 1000;
                int alpha = Math.max(0, Math.min(255, (luminance - 145) * 3));
                glyph.setRGB(x, y, (alpha << 24) | 0x00ffffff);
            }
        }
        return glyph;
    }

    private static BufferedImage trimGlyph(BufferedImage source) {
        int left = source.getWidth();
        int top = source.getHeight();
        int right = -1;
        int bottom = -1;
        for (int y = 0; y < source.getHeight(); y++) {
            for (int x = 0; x < source.getWidth(); x++) {
                if (((source.getRGB(x, y) >>> 24) & 0xff) > 48) {
                    left = Math.min(left, x);
                    top = Math.min(top, y);
                    right = Math.max(right, x);
                    bottom = Math.max(bottom, y);
                }
            }
        }
        if (right < left || bottom < top) {
            throw new IllegalStateException("Icon không có nét vẽ hữu hình.");
        }

        int glyphWidth = right - left + 1;
        int glyphHeight = bottom - top + 1;
        int side = Math.max(glyphWidth, glyphHeight);
        int padding = Math.max(4, side / 12);
        BufferedImage trimmed = new BufferedImage(
                side + padding * 2, side + padding * 2, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = trimmed.createGraphics();
        int offsetX = padding + (side - glyphWidth) / 2 - left;
        int offsetY = padding + (side - glyphHeight) / 2 - top;
        graphics.drawImage(source, offsetX, offsetY, null);
        graphics.dispose();
        return trimmed;
    }

    private static BufferedImage removeTileAndTint(BufferedImage source, Color tint) {
        BufferedImage glyph = new BufferedImage(
                source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < source.getHeight(); y++) {
            for (int x = 0; x < source.getWidth(); x++) {
                int pixel = source.getRGB(x, y);
                int originalAlpha = (pixel >>> 24) & 0xff;
                if (originalAlpha == 0) {
                    continue;
                }
                int red = (pixel >>> 16) & 0xff;
                int green = (pixel >>> 8) & 0xff;
                int blue = pixel & 0xff;
                int luminance = (red * 299 + green * 587 + blue * 114) / 1000;
                int glyphAlpha = Math.max(0, Math.min(255, (170 - luminance) * 5));
                int alpha = originalAlpha * glyphAlpha / 255;
                glyph.setRGB(x, y, (alpha << 24) | (tint.getRGB() & 0x00ffffff));
            }
        }
        return glyph;
    }
}
