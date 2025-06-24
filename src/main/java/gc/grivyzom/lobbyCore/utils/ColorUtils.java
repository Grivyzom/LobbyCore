package gc.grivyzom.lobbyCore.utils;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ColorUtils {

    // Patrón para colores hexadecimales
    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");

    /**
     * Traduce códigos de color tanto legacy (&) como hexadecimales (&#RRGGBB)
     * @param message El mensaje a traducir
     * @return El mensaje con colores aplicados
     */
    public static String translate(String message) {
        if (message == null) return "";

        // Traducir colores hexadecimales
        Matcher matcher = HEX_PATTERN.matcher(message);
        StringBuffer buffer = new StringBuffer();

        while (matcher.find()) {
            String hexColor = matcher.group(1);
            matcher.appendReplacement(buffer, ChatColor.of("#" + hexColor).toString());
        }
        matcher.appendTail(buffer);

        // Traducir códigos legacy
        return ChatColor.translateAlternateColorCodes('&', buffer.toString());
    }

    /**
     * Traduce una lista de mensajes
     * @param messages Lista de mensajes
     * @return Lista de mensajes con colores aplicados
     */
    public static List<String> translate(List<String> messages) {
        return messages.stream()
                .map(ColorUtils::translate)
                .collect(Collectors.toList());
    }

    /**
     * Envía un mensaje colorido a un jugador
     * @param player El jugador
     * @param message El mensaje
     */
    public static void sendMessage(Player player, String message) {
        player.sendMessage(translate(message));
    }

    /**
     * Envía múltiples mensajes coloridos a un jugador
     * @param player El jugador
     * @param messages Lista de mensajes
     */
    public static void sendMessages(Player player, List<String> messages) {
        messages.forEach(message -> sendMessage(player, message));
    }

    /**
     * Crea un degradado de color entre dos colores hexadecimales
     * @param text El texto
     * @param startColor Color inicial (formato: RRGGBB)
     * @param endColor Color final (formato: RRGGBB)
     * @return El texto con degradado aplicado
     */
    public static String createGradient(String text, String startColor, String endColor) {
        if (text == null || text.isEmpty()) return "";

        StringBuilder result = new StringBuilder();
        int length = text.length();

        for (int i = 0; i < length; i++) {
            char c = text.charAt(i);
            if (c == ' ') {
                result.append(c);
                continue;
            }

            double ratio = (double) i / (length - 1);
            String interpolatedColor = interpolateColor(startColor, endColor, ratio);
            result.append("&#").append(interpolatedColor).append(c);
        }

        return translate(result.toString());
    }

    /**
     * Interpola entre dos colores
     * @param startColor Color inicial
     * @param endColor Color final
     * @param ratio Ratio de interpolación (0.0 - 1.0)
     * @return Color interpolado
     */
    private static String interpolateColor(String startColor, String endColor, double ratio) {
        int startR = Integer.parseInt(startColor.substring(0, 2), 16);
        int startG = Integer.parseInt(startColor.substring(2, 4), 16);
        int startB = Integer.parseInt(startColor.substring(4, 6), 16);

        int endR = Integer.parseInt(endColor.substring(0, 2), 16);
        int endG = Integer.parseInt(endColor.substring(2, 4), 16);
        int endB = Integer.parseInt(endColor.substring(4, 6), 16);

        int r = (int) (startR + (endR - startR) * ratio);
        int g = (int) (startG + (endG - startG) * ratio);
        int b = (int) (startB + (endB - startB) * ratio);

        return String.format("%02X%02X%02X", r, g, b);
    }

    /**
     * Obtiene un color arcoíris basado en el tiempo
     * @param text El texto
     * @param offset Offset para la animación
     * @return El texto con colores arcoíris
     */
    public static String createRainbow(String text, int offset) {
        StringBuilder result = new StringBuilder();
        String[] colors = {"FF0000", "FF8000", "FFFF00", "80FF00", "00FF00", "00FF80", "00FFFF", "0080FF", "0000FF", "8000FF", "FF00FF", "FF0080"};

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == ' ') {
                result.append(c);
                continue;
            }

            int colorIndex = (i + offset) % colors.length;
            result.append("&#").append(colors[colorIndex]).append(c);
        }

        return translate(result.toString());
    }

    /**
     * Remueve todos los códigos de color de un texto
     * @param text El texto
     * @return El texto sin colores
     */
    public static String stripColor(String text) {
        if (text == null) return "";
        return ChatColor.stripColor(translate(text));
    }
}