#!/bin/bash

# SmartJARVIS Avro Schemas Registration Script

SCHEMA_REGISTRY_URL=${SCHEMA_REGISTRY_URL:-http://localhost:8081}
SCHEMAS_DIR="shared/avro-schemas"

echo "🚀 Регистрация Avro схем в Schema Registry..."

# Функция для регистрации схемы
register_schema() {
    local schema_file="$1"
    local topic_name="$2"
    
    if [ -f "$schema_file" ]; then
        echo "📝 Регистрация схемы: $(basename "$schema_file")"
        
        # Регистрируем схему
        schema_json=$(cat "$schema_file" | jq -c .)
        curl -X POST \
            -H "Content-Type: application/vnd.schemaregistry.v1+json" \
            --data "{\"schema\": $schema_json}" \
            "$SCHEMA_REGISTRY_URL/subjects/$topic_name-value/versions" \
            > /dev/null 2>&1
        
        if [ $? -eq 0 ]; then
            echo "  ✅ Схема зарегистрирована для топика: $topic_name"
        else
            echo "  ❌ Ошибка регистрации схемы для топика: $topic_name"
        fi
    else
        echo "  ⚠️ Файл схемы не найден: $schema_file"
    fi
}

# Регистрируем схемы для голосовых событий
register_schema "$SCHEMAS_DIR/voice/AudioIncomingEvent.avsc" "voice.audio.incoming"
register_schema "$SCHEMAS_DIR/voice/TranscriptionCompletedEvent.avsc" "voice.transcription.completed"
register_schema "$SCHEMAS_DIR/voice/IntentRecognizedEvent.avsc" "voice.intent.recognized"
register_schema "$SCHEMAS_DIR/voice/ResponseGeneratedEvent.avsc" "voice.response.generated"

# Регистрируем схемы для задач
register_schema "$SCHEMAS_DIR/todo/TodoCreatedEvent.avsc" "todo.created"
register_schema "$SCHEMAS_DIR/todo/TodoUpdatedEvent.avsc" "todo.updated"
register_schema "$SCHEMAS_DIR/todo/TodoCompletedEvent.avsc" "todo.completed"

# Регистрируем схемы для календаря
register_schema "$SCHEMAS_DIR/calendar/EventCreatedEvent.avsc" "calendar.event.created"

# Регистрируем схемы для финансов
register_schema "$SCHEMAS_DIR/money/TransactionCreatedEvent.avsc" "money.transaction.created"

# Регистрируем схемы для памяти
register_schema "$SCHEMAS_DIR/memory/ContextUpdatedEvent.avsc" "memory.context.updated"

# Регистрируем схемы для устройств
register_schema "$SCHEMAS_DIR/device/DeviceCommandExecutedEvent.avsc" "device.command.executed"

# Регистрируем схемы для умного дома
register_schema "$SCHEMAS_DIR/home/DeviceControlledEvent.avsc" "home.device.controlled"

# Регистрируем схемы для системных событий
register_schema "$SCHEMAS_DIR/system/UserSessionStartedEvent.avsc" "system.user.session.started"

echo ""
echo "📊 Список зарегистрированных схем:"
curl -s "$SCHEMA_REGISTRY_URL/subjects" | jq -r '.[]' 2>/dev/null || echo "Ошибка получения списка схем"

echo ""
echo "✅ Регистрация схем завершена!"
