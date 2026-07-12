# Clarity - Career Roadmap & Skill Tracking Application

A comprehensive AI-powered learning platform that guides users through personalized career roadmaps, tracks skill development, and provides intelligent assessments.

## Features

### 🎯 Goal & Roadmap Management
- **Career Goals**: Create and manage career goals (e.g., "Java Developer")
- **AI-Generated Roadmaps**: Automatically generate structured learning paths using AI
- **Hierarchical Topics**: Support parent-child topic relationships for better organization
- **Custom Sequencing**: Organize topics in a logical learning order

### 📊 Progress Tracking
- **Skill Coverage**: Track proficiency percentage across all topics (0-100%)
- **Proficiency Levels**: Categorize skills as Beginner, Proficient, or Mastered
- **Progress Analytics**: View coverage percentages for roadmaps and individual topics
- **Last Assessment Tracking**: Record when topics were last assessed

### 🤖 AI-Powered Assessment
- **Intelligent Questions**: Generate contextual assessment questions using AI
- **Answer Evaluation**: AI evaluates answers against expected keywords and scoring criteria
- **Cross-Questions**: Targeted questions to assess specific topic comprehension
- **Auto-Scoring**: Automatic proficiency calculation based on assessment results

### 💻 Project Management
- **Project Submissions**: Submit completed projects with links and descriptions
- **AI Assessment**: Get AI-powered feedback on submitted projects
- **Progress Integration**: Link projects to topics for skill verification

### 💬 Chat Integration
- **AI Assistant**: Chat with an AI assistant about learning paths
- **Real-time Guidance**: Get recommendations and clarifications

## Project Structure

```
src/main/java/com/example/chatbot/
├── learning/                          # Core learning module
│   ├── Goal.java                     # Career goal entity
│   ├── Topic.java                    # Learning topic with hierarchy
│   ├── Roadmap.java                  # Roadmap entity
│   ├── RoadmapTopic.java             # Join table for roadmap-topic
│   ├── UserTopicProgress.java        # Tracks user proficiency
│   ├── ProjectSubmission.java        # Project submission entity
│   ├── *Repository.java              # Data access layer
│   ├── RoadmapService.java           # Roadmap generation & management
│   ├── ProgressService.java          # Progress calculation & tracking
│   ├── AssessmentService.java        # AI assessment logic
│   ├── *Controller.java              # REST API endpoints
│
├── ai/
│   ├── AIService.java                # AI integration service
│   └── GeminiClient.java             # Gemini API client
│
├── user/                              # User management
├── auth/                              # Authentication & JWT
├── session/                           # Chat session management
└── config/                            # Security configuration
```

## Database Schema

### Core Tables
- `goals` - User career goals
- `topics` - Learning topics with hierarchy support (parent_id)
- `roadmaps` - Learning roadmaps linked to goals
- `roadmap_topics` - Join table mapping topics to roadmaps with ordering
- `user_topic_progress` - Tracks proficiency for each user-topic pair
- `project_submissions` - User-submitted projects

## API Endpoints

### Goals Endpoints
```
POST   /goals/create              Create a new career goal
GET    /goals/{goalId}            Get goal details
GET    /goals/user/{userId}       Get all goals for a user
DELETE /goals/{goalId}            Delete a goal
```

### Roadmap Endpoints
```
POST   /roadmaps/generate              Generate AI roadmap for goal
GET    /roadmaps/{roadmapId}           Get roadmap details
POST   /roadmaps/{roadmapId}/topics    Add topic to roadmap
DELETE /roadmaps/topics/{roadmapTopicId}  Remove topic from roadmap
GET    /roadmaps/{roadmapId}/progress  Get roadmap progress
GET    /roadmaps/{roadmapId}/next-topics  Get recommended next topics
```

### Progress Endpoints
```
POST   /progress/update-topic                Update topic proficiency
GET    /progress/user/{userId}                Get all user progress
GET    /progress/user/{userId}/coverage      Get skill coverage report
GET    /progress/roadmap/{roadmapId}         Get roadmap progress
GET    /progress/{topicId}/user/{userId}    Get specific topic progress
```

### Topic Endpoints
```
POST   /topics/create                Create a new topic
GET    /topics/{topicId}             Get topic details
GET    /topics/slug/{slug}           Get topic by slug
GET    /topics/{topicId}/subtopics   Get subtopics
GET    /topics/root                  Get root topics
GET    /topics/all                   Get all topics
PUT    /topics/{topicId}             Update topic
DELETE /topics/{topicId}             Delete topic
```

### Assessment Endpoints
```
POST   /assessments/generate-question  Generate assessment question
POST   /assessments/evaluate-answer    Evaluate user's answer
POST   /assessments/complete-assessment Complete full assessment
GET    /assessments/recommended-topics Get recommended topics for learning
```

### Project Endpoints
```
POST   /projects/submit              Submit project
POST   /projects/{projectId}/assess  Assess project with score & notes
GET    /projects/user/{userId}       Get user's projects
GET    /projects/{projectId}         Get project details
DELETE /projects/{projectId}         Delete project
```

## Data Models

### Goal
```json
{
  "id": "uuid",
  "userId": "uuid",
  "name": "Java Developer",
  "description": "Become proficient in Java development",
  "createdAt": "2024-01-15T10:00:00"
}
```

### Topic (with Hierarchy)
```json
{
  "id": "uuid",
  "parentId": "uuid",  // Optional parent topic
  "slug": "java-collections",
  "title": "Collections Framework",
  "description": "Learn Java collections API",
  "difficulty": "INTERMEDIATE",
  "estimatedHours": 10,
  "ordering": 0,
  "subtopics": []  // Child topics
}
```

