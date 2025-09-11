package com.smartjarvis.nlu.service;

import com.smartjarvis.nlu.model.IntentResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Rule-based Natural Language Understanding service
 * Uses regex patterns to extract intents and entities from text
 */
@Service
@Slf4j
public class RuleBasedNLU {

    // Intent patterns with confidence scores
    private final Map<Pattern, IntentInfo> intentPatterns;
    
    // Entity extraction patterns
    private final Map<String, Pattern> entityPatterns;

    public RuleBasedNLU() {
        this.intentPatterns = initializeIntentPatterns();
        this.entityPatterns = initializeEntityPatterns();
        
        log.info("Rule-based NLU initialized with {} intent patterns and {} entity patterns", 
                intentPatterns.size(), entityPatterns.size());
    }

    /**
     * Extract intent and entities from text
     */
    public IntentResult extractIntent(String text) {
        if (text == null || text.trim().isEmpty()) {
            return new IntentResult("unknown", 0.0f, Map.of(), text);
        }

        String normalizedText = text.toLowerCase().trim();
        log.debug("Processing text: '{}'", normalizedText);

        // Find matching intent
        for (Map.Entry<Pattern, IntentInfo> entry : intentPatterns.entrySet()) {
            Pattern pattern = entry.getKey();
            IntentInfo intentInfo = entry.getValue();
            
            Matcher matcher = pattern.matcher(normalizedText);
            if (matcher.find()) {
                String intent = intentInfo.intent();
                float confidence = intentInfo.confidence();
                
                // Extract entities
                Map<String, String> entities = extractEntities(normalizedText, intent);
                
                log.info("Intent recognized: '{}' with confidence {:.2f} for text: '{}'", 
                        intent, confidence, text);
                
                return new IntentResult(intent, confidence, entities, text);
            }
        }

        // No intent matched
        log.debug("No intent matched for text: '{}'", text);
        return new IntentResult("unknown", 0.1f, Map.of(), text);
    }

    /**
     * Extract entities from text based on intent
     */
    private Map<String, String> extractEntities(String text, String intent) {
        Map<String, String> entities = new HashMap<>();

        // Common entity extraction
        extractCommonEntities(text, entities);
        
        // Intent-specific entity extraction
        switch (intent) {
            case "todo_create" -> extractTodoEntities(text, entities);
            case "home_control" -> extractHomeControlEntities(text, entities);
            case "time_query" -> extractTimeEntities(text, entities);
            case "weather_query" -> extractWeatherEntities(text, entities);
            case "device_volume_set", "device_volume_up", "device_volume_down" -> extractVolumeEntities(text, entities);
            case "device_open_app" -> extractAppEntities(text, entities);
            case "device_open_url" -> extractUrlEntities(text, entities);
            case "device_media" -> extractMediaEntities(text, entities);
            case "home_light_control" -> extractHomeLightEntities(text, entities);
            case "home_media_control" -> extractHomeMediaEntities(text, entities);
            case "home_scene" -> extractSceneEntities(text, entities);
            case "money_expense", "money_income" -> extractMoneyTransactionEntities(text, entities);
            case "money_expense_report", "money_income_report", "money_category_spending" -> extractMoneyReportEntities(text, entities);
        }

        return entities;
    }

    /**
     * Extract common entities (numbers, times, dates)
     */
    private void extractCommonEntities(String text, Map<String, String> entities) {
        // Extract numbers
        Pattern numberPattern = entityPatterns.get("number");
        Matcher numberMatcher = numberPattern.matcher(text);
        if (numberMatcher.find()) {
            entities.put("number", numberMatcher.group());
        }

        // Extract time
        Pattern timePattern = entityPatterns.get("time");
        Matcher timeMatcher = timePattern.matcher(text);
        if (timeMatcher.find()) {
            entities.put("time", timeMatcher.group());
        }

        // Extract date
        Pattern datePattern = entityPatterns.get("date");
        Matcher dateMatcher = datePattern.matcher(text);
        if (dateMatcher.find()) {
            entities.put("date", dateMatcher.group());
        }
    }

