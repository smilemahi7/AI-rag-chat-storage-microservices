#!/bin/bash
# generate-api-key.sh

echo "Generating secure API key..."
API_KEY=$(openssl rand -hex 32)

echo "Your API key: $API_KEY"
echo ""
echo "Add this to your .env file:"
echo "API_KEY=$API_KEY"

# Automatically update .env if it exists
if [ -f .env ]; then
    sed -i.bak "s/API_KEY=.*/API_KEY=$API_KEY/" .env
    echo "✅ Updated .env file"
else
    echo "API_KEY=$API_KEY" > .env
    echo "✅ Created .env file"
fi