### Roadmap
```json
{
  "id": "uuid",
  "goalId": "uuid",
  "name": "Java Developer Roadmap",
  "roadmapTopics": [
    {
      "id": "uuid",
      "topic": { ... },
      "ordering": 0
    }
  ]
}
```

### User Topic Progress
```json
{
  "userId": "uuid",
  "topicId": "uuid",
  "proficiency": 85.0,  // 0-100%
  "lastAssessedAt": "2024-01-20T14:30:00",
  "notes": "Assessment score: 85%"
}
```

### Skill Coverage Report
```json
{
  "userId": "uuid",
  "totalTopicsTracked": 15,
  "masteredTopics": 5,      // 80%+
  "proficientTopics": 8,    // 50-79%
  "beginnerTopics": 2,      // <50%
  "overallSkillPercentage": 72.5
}
```

## Getting Started

### Prerequisites
- Java 17+
- PostgreSQL 12+
- Maven 3.8+
- Google Gemini API key

### Setup

1. **Clone and build**
```bash
cd chatBot
mvn clean install
```

2. **Configure database** (application.yml)
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/clarity
    username: postgres
    password: your-password
  jpa:
    hibernate:
      ddl-auto: validate
```

3. **Set AI credentials**
```yaml
gemini:
  api-key: YOUR_GEMINI_API_KEY
```

4. **Run migrations**
   - Flyway automatically runs: V1__init_schema.sql, V2__session_and_messages.sql, V3__roadmap_and_progress.sql

5. **Start application**
```bash
mvn spring-boot:run
```

6. **Access UI**
   - Open http://localhost:8080/login.html
   - Register a new account
   - Start creating goals and roadmaps!

## Usage Examples

### 1. Create a Career Goal
```javascript
POST /goals/create?userId=<user-uuid>
{
  "name": "Java Developer",
  "description": "Become a proficient Java backend developer"
}
```

### 2. Generate AI Roadmap
```javascript
POST /roadmaps/generate?goalId=<goal-uuid>
// Returns roadmap with AI-generated topic sequence
```

### 3. Assess a Topic
```javascript
POST /assessments/generate-question
{
  "topic": "Java Collections",
  "userContext": "User is learning arrays and lists"
}
// Response includes question, difficulty, keywords, explanation
```

### 4. Track Progress
```javascript
POST /progress/update-topic?userId=<user-uuid>&topicId=<topic-uuid>
{
  "proficiency": 85.0,
  "notes": "Completed collections quiz with 85%"
}
```

### 5. Get Coverage Report
```javascript
GET /progress/user/<user-uuid>/coverage
// Returns skill coverage across all topics
```

## Frontend Features

### Dashboard
- Total active goals
- Overall skill coverage percentage
- Mastered topics count
- Quick stats overview

### Goals Section
- Create new career goals
- View all goals with descriptions
- Generate roadmaps for goals
- Track associated roadmaps

### Roadmap View
- Visual topic sequence
- Topic difficulty levels
- Estimated hours per topic
- Direct assessment links

### Progress Tracking
- Overall skill coverage
- Per-topic proficiency scores
- Roadmap-specific progress
- Mastered/Proficient/Beginner breakdown

### Assessment Flow
- Select topic to assess
- AI-generated contextual questions
- Answer submission
- Score and feedback
- Explanation display

### Project Submissions
- Submit project with link
- Get AI assessment
- Track assessment scores
- View feedback notes

##Key Features Implementation

### AI-Powered Roadmap Generation
The system uses Google Gemini API to:
- Analyze career goals
- Generate optimal learning sequences
- Create contextual assessment questions
- Evaluate user answers

### Hierarchical Topic Organization
Topics support parent-child relationships:
```
Java Developer
├── Collections Framework
│   ├── Lists
│   ├── Sets
│   └── Maps
├── Concurrency
│   ├── Threads
│   └── Synchronization
└── Spring Framework
    ├── Dependency Injection
    └── REST APIs
```

### Progress Calculation
- **Proficiency**: 0-100% score per topic from assessments
- **Coverage**: % of topics with proficiency ≥ 70%
- **Skill Levels**:
  - Beginner: < 50%
  - Proficient: 50-79%
  - Mastered: 80%+

### Cross-Question Assessment
- AI generates contextual questions
- Expected keywords define correct answers
- AI evaluates student answers against keywords
- Automatic scoring and feedback

## Architecture Highlights

### Services Layer
- **RoadmapService**: Generates and manages roadmaps
- **ProgressService**: Calculates proficiency and coverage metrics
- **AssessmentService**: Manages AI-powered assessments

### Controllers Layer
- Clean REST API with consistent response formats
- Exception handling with meaningful error messages
- CORS enabled for frontend integration

### Data Access Layer
- JPA repositories with custom queries
- Transactional operations for data consistency
- Support for complex filtering and sorting

## Technology Stack

- **Backend**: Spring Boot 3.x
- **Database**: PostgreSQL with Flyway migrations
- **AI**: Google Gemini API
- **Frontend**: HTML5, CSS3, Vanilla JavaScript
- **Authentication**: JWT tokens
- **ORM**: Hibernate/JPA

## Future Enhancements

- [ ] Machine learning for personalized learning paths
- [ ] Video tutorial integration
- [ ] Peer code review system
- [ ] Community challenges
- [ ] Mobile app
- [ ] Real-time collaboration features
- [ ] Advanced analytics dashboard
- [ ] Certificate generation

## Contributing

1. Follow the existing project structure
2. Write unit tests for new features
3. Use meaningful commit messages
4. Create pull requests for review

## License

MIT License

## Support

For issues and questions:
1. Check existing documentation
2. Review API endpoint examples
3. Consult database schema
4. Contact support team