    /**
     * Extract todo-specific entities
     */
    private void extractTodoEntities(String text, Map<String, String> entities) {
        // Extract task description (everything after "задач", "дел", etc.)
        Pattern taskPattern = Pattern.compile("(?:задач|дел|todo)\\w*\\s+(.+?)(?:\\s+на|\\s+в|$)");
        Matcher taskMatcher = taskPattern.matcher(text);
        if (taskMatcher.find()) {
            entities.put("task", taskMatcher.group(1).trim());
        }
    }

    /**
     * Extract home control entities
     */
    private void extractHomeControlEntities(String text, Map<String, String> entities) {
        // Extract device
        Pattern devicePattern = entityPatterns.get("device");
        Matcher deviceMatcher = devicePattern.matcher(text);
        if (deviceMatcher.find()) {
            entities.put("device", deviceMatcher.group());
        }

        // Extract action
        if (text.contains("включи") || text.contains("включить")) {
            entities.put("action", "turn_on");
        } else if (text.contains("выключи") || text.contains("выключить")) {
            entities.put("action", "turn_off");
        }

        // Extract room
        Pattern roomPattern = entityPatterns.get("room");
        Matcher roomMatcher = roomPattern.matcher(text);
        if (roomMatcher.find()) {
            entities.put("room", roomMatcher.group());
        }
    }

    /**
     * Extract time-related entities
     */
    private void extractTimeEntities(String text, Map<String, String> entities) {
        if (text.contains("сейчас") || text.contains("время")) {
            entities.put("time_type", "current");
        } else if (text.contains("завтра")) {
            entities.put("time_type", "tomorrow");
        } else if (text.contains("вчера")) {
            entities.put("time_type", "yesterday");
        }
    }

    /**
     * Extract weather-related entities
     */
    private void extractWeatherEntities(String text, Map<String, String> entities) {
        if (text.contains("сегодня")) {
            entities.put("weather_time", "today");
        } else if (text.contains("завтра")) {
            entities.put("weather_time", "tomorrow");
        }
    }

