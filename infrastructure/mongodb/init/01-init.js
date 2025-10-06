// SmartJARVIS MongoDB Initialization
// Создание коллекций и индексов для MongoDB

// Переключение на базу данных smartjarvis
db = db.getSiblingDB('smartjarvis');

// Создание пользователя для приложения
db.createUser({
  user: 'jarvis_app',
  pwd: 'jarvis_app_secret_2024',
  roles: [
    {
      role: 'readWrite',
      db: 'smartjarvis'
    }
  ]
});

// Коллекция для аудио данных
db.createCollection('audio_chunks', {
  validator: {
    $jsonSchema: {
      bsonType: 'object',
      required: ['sessionId', 'userId', 'audioData', 'timestamp'],
      properties: {
        sessionId: {
          bsonType: 'string',
          description: 'Session ID is required and must be a string'
        },
        userId: {
          bsonType: 'string',
          description: 'User ID is required and must be a string'
        },
        audioData: {
          bsonType: 'binData',
          description: 'Audio data is required and must be binary'
        },
        timestamp: {
          bsonType: 'date',
          description: 'Timestamp is required and must be a date'
        },
        chunkIndex: {
          bsonType: 'int',
          description: 'Chunk index must be an integer'
        },
        sampleRate: {
          bsonType: 'int',
          description: 'Sample rate must be an integer'
        },
        channels: {
          bsonType: 'int',
          description: 'Number of channels must be an integer'
        }
      }
    }
  }
});

// Коллекция для контекста диалогов
db.createCollection('dialog_context', {
  validator: {
    $jsonSchema: {
      bsonType: 'object',
      required: ['sessionId', 'userId', 'context'],
      properties: {
        sessionId: {
          bsonType: 'string',
          description: 'Session ID is required and must be a string'
        },
        userId: {
          bsonType: 'string',
          description: 'User ID is required and must be a string'
        },
        context: {
          bsonType: 'object',
          description: 'Context object is required'
        },
        lastUpdated: {
          bsonType: 'date',
          description: 'Last updated timestamp must be a date'
        }
      }
    }
  }
});

// Коллекция для истории команд
db.createCollection('command_history', {
  validator: {
    $jsonSchema: {
      bsonType: 'object',
      required: ['userId', 'command', 'timestamp'],
      properties: {
        userId: {
          bsonType: 'string',
          description: 'User ID is required and must be a string'
        },
        command: {
          bsonType: 'string',
          description: 'Command text is required and must be a string'
        },
        timestamp: {
          bsonType: 'date',
          description: 'Timestamp is required and must be a date'
        },
        intent: {
          bsonType: 'string',
          description: 'Intent must be a string'
        },
        entities: {
          bsonType: 'array',
          description: 'Entities must be an array'
        },
        response: {
          bsonType: 'string',
          description: 'Response must be a string'
        },
        success: {
          bsonType: 'bool',
          description: 'Success flag must be a boolean'
        }
      }
    }
  }
});

// Коллекция для пользовательских настроек
db.createCollection('user_preferences', {
  validator: {
    $jsonSchema: {
      bsonType: 'object',
      required: ['userId', 'preferences'],
      properties: {
        userId: {
          bsonType: 'string',
          description: 'User ID is required and must be a string'
        },
        preferences: {
          bsonType: 'object',
          description: 'Preferences object is required'
        },
        lastUpdated: {
          bsonType: 'date',
          description: 'Last updated timestamp must be a date'
        }
      }
    }
  }
});

// Коллекция для логов событий
db.createCollection('event_logs', {
  validator: {
    $jsonSchema: {
      bsonType: 'object',
      required: ['eventType', 'userId', 'timestamp'],
      properties: {
        eventType: {
          bsonType: 'string',
          description: 'Event type is required and must be a string'
        },
        userId: {
          bsonType: 'string',
          description: 'User ID is required and must be a string'
        },
        timestamp: {
          bsonType: 'date',
          description: 'Timestamp is required and must be a date'
        },
        data: {
          bsonType: 'object',
          description: 'Event data must be an object'
        },
        sessionId: {
          bsonType: 'string',
          description: 'Session ID must be a string'
        },
        correlationId: {
          bsonType: 'string',
          description: 'Correlation ID must be a string'
        }
      }
    }
  }
});

// Создание индексов для производительности
db.audio_chunks.createIndex({ "sessionId": 1, "timestamp": 1 });
db.audio_chunks.createIndex({ "userId": 1, "timestamp": 1 });
db.audio_chunks.createIndex({ "timestamp": 1 }, { expireAfterSeconds: 86400 }); // TTL 24 часа

db.dialog_context.createIndex({ "sessionId": 1 }, { unique: true });
db.dialog_context.createIndex({ "userId": 1 });
db.dialog_context.createIndex({ "lastUpdated": 1 }, { expireAfterSeconds: 604800 }); // TTL 7 дней

db.command_history.createIndex({ "userId": 1, "timestamp": -1 });
db.command_history.createIndex({ "timestamp": 1 }, { expireAfterSeconds: 2592000 }); // TTL 30 дней
db.command_history.createIndex({ "intent": 1 });

db.user_preferences.createIndex({ "userId": 1 }, { unique: true });

db.event_logs.createIndex({ "userId": 1, "timestamp": -1 });
db.event_logs.createIndex({ "eventType": 1, "timestamp": -1 });
db.event_logs.createIndex({ "timestamp": 1 }, { expireAfterSeconds: 7776000 }); // TTL 90 дней
db.event_logs.createIndex({ "sessionId": 1 });
db.event_logs.createIndex({ "correlationId": 1 });

// Создание текстового индекса для поиска
db.command_history.createIndex({ 
  "command": "text", 
  "response": "text" 
});

print('MongoDB initialization completed successfully!');
