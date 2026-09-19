-- Add tables for goals, topics, roadmaps, progress and project submissions

CREATE TABLE goals (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_goal_user
        FOREIGN KEY(user_id)
        REFERENCES users(id)
        ON DELETE CASCADE
);

CREATE TABLE topics (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    parent_id UUID,
    slug VARCHAR(255) UNIQUE NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    difficulty VARCHAR(50),
    estimated_hours INTEGER,
    ordering INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_topic_parent
        FOREIGN KEY(parent_id)
        REFERENCES topics(id)
        ON DELETE CASCADE
);

CREATE TABLE roadmaps (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    goal_id UUID NOT NULL,
    name VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_roadmap_goal
        FOREIGN KEY(goal_id)
        REFERENCES goals(id)
        ON DELETE CASCADE
);

CREATE TABLE roadmap_topics (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    roadmap_id UUID NOT NULL,
    topic_id UUID NOT NULL,
    ordering INTEGER DEFAULT 0,
    notes TEXT,
    CONSTRAINT fk_rt_roadmap
        FOREIGN KEY(roadmap_id)
        REFERENCES roadmaps(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_rt_topic
        FOREIGN KEY(topic_id)
        REFERENCES topics(id)
        ON DELETE CASCADE
);

CREATE TABLE user_topic_progress (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL,
    topic_id UUID NOT NULL,
    proficiency DOUBLE PRECISION DEFAULT 0,
    last_assessed_at TIMESTAMP,
    notes TEXT,
    CONSTRAINT fk_utp_user
        FOREIGN KEY(user_id)
        REFERENCES users(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_utp_topic
        FOREIGN KEY(topic_id)
        REFERENCES topics(id)
        ON DELETE CASCADE
);

CREATE TABLE project_submissions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    link VARCHAR(1024),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    assessment_score INTEGER,
    assessment_notes TEXT,
    CONSTRAINT fk_ps_user
        FOREIGN KEY(user_id)
        REFERENCES users(id)
        ON DELETE CASCADE
);