    /**
     * Initialize intent recognition patterns
     */
    private Map<Pattern, IntentInfo> initializeIntentPatterns() {
        Map<Pattern, IntentInfo> patterns = new HashMap<>();

        // Greetings
        patterns.put(
            Pattern.compile("(?i).*(привет|здравствуй|добрый день|добрый вечер|добрый утро|хай|hello).*"),
            new IntentInfo("greeting", 0.95f)
        );

        // Help requests
        patterns.put(
            Pattern.compile("(?i).*(помощь|help|что умеешь|команды|возможности).*"),
            new IntentInfo("help", 0.90f)
        );

        // Todo operations - расширенные правила
        patterns.put(
            Pattern.compile("(?i).*(добавь|создай|сделай|напомни).*(задач|дел|todo)\\s+(.+)"),
            new IntentInfo("todo_create", 0.90f)
        );
        
        patterns.put(
            Pattern.compile("(?i).*(покажи|список|какие).*(задач|дел|todo).*"),
            new IntentInfo("todo_list", 0.85f)
        );
        
        patterns.put(
            Pattern.compile("(?i).*(выполн|готов|сделал|завершил).*(задач|дел)\\s*(\\d+).*"),
            new IntentInfo("todo_complete", 0.85f)
        );
        
        patterns.put(
            Pattern.compile("(?i).*(удали|убери|отмени).*(задач|дел)\\s*(\\d+).*"),
            new IntentInfo("todo_delete", 0.85f)
        );
        
        patterns.put(
            Pattern.compile("(?i).*(статистика|сколько|количество).*(задач|дел).*"),
            new IntentInfo("todo_stats", 0.80f)
        );

        // Home control - расширенные правила
        patterns.put(
            Pattern.compile("(?i).*(включи|выключи|переключи).*(свет|лампа|освещение).*в\\s+(\\w+).*"),
            new IntentInfo("home_light_control", 0.90f)
        );
        
        patterns.put(
            Pattern.compile("(?i).*(включи|выключи|переключи).*(свет|лампа|освещение).*"),
            new IntentInfo("home_light_control", 0.85f)
        );
        
        patterns.put(
            Pattern.compile("(?i).*(музык|звук|аудио).*(включи|выключи|громче|тише|громкость).*в\\s+(\\w+).*"),
            new IntentInfo("home_media_control", 0.90f)
        );
        
        patterns.put(
            Pattern.compile("(?i).*(музык|звук|аудио).*(включи|выключи|громче|тише|громкость).*"),
            new IntentInfo("home_media_control", 0.85f)
        );
        
        patterns.put(
            Pattern.compile("(?i).*(включи|активируй|запусти).*(сцен\\w*)\\s+(\\w+).*"),
            new IntentInfo("home_scene", 0.85f)
        );
        
        patterns.put(
            Pattern.compile("(?i).*(режим|сцена)\\s+(\\w+).*"),
            new IntentInfo("home_scene", 0.80f)
        );

        // Time queries
        patterns.put(
            Pattern.compile("(?i).*(который час|сколько времени|время).*"),
            new IntentInfo("time_query", 0.85f)
        );

        // Weather queries
        patterns.put(
            Pattern.compile("(?i).*(погода|температура|дождь|солнце).*"),
            new IntentInfo("weather_query", 0.80f)
        );

        // Device control commands
        patterns.put(
            Pattern.compile("(?i).*(громче|увеличь громкость|сделай громче).*"),
            new IntentInfo("device_volume_up", 0.90f)
        );
        
        patterns.put(
            Pattern.compile("(?i).*(тише|уменьши громкость|сделай тише).*"),
            new IntentInfo("device_volume_down", 0.90f)
        );
        
        patterns.put(
            Pattern.compile("(?i).*(громкость)\\s+(\\d+).*"),
            new IntentInfo("device_volume_set", 0.90f)
        );
        
        patterns.put(
            Pattern.compile("(?i).*(открой|запусти|включи)\\s+(\\w+).*"),
            new IntentInfo("device_open_app", 0.85f)
        );
        
        patterns.put(
            Pattern.compile("(?i).*(открой|перейди)\\s+(https?://\\S+|www\\.\\S+).*"),
            new IntentInfo("device_open_url", 0.85f)
        );
        
        patterns.put(
            Pattern.compile("(?i).*(скриншот|снимок экрана|сделай снимок).*"),
            new IntentInfo("device_screenshot", 0.90f)
        );
        
        patterns.put(
            Pattern.compile("(?i).*(заблокируй|блокировка|заблокировать экран).*"),
            new IntentInfo("device_lock", 0.85f)
        );
        
        patterns.put(
            Pattern.compile("(?i).*(пауза|воспроизведение|плей|стоп музыка).*"),
            new IntentInfo("device_media", 0.85f)
        );

        // Money/Finance commands
        patterns.put(
            Pattern.compile("(?i).*(потратил|трата|расход|купил|заплатил)\\s+(\\d+).*(?:рубл|руб|₽).*(?:на|за)\\s+(.+)"),
            new IntentInfo("money_expense", 0.90f)
        );
        
        patterns.put(
            Pattern.compile("(?i).*(получил|доход|зарплата|премия|заработал)\\s+(\\d+).*(?:рубл|руб|₽).*"),
            new IntentInfo("money_income", 0.85f)
        );
        
        patterns.put(
            Pattern.compile("(?i).*(сколько потратил|расходы|трат).*(?:за|в)\\s+(месяц|неделю|день|сегодня).*"),
            new IntentInfo("money_expense_report", 0.85f)
        );
        
        patterns.put(
            Pattern.compile("(?i).*(сколько заработал|доходы|доход).*(?:за|в)\\s+(месяц|неделю|день|сегодня).*"),
            new IntentInfo("money_income_report", 0.85f)
        );
        
        patterns.put(
            Pattern.compile("(?i).*(баланс|сколько денег|остаток|сальдо).*"),
            new IntentInfo("money_balance", 0.85f)
        );
        
        patterns.put(
            Pattern.compile("(?i).*(статистика|отчет|аналитика).*(?:финанс|денег|трат).*"),
            new IntentInfo("money_stats", 0.80f)
        );
        
        patterns.put(
            Pattern.compile("(?i).*(по категори).*(?:трат|расход).*"),
            new IntentInfo("money_category_spending", 0.80f)
        );

        // Calendar/Event commands
        patterns.put(
            Pattern.compile("(?i).*(создай|добавь|запланируй).*(встреч|событие|мероприятие).*"),
            new IntentInfo("calendar_create", 0.85f)
        );
        
        patterns.put(
            Pattern.compile("(?i).*(что|какие).*(сегодня|на сегодня).*(?:встреч|событи|план).*"),
            new IntentInfo("calendar_today", 0.85f)
        );
        
        patterns.put(
            Pattern.compile("(?i).*(что|какие).*(завтра|на завтра).*(?:встреч|событи|план).*"),
            new IntentInfo("calendar_tomorrow", 0.85f)
        );
        
        patterns.put(
            Pattern.compile("(?i).*(что|какие).*(на неделе|на этой неделе).*(?:встреч|событи|план).*"),
            new IntentInfo("calendar_week", 0.80f)
        );
        
        patterns.put(
            Pattern.compile("(?i).*(свободен|свободно|занят).*(?:сегодня|завтра|в).*"),
            new IntentInfo("calendar_availability", 0.80f)
        );

        // System commands
        patterns.put(
            Pattern.compile("(?i).*(стоп|остановись|хватит|отмена).*"),
            new IntentInfo("stop", 0.95f)
        );

        return patterns;
    }

