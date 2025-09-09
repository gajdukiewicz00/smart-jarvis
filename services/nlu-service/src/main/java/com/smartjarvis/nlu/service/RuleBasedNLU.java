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

        // Todo operations
        patterns.put(
            Pattern.compile("(?i).*(добавь|создай|сделай|напомни).*(задач|дел|todo).*"),
            new IntentInfo("todo_create", 0.85f)
        );
        
        patterns.put(
            Pattern.compile("(?i).*(покажи|список|какие).*(задач|дел|todo).*"),
            new IntentInfo("todo_list", 0.85f)
        );

        // Home control
        patterns.put(
            Pattern.compile("(?i).*(включи|выключи|переключи).*(свет|лампа|освещение).*"),
            new IntentInfo("home_control", 0.90f)
        );
        
        patterns.put(
            Pattern.compile("(?i).*(включи|выключи|громче|тише).*(музык|звук|аудио).*"),
            new IntentInfo("home_control", 0.90f)
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
     * Intent information record
     */
    private record IntentInfo(String intent, float confidence) {}
}
