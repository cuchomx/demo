#bin/bash

echo "Create Queue Web"
aws --endpoint-url=http://localhost:9324 sqs create-queue --queue-name product-create-web

echo "Create Queue Service"
aws --endpoint-url=http://localhost:9324 sqs create-queue --queue-name product-create-service

echo "Test Create Queue"
aws --endpoint-url=http://localhost:9324 sqs get-queue-url --queue-name product-create-web
aws --endpoint-url=http://localhost:9324 sqs get-queue-url --queue-name product-create-service

echo "List Existing Queues:"
aws --endpoint-url=http://localhost:9324 sqs list-queues