    /**
     * Initialize entity extraction patterns
     */
    private Map<String, Pattern> initializeEntityPatterns() {
        Map<String, Pattern> patterns = new HashMap<>();

        // Numbers
        patterns.put("number", Pattern.compile("\\d+"));
        
        // Time patterns
        patterns.put("time", Pattern.compile("\\d{1,2}[:\\.]\\d{2}|\\d{1,2}\\s*час"));
        
        // Date patterns
        patterns.put("date", Pattern.compile("завтра|сегодня|вчера|\\d{1,2}[./]\\d{1,2}"));
        
        // Device patterns
        patterns.put("device", Pattern.compile("свет|лампа|освещение|музык|телевизор|кондиционер"));
        
        // Room patterns
        patterns.put("room", Pattern.compile("гостин|спальн|кухн|ванн|коридор|прихож"));

        return patterns;
    }

    /**
     * Extract volume-related entities
     */
    private void extractVolumeEntities(String text, Map<String, String> entities) {
        // Extract volume level
        Pattern volumePattern = Pattern.compile("(?:громкость|volume)\\s+(\\d+)");
        Matcher volumeMatcher = volumePattern.matcher(text);
        if (volumeMatcher.find()) {
            entities.put("volume", volumeMatcher.group(1));
        }

        // Extract volume action
        if (text.contains("громче") || text.contains("увеличь")) {
            entities.put("volume_action", "up");
        } else if (text.contains("тише") || text.contains("уменьши")) {
            entities.put("volume_action", "down");
        }
    }

    /**
     * Extract application entities
     */
    private void extractAppEntities(String text, Map<String, String> entities) {
        // Extract application name
        Pattern appPattern = Pattern.compile("(?:открой|запусти|включи)\\s+(\\w+)");
        Matcher appMatcher = appPattern.matcher(text);
        if (appMatcher.find()) {
            String appName = appMatcher.group(1).toLowerCase();
            
            // Map common names
            String mappedApp = switch (appName) {
                case "код", "vscode" -> "code";
                case "браузер", "интернет" -> "firefox";
                case "терминал", "консоль" -> "terminal";
                case "файлы", "проводник" -> "files";
                case "калькулятор" -> "calculator";
                case "настройки" -> "settings";
                default -> appName;
            };
            
            entities.put("app_name", mappedApp);
        }
    }

    /**
     * Extract URL entities
     */
    private void extractUrlEntities(String text, Map<String, String> entities) {
        // Extract URL
        Pattern urlPattern = Pattern.compile("(https?://\\S+|www\\.\\S+)");
        Matcher urlMatcher = urlPattern.matcher(text);
        if (urlMatcher.find()) {
            String url = urlMatcher.group(1);
            if (!url.startsWith("http")) {
                url = "https://" + url;
            }
            entities.put("url", url);
        }
    }

