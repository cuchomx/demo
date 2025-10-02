#bin/bash

echo "Receive message"
aws --endpoint-url=http://localhost:9324 sqs receive-message --queue-url http://localhost:9324/000000000000/product-create-service --attribute-names All --message-attribute-names All

