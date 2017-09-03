package me.semx11.autotip.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.stream.Collectors;
import me.semx11.autotip.Autotip;
import me.semx11.autotip.misc.Stats;
import me.semx11.autotip.misc.TipTracker;
import me.semx11.autotip.misc.Writer;
import org.apache.commons.io.FileUtils;

public class FileUtil {

    public static void getVars() throws IOException {
        try {
            File statsDir = new File(Autotip.USER_DIR + "stats");

            if (!statsDir.exists()) {
                statsDir.mkdirs();
            }

            if (exists(Autotip.USER_DIR + "upgrade-date.at")) {
                String date = FileUtils
                        .readFileToString(Paths.get(Autotip.USER_DIR + "upgrade-date.at").toFile());
                LocalDate parsed;
                try {
                    parsed = LocalDate.parse(date, DateTimeFormatter.ofPattern("dd-MM-yyyy"));
                } catch (DateTimeParseException e) {
                    e.printStackTrace();
                    parsed = LocalDate.now();
                }
                Stats.setUpgradeDate(parsed);
            } else {
                LocalDate date = LocalDate.now().plusDays(1);
                String dateString = DateTimeFormatter.ofPattern("dd-MM-yyyy").format(date);
                FileUtils
                        .writeStringToFile(Paths.get(Autotip.USER_DIR + "upgrade-date.at").toFile(),
                                dateString);
                Stats.setUpgradeDate(date);
            }

            boolean executeWriter = false;

            if (exists(Autotip.USER_DIR + "options.at")) {
                try (BufferedReader readOptions = new BufferedReader(
                        new FileReader(Autotip.USER_DIR + "options.at"))) {
                    List<String> lines = readOptions.lines().collect(Collectors.toList());
                    if (lines.size() >= 4) {
                        Autotip.toggle = Boolean.parseBoolean(lines.get(0));
                        String chatSetting = lines.get(1);
                        switch (chatSetting) {
                            case "true":
                            case "false":
                                Autotip.messageOption = Boolean.parseBoolean(chatSetting)
                                        ? MessageOption.SHOWN
                                        : MessageOption.COMPACT;
                                break;
                            case "SHOWN":
                            case "COMPACT":
                            case "HIDDEN":
                                Autotip.messageOption = MessageOption.valueOf(chatSetting);
                                break;
                            default:
                                Autotip.messageOption = MessageOption.SHOWN;
                        }
                        try {
                            Autotip.totalTipsSent = Integer.parseInt(lines.get(3));
                        } catch (NumberFormatException e) {
                            Autotip.totalTipsSent = 0;
                            executeWriter = true;
                        }
                    } else {
                        executeWriter = true;
                    }
                }
            } else {
                executeWriter = true;
            }

            Path path = NioWrapper.getPath(Autotip.USER_DIR + "stats/" + getDate() + ".at");
            if (NioWrapper.exists(path)) {
                List<String> lines = Files.lines(path).collect(Collectors.toList());
                if (lines.size() >= 2) {
                    String[] tipStats = lines.get(0).split(":");
                    TipTracker.tipsSent = Integer.parseInt(tipStats[0]);
                    TipTracker.tipsReceived =
                            tipStats.length > 1 ? Integer.parseInt(tipStats[1]) : 0;
                    TipTracker.karmaCount = Integer.parseInt(lines.get(1));
                    lines.stream().skip(2).forEach(line -> {
                        String[] stats = line.split(":");
                        TipTracker.tipsSentEarnings.put(stats[0], Integer.parseInt(stats[1]));
                        if (stats.length > 2) {
                            TipTracker.tipsReceivedEarnings
                                    .put(stats[0], Integer.parseInt(stats[2]));
                        }
                    });
                }
            } else {
                executeWriter = true;
            }

            if (executeWriter) {
                Writer.execute();
            }
        } catch (IOException | IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    private static boolean exists(String path) {
        return Files.exists(Paths.get(path));
    }

    public static String getDate() {
        return new SimpleDateFormat("dd-MM-yyyy").format(new Date());
    }

    public static String getServerDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
        sdf.setTimeZone(TimeZone.getTimeZone("EST"));
        return sdf.format(new Date());
    }

}