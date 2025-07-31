package com.jb.afrostyle.core.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.RuntimeMXBean;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Utilitaires système pour AfroStyle
 * Fournit des informations sur l'environnement d'exécution, le système et la JVM
 * Utile pour monitoring, debugging et configuration dynamique
 * 
 * @version 1.0
 * @since Java 21
 */
public final class SystemUtils {
    
    private static final Logger logger = LoggerFactory.getLogger(SystemUtils.class);
    
    // Cache pour éviter les appels système répétitifs
    private static volatile Map<String, Object> systemInfoCache = null;
    private static volatile LocalDateTime cacheTime = null;
    private static final Duration CACHE_DURATION = Duration.ofMinutes(5);
    
    /**
     * Constructeur privé pour classe utilitaire
     */
    private SystemUtils() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }
    
    // ==================== INFORMATIONS SYSTÈME DE BASE ====================
    
    /**
     * Obtient le nom du système d'exploitation
     * @return Nom de l'OS
     */
    public static String getOperatingSystem() {
        return System.getProperty("os.name");
    }
    
    /**
     * Obtient la version du système d'exploitation
     * @return Version de l'OS
     */
    public static String getOperatingSystemVersion() {
        return System.getProperty("os.version");
    }
    
    /**
     * Obtient l'architecture du système
     * @return Architecture (x86, x64, etc.)
     */
    public static String getSystemArchitecture() {
        return System.getProperty("os.arch");
    }
    
    /**
     * Vérifie si le système est Windows
     * @return true si Windows
     */
    public static boolean isWindows() {
        return getOperatingSystem().toLowerCase().contains("windows");
    }
    
    /**
     * Vérifie si le système est Linux
     * @return true si Linux
     */
    public static boolean isLinux() {
        return getOperatingSystem().toLowerCase().contains("linux");
    }
    
    /**
     * Vérifie si le système est macOS
     * @return true si macOS
     */
    public static boolean isMacOS() {
        return getOperatingSystem().toLowerCase().contains("mac");
    }
    
    /**
     * Vérifie si le système est Unix-like
     * @return true si Unix-like
     */
    public static boolean isUnixLike() {
        return isLinux() || isMacOS();
    }
    
    // ==================== INFORMATIONS JAVA/JVM ====================
    
    /**
     * Obtient la version de Java
     * @return Version Java
     */
    public static String getJavaVersion() {
        return System.getProperty("java.version");
    }
    
    /**
     * Obtient le vendor Java
     * @return Vendor Java
     */
    public static String getJavaVendor() {
        return System.getProperty("java.vendor");
    }
    
    /**
     * Obtient le répertoire d'installation Java
     * @return Répertoire Java
     */
    public static String getJavaHome() {
        return System.getProperty("java.home");
    }
    
    /**
     * Obtient le nom de la JVM
     * @return Nom JVM
     */
    public static String getJvmName() {
        return System.getProperty("java.vm.name");
    }
    
    /**
     * Obtient la version de la JVM
     * @return Version JVM
     */
    public static String getJvmVersion() {
        return System.getProperty("java.vm.version");
    }
    
    /**
     * Vérifie si on utilise Java 21 ou plus
     * @return true si Java 21+
     */
    public static boolean isJava21OrLater() {
        try {
            String version = getJavaVersion();
            int majorVersion = Integer.parseInt(version.split("\\.")[0]);
            return majorVersion >= 21;
        } catch (Exception e) {
            logger.warn("Could not determine Java version: {}", e.getMessage());
            return false;
        }
    }
    
    // ==================== INFORMATIONS MÉMOIRE ====================
    
    /**
     * Obtient la mémoire totale de la JVM en bytes
     * @return Mémoire totale
     */
    public static long getTotalMemory() {
        return Runtime.getRuntime().totalMemory();
    }
    
    /**
     * Obtient la mémoire libre de la JVM en bytes
     * @return Mémoire libre
     */
    public static long getFreeMemory() {
        return Runtime.getRuntime().freeMemory();
    }
    
    /**
     * Obtient la mémoire utilisée de la JVM en bytes
     * @return Mémoire utilisée
     */
    public static long getUsedMemory() {
        return getTotalMemory() - getFreeMemory();
    }
    
    /**
     * Obtient la mémoire maximale de la JVM en bytes
     * @return Mémoire maximale
     */
    public static long getMaxMemory() {
        return Runtime.getRuntime().maxMemory();
    }
    
    /**
     * Obtient le pourcentage de mémoire utilisée
     * @return Pourcentage (0-100)
     */
    public static double getMemoryUsagePercentage() {
        long used = getUsedMemory();
        long max = getMaxMemory();
        return max > 0 ? (double) used / max * 100.0 : 0.0;
    }
    
    /**
     * Formate la mémoire en unité lisible
     * @param bytes Bytes à formater
     * @return String formatée (ex: "512 MB")
     */
    public static String formatMemory(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        }
        
        String[] units = {"B", "KB", "MB", "GB", "TB"};
        int unitIndex = 0;
        double size = bytes;
        
        while (size >= 1024 && unitIndex < units.length - 1) {
            size /= 1024;
            unitIndex++;
        }
        
        return String.format("%.1f %s", size, units[unitIndex]);
    }
    
    /**
     * Obtient des informations détaillées sur la mémoire heap
     * @return Map avec informations mémoire
     */
    public static Map<String, Object> getMemoryInfo() {
        MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
        
        Map<String, Object> memoryInfo = new HashMap<>();
        memoryInfo.put("heapUsed", memoryMXBean.getHeapMemoryUsage().getUsed());
        memoryInfo.put("heapMax", memoryMXBean.getHeapMemoryUsage().getMax());
        memoryInfo.put("heapCommitted", memoryMXBean.getHeapMemoryUsage().getCommitted());
        memoryInfo.put("nonHeapUsed", memoryMXBean.getNonHeapMemoryUsage().getUsed());
        memoryInfo.put("nonHeapMax", memoryMXBean.getNonHeapMemoryUsage().getMax());
        memoryInfo.put("totalMemory", getTotalMemory());
        memoryInfo.put("freeMemory", getFreeMemory());
        memoryInfo.put("usedMemory", getUsedMemory());
        memoryInfo.put("maxMemory", getMaxMemory());
        memoryInfo.put("usagePercentage", getMemoryUsagePercentage());
        
        return memoryInfo;
    }
    
    // ==================== INFORMATIONS PROCESSEUR ====================
    
    /**
     * Obtient le nombre de processeurs disponibles
     * @return Nombre de CPUs
     */
    public static int getAvailableProcessors() {
        return Runtime.getRuntime().availableProcessors();
    }
    
    /**
     * Obtient la charge système moyenne (Unix-like seulement)
     * @return Charge système ou -1 si non disponible
     */
    public static double getSystemLoadAverage() {
        try {
            return ManagementFactory.getOperatingSystemMXBean().getSystemLoadAverage();
        } catch (Exception e) {
            logger.debug("System load average not available: {}", e.getMessage());
            return -1.0;
        }
    }
    
    // ==================== INFORMATIONS RÉSEAU ====================
    
    /**
     * Obtient le nom de l'hôte
     * @return Nom de l'hôte
     */
    public static String getHostname() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {
            logger.warn("Could not determine hostname: {}", e.getMessage());
            return "unknown";
        }
    }
    
    /**
     * Obtient l'adresse IP locale
     * @return Adresse IP
     */
    public static String getLocalIpAddress() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (Exception e) {
            logger.warn("Could not determine local IP: {}", e.getMessage());
            return "unknown";
        }
    }
    
    /**
     * Obtient toutes les adresses IP de la machine
     * @return Liste des adresses IP
     */
    public static List<String> getAllIpAddresses() {
        List<String> ipAddresses = new ArrayList<>();
        
        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            
            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = networkInterfaces.nextElement();
                
                if (networkInterface.isLoopback() || !networkInterface.isUp()) {
                    continue;
                }
                
                Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress address = addresses.nextElement();
                    if (!address.isLoopbackAddress() && !address.isLinkLocalAddress()) {
                        ipAddresses.add(address.getHostAddress());
                    }
                }
            }
        } catch (Exception e) {
            logger.warn("Could not enumerate IP addresses: {}", e.getMessage());
        }
        
        return ipAddresses;
    }
    
    // ==================== INFORMATIONS UTILISATEUR ====================
    
    /**
     * Obtient le nom de l'utilisateur courant
     * @return Nom utilisateur
     */
    public static String getCurrentUser() {
        return System.getProperty("user.name");
    }
    
    /**
     * Obtient le répertoire home de l'utilisateur
     * @return Répertoire home
     */
    public static String getUserHome() {
        return System.getProperty("user.home");
    }
    
    /**
     * Obtient le répertoire de travail courant
     * @return Répertoire courant
     */
    public static String getCurrentDirectory() {
        return System.getProperty("user.dir");
    }
    
    /**
     * Obtient le répertoire temporaire système
     * @return Répertoire temporaire
     */
    public static String getTempDirectory() {
        return System.getProperty("java.io.tmpdir");
    }
    
    // ==================== INFORMATIONS DISQUE ====================
    
    /**
     * Obtient l'espace disque total du répertoire courant
     * @return Espace total en bytes
     */
    public static long getTotalDiskSpace() {
        return new File(getCurrentDirectory()).getTotalSpace();
    }
    
    /**
     * Obtient l'espace disque libre du répertoire courant
     * @return Espace libre en bytes
     */
    public static long getFreeDiskSpace() {
        return new File(getCurrentDirectory()).getFreeSpace();
    }
    
    /**
     * Obtient l'espace disque utilisé du répertoire courant
     * @return Espace utilisé en bytes
     */
    public static long getUsedDiskSpace() {
        return getTotalDiskSpace() - getFreeDiskSpace();
    }
    
    /**
     * Obtient le pourcentage d'utilisation du disque
     * @return Pourcentage (0-100)
     */
    public static double getDiskUsagePercentage() {
        long total = getTotalDiskSpace();
        long used = getUsedDiskSpace();
        return total > 0 ? (double) used / total * 100.0 : 0.0;
    }
    
    // ==================== INFORMATIONS RUNTIME ====================
    
    /**
     * Obtient le temps de fonctionnement de la JVM
     * @return Durée de fonctionnement
     */
    public static Duration getUptime() {
        RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
        return Duration.ofMillis(runtimeMXBean.getUptime());
    }
    
    /**
     * Obtient la date de démarrage de la JVM
     * @return Date de démarrage
     */
    public static LocalDateTime getStartTime() {
        RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
        return LocalDateTime.now().minus(Duration.ofMillis(runtimeMXBean.getUptime()));
    }
    
    /**
     * Formate la durée de fonctionnement
     * @return String formatée (ex: "2j 3h 45min")
     */
    public static String formatUptime() {
        Duration uptime = getUptime();
        
        long days = uptime.toDays();
        long hours = uptime.toHours() % 24;
        long minutes = uptime.toMinutes() % 60;
        
        StringBuilder sb = new StringBuilder();
        
        if (days > 0) {
            sb.append(days).append("j ");
        }
        if (hours > 0) {
            sb.append(hours).append("h ");
        }
        if (minutes > 0) {
            sb.append(minutes).append("min");
        }
        
        return sb.length() > 0 ? sb.toString().trim() : "0min";
    }
    
    // ==================== VARIABLES D'ENVIRONNEMENT ====================
    
    /**
     * Obtient une variable d'environnement
     * @param name Nom de la variable
     * @return Valeur ou null si non définie
     */
    public static String getEnvironmentVariable(String name) {
        return System.getenv(name);
    }
    
    /**
     * Obtient une variable d'environnement avec valeur par défaut
     * @param name Nom de la variable
     * @param defaultValue Valeur par défaut
     * @return Valeur ou valeur par défaut
     */
    public static String getEnvironmentVariable(String name, String defaultValue) {
        String value = System.getenv(name);
        return value != null ? value : defaultValue;
    }
    
    /**
     * Obtient toutes les variables d'environnement
     * @return Map des variables d'environnement
     */
    public static Map<String, String> getAllEnvironmentVariables() {
        return System.getenv();
    }
    
    /**
     * Vérifie si une variable d'environnement existe
     * @param name Nom de la variable
     * @return true si la variable existe
     */
    public static boolean hasEnvironmentVariable(String name) {
        return System.getenv(name) != null;
    }
    
    // ==================== PROPRIÉTÉS SYSTÈME ====================
    
    /**
     * Obtient une propriété système
     * @param name Nom de la propriété
     * @return Valeur ou null si non définie
     */
    public static String getSystemProperty(String name) {
        return System.getProperty(name);
    }
    
    /**
     * Obtient une propriété système avec valeur par défaut
     * @param name Nom de la propriété
     * @param defaultValue Valeur par défaut
     * @return Valeur ou valeur par défaut
     */
    public static String getSystemProperty(String name, String defaultValue) {
        return System.getProperty(name, defaultValue);
    }
    
    /**
     * Définit une propriété système
     * @param name Nom de la propriété
     * @param value Valeur
     * @return Ancienne valeur ou null
     */
    public static String setSystemProperty(String name, String value) {
        return System.setProperty(name, value);
    }
    
    // ==================== INFORMATIONS SPRING ====================
    
    /**
     * Obtient le profil Spring actif depuis l'environnement
     * @param environment Environnement Spring
     * @return Profils actifs
     */
    public static String[] getActiveProfiles(Environment environment) {
        return environment != null ? environment.getActiveProfiles() : new String[0];
    }
    
    /**
     * Vérifie si un profil Spring est actif
     * @param environment Environnement Spring
     * @param profile Profil à vérifier
     * @return true si profil actif
     */
    public static boolean isProfileActive(Environment environment, String profile) {
        if (environment == null || StringUtils.isBlank(profile)) {
            return false;
        }
        
        return Arrays.asList(environment.getActiveProfiles()).contains(profile);
    }
    
    /**
     * Obtient une propriété Spring
     * @param environment Environnement Spring
     * @param key Clé de la propriété
     * @return Valeur ou null
     */
    public static String getSpringProperty(Environment environment, String key) {
        return environment != null ? environment.getProperty(key) : null;
    }
    
    /**
     * Obtient une propriété Spring avec valeur par défaut
     * @param environment Environnement Spring
     * @param key Clé de la propriété
     * @param defaultValue Valeur par défaut
     * @return Valeur ou valeur par défaut
     */
    public static String getSpringProperty(Environment environment, String key, String defaultValue) {
        return environment != null ? environment.getProperty(key, defaultValue) : defaultValue;
    }
    
    // ==================== INFORMATIONS COMPLÈTES ====================
    
    /**
     * Obtient toutes les informations système dans une Map
     * @return Map complète des informations système
     */
    public static Map<String, Object> getSystemInfo() {
        // Utiliser le cache si valide
        if (systemInfoCache != null && cacheTime != null && 
            Duration.between(cacheTime, LocalDateTime.now()).compareTo(CACHE_DURATION) < 0) {
            return new HashMap<>(systemInfoCache);
        }
        
        Map<String, Object> info = new HashMap<>();
        
        // Informations OS
        info.put("os.name", getOperatingSystem());
        info.put("os.version", getOperatingSystemVersion());
        info.put("os.arch", getSystemArchitecture());
        info.put("os.isWindows", isWindows());
        info.put("os.isLinux", isLinux());
        info.put("os.isMacOS", isMacOS());
        
        // Informations Java
        info.put("java.version", getJavaVersion());
        info.put("java.vendor", getJavaVendor());
        info.put("java.home", getJavaHome());
        info.put("jvm.name", getJvmName());
        info.put("jvm.version", getJvmVersion());
        info.put("java.isJava21Plus", isJava21OrLater());
        
        // Informations mémoire
        info.putAll(getMemoryInfo());
        
        // Informations processeur
        info.put("cpu.processors", getAvailableProcessors());
        info.put("cpu.loadAverage", getSystemLoadAverage());
        
        // Informations réseau
        info.put("network.hostname", getHostname());
        info.put("network.localIp", getLocalIpAddress());
        info.put("network.allIps", getAllIpAddresses());
        
        // Informations utilisateur
        info.put("user.name", getCurrentUser());
        info.put("user.home", getUserHome());
        info.put("user.dir", getCurrentDirectory());
        info.put("user.temp", getTempDirectory());
        
        // Informations disque
        info.put("disk.total", getTotalDiskSpace());
        info.put("disk.free", getFreeDiskSpace());
        info.put("disk.used", getUsedDiskSpace());
        info.put("disk.usagePercentage", getDiskUsagePercentage());
        
        // Informations runtime
        info.put("runtime.uptime", getUptime());
        info.put("runtime.startTime", getStartTime());
        info.put("runtime.uptimeFormatted", formatUptime());
        
        // Mettre en cache
        systemInfoCache = info;
        cacheTime = LocalDateTime.now();
        
        return new HashMap<>(info);
    }
    
    /**
     * Génère un rapport système complet
     * @return String avec rapport formaté
     */
    public static String generateSystemReport() {
        Map<String, Object> info = getSystemInfo();
        
        StringBuilder report = new StringBuilder();
        report.append("=== SYSTEM REPORT ===\n");
        
        report.append("\n--- Operating System ---\n");
        report.append(String.format("Name: %s\n", info.get("os.name")));
        report.append(String.format("Version: %s\n", info.get("os.version")));
        report.append(String.format("Architecture: %s\n", info.get("os.arch")));
        
        report.append("\n--- Java Runtime ---\n");
        report.append(String.format("Java Version: %s\n", info.get("java.version")));
        report.append(String.format("Java Vendor: %s\n", info.get("java.vendor")));
        report.append(String.format("JVM: %s %s\n", info.get("jvm.name"), info.get("jvm.version")));
        
        report.append("\n--- Memory ---\n");
        report.append(String.format("Heap Used: %s\n", formatMemory((Long) info.get("heapUsed"))));
        report.append(String.format("Heap Max: %s\n", formatMemory((Long) info.get("heapMax"))));
        report.append(String.format("Usage: %.1f%%\n", (Double) info.get("usagePercentage")));
        
        report.append("\n--- CPU ---\n");
        report.append(String.format("Processors: %d\n", info.get("cpu.processors")));
        double loadAvg = (Double) info.get("cpu.loadAverage");
        if (loadAvg >= 0) {
            report.append(String.format("Load Average: %.2f\n", loadAvg));
        }
        
        report.append("\n--- Network ---\n");
        report.append(String.format("Hostname: %s\n", info.get("network.hostname")));
        report.append(String.format("Local IP: %s\n", info.get("network.localIp")));
        
        report.append("\n--- Storage ---\n");
        report.append(String.format("Disk Total: %s\n", formatMemory((Long) info.get("disk.total"))));
        report.append(String.format("Disk Free: %s\n", formatMemory((Long) info.get("disk.free"))));
        report.append(String.format("Disk Usage: %.1f%%\n", (Double) info.get("disk.usagePercentage")));
        
        report.append("\n--- Runtime ---\n");
        report.append(String.format("Start Time: %s\n", info.get("runtime.startTime")));
        report.append(String.format("Uptime: %s\n", info.get("runtime.uptimeFormatted")));
        
        report.append("\n=====================");
        
        return report.toString();
    }
    
    /**
     * Vide le cache des informations système
     */
    public static void clearCache() {
        systemInfoCache = null;
        cacheTime = null;
    }
    
    /**
     * Force le garbage collection
     * Note: Utiliser avec précaution, peut impacter les performances
     */
    public static void forceGarbageCollection() {
        logger.info("Forcing garbage collection...");
        System.gc();
    }
}