    /**
     * Extract media control entities
     */
    private void extractMediaEntities(String text, Map<String, String> entities) {
        if (text.contains("пауза") || text.contains("pause")) {
            entities.put("media_action", "pause");
        } else if (text.contains("плей") || text.contains("воспроизведение") || text.contains("play")) {
            entities.put("media_action", "play");
        } else if (text.contains("стоп") || text.contains("stop")) {
            entities.put("media_action", "stop");
        } else if (text.contains("дальше") || text.contains("next")) {
            entities.put("media_action", "next");
        } else if (text.contains("назад") || text.contains("previous")) {
            entities.put("media_action", "previous");
        } else {
            entities.put("media_action", "play-pause"); // Default
        }
    }

    /**
     * Extract home light control entities
     */
    private void extractHomeLightEntities(String text, Map<String, String> entities) {
        // Extract room
        Pattern roomPattern = Pattern.compile("в\\s+(гостин|спальн|кухн|ванн|коридор|кабинет|прихож)\\w*");
        Matcher roomMatcher = roomPattern.matcher(text);
        if (roomMatcher.find()) {
            String room = roomMatcher.group(1);
            entities.put("room", normalizeRoom(room));
        }

        // Extract light action
        if (text.contains("включи") || text.contains("включить")) {
            entities.put("action", "turn_on");
        } else if (text.contains("выключи") || text.contains("выключить")) {
            entities.put("action", "turn_off");
        } else if (text.contains("переключи")) {
            entities.put("action", "toggle");
        }

        // Extract brightness if present
        Pattern brightnessPattern = Pattern.compile("(\\d+)\\s*%|яркость\\s+(\\d+)");
        Matcher brightnessMatcher = brightnessPattern.matcher(text);
        if (brightnessMatcher.find()) {
            String brightness = brightnessMatcher.group(1) != null ? 
                              brightnessMatcher.group(1) : brightnessMatcher.group(2);
            entities.put("brightness", brightness);
        }
    }

    /**
     * Extract home media control entities
     */
    private void extractHomeMediaEntities(String text, Map<String, String> entities) {
        // Extract room
        Pattern roomPattern = Pattern.compile("в\\s+(гостин|спальн|кухн|везде|компьютер)\\w*");
        Matcher roomMatcher = roomPattern.matcher(text);
        if (roomMatcher.find()) {
            String room = roomMatcher.group(1);
            entities.put("room", normalizeRoom(room));
        }

        // Extract media action
        if (text.contains("включи") || text.contains("включить")) {
            entities.put("media_action", "play");
        } else if (text.contains("выключи") || text.contains("выключить")) {
            entities.put("media_action", "stop");
        } else if (text.contains("пауза")) {
            entities.put("media_action", "pause");
        } else if (text.contains("громче")) {
            entities.put("media_action", "volume_up");
        } else if (text.contains("тише")) {
            entities.put("media_action", "volume_down");
        }

        // Extract volume level
        Pattern volumePattern = Pattern.compile("громкость\\s+(\\d+)|до\\s+(\\d+)\\s*%");
        Matcher volumeMatcher = volumePattern.matcher(text);
        if (volumeMatcher.find()) {
            String volume = volumeMatcher.group(1) != null ? 
                           volumeMatcher.group(1) : volumeMatcher.group(2);
            entities.put("volume_level", volume);
        }
    }

    /**
     * Extract scene entities
     */
    private void extractSceneEntities(String text, Map<String, String> entities) {
        // Extract scene name
        Pattern scenePattern = Pattern.compile("(?:сцен\\w*|режим)\\s+(фокус|релакс|сон|работа|кино|вечеринка|утро|вечер)");
        Matcher sceneMatcher = scenePattern.matcher(text);
        if (sceneMatcher.find()) {
            entities.put("scene_name", sceneMatcher.group(1));
        }
    }

    /**
     * Normalize room names
     */
    private String normalizeRoom(String room) {
        return switch (room.toLowerCase()) {
            case "гостин" -> "гостиная";
            case "спальн" -> "спальня";
            case "кухн" -> "кухня";
            case "ванн" -> "ванная";
            case "коридор", "прихож" -> "коридор";
            case "кабинет" -> "кабинет";
            default -> room;
        };
    }

