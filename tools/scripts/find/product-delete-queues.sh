#bin/bash

aws --endpoint-url=http://localhost:9324 sqs delete-queue --queue-url http://localhost:9324/000000000000/product-create-web
aws --endpoint-url=http://localhost:9324 sqs delete-queue --queue-url http://localhost:9324/000000000000/product-create-service

echo "List Existing Queues:"
aws --endpoint-url=http://localhost:9324 sqs list-queues
