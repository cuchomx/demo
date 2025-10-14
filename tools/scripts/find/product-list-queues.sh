#!/bin/zsh

echo "List Existing Queues:"
aws --endpoint-url=http://localhost:9324 sqs list-queues

