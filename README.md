# RAG Chat Storage Service
A production-ready Spring Boot microservice for storing and managing chat histories from RAG (Retrieval-Augmented Generation) based chatbot systems. Features optional integration with multiple LLM providers including Groq, OpenAI, and local models via Ollama.

🚀 Features
Session Management: Create, rename, favorite, delete, and manage chat sessions
Message Storage: Store chat messages with RAG context data and metadata
LLM Integration: Optional integration with Groq, OpenAI, and local LLMs (Ollama)
Security: API key authentication with configurable rate limiting
Caching: Redis-based caching for improved performance
Documentation: Comprehensive OpenAPI/Swagger documentation
Monitoring: Health checks, metrics, and centralized logging with Elasticsearch/Kibana
Database: PostgreSQL with optimized schema for chat data
Testing: Comprehensive unit tests using JUNIT 5 and Mockito
🏗️ Architecture
This microservice follows Clean Architecture principles with:

Core Layer: Entities, repositories, and business rules
Application Layer: Services, DTOs, and business logic
Infrastructure Layer: Security, caching, and external integrations
Presentation Layer: REST controllers and exception handling
LLM Integration Layer: Pluggable AI provider integrations
🛠️ Tech Stack
Java 21 with modern language features
Spring Boot 3.2 with Spring Security, Data JPA, WebFlux, and Caching
PostgreSQL 15 for data persistence with JSONB support
Liquibase for database schema versioning and migrations
Redis Stack for caching, rate limiting, and session management
Elasticsearch & Kibana for centralized logging and monitoring
Docker & Docker Compose for containerization
Lombok to reduce boilerplate code
MapStruct for object mapping
Bucket4J for rate limiting
📦 Installation & Setup
Prerequisites
Java 21 or higher
Docker and Docker Compose
Maven 3.8+
Quick Start with Docker
Clone and setup
git clone <repository-url>
cd rag-chat-storage-service

# Copy environment file
cp .env.example .env

# Edit .env with your configuration
# - Set your API_KEY for service authentication
# - Optional: Set GROQ_API_KEY for AI integration
Start all services
docker compose up --build -d
Access the services
Main API: http://localhost:8080/ragchat
Swagger UI: http://localhost:8080/ragchat/swagger-ui.html
Kibana (Logs): http://localhost:5601
RedisInsight: http://localhost:8001
PostgreSQL: jdbc:postgresql://localhost:5432/ragchatdb
Local Development
Start dependencies only
docker compose up postgres redis elasticsearch kibana -d
Run the application locally
./mvnw spring-boot:run
🔌 LLM Integration Configuration
Groq Integration (Recommended)
Get a free API key from console.groq.com

Update your .env file:

GROQ_API_KEY=gsk_your_actual_key_here
Configure in application.yml:
app:
  llm:
    provider: groq
    model: llama-3.1-8b-instant
Local Ollama Integration
Install Ollama from ollama.ai

Pull a model:

ollama pull llama3.1:8b
Configure local model:
app:
  llm:
    provider: local-ollama
    base-url: http://localhost:11434
    model: llama3.1:8b
Multiple Provider Support
The service supports multiple LLM providers that can be switched via configuration:

app:
  llm:
    provider: groq  # Options: groq, openai, gemini, local-ollama
    # Provider-specific settings...
📖 API Usage
Authentication
All endpoints require API key authentication:

curl -H "X-API-KEY: your-api-key" http://localhost:8080/ragchat/api/sessions
Core Chat Endpoints
Session Management
# Create a new session
curl -X POST "http://localhost:8080/ragchat/api/v1/sessions" \
  -H "X-API-KEY: your-api-key" \
  -H "Content-Type: application/json" \
  -d '{"userId": "test", "sessionName": "My Chat Session"}'

# Get a specific session
curl -X GET "http://localhost:8080/ragchat/api/v1/sessions/{sessionId}?userId=test" \
  -H "X-API-KEY: your-api-key"

# Get user sessions with pagination
curl -X GET "http://localhost:8080/ragchat/api/v1/sessions?userId=test&page=0&size=20&sortBy=createdAt&sortDir=desc" \
  -H "X-API-KEY: your-api-key"

# Get favorite sessions
curl -X GET "http://localhost:8080/ragchat/api/v1/sessions/favorites?userId=test" \
  -H "X-API-KEY: your-api-key"

# Update session details (rename)
curl -X PUT "http://localhost:8080/ragchat/api/v1/sessions/{sessionId}?userId=test" \
  -H "X-API-KEY: your-api-key" \
  -H "Content-Type: application/json" \
  -d '{"sessionName": "Updated Session Name"}'

# Toggle favorite status
curl -X PATCH "http://localhost:8080/ragchat/api/v1/sessions/{sessionId}/favorite?userId=test" \
  -H "X-API-KEY: your-api-key"

# Delete a session
curl -X DELETE "http://localhost:8080/ragchat/api/v1/sessions/{sessionId}?userId=test" \
  -H "X-API-KEY: your-api-key"
Message Management
# Send a message to a session
curl -X POST "http://localhost:8080/ragchat/api/v1/sessions/{sessionId}/messages?userId=test" \
  -H "X-API-KEY: your-api-key" \
  -H "Content-Type: application/json" \
  -d '{"content": "Hello, this is my message", "senderType": "USER", "metadata": {"context": "optional"}}'

