#!/bin/zsh

echo "Delete Queues:"
aws --endpoint-url=http://localhost:9324 sqs delete-queue --queue-url http://localhost:9324/000000000000/product-find-web
aws --endpoint-url=http://localhost:9324 sqs delete-queue --queue-url http://localhost:9324/000000000000/product-find-service

echo "Create Queues"
aws --endpoint-url=http://localhost:9324 sqs create-queue --queue-name product-find-web
aws --endpoint-url=http://localhost:9324 sqs create-queue --queue-name product-find-service

echo "List Existing Queues:"
aws --endpoint-url=http://localhost:9324 sqs list-queues

