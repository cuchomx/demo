#bin/bash

echo "List Existing Queues:"
aws --endpoint-url=http://localhost:9324 sqs list-queues

echo "Create Queue"
aws --endpoint-url=http://localhost:9324 sqs create-queue --queue-name test-queue

echo "Test Queue"
aws --endpoint-url=http://localhost:9324 sqs get-queue-url --queue-name test-queue

echo "Send message"
aws --endpoint-url=http://localhost:9324 sqs send-message --queue-url http://localhost:9324/queue/test-queue --message-body "Hello World!"

echo "Receive message"
aws --endpoint-url=http://localhost:9324 sqs receive-message --queue-url http://localhost:9324/queue/test-queue

echo "Delete Queue"
aws --endpoint-url=http://localhost:9324 sqs delete-queue --queue-url http://localhost:9324/queue/test-queue

echo "Show messages on queue"
aws --endpoint-url=http://localhost:9324 sqs help