# Get session messages with pagination
curl -X GET "http://localhost:8080/ragchat/api/v1/sessions/{sessionId}/messages?userId=test&page=0&size=50" \
  -H "X-API-KEY: your-api-key"

# Get all messages for a session
curl -X GET "http://localhost:8080/ragchat/api/v1/sessions/{sessionId}/messages/all?userId=test" \
  -H "X-API-KEY: your-api-key"

# Delete a specific message
curl -X DELETE "http://localhost:8080/ragchat/api/v1/sessions/{sessionId}/messages/{messageId}?userId=test" \
  -H "X-API-KEY: your-api-key"
Chat Endpoints with LLM Integration
# Start a new chat with AI integration
curl -X POST "http://localhost:8080/ragchat/api/v1/chat/sessions?userId=test&message=Hello&title=MyChat" \
  -H "X-API-KEY: your-api-key"

# Continue conversation
curl -X POST "http://localhost:8080/ragchat/api/v1/chat/sessions/{sessionId}?userId=test&message=Tell me more" \
  -H "X-API-KEY: your-api-key"

# Check LLM status
curl "http://localhost:8080/ragchat/api/v1/chat/status" \
  -H "X-API-KEY: your-api-key"
🔧 Configuration
Environment Variables
# Required
API_KEY=your_super_secret_api_key

# Optional LLM Integration
GROQ_API_KEY=your_groq_api_key
OPENAI_API_KEY=your_openai_key
Application Configuration
Key settings in application.yml:

app:
  security:
    api-key: ${API_KEY}
    header-name: X-API-KEY
  
  llm:
    provider: groq  # groq, openai, gemini, local-ollama
    api-key: ${GROQ_API_KEY}
    api-url: https://api.groq.com/openai/v1/chat/completions
    model: llama-3.1-8b-instant
    temperature: 0.7
    max-tokens: 1024

spring:
  datasource:
    url: jdbc:postgresql://postgres:5432/ragchatdb
  data:
    redis:
      host: redis
📊 Monitoring & Logging
Health Checks
curl http://localhost:8080/ragchat/actuator/health
Kibana Dashboard
Access http://localhost:5601
Create index pattern for logstash-* or logs-*
Explore logs in the Discover section
Metrics
Available at /ragchat/actuator/metrics including:

HTTP request metrics
Database connection pool metrics
Cache statistics
JVM metrics
Custom business metrics
🧪 Testing
# Unit tests
./mvnw test

# Integration tests (requires Docker)
./mvnw verify

# Test with coverage
./mvnw jacoco:report

# Test specific LLM integration
./mvnw test -Dtest=*Groq*
🚀 Deployment
Docker Deployment
# Build and deploy
docker compose up --build -d

# Scale services
docker compose up -d --scale app=3
🔒 Security Features
API Key Authentication: Secure access to all endpoints
Rate Limiting: Configurable rate limits per API key
Input Validation: Comprehensive request validation
SQL Injection Prevention: JPA parameterized queries
CORS Configuration: Secure cross-origin settings
HTTPS Ready: Prepared for SSL/TLS termination
🔄 Database Schema
The service uses an optimized schema for chat data with Liquibase for version-controlled database migrations:

chat_sessions: Session metadata and user information
chat_messages: Individual messages with context data (JSONB)
Automatic indexing on frequently queried fields
Efficient pagination support for large histories
Schema versioning: All database changes tracked through Liquibase changesets
Environment consistency: Automated schema deployment across dev/staging/production
💡 Future Enhancements
Short-term Improvements
Async Processing: Implement message processing queues
Advanced Caching: Cache AI responses and session data
WebSocket Support: Real-time chat capabilities
File Attachments: Support for file uploads and storage
Advanced Search: Full-text search across messages and context
Medium-term Roadmap
Multi-tenant Support: Isolation between different users/organizations
Export Functionality: PDF/JSON export of conversations
Audit Logging: Comprehensive audit trails
Performance Analytics: Chat performance metrics and dashboards
Plugin System: Extensible architecture for custom integrations
Long-term Vision
Federated Learning: On-device model training while preserving privacy
Advanced RAG Integration: Deeper integration with vector databases
Multi-modal Support: Support for images, audio, and video
AI Governance: Content moderation and compliance features
Edge Deployment: Lightweight deployment options for edge devices
🐛 Troubleshooting
Common Issues
LLM Integration Not Working

Check API keys in .env file
Verify model names are correct
Check network connectivity to LLM providers
Database Connection Issues

Verify PostgreSQL is running
Check connection string in configuration
Rate Limiting Errors

Adjust rate limit configuration
Check Redis connectivity
Getting Help
Check the following resources:

Application logs via Kibana
Swagger documentation for API details
Docker container logs for infrastructure issues
🤝 Contributing
Fork the repository
Create a feature branch (git checkout -b feature/amazing-feature)
Write tests for your changes
Ensure all tests pass
Submit a pull request
📄 License
This project is licensed under the MIT License - see the LICENSE file for details.

🙏 Acknowledgments
Built with Spring Boot and the Spring ecosystem
LLM integrations powered by Groq and Ollama
Monitoring with Elasticsearch and Kibana
Containerization with Docker