    /**
     * Extract money transaction entities
     */
    private void extractMoneyTransactionEntities(String text, Map<String, String> entities) {
        // Extract amount
        Pattern amountPattern = Pattern.compile("(\\d+(?:[.,]\\d{1,2})?)\\s*(?:рубл|руб|₽)");
        Matcher amountMatcher = amountPattern.matcher(text);
        if (amountMatcher.find()) {
            String amount = amountMatcher.group(1).replace(",", ".");
            entities.put("amount", amount);
        }

        // Extract description/category from expense
        Pattern expenseDescPattern = Pattern.compile("(?:потратил|купил|заплатил|трата|расход).*(?:на|за)\\s+(.+?)(?:\\s+\\d+|$)");
        Matcher expenseDescMatcher = expenseDescPattern.matcher(text);
        if (expenseDescMatcher.find()) {
            String description = expenseDescMatcher.group(1).trim();
            entities.put("description", description);
            
            // Try to map to category
            String category = mapDescriptionToCategory(description);
            if (category != null) {
                entities.put("category", category);
            }
        }

        // Extract description from income
        Pattern incomeDescPattern = Pattern.compile("(?:получил|доход|зарплата|премия|заработал).*(?:от|за)\\s+(.+?)(?:\\s+\\d+|$)");
        Matcher incomeDescMatcher = incomeDescPattern.matcher(text);
        if (incomeDescMatcher.find()) {
            String description = incomeDescMatcher.group(1).trim();
            entities.put("description", description);
        }

        // Extract payment method
        if (text.contains("картой") || text.contains("карта")) {
            entities.put("payment_method", "CARD");
        } else if (text.contains("наличными") || text.contains("наличные")) {
            entities.put("payment_method", "CASH");
        } else if (text.contains("переводом") || text.contains("перевод")) {
            entities.put("payment_method", "TRANSFER");
        }
    }

    /**
     * Extract money report entities
     */
    private void extractMoneyReportEntities(String text, Map<String, String> entities) {
        // Extract time period
        if (text.contains("сегодня")) {
            entities.put("period", "today");
        } else if (text.contains("вчера")) {
            entities.put("period", "yesterday");
        } else if (text.contains("неделю")) {
            entities.put("period", "week");
        } else if (text.contains("месяц")) {
            entities.put("period", "month");
        } else if (text.contains("год")) {
            entities.put("period", "year");
        }

        // Extract specific category if mentioned
        Pattern categoryPattern = Pattern.compile("(?:по категории|категория)\\s+(\\w+)");
        Matcher categoryMatcher = categoryPattern.matcher(text);
        if (categoryMatcher.find()) {
            entities.put("category", categoryMatcher.group(1));
        }
    }

    /**
     * Map description to category (simplified)
     */
    private String mapDescriptionToCategory(String description) {
        String lowerDesc = description.toLowerCase();
        
        if (lowerDesc.contains("еда") || lowerDesc.contains("обед") || lowerDesc.contains("ужин") || 
            lowerDesc.contains("завтрак") || lowerDesc.contains("кафе") || lowerDesc.contains("ресторан")) {
            return "Еда";
        } else if (lowerDesc.contains("такси") || lowerDesc.contains("автобус") || lowerDesc.contains("метро") ||
                  lowerDesc.contains("бензин") || lowerDesc.contains("транспорт")) {
            return "Транспорт";
        } else if (lowerDesc.contains("кино") || lowerDesc.contains("театр") || lowerDesc.contains("концерт") ||
                  lowerDesc.contains("игра") || lowerDesc.contains("развлечение")) {
            return "Развлечения";
        } else if (lowerDesc.contains("одежда") || lowerDesc.contains("обувь") || lowerDesc.contains("покупка")) {
            return "Покупки";
        } else if (lowerDesc.contains("свет") || lowerDesc.contains("газ") || lowerDesc.contains("вода") ||
                  lowerDesc.contains("интернет") || lowerDesc.contains("коммунальн")) {
            return "Коммунальные";
        } else if (lowerDesc.contains("врач") || lowerDesc.contains("лекарств") || lowerDesc.contains("больниц")) {
            return "Здоровье";
        }
        
        return null; // Use "Прочее" as default
    }

    /**
     * Intent information record
     */
    private record IntentInfo(String intent, float confidence) {}
}
