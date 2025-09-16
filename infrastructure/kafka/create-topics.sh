#!/bin/bash

# SmartJARVIS Kafka Topics Creation Script

KAFKA_BOOTSTRAP_SERVERS=${KAFKA_BOOTSTRAP_SERVERS:-localhost:9092}

echo "🚀 Creating SmartJARVIS Kafka topics..."

# Voice Pipeline Topics
kafka-topics --bootstrap-server $KAFKA_BOOTSTRAP_SERVERS --create --topic voice.audio.incoming --partitions 3 --replication-factor 1 --if-not-exists
kafka-topics --bootstrap-server $KAFKA_BOOTSTRAP_SERVERS --create --topic voice.transcription.completed --partitions 3 --replication-factor 1 --if-not-exists
kafka-topics --bootstrap-server $KAFKA_BOOTSTRAP_SERVERS --create --topic voice.intent.recognized --partitions 3 --replication-factor 1 --if-not-exists
kafka-topics --bootstrap-server $KAFKA_BOOTSTRAP_SERVERS --create --topic voice.response.generated --partitions 3 --replication-factor 1 --if-not-exists
kafka-topics --bootstrap-server $KAFKA_BOOTSTRAP_SERVERS --create --topic voice.tts.requested --partitions 3 --replication-factor 1 --if-not-exists

# Domain Events Topics
kafka-topics --bootstrap-server $KAFKA_BOOTSTRAP_SERVERS --create --topic todo.created --partitions 3 --replication-factor 1 --if-not-exists
kafka-topics --bootstrap-server $KAFKA_BOOTSTRAP_SERVERS --create --topic todo.updated --partitions 3 --replication-factor 1 --if-not-exists
kafka-topics --bootstrap-server $KAFKA_BOOTSTRAP_SERVERS --create --topic todo.completed --partitions 3 --replication-factor 1 --if-not-exists
kafka-topics --bootstrap-server $KAFKA_BOOTSTRAP_SERVERS --create --topic todo.deleted --partitions 3 --replication-factor 1 --if-not-exists

kafka-topics --bootstrap-server $KAFKA_BOOTSTRAP_SERVERS --create --topic calendar.event.created --partitions 3 --replication-factor 1 --if-not-exists
kafka-topics --bootstrap-server $KAFKA_BOOTSTRAP_SERVERS --create --topic calendar.event.updated --partitions 3 --replication-factor 1 --if-not-exists
kafka-topics --bootstrap-server $KAFKA_BOOTSTRAP_SERVERS --create --topic calendar.event.deleted --partitions 3 --replication-factor 1 --if-not-exists
kafka-topics --bootstrap-server $KAFKA_BOOTSTRAP_SERVERS --create --topic calendar.reminder.triggered --partitions 3 --replication-factor 1 --if-not-exists

kafka-topics --bootstrap-server $KAFKA_BOOTSTRAP_SERVERS --create --topic money.transaction.created --partitions 3 --replication-factor 1 --if-not-exists
kafka-topics --bootstrap-server $KAFKA_BOOTSTRAP_SERVERS --create --topic money.transaction.updated --partitions 3 --replication-factor 1 --if-not-exists
kafka-topics --bootstrap-server $KAFKA_BOOTSTRAP_SERVERS --create --topic money.budget.exceeded --partitions 3 --replication-factor 1 --if-not-exists

# Memory and Context Topics
kafka-topics --bootstrap-server $KAFKA_BOOTSTRAP_SERVERS --create --topic memory.context.updated --partitions 3 --replication-factor 1 --if-not-exists
kafka-topics --bootstrap-server $KAFKA_BOOTSTRAP_SERVERS --create --topic memory.knowledge.stored --partitions 3 --replication-factor 1 --if-not-exists
kafka-topics --bootstrap-server $KAFKA_BOOTSTRAP_SERVERS --create --topic memory.preference.updated --partitions 3 --replication-factor 1 --if-not-exists

# Device Control Topics
kafka-topics --bootstrap-server $KAFKA_BOOTSTRAP_SERVERS --create --topic device.command.executed --partitions 3 --replication-factor 1 --if-not-exists
kafka-topics --bootstrap-server $KAFKA_BOOTSTRAP_SERVERS --create --topic device.status.changed --partitions 3 --replication-factor 1 --if-not-exists
kafka-topics --bootstrap-server $KAFKA_BOOTSTRAP_SERVERS --create --topic device.error.occurred --partitions 3 --replication-factor 1 --if-not-exists

# Home Automation Topics
kafka-topics --bootstrap-server $KAFKA_BOOTSTRAP_SERVERS --create --topic home.device.controlled --partitions 3 --replication-factor 1 --if-not-exists
kafka-topics --bootstrap-server $KAFKA_BOOTSTRAP_SERVERS --create --topic home.scene.activated --partitions 3 --replication-factor 1 --if-not-exists
kafka-topics --bootstrap-server $KAFKA_BOOTSTRAP_SERVERS --create --topic home.automation.triggered --partitions 3 --replication-factor 1 --if-not-exists

# System Events Topics
kafka-topics --bootstrap-server $KAFKA_BOOTSTRAP_SERVERS --create --topic system.user.session.started --partitions 3 --replication-factor 1 --if-not-exists
kafka-topics --bootstrap-server $KAFKA_BOOTSTRAP_SERVERS --create --topic system.user.session.ended --partitions 3 --replication-factor 1 --if-not-exists
kafka-topics --bootstrap-server $KAFKA_BOOTSTRAP_SERVERS --create --topic system.service.health.changed --partitions 3 --replication-factor 1 --if-not-exists
kafka-topics --bootstrap-server $KAFKA_BOOTSTRAP_SERVERS --create --topic system.audit.log.created --partitions 3 --replication-factor 1 --if-not-exists

echo "✅ All Kafka topics created successfully!"
echo "📊 Topic list:"
kafka-topics --bootstrap-server $KAFKA_BOOTSTRAP_SERVERS